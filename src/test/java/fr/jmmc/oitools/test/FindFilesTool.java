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
/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.test;

import static fr.jmmc.oitools.JUnitBaseTest.getFitsFiles;
import fr.jmmc.oitools.image.FitsImageHDU;
import fr.jmmc.oitools.model.OIData;
import fr.jmmc.oitools.model.OIFitsFile;
import fr.jmmc.oitools.model.OIFitsLoader;
import fr.jmmc.oitools.model.OIFlux;
import fr.jmmc.oitools.model.OITable;
import fr.jmmc.oitools.model.OITarget;
import fr.jmmc.oitools.model.OIVis;
import fr.jmmc.oitools.model.OIWavelength;
import fr.jmmc.oitools.model.Target;
import fr.jmmc.oitools.model.TargetManager;
import java.io.File;
import java.util.Arrays;
import java.util.Locale;
import java.util.logging.Level;

/**
 * Allows to carry out numerous tests to choose files that respect these tests.
 * 
 * @author kempsc
 */
public class FindFilesTool implements TestEnv {

//    public final static String FIND_DIR = TEST_DIR;
    public final static String FIND_DIR = System.getProperty("user.home") + "/data-oidb-mirror/";

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);

        // disable logs below WARNING level:
        System.setProperty("java.util.logging.config.file", "./src/test/resources/logging.properties");

        logger.log(Level.WARNING, "Processing directory: {0}", FIND_DIR);

        int n = 0;
        for (String filePath : getFitsFiles(new File(FIND_DIR))) {
            processFile(filePath);
            n++;
        }
        logger.log(Level.WARNING, "Processed {0} files", n);
    }

    private static void processFile(final String absFilePath) {
        logger.log(Level.INFO, "Checking file : {0}", absFilePath);

        try {
            OIFitsFile OIFITS = OIFitsLoader.loadOIFits(absFilePath);
            OIFITS.analyze();

            testFile(OIFITS);

        } catch (Throwable th) {
            logger.log(Level.SEVERE, "IO failure occured while reading file : " + absFilePath, th);

        }
    }

    private static void testFile(OIFitsFile oifits) {
        if (findOIVis(oifits)) {
            logger.log(Level.WARNING, "findOIVis: {0}", display(oifits));
        }
        if (findOIFlux(oifits)) {
            logger.log(Level.WARNING, "findOIFlux: {0}", display(oifits));
        }
        if (multiTargets(oifits)) {
            logger.log(Level.WARNING, "multiTargets: {0}", display(oifits));

            if (multiTargetsInOIData(oifits)) {
                logger.log(Level.WARNING, "multiTargetsInOIData: {0}", display(oifits));
            }
            if (duplicatedTargetNames(oifits)) {
                logger.log(Level.WARNING, "duplicatedTargetNames: {0}", display(oifits));
            }
        }
        if (singleNWave(oifits)) {
            logger.log(Level.WARNING, "singleNWave: {0}", display(oifits));
        }
        if (multiTables(oifits)) {
            logger.log(Level.WARNING, "multiTables: {0}", display(oifits));
        }
        if (multiNightIds(oifits)) {
            logger.log(Level.WARNING, "multiNightIds: {0}", display(oifits));
        }
        if (isOIFits2(oifits)) {
            logger.log(Level.WARNING, "isOIFits2: {0}", display(oifits));
        }
        if (hasImage(oifits)) {
            logger.log(Level.WARNING, "hasImage: {0}", display(oifits));
        }
    }

    private static boolean findOIVis(OIFitsFile oifits) {
        if (oifits.hasOiVis()) {
            for (OIVis oivis : oifits.getOiVis()) {
                if (oivis.getNbRows() > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean findOIFlux(OIFitsFile oifits) {
        if (oifits.hasOiFlux()) {
            for (OIFlux oispect : oifits.getOiFlux()) {
                if (oispect.getNbRows() > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean multiTargets(OIFitsFile oifits) {
        if (oifits.hasOiTarget()) {
            if (oifits.getOiTarget().getNbRows() > 2) {
                return true;
            }
        }
        return false;
    }

    private static boolean duplicatedTargetNames(OIFitsFile oifits) {
        boolean match = false;

        if (oifits.hasOiTarget()) {
            final OITarget oiTarget = oifits.getOiTarget();
            final int len = oiTarget.getNbTargets();

            if (len > 2) {
                if (false) {
                    final String[] targetNames = oiTarget.getTarget();

                    for (int i = 0; i < len; i++) {
                        final String refName = targetNames[i];
                        if (refName != null) {

                            for (int j = i + 1; j < len; j++) {
                                if (refName.equals(targetNames[j])) {
                                    match = true;

                                    // rule [OI_TARGET_TARGET_UNIQ] check duplicated values in the TARGET column of the OI_TARGET table
                                    logger.log(Level.WARNING, "duplicated Target name: {0}", refName);
                                    break;
                                }
                            }
                        }
                    }
                }

                final TargetManager tm = TargetManager.newInstance();

                for (Target target : oiTarget.getTargetSet()) {
                    tm.register(target);
                }

                // test global mapping ?
                for (Target target : oiTarget.getTargetSet()) {
                    final Target globalTarget = tm.getGlobal(target);
                    if (!target.getTarget().equalsIgnoreCase(globalTarget.getTarget())) {
                        match = true;
                        logger.log(Level.WARNING, "Global Target name {0} different than Local Target name {1}",
                                new Object[]{globalTarget.getTarget(), target.getTarget()});
                    }
                }

                if (match) {
                    logger.log(Level.WARNING, "Global Targets: {0}", tm.getGlobals());
                }
            }
        }
        return match;
    }

    private static boolean multiTargetsInOIData(OIFitsFile oifits) {
        for (OIData oidata : oifits.getOiDataList()) {
            if (!oidata.hasSingleTarget()) {
                return true;
            }
        }
        return false;
    }

    private static boolean multiNightIds(OIFitsFile oifits) {
        for (OIData oidata : oifits.getOiDataList()) {
            if (!oidata.hasSingleNight()) {
                return true;
            }
        }
        return false;
    }

    private static boolean singleNWave(OIFitsFile oifits) {
        if (oifits.hasOiWavelengths()) {
            for (OIWavelength oiwave : oifits.getOiWavelengths()) {
                if (oiwave.getNWave() == 1) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean multiTables(OIFitsFile oifits) {
        return oifits.getNbOiArrays() > 1 && oifits.getNbOiWavelengths() > 1;
    }

    private static boolean isOIFits2(OIFitsFile oifits) {
        for (OITable oitable : oifits.getOiDataList()) {
            if (oitable.getOiRevn() == 2) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasImage(OIFitsFile oifits) {
        final FitsImageHDU primaryHDU = oifits.getPrimaryImageHDU();

        boolean ok = (primaryHDU != null && primaryHDU.hasImages());
        if (ok) {
            logger.log(Level.INFO, "image: {0}", primaryHDU);
        }
        return ok;
    }

    private static String display(OIFitsFile oifits) {
        return " FilePath : " + oifits.getAbsoluteFilePath() + ": [" + oifits.getNbOiTables()
                + "] : arrNames : " + Arrays.toString(oifits.getAcceptedArrNames())
                + " : insNames : " + Arrays.toString(oifits.getAcceptedInsNames())
                + " : Tables : " + oifits.getOITableList();
    }

}
