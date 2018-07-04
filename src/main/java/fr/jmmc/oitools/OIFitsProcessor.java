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

import fr.jmmc.oitools.fits.FitsUtils;
import fr.jmmc.oitools.model.OIFitsChecker;
import fr.jmmc.oitools.model.OIFitsCollection;
import fr.jmmc.oitools.model.OIFitsFile;
import fr.jmmc.oitools.model.OIFitsLoader;
import fr.jmmc.oitools.model.OIFitsWriter;
import fr.jmmc.oitools.processing.Merger;
import fr.jmmc.oitools.processing.Selector;
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
    private static final String COMMAND_DUMP = "dump";
    private static final String COMMAND_MERGE = "merge";

    private static final String OPTION_OUTPUT = "-output";
    private static final String OPTION_TARGET = "-target";
    private static final String OPTION_INSNAME = "-insname";

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

            final boolean quiet = !hasOptionArg(args, "-l", "-log");
            bootstrap(quiet);

            final String command = args[0];

            // command processing
            if (COMMAND_HELP.equals(command)) {
                showArgumentsHelp();
            } else if (COMMAND_DUMP.equals(command)) {
                dump(args);
            } else if (COMMAND_LIST.equals(command)) {
                list(args);
            } else if (COMMAND_CONVERT.equals(command)) {
                copy(args);
            } else if (COMMAND_MERGE.equals(command)) {
                merge(args);
            } else {
                throw new IllegalArgumentException("Unknown command.");
            }
        } catch (IllegalArgumentException iae) {
            error(iae.getMessage());
            showArgumentsHelp();
        } catch (Exception e) {
            error("Processor: exception occured", e);
        }
    }

    /**
     * List content of files
     *
     * @param args command line arguments.
     */
    private static void list(final String[] args) throws FitsException, IOException {
        final List<String> fileLocations = getInputFiles(args);
        final boolean check = hasOptionArg(args, "-c", "-check");

        final OIFitsChecker checker = new OIFitsChecker();

        final OIFitsCollection oiFitsCollection = OIFitsCollection.create(checker, fileLocations);

        if (check) {
            info("validation results:\n" + checker.getCheckReport());
        }

        OIFitsCollectionViewer.process(oiFitsCollection);
    }

    /**
     * Dump content of files
     *
     * @param args command line arguments.
     */
    private static void dump(final String[] args) throws FitsException, IOException {
        final List<String> fileLocations = getInputFiles(args);

        FitsUtils.setup();

        final StringBuilder sb = new StringBuilder(16 * 1024);

        for (String fileLocation : fileLocations) {
            info("Processing: " + fileLocation);
            try {
                FitsUtils.dumpFile(fileLocation, false, sb);

                info(sb.toString());
                sb.setLength(0); // reset

            } catch (Exception e) {
                error("Error reading file '" + fileLocation + "'", e);
            }
        }
    }

    /**
     * Copy content of a oifits file in another
     *
     * @param inputFileLocations
     * @param outputFilePath
     */
    private static void copy(final String[] args) throws FitsException, Exception {
        final List<String> fileLocations = getInputFiles(args);
        if (fileLocations.size() > 1) {
            throw new IllegalArgumentException("too many input files, only one is accepted.");
        }

        final String inputFileLocation = fileLocations.get(0);
        final String outputFilePath = getOutputFilepath(args);
        final boolean check = hasOptionArg(args, "-c", "-check");

        // Load then save file content
        final OIFitsFile result = OIFitsLoader.loadOIFits(inputFileLocation);
        // Store result
        write(outputFilePath, result, check);
    }

    /**
     * Merge some oifits files, store result in output file
     *
     * @param args: parameter of command line
     * @param fileLocations
     * @param outputFilePath
     */
    private static void merge(final String[] args) throws FitsException, IOException {
        final List<String> fileLocations = getInputFiles(args);
        final String outputFilePath = getOutputFilepath(args);
        final boolean check = hasOptionArg(args, "-c", "-check");

        // Optional filters:
        final String targetUID = getOptionArgValue(args, OPTION_TARGET);
        final String insModeUID = getOptionArgValue(args, OPTION_INSNAME);

        final OIFitsCollection oiFitsCollection = OIFitsCollection.create(null, fileLocations);

        final Selector selector = new Selector();
        if (targetUID != null) {
            selector.setTargetUID(targetUID);
        }
        if (insModeUID != null) {
            selector.setInsModeUID(insModeUID);
        }

        // Call merge
        final OIFitsFile result = Merger.process(oiFitsCollection, selector);
        if (result.hasOiData()) {
            // Store result
            write(outputFilePath, result, check);
        } else {
            info("Result is empty, no file created.");
        }
    }

    private static void write(final String outputFilePath, final OIFitsFile result, final boolean check) throws IOException, FitsException {
        if (check) {
            final OIFitsChecker checker = new OIFitsChecker();
            result.check(checker);
            info("validation results:\n" + checker.getCheckReport());
        }

        info("Writing: " + outputFilePath);
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
        String outputFilePath = null;

        for (int i = 1; i < args.length; i++) {
            if (OPTION_OUTPUT.substring(0, 2).equals(args[i]) || OPTION_OUTPUT.equals(args[i])) {
                outputFilePath = (++i < args.length) ? args[i] : null;
                break;
            }
        }
        if (outputFilePath == null) {
            throw new IllegalArgumentException("No output file given in arguments.");
        }
        return outputFilePath;
    }

    /**
     * Get input file paths from command arguments
     *
     * @param args
     * @return input file paths
     */
    private static List<String> getInputFiles(String[] args) {
        final List<String> fileLocations = new ArrayList<String>();

        for (int i = 1; i < args.length; i++) {
            // note: should be generalized to any argument having value(s):
            if (OPTION_OUTPUT.substring(0, 2).equals(args[i])
                    || OPTION_OUTPUT.equals(args[i])
                    || OPTION_TARGET.equals(args[i])
                    || OPTION_INSNAME.equals(args[i])) {
                i++;  // skip next parameter which is the output file
            } else if (args[i].startsWith("-")) {
                // ignore short options
            } else {
                fileLocations.add(args[i]);
            }
        }

        if (fileLocations.isEmpty()) {
            throw new IllegalArgumentException("No file location given in arguments.");
        }
        return fileLocations;
    }

    /**
     * Show command arguments help
     */
    protected static void showArgumentsHelp() {
        info("--------------------------------------------------------------------------------------");
        info("Usage: " + OIFitsProcessor.class.getName() + " command -o <path_output_file> <file locations>");
        info("------------- Arguments help ---------------------------------------------------------");
        info("| Key          Value           Description                                           |");
        info("|------------------------------------------------------------------------------------|");
        info("| command      " + COMMAND_HELP + "           Show this help                                         |");
        info("| command      " + COMMAND_LIST + "           List content of several oifits files                   |");
        info("| command      " + COMMAND_DUMP + "           Dump the given oifits files                           |");
        info("| command      " + COMMAND_CONVERT + "        Convert the given input file                           |");
        info("| command      " + COMMAND_MERGE + "          Merge several oifits files                             |");
        info("| " + OPTION_OUTPUT.substring(0, 2) + " or " + OPTION_OUTPUT
                + " <file_path>   Complete path, absolute or relative, for output file   |");
        info("| [-l] or [-log]              Enable logging (quiet by default)                      |");
        info("| [-c] or [-check]            Check output file before writing                       |");
        info("| [-target]    <target value>  Filter result on given target                         |");
        info("| [-insname]   <insname value> Filter result on given insname                        |");
        info("--------------------------------------------------------------------------------------");
    }

}
