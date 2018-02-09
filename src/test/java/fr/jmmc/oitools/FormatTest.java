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

import static fr.jmmc.oitools.JUnitBaseTest.logger;
import fr.jmmc.oitools.meta.OIFitsStandard;
import fr.jmmc.oitools.model.OIFitsChecker;
import fr.jmmc.oitools.model.OIFitsFile;
import fr.jmmc.oitools.model.OIPrimaryHDU;
import fr.jmmc.oitools.model.OIVis;
import java.util.logging.Level;
import org.junit.Assert;
import org.junit.Test;

/**
 * Create DATE_OBS and MJD (MJD_OBS, MJD_END) in order to test the checker functions.
 * Different values considered "bad" and "good" are tested. 
 * The expected result is an error for the "bad" values and no errors for the "good" ones.
 * @author kempsc
 */
public class FormatTest {

    @Test
    public void testDateOk() {
        testDateObs("2017-02-15", false);
    }

    @Test
    public void testDateOk2() {
        testDateObs("2017-02-07T00:00:00", false);
    }

    @Test
    public void testDateOk3() {
        testDateObs("2017-02-07TEST", false);
    }

    @Test
    public void testDateBad1() {
        testDateObs("2017-02-31", true);
    }

    @Test
    public void testDateBad2() {
        testDateObs("02-07", true);
    }

    @Test
    public void testDateBad3() {
        testDateObs("1508-02-07", true);
    }

    @Test
    public void testDateBad4() {
        testDateObs("2398-02-07", true);
    }

    @Test
    public void testDateBad5() {
        testDateObs(" 2017-02-07", true);
    }

    @Test
    public void testDateEmpty() {
        testDateObs("", true);
    }

    @Test
    public void testDateNull() {
        testDateObs(null, true);
    }

    private static void testDateObs(final String dateObs, final boolean expected) {

        final OIFitsFile oiFitsFile = new OIFitsFile(OIFitsStandard.VERSION_1);

        final OIVis vis = new OIVis(oiFitsFile, "test", 1);
        vis.setDateObs(dateObs);
        oiFitsFile.addOiTable(vis);

        final OIFitsChecker checker = new OIFitsChecker();
        oiFitsFile.check(checker);

        // validation results
        final String report = checker.getCheckReport();

        logger.log(Level.INFO, "testDateObs(" + dateObs + ") : validation results\n{0}", report);

        if (report.contains("DATE-OBS") != expected) {
            Assert.fail("Validation error on DATE-OBS: " + dateObs);
        }
    }

    @Test
    public void testMJDOk() {
        testMJD(57930.0, false);
    }

    @Test
    public void testMJDBad1() {
        testMJD(0, true);
    }

    @Test
    public void testMJDBad2() {
        testMJD(200000, true);
    }

    @Test
    public void testMJDNaN() {
        //NaN means undefined, MJD is optional keyword, no validation error
        testMJD(Double.NaN, false);
    }

    private static void testMJD(final double mjd, final boolean expected) {

        final OIFitsFile oiFitsFile = new OIFitsFile(OIFitsStandard.VERSION_2);

        final OIPrimaryHDU imageHDU = new OIPrimaryHDU();
        imageHDU.setMJDObs(mjd);
        oiFitsFile.setPrimaryImageHdu(imageHDU);

        final OIFitsChecker checker = new OIFitsChecker();
        oiFitsFile.check(checker);

        // validation results
        final String report = checker.getCheckReport();
        logger.log(Level.INFO, "testMJD(" + mjd + ") :  validation results\n{0}", report);

        if (report.contains("MJD-OBS") != expected) {
            Assert.fail("Validation error on MJD-OBS: " + mjd);
        }
    }
}
