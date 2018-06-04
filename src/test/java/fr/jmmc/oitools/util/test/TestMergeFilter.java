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

    private static final String INPUT_FILE_NAME = "GRAVITY.2016-01-09T05-37-06_singlesci_calibrated.fits";
    private static final String INSNAME_FILTER_VALUE = "SPECTRO_SC";   // SPECTRO_FT
    
    // Common data used by several tests
    private static OIFitsFile input = null;
    private static OIFitsFile mergeFilterBlockAll = null;
    private static OIFitsFile mergeFilterPassSome = null;
    private static OIFitsFile mergeFilterPassAll = null;

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
                TEST_DIR_OIFITS + INPUT_FILE_NAME);

        Selector selector = new Selector();

        // Filter block data of the files
        selector.addPattern(Selector.INSTRUMENT_FILTER, "GRAV");
        mergeFilterBlockAll = Merger.process(selector, input);
        Assert.assertNotNull("Merge return a null value", mergeFilterBlockAll);

        // Filter let pass data of the files
        selector.addPattern(Selector.INSTRUMENT_FILTER, INSNAME_FILTER_VALUE);
        mergeFilterPassSome = merge(selector, input);
        Assert.assertNotNull("Merge return a null value", mergeFilterPassSome);
        
        // Filter don't block any
        mergeFilterPassAll = Merger.process(input);
        Assert.assertNotNull("Merge return a null value", mergeFilterPassAll);

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

        List<OIData> dataList = mergeFilterBlockAll.getOiDataList();
        Assert.assertEquals("No data should be in merge result of blocking filter",
                0, dataList != null ? dataList.size() : 0);

        dataList = mergeFilterPassSome.getOiDataList();
        Assert.assertEquals("4 Data tables should be in merge result of blocking filter",
                4, dataList != null ? dataList.size() : 0);

        dataList = mergeFilterPassAll.getOiDataList();
        Assert.assertEquals("Bad number of Data in merge result of blocking filter",
                input.getOiDatas().length, dataList != null ? dataList.size() : 0);

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

        OIWavelength[] insList = mergeFilterBlockAll.getOiWavelengths();
        Assert.assertEquals("No ins should be in merge result of blocking filter",
                0, insList != null ? insList.length : 0);

        insList = mergeFilterPassSome.getOiWavelengths();
        Assert.assertEquals("1 Ins should be in merge result of blocking filter",
                1, insList != null ? insList.length : 0);
        Assert.assertEquals("1 Ins should be in merge result of blocking filter",
                INSNAME_FILTER_VALUE, insList[0].getInsName());

        insList = mergeFilterPassAll.getOiWavelengths();
        Assert.assertEquals("Bad number of Ins in merge result of passing filter",
                input.getOiWavelengths().length, insList != null ? insList.length : 0);

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

        OIArray[] arrayList = mergeFilterBlockAll.getOiArrays();
        Assert.assertEquals("No array should be in merge result of blocking filter",
                0, arrayList != null ? arrayList.length : 0);

        arrayList = mergeFilterPassSome.getOiArrays();
        Assert.assertEquals("1 array should be in merge result of blocking filter",
                1, arrayList != null ? arrayList.length : 0);

        arrayList = mergeFilterPassAll.getOiArrays();
        Assert.assertEquals("Bad Insname in result",
                input.getOiArrays().length, arrayList != null ? arrayList.length : 0);

    }

    private static OIFitsFile merge(final Selector selector, final OIFitsFile... oiFitsToMerge) {

        OIFitsFile mergeResult = Merger.process(selector, oiFitsToMerge);

        final OIFitsChecker checker = new OIFitsChecker();
        mergeResult.check(checker);
        logger.log(Level.INFO, "MERGE: validation results\n{0}", checker.getCheckReport());

        return mergeResult;
    }
}
