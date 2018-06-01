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
package fr.jmmc.oitools.processing;

import fr.jmmc.oitools.meta.OIFitsStandard;
import fr.jmmc.oitools.model.OIArray;
import fr.jmmc.oitools.model.OICorr;
import fr.jmmc.oitools.model.OIData;
import fr.jmmc.oitools.model.OIFitsFile;
import fr.jmmc.oitools.model.OITarget;
import fr.jmmc.oitools.model.OIWavelength;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Processing class to merge given OIFitsFile instances into a single OIFitsFile instance
 * @author jammetv
 */
public final class Merger {

    private static final Logger logger = Logger.getLogger(Merger.class.getName());

    /** Utility class */
    private Merger() {
        super();
    }

    /**
     * Merge of OIFitsFile structure.
     *
     * @param oiFitsToMerge OIFitsFile instances
     * @return new OIFitsFile instance
     * @throws IllegalArgumentException
     */
    public static OIFitsFile process(final OIFitsFile... oiFitsToMerge) throws IllegalArgumentException {
        return process(null, oiFitsToMerge);
    }

    /**
     * Merge of OIFitsFile structure.
     *
     * @param selector optional Selector instance to filter OIFits content
     * @param oiFitsToMerge OIFitsFile instances
     * @return new OIFitsFile instance
     * @throws IllegalArgumentException
     */
    public static OIFitsFile process(final Selector selector, final OIFitsFile... oiFitsToMerge) throws IllegalArgumentException {
        return process(selector, null, oiFitsToMerge);
    }

    /**
     * Merge of OIFitsFile structure.
     * Note: only one target by input files are accepted and must be the the same (name)
     *
     * @param selector optional Selector instance to filter OIFits content
     * @param std OIFits standard for the output OIFitsFile
     * @param oiFitsToMerge OIFitsFile instances
     * @return new OIFitsFile instance
     * @throws IllegalArgumentException
     */
    public static OIFitsFile process(final Selector selector, final OIFitsStandard std,
                                     final OIFitsFile... oiFitsToMerge) throws IllegalArgumentException {

        if (oiFitsToMerge == null || oiFitsToMerge.length < 1) {
            throw new IllegalArgumentException("Merge: Missing OIFits inputs");
        }

        // createOIFits
        final OIFitsFile resultFile = createOIFits(std, oiFitsToMerge);

        final Context ctx = new Context(selector, resultFile);

        // Process input files:
        for (OIFitsFile fileToMerge : oiFitsToMerge) {
            // analyze first to use Target objects:
            // fileToMerge.analyze(); // will be usefull in the future
            ctx.fileToMerge = fileToMerge;

            // Prefilter data, and get all useful Ins, Array and Corr in data tables
            filterOfData(ctx);

            /*  Merge metadata FIRST to prepare mappings */
            // Process OI_TARGET:
            processOITarget(ctx);

            // Process OI_WAVELENGTH tables
            processOIWL(ctx);

            // Process OI_ARRAY tables 
            processOIArray(ctx);

            // Specific to OIFits V2
            if (resultFile.isOIFits2()) {
                // Process OI_CORR tables
                processOICorr(ctx);

                // TODO: ....
            }

            /* Merge OI_DATA tables */
            processOIData(ctx);
        }

        return resultFile;
    }

    private static OIFitsFile createOIFits(final OIFitsStandard std, final OIFitsFile[] oiFitsToMerge) {
        OIFitsStandard version = std;

        if (version == null) {
            // Use the highest version from the given OIFitsFile instances:
            for (OIFitsFile oiFits : oiFitsToMerge) {
                if (version == null || oiFits.getVersion().ordinal() > version.ordinal()) {
                    version = oiFits.getVersion();
                }
            }
        }

        final OIFitsFile resultFile = new OIFitsFile(version);
        // TODO: if V2 => use setPrimaryImageHdu() to fill V2 keywords

        return resultFile;
    }

    /**
     * Merge OITarget
     *
     * @param ctx merge context
     * @throws IllegalArgumentException
     */
    private static void processOITarget(final Context ctx) throws IllegalArgumentException {
        final OIFitsFile fileToMerge = ctx.fileToMerge;
        final OIFitsFile resultFile = ctx.resultFile;

        final OITarget oiTargetInput = fileToMerge.getOiTarget();

        if (oiTargetInput == null || oiTargetInput.getNbRows() < 1) {
            throw new IllegalArgumentException(String.format("Merge: target for %s is null or empty", fileToMerge.getAbsoluteFilePath()));
        }
        if (oiTargetInput.getNbRows() > 1) {
            throw new IllegalArgumentException("Merge: more than one target for " + fileToMerge.getAbsoluteFilePath());
        }
        final String targetName = oiTargetInput.getTarget()[0];
        if (targetName == null || targetName.length() == 0) {
            throw new IllegalArgumentException("Merge: empty target name for " + fileToMerge.getAbsoluteFilePath());
        }

        final OITarget oiTargetOutput = resultFile.getOiTarget();

        // No exception raised, store target 1 in result
        if (oiTargetOutput == null || oiTargetOutput.getNbTargets() == 0) {
            logger.log(Level.INFO, "Target name: {0}", targetName);

            ctx.targetName = targetName;

            final OITarget newOiTarget = (OITarget) resultFile.copyTable(oiTargetInput);
            resultFile.addOiTable(newOiTarget);
        } else {
            final String resultTargetName = oiTargetOutput.getTarget()[0];

            if (!targetName.equals(resultTargetName)) {
                throw new IllegalArgumentException("Merge: files have not the same target [" + targetName + "] vs [" + resultTargetName + "]");
            }
        }
    }

    /**
     * Merge OIWavelength tables
     *
     * @param ctx merge context
     */
    private static void processOIWL(final Context ctx) {
        final OIFitsFile fileToMerge = ctx.fileToMerge;

        // Browse all names of file to merge, change name if already present in result, 
        // add to map old_name > new name
        if (fileToMerge.hasOiWavelengths()) {
            final OIFitsFile resultFile = ctx.resultFile;
            final Set<String> usedNames = ctx.insUsedByData;
            final Map<String, String> mapInsNames = ctx.mapInsNames;

            // Browse all OiWavelengths of the file to merge
            for (OIWavelength oiWaveLength : fileToMerge.getOiWavelengths()) {
                final String oldName = oiWaveLength.getInsName();

                // Only if this OiWavelengths is pointed by some data, keep it
                if (usedNames.contains(oldName)) {
                    // If name is already present in result, 
                    // change the name and memorise this change to update data information later
                    String newName = oldName;
                    int idx = 0;
                    while (resultFile.getOiWavelength(newName) != null) {
                        idx++;
                        newName = oldName + "_" + idx;
                    }

                    final OIWavelength newOiWave = (OIWavelength) resultFile.copyTable(oiWaveLength);
                    newOiWave.setInsName(newName);
                    resultFile.addOiTable(newOiWave);

                    mapInsNames.put(oldName, newName);
                }
            }
            logger.log(Level.INFO, "mapWLNames: {0}", mapInsNames);
            logger.log(Level.INFO, "insnames:   {0}", Arrays.toString(resultFile.getAcceptedInsNames()));
        }
    }

    /**
     * Merge Array part of OIFitsFiles
     *
     * @param ctx merge context
     */
    private static void processOIArray(final Context ctx) {
        final OIFitsFile fileToMerge = ctx.fileToMerge;

        // Browse all names of file to merge, change name if already present in result, 
        // add to map old_name > new name
        if (fileToMerge.hasOiArray()) {
            final OIFitsFile resultFile = ctx.resultFile;
            final Set<String> usedNames = ctx.arraysUsedByData;
            final Map<String, String> mapArrayNames = ctx.mapArrayNames;

            // Browse all OIArray of the file to merge
            for (OIArray oiArray : fileToMerge.getOiArrays()) {
                final String oldName = oiArray.getArrName();

                // Only if this OIArray is pointed by some data, keep it
                if (usedNames.contains(oldName)) {
                    // If name is already present in result, 
                    // change the name and memorise this change to update data information later
                    String newName = oldName;
                    int idx = 0;
                    while (resultFile.getOiArray(newName) != null) {
                        idx++;
                        newName = oldName + "_" + idx;
                    }

                    final OIArray newOiArray = (OIArray) resultFile.copyTable(oiArray);
                    newOiArray.setArrName(newName);
                    resultFile.addOiTable(newOiArray);

                    mapArrayNames.put(oldName, newName);
                }
            }
            logger.log(Level.INFO, "mapArrayNames: {0}", mapArrayNames);
            logger.log(Level.INFO, "arrnames:      {0}", Arrays.toString(resultFile.getAcceptedArrNames()));
        }
    }

    /**
     * Merge Array part of OIFitsFiles
     *
     * @param ctx merge context
     */
    private static void processOICorr(final Context ctx) {
        final OIFitsFile fileToMerge = ctx.fileToMerge;

        // Browse all names of file to merge, change name if already present in result, add to map 
        // old_name > new name
        if (fileToMerge.hasOiCorr()) {
            final OIFitsFile resultFile = ctx.resultFile;
            final Set<String> usedNames = ctx.corrUsedByData;
            final Map<String, String> mapCorrNames = ctx.mapCorrNames;

            // Browse all OICorr of the file to merge
            for (OICorr oiCorr : fileToMerge.getOiCorrs()) {
                final String oldName = oiCorr.getCorrName();

                // Only if this OICorr is pointed by some data, keep it
                if (usedNames.contains(oldName)) {
                    // If name is already present in result, 
                    // change the name and memorise this change to update data information later
                    String newName = oldName;
                    int idx = 0;
                    while (resultFile.getOiCorr(newName) != null) {
                        idx++;
                        newName = oldName + "_" + idx;
                    }

                    final OICorr newOiCorr = (OICorr) resultFile.copyTable(oiCorr);
                    newOiCorr.setCorrName(newName);
                    resultFile.addOiTable(newOiCorr);

                    mapCorrNames.put(oldName, newName);
                }
            }
            logger.log(Level.INFO, "mapCorrNames: {0}", mapCorrNames);
            logger.log(Level.INFO, "corrnames:    {0}", Arrays.toString(resultFile.getAcceptedCorrNames()));
        }
    }

    /**
    
     * @param ctx merge context
     */
    private static void processOIData(final Context ctx) {
        final List<OIData> dataToKeep = ctx.dataToKeep;

        if (!dataToKeep.isEmpty()) {
            final OIFitsFile resultFile = ctx.resultFile;

            final Map<String, String> mapArrayNames = ctx.mapArrayNames;
            final Map<String, String> mapInsNames = ctx.mapInsNames;
            final Map<String, String> mapCorrNames = ctx.mapCorrNames;

            for (OIData oiData : dataToKeep) {
                // Change ARRNAME, INSNAME & CORRNAME keywords:
                final String newArrName = mapArrayNames.get(oiData.getArrName());
                // What to do if not found (invalid ref) ?
                if (newArrName == null) {
                    logger.info("Invalid ARRNAME[" + oiData.getArrName() + "] found !");
                    continue;
                }

                final String newInsName = mapInsNames.get(oiData.getInsName());
                // What to do if not found (invalid ref) ?
                if (newInsName == null) {
                    logger.info("Invalid INSNAME[" + oiData.getInsName() + "] found !");
                    continue;
                }

                String newCorrName = null;
                // optional:
                if (oiData.getCorrName() != null) {
                    newCorrName = mapCorrNames.get(oiData.getCorrName());
                    // What to do if not found (invalid ref) ?
                    if (newCorrName == null) {
                        logger.info("Invalid CORRNAME[" + oiData.getCorrName() + "] found !");
                        continue;
                    }
                }

                final OIData newOIData = (OIData) resultFile.copyTable(oiData);
                newOIData.setArrName(newArrName);
                newOIData.setInsName(newInsName);

                if (newCorrName != null) {
                    newOIData.setCorrName(newCorrName);
                }

                resultFile.addOiTable(newOIData);
            }
        }
    }

    /**
     * Filter OiData tables
     *
     * @param ctx merge context
     */
    private static void filterOfData(final Context ctx) {
        final OIFitsFile fileToMerge = ctx.fileToMerge;

        final List<OIData> dataToKeep = ctx.dataToKeep;
        // reset
        dataToKeep.clear();

        if (fileToMerge.hasOiData()) {
            final Selector selector = ctx.selector;

            for (OIData data : fileToMerge.getOiDataList()) {
                // delegate to generic Selector:
                if (selector == null || selector.match(data)) {
                    dataToKeep.add(data);
                    ctx.insUsedByData.add(data.getInsName());
                    ctx.arraysUsedByData.add(data.getArrName());

                    if (data.getCorrName() != null) {
                        ctx.corrUsedByData.add(data.getCorrName());
                    }
                }
            }
        }
    }

    /**
     * Hold temporary data of merge operation
     */
    static final class Context {

        /** Optional Selector */
        final Selector selector;

        /** output OIFits */
        final OIFitsFile resultFile;

        /** Currently processed file */
        OIFitsFile fileToMerge = null;

        /**
         * TODO remove later. NAme of the single accepted target
         */
        String targetName = null;
        /**
         * Map to link old name to new name for Ins, Array, Corr
         */
        final Map<String, String> mapInsNames = new HashMap<String, String>();
        final Map<String, String> mapArrayNames = new HashMap<String, String>();
        final Map<String, String> mapCorrNames = new HashMap<String, String>();
        /**
         * List of ins, array, corr to keep after browsing and filter of data
         */
        final Set<String> insUsedByData = new HashSet<String>();
        final Set<String> arraysUsedByData = new HashSet<String>();
        final Set<String> corrUsedByData = new HashSet<String>();

        /** All OIData to keep after filter */
        final List<OIData> dataToKeep = new ArrayList<OIData>();

        private Context(final Selector selector, final OIFitsFile resultFile) {
            this.selector = selector;
            this.resultFile = resultFile;
        }
    }

}
