/** *****************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools;

import static fr.jmmc.oitools.JUnitBaseTest.TEST_DIR_OIFITS;
import fr.jmmc.oitools.model.OIFitsChecker;
import fr.jmmc.oitools.model.OIFitsCollection;
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

    private static OIFitsCollection oiFitsCollection = null;

    @BeforeClass
    public static void init() throws IOException, MalformedURLException, FitsException {
        final OIFitsChecker checker = new OIFitsChecker();

        oiFitsCollection = OIFitsCollection.create(checker, Arrays.asList(new String[]{
            TEST_DIR_OIFITS + INPUT_FILE_NAME
        }));

        logger.info("validation results:\n" + checker.getCheckReport());

        logger.info("oiFitsCollection:\n" + oiFitsCollection);
    }

    @Test
    public void testFindOIData() {
        final Selector selector = new Selector();
        selector.setTargetUID(TARGET_NAME);
        selector.setWavelengthRanges(Arrays.asList(new Range(2.4E-6, 2.6E-6)));

        SelectorResult selectorResult = oiFitsCollection.findOIData(selector);
        System.out.println("testFindOIData: \n" + selectorResult);

        Assert.assertEquals(1, selectorResult.getDistinctInstrumentModes().size());

        /*
        maskOIWavelengths={
            OI_WAVELENGTH#3 [ INSNAME=SPECTRO_SC | NWAVE=235 ]=IndexMask{nbRows=235, nbCols=1, 
                bitSet={189, 190, 191, 192, 193, 194, 195, 196, 197, 198, 199, 200, 201, 202, 203, 204, 205, 206, 207, 208, 209, 210, 211, 212, 213, 214, 215, 216, 217, 218, 219, 220, 221, 222, 223, 224, 225, 226, 227, 228, 229, 230, 231, 232, 233, 234}}
            }
        }
         */
    }

    @Test
    public void testFindOIDataEmpty() {
        final Selector selector = new Selector();
        selector.setTargetUID(TARGET_NAME);
        selector.setWavelengthRanges(Arrays.asList(new Range(24E-6, 26E-6)));

        SelectorResult selectorResult = oiFitsCollection.findOIData(selector);
        System.out.println("testFindOIDataEmpty: \n" + selectorResult);

        Assert.assertNull(selectorResult);
    }

    @Test
    public void testFindOIDataFull() {
        final Selector selector = new Selector();
        selector.setTargetUID(TARGET_NAME);
        selector.setWavelengthRanges(Arrays.asList(new Range(1E-6, 10E-6)));

        SelectorResult selectorResult = oiFitsCollection.findOIData(selector);
        System.out.println("testFindOIDataFull: \n" + selectorResult);

        Assert.assertEquals(2, selectorResult.getDistinctInstrumentModes().size());

        /*
        maskOIWavelengths={
            OI_WAVELENGTH#3 [ INSNAME=SPECTRO_SC | NWAVE=235 ]=IndexMask{nbRows=0, nbCols=0, bitSet=null}, 
            OI_WAVELENGTH#4 [ INSNAME=SPECTRO_FT | NWAVE=5 ]=IndexMask{nbRows=0, nbCols=0, bitSet=null}}
        }
         */
    }
}
