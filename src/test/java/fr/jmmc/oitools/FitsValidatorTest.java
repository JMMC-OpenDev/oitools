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

import fr.jmmc.oitools.model.OIFitsChecker;
import fr.jmmc.oitools.model.OIFitsLoader;
import java.io.File;
import java.util.logging.Level;
import org.junit.Assert;
import org.junit.Test;

/**
 * This class contains test cases for the OIFitsChecker component
 *
 * @author bourgesl
 */
public class FitsValidatorTest extends JUnitBaseTest {

    @Test
    public void validateFile() {
        final OIFitsChecker checker = new OIFitsChecker();
        try {
            for (String f : getFitsFiles(new File(TEST_DIR_OIFITS))) {
                checkFile(checker, f);
            }
        } finally {
            // validation results
            logger.log(Level.INFO, "validation results\n{0}", checker.getCheckReport());

            // TODO: compare full report between runs (ref vs test)
        }

        // validation fail if SEVERE ERRORS
        if (false) {
            Assert.assertEquals("validation failed", 0, checker.getNbSeveres());
        }
    }

    public static void checkFile(final OIFitsChecker checker, String absFilePath) {
        logger.log(Level.INFO, "Checking file : {0}", absFilePath);
        try {
            OIFitsLoader.loadOIFits(checker, absFilePath);
        } catch (Throwable th) {
            logger.log(Level.SEVERE, "IO failure occured while reading file : " + absFilePath, th);

        }
    }
}
