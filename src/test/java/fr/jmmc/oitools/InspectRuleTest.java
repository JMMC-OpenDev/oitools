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

import static fr.jmmc.oitools.JUnitBaseTest.TEST_DIR_OIFITS;
import static fr.jmmc.oitools.JUnitBaseTest.getFitsFiles;
import fr.jmmc.oitools.model.OIFitsChecker;
import fr.jmmc.oitools.model.OIFitsLoader;
import fr.jmmc.oitools.model.Rule;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import nom.tam.fits.FitsException;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test the code robustness: enable the OIFitsChecker.isInspectRule flag
 * @author kempsc
 */
public class InspectRuleTest extends AbstractFileBaseTest {

    /**
     * Test loading lots of files when isInspectRule is active
     * @throws IOException
     * @throws FitsException
     */
    @Test
    public void dumpFile() throws IOException, FitsException {
        // Enable inspectRules FIRST:
        OIFitsChecker.setInspectRules(true);

        if (Rule.ARRNAME_UNIQ.getApplyTo() == null) {
            throw new IllegalStateException("Rule.applyToSet is null !");
        }

        try {
            final OIFitsChecker checker = new OIFitsChecker();

            final List<String> failureMsgs = new ArrayList<String>();

            for (String f : getFitsFiles(new File(TEST_DIR_OIFITS))) {
                try {
                    OIFitsLoader.loadOIFits(checker, f);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "exception:", e);
                    failureMsgs.add("File: " + (f.replaceAll(TEST_DIR_OIFITS, ""))
                            + "throws an exception : " + e.getMessage() + "\n");
                }

            }
            if (!failureMsgs.isEmpty()) {
                Assert.fail("IsInspectRuleTest failed " + failureMsgs.size() + " times:\n" + failureMsgs);
            }

        } finally {
            // Always disable the inspectRules flag before any other test:
            OIFitsChecker.setInspectRules(false);
        }
    }
}
