/* 
 * Copyright (C) 2022 CNRS - JMMC project ( http://www.jmmc.fr )
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
package fr.jmmc.oitools;

import static fr.jmmc.oitools.JUnitBaseTest.getFitsFiles;
import static fr.jmmc.oitools.JUnitBaseTest.logger;
import fr.jmmc.oitools.model.OIFitsFile;
import fr.jmmc.oitools.model.OIFitsLoader;
import fr.jmmc.oitools.test.OITableUtils;
import fr.nom.tam.fits.FitsException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Level;
import static junit.framework.Assert.fail;
import org.junit.Test;

/**
 * Load an ImageOI OIFits file from the /oimg folder to copy the complete OIFITSFile structure 
 * and compare the copied structure.
 */
public class CopyTest extends JUnitBaseTest {

    /**
     * absolute path to test folder to load ImageOI OIFITS test resources
     */
    public final static String TEST_DIR_OIMG = TEST_DIR + "oimg/";

    @Test
    public void copyOIFits() throws IOException, MalformedURLException, FitsException {

        for (String pathFile : getFitsFiles(new File(TEST_DIR_OIMG))) {

            final OIFitsFile srcOIFitsFile = OIFitsLoader.loadOIFits(pathFile);

            if (srcOIFitsFile == null) {
                fail("Error loadOIFits: " + pathFile);
            } else {
                // Fix OITarget.EXTVER:
                srcOIFitsFile.getOiTarget().setExtVer(1);

                try {
                    // Copy structure:
                    final OIFitsFile destOIFitsFile = new OIFitsFile(srcOIFitsFile);

                    // verify and check :
                    if (!OITableUtils.compareOIFitsFile(srcOIFitsFile, destOIFitsFile, true)) {
                        fail("Error compareOIFitsFile: " + pathFile);
                    }

                } catch (Throwable th) {
                    logger.log(Level.SEVERE, "failure in copy: ", th);
                    throw th;
                }

                logger.info("\n-------------------\n");
            }
        }
    }
}
