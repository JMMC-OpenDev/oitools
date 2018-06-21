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
import fr.jmmc.oitools.fits.FitsTable;
import fr.jmmc.oitools.meta.ColumnMeta;
import fr.jmmc.oitools.meta.WaveColumnMeta;
import fr.jmmc.oitools.model.OIData;
import fr.jmmc.oitools.model.OIFitsChecker;
import fr.jmmc.oitools.model.OIFitsFile;
import fr.jmmc.oitools.model.OIFitsLoader;
import fr.jmmc.oitools.model.OITable;
import fr.jmmc.oitools.util.FileUtils;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.logging.Level;
import nom.tam.fits.FitsException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Load OIFits files from the test/oifits folder to dump the complete OIFITSFile structure 
 * into a [filename].properties file (key / value pairs) in the test/test folder.
 * 
 * @see LoadOIFitsTest
 * @author kempsc
 */
public class DumpOIFitsTest extends AbstractFileBaseTest {

    // constants:
    public final static String COLUMN_SEC_2000 = "SEC_2000";
    public final static String COLUMN_SEC_2000_EXPR = "(MJD - 51544.5) * 86400.0";

    public final static String FILE_VALIDATION = "OIFITS-validation.properties";
    public final static String FILE_REPORT = "OIFITS-report.log";

    // members:
    private static OIFitsFile OIFITS = null;

    @BeforeClass
    public static void setupClass() {
        initializeTest();
    }

    @AfterClass
    public static void tearDownClass() {
        OIFITS = null;
        shutdownTest();
    }

    @Test
    public void dumpFile() throws IOException, FitsException {

        final OIFitsChecker checker = new OIFitsChecker();
        try {
            for (String f : getFitsFiles(new File(TEST_DIR_OIFITS))) {

                // reset properties anyway
                reset();

                dumpOIFits(checker, f);

                save(new File(TEST_DIR_TEST, OIFITS.getFileName() + ".properties"));
            }
        } finally {
            // validation results
            logger.log(Level.INFO, "validation results\n{0}", checker.getCheckReport());

            //logger.log(Level.INFO, "ERRORMESSAGE\n{0}", checker.getFailuresAsString());
        }

        // reset properties anyway
        reset();

        putInt("SEVERE.COUNT", checker.getNbSeveres());
        putInt("WARNING.COUNT", checker.getNbWarnings());
        save(new File(TEST_DIR_TEST, FILE_VALIDATION));

        // save report as full text:
        FileUtils.writeFile(new File(TEST_DIR_TEST, FILE_REPORT), checker.getCheckReport());

        // validation fail if SEVERE ERRORS
        if (false) {
            Assert.assertEquals("validation failed", 0, checker.getNbSeveres());
        }
    }

    private void dumpOIFits(final OIFitsChecker checker, String absFilePath) throws IOException, FitsException {
        logger.log(Level.INFO, "Checking file: {0}", absFilePath);

        OIFITS = OIFitsLoader.loadOIFits(checker, absFilePath);
        OIFITS.analyze();

        computeCustomExp(OIFITS.getOiDatas());

        DumpFitsTest.dump(OIFITS.getPrimaryImageHDU());

        put("FILENAME", OIFITS.getFileName());
        putInt("OI_ARRAY.COUNT", OIFITS.getNbOiArrays());
        putInt("OI_TARGET.COUNT", OIFITS.hasOiTarget() ? 1 : 0);
        putInt("OI_WAVELENGTH.COUNT", OIFITS.getNbOiWavelengths());
        putInt("OI_VIS.COUNT", OIFITS.getNbOiVis());
        putInt("OI_VIS2.COUNT", OIFITS.getNbOiVis2());
        putInt("OI_T3.COUNT", OIFITS.getNbOiT3());

        for (OITable oitable : OIFITS.getOiTables()) {
            dumpTable(oitable);
        }
    }

    public static void computeCustomExp(OIData[] datas) {
        // Compute custom expression on OIData tables:
        for (OIData oiData : datas) {
            oiData.checkExpression(COLUMN_SEC_2000, COLUMN_SEC_2000_EXPR);
            // Define the computed column:
            oiData.updateExpressionColumn(COLUMN_SEC_2000, COLUMN_SEC_2000_EXPR);
        }
    }

    private static void dumpTable(FitsTable table) {
        logger.log(Level.INFO, "Table: {0}", table.idToString());
        logger.log(Level.INFO, "nbRows: {0}", table.getNbRows());

        final String prefix = getHduId(table);

        putInt(prefix + ".NBRow", table.getNbRows());

        DumpFitsTest.dumpKeywords(table, getHduId(table));
        dumpColumns(table);
        dumpHeaderCards(table);

        // Specific tests:
        if (table instanceof OIData) {
            dumpOIData((OIData) table);
        }
    }

    private static void dumpOIData(OIData oidata) {

        final String hduId = getHduId(oidata);
        /** internal buffer */
        StringBuilder buffer = new StringBuilder();

        logger.log(Level.INFO, "Table: {0}", oidata.idToString());
        logger.log(Level.INFO, "nWaves: {0}", oidata.getNWave());
        putInt(hduId + ".nWave", oidata.getNWave());
        logger.log(Level.INFO, "nFlagged: {0}", oidata.getNFlagged());
        putInt(hduId + ".nFlagged", oidata.getNFlagged());

        Set<short[]> staIndexes = oidata.getDistinctStaIndex();
        logger.info("Distinct staIndexes:");

        for (short[] staIndex : staIndexes) {
            logger.log(Level.INFO, "{0} = {1}", new Object[]{Arrays.toString(staIndex), oidata.getStaNames(staIndex)});
            buffer.append(oidata.getStaNames(staIndex));
            buffer.append(", ");
        }
        put(hduId + ".staIndex", buffer.toString());

        buffer.setLength(0);

        Set<short[]> staConfs = oidata.getDistinctStaConf();
        logger.info("Distinct staConfs:");

        for (short[] staConf : staConfs) {
            logger.log(Level.INFO, "{0} = {1}", new Object[]{Arrays.toString(staConf), oidata.getStaNames(staConf)});
            buffer.append(oidata.getStaNames(staConf));
        }
        put(hduId + ".staConf", buffer.toString());

        logger.info("oidata");
    }

    private static void dumpColumns(FitsTable table) {

        final String hduId = getHduId(table);
        final String prefixC = hduId + ".C.";
        final String prefixDC = hduId + ".DC.";
        final String prefixMM = hduId + ".MM.";

        for (ColumnMeta columnMeta : table.getAllColumnDescCollection()) {
            dumpColumnMeta(columnMeta);

            if (table.getColumnDerivedDesc(columnMeta.getName()) != null) {
                dumpColumnValues(table, columnMeta, prefixDC);
                // No MinMax for derived columns
                // dumpColunmMinMax(table, columnMeta, prefixMM);
            } else {
                dumpColumnValues(table, columnMeta, prefixC);
                dumpColunmMinMax(table, columnMeta, prefixMM);
            }
        }
    }

    private static void dumpColumnValues(FitsTable table, ColumnMeta columnMeta, String prefix) {
        put(prefix + columnMeta.getName(), getColumnValues(table, columnMeta));
    }

    private static void dumpColunmMinMax(FitsTable table, ColumnMeta columnMeta, String prefix) {
        put(prefix + columnMeta.getName(), getColunmMinMax(table, columnMeta));
    }

    private static void dumpColumnMeta(ColumnMeta columnMeta) {
        DumpFitsTest.dumpMeta(columnMeta);

        logger.log(Level.INFO, "Alias      : {0}", columnMeta.getAlias());
        logger.log(Level.INFO, "DataRange  : {0}", columnMeta.getDataRange());
        logger.log(Level.INFO, "ErrName    : {0}", columnMeta.getErrorColumnName());
        logger.log(Level.INFO, "Array ?    : {0}", columnMeta.isArray());

        if (columnMeta instanceof WaveColumnMeta) {
            WaveColumnMeta wcm = (WaveColumnMeta) columnMeta;
            logger.log(Level.INFO, "Expr       : {0}", wcm.getExpression());
        }
    }

    private static void dumpHeaderCards(FitsTable table) {
        if (table.hasHeaderCards()) {
            DumpFitsTest.dumpHeaderCards(getHduId(table), table.getHeaderCards());
        }
    }

    public static String getHduId(FitsTable table) {
        return table.getExtName() + '.' + table.getExtNb();
    }
}
