package org.ncpsb.phoenixcluster.pridexmltophoenix;

/**
 * Created by baimi on 2017/11/7.
 */

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessController;
import uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl.PrideXmlControllerImpl;

public class Cli {
    private static final Logger log = Logger.getLogger(Cli.class.getName());
    private String[] args = null;
    private Options options = new Options();

    public Cli(String[] args) {

        this.args = args;

        options.addOption("h", "help", false, "show help.");
        options.addOption("v", "var", true, "Here you can set parameter .");
        options.addOption("i", "input", true, "The path of the input PRIDE XML file");
        options.addOption("p", "projectId", true, "The project id");
        options.addOption("om", "outputMgf", false, "If want to output the mgf file");
    }

    public void parse() {
        CommandLineParser parser = new BasicParser();

        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);

            if (cmd.hasOption("h"))
                help();

            if (cmd.hasOption("i") && cmd.hasOption("p")) {


                String inputFileName = cmd.getOptionValue("i");
                String projectId = cmd.getOptionValue("p").toUpperCase();
                String suffix = inputFileName.substring(inputFileName.lastIndexOf(".") + 1);
                if (!suffix.equalsIgnoreCase("xml")) {
                    System.out.println("The suffix of the input file is not xml/XML, please have a check");
                    System.exit(1);
                }
                File inputFile = new File(inputFileName);

                DataAccessController controller = new PrideXmlControllerImpl(inputFile);
                PrideXmlImporter importer = new PrideXmlImporter(controller, inputFile, projectId);
                importer.importToPhoenix(inputFile);
//                log.log(Level.INFO, "Using cli argument -v=" + cmd.getOptionValue("v"));
                // Whatever you want to do with the setting goes here

                if (cmd.hasOption("om")) {
                    File outputFile = new File(inputFileName.substring(0,inputFileName.length()-4) + ".mgf");
                    if (outputFile.exists() && outputFile.length()>0) {
                        System.out.println(outputFile.length());
                        System.out.println(outputFile.getAbsolutePath() + "is already there, abort converting");
                    }
                    else{
                        MGFConverterOverride converter = new MGFConverterOverride(controller, outputFile.getAbsolutePath(), projectId);
                        converter.convert();
                    }
                }

            } else {
                log.log(Level.SEVERE, "Missing i or p option");
                help();
            }


        } catch (ParseException e) {
            log.log(Level.SEVERE, "Failed to parse comand line properties", e);
            help();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void help() {
        // This prints out some help
        HelpFormatter formater = new HelpFormatter();

        formater.printHelp("Main", options);
        System.exit(0);
    }
}
