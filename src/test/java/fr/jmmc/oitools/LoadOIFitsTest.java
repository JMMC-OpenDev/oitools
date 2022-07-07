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

import static fr.jmmc.oitools.AbstractFileBaseTest.reset;
import static fr.jmmc.oitools.DumpOIFitsTest.FILE_REPORT;
import static fr.jmmc.oitools.DumpOIFitsTest.FILE_VALIDATION;
import static fr.jmmc.oitools.DumpOIFitsTest.computeCustomExp;
import static fr.jmmc.oitools.DumpOIFitsTest.getHduId;
import static fr.jmmc.oitools.JUnitBaseTest.logger;
import fr.jmmc.oitools.fits.FitsTable;
import fr.jmmc.oitools.meta.ColumnMeta;
import fr.jmmc.oitools.model.OIData;
import fr.jmmc.oitools.model.OIFitsChecker;
import fr.jmmc.oitools.model.OIFitsFile;
import fr.jmmc.oitools.model.OIFitsLoader;
import fr.jmmc.oitools.model.OITable;
import fr.jmmc.oitools.util.FileUtils;
import fr.nom.tam.fits.FitsException;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Load OIFits files from the test/oifits folder and load properties file from test/ref (reference files)
 * to compare the complete OIFITSFile structure with the stored (key / value) pairs.
 * See DumpOIFitsTest
 * @author kempsc
 */
public class LoadOIFitsTest extends AbstractFileBaseTest {

    @BeforeClass
    public static void setUpClass() {
        initializeTest();
    }

    @AfterClass
    public static void tearDownClass() {
        shutdownTest();
    }

    @Test
    public void compareFiles() throws IOException, FitsException {

        final OIFitsChecker checker = new OIFitsChecker();
        try {
            for (String f : getFitsFiles(new File(TEST_DIR_OIFITS))) {

                // reset properties anyway
                reset();

                compareOIFits(checker, f);

                checkAssertCount();
            }
        } finally {
            // validation results
            logger.log(Level.INFO, "validation results\n{0}", checker.getCheckReport());
        }

        // reset properties anyway
        reset();

        // Load property file to map
        load(new File(TEST_DIR_REF, FILE_VALIDATION));

        assertEqualsInt(get("SEVERE.COUNT"), checker.getNbSeveres());
        assertEqualsInt(get("WARNING.COUNT"), checker.getNbWarnings());

        // read report as full text:
        final File refFile = new File(TEST_DIR_REF, FILE_REPORT);
        String report = FileUtils.readFile(refFile);

        if (!report.equals(checker.getCheckReport())) {
            final File tstFile = new File(getProjectFolderPath(), FILE_REPORT);
            FileUtils.writeFile(tstFile, checker.getCheckReport());
            Assert.fail("CheckReport comparison failed: see log files: diff " + refFile + " " + tstFile);
        }
    }

    private void compareOIFits(final OIFitsChecker checker, String f) throws IOException, FitsException {

        OIFitsFile OIFITS = OIFitsLoader.loadOIFits(checker, f);
        OIFITS.analyze();

        // Load property file to map
        load(new File(TEST_DIR_REF, OIFITS.getFileName() + ".properties"));

        computeCustomExp(OIFITS.getOiDatas());

        LoadFitsTest.compare(OIFITS.getPrimaryImageHDU());

        assertEquals(get("FILENAME"), OIFITS.getFileName());
        assertEqualsInt(get("OI_ARRAY.COUNT"), OIFITS.getNbOiArrays());
        assertEqualsInt(get("OI_TARGET.COUNT"), OIFITS.hasOiTarget() ? 1 : 0);
        assertEqualsInt(get("OI_WAVELENGTH.COUNT"), OIFITS.getNbOiWavelengths());
        assertEqualsInt(get("OI_VIS.COUNT"), OIFITS.getNbOiVis());
        assertEqualsInt(get("OI_VIS2.COUNT"), OIFITS.getNbOiVis2());
        assertEqualsInt(get("OI_T3.COUNT"), OIFITS.getNbOiT3());

        for (OITable oitable : OIFITS.getOiTables()) {
            compareTable(oitable);
        }
    }

    private static void compareTable(FitsTable table) {

        final String prefix = getHduId(table);

        assertEqualsInt(get(prefix + ".NBRow"), table.getNbRows());

        LoadFitsTest.compareKeywords(table, getHduId(table));
        compareColumns(table);
        compareHeaderCards(table);

        // Specific tests:
        if (table instanceof OIData) {
            compareOIData((OIData) table);
        }
    }

    private static void compareOIData(OIData oidata) {

        final String hduId = getHduId(oidata);
        /** internal buffer */
        StringBuilder buffer = new StringBuilder();

        assertEqualsInt(get(hduId + ".nWave"), oidata.getNWave());
        assertEqualsInt(get(hduId + ".nFlagged"), oidata.getNFlagged());

        Set<short[]> staIndexes = oidata.getDistinctStaIndex();
        for (short[] staIndex : staIndexes) {
            buffer.append(oidata.getStaNames(staIndex));
            buffer.append(", ");
        }
        assertEquals(get(hduId + ".staIndex"), buffer.toString());

        buffer.setLength(0);

        Set<short[]> staConfs = oidata.getDistinctStaConf();
        for (short[] staConf : staConfs) {
            buffer.append(oidata.getStaNames(staConf));
        }
        assertEquals(get(hduId + ".staConf"), buffer.toString());
    }

    private static void compareColumns(FitsTable table) {

        Object expected;
        final String hduId = getHduId(table);
        final String prefixC = hduId + ".C.";
        final String prefixDC = hduId + ".DC.";
        final String prefixMM = hduId + ".MM.";

        for (ColumnMeta columnMeta : table.getAllColumnDescCollection()) {
            // Test if column is standard or derived:
            if (table.getColumnDesc(columnMeta.getName()) == columnMeta) {
                expected = get(prefixC + columnMeta.getName());
                compareColumnValues(table, columnMeta, expected);

                expected = get(prefixMM + columnMeta.getName());
                compareColumnMinmax(table, columnMeta, expected);
            } else if (table.getColumnDerivedDesc(columnMeta.getName()) == columnMeta) {
                expected = get(prefixDC + columnMeta.getName());
                compareColumnValues(table, columnMeta, expected);
                // No MinMax for derived columns
            }
        }
    }

    private static void compareColumnValues(FitsTable table, ColumnMeta columnMeta, Object expected) {
        try {
            assertEquals(expected, getColumnValues(table, columnMeta));
        } catch (Error e) {
            logger.log(Level.SEVERE, "compareColumnValues failed: {0}", columnMeta);
        }
    }

    private static void compareColumnMinmax(FitsTable table, ColumnMeta columnMeta, Object expected) {
        assertEquals(expected, getColunmMinMax(table, columnMeta));
    }

    private static void compareHeaderCards(FitsTable table) {
        if (table.hasHeaderCards()) {
            LoadFitsTest.compareHeaderCards(getHduId(table), table.getHeaderCards());
        }
    }

}
