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
import fr.jmmc.oitools.OIFitsViewer;
import fr.jmmc.oitools.model.OIArray;
import fr.jmmc.oitools.model.OIData;
import fr.jmmc.oitools.model.OIFitsChecker;
import fr.jmmc.oitools.model.OIFitsFile;
import fr.jmmc.oitools.model.OIFitsLoader;
import fr.jmmc.oitools.model.OIWavelength;
import fr.jmmc.oitools.processing.Merger;
import fr.jmmc.oitools.processing.Selector;
import fr.nom.tam.fits.FitsException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.logging.Level;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author jammetv
 */
public class TestMergeFilter extends JUnitBaseTest {

    // Common data used by several tests
    private static OIFitsFile input = null;
    private static OIFitsFile mergeFilterBlock = null;
    private static OIFitsFile mergeFilterPass = null;

    /**
     * Do the merge of 2 OIFitsFiles
     *
     * @throws IOException
     * @throws MalformedURLException
     * @throws FitsException
     */
    @BeforeClass
    public static void init() throws IOException, MalformedURLException, FitsException {
        input = OIFitsLoader.loadOIFits(
                TEST_DIR_OIFITS + "GRAVITY.2016-01-09T05-37-06_singlesci_calibrated.fits");

        Selector selector = new Selector();

        // Filter block data of the files
        selector.addPattern(Selector.INSTRUMENT_FILTER, "GRAV");
        mergeFilterBlock = Merger.process(selector, input);
        Assert.assertNotNull("Merge return a null value", mergeFilterBlock);

        // Filter let pass data of the files
        selector.addPattern(Selector.INSTRUMENT_FILTER, "SPECTRO_SC"); // SPECTRO_FT
        mergeFilterPass = merge(selector, input);
        Assert.assertNotNull("Merge return a null value", mergeFilterPass);
    }

    /**
     * Test if data are filtered
     *
     * @throws IOException
     * @throws MalformedURLException
     * @throws FitsException
     */
    @Test // @Ignore
    public void testData() throws IOException, MalformedURLException, FitsException {

        List<OIData> dataList = mergeFilterBlock.getOiDataList();
        Assert.assertEquals("No data should be in merge result of blocking filter",
                0, dataList != null ? dataList.size() : 0);

        dataList = mergeFilterPass.getOiDataList();
        Assert.assertEquals("4 Data tables should be in merge result of blocking filter",
                4, dataList != null ? dataList.size() : 0);

    }

    /**
     * Test if ins are filtered
     *
     * @throws IOException
     * @throws MalformedURLException
     * @throws FitsException
     */
    @Test // @Ignore
    public void testIns() throws IOException, MalformedURLException, FitsException {

        OIWavelength[] insList = mergeFilterBlock.getOiWavelengths();
        Assert.assertEquals("No ins should be in merge result of blocking filter",
                0, insList != null ? insList.length : 0);

        insList = mergeFilterPass.getOiWavelengths();
        Assert.assertEquals("1 Ins should be in merge result of blocking filter",
                1, insList != null ? insList.length : 0);

    }

    /**
     * Test if arrays are filtered
     *
     * @throws IOException
     * @throws MalformedURLException
     * @throws FitsException
     */
    @Test // @Ignore
    public void testArraysIns() throws IOException, MalformedURLException, FitsException {

        OIArray[] arrayList = mergeFilterBlock.getOiArrays();
        Assert.assertEquals("No array should be in merge result of blocking filter",
                0, arrayList != null ? arrayList.length : 0);

        arrayList = mergeFilterPass.getOiArrays();
        Assert.assertEquals("1 array should be in merge result of blocking filter",
                1, arrayList != null ? arrayList.length : 0);

    }

    private static OIFitsFile merge(final Selector selector, final OIFitsFile... oiFitsToMerge) {

        OIFitsFile mergeResult = Merger.process(selector, oiFitsToMerge);

        final OIFitsChecker checker = new OIFitsChecker();
        mergeResult.check(checker);
        logger.log(Level.INFO, "MERGE: validation results\n{0}", checker.getCheckReport());

        return mergeResult;
    }
}
