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
package fr.jmmc.oitools;

import fr.jmmc.oitools.model.OIFitsFile;
import fr.jmmc.oitools.model.OIFitsLoader;
import fr.jmmc.oitools.model.OIFitsWriter;
import fr.jmmc.oitools.test.OITableUtils;
import fr.jmmc.oitools.test.fits.TamFitsTest;
import fr.nom.tam.fits.FitsException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Level;
import static junit.framework.Assert.fail;
import org.junit.Test;

/**
 * Load OIFits files from the /oifits folder to write the complete OIFITSFile structure 
 * into a [filename]-copy file in the test/oifits folder.
 * After we load the file again and we compare it to the one written.
 * 
 * @author kempsc
 */
public class WriteOIFitsTest extends JUnitBaseTest {

    /**
     * absolute path to test folder to save test OIFITS files
     */
    private final static String TEST_DIR_TEST_OIFITS = TEST_DIR_TEST + "oifits/";

    /** */
    private final static boolean COMPARE_RAW = false;
    /** */
    private final static boolean STRICT = true;

    @Test
    public void writeLoadCompare() throws IOException, MalformedURLException, FitsException {

        //Mode lenient
        TamFitsTest.setStrict(STRICT);

        final File copyDir = new File(TEST_DIR_TEST_OIFITS);
        copyDir.mkdirs();

        for (String pathFile : getFitsFiles(new File(TEST_DIR_OIFITS))) {

            final OIFitsFile srcOIFitsFile = OIFitsLoader.loadOIFits(pathFile);

            if (srcOIFitsFile == null) {
                fail("Error loadOIFits: " + pathFile);
            } else {
                final String fileTo = new File(copyDir, new File(pathFile).getName().replaceFirst("\\.", "-copy.")).getAbsolutePath();

                logger.log(Level.INFO, "fileTo: {0}", fileTo);

                OIFitsWriter.writeOIFits(fileTo, srcOIFitsFile);

                // verify and check :
                final OIFitsFile destOIFitsFile = OIFitsLoader.loadOIFits(fileTo);
                if (destOIFitsFile == null) {
                    fail("Error loadOIFits: " + fileTo);
                } else if (!OITableUtils.compareOIFitsFile(srcOIFitsFile, destOIFitsFile)) {
                    fail("Error compareOIFitsFile: " + pathFile);
                }

                // compare fits files at fits level (header / data) :
                if (COMPARE_RAW && !TamFitsTest.compareFile(pathFile, fileTo)) {
                    // known failure: disabled raw comparison until TamFitsTest is fixed:
                    fail("Error TamFitsTest.compareFile: " + pathFile);
                }

                logger.info("\n-------------------\n");
            }
        }
    }
}
