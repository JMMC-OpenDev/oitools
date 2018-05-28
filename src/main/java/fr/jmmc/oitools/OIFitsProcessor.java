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

/**
 *
 * @author jammetv
 */
public class OIFitsProcessor extends OIFitsCommand {

    private static final String COMMAND_MERGE = "merge";
    private static final String COMMAND_LIST = "list";
    private static final String COMMAND_CONVERT = "convert";

    private static final String OPTION_HELP = "-help";
    private static final String OPTION_OUTPUT = "-output";

    /**
     * Main entry point. TODO >> Ailleurs
     *
     * @param args command line arguments.
     */
    public static void main(final String[] args) {

        try {
            if (args.length < 1) {
                showArgumentsHelp("No parameters given.");
                return;
            }
            
            boolean quiet = true;
            for (final String arg : args) {
                if (arg.equals("-l") || arg.equals("-log")) {
                    quiet = false;
                } 
            }
            bootstrap(quiet);

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

                    list(inputFiles, !quiet);

                } else if (COMMAND_CONVERT.equals(command)) {

                    if (inputFiles.size() > 1) {
                        throw new IllegalArgumentException("Copy: too many input file, only one is accepted.");
                    }

                    copy(inputFiles.get(0), outputFile);

                } else {

                    showArgumentsHelp("Unknown command.");
                }
            }
        } catch (Exception e) {
            error("Processor: exception occurs", e);
        }

    }

    /**
     * List content of files
     *
     * @param fileLocations
     */
    private static void list(List<String> fileLocations, boolean logAsked) throws FitsException, IOException {
        // List data of input file
        ArrayList<String> argsList = new ArrayList<String>();
        argsList.add("-tsv");
        if (logAsked) {
            argsList.add("-log");
        }
        argsList.addAll(fileLocations);
        
        
        OIFitsViewer.main((String[]) (argsList.toArray(new String[argsList.size()])));
//        OIFitsViewer viewer = new OIFitsViewer(true, true, false);
//        for (String fileLocation : fileLocations) {
//            // Visualisation of file data
//            String display = viewer.process(fileLocation);
//            System.out.println(display);
//        }
    }

    /**
     * Copy content of a oifits file in another
     *
     * @param inputFileLocations
     * @param outputFilePath
     */
    private static void copy(String inputFileLocation, String outputFilePath) throws FitsException, Exception {

        // Load then save file content
        OIFitsFile file = OIFitsLoader.loadOIFits(inputFileLocation);
        OIFitsWriter.writeOIFits(outputFilePath, file);

    }

    /**
     * Merge some oifits files, store result in output file
     *
     * @param args: parameter of command line
     * @param fileLocations
     * @param outputFilePath
     */
    private static void merge(List<String> fileLocations, String outputFilePath) throws FitsException, IOException {

        if (fileLocations != null && fileLocations.size() > 0) {
            if (outputFilePath != null) {

                OIFitsFile[] inputs = new OIFitsFile[fileLocations.size()];
                int i = 0;
                // Get input files
                for (String fileLocation : fileLocations) {
                    inputs[i] = OIFitsLoader.loadOIFits(fileLocation);
                    i++;
                }

                // Call merge
                OIFitsFile result = MergeUtil.mergeOIFitsFiles(inputs);
                // Store result
                result.setAbsoluteFilePath(outputFilePath);
                OIFitsWriter.writeOIFits(outputFilePath, result);

            } else {
                showArgumentsHelp("No output files given in parameters.");
            }

        } else {
            showArgumentsHelp("No input file given in parameters.");
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
                if (OPTION_OUTPUT.substring(0, 2).equals(args[i]) || OPTION_OUTPUT.equals(args[i])) {
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
                if (OPTION_OUTPUT.substring(0, 2).equals(args[i]) || OPTION_OUTPUT.equals(args[i])) {
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
    protected static void showArgumentsHelp() {
        showArgumentsHelp(null);
    }

    private static void showArgumentsHelp(String prefixMessage) {
        if (prefixMessage != null) {
            error(prefixMessage);
        }
        info("--------------------------------------------------------------------------------------");
        info("Usage: " + OIFitsProcessor.class.getName() + " command -o <path_output_file> <file locations>");
        info("------------- Arguments help ---------------------------------------------------------");
        info("| Key          Value           Description                                           |");
        info("|------------------------------------------------------------------------------------|");
        info("| command      " + COMMAND_MERGE + "          Merge several oifits files                         |");
        info("| command      " + COMMAND_LIST + "           List content of several oifits files                |");
        info("| command      " + COMMAND_CONVERT + "        REmove unused data from file                       |");
        info("| " + OPTION_OUTPUT.substring(0, 2) + " or " + OPTION_OUTPUT
                + " <file_path>    Complete path, absolut or relative, for output file   |");
        info("--------------------------------------------------------------------------------------");
    }

}
