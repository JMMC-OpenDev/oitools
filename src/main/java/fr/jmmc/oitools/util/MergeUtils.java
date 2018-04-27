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
import fr.jmmc.oitools.model.OIData;
import fr.jmmc.oitools.model.OIFitsFile;
import fr.jmmc.oitools.model.OITarget;
import fr.jmmc.oitools.model.OIWavelength;
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
     * @param f1 first file to be merged
     * @param f2 second file to be merged
     * @return result of merge
     * @throws IllegalArgumentException
     */
    public static OIFitsFile mergeOIFitsFile(
            final OIFitsFile f1, final OIFitsFile f2)
            throws IllegalArgumentException {

        // TODO: clone fits structure by save/load on disk ? 
        // aims: get local copy modifiable
        // UUID.;
        // add some data in structures
        f1.analyze();
        f2.analyze();

        OIFitsFile result = new OIFitsFile(OIFitsStandard.VERSION_1);

        // Process OI_TARGET part
        OITarget target1 = f1.getOiTarget();
        OITarget target2 = f2.getOiTarget();
        checkOITarget(target1, target2);
        // No exception raised, store target 1 in result
        target1.setOIFitsFile(result);
        result.addOiTable(target1);
        // TODO: ? cloner l'instance
        logger.info("Target name: " + result.getOiTarget());

        // Process OI_WAVELENGTH part
        Map<String, String> mapWLNames = mergeOIWL(result, f1, f2);
        logger.info("mapWLNames: "+mapWLNames);
        logger.info("insnames: "+Arrays.toString(result.getAcceptedInsNames()));

        // Process OI_ARRAY part 
        Map<String, String> mapArrayNames = mergeOIArray(result, f1, f2);
        logger.info("mapArrayNames: "+mapArrayNames);
        
        // Merge data
        mergeOIData(result, f1, f2, mapWLNames, mapArrayNames);

        return result;
    }

    /**
     * Merge Target part of OIFitsFile
     *
     * @param oit1
     * @param oit2
     * @throws IllegalArgumentException
     */
    private static void checkOITarget(OITarget oit1, OITarget oit2) throws IllegalArgumentException {
        if (oit1 == null || oit1.getNbRows() < 1) {
            throw new IllegalArgumentException("Merge: first file has null or empty target");
        }
        if (oit1.getNbRows() > 1) {
            throw new IllegalArgumentException("Merge: first file has more than one target");
        }
        if (oit2 == null || oit2.getNbRows() < 1) {
            throw new IllegalArgumentException("Merge: second file has null or empty target");
        }
        if (oit2.getNbRows() > 1) {
            throw new IllegalArgumentException("Merge: second file has more than one target");
        }

        String targetName1 = oit1.getTarget()[0];
        if (targetName1 == null || targetName1.length() == 0) {
            throw new IllegalArgumentException("Merge: first file has a null or empty name for target");
        }
        String t2 = oit2.getTarget()[0];
        if (!targetName1.equals(t2)) {
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
    private static Map< String, String> mergeOIWL(OIFitsFile result, OIFitsFile f1, OIFitsFile f2) {
        // Collect all WL names of first file, store each WL in result list
        for (OIWavelength oiwl : f1.getOiWavelengths()) {
            oiwl.setOIFitsFile(result);
            result.addOiTable(oiwl);
        }
        // Browse all names of second file, change name if already present in first file, build a map 
        // old_name > new name
        Map<String, String> mapWLNames = new HashMap<String, String>();

        for (OIWavelength oiwl : f2.getOiWavelengths()) {
            final String oldName = oiwl.getInsName();
            String newName = oldName;
            int idx = 0;
            while (result.getOiWavelength(newName) != null) {
                idx++;
                newName = oldName + "_" + idx;
            }
            // before modify: TODO clone
            oiwl.setInsName(newName);
            mapWLNames.put(oldName, newName);
            oiwl.setOIFitsFile(result);
            result.addOiTable(oiwl);
        }
        return mapWLNames;
    }

    /**
     * Merge Array part of OIFitsFiles
     *
     * @param result
     * @param f1
     * @param f2
     * @return
     */
    private static Map< String, String> mergeOIArray(OIFitsFile result, OIFitsFile f1, OIFitsFile f2) {
        // Collect all WL names of first file, store each WL in result list
        for (OIArray oiwl : f1.getOiArrays()) {
            oiwl.setOIFitsFile(result);
            result.addOiTable(oiwl);
        }
        // Browse all names of second file, change name if already present in first file, build a map 
        // old_name > new name
        Map<String, String> mapArrayNames = new HashMap<String, String>();

        for (OIArray oiwl : f2.getOiArrays()) {
            String oldName = oiwl.getArrName(), newName = oldName;
            int idx = 0;
            while (result.getOiArray(newName) != null) {
                idx++;
                newName = oldName + "_" + idx;
            }
            // before modify: TODO clone
            oiwl.setArrName(newName);
            mapArrayNames.put(oldName, newName);
            oiwl.setOIFitsFile(result);
            result.addOiTable(oiwl);
        }
        return mapArrayNames;
    }

    
    private static void mergeOIData(
            OIFitsFile result, OIFitsFile f1, OIFitsFile f2,
            Map<String,String> mapWLNames, Map<String,String> mapArrayNames) {
        
        // Process Data VIS 
        // Store all oivis of file 1
        
        for (OIData oiData : f1.getOiDatas()) {
            oiData.setOIFitsFile(result);
            result.addOiTable(oiData);
            
        }
        // Browse all oivis of file 2
        
        for (OIData oiData : f2.getOiDatas()) {
            oiData.setOIFitsFile(result);
//            OIVis newData = new OIVis(result);
//            newData.setAmpOrder(visData.getAmpOrder());
//            newData.setAmpTyp(visData.getAmpTyp());
            oiData.setArrName(mapArrayNames.get(oiData.getArrName()));
//            newData.setCorrName(visData.getCorrName());
//            newData.setDateObs(visData.getDateObs());
//            newData.setExtName(visData.getExtName());
//            newData.setExtNb(visData.getExtNb());
//            newData.setExtVer(visData.getExtNb());
            oiData.setInsName(mapWLNames.get(oiData.getInsName()));
//            newData.setPhiOrder(visData.getPhiOrder());
//            newData.setPhiTyp(visData.getPhiType());

//            for (Map.Entry<String, Object> columnValue : visData.getColumnsValue().entrySet()) {
//                newData.setColumnValue(columnValue.getKey(), columnValue.getValue());
//            }
//            for (Map.Entry<String, Object> keywordValue : visData.getKeywordsValue().entrySet()) {
//                newData.setKeywordValue(keywordValue.getKey(), keywordValue.getValue());
//            }
//            for (ColumnMeta columnDesc : visData.getAllColumnDescCollection()) {
//                newData.setColumnUnit(columnDesc.getName(), columnDesc.getUnit());
//            }
            oiData.setOIFitsFile(result);
            result.addOiTable(oiData);
        
        }

    }
    
}
