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
package fr.jmmc.oitools.util;

import fr.jmmc.oitools.meta.OIFitsStandard;
import fr.jmmc.oitools.model.OIArray;
import fr.jmmc.oitools.model.OICorr;
import fr.jmmc.oitools.model.OIData;
import fr.jmmc.oitools.model.OIFitsFile;
import fr.jmmc.oitools.model.OITable;
import fr.jmmc.oitools.model.OITarget;
import fr.jmmc.oitools.model.OIWavelength;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 *
 * @author jammetv
 */
public class MergeUtil {

    private static final Logger logger = Logger.getLogger(MergeUtil.class.getName());

    /**
     * Merge of oifits files.
     *
     * @param oiFitsToMerge
     * @return
     * @throws IllegalArgumentException
     */
    public static OIFitsFile mergeOIFitsFiles(final OIFitsFile... oiFitsToMerge) throws IllegalArgumentException {
        return mergeOIFitsFiles(null, null, oiFitsToMerge);
    }

    /**
     * Merge of oifits files. Only one target by input files are accepted, have to be the the same (name) in the input
     * files.
     *
     * @param filters: values to apply on criteria to determine data to keep
     * @param oiFitsToMerge files to be merged
     * @return result of merge
     * @throws IllegalArgumentException
     */
    public static OIFitsFile mergeOIFitsFiles(
            final Selector filters,
            final OIFitsStandard std,
            final OIFitsFile... oiFitsToMerge) throws IllegalArgumentException {

        if (oiFitsToMerge == null || oiFitsToMerge.length < 1) {
            throw new IllegalArgumentException(
                    "Merge: Not enough files as parameters: " + oiFitsToMerge);
        }

        // createOIFits
        OIFitsFile resultFile = new OIFitsFile(std != null ? std : OIFitsStandard.VERSION_1); // TODO default ?

        final Context ctx = new Context(resultFile);
        ctx.filter = filters;

        // Merge metadata FIRST to prepare mappings
        for (OIFitsFile fileToMerge : oiFitsToMerge) {
            // analyze first to use Target objects:
            // fileToMerge.analyze(); // will be usefull in the future
            ctx.fileToMerge = fileToMerge;

            // Prefilter data, and get all useful Ins, Array and Corr in data tables
            
            ctx.dataToKeep = filterOfData(ctx);

            // Process OI_TARGET part: map old name > new name
            processOITarget(ctx);

            // Process OI_WAVELENGTH part
            processOIWL(ctx);

            // Process OI_ARRAY part 
            processOIArray(ctx);

            // Specific to OIFits V2
            if (resultFile.isOIFits2()) {
                // Process OI_CORR part if V2
                processOICorr(ctx);

                // TODO: ....
            }

            // Merge data
            processOIData(ctx);

        }

        return ctx.resultFile;
    }

    /**
     * Merge Target part of OIFitsFile
     *
     * @param oit1
     * @param oit2
     * @throws IllegalArgumentException
     */
    private static void processOITarget(Context ctx) throws IllegalArgumentException {
        OIFitsFile resultFile = ctx.resultFile;
        OITarget oiTarget = ctx.fileToMerge.getOiTarget();
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
        String resultTargetName = (resultFile.getOiTarget() != null && resultFile.getOiTarget().getNbTargets() > 0)
                ? resultFile.getOiTarget().getTarget()[0] : null;
        if (resultTargetName != null && !resultTargetName.equals(targetNameX)) {
            throw new IllegalArgumentException("Merge: files have not the same target");
        }
        // No exception raised, store target 1 in result
        if (resultFile.getOiTarget() == null || resultFile.getOiTarget().getNbTargets() == 0) {
            oiTarget.setOIFitsFile(resultFile); // TODO FIX
            resultFile.addOiTable(oiTarget);
            ctx.targetName = oiTarget.getTarget()[0];
        }
        // TODO: ? clone instance
        logger.info("Target name: " + resultFile.getOiTarget());

    }

    /**
     * Merge the WL part of OIFitsFiles
     *
     * @param result
     * @param f1
     * @param f2
     * @return
     */
    private static void processOIWL(Context ctx) {

        // Browse all names of file to merge, change name if already present in result, add to map 
        // old_name > new name
        if (ctx.fileToMerge != null && ctx.fileToMerge.getOiWavelengths() != null) {
            Set<String> usedNames = ctx.insUsedByData;

            // Browse all OiWavelengths of the file to merge
            for (OIWavelength oiWaveLength : ctx.fileToMerge.getOiWavelengths()) {

                final String oldName = oiWaveLength.getInsName();
                // Only if this OiWavelengths is pointed by some data, keep it
                if (usedNames.contains(oldName)) {
                    // If name is already present in result, 
                    // change the name and memorise this change to update data information later
                    String newName = oldName;
                    int idx = 0;
                    while (ctx.resultFile.getOiWavelength(newName) != null) {
                        idx++;
                        newName = oldName + "_" + idx;
                    }

                    // TODO : clone instance of source data
                    OIWavelength newOiWave = (OIWavelength) copyTable(ctx.resultFile, oiWaveLength);

                    newOiWave.setInsName(newName);
                    ctx.mapWLNames.put(oldName, newName);

                    ctx.resultFile.addOiTable(newOiWave);
                }
            }
        }
        logger.info("mapWLNames: " + ctx.mapWLNames);
        logger.info("insnames: " + Arrays.toString(ctx.resultFile.getAcceptedInsNames()));

    }

    /**
     * Merge Array part of OIFitsFiles
     *
     * @param result
     * @param f1
     * @param f2
     * @return
     */
    private static void processOIArray(Context ctx) {

        // Browse all names of file to merge, change name if already present in result, add to map 
        // old_name > new name
        if (ctx.fileToMerge != null && ctx.fileToMerge.getOiArrays() != null) {
            Set<String> usedNames = ctx.arraysUsedByData;

            // Browse all OIArray of the file to merge
            for (OIArray oiArray : ctx.fileToMerge.getOiArrays()) {

                String oldName = oiArray.getArrName(), newName = oldName;
                // Only if this OIArray is pointed by some data, keep it
                if (usedNames.contains(oldName)) {

                    // If name is already present in result, 
                    // change the name and memorise this change to update data information later
                    int idx = 0;
                    while (ctx.resultFile.getOiArray(newName) != null) {
                        idx++;
                        newName = oldName + "_" + idx;
                    }

                    // TODO : clone instance of source data
                    OIArray newOiArray = (OIArray) copyTable(ctx.resultFile, oiArray);

                    newOiArray.setArrName(newName);
                    ctx.mapArrayNames.put(oldName, newName);

                    ctx.resultFile.addOiTable(newOiArray);
                }
            }
        }
        logger.info("mapArrayNames: " + ctx.mapArrayNames);

    }

    /**
     * Merge Array part of OIFitsFiles
     *
     * @param result
     * @param f1
     * @param f2
     * @return
     */
    private static void processOICorr(Context ctx) {
        // Browse all names of file to merge, change name if already present in result, add to map 
        // old_name > new name
        if (ctx.fileToMerge != null && ctx.fileToMerge.getOiCorr() != null) {
            for (OICorr oiCorr : ctx.fileToMerge.getOiCorr()) {
                String oldName = oiCorr.getCorrName(), newName = oldName;
                int idx = 0;
                while (ctx.resultFile.getOiCorr(newName) != null) {
                    idx++;
                    newName = oldName + "_" + idx;
                }

                // TODO : clone instance of source data
                OICorr newOiCorr = (OICorr) copyTable(ctx.resultFile, oiCorr);

                newOiCorr.setCorrName(newName);
                ctx.mapCorrNames.put(oldName, newName);

                ctx.resultFile.addOiTable(newOiCorr);

            }
        }
        logger.info("mapCorrNames: " + ctx.mapCorrNames);

    }

    private static void processOIData(Context ctx) {

        Map<String, String> mapArrayNames = ctx.mapArrayNames;
        Map<String, String> mapWLNames = ctx.mapWLNames;
        Map<String, String> mapCorrNames = ctx.mapCorrNames;

        // Browse all data
        if (ctx.dataToKeep != null) {
            for (OIData oiData : ctx.dataToKeep) {

                // TODO : clone instance of source data
                OIData newData = (OIData) copyTable(ctx.resultFile, oiData);

                // Change wave and array by new ones
                newData.setInsName(mapWLNames.get(oiData.getInsName()));
                newData.setArrName(mapArrayNames.get(oiData.getArrName()));
                if (mapCorrNames.get(oiData.getCorrName()) != null) {
                    newData.setCorrName(mapCorrNames.get(oiData.getCorrName()));
                }

                ctx.resultFile.addOiTable(newData);

            }
        }
    }

    private static OITable copyTable(OIFitsFile result, OITable oiTable) {

        // TODO : clone instance of source data
        OITable newTable = oiTable;
        newTable.setOIFitsFile(result);

        return newTable;
    }

    /**
     * Browse data tables, filter
     *
     * @param ctx: work data
     * @param dataToFilter: data table to browse and filter
     * @param filterInsName
     * @return
     */
    private static List<OIData> filterOfData(
            Context ctx) {
        List<OIData> dataToFilter = ctx.fileToMerge.getOiDataList();
        List<OIData> dataToKeep = new ArrayList<OIData>();
        String patternInsname = ctx.filter != null ? ctx.filter.getPattern(Selector.INSTRUMENT_FILTER) : null; //"PRIMAMBR";

        if (dataToFilter != null) {
            for (OIData data : dataToFilter) {
                if (patternInsname == null || data.getInsName().equals(patternInsname)) {
                    dataToKeep.add(data);
                    ctx.insUsedByData.add(data.getInsName());
                    ctx.arraysUsedByData.add(data.getArrName());
                    ctx.corrUsedByData.add(data.getCorrName());
                }
            }
        }
        return dataToKeep;
    }

    // Businness méthods
    private static boolean isWLMatching(OIWavelength oiwavelength1, OIWavelength oiwavelength2) {
        if (oiwavelength1 == null || oiwavelength2 != null) {
            return false;
        } else {
            return oiwavelength1.getInsName().equals(oiwavelength2.getInsName());

        }
    }

    /**
     * Hold temporary data of merge operation
     */
    static class Context {

        /**
         * output OIFits
         */
        final OIFitsFile resultFile;

        /**
         * TODO remove later. NAme of the single accepted target
         */
        String targetName = null;
        /**
         * Map to link old name to new name for Ins, Array, Corr
         */
        Map<String, String> mapWLNames = new HashMap<String, String>();
        Map<String, String> mapArrayNames = new HashMap<String, String>();
        Map<String, String> mapCorrNames = new HashMap<String, String>();
        /**
         * List of ins, array, corr to keep after browsing and filter of data
         */
        Set<String> insUsedByData = new HashSet<String>();
        Set<String> arraysUsedByData = new HashSet<String>();
        Set<String> corrUsedByData = new HashSet<String>();
        /**
         * Currently processed file
         */
        OIFitsFile fileToMerge = null;
        /**
         * Handler of filetering valeus and methods
         */
        Selector filter = null;
        
        /**
         * All OIData to keep after filter
         */
        List<OIData> dataToKeep = null;
        

        private Context(final OIFitsFile resultFile) {
            this.resultFile = resultFile;
        }

    }

    /**
     * Hold temporary data of merge operation
     */
    public static class Selector {

        public static final String INSTRUMENT_FILTER = "INSNAME_PATTERN";

        final private Map<String, String> patterns = new HashMap<String, String>();

        public void addPattern(String name, String value) {
            patterns.put(name, value);
        }

        public String getPattern(String name) {
            return patterns.get(name);
        }
    }

}
