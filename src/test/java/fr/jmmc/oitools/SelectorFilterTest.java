/** *****************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools;

import static fr.jmmc.oitools.JUnitBaseTest.TEST_DIR_OIFITS;
import fr.jmmc.oitools.model.IndexMask;
import fr.jmmc.oitools.model.OIData;
import fr.jmmc.oitools.model.OIFitsChecker;
import fr.jmmc.oitools.model.OIFitsCollection;
import fr.jmmc.oitools.model.OIWavelength;
import fr.jmmc.oitools.model.range.Range;
import fr.jmmc.oitools.processing.Selector;
import fr.jmmc.oitools.processing.SelectorResult;
import fr.nom.tam.fits.FitsException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class SelectorFilterTest extends JUnitBaseTest {

    private static final String INPUT_FILE_NAME = "GRAVITY.2016-01-09T05-37-06_singlesci_calibrated.fits";
    private static final String TARGET_NAME = "tet01OriC";

    private static OIFitsCollection oiFitsCollection = null;

    @BeforeClass
    public static void init() throws IOException, MalformedURLException, FitsException {
        final OIFitsChecker checker = new OIFitsChecker();

        oiFitsCollection = OIFitsCollection.create(checker, Arrays.asList(new String[]{
            TEST_DIR_OIFITS + INPUT_FILE_NAME
        }));

        logger.log(Level.INFO, "validation results:\n{0}", checker.getCheckReport());

        logger.log(Level.INFO, "oiFitsCollection:\n{0}", oiFitsCollection);
    }

    @Test
    public void testFindOIData() {
        final Selector selector = new Selector();
        selector.setTargetUID(TARGET_NAME);
        selector.setWavelengthRanges(Arrays.asList(new Range(2.4E-6, 2.6E-6)));

        final SelectorResult selectorResult = oiFitsCollection.findOIData(selector);
        // logger.info("testFindOIData: \n" + selectorResult);

        Assert.assertEquals(1, selectorResult.getDistinctInstrumentModes().size());

        final List<OIData> oiDatas = selectorResult.getSortedOIDatas();
        logger.log(Level.INFO, "oiDatas: {0}", oiDatas.size());
        Assert.assertEquals(4, oiDatas.size());

        final OIWavelength oiWaveLength = oiDatas.get(0).getOiWavelength();

        final IndexMask mask = selectorResult.getMask(oiWaveLength);
        // logger.info("mask: " + mask);

        logger.log(Level.INFO, "mask bits: {0}", mask.cardinality());
        Assert.assertEquals(46, mask.cardinality());
    }

    @Test
    public void testFindOIDataEmpty() {
        final Selector selector = new Selector();
        selector.setTargetUID(TARGET_NAME);
        selector.setWavelengthRanges(Arrays.asList(new Range(24E-6, 26E-6)));

        SelectorResult selectorResult = oiFitsCollection.findOIData(selector);
        // logger.info("testFindOIDataEmpty: \n" + selectorResult);

        Assert.assertNull(selectorResult);
    }

    @Test
    public void testFindOIDataFull() {
        final Selector selector = new Selector();
        selector.setTargetUID(TARGET_NAME);
        selector.setWavelengthRanges(Arrays.asList(new Range(1E-6, 10E-6)));

        SelectorResult selectorResult = oiFitsCollection.findOIData(selector);
        // logger.info("testFindOIDataFull: \n" + selectorResult);

        Assert.assertEquals(2, selectorResult.getDistinctInstrumentModes().size());

        final List<OIData> oiDatas = selectorResult.getSortedOIDatas();
        logger.log(Level.INFO, "oiDatas: {0}", oiDatas.size());
        Assert.assertEquals(8, oiDatas.size());

        for (OIData oiData : oiDatas) {
            final IndexMask mask = selectorResult.getMask(oiData.getOiWavelength());
            // logger.info("mask: \n" + mask);

            Assert.assertTrue(mask.isFull());
        }
    }
}
