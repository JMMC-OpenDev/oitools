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
package fr.jmmc.oitools.util.test;

import fr.jmmc.oitools.JUnitBaseTest;
import static fr.jmmc.oitools.JUnitBaseTest.TEST_DIR_OIFITS;
import fr.jmmc.oitools.OIFitsProcessor;
import fr.jmmc.oitools.model.OIFitsFile;
import fr.jmmc.oitools.model.OIFitsLoader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import junit.framework.Assert;
import nom.tam.fits.FitsException;
import org.junit.Test;

/**
 *
 * @author jammetv
 */
public class TestProcessorCommandLine {

    private static final int OUT_INDEX = 0;
    private static final int ERR_INDEX = 1;

    @Test
    public void testNoInputFile() {
        String[] result = callProcessor(new String[]{"merge"});
        Assert.assertTrue("Bad return message: " + result[ERR_INDEX],
                getError(result).startsWith("No file location given in arguments."));
    }

    @Test
    public void testNoOutputFile() {
        String[] result = callProcessor(new String[]{"merge",
            TEST_DIR_OIFITS + "A-CLUSTER__2T3T__1-PHASEREF__SIMPLE_nsr0.05__20160812_193521_1.image-oi.oifits",
            "-o"});
        Assert.assertTrue("Bad return message: " + result[ERR_INDEX],
                getError(result).startsWith("No output file"));
    }

    @Test
    public void testOk() {
        File output = new File(JUnitBaseTest.TEST_DIR_TEST + "merge_result.fits");
        output.delete();
        String[] result = callProcessor(new String[]{"merge", "-o", output.getAbsolutePath(),
            TEST_DIR_OIFITS + "A-CLUSTER__2T3T__1-PHASEREF__SIMPLE_nsr0.05__20160812_193521_1.image-oi.oifits",
            TEST_DIR_OIFITS + "A-CLUSTER__2T3T__1-PHASEREF__SIMPLE_nsr0.05__20160812_193521_1.oifits"
        });
        Assert.assertTrue("Bad return message: " + result[OUT_INDEX],
                getOut(result).length() == 0);
        Assert.assertTrue("No result file created", output.exists());
    }

    @Test
    public void testList() {
        String[] result = callProcessor(new String[]{"list",
            TEST_DIR_OIFITS + "A-CLUSTER__2T3T__1-PHASEREF__SIMPLE_nsr0.05__20160812_193521_1.image-oi.oifits"
        });
        Assert.assertTrue("Bad return message: " + result[OUT_INDEX],
                getOut(result).startsWith("Processing"));
    }

    @Test
    public void testConvert() {
        File output = new File(JUnitBaseTest.TEST_DIR_TEST + "convert_result.fits");
        output.delete();
        
        callProcessor(new String[]{"convert", "-o", output.getAbsolutePath(),
            TEST_DIR_OIFITS + "A-CLUSTER__2T3T__1-PHASEREF__SIMPLE_nsr0.05__20160812_193521_1.image-oi.oifits"
        });
        Assert.assertTrue("No output file present.", output.exists());
    }

    @Test
    public void testMergeFilterInsname() throws IOException, MalformedURLException, FitsException {
        File output = new File(JUnitBaseTest.TEST_DIR_TEST + "merge_filter_result.fits");
        output.delete();

        // Filter block
        String[] result = callProcessor(new String[]{"merge",
            "-o", output.getAbsolutePath(), "-insname", "SPECTRO_SC",
            TEST_DIR_OIFITS + "NGC5128_2005.oifits"
        });
        Assert.assertTrue("Bad return message: " + result[OUT_INDEX], 
                getOut(result) != null ? getOut(result).startsWith("Result is empty, no file created.") : false);
        Assert.assertTrue("No result file should be created", !output.exists());

        // Filter pass
        result = callProcessor(new String[]{"merge",
            "-o", output.getAbsolutePath(), "-insname", "MIDI/PRISM",
            TEST_DIR_OIFITS + "NGC5128_2005.oifits"
        });
        Assert.assertTrue("Bad return message: " + result[OUT_INDEX], getOut(result).length() == 0);
        OIFitsFile mergedFile = OIFitsLoader.loadOIFits(output.getAbsolutePath());
        Assert.assertEquals("Bad number of Ins in merged file", 1, mergedFile.getOiWavelengths().length);
    }

    private static String getOut(String[] result) {
        return result[OUT_INDEX] != null ? result[OUT_INDEX] : "";
    }

    private static String getError(String[] result) {
        return result[ERR_INDEX] != null ? result[ERR_INDEX] : "";
    }

    /**
     * Call merge, return out and err stream of the operation
     *
     * @param args
     * @return String[2], [0]: out, [1]: err
     */
    private static String[] callProcessor(String[] args) {
        String[] result = new String[2];

        // Create a stream to hold the output
        PrintStream psOut = null;
        PrintStream psErr = null;

        try {

            ByteArrayOutputStream baosOut = new ByteArrayOutputStream();
            ByteArrayOutputStream baosErr = new ByteArrayOutputStream();
            psOut = new PrintStream(baosOut);
            psErr = new PrintStream(baosErr);
            // IMPORTANT: Save the old System.out!
            PrintStream oldOut = System.out;
            PrintStream oldErr = System.err;
            // Tell Java to use special stream
            System.setOut(psOut);
            System.setErr(psErr);

            // Call operation
            OIFitsProcessor.main(args);

            System.out.flush();
            System.err.flush();
            System.setOut(oldOut);
            System.setErr(oldErr);

            result[0] = baosOut.toString();
            result[1] = baosErr.toString();
            return result;

        } finally {
            if (psOut != null) {
                psOut.close();
            }
            if (psErr != null) {
                psErr.close();
            }
        }
    }

}
