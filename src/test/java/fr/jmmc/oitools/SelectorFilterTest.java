/** *****************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/

package fr.jmmc.oitools;

import static fr.jmmc.oitools.JUnitBaseTest.TEST_DIR_OIFITS;
import fr.jmmc.oitools.model.OIFitsCollection;
import fr.jmmc.oitools.model.OIFitsFile;
import fr.jmmc.oitools.model.OIFitsLoader;
import fr.jmmc.oitools.model.range.Range;
import fr.jmmc.oitools.processing.Selector;
import fr.jmmc.oitools.processing.SelectorResult;
import fr.nom.tam.fits.FitsException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;


public class SelectorFilterTest extends JUnitBaseTest {

    private static final String INPUT_FILE_NAME = "GRAVITY.2016-01-09T05-37-06_singlesci_calibrated.fits";
    private static final String TARGET_NAME = "tet01OriC";

    private static final Range range1 = new Range(2.4E-6, 2.6E-6);
    private static final Range range2 = new Range(2.0E-6, 2.3E-6);
    private static final Selector selector1 = new Selector();
    private static final Selector selector2 = new Selector();
    private static final OIFitsCollection oiFitsCollection = new OIFitsCollection();

    private static OIFitsFile oiFitsFile;
    private static SelectorResult selectorResult;

    @BeforeClass
    public static void init() throws IOException, MalformedURLException, FitsException {

        selector1.setTargetUID(TARGET_NAME);
        selector2.setTargetUID(TARGET_NAME);
        selector1.setWavelengthRanges(Arrays.asList(range1));
        selector2.setWavelengthRanges(Arrays.asList(range2));

        oiFitsFile = OIFitsLoader.loadOIFits(TEST_DIR_OIFITS + INPUT_FILE_NAME);
        oiFitsCollection.addOIFitsFile(oiFitsFile);
        oiFitsCollection.analyzeCollection();
    }

    @Test
    public void testFindOIData() {
        selectorResult = oiFitsCollection.findOIData(selector1);
        System.out.println(selectorResult);
        Assert.assertEquals(1, selectorResult.getDistinctInstrumentModes().size());
    }

    @Test
    public void testFindOIDataSecondSelector() {
        selectorResult = oiFitsCollection.findOIData(selector2, selectorResult);
        System.out.println(selectorResult);
        //Assert.assertEquals(2, selectorResult.getDistinctInstrumentModes().size());
    }
}
