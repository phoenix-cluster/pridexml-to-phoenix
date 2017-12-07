package org.ncpsb.phoenixcluster.pridexmltophoenix;

import org.ncpsb.phoenixcluster.pridexmltophoenix.model.PSMRow;
import org.ncpsb.phoenixcluster.pridexmltophoenix.model.SpectrumRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessController;
import uk.ac.ebi.pride.utilities.data.core.*;
import uk.ac.ebi.pride.utilities.data.utils.CvUtilities;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

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
    }

    public void importToPhoenix(File inputFile) {


        this.relativeFileName = inputFile.getName();
        ExperimentMetaData exp = controller.getExperimentMetaData();

        String acc = (exp.getId() !=null)?exp.getId().toString():null; // Experiment accession


        loadPeptidesAndSpectra(this.controller);
        try {
            persisToPhoenix();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadPeptidesAndSpectra(DataAccessController controller) {
        List <Spectrum> spectra = new ArrayList<Spectrum>();
        Collection<Comparable> spectrumIds = controller.getSpectrumIds();
        for (Comparable spectrumId : spectrumIds) {
            Spectrum spectrum= controller.getSpectrumById(spectrumId);
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
            }
        }


        List<Peptide> peptides = new ArrayList<Peptide>();
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
                this.psmRows.add(psmRow);
            }
        }

//        int n = controller.getNumberOfPeptides();
    }

    //todo: consider multiple modifications in one location
    private String getStringFromModList(List<Modification> mods) {
        StringBuffer modsStringB = new StringBuffer();
        for (Modification mod : mods){
            int loc = mod.getLocation();
            List<String> residues = mod.getResidues();
            List<Double> massDeltaList = mod.getAvgMassDelta();
            modsStringB.append(loc);
            modsStringB.append(",");
            modsStringB.append(residues.get(0));
            modsStringB.append(",");
            modsStringB.append(massDeltaList.get(0));
            modsStringB.append(";");
//            StringBuffer massDeltaStringB = new StringBuffer();
//            for (Double massDelta : massDeltaList) {
//                massDeltaStringB.append(massDelta);
//                massDeltaStringB.append("|");
//            }
//            massDeltaStringB.deleteCharAt(massDeltaStringB.length() - 1);
        }
        return modsStringB.toString();
    }

    private void persisToPhoenix() throws ClassNotFoundException, SQLException {
        Connection conn;
        Properties prop = new Properties();
        Class.forName("org.apache.phoenix.jdbc.PhoenixDriver");
        conn =  DriverManager.getConnection("jdbc:phoenix:192.168.6.20:2181:/hbase");

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
                "peptide_sequence VARCHAR" +
                ")";


        boolean returnStatus = conn.createStatement().execute(createSpectrumTableSql);
        boolean returnStatus2 = conn.createStatement().execute(createPsmTableSql);

        for (PSMRow psmRow : this.psmRows) {
            StringBuffer sqlStr = new StringBuffer();
            sqlStr.append("upsert into " + psmTableName.toUpperCase() + " values (");
            sqlStr.append("'" + psmRow.getSpectrumTitle() + "',");
            sqlStr.append("'" + psmRow.getPeptideSequence()+ "'");
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

}
