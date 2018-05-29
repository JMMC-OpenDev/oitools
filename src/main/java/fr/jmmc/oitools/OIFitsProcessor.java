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

    private static final String COMMAND_HELP = "help";
    private static final String COMMAND_LIST = "list";
    private static final String COMMAND_CONVERT = "convert";
    private static final String COMMAND_MERGE = "merge";

    private static final String OPTION_OUTPUT = "-output";

    /**
     * Main entry point.
     *
     * @param args command line arguments.
     */
    public static void main(final String[] args) {
        try {
            if (args.length < 1) {
                showArgumentsHelp();
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

            List<String> inputFiles = getInputFiles(args);
            String outputFilePath = getOutputFilepath(args);

            // command processing
            if (COMMAND_HELP.equals(command)) {

                showArgumentsHelp();

            } else if (COMMAND_LIST.equals(command)) {

                list(inputFiles);

            } else if (COMMAND_CONVERT.equals(command)) {

                if (inputFiles.isEmpty()) {
                    errorArg("No file location given in arguments.");
                }
                if (inputFiles.size() > 1) {
                    errorArg("too many input files, only one is accepted.");
                }

                copy(inputFiles.get(0), outputFilePath);

            } else if (COMMAND_MERGE.equals(command)) {

                merge(inputFiles, outputFilePath);

            } else {
                errorArg("Unknown command.");
            }
        } catch (Exception e) {
            error("Processor: exception occured", e);
        }
    }

    /**
     * List content of files
     *
     * @param fileLocations
     */
    private static void list(final List<String> fileLocations) throws FitsException, IOException {
        // TODO: implement later a simplified output

        if (fileLocations.isEmpty()) {
            errorArg("No file location given in arguments.");
        }

        OIFitsViewer.process(false, true, false, false, fileLocations);
    }

    /**
     * Copy content of a oifits file in another
     *
     * @param inputFileLocations
     * @param outputFilePath
     */
    private static void copy(String inputFileLocation, String outputFilePath) throws FitsException, Exception {
        if (outputFilePath == null) {
            errorArg("No output file given in arguments.");
        }

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
        if (fileLocations == null || fileLocations.isEmpty()) {
            errorArg("No file location given in arguments.");
        }
        if (outputFilePath == null) {
            errorArg("No output file given in arguments.");
        }

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
        OIFitsWriter.writeOIFits(outputFilePath, result);
    }

    /**
     * Get output file path from command arguments
     *
     * @param args
     * @return output file path 
     */
    private static String getOutputFilepath(String[] args) {
        String filePath = null;
        for (int i = 1; i < args.length; i++) {
            if (OPTION_OUTPUT.substring(0, 2).equals(args[i]) || OPTION_OUTPUT.equals(args[i])) {
                filePath = (++i < args.length) ? args[i] : null;
                break;
            }
        }
        return filePath;
    }

    /**
     * Get input file paths from command arguments
     *
     * @param args
     * @return input file paths
     */
    private static List<String> getInputFiles(String[] args) {
        List<String> files = new ArrayList<String>();
        if (args.length >= 2) {
            for (int i = 1; i < args.length; i++) {
                // note: should be generalized to any argument having value(s):
                if (OPTION_OUTPUT.substring(0, 2).equals(args[i]) || OPTION_OUTPUT.equals(args[i])) {
                    i++;  // skip next parameter which is the output file
                } else if (args[i].startsWith("-")) {
                    // ignore short options
                } else {
                    files.add(args[i]);
                }
            }
        }
        return files;
    }

    /**
     * Print an error message when parsing the command line arguments
     *
     * @param message message to print
     */
    protected static void errorArg(final String message) {
        error(message);
        showArgumentsHelp();
        System.exit(1);
    }

    /** Show command arguments help */
    protected static void showArgumentsHelp() {
        info("--------------------------------------------------------------------------------------");
        info("Usage: " + OIFitsProcessor.class.getName() + " command -o <path_output_file> <file locations>");
        info("------------- Arguments help ---------------------------------------------------------");
        info("| Key          Value           Description                                           |");
        info("|------------------------------------------------------------------------------------|");
        info("| command      " + COMMAND_LIST + "           List content of several oifits files                   |");
        info("| command      " + COMMAND_CONVERT + "        Convert the given input file                           |");
        info("| command      " + COMMAND_MERGE + "          Merge several oifits files                             |");
        info("| " + OPTION_OUTPUT.substring(0, 2) + " or " + OPTION_OUTPUT
                + " <file_path>   Complete path, absolute or relative, for output file   |");

        info("| [-l] or [-log]              Enable logging (quiet by default)                      |");
        info("--------------------------------------------------------------------------------------");
    }

}
