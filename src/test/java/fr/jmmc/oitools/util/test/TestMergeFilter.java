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
import fr.jmmc.oitools.OIFitsCollectionViewer;
import fr.jmmc.oitools.model.OIArray;
import fr.jmmc.oitools.model.OIData;
import fr.jmmc.oitools.model.OIFitsChecker;
import fr.jmmc.oitools.model.OIFitsCollection;
import fr.jmmc.oitools.model.OIFitsFile;
import fr.jmmc.oitools.model.OIFitsLoader;
import fr.jmmc.oitools.model.OITable;
import fr.jmmc.oitools.model.OITarget;
import fr.jmmc.oitools.model.OIWavelength;
import fr.jmmc.oitools.model.range.Range;
import fr.jmmc.oitools.processing.Merger;
import fr.jmmc.oitools.processing.Selector;
import fr.nom.tam.fits.FitsException;
import java.io.IOException;
import java.net.MalformedURLException;
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
public class TestMergeFilter extends JUnitBaseTest {

    private static final String INPUT_FILE_NAME = "GRAVITY.2016-01-09T05-37-06_singlesci_calibrated.fits";
    private static final String INSNAME_FILTER_VALUE = "SPECTRO_SC";   // SPECTRO_FT

    private static final String INPUT_FILE_NAME_TARGETS = "2012-03-24_ALL_oiDataCalib.fits";
    private static final String TARGET_FILTER_VALUE = "HD100546"; // HD73495 ...

    private static final String INPUT_FILE_NAME_NIGHTS = "2009-11-eps_Aur-avg5.oifits";
    private static final Integer NIGHT_FILTER_VALUE = 55137; // 55138 ... 55140

    // "2009-11-eps_Aur-avg5.oifits";
    // A1-G1 A1-I1 G1-I1 K0-A1 K0-G1 K0-I1 A1-G1-I1 K0-A1-G1 K0-A1-I1 K0-G1-I1
    private static final List<String> BASELINES_FILTER_VALUE = Arrays.asList("A1-G1", "K0-A1", "K0-G1", "K0-A1-G1");

    // GRAVITY.2016-...
    private static final List<Range> MJDS_FILTER_VALUE = Arrays.asList(new Range(57396.231169, 57396.23200)); // truncated to get 1
    // GRAVITY.2016-...
    private static final List<Range> WLS_FILTER_VALUE = Arrays.asList(new Range(2.1E-6, 2.3E-6));

    // Common data used by several tests
    private static OIFitsFile input = null;
    private static OIFitsFile mergeFilterPassAll = null;

    private static OIFitsFile mergeFilterInsModeBlockAll = null;
    private static OIFitsFile mergeFilterInsModePassSome = null;

    private static OIFitsFile mergeFilterTargetBlockAll = null;
    private static OIFitsFile mergeFilterTargetPassSome = null;

    private static OIFitsFile mergeFilterNightBlockAll = null;
    private static OIFitsFile mergeFilterNightPassSome = null;

    private static OIFitsFile mergeFilterBaselinesBlockAll = null;
    private static OIFitsFile mergeFilterBaselinesPassSome = null;

    private static OIFitsFile mergeFilterMjdsBlockAll = null;
    private static OIFitsFile mergeFilterMjdsPassSome = null;

    private static OIFitsFile mergeFilterWlsBlockAll = null;
    private static OIFitsFile mergeFilterWlsPassSome = null;

    /**
     * Filter OIFitsFiles
     *
     * @throws IOException
     * @throws MalformedURLException
     * @throws FitsException
     */
    @BeforeClass
    public static void init() throws IOException, MalformedURLException, FitsException {
        input = OIFitsLoader.loadOIFits(TEST_DIR_OIFITS + INPUT_FILE_NAME);
        final OIFitsFile inputTargets = OIFitsLoader.loadOIFits(TEST_DIR_OIFITS + INPUT_FILE_NAME_TARGETS);
        final OIFitsFile inputNights = OIFitsLoader.loadOIFits(TEST_DIR_OIFITS + INPUT_FILE_NAME_NIGHTS);

        OIFitsFile testFile = null;

        final Selector selector = new Selector();
        // INSMODE:
        {
            final OIFitsCollection oiColInput = new OIFitsCollection();
            oiColInput.addOIFitsFile(input);
            oiColInput.analyzeCollection();

            // Filter don't block any
            mergeFilterPassAll = Merger.process(oiColInput, null);
            Assert.assertNotNull("Merge return a null value", mergeFilterPassAll);

            // Filter block data of the files
            selector.setInsModeUID("GRAV");
            mergeFilterInsModeBlockAll = Merger.process(oiColInput, selector);
            Assert.assertNull("Merge return not a null value", mergeFilterInsModeBlockAll);

            // Filter let pass data of the files
            selector.setInsModeUID(INSNAME_FILTER_VALUE);
            mergeFilterInsModePassSome = Merger.process(oiColInput, selector);
            Assert.assertNotNull("Merge return a null value", mergeFilterInsModePassSome);

            final OIFitsChecker checker = new OIFitsChecker();
            mergeFilterInsModePassSome.check(checker);
            logger.log(Level.INFO, "MERGE: validation results\n{0}", checker.getCheckReport());
        }

        selector.reset();
        // TARGET:
        {
            final OIFitsCollection oiColTargets = new OIFitsCollection();
            oiColTargets.addOIFitsFile(inputTargets);
            oiColTargets.analyzeCollection();

            // Filter block data of the files
            selector.setTargetUID("MISSING_TARGET");
            mergeFilterTargetBlockAll = Merger.process(oiColTargets, selector);
            Assert.assertNull("Merge return not a null value", mergeFilterTargetBlockAll);

            // Filter let pass data of the files
            selector.setTargetUID(TARGET_FILTER_VALUE);
            mergeFilterTargetPassSome = Merger.process(oiColTargets, selector);
            Assert.assertNotNull("Merge return a null value", mergeFilterTargetPassSome);

            final OIFitsChecker checker = new OIFitsChecker();
            mergeFilterTargetPassSome.check(checker);
            logger.log(Level.INFO, "MERGE: validation results\n{0}", checker.getCheckReport());

            mergeFilterTargetPassSome.analyze();
        }

        selector.reset();
        // NIGHT:
        {
            final OIFitsCollection oiColNights = new OIFitsCollection();
            oiColNights.addOIFitsFile(inputNights);
            oiColNights.analyzeCollection();

            // Filter block data of the files
            selector.setNightID(Integer.valueOf(0));
            mergeFilterNightBlockAll = Merger.process(oiColNights, selector);
            Assert.assertNull("Merge return not a null value", mergeFilterNightBlockAll);

            // Filter let pass data of the files
            selector.setNightID(NIGHT_FILTER_VALUE);
            mergeFilterNightPassSome = Merger.process(oiColNights, selector);
            Assert.assertNotNull("Merge return a null value", mergeFilterNightPassSome);

            final OIFitsChecker checker = new OIFitsChecker();
            mergeFilterNightPassSome.check(checker);
            logger.log(Level.INFO, "MERGE: validation results\n{0}", checker.getCheckReport());

            mergeFilterNightPassSome.analyze();
        }

        selector.reset();
        // BASELINES:
        {
            final OIFitsCollection oiColBaselines = new OIFitsCollection();
            oiColBaselines.addOIFitsFile(inputTargets);
            oiColBaselines.analyzeCollection();

            // Filter block data of the files
            selector.setBaselines(Arrays.asList("X1-Y1"));
            mergeFilterBaselinesBlockAll = Merger.process(oiColBaselines, selector);
            Assert.assertNull("Merge return not a null value", mergeFilterBaselinesBlockAll);

            // Filter let pass data of the files
            selector.setBaselines(BASELINES_FILTER_VALUE);
            mergeFilterBaselinesPassSome = Merger.process(oiColBaselines, selector);
            Assert.assertNotNull("Merge return a null value", mergeFilterBaselinesPassSome);

            final OIFitsChecker checker = new OIFitsChecker();
            mergeFilterBaselinesPassSome.check(checker);
            logger.log(Level.INFO, "MERGE: validation results\n{0}", checker.getCheckReport());

            mergeFilterBaselinesPassSome.analyze();

            Assert.assertEquals("4 baselines should be in merge result of blocking filter",
                    4, mergeFilterBaselinesPassSome.getUsedStaNamesMap().size());
        }

        selector.reset();
        // MJDs:
        {
            final OIFitsCollection oiColMjds = new OIFitsCollection();
            oiColMjds.addOIFitsFile(input);
            oiColMjds.analyzeCollection();

            // Filter block data of the files
            selector.setMJDRanges(Arrays.asList(new Range(50000, 55000)));
            mergeFilterMjdsBlockAll = Merger.process(oiColMjds, selector);
            Assert.assertNull("Merge return not a null value", mergeFilterMjdsBlockAll);

            // Filter let pass data of the files
            selector.setMJDRanges(MJDS_FILTER_VALUE);
            mergeFilterMjdsPassSome = Merger.process(oiColMjds, selector);
            Assert.assertNotNull("Merge return a null value", mergeFilterMjdsPassSome);

            final OIFitsChecker checker = new OIFitsChecker();
            mergeFilterMjdsPassSome.check(checker);
            logger.log(Level.INFO, "MERGE: validation results\n{0}", checker.getCheckReport());

            mergeFilterMjdsPassSome.analyze();
        }

        selector.reset();
        // Wavelengths:
        {
            final OIFitsCollection oiColMjds = new OIFitsCollection();
            oiColMjds.addOIFitsFile(input);
            oiColMjds.analyzeCollection();

            // Filter block data of the files
            selector.setWavelengthRanges(Arrays.asList(new Range(0.5E-6, 1E-6)));
            mergeFilterWlsBlockAll = Merger.process(oiColMjds, selector);
            Assert.assertNull("Merge return not a null value", mergeFilterWlsBlockAll);

            // Filter let pass data of the files
            selector.setWavelengthRanges(WLS_FILTER_VALUE);
            mergeFilterWlsPassSome = Merger.process(oiColMjds, selector);
            Assert.assertNotNull("Merge return a null value", mergeFilterWlsPassSome);

            final OIFitsChecker checker = new OIFitsChecker();
            mergeFilterWlsPassSome.check(checker);
            logger.log(Level.INFO, "MERGE: validation results\n{0}", checker.getCheckReport());

            mergeFilterWlsPassSome.analyze();
        }
        // testFile = mergeFilterWlsPassSome;

        if (testFile != null) {
            testFile.setAbsoluteFilePath("fake_path");
            final OIFitsCollection oiColDump = OIFitsCollection.create(testFile);
            OIFitsCollectionViewer.process(oiColDump);
            OIFitsCollectionViewer.processBaselines(oiColDump);
        }
    }

    /**
     * Test if data are filtered
     *
     * @throws IOException
     * @throws MalformedURLException
     * @throws FitsException
     */
    @Test // @Ignore
    public void testTables() throws IOException, MalformedURLException, FitsException {

        List<OITable> tableList = mergeFilterPassAll.getOITableList();
        Assert.assertEquals("Bad number of Data in merge result of blocking filter",
                input.getOITableList().size(), (tableList != null) ? tableList.size() : 0);
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

        List<OIData> dataList = mergeFilterPassAll.getOiDataList();
        Assert.assertEquals("Bad number of Data in merge result of blocking filter",
                input.getOiDatas().length, (dataList != null) ? dataList.size() : 0);

        dataList = mergeFilterInsModePassSome.getOiDataList();
        Assert.assertEquals("4 Data tables should be in merge result of blocking filter",
                4, (dataList != null) ? dataList.size() : 0);

        dataList = mergeFilterTargetPassSome.getOiDataList();
        Assert.assertEquals("2 Data tables should be in merge result of blocking filter",
                2, (dataList != null) ? dataList.size() : 0);

        if (dataList != null) {
            for (OIData data : dataList) {
                Assert.assertEquals("1 targetId value in data tables should be in merge result of blocking filter",
                        1, data.getDistinctTargetId().size());
            }
        }

        dataList = mergeFilterNightPassSome.getOiDataList();
        Assert.assertEquals("12 Data tables should be in merge result of blocking filter",
                12, (dataList != null) ? dataList.size() : 0);

        if (dataList != null) {
            for (OIData data : dataList) {
                Assert.assertEquals("1 nightId value in data tables should be in merge result of blocking filter",
                        1, data.getDistinctNightId().size());
            }
        }

        dataList = mergeFilterBaselinesPassSome.getOiDataList();
        Assert.assertEquals("2 Data tables should be in merge result of blocking filter",
                2, (dataList != null) ? dataList.size() : 0);

        dataList = mergeFilterMjdsPassSome.getOiDataList();
        Assert.assertEquals("6 Data tables should be in merge result of blocking filter",
                6, (dataList != null) ? dataList.size() : 0);

        if (dataList != null) {
            for (OIData data : dataList) {
                Assert.assertEquals("1 mjd range in data tables should be in merge result of blocking filter",
                        1, data.getDistinctMJDRanges().size());
                Assert.assertTrue("mjd range in data tables should be within range in merge result of blocking filter",
                        Range.matchRanges(MJDS_FILTER_VALUE, data.getDistinctMJDRanges().keySet()));
            }
        }

        dataList = mergeFilterWlsPassSome.getOiDataList();
        Assert.assertEquals("8 Data tables should be in merge result of blocking filter",
                8, (dataList != null) ? dataList.size() : 0);

        if (dataList != null) {
            for (OIData data : dataList) {
                Assert.assertTrue("WL range in data tables should be within range in merge result of blocking filter",
                        Range.matchFully(WLS_FILTER_VALUE, data.getEffWaveRange()));
            }
        }
    }

    /**
     * Test if ins are filtered
     *
     * @throws IOException
     * @throws MalformedURLException
     * @throws FitsException
     */
    @Test // @Ignore
    public void testOiWavelengths() throws IOException, MalformedURLException, FitsException {

        OIWavelength[] insList = mergeFilterPassAll.getOiWavelengths();
        Assert.assertEquals("Bad number of Ins in merge result of passing filter",
                input.getOiWavelengths().length, (insList != null) ? insList.length : 0);

        insList = mergeFilterInsModePassSome.getOiWavelengths();
        Assert.assertEquals("1 Ins should be in merge result of blocking filter",
                1, (insList != null) ? insList.length : 0);
        Assert.assertEquals("1 Ins should be in merge result of blocking filter",
                INSNAME_FILTER_VALUE, insList[0].getInsName());

        insList = mergeFilterWlsPassSome.getOiWavelengths();
        if (insList != null) {
            for (OIWavelength oiWavelength : insList) {
                Assert.assertTrue("WL range in data tables should be within range in merge result of blocking filter",
                        Range.matchFully(WLS_FILTER_VALUE, oiWavelength.getInstrumentMode().getWavelengthRange()));
            }
        }
    }

    /**
     * Test if arrays are filtered
     *
     * @throws IOException
     * @throws MalformedURLException
     * @throws FitsException
     */
    @Test // @Ignore
    public void testOiArrays() throws IOException, MalformedURLException, FitsException {

        OIArray[] arrayList = mergeFilterPassAll.getOiArrays();
        Assert.assertEquals("Bad number of Ins in merge result of passing filter",
                input.getOiArrays().length, (arrayList != null) ? arrayList.length : 0);

        arrayList = mergeFilterInsModePassSome.getOiArrays();
        Assert.assertEquals("1 array should be in merge result of blocking filter",
                1, (arrayList != null) ? arrayList.length : 0);
    }

    /**
     * Test if targets are filtered
     *
     * @throws IOException
     * @throws MalformedURLException
     * @throws FitsException
     */
    @Test // @Ignore
    public void testOiTargets() throws IOException, MalformedURLException, FitsException {

        OITarget oiTarget = mergeFilterTargetPassSome.getOiTarget();
        Assert.assertEquals("1 target should be in merge result of blocking filter",
                1, oiTarget.getNbTargets());

        oiTarget = mergeFilterNightPassSome.getOiTarget();
        Assert.assertEquals("1 target should be in merge result of blocking filter",
                1, oiTarget.getNbTargets());
    }
}
