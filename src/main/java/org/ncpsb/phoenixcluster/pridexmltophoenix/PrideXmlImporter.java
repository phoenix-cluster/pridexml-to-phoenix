package org.ncpsb.phoenixcluster.pridexmltophoenix;

import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import org.ncpsb.phoenixcluster.pridexmltophoenix.model.PSMRow;
import org.ncpsb.phoenixcluster.pridexmltophoenix.model.SpectrumRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessController;
import uk.ac.ebi.pride.utilities.data.core.*;
import uk.ac.ebi.pride.utilities.data.utils.CvUtilities;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by baimi on 2017/11/7.
 */
public class PrideXmlImporter {

    protected static Logger logger = (Logger) LoggerFactory.getLogger(PrideXmlImporter.class);
    private DataAccessController controller;
    private File inputFile;
    private String projectId;
    private String relativeFileName;
    private List<SpectrumRow> spectrumRows;
    private List<PSMRow> psmRows;

    public PrideXmlImporter(DataAccessController controller, File inputFile, String projectId) {
        this.controller = controller;
        this.inputFile = inputFile;
        this.projectId = projectId;
        this.spectrumRows = new ArrayList<SpectrumRow>();
        this.psmRows = new ArrayList<PSMRow>();
        retriveFromPrideXml();
    }

    private void retriveFromPrideXml(){
        this.relativeFileName = inputFile.getName();
        ExperimentMetaData exp = controller.getExperimentMetaData();
        String acc = (exp.getId() !=null)?exp.getId().toString():null; // Experiment accession
        loadPeptidesAndSpectra(this.controller);
    }

    public void importToPhoenix() {
        try {
            persistToPhoenix();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadPeptidesAndSpectra(DataAccessController controller) {
        List <Spectrum> spectra = new ArrayList<Spectrum>();
        ExperimentMetaData experimentMetaData = controller.getExperimentMetaData();
        Collection<Comparable> spectrumIds = controller.getSpectrumIds();
        for (Comparable spectrumId : spectrumIds) {
            Spectrum spectrum = controller.getSpectrumById(spectrumId);
            int msLevel = controller.getSpectrumMsLevel(spectrumId);

            if (msLevel == 2) {
                spectra.add(spectrum);
                Integer charge = controller.getSpectrumPrecursorCharge(spectrumId);
                double precursorMz = controller.getSpectrumPrecursorMz(spectrumId);
                double precursorIntens = controller.getSpectrumPrecursorIntensity(spectrumId);

                //get both arrays
                double[] mzBinaryArray = spectrum.getMzBinaryDataArray().getDoubleArray();
                double[] intensityArray = spectrum.getIntensityBinaryDataArray().getDoubleArray();

                SpectrumRow spectrumRow = new SpectrumRow();
                spectrumRow.setSpectrumTitle(this.projectId + ";" + this.relativeFileName + ";" + "spectrum=" + spectrumId);
                spectrumRow.setCharge(charge);
                spectrumRow.setPrecursorMz(precursorMz);
                spectrumRow.setPrecursorIntens(precursorIntens);
                spectrumRow.setPeaklistMz(Utils.doubleArrayToString(mzBinaryArray));
                spectrumRow.setPeaklistIntens(Utils.doubleArrayToString(intensityArray));

                this.spectrumRows.add(spectrumRow);

//                List<Peptide> peptideIds = controller.getPeptidesBySpectrum(spectrumId);
//                for (Peptide peptideItem : peptideIds) {
//                    PSMRow psmRow = new PSMRow();
//                    psmRow.setSpectrumTitle(this.projectId + ";" + this.relativeFileName + ";" + "spectrum=" + spectrumId);
//                    psmRow.setPeptideSequence(peptideItem.getSequence());
//                    List<Modification> mods = peptideItem.getModifications();
//                    String modsString = getStringFromModList(mods);
//                    psmRow.setModifications(modsString);
//                    this.psmRows.add(psmRow);
//            }
            }
        }
//        List<Peptide> peptides = new ArrayList<Peptide>();
        HashSet<String> psmSet = new HashSet<>();
        Collection<Comparable> proteinIds = controller.getProteinIds();

        for (Comparable proteinId : proteinIds) {
            Protein identification = controller.getProteinById(proteinId);
            if (CvUtilities.isDecoyHit(identification) || CvUtilities.isAccessionDecoy(identification)) {
                continue;
            }

            for (Peptide peptideItem : identification.getPeptides()) {
                Comparable spectrumId = peptideItem.getSpectrumIdentification().getSpectrum().getId();
                PSMRow psmRow = new PSMRow();
                psmRow.setSpectrumTitle(this.projectId + ";" + this.relativeFileName + ";" + "spectrum=" + spectrumId);
                psmRow.setPeptideSequence(peptideItem.getSequence());
                List<Modification> mods = peptideItem.getModifications();
                String modsString = getStringFromModList(mods);
                psmRow.setModifications(modsString);
                if (psmSet.contains(psmRow.toString()))
                    continue;
                else
                    this.psmRows.add(psmRow);
                    psmSet.add(psmRow.toString());
            }
        }

//        int n = controller.getNumberOfPeptides();
    }

    //todo: consider multiple modifications in one location
    private String getStringFromModList(List<Modification> mods) {
        StringBuffer modsStringB = new StringBuffer();
        for (Modification mod : mods){
            int loc = mod.getLocation();
            Comparable id = mod.getId();
            modsStringB.append(loc);
            modsStringB.append('-');
            modsStringB.append(id);
            modsStringB.append(",");
        }
        if(modsStringB.length() > 1)
            modsStringB.deleteCharAt(modsStringB.length() - 1);
        return modsStringB.toString();
    }

    private void persistToPhoenix() throws ClassNotFoundException, SQLException {
        Connection conn;
        Properties prop = new Properties();
        Class.forName("org.apache.phoenix.jdbc.PhoenixDriver");
        conn =  DriverManager.getConnection("jdbc:phoenix:192.168.6.20:2181:/hbase", prop);
        conn.setAutoCommit(true);

        String spectrumTableName = "t_spectrum";

        String createSpectrumTableSql = "CREATE TABLE IF NOT EXISTS \"" + spectrumTableName.toUpperCase() + "\" (" +
                "spectrum_title VARCHAR NOT NULL PRIMARY KEY ," +
                "precursor_mz FLOAT," +
                "precursor_intens FLOAT," +
                "charge INTEGER," +
                "peaklist_mz VARCHAR," +
                "peaklist_intens VARCHAR" +
                ")";

        String psmTableName = "t_" + this.projectId + "_psm";

        String createPsmTableSql = "CREATE TABLE IF NOT EXISTS \"" + psmTableName.toUpperCase() + "\" (" +
                "spectrum_title VARCHAR NOT NULL PRIMARY KEY ," +
                "peptide_sequence VARCHAR," +
                "modifications VARCHAR" +
                ")";

        logger.debug(createPsmTableSql);
        boolean returnStatus = conn.createStatement().execute(createSpectrumTableSql);
        logger.debug(String.valueOf(returnStatus));

        logger.debug(createPsmTableSql);
        boolean returnStatus2 = conn.createStatement().execute(createPsmTableSql);
        logger.debug(String.valueOf(returnStatus2));

        for (PSMRow psmRow : this.psmRows) {
            StringBuffer sqlStr = new StringBuffer();
            sqlStr.append("upsert into " + psmTableName.toUpperCase() + " values (");
            sqlStr.append("'" + psmRow.getSpectrumTitle() + "',");
            sqlStr.append("'" + psmRow.getPeptideSequence()+ "',");
            sqlStr.append("'" + psmRow.getModifications()+ "'");
            sqlStr.append(")");
            conn.createStatement().executeUpdate(sqlStr.toString());
        }

        for (SpectrumRow spectrumRow : this.spectrumRows) {
            StringBuffer sqlStr = new StringBuffer();
            sqlStr.append("upsert into " + spectrumTableName.toUpperCase() + " values (");
            sqlStr.append("'" + spectrumRow.getSpectrumTitle() + "',");
            sqlStr.append(spectrumRow.getPrecursorMz() + ",");
            sqlStr.append(spectrumRow.getPrecursorIntens() + ",");
            sqlStr.append(spectrumRow.getCharge() + ",");
            sqlStr.append("'" + spectrumRow.getPeaklistMz() + "',");
            sqlStr.append("'" + spectrumRow.getPeaklistIntens() + "'");
            sqlStr.append(")");
            conn.createStatement().executeUpdate(sqlStr.toString());
        }

        conn.commit();
    }


    public void persistToCsv(String projectId) throws ClassNotFoundException, IOException, CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {

        File psmFile = new File(projectId + "_psm.csv");
        File specFile = new File(projectId + "_spec.csv");
//        String spectrumTableName = "t_spectrum";
//
//        String createSpectrumTableSql = "CREATE TABLE IF NOT EXISTS \"" + spectrumTableName.toUpperCase() + "\" (" +
//                "spectrum_title VARCHAR NOT NULL PRIMARY KEY ," +
//                "precursor_mz FLOAT," +
//                "precursor_intens FLOAT," +
//                "charge INTEGER," +
//                "peaklist_mz VARCHAR," +
//                "peaklist_intens VARCHAR" +
//                ")";
//
//        String psmTableName = "t_" + this.projectId + "_psm";
//
//        String createPsmTableSql = "CREATE TABLE IF NOT EXISTS \"" + psmTableName.toUpperCase() + "\" (" +
//                "spectrum_title VARCHAR NOT NULL PRIMARY KEY ," +
//                "peptide_sequence VARCHAR," +
//                "modifications VARCHAR" +
//                ")";

        Writer psmWriter = new FileWriter(psmFile, true);// true for append mode
        StatefulBeanToCsv psmBeanToCsv = new StatefulBeanToCsvBuilder(psmWriter).build();
        psmBeanToCsv.write(this.psmRows);
        System.out.println(this.psmRows.size() + " psm rows have been imported");
        psmWriter.close();

        Writer specWriter = new FileWriter(specFile, true);// true for append mode
        StatefulBeanToCsv specBeanToCsv = new StatefulBeanToCsvBuilder(specWriter).build();
        specBeanToCsv.write(this.spectrumRows);
        System.out.println(this.spectrumRows.size() + " spec rows have been imported");
        specWriter.close();

//        for (PSMRow psmRow : this.psmRows) {
//            StringBuffer sqlStr = new StringBuffer();
//            sqlStr.append("upsert into " + psmTableName.toUpperCase() + " values (");
//            sqlStr.append("'" + psmRow.getSpectrumTitle() + "',");
//            sqlStr.append("'" + psmRow.getPeptideSequence() + "',");
//            sqlStr.append("'" + psmRow.getModifications() + "'");
//            sqlStr.append(")");
//        }
//
//        for (SpectrumRow spectrumRow : this.spectrumRows) {
//            StringBuffer sqlStr = new StringBuffer();
//            sqlStr.append("upsert into " + spectrumTableName.toUpperCase() + " values (");
//            sqlStr.append("'" + spectrumRow.getSpectrumTitle() + "',");
//            sqlStr.append(spectrumRow.getPrecursorMz() + ",");
//            sqlStr.append(spectrumRow.getPrecursorIntens() + ",");
//            sqlStr.append(spectrumRow.getCharge() + ",");
//            sqlStr.append("'" + spectrumRow.getPeaklistMz() + "',");
//            sqlStr.append("'" + spectrumRow.getPeaklistIntens() + "'");
//            sqlStr.append(")");
//        }

    }

}
