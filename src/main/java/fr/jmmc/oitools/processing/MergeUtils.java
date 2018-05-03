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
import fr.jmmc.oitools.model.OIData;
import fr.jmmc.oitools.model.OIFitsFile;
import fr.jmmc.oitools.model.OITable;
import fr.jmmc.oitools.model.OITarget;
import fr.jmmc.oitools.model.OIWavelength;
import fr.jmmc.oitools.model.Target;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 *
 * @author jammetv
 */
public class MergeUtils {

    private static final Logger logger = Logger.getLogger(MergeUtils.class.getName());

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
     * Indicate if 2 targets can be considered as similar regarding their polar distance
     *
     * @param target1
     * @param target2
     * @param distance in sec, if null 1 sec is used.
     * @return
     */
    public static boolean areTargetsSimilar(final Target target1, final Target target2, Double distance) {
        // BigDecimal pour la précision (16 chiffres sign par défaut). Ici présicion de 34 
        // Avec doubble 10.0*0.09 donne 0.8999999999999 !)
        final BigDecimal squareDistanceReference
                = BigDecimal.valueOf(distance != null ? distance : 1.0).pow(2);
        final MathContext precision = MathContext.DECIMAL64;
        final BigDecimal ra1
                = new BigDecimal(target1.getRaEp0(), precision).multiply(BigDecimal.valueOf(3600.0)); // deg to sec
        final BigDecimal dec1
                = new BigDecimal(target1.getDecEp0(), precision).multiply(BigDecimal.valueOf(3600.0)); // deg to sec
        final BigDecimal ra2
                = new BigDecimal(target2.getRaEp0(), precision).multiply(BigDecimal.valueOf(3600.0)); // deg to sec
        final BigDecimal dec2
                = new BigDecimal(target2.getDecEp0(), precision).multiply(BigDecimal.valueOf(3600.0)); // deg to sec
        final BigDecimal squareTargetDistance = ra2.subtract(ra1).pow(2).add(dec2.subtract(dec1).pow(2));
        return squareTargetDistance.compareTo(squareDistanceReference) < 0;
    }

}
