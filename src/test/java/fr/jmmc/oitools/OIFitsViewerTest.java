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

import fr.jmmc.oitools.util.FileUtils;
import fr.nom.tam.fits.FitsException;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.logging.Level;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author bourgesl
 */
public class OIFitsViewerTest extends AbstractFileBaseTest {

    private static final String[] OIFITS_FILENAME = new String[]{
        "TEST_CreateOIFileV2.fits",
        "2008-Contest_Binary.oifits",
        "GRAVI.2016-06-23T03:10:17.458_singlesciviscalibrated.fits",
        "A-CLUSTER__2T3T__1-PHASEREF__SIMPLE_nsr0.05__20160812_193521_1.image-oi.oifits",
        "2012-03-24_ALL_oiDataCalib.fits",
        "testdata_opt.fits"
    };

    private final static int REMOTE_COUNT = 1;
    private final static String OIFITS_URL = "http://apps.jmmc.fr/oidata/BeautyContest/2008-Contest_Binary.oifits";

    @Test
    public void useOIFitsViewer() {
        for (String fileName : OIFITS_FILENAME) {
            String[] args = new String[]{
                "-t",
                "-c",
                "-v",
                /* file */
                TEST_DIR_OIFITS + fileName
            };
            OIFitsViewer.main(args);
        }
        String[] args = new String[]{
            "-t",
            "-c",
            "-v",
            /* file */
            getProjectFolderPath() + "src/test/resources/corrupted/Multi_OIINSPOL_TESTV2.fits"
        };
        OIFitsViewer.main(args);
    }

    @Test
    public void dumpTSV() throws IOException, FitsException {
        final OIFitsViewer viewer = new OIFitsViewer(false, true, false, true);
        testProcessFiles("dumpTSV", ".csv", viewer);
    }

    @Test
    public void dumpXML() throws IOException, FitsException {
        final OIFitsViewer viewer = new OIFitsViewer(true, false, false, true);
        testProcessFiles("dumpXML", ".xml", viewer);
    }

    @Test
    public void dumpFormattedXML() throws IOException, FitsException {
        final OIFitsViewer viewer = new OIFitsViewer(true, false, true, true);
        testProcessFiles("dumpFormattedXML", "-formatted.xml", viewer);
    }

    @Test
    public void existDB_check() throws IOException, FitsException {
        for (String fileName : OIFITS_FILENAME) {
            existDB_process(TEST_DIR_OIFITS + fileName, false);
        }
    }

    @Test
    public void existDB_to_xml() throws IOException, FitsException {
        for (String fileName : OIFITS_FILENAME) {
            try {
                final String result = existDB_process(TEST_DIR_OIFITS + fileName, true);
                logger.log(Level.INFO, "existDB_to_xml:\n{0}", result);

                try {
                    DocumentBuilderFactory dBF = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = dBF.newDocumentBuilder();
                    InputSource is = new InputSource(new StringReader(result));
                    builder.parse(is);
                } catch (ParserConfigurationException ex) {
                    logger.log(Level.INFO, "existDB_to_xml:", ex);
                    // do not consider this case as a faulty xml output
                } catch (SAXException ex) {
                    logger.log(Level.INFO, "existDB_to_xml:", ex);
                    Assert.fail("OIFitsViewer xml output is not well formed");
                }

            } catch (RuntimeException re) {
                logger.log(Level.INFO, "existDB_to_xml:", re);
                // unexpected
                Assert.fail("exception not expected");
            }
        }
    }

    @Test
    public void existDB_unknown() throws IOException, FitsException {
        for (String fileName : OIFITS_FILENAME) {
            try {
                existDB_process(TEST_DIR_OIFITS + fileName + "UNKNOWN", true);
                Assert.fail("exception expected");
            } catch (RuntimeException re) {
                logger.log(Level.INFO, "existDB_unknown failure as expected:", re);
                // expected
            }
        }
    }

    @Test
    public void existDB_remote_to_xml() throws IOException, FitsException {
        try {
            for (int i = 0; i < REMOTE_COUNT; i++) {
                final String result = existDB_process(OIFITS_URL, true);
                logger.log(Level.INFO, "existDB_remote_to_xml:\n{0}", result);
                try {
                    Thread.sleep(10l);
                } catch (InterruptedException ie) {
                    logger.log(Level.INFO, "Interrupted");
                    break;
                }
            }
        } catch (RuntimeException re) {
            logger.log(Level.INFO, "existDB_remote_to_xml:", re);
            // unexpected
            Assert.fail("exception not expected");
        }
    }

    private static String existDB_process(final String filename, final boolean outputXml) {
        String output = null;
        try {
            logger.log(Level.INFO, "Process data from {0}", filename);
            final OIFitsViewer viewer = new OIFitsViewer(outputXml, true, false);
            output = viewer.process(filename);
        } catch (final Exception e) {
            throw new RuntimeException("Can't read oifits properly: " + e.getMessage(), e);
        }
        return output;
    }

    private void testProcessFiles(final String testName, final String extension, final OIFitsViewer viewer) throws IOException, FitsException {
        logger.log(Level.INFO, "testProcessFiles: {0}", testName);
        int nFailures = 0;
        String failureMsg = "";
        for (String fileName : OIFITS_FILENAME) {
            try {
                String result = viewer.process(TEST_DIR_OIFITS + fileName);

                // Remove any absolute path (depending on the local machine):
                result = result.replaceAll(TEST_DIR_OIFITS, "");

                saveAndCompare(testName, fileName + extension, result);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "exception:", e);
                nFailures++;
                failureMsg += e.getMessage() + "\n";
            } catch (AssertionError assertFailed) {
                nFailures++;

                final File logFile = new File(getProjectFolderPath(), "OIFitsViewer-" + testName + "-" + fileName + ".log");
                FileUtils.writeFile(logFile, assertFailed.getMessage());

                failureMsg += "Assertion error: see log file: " + logFile + "\n";
            }
        }
        if (!failureMsg.isEmpty()) {
            Assert.fail(testName + " failed " + nFailures + " times:\n" + failureMsg);
        }
    }

    private void saveAndCompare(final String testName, final String fileName, final String result) throws IOException {
        logger.log(Level.FINE, "{0}:\n{1}", new Object[]{testName, result});
        FileUtils.writeFile(new File(TEST_DIR_TEST, fileName), result);

        String expected = FileUtils.readFile(new File(TEST_DIR_REF + fileName));
        assertEquals(expected, result);
    }

}
