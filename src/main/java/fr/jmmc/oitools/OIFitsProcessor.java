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

import fr.jmmc.oitools.meta.OIFitsStandard;
import fr.jmmc.oitools.model.OIArray;
import fr.jmmc.oitools.model.OIData;
import fr.jmmc.oitools.model.OIFitsFile;
import fr.jmmc.oitools.model.OIFitsLoader;
import fr.jmmc.oitools.model.OIFitsWriter;
import fr.jmmc.oitools.model.OITable;
import fr.jmmc.oitools.model.OITarget;
import fr.jmmc.oitools.model.OIWavelength;
import fr.nom.tam.fits.FitsException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jammetv
 */
public class OIFitsProcessor {

    private static final Logger logger = Logger.getLogger(OIFitsProcessor.class.getName());

    /**
     * Merge of two oifits files. Only one target by input files are accepted, have to be the the same (name) in the two
     * files. Target of file 1 is cloned in result.
     *
     * @param oifitsOne first file to be merged
     * @param oifitsTwo second file to be merged
     * @return result of merge
     * @throws IllegalArgumentException
     */
    public static OIFitsFile mergeOIFitsFile(
            final OIFitsFile oifitsOne, final OIFitsFile oifitsTwo)
            throws IllegalArgumentException {
        return mergeOIFitsFiles(oifitsOne, oifitsTwo);
    }

    public static OIFitsFile mergeOIFitsFiles(
            final OIFitsFile... oiFitsToMerge)
            throws IllegalArgumentException {
        // TODO: clone fits structure by save/load on disk ? 
        // aims: get local copy modifiable

        if (oiFitsToMerge == null || oiFitsToMerge.length < 2) {
            throw new IllegalArgumentException(
                    "Merge: Not enough files as parameters: " + oiFitsToMerge);
        }

        final OIFitsFile result = new OIFitsFile(OIFitsStandard.VERSION_1);

        final Map<String, String> mapWLNames = new HashMap<String, String>();
        final Map<String, String> mapArrayNames = new HashMap<String, String>();

        String targetName = null;

        // Merge metadata FIRST to prepare mappings
        for (OIFitsFile fileToMerge : oiFitsToMerge) {
            // analyze first to use Target objects:
            // fileToMerge.analyze(); // will be usefull in the future

            // Process OI_TARGET part
            OITarget oiTarget = fileToMerge.getOiTarget();
            checkOITarget(targetName, oiTarget);

            // No exception raised, store target 1 in result
            if (result.getOiTarget() == null || result.getOiTarget().getNbTargets() == 0) {
                oiTarget.setOIFitsFile(result); // TODO FIX
                result.addOiTable(oiTarget);
                targetName = oiTarget.getTarget()[0];
            }
            // TODO: ? cloner l'instance
            logger.info("Target name: " + result.getOiTarget());

            // Process OI_WAVELENGTH part
            mergeOIWL(result, fileToMerge, mapWLNames);
            logger.info("mapWLNames: " + mapWLNames);
            logger.info("insnames: " + Arrays.toString(result.getAcceptedInsNames()));

            // Process OI_ARRAY part 
            mergeOIArray(result, mapArrayNames, fileToMerge);
            logger.info("mapArrayNames: " + mapArrayNames);
        }

        // Merge data
        for (OIFitsFile filetoMerge : oiFitsToMerge) {
            mergeOIData(result, filetoMerge, mapWLNames, mapArrayNames);
        }

        return result;
    }

    /**
     * Merge Target part of OIFitsFile
     *
     * @param oit1
     * @param oit2
     * @throws IllegalArgumentException
     */
    private static void checkOITarget(String resultTargetName, OITarget oiTarget) throws IllegalArgumentException {
        if (oiTarget == null || oiTarget.getNbRows() < 1) {
            throw new IllegalArgumentException(
                    String.format("Merge: target for %s is null or empty",
                            (oiTarget != null) ? oiTarget.getOIFitsFile().getAbsoluteFilePath() : "null"));
        }
        if (oiTarget.getNbRows() > 1) {
            throw new IllegalArgumentException(
                    "Merge: more than one target for " + oiTarget.getOIFitsFile().getAbsoluteFilePath());
        }
        String targetNameX = oiTarget.getTarget()[0];
        if (targetNameX == null || targetNameX.length() == 0) {
            throw new IllegalArgumentException(
                    "Merge: null or empty name for target of " + oiTarget.getOIFitsFile().getAbsoluteFilePath());
        }
        if (resultTargetName != null && !resultTargetName.equals(targetNameX)) {
            throw new IllegalArgumentException("Merge: files have not the same target");
        }
    }

    /**
     * Merge the WL part of OIFitsFiles
     *
     * @param result
     * @param f1
     * @param f2
     * @return
     */
    private static void mergeOIWL(OIFitsFile result, OIFitsFile fileToMerge, Map< String, String> mapWaveNames) {
        // Browse all names of file to merge, change name if already present in result, add to map 
        // old_name > new name
        for (OIWavelength oiWaveLength : fileToMerge.getOiWavelengths()) {
            final String oldName = oiWaveLength.getInsName();
            String newName = oldName;
            int idx = 0;
            while (result.getOiWavelength(newName) != null) {
                idx++;
                newName = oldName + "_" + idx;
            }

            // TODO : clone instance of source data
            OIWavelength newOiWave = (OIWavelength) copyTable(result, oiWaveLength);

            newOiWave.setInsName(newName);
            mapWaveNames.put(oldName, newName);

            result.addOiTable(newOiWave);
        }
    }

    /**
     * Merge Array part of OIFitsFiles
     *
     * @param result
     * @param f1
     * @param f2
     * @return
     */
    private static void mergeOIArray(OIFitsFile result, Map<String, String> mapArrayNames, OIFitsFile fileToMerge) {
        // Browse all names of file to merge, change name if already present in result, add to map 
        // old_name > new name
        for (OIArray oiArray : fileToMerge.getOiArrays()) {
            String oldName = oiArray.getArrName(), newName = oldName;
            int idx = 0;
            while (result.getOiArray(newName) != null) {
                idx++;
                newName = oldName + "_" + idx;
            }

            // TODO : clone instance of source data
            OIArray newOiArray = (OIArray) copyTable(result, oiArray);

            newOiArray.setArrName(newName);
            mapArrayNames.put(oldName, newName);

            result.addOiTable(newOiArray);
        }
    }

    private static void mergeOIData(
            OIFitsFile result, OIFitsFile fileToMerge,
            Map<String, String> mapWLNames, Map<String, String> mapArrayNames) {

        // Browse all data
        for (OIData oiData : fileToMerge.getOiDatas()) {

            // TODO : clone instance of source data
            OIData newData = (OIData) copyTable(result, oiData);

            // Change wave and array by new ones
            newData.setArrName(mapArrayNames.get(oiData.getArrName()));
            newData.setInsName(mapWLNames.get(oiData.getInsName()));

            result.addOiTable(newData);

        }
    }

    private static OITable copyTable(OIFitsFile result, OITable oiTable) {

        // TODO : clone instance of source data
        OITable newTable = oiTable;
        newTable.setOIFitsFile(result);

        return newTable;
    }

    /**
     * Main entry point.
     *
     * @param args command line arguments.
     */
    public static void main(final String[] args) {

        if (args.length < 1) {
            // No prameters, show help
            System.out.print("Not enough parameters");
            showArgumentsHelp();
            return;
        }

        String command = args[0];
        String outputFile;
        List<String> inputFiles = new ArrayList<String>();

        if ("-h".equals(command) || "-help".equals(command)) {
            // help asked
            showArgumentsHelp();

        } else {

            // command
            if ("merge".equals(command)) {

                inputFiles = getInputFiles(args);
                if (inputFiles != null && inputFiles.size() > 0) {
                    outputFile = getOutputFilepath(args);
                    if (outputFile != null) {

                        OIFitsFile[] inputs = new OIFitsFile[inputFiles.size()];
                        int i = 0;
                        for (String inputFile : inputFiles) {
                            try {
                                inputs[i] = OIFitsLoader.loadOIFits(inputFile);
                                i++;
                            } catch (FitsException fe) {
                                Logger.getLogger(OIFitsProcessor.class.getName()).log(Level.SEVERE,
                                        String.format("Content of file %s is not valid", inputFile), fe);
                                System.exit(1);
                            } catch (IOException ioe) {
                                Logger.getLogger(OIFitsProcessor.class.getName()).log(Level.SEVERE,
                                        String.format("File %s is not valid", inputFile), ioe);
                                System.exit(1);
                            }
                        }
                        System.out.println("Start merge ...");
                        OIFitsFile result = OIFitsProcessor.mergeOIFitsFiles(inputs);
                        result.setAbsoluteFilePath(outputFile);
                        try {
                            OIFitsWriter.writeOIFits( outputFile, result);
                            } catch (FitsException fe) {
                                Logger.getLogger(OIFitsProcessor.class.getName()).log(Level.SEVERE,
                                        String.format("Content of output file %s is not valid", outputFile), fe);
                                System.exit(1);
                            } catch (IOException ioe) {
                                Logger.getLogger(OIFitsProcessor.class.getName()).log(Level.SEVERE,
                                        String.format("Output file %s is not valid", outputFile), ioe);
                                System.exit(1);
                        }
                        System.out.println("Merge done.");

                    } else {
                        System.out.println("Output file path is missing");
                        showArgumentsHelp();
                    }
                    
                } else {
                    System.out.println("No input file given");
                    showArgumentsHelp();
                }

            } else if ("list".equals(command)) {
                // List data of input file
                OIFitsViewer viewer = new OIFitsViewer(true, false);
                for (String inputFile : getInputFiles(args)) {
                    try {
                        // Visualisation of file data
                        viewer.process(inputFile);
                    } catch (IOException ioe) {
                        Logger.getLogger(OIFitsProcessor.class.getName()).log(Level.SEVERE, "", ioe);
                    } catch (FitsException fe) {
                        Logger.getLogger(OIFitsProcessor.class.getName()).log(Level.SEVERE, null, fe);
                    }
                }

            } else {
                System.out.println("No valid command given");
                showArgumentsHelp();
            }
        }

//        i = 0;
//        // get the command
//        
//        
//        // parse command line arguments :
//        for (final String arg : args) {
//            if (arg.startsWith("-")) {
//                if (arg.equals("-t") || arg.equals("-tsv")) {
//                    tsv = true;
//                } else if (arg.equals("-f") || arg.equals("-format")) {
//                    format = true;
//                } else if (arg.equals("-v") || arg.equals("-verbose")) {
//                    verbose = true;
//                } else if (arg.equals("-c") || arg.equals("-check")) {
//                    xml = false;
//                } else if (arg.equals("-h") || arg.equals("-help")) {
//                    showArgumentsHelp();
//                    System.exit(0);
//                } else {
//                    error("'" + arg + "' option not supported.");
//                }
//            } else {
//                fileNames.add(arg);
//            }
//        }
//
//        if (fileNames.isEmpty()) {
//            error("Missing file name argument.");
//        }
//
//        final OIFitsViewer viewer = new OIFitsViewer(xml, tsv, format, verbose);
//
//        if (!tsv) {
//            System.out.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n<oifits_list>");
//        }
//        for (String fileName : fileNames) {
//            try {
//                System.out.println(viewer.process(fileName));
//            } catch (Exception e) {
//                e.printStackTrace(System.err);
//                System.out.println("Error reading file '" + fileName + "'");
//            }
//        }
//        if (!tsv) {
//            System.out.println("</oifits_list>");
//        }
//
    }

    /**
     * Get path of output file from command parameters
     *
     * @param args
     * @return
     */
    private static String getOutputFilepath(String[] args) {
        String retour = null;
        if (args.length > 3) {
            for (int i = 1; i < args.length; i++) {
                if ("-o".equals(args[i]) || "-output".equals(args[i])) {
                    i++;
                    retour = i < args.length ? args[i] : null;
                    break;
                }
            }
        }
        return retour;
    }

    /**
     * Get path of output file from command parameters
     *
     * @param args
     * @return
     */
    private static List<String> getInputFiles(String[] args) {
        List<String> retour = new ArrayList<String>();
        if (args.length > 2) {
            for (int i = 1; i < args.length; i++) {
                if ("-o".equals(args[i]) || "-output".equals(args[i])) {
                    i++;  // skip next parameter which is the output file
                } else {
                    retour.add(args[i]);
                }
            }
        }
        return retour;
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
                "| command      merge           Merge several oifits files                            |");
        System.out.println(
                "| -o or -output <file_path>    Complete path, absolut or relative, for output file   |");
        System.out.println(
                "--------------------------------------------------------------------------------------");
    }

}
