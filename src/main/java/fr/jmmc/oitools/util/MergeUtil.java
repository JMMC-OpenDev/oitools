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

//    public static OIFitsFile mergeOIFitsFile(
//            final OIFitsFile oifitsOne, final OIFitsFile oifitsTwo)
//            throws IllegalArgumentException {
//        return mergeOIFitsFiles(oifitsOne, oifitsTwo);
//    }
    /**
     * Merge of two oifits files. Only one target by input files are accepted, have to be the the same (name) in the
     * input files.
     *
     * @param filters: values to apply on criteria to determine data to keep
     * @param oiFitsToMerge files to be merged
     * @return result of merge
     * @throws IllegalArgumentException
     */
    public static OIFitsFile mergeOIFitsFiles(
            final Selector filters,
            final OIFitsFile... oiFitsToMerge)
            throws IllegalArgumentException {

        final Context context = new Context();

        // TODO: clone fits structure by save/load on disk ? 
        // aims: get local copy modifiable
        if (oiFitsToMerge == null || oiFitsToMerge.length < 1) {
            throw new IllegalArgumentException(
                    "Merge: Not enough files as parameters: " + oiFitsToMerge);
        }

        // Merge metadata FIRST to prepare mappings
        for (OIFitsFile fileToMerge : oiFitsToMerge) {
            // analyze first to use Target objects:
            // fileToMerge.analyze(); // will be usefull in the future

            // Process OI_TARGET part
            OITarget oiTarget = fileToMerge.getOiTarget();
            checkOITarget(context, oiTarget);

            // No exception raised, store target 1 in result
            if (context.getResultFile().getOiTarget() == null || context.getResultFile().getOiTarget().getNbTargets() == 0) {
                oiTarget.setOIFitsFile(context.getResultFile()); // TODO FIX
                context.getResultFile().addOiTable(oiTarget);
                context.setTargetName(oiTarget.getTarget()[0]);
            }
            // TODO: ? clone instance
            logger.info("Target name: " + context.getResultFile().getOiTarget());

            // Prefilter data, and get all useful Ins, Array and Corr in data tables
            List<OIData> dataOfTarget = fileToMerge.getOiDataList();
            List<OIData> dataToKeep = preFilterOfData(context, dataOfTarget, filters);

            // Process OI_WAVELENGTH part
            mergeOIWL(context, fileToMerge);
            logger.info("mapWLNames: " + context.getMapWLNames());
            logger.info("insnames: " + Arrays.toString(context.getResultFile().getAcceptedInsNames()));

            // Process OI_ARRAY part 
            mergeOIArray(context, fileToMerge);
            logger.info("mapArrayNames: " + context.getMapArrayNames());

            // Process OI_CORR part if V2
            if (context.getResultFile().getVersion().equals(OIFitsStandard.VERSION_2)) {
                mergeOICorr(context, fileToMerge);
                logger.info("mapCorrNames: " + context.getMapCorrNames());
            }

            // Merge data
            mergeOIData(context, fileToMerge.getOiDataList(context.getTargetName()));

        }

        return context.getResultFile();
    }

    /**
     * Merge Target part of OIFitsFile
     *
     * @param oit1
     * @param oit2
     * @throws IllegalArgumentException
     */
    private static void checkOITarget(Context context, OITarget oiTarget) throws IllegalArgumentException {
        OIFitsFile resultFile = context.getResultFile();
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
    }

    /**
     * Merge the WL part of OIFitsFiles
     *
     * @param result
     * @param f1
     * @param f2
     * @return
     */
    private static void mergeOIWL(Context context, OIFitsFile fileToMerge) {
        // Browse all names of file to merge, change name if already present in result, add to map 
        // old_name > new name
        if (fileToMerge != null && fileToMerge.getOiWavelengths() != null) {
            for (OIWavelength oiWaveLength : fileToMerge.getOiWavelengths()) {
                final String oldName = oiWaveLength.getInsName();
                String newName = oldName;
                int idx = 0;
                while (context.getResultFile().getOiWavelength(newName) != null) {
                    idx++;
                    newName = oldName + "_" + idx;
                }

                // TODO : clone instance of source data
                OIWavelength newOiWave = (OIWavelength) copyTable(context.getResultFile(), oiWaveLength);

                newOiWave.setInsName(newName);
                context.getMapWLNames().put(oldName, newName);

                context.getResultFile().addOiTable(newOiWave);
            }
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
    private static void mergeOIArray(Context context, OIFitsFile fileToMerge) {
        // Browse all names of file to merge, change name if already present in result, add to map 
        // old_name > new name
        if (fileToMerge != null && fileToMerge.getOiArrays() != null) {
            for (OIArray oiArray : fileToMerge.getOiArrays()) {
                String oldName = oiArray.getArrName(), newName = oldName;
                int idx = 0;
                while (context.getResultFile().getOiArray(newName) != null) {
                    idx++;
                    newName = oldName + "_" + idx;
                }

                // TODO : clone instance of source data
                OIArray newOiArray = (OIArray) copyTable(context.getResultFile(), oiArray);

                newOiArray.setArrName(newName);
                context.getMapArrayNames().put(oldName, newName);

                context.getResultFile().addOiTable(newOiArray);
            }
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
    private static void mergeOICorr(Context context, OIFitsFile fileToMerge) {
        // Browse all names of file to merge, change name if already present in result, add to map 
        // old_name > new name
        if (fileToMerge != null && fileToMerge.getOiCorr() != null) {
            for (OICorr oiCorr : fileToMerge.getOiCorr()) {
                String oldName = oiCorr.getCorrName(), newName = oldName;
                int idx = 0;
                while (context.getResultFile().getOiCorr(newName) != null) {
                    idx++;
                    newName = oldName + "_" + idx;
                }

                // TODO : clone instance of source data
                OICorr newOiCorr = (OICorr) copyTable(context.getResultFile(), oiCorr);

                newOiCorr.setCorrName(newName);
                context.getMapCorrNames().put(oldName, newName);

                context.getResultFile().addOiTable(newOiCorr);

            }
        }
    }

    private static void mergeOIData(Context context, List<OIData> dataToMerge) {

        Map<String, String> mapArrayNames = context.getMapArrayNames();
        Map<String, String> mapWLNames = context.getMapWLNames();
        Map<String, String> mapCorrNames = context.getMapCorrNames();

        // Browse all data
        if (dataToMerge != null) {
            for (OIData oiData : dataToMerge) {

                // TODO : clone instance of source data
                OIData newData = (OIData) copyTable(context.getResultFile(), oiData);

                // Change wave and array by new ones
                newData.setInsName(mapWLNames.get(oiData.getInsName()));
                newData.setArrName(mapArrayNames.get(oiData.getArrName()));
                if (mapCorrNames.get(oiData.getCorrName()) != null) {
                    newData.setCorrName(mapCorrNames.get(oiData.getCorrName()));
                }

                context.getResultFile().addOiTable(newData);

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
    private static List<OIData> preFilterOfData(
            Context context, List<OIData> dataToFilter, Selector filters) {
        List<OIData> dataToKeep = new ArrayList<OIData>();
        String patternInsname = filters != null ? filters.getPattern(Selector.INSTRUMENT_FILTER) : null; //"PRIMAMBR";

        if (dataToFilter != null) {
            for (OIData data : dataToFilter) {
                if (patternInsname == null || data.getInsName().equals(patternInsname)) {
                    dataToKeep.add(data);
                    context.getInsUsedByData().add(data.getInsName());
                    context.getArraysUsedByData().add(data.getArrName());
                    context.getCorrUsedByData().add(data.getCorrName());
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
    public static class Context {

        private String targetName = null;
        private OIFitsFile resultFile = new OIFitsFile(OIFitsStandard.VERSION_1);
        private Map<String, String> mapWLNames = new HashMap<String, String>();
        private Map<String, String> mapArrayNames = new HashMap<String, String>();
        private Map<String, String> mapCorrNames = new HashMap<String, String>();
        Set<String> insUsedByData = new HashSet<String>();
        Set<String> arraysUsedByData = new HashSet<String>();
        Set<String> corrUsedByData = new HashSet<String>();

        public Set<String> getInsUsedByData() {
            return insUsedByData;
        }

        public void setInsUsedByData(Set<String> insUsedByData) {
            this.insUsedByData = insUsedByData;
        }

        public Set<String> getArraysUsedByData() {
            return arraysUsedByData;
        }

        public void setArraysUsedByData(Set<String> arraysUsedByData) {
            this.arraysUsedByData = arraysUsedByData;
        }

        public Set<String> getCorrUsedByData() {
            return corrUsedByData;
        }

        public void setCorrUsedByData(Set<String> corrUsedByData) {
            this.corrUsedByData = corrUsedByData;
        }

        public OIFitsFile getResultFile() {
            return resultFile;
        }

        public void setResultFile(OIFitsFile resultFile) {
            this.resultFile = resultFile;
        }

        public String getTargetName() {
            return targetName;
        }

        public void setTargetName(String targetName) {
            this.targetName = targetName;
        }

        public Map<String, String> getMapWLNames() {
            return mapWLNames;
        }

        public void setMapWLNames(Map<String, String> mapWLNames) {
            this.mapWLNames = mapWLNames;
        }

        public Map<String, String> getMapArrayNames() {
            return mapArrayNames;
        }

        public void setMapArrayNames(Map<String, String> mapArrayNames) {
            this.mapArrayNames = mapArrayNames;
        }

        public Map<String, String> getMapCorrNames() {
            return mapCorrNames;
        }

        public void setMapCorrNames(Map<String, String> mapCorrNames) {
            this.mapCorrNames = mapCorrNames;
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
