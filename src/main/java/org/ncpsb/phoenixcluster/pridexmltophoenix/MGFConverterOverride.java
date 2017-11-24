package org.ncpsb.phoenixcluster.pridexmltophoenix;

import uk.ac.ebi.pride.utilities.data.controller.DataAccessController;
import uk.ac.ebi.pride.utilities.data.exporters.MGFConverter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessException;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessController.Type;
import uk.ac.ebi.pride.utilities.data.core.ExperimentMetaData;
import uk.ac.ebi.pride.utilities.data.core.Spectrum;
/**
 * Created by baimi on 2017/11/21.
 * To access the protected convert method.
 */
public class MGFConverterOverride{
    protected static Logger logger = LoggerFactory.getLogger(MGFConverterOverride.class);
    private String outputFilePath;
    private String projectId;
    private DataAccessController controller;

    public MGFConverterOverride(DataAccessController controller, String outputFilePath, String projectId) {
        this.projectId = projectId;
        this.outputFilePath = outputFilePath;
        this.controller = controller;
    }
    public void convert() throws Exception {
        PrintWriter writer = null;

        try {
            String acc;
            try {
                writer = new PrintWriter(new FileWriter(new File(this.outputFilePath)));
                ExperimentMetaData exp = this.controller.getExperimentMetaData();
                if(this.controller.getType().equals(Type.XML_FILE)) {
                    writer.println("# Data source: " + ((File)this.controller.getSource()).getAbsolutePath());
                }

                acc = exp.getId() != null?exp.getId().toString():null;
                if(acc != null) {
                    writer.println("# Experiment accession: " + acc);
                }

                String title = exp.getName();
                if(title != null) {
                    writer.println("# Experiment title: " + title);
                }

                if(this.controller.hasSpectrum()) {
                    writer.println("# Number of spectra: " + this.controller.getNumberOfSpectra());
                }

                if(this.controller.hasProtein()) {
                    writer.println("# Number of protein identifications: " + this.controller.getNumberOfProteins());
                }

                if(this.controller.hasPeptide()) {
                    writer.println("# Number of peptides: " + this.controller.getNumberOfPeptides());
                }

                Iterator var5 = this.controller.getSpectrumIds().iterator();

                while(true) {
                    Comparable spectrumId;
                    Spectrum spectrum;
                    int msLevel;
                    do {
                        if(!var5.hasNext()) {
                            writer.flush();
                            return;
                        }

                        spectrumId = (Comparable)var5.next();
                        spectrum = this.controller.getSpectrumById(spectrumId);
                        msLevel = this.controller.getSpectrumMsLevel(spectrumId);
                    } while(msLevel != 2);

                    writer.println("BEGIN IONS");
                    String spectrumTitle = this.projectId + ";" +
                                           ((File)this.controller.getSource()).getName() + ";" +
                                           "spectrum=" + spectrumId;
                    writer.println("TITLE=" + spectrumTitle);
                    writer.println("PEPMASS=" + this.controller.getSpectrumPrecursorMz(spectrumId));
                    Integer charge = this.controller.getSpectrumPrecursorCharge(spectrumId);
                    if(charge != null) {
                        writer.println("CHARGE=" + charge + (charge.intValue() >= 0?"+":"-"));
                    }

                    double[] mzBinaryArray = spectrum.getMzBinaryDataArray().getDoubleArray();
                    double[] intensityArray = spectrum.getIntensityBinaryDataArray().getDoubleArray();

                    for(int i = 0; i < mzBinaryArray.length; ++i) {
                        writer.println(mzBinaryArray[i] + "\t" + intensityArray[i]);
                    }

                    writer.println("END IONS\n");
                    if(Thread.interrupted()) {
                        throw new InterruptedException();
                    }

                    writer.flush();
                }
            } catch (DataAccessException var17) {
                acc = "Failed to retrieve data from data source";
                logger.error(acc, var17);
            } catch (IOException var18) {
                acc = "Failed to write data to the output file, please check you have the right permission";
                logger.error(acc, var18);
            }
        } finally {
            if(writer != null) {
                writer.close();
            }

        }


    }
}
