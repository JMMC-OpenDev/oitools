/* 
 * Copyright (C) 2018 CNRS - JMMC project ( http://www.jmmc.fr )
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.jmmc.oitools;

import fr.jmmc.oitools.model.OIFitsFile;
import fr.jmmc.oitools.model.OIFitsLoader;
import fr.jmmc.oitools.model.OIFitsWriter;
import fr.jmmc.oitools.util.MergeUtil;
import fr.nom.tam.fits.FitsException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jammetv
 */
public class OIFitsProcessor {

    private static final Logger logger = Logger.getLogger(OIFitsProcessor.class.getName());

    private static final String COMMAND_MERGE = "merge";
    private static final String COMMAND_LIST = "list";

    private static final String OPTION_HELP = "-help";

    /**
     * Main entry point. TODO >> Ailleurs
     *
     * @param args command line arguments.
     */
    public static void main(final String[] args) {

        if (args.length < 1) {
            // No prameters, show help
            showArgumentsHelp();
            return;
        }

        String command = args[0];

        if (OPTION_HELP.substring(0, 2).equals(command) || OPTION_HELP.equals(command)) {
            // help asked
            showArgumentsHelp();

        } else {

            List<String> inputFiles = getInputFiles(args);
            String outputFile = getOutputFilepath(args);

            // command
            if (COMMAND_MERGE.equals(command)) {

                merge(inputFiles, outputFile);

            } else if (COMMAND_LIST.equals(command)) {

                list(inputFiles);

            } else {
                showArgumentsHelp();
            }
        }

    }

    /**
     *
     * @param inputFiles
     * @param outputFile
     */
    private static void list(List<String> inputFiles) {
        // List data of input file
        OIFitsViewer viewer = new OIFitsViewer(true, false);
        for (String inputFile : inputFiles) {
            try {
                // Visualisation of file data
                viewer.process(inputFile);
            } catch (IOException ioe) {
                logger.log(Level.SEVERE, "OIFitsViewer: File not valid", ioe);
            } catch (FitsException fe) {
                logger.log(Level.SEVERE, null, fe);
            }
        }
    }

    /**
     * Merge some oifits files, store result in output file
     *
     * @param args: parameter of command line
     * @param inputFiles
     * @param outputFile
     */
    private static void merge(List<String> inputFiles, String outputFile) {
        if (inputFiles != null && inputFiles.size() > 0) {
            if (outputFile != null) {

                OIFitsFile[] inputs = new OIFitsFile[inputFiles.size()];
                int i = 0;
                for (String inputFile : inputFiles) {
                    try {
                        inputs[i] = OIFitsLoader.loadOIFits(inputFile);
                        i++;
                    } catch (FitsException fe) {
                        logger.log(Level.SEVERE,
                                String.format("Content of file %s is not valid", inputFile), fe);
                        System.exit(1);
                    } catch (IOException ioe) {
                        logger.log(Level.SEVERE,
                                String.format("File %s is not valid", inputFile), ioe);
                        System.exit(1);
                    }
                }
                OIFitsFile result = MergeUtil.mergeOIFitsFiles(inputs);
                result.setAbsoluteFilePath(outputFile);
                try {
                    OIFitsWriter.writeOIFits(outputFile, result);
                } catch (FitsException fe) {
                    logger.log(Level.SEVERE,
                            String.format("Content of output file %s is not valid", outputFile), fe);
                    System.exit(1);
                } catch (IOException ioe) {
                    logger.log(Level.SEVERE,
                            String.format("Output file %s is not valid", outputFile), ioe);
                    System.exit(1);
                }

            } else {
                showArgumentsHelp();
            }

        } else {
            showArgumentsHelp();
        }

    }

    /**
     * Get path of output file from command parameters
     *
     * @param args
     * @return
     */
    private static String getOutputFilepath(String[] args) {
        String files = null;
        if (args.length > 3) {
            for (int i = 1; i < args.length; i++) {
                if ("-o".equals(args[i]) || "-output".equals(args[i])) {
                    i++;
                    files = i < args.length ? args[i] : null;
                    break;
                }
            }
        }
        return files;
    }

    /**
     * Get path of output file from command parameters
     *
     * @param args
     * @return
     */
    private static List<String> getInputFiles(String[] args) {
        List<String> files = new ArrayList<String>();
        if (args.length >= 2) {
            for (int i = 1; i < args.length; i++) {
                if ("-o".equals(args[i]) || "-output".equals(args[i])) {
                    i++;  // skip next parameter which is the output file
                } else {
                    files.add(args[i]);
                }
            }
        }
        return files;
    }

    /**
     * Show command arguments help
     */
    private static void showArgumentsHelp() {
        System.out.println(
                "--------------------------------------------------------------------------------------");
        System.out.println(
                "Usage: " + OIFitsProcessor.class.getName() + " command -o <path_output_file> <file names>");
        System.out.println(
                "------------- Arguments help ---------------------------------------------------------");
        System.out.println(
                "| Key          Value           Description                                           |");
        System.out.println(
                "|------------------------------------------------------------------------------------|");
        System.out.println(
                "| command      "+COMMAND_MERGE+"           Merge several oifits files                            |");
        System.out.println(
                "| command      "+COMMAND_LIST+"           List content of several oifits files                            |");
        System.out.println(
                "| -o or -output <file_path>    Complete path, absolut or relative, for output file   |");
        System.out.println(
                "--------------------------------------------------------------------------------------");
    }

}
