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

        final Context context = new Context(resultFile);

        // Merge metadata FIRST to prepare mappings
        for (OIFitsFile fileToMerge : oiFitsToMerge) {
            // analyze first to use Target objects:
            // fileToMerge.analyze(); // will be usefull in the future

            // Prefilter data, and get all useful Ins, Array and Corr in data tables
            List<OIData> dataOfTarget = fileToMerge.getOiDataList();
            List<OIData> dataToKeep = filterOfData(context, dataOfTarget, filters);

            // Process OI_TARGET part
            OITarget oiTarget = fileToMerge.getOiTarget();
            processOITarget(context, oiTarget);

            // Process OI_WAVELENGTH part
            processOIWL(context, fileToMerge);

            // Process OI_ARRAY part 
            processOIArray(context, fileToMerge);

            // Specific to OIFits V2
            if (resultFile.isOIFits2()) {
                // Process OI_CORR part if V2
                processOICorr(context, fileToMerge);

                // TODO: ....
            }

            // Merge data
            processOIData(context, dataToKeep);

        }

        return context.resultFile;
    }

    /**
     * Merge Target part of OIFitsFile
     *
     * @param oit1
     * @param oit2
     * @throws IllegalArgumentException
     */
    private static void processOITarget(Context context, OITarget oiTarget) throws IllegalArgumentException {
        OIFitsFile resultFile = context.resultFile;
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
            context.targetName = oiTarget.getTarget()[0];
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
    private static void processOIWL(Context context, OIFitsFile fileToMerge) {

        // Browse all names of file to merge, change name if already present in result, add to map 
        // old_name > new name
        if (fileToMerge != null && fileToMerge.getOiWavelengths() != null) {
            Set<String> usedNames = context.insUsedByData;

            // Browse all OiWavelengths of the file to merge
            for (OIWavelength oiWaveLength : fileToMerge.getOiWavelengths()) {

                final String oldName = oiWaveLength.getInsName();
                // Only if this OiWavelengths is pointed by some data, keep it
                if (usedNames.contains(oldName)) {
                    // If name is already present in result, 
                    // change the name and memorise this change to update data information later
                    String newName = oldName;
                    int idx = 0;
                    while (context.resultFile.getOiWavelength(newName) != null) {
                        idx++;
                        newName = oldName + "_" + idx;
                    }

                    // TODO : clone instance of source data
                    OIWavelength newOiWave = (OIWavelength) copyTable(context.resultFile, oiWaveLength);

                    newOiWave.setInsName(newName);
                    context.mapWLNames.put(oldName, newName);

                    context.resultFile.addOiTable(newOiWave);
                }
            }
        }
        logger.info("mapWLNames: " + context.mapWLNames);
        logger.info("insnames: " + Arrays.toString(context.resultFile.getAcceptedInsNames()));

    }

    /**
     * Merge Array part of OIFitsFiles
     *
     * @param result
     * @param f1
     * @param f2
     * @return
     */
    private static void processOIArray(Context context, OIFitsFile fileToMerge) {

        // Browse all names of file to merge, change name if already present in result, add to map 
        // old_name > new name
        if (fileToMerge != null && fileToMerge.getOiArrays() != null) {
            Set<String> usedNames = context.insUsedByData;

            // Browse all OIArray of the file to merge
            for (OIArray oiArray : fileToMerge.getOiArrays()) {

                String oldName = oiArray.getArrName(), newName = oldName;
                // Only if this OIArray is pointed by some data, keep it
                if (usedNames.contains(oldName)) {

                    // If name is already present in result, 
                    // change the name and memorise this change to update data information later
                    int idx = 0;
                    while (context.resultFile.getOiArray(newName) != null) {
                        idx++;
                        newName = oldName + "_" + idx;
                    }

                    // TODO : clone instance of source data
                    OIArray newOiArray = (OIArray) copyTable(context.resultFile, oiArray);

                    newOiArray.setArrName(newName);
                    context.mapArrayNames.put(oldName, newName);

                    context.resultFile.addOiTable(newOiArray);
                }
            }
        }
        logger.info("mapArrayNames: " + context.mapArrayNames);

    }

    /**
     * Merge Array part of OIFitsFiles
     *
     * @param result
     * @param f1
     * @param f2
     * @return
     */
    private static void processOICorr(Context context, OIFitsFile fileToMerge) {
        // Browse all names of file to merge, change name if already present in result, add to map 
        // old_name > new name
        if (fileToMerge != null && fileToMerge.getOiCorr() != null) {
            for (OICorr oiCorr : fileToMerge.getOiCorr()) {
                String oldName = oiCorr.getCorrName(), newName = oldName;
                int idx = 0;
                while (context.resultFile.getOiCorr(newName) != null) {
                    idx++;
                    newName = oldName + "_" + idx;
                }

                // TODO : clone instance of source data
                OICorr newOiCorr = (OICorr) copyTable(context.resultFile, oiCorr);

                newOiCorr.setCorrName(newName);
                context.mapCorrNames.put(oldName, newName);

                context.resultFile.addOiTable(newOiCorr);

            }
        }
        logger.info("mapCorrNames: " + context.mapCorrNames);

    }

    private static void processOIData(Context context, List<OIData> dataToMerge) {

        Map<String, String> mapArrayNames = context.mapArrayNames;
        Map<String, String> mapWLNames = context.mapWLNames;
        Map<String, String> mapCorrNames = context.mapCorrNames;

        // Browse all data
        if (dataToMerge != null) {
            for (OIData oiData : dataToMerge) {

                // TODO : clone instance of source data
                OIData newData = (OIData) copyTable(context.resultFile, oiData);

                // Change wave and array by new ones
                newData.setInsName(mapWLNames.get(oiData.getInsName()));
                newData.setArrName(mapArrayNames.get(oiData.getArrName()));
                if (mapCorrNames.get(oiData.getCorrName()) != null) {
                    newData.setCorrName(mapCorrNames.get(oiData.getCorrName()));
                }

                context.resultFile.addOiTable(newData);

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
     * @param context: work data
     * @param dataToFilter: data table to browse and filter
     * @param filterInsName
     * @return
     */
    private static List<OIData> filterOfData(
            Context context, List<OIData> dataToFilter, Selector filters) {
        List<OIData> dataToKeep = new ArrayList<OIData>();
        String patternInsname = filters != null ? filters.getPattern(Selector.INSTRUMENT_FILTER) : null; //"PRIMAMBR";

        if (dataToFilter != null) {
            for (OIData data : dataToFilter) {
                if (patternInsname == null || data.getInsName().equals(patternInsname)) {
                    dataToKeep.add(data);
                    context.insUsedByData.add(data.getInsName());
                    context.arraysUsedByData.add(data.getArrName());
                    context.corrUsedByData.add(data.getCorrName());
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
         *
         */
        Map<String, String> mapWLNames = new HashMap<String, String>();
        Map<String, String> mapArrayNames = new HashMap<String, String>();
        Map<String, String> mapCorrNames = new HashMap<String, String>();
        Set<String> insUsedByData = new HashSet<String>();
        Set<String> arraysUsedByData = new HashSet<String>();
        Set<String> corrUsedByData = new HashSet<String>();

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
