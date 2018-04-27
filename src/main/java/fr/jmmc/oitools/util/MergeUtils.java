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
import fr.jmmc.oitools.model.OIFitsFile;
import fr.jmmc.oitools.model.OITarget;
import fr.jmmc.oitools.model.OIWavelength;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
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

//        try {

            // Process OI_TARGET part **********************************************************************
            OITarget target1 = f1.getOiTarget();
            OITarget target2 = f2.getOiTarget();
            checkOITarget(target1, target2);
            // No exception raised, store target 1 in result
            result.addOiTable(target1);
            // TODO: ? cloner l'instance
            logger.info("Target name: " + result.getOiTarget());

            // Process OI_WAVELENGTH part **********************************************************************
            Set<OIWavelength> allWl = new HashSet<OIWavelength>();
            OIWavelength[] oiwls1 = f1.getOiWavelengths();
            Set<String> wlNames = new HashSet<String>();
            // Collect all WL names of first file, store each WL in result list
            for (OIWavelength oiwl : oiwls1) {
                wlNames.add(oiwl.getInsName());
                allWl.add(oiwl); // TODO: clone
            }
            // Browse all names of second file, change name if already present in first file, build a map 
            // old_name > new name
            Map<String, String> mapWLNames = new HashMap<String, String>();
            OIWavelength[] oiwls2 = f2.getOiWavelengths();
            for (OIWavelength oiwl : oiwls2) {
                String oldName = oiwl.getInsName(), newName = oldName;
                if (wlNames.contains(newName)) {
                    int idx = 0;
                    do {
                        idx++;
                        newName = oldName + "_" + idx;
                    } while (wlNames.contains(newName));
                }
                mapWLNames.put(oldName, newName);
                wlNames.add(newName);
            }
            //  Store in result
            for (OIWavelength wl : allWl) {
                result.addOiTable(wl); // TODO: ? clone instance ?
            }

            // Process OI_ARRAY part **********************************************************************
            Set<OIArray> allArrays = new HashSet<OIArray>();
            OIArray[] oiArrays1 = f1.getOiArrays();
            Set<String> arraysName = new HashSet<String>();
            // Collect all WL names of first file, store each WL in result list
            for (OIArray oiArray : oiArrays1) {
                wlNames.add(oiArray.getArrName());
                allArrays.add(oiArray); // TODO: clone
            }
            // Browse all names of second file, change name if already present in first file, build a map 
            // old_name > new name
            Map<String, String> mapArrayNames = new HashMap<String, String>();
            OIArray[] oiArray2 = f2.getOiArrays();
            for (OIArray array : oiArray2) {
                String oldName = array.getArrName(), newName = oldName;
                if (arraysName.contains(newName)) {
                    int idx = 0;
                    do {
                        idx++;
                        newName = oldName + "_" + idx;
                    } while (arraysName.contains(newName));
                }
                mapArrayNames.put(oldName, newName);
                arraysName.add(newName);
            }
            //  Store in result
            for (OIArray arr : allArrays) {
                result.addOiTable(arr);  // TODO: clone instance
            }

            // Process Data VIS ********************************************************
//            List<OIVis> resultDataVis = new ArrayList<OIVis>();
//            OIVis[] oiVis1 = f1.getOiVis();
//            // Store all oivis of file 1
//            for (OIVis visData : oiVis1) {
//                resultDataVis.add(visData);
//            }
//            OIVis[] oiVis2 = f2.getOiVis();
//            // Browse all oivis of file 2
//            for (OIVis visData : oiVis2) {
//                OIVis newData = new OIVis(result);
//                newData.setAmpOrder(visData.getAmpOrder());
//                newData.setAmpTyp(visData.getAmpTyp());
//                newData.setArrName(mapArrayNames.get(visData.getArrName()));
//                newData.setCorrName(visData.getCorrName());
//                newData.setDateObs(visData.getDateObs());
//                newData.setExtName(visData.getExtName());
//                newData.setExtNb(visData.getExtNb());
//                newData.setExtVer(visData.getExtNb());
//                newData.setInsName(mapWLNames.get(visData.getInsName()));
//                newData.setPhiOrder(visData.getPhiOrder());
//                newData.setPhiTyp(visData.getPhiType());
//                
//                for (Map.Entry<String, Object> columnValue : visData.getColumnsValue().entrySet()) {
//                    newData.setColumnValue( columnValue.getKey(), columnValue.getValue());
//                }
//                for (Map.Entry<String, Object> keywordValue : visData.getKeywordsValue().entrySet()) {
//                    newData.setKeywordValue( keywordValue.getKey(), keywordValue.getValue());
//                }
//                for (ColumnMeta columnDesc : visData.getAllColumnDescCollection()) {
//                    newData.setColumnUnit( columnDesc.getName(), columnDesc.getUnit());
//                }
//                newData.setChanged();
//                resultDataVis.add(newData);
//            }
//            // Store
//            OIVis dataVis = new OIVis(result, );
//        } catch (FitsException ex) {
//            LOG.log(Level.SEVERE, null, ex);
//        } catch (IOException ex) {
//            LOG.log(Level.SEVERE, null, ex);
//        } catch (IllegalArgumentException ex) {
//            logger.log(Level.SEVERE, null, ex);
//            throw 
//        }
        return result;
    }

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
}
