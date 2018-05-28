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
/** *****************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ***************************************************************************** */
package fr.jmmc.oitools.util.test;

import fr.jmmc.oitools.JUnitBaseTest;
import static fr.jmmc.oitools.JUnitBaseTest.TEST_DIR_OIFITS;
import static fr.jmmc.oitools.JUnitBaseTest.TEST_DIR_TEST;
import static fr.jmmc.oitools.JUnitBaseTest.logger;
import fr.jmmc.oitools.OIFitsViewer;
import fr.jmmc.oitools.model.OIArray;
import fr.jmmc.oitools.model.OIFitsChecker;
import fr.jmmc.oitools.model.OIFitsFile;
import fr.jmmc.oitools.model.OIFitsLoader;
import fr.jmmc.oitools.model.OIFitsWriter;
import fr.jmmc.oitools.model.OIT3;
import fr.jmmc.oitools.model.OITarget;
import fr.jmmc.oitools.model.OIVis;
import fr.jmmc.oitools.model.OIVis2;
import fr.jmmc.oitools.model.OIWavelength;
import fr.jmmc.oitools.OIFitsProcessor;
import fr.jmmc.oitools.util.MergeUtil;
import fr.nom.tam.fits.FitsException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author jammetv
 */
public class TestMergeUtils3Files extends JUnitBaseTest {

    private static final boolean DEBUG = true;

    // Common data used by several tests
    private static OIFitsFile f1 = null;
    private static OIFitsFile f2 = null;
    private static OIFitsFile f3 = null;
    private static OIFitsFile merge = null;

    /**
     * Do the merge of 2 OIFitsFiles
     *
     * @throws IOException
     * @throws MalformedURLException
     * @throws FitsException
     */
    @BeforeClass
    public static void init() throws IOException, MalformedURLException, FitsException {
        f1 = OIFitsLoader.loadOIFits(
                TEST_DIR_OIFITS + "A-CLUSTER__2T3T__1-PHASEREF__SIMPLE_nsr0.05__20160812_193521_1.image-oi.oifits");
        f2 = OIFitsLoader.loadOIFits(
                TEST_DIR_OIFITS + "A-CLUSTER__2T3T__1-PHASEREF__SIMPLE_nsr0.05__20160812_193521_1.oifits");
        f3 = OIFitsLoader.loadOIFits(
                TEST_DIR_OIFITS + "A-CLUSTER__2T3T__1-PHASEREF__SIMPLE_nsr0.05__20160812_193521_1.oifits");
        merge = MergeUtil.mergeOIFitsFiles(f1, f2, f3);
        Assert.assertNotNull("Merge return a null value", merge);
    }

    /**
     * Test target part of the merge
     *
     * @throws IOException
     * @throws MalformedURLException
     * @throws FitsException
     */
    @Test
    public void testTargetWLArray() throws IOException, MalformedURLException, FitsException {

        OITarget oiTarget = merge.getOiTarget();
        Assert.assertEquals("Merge result has more or less than one target", 1, oiTarget.getNbRows());
        Assert.assertEquals("Merge result has not the expected target: " + oiTarget.getTarget(oiTarget.getTargetId()[0]),
                f1.getOiTarget().getTarget(f1.getOiTarget().getTargetId()[0]),
                oiTarget.getTarget(oiTarget.getTargetId()[0]));

        OIWavelength[] oiWl = merge.getOiWavelengths();
        Assert.assertEquals("Merge result has bad number of WL",
                f1.getOiWavelengths().length + f2.getOiWavelengths().length + f3.getOiWavelengths().length,
                oiWl.length);
//        List<String> wlNames = new ArrayList<String>(f1.getOIW oiWl.get);

        OIArray[] oiArray = merge.getOiArrays();
        Assert.assertEquals("Merge result has bad number of array",
                f1.getOiArrays().length + f2.getOiArrays().length + f3.getOiArrays().length,
                oiArray.length);

    }

    /**
     * Test OIVIS data part of the merge
     *
     * @throws IOException
     * @throws MalformedURLException
     * @throws FitsException
     */
    @Test
    public void testOIVIS() throws IOException, MalformedURLException, FitsException {

        OIVis[] oiVis1 = f1.getOiVis();
        OIVis[] oiVis2 = f2.getOiVis();
        OIVis[] oiVis3 = f3.getOiVis();
        OIVis[] oiVisMerge = merge.getOiVis();

        // Check returned OIVis part
        Assert.assertNotNull("Merge return null for OIVis part", oiVisMerge);
        Assert.assertEquals(oiVis1.length + oiVis2.length + oiVis3.length, oiVisMerge.length);

        // Check content OIVis
        List<OIVis> allResultOiVis = new ArrayList<OIVis>(Arrays.asList(oiVisMerge));

        // TODO : compare used by remove is on identiy, do comparaison method
        // see compareTable of OITableUtils
        for (OIVis oiVis : oiVis1) {
            if (!allResultOiVis.remove(oiVis)) {
                Assert.fail(String.format("Data %s of file 1 not found in result of merge.", oiVis));
            }
        }
        System.out.print("VJT: restant dans result: " + allResultOiVis);
        System.out.print("VJT: dans f2: " + Arrays.toString(oiVis2));
        for (OIVis oiVis : oiVis2) {
            if (!allResultOiVis.remove(oiVis)) {
                Assert.fail(String.format("Data %s of file 2 not found in result of merge.", oiVis));
            }
        }
        for (OIVis oiVis : oiVis3) {
            if (!allResultOiVis.remove(oiVis)) {
                Assert.fail(String.format("Data %s of file 3 not found in result of merge.", oiVis));
            }
        }
        Assert.assertEquals(
                "Result of merge cointains more OIVis data than it should: " + allResultOiVis,
                0, allResultOiVis.size());

    }

    /**
     * Test OIVIS2 data part of the merge
     *
     * @throws IOException
     * @throws MalformedURLException
     * @throws FitsException
     */
    @Test
    public void testOIVIS2() throws IOException, MalformedURLException, FitsException {

        OIVis2[] oiVis2Merge = merge.getOiVis2();

        Assert.assertNotNull("Merge return null for OIVis2 part", oiVis2Merge);
        Assert.assertEquals(f1.getOiVis2().length + f2.getOiVis2().length + f3.getOiVis2().length, oiVis2Merge.length);
    }

    /**
     * Test OIT3 data part of the merge
     *
     * @throws IOException
     * @throws MalformedURLException
     * @throws FitsException
     */
    @Test
    public void testOIT3() throws IOException, MalformedURLException, FitsException {

        OIT3[] mergeOiT3 = merge.getOiT3();
        Assert.assertNotNull("Merge return null for OIT3 part", mergeOiT3);
        Assert.assertEquals(f1.getOiT3().length + f2.getOiT3().length + f3.getOiT3().length, mergeOiT3.length);

    }
}