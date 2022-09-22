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

import fr.jmmc.jmcs.util.NumberUtils;
import fr.jmmc.oitools.JUnitBaseTest;
import static fr.jmmc.oitools.JUnitBaseTest.TEST_DIR_OIFITS;
import fr.jmmc.oitools.OIFitsCollectionViewer;
import fr.jmmc.oitools.OIFitsConstants;
import fr.jmmc.oitools.model.OIArray;
import fr.jmmc.oitools.model.OIData;
import fr.jmmc.oitools.model.OIDataListHelper;
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

    private static final boolean TRACE = false;
    private static final boolean CHECKER = false;

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
    // GRAVITY.2016-...
    private static final List<Range> VIS2_FILTER_VALUE = Arrays.asList(new Range(0.0, 1.0));

    // Common data used by several tests
    private static OIFitsFile input = null;
    // targetUID:
    private static OIFitsFile mergeFilterTargetBlockAll = null;
    private static OIFitsFile mergeFilterTargetPassSome = null;
    // insModeUID:
    private static OIFitsFile mergeFilterInsModePassAll = null;
    private static OIFitsFile mergeFilterInsModeBlockAll = null;
    private static OIFitsFile mergeFilterInsModePassSome = null;
    // NightId:
    private static OIFitsFile mergeFilterNightBlockAll = null;
    private static OIFitsFile mergeFilterNightPassSome = null;
    // StaIndex:
    private static OIFitsFile mergeFilterBaselinesBlockAll = null;
    private static OIFitsFile mergeFilterBaselinesPassSome = null;
    private static OIFitsFile mergeFilterBaselinesSkipSome = null;
    // Mjd:
    private static OIFitsFile mergeFilterMjdsBlockAll = null;
    private static OIFitsFile mergeFilterMjdsPassSome = null;
    private static OIFitsFile mergeFilterMjdsSkipSome = null;
    // Wavelength:
    private static OIFitsFile mergeFilterWlsBlockAll = null;
    private static OIFitsFile mergeFilterWlsPassAll = null;
    private static OIFitsFile mergeFilterWlsPassSome = null;
    private static OIFitsFile mergeFilterWlsSkipSome = null;
    // Vis2
    private static OIFitsFile mergeFilterVis2BlockAll = null;
    private static OIFitsFile mergeFilterVis2PassSome = null;
    private static OIFitsFile mergeFilterVis2SkipSome = null;

    /**
     * Filter OIFitsFiles
     *
     * @throws IOException
     * @throws MalformedURLException
     * @throws FitsException
     */
    @BeforeClass
    public static void init() throws IOException, MalformedURLException, FitsException {
        try {
            input = OIFitsLoader.loadOIFits(TEST_DIR_OIFITS + INPUT_FILE_NAME);
            final OIFitsFile inputTargets = OIFitsLoader.loadOIFits(TEST_DIR_OIFITS + INPUT_FILE_NAME_TARGETS);
            final OIFitsFile inputNights = OIFitsLoader.loadOIFits(TEST_DIR_OIFITS + INPUT_FILE_NAME_NIGHTS);

            // debugging file:
            OIFitsFile dumpFile = null;

            final Selector selector = new Selector();

            // TARGET:
            selector.reset();
            {
                System.out.println("--- filter TargetUID ---");

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

                doCheck(mergeFilterTargetPassSome);
                mergeFilterTargetPassSome.analyze();
            }

            // INSMODE:
            selector.reset();
            {
                System.out.println("--- filter InsModeUID ---");

                final OIFitsCollection oiColInput = new OIFitsCollection();
                oiColInput.addOIFitsFile(input);
                oiColInput.analyzeCollection();

                // Filter don't block any
                mergeFilterInsModePassAll = Merger.process(oiColInput, null);
                Assert.assertNotNull("Merge return a null value", mergeFilterInsModePassAll);

                // Filter block data of the files
                selector.setInsModeUID("GRAV");
                mergeFilterInsModeBlockAll = Merger.process(oiColInput, selector);
                Assert.assertNull("Merge return not a null value", mergeFilterInsModeBlockAll);

                // Filter let pass data of the files
                selector.setInsModeUID(INSNAME_FILTER_VALUE);
                mergeFilterInsModePassSome = Merger.process(oiColInput, selector);
                Assert.assertNotNull("Merge return a null value", mergeFilterInsModePassSome);

                doCheck(mergeFilterInsModePassSome);
                mergeFilterInsModePassSome.analyze();
            }

            // NIGHT:
            selector.reset();
            {
                System.out.println("--- filter NightID ---");

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

                doCheck(mergeFilterNightPassSome);
                mergeFilterNightPassSome.analyze();
            }

            // BASELINES:
            selector.reset();
            {
                System.out.println("--- filter STA_INDEX ---");

                final OIFitsCollection oiColBaselines = new OIFitsCollection();
                oiColBaselines.addOIFitsFile(inputTargets);
                oiColBaselines.analyzeCollection();

                logger.log(Level.INFO, "oiColBaselines: UsedStaNamesMap {0}", oiColBaselines.getUsedStaNamesMap());
                logger.log(Level.INFO, "oiColBaselines: {0} data points", oiColBaselines.getNbDataPoints());

                // Filter block data of the files
                selector.addFilter(Selector.FILTER_STAINDEX, Arrays.asList("X1-Y1"));
                mergeFilterBaselinesBlockAll = Merger.process(oiColBaselines, selector);
                Assert.assertNull("Merge return not a null value", mergeFilterBaselinesBlockAll);

                // Filter let pass data of the files
                selector.addFilter(Selector.FILTER_STAINDEX, BASELINES_FILTER_VALUE);
                mergeFilterBaselinesPassSome = Merger.process(oiColBaselines, selector);
                Assert.assertNotNull("Merge return a null value", mergeFilterBaselinesPassSome);

                logger.log(Level.INFO, "BASELINES_FILTER_VALUE: {0}", BASELINES_FILTER_VALUE);

                doCheck(mergeFilterBaselinesPassSome);
                mergeFilterBaselinesPassSome.analyze();

                logger.log(Level.INFO, "MERGE: UsedStaNamesMap: {0}", mergeFilterBaselinesPassSome.getUsedStaNamesMap());

                Assert.assertEquals("4 baselines should be in merge result of blocking filter",
                        4, mergeFilterBaselinesPassSome.getUsedStaNamesMap().size());

                if (TRACE) {
                    // reversed baselines:
                    for (String staName : mergeFilterBaselinesPassSome.getUsedStaNamesMap().keySet()) {
                        if (!BASELINES_FILTER_VALUE.contains(staName)) {
                            logger.log(Level.INFO, "mergeFilterBaselinesPassSome: Filtered baselines {0} should not contain :{1} ({2})!",
                                    new Object[]{mergeFilterBaselinesPassSome.getUsedStaNamesMap().keySet(), staName, BASELINES_FILTER_VALUE});
                        }
                    }
                }

                logger.log(Level.INFO, "mergeFilterBaselinesPassSome: {0} data points", mergeFilterBaselinesPassSome.getNbDataPoints());

                // Filter let skip data of the files
                if (!selector.removeFilter(Selector.FILTER_STAINDEX)) {
                    Assert.fail("can not remove filter: " + Selector.FILTER_STAINDEX);
                }
                selector.addExcludingFilter(Selector.FILTER_STAINDEX, BASELINES_FILTER_VALUE);
                mergeFilterBaselinesSkipSome = Merger.process(oiColBaselines, selector);
                Assert.assertNotNull("Merge return a null value", mergeFilterBaselinesSkipSome);

                doCheck(mergeFilterBaselinesSkipSome);
                mergeFilterBaselinesSkipSome.analyze();

                logger.log(Level.INFO, "MERGE: UsedStaNamesMap: {0}", mergeFilterBaselinesSkipSome.getUsedStaNamesMap());

                for (String staName : mergeFilterBaselinesSkipSome.getUsedStaNamesMap().keySet()) {
                    if (BASELINES_FILTER_VALUE.contains(staName)) {
                        logger.log(Level.INFO, "mergeFilterBaselinesSkipSome: Filtered baselines {0} should not contain :{1} ({2})!",
                                new Object[]{mergeFilterBaselinesSkipSome.getUsedStaNamesMap().keySet(), staName, BASELINES_FILTER_VALUE});
                    }
                }

                logger.log(Level.INFO, "mergeFilterBaselinesSkipSome: {0} data points", mergeFilterBaselinesSkipSome.getNbDataPoints());

                if (mergeFilterBaselinesPassSome.getNbDataPoints() + mergeFilterBaselinesSkipSome.getNbDataPoints() != oiColBaselines.getNbDataPoints()) {
                    Assert.fail("tot(bl) != PassSome + SkipSome");
                }
            }

            // MJDs:
            selector.reset();
            {
                System.out.println("--- filter MJD ---");

                final OIFitsCollection oiColMjds = new OIFitsCollection();
                oiColMjds.addOIFitsFile(input);
                oiColMjds.analyzeCollection();

                logger.log(Level.INFO, "oiColMjds: {0} data points", oiColMjds.getNbDataPoints());

                if (TRACE) {
                    for (OIData oidata : oiColMjds.getAllOiDatas()) {
                        logger.log(Level.INFO, "oiColMjds: {0}", oidata.getMjdRange());
                    }
                }

                // Filter block data of the files
                selector.addFilter(Selector.FILTER_MJD, Arrays.asList(new Range(50000, 55000)));
                mergeFilterMjdsBlockAll = Merger.process(oiColMjds, selector);
                Assert.assertNull("Merge return not a null value", mergeFilterMjdsBlockAll);

                // Filter let pass data of the files
                selector.addFilter(Selector.FILTER_MJD, MJDS_FILTER_VALUE);
                mergeFilterMjdsPassSome = Merger.process(oiColMjds, selector);
                Assert.assertNotNull("Merge return a null value", mergeFilterMjdsPassSome);

                logger.log(Level.INFO, "MJDS_FILTER_VALUE: {0}", MJDS_FILTER_VALUE);

                doCheck(mergeFilterMjdsPassSome);
                mergeFilterMjdsPassSome.analyze();

                logger.log(Level.INFO, "mergeFilterMjdsPassSome: {0} data points", mergeFilterMjdsPassSome.getNbDataPoints());

                // Filter let skip data of the files
                if (!selector.removeFilter(Selector.FILTER_MJD)) {
                    Assert.fail("can not remove filter: " + Selector.FILTER_MJD);
                }
                selector.addExcludingFilter(Selector.FILTER_MJD, MJDS_FILTER_VALUE);
                mergeFilterMjdsSkipSome = Merger.process(oiColMjds, selector);
                Assert.assertNotNull("Merge return a null value", mergeFilterMjdsSkipSome);

                doCheck(mergeFilterMjdsSkipSome);
                mergeFilterMjdsSkipSome.analyze();

                logger.log(Level.INFO, "mergeFilterMjdsSkipSome: {0} data points", mergeFilterMjdsSkipSome.getNbDataPoints());

                if (mergeFilterMjdsPassSome.getNbDataPoints() + mergeFilterMjdsSkipSome.getNbDataPoints() != oiColMjds.getNbDataPoints()) {
                    Assert.fail("tot(mjd) != PassSome + SkipSome");
                }
            }

            // Wavelengths:
            selector.reset();
            {
                System.out.println("--- filter EFF_WAVE ---");

                final OIFitsCollection oiColWls = new OIFitsCollection();
                oiColWls.addOIFitsFile(input);
                oiColWls.analyzeCollection();

                logger.log(Level.INFO, "oiColWls: {0} data points", oiColWls.getNbDataPoints());

                // Filter block data of the files
                selector.addFilter(Selector.FILTER_EFFWAVE, Arrays.asList(new Range(0.5E-6, 1E-6)));
                mergeFilterWlsBlockAll = Merger.process(oiColWls, selector);
                Assert.assertNull("Merge return not a null value", mergeFilterWlsBlockAll);

                // Filter let pass all data of the files
                selector.addFilter(Selector.FILTER_EFFWAVE, Arrays.asList(new Range(0.1E-6, 1E-4)));
                mergeFilterWlsPassAll = Merger.process(oiColWls, selector);
                Assert.assertNotNull("Merge return a null value", mergeFilterWlsPassAll);

                logger.log(Level.INFO, "mergeFilterWlsPassAll: {0} data points", mergeFilterWlsPassAll.getNbDataPoints());

                if (mergeFilterWlsPassAll.getNbDataPoints() != oiColWls.getNbDataPoints()) {
                    Assert.fail("tot(wl) != PassAll");
                }

                // Filter let pass data of the files
                selector.addFilter(Selector.FILTER_EFFWAVE, WLS_FILTER_VALUE);
                mergeFilterWlsPassSome = Merger.process(oiColWls, selector);
                Assert.assertNotNull("Merge return a null value", mergeFilterWlsPassSome);

                logger.log(Level.INFO, "WLS_FILTER_VALUE: {0}", WLS_FILTER_VALUE);

                doCheck(mergeFilterWlsPassSome);
                mergeFilterWlsPassSome.analyze();

                logger.log(Level.INFO, "mergeFilterWlsPassSome: {0} data points", mergeFilterWlsPassSome.getNbDataPoints());

                // Filter let skip data of the files
                if (!selector.removeFilter(Selector.FILTER_EFFWAVE)) {
                    Assert.fail("can not remove filter: " + Selector.FILTER_EFFWAVE);
                }
                selector.addExcludingFilter(Selector.FILTER_EFFWAVE, WLS_FILTER_VALUE);
                mergeFilterWlsSkipSome = Merger.process(oiColWls, selector);
                Assert.assertNotNull("Merge return a null value", mergeFilterWlsSkipSome);

                doCheck(mergeFilterWlsSkipSome);
                mergeFilterWlsSkipSome.analyze();

                logger.log(Level.INFO, "mergeFilterWlsSkipSome: {0} data points", mergeFilterWlsSkipSome.getNbDataPoints());

                if (mergeFilterWlsSkipSome.getNbDataPoints() + mergeFilterWlsPassSome.getNbDataPoints() != oiColWls.getNbDataPoints()) {
                    Assert.fail("tot(wl) != PassSome + SkipSome");
                }
            }

            // Vis2:
            selector.reset();
            {
                System.out.println("--- filter VIS2 ---");

                final OIFitsCollection oiColVis2 = new OIFitsCollection();
                oiColVis2.addOIFitsFile(input);
                oiColVis2.analyzeCollection();

                final int nbVis2All = OIDataListHelper.getNbFiniteDataPoints(
                        oiColVis2.getOIDataList(OIFitsConstants.TABLE_OI_VIS2),
                        OIFitsConstants.COLUMN_VIS2DATA
                );

                logger.log(Level.INFO, "oiColVis2: {0} data points", oiColVis2.getNbDataPoints());
                logger.log(Level.INFO, "oiColVis2: {0} VIS data points", nbVis2All);

                logger.log(Level.INFO, "oiColVis2: VIS2 range: {0} ", oiColVis2.getColumnRange(OIFitsConstants.COLUMN_VIS2DATA));
                // [-5.082283549122882, 36.20106319273849]

                // Filter block data of the files
                selector.addFilter(OIFitsConstants.COLUMN_VIS2DATA, Arrays.asList(new Range(-100, -5.1), new Range(36.3, 100)));
                mergeFilterVis2BlockAll = Merger.process(oiColVis2, selector);
                Assert.assertNotNull("Merge return a null value", mergeFilterVis2BlockAll);

                final int nbVis2BlockAll = OIDataListHelper.getNbFiniteDataPoints(
                        mergeFilterVis2BlockAll.getOIDataList(OIFitsConstants.TABLE_OI_VIS2),
                        OIFitsConstants.COLUMN_VIS2DATA
                );

                logger.log(Level.INFO, "mergeFilterVis2BlockAll: {0} data points", mergeFilterVis2BlockAll.getNbDataPoints());
                logger.log(Level.INFO, "mergeFilterVis2BlockAll: {0} VIS2 data points", nbVis2BlockAll);

                Assert.assertTrue("VIS2 table present", mergeFilterVis2BlockAll.getNbOiVis2() == 0);

                // Filter let pass data of the files
                selector.addFilter(OIFitsConstants.COLUMN_VIS2DATA, VIS2_FILTER_VALUE);
                mergeFilterVis2PassSome = Merger.process(oiColVis2, selector);
                Assert.assertNotNull("Merge return a null value", mergeFilterVis2PassSome);

                logger.log(Level.INFO, "VIS2_FILTER_VALUE: {0}", VIS2_FILTER_VALUE);

                doCheck(mergeFilterVis2PassSome);
                mergeFilterVis2PassSome.analyze();

                final int nbVis2PassSome = OIDataListHelper.getNbFiniteDataPoints(
                        mergeFilterVis2PassSome.getOIDataList(OIFitsConstants.TABLE_OI_VIS2),
                        OIFitsConstants.COLUMN_VIS2DATA
                );

                logger.log(Level.INFO, "mergeFilterVis2PassSome: {0} data points", mergeFilterVis2PassSome.getNbDataPoints());
                logger.log(Level.INFO, "mergeFilterVis2PassSome: {0} VIS2 data points", nbVis2PassSome);

                // Filter let skip data of the files
                if (!selector.removeFilter(OIFitsConstants.COLUMN_VIS2DATA)) {
                    Assert.fail("can not remove filter: " + OIFitsConstants.COLUMN_VIS2DATA);
                }
                selector.addExcludingFilter(OIFitsConstants.COLUMN_VIS2DATA, VIS2_FILTER_VALUE);
                mergeFilterVis2SkipSome = Merger.process(oiColVis2, selector);
                Assert.assertNotNull("Merge return a null value", mergeFilterVis2SkipSome);

                doCheck(mergeFilterVis2SkipSome);
                mergeFilterVis2SkipSome.analyze();

                final int nbVis2SkipSome = OIDataListHelper.getNbFiniteDataPoints(
                        mergeFilterVis2SkipSome.getOIDataList(OIFitsConstants.TABLE_OI_VIS2),
                        OIFitsConstants.COLUMN_VIS2DATA
                );

                logger.log(Level.INFO, "mergeFilterVis2SkipSome: {0} data points", mergeFilterVis2SkipSome.getNbDataPoints());
                logger.log(Level.INFO, "mergeFilterVis2SkipSome: {0} VIS2 data points", nbVis2SkipSome);

                if (nbVis2SkipSome + nbVis2PassSome != nbVis2All) {
                    Assert.fail("tot(v2) != PassSome + SkipSome");
                }
            }
            // dumpFile = mergeFilterVis2SkipSome;

            if (dumpFile != null) {
                System.out.println("--- dumping file ---");

                dumpFile.setAbsoluteFilePath("fake_path");
                final OIFitsCollection oiColDump = OIFitsCollection.create(dumpFile);
                OIFitsCollectionViewer.process(oiColDump);
                OIFitsCollectionViewer.processBaselines(oiColDump);
            }
        } catch (IOException | FitsException | RuntimeException e) {
            logger.log(Level.SEVERE, "Init failed: ", e);
            throw e;
        }
    }

    /**
     * Test if data are filtered
     *
     * @throws IOException
     * @throws MalformedURLException
     * @throws FitsException
     */
    @Test
    public void testTables() throws IOException, MalformedURLException, FitsException {

        List<OITable> tableList = mergeFilterInsModePassAll.getOITableList();
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
    @Test
    public void testData() throws IOException, MalformedURLException, FitsException {

        List<OIData> oiDataList = mergeFilterInsModePassAll.getOiDataList();
        Assert.assertEquals("Bad number of Data in merge result of blocking filter",
                input.getOiDatas().length, (oiDataList != null) ? oiDataList.size() : 0);

        oiDataList = mergeFilterInsModePassSome.getOiDataList();
        Assert.assertEquals("4 Data tables should be in merge result of blocking filter",
                4, (oiDataList != null) ? oiDataList.size() : 0);

        oiDataList = mergeFilterTargetPassSome.getOiDataList();
        Assert.assertEquals("2 Data tables should be in merge result of blocking filter",
                2, (oiDataList != null) ? oiDataList.size() : 0);

        if (oiDataList != null) {
            for (OIData oidata : oiDataList) {
                Assert.assertEquals("1 targetId value in data tables should be in merge result of blocking filter",
                        1, oidata.getDistinctTargetId().size());
            }
        }

        oiDataList = mergeFilterNightPassSome.getOiDataList();
        Assert.assertEquals("12 Data tables should be in merge result of blocking filter",
                12, (oiDataList != null) ? oiDataList.size() : 0);

        if (oiDataList != null) {
            for (OIData oidata : oiDataList) {
                Assert.assertEquals("1 nightId value in data tables should be in merge result of blocking filter",
                        1, oidata.getDistinctNightId().size());
            }
        }

        oiDataList = mergeFilterBaselinesPassSome.getOiDataList();
        Assert.assertEquals("2 Data tables should be in merge result of blocking filter",
                2, (oiDataList != null) ? oiDataList.size() : 0);

        oiDataList = mergeFilterMjdsPassSome.getOiDataList();
        Assert.assertEquals("6 Data tables should be in merge result of blocking filter",
                6, (oiDataList != null) ? oiDataList.size() : 0);

        if (oiDataList != null) {
            for (OIData oidata : oiDataList) {
                Assert.assertTrue("mjd range in data tables should be within range in merge result of blocking filter",
                        Range.matchFully(MJDS_FILTER_VALUE, oidata.getMjdRange()));
            }
        }

        oiDataList = mergeFilterMjdsSkipSome.getOiDataList();
        if (oiDataList != null) {
            for (OIData oidata : oiDataList) {
                Assert.assertTrue("mjd range in data tables should not be within range in merge result of skip filter",
                        !Range.matchRange(MJDS_FILTER_VALUE, oidata.getMjdRange()));
            }
        }

        oiDataList = mergeFilterWlsPassSome.getOiDataList();
        Assert.assertEquals("8 Data tables should be in merge result of blocking filter",
                8, (oiDataList != null) ? oiDataList.size() : 0);

        if (oiDataList != null) {
            for (OIData oidata : oiDataList) {
                Assert.assertTrue("WL range in data tables should be within range in merge result of blocking filter",
                        Range.matchFully(WLS_FILTER_VALUE, oidata.getEffWaveRange()));
            }
        }

        oiDataList = mergeFilterVis2PassSome.getOIDataList(OIFitsConstants.TABLE_OI_VIS2);
        if (oiDataList != null) {
            for (OIData oiData : oiDataList) {
                final double[][] vis2 = oiData.getColumnAsDoubles(OIFitsConstants.COLUMN_VIS2DATA);

                for (int i = 0, nRows = oiData.getNbRows(); i < nRows; i++) {
                    for (double v2 : vis2[i]) {
                        if (NumberUtils.isFinite(v2)) {
                            Assert.assertTrue("V2 (=" + v2 + ") in data tables should not be within range in merge result of pass filter",
                                    Range.contains(VIS2_FILTER_VALUE, v2));
                        } else if (TRACE) {
                            logger.log(Level.INFO, "ignored v2 = {0}", Double.toString(v2));
                        }
                    }
                }
            }
        }

        oiDataList = mergeFilterVis2SkipSome.getOIDataList(OIFitsConstants.TABLE_OI_VIS2);
        if (oiDataList != null) {
            for (OIData oiData : oiDataList) {
                final double[][] vis2 = oiData.getColumnAsDoubles(OIFitsConstants.COLUMN_VIS2DATA);

                for (int i = 0, nRows = oiData.getNbRows(); i < nRows; i++) {
                    for (double v2 : vis2[i]) {
                        if (TRACE && NumberUtils.isFinite(v2)) {
                            logger.log(Level.INFO, "test v2 = {0}", Double.toString(v2));
                        }
                        Assert.assertTrue("V2 (=" + v2 + ") in data tables should not be within range in merge result of skip filter",
                                !Range.contains(VIS2_FILTER_VALUE, v2));
                    }
                }
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
    @Test
    public void testOiWavelengths() throws IOException, MalformedURLException, FitsException {

        OIWavelength[] insList = mergeFilterInsModePassAll.getOiWavelengths();
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
        insList = mergeFilterWlsSkipSome.getOiWavelengths();
        if (insList != null) {
            for (OIWavelength oiWavelength : insList) {
                // using ranges do not work as the wavelength range can have a hole (filtered)...
                for (double wl : oiWavelength.getEffWaveAsDouble()) {
                    Assert.assertTrue("WL (=" + wl + ") in data tables should not be within range in merge result of skip filter",
                            !Range.contains(WLS_FILTER_VALUE, wl));
                }
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
    @Test
    public void testOiArrays() throws IOException, MalformedURLException, FitsException {

        OIArray[] arrayList = mergeFilterInsModePassAll.getOiArrays();
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
    @Test
    public void testOiTargets() throws IOException, MalformedURLException, FitsException {

        OITarget oiTarget = mergeFilterTargetPassSome.getOiTarget();
        Assert.assertEquals("1 target should be in merge result of blocking filter",
                1, oiTarget.getNbTargets());

        oiTarget = mergeFilterNightPassSome.getOiTarget();
        Assert.assertEquals("1 target should be in merge result of blocking filter",
                1, oiTarget.getNbTargets());
    }

    private static void doCheck(final OIFitsFile oiFitsFile) {
        if (CHECKER) {
            final OIFitsChecker checker = new OIFitsChecker();
            oiFitsFile.check(checker);
            logger.log(Level.INFO, "MERGE: validation results\n{0}", checker.getCheckReport());
        }
    }
}
