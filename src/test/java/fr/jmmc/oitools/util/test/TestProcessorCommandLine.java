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

import static fr.jmmc.oitools.JUnitBaseTest.TEST_DIR_OIFITS;
import static fr.jmmc.oitools.JUnitBaseTest.TEST_DIR;
import fr.jmmc.oitools.OIFitsProcessor;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import junit.framework.Assert;
import org.junit.Test;

/**
 *
 * @author jammetv
 */
public class TestProcessorCommandLine {

    private static final int OUT_INDEX = 0;
    private static final int ERR_INDEX = 1;

    @Test
    public void testNoParameter() {
        String[] result = callProcessor(new String[0]);
        Assert.assertTrue("Bad return message: " + result[ERR_INDEX],
                result[ERR_INDEX] != null && result[ERR_INDEX].startsWith("No parameters"));
    }

    @Test
    public void testNoInputFile() {
        String[] result = callProcessor(new String[]{"merge"});
        Assert.assertTrue("Bad return message: " + result[ERR_INDEX],
                result[ERR_INDEX] != null && result[ERR_INDEX].startsWith("No input file"));
    }

    @Test
    public void testNoOutputFile() {
        String[] result = callProcessor(new String[]{"merge",
            TEST_DIR_OIFITS + "A-CLUSTER__2T3T__1-PHASEREF__SIMPLE_nsr0.05__20160812_193521_1.image-oi.oifits",
            TEST_DIR_OIFITS + "A-CLUSTER__2T3T__1-PHASEREF__SIMPLE_nsr0.05__20160812_193521_1.oifits", "-o"
        });
        Assert.assertTrue("Bad return message: " + result[ERR_INDEX],
                result[ERR_INDEX] != null && result[ERR_INDEX].startsWith("No output file"));
    }

    @Test
    public void testOk() {
        File output = new File(TEST_DIR + "merge_result.oifits");
        String[] result = callProcessor(new String[]{"merge", "-o", output.getAbsolutePath(),
            TEST_DIR_OIFITS + "A-CLUSTER__2T3T__1-PHASEREF__SIMPLE_nsr0.05__20160812_193521_1.image-oi.oifits",
            TEST_DIR_OIFITS + "A-CLUSTER__2T3T__1-PHASEREF__SIMPLE_nsr0.05__20160812_193521_1.oifits"
        });
        Assert.assertTrue("Bad return message: " + result[OUT_INDEX],
                result[OUT_INDEX] != null && result[OUT_INDEX].length() == 0);
        Assert.assertTrue("No result file created", output.exists());
        output.delete();
    }

    @Test
    public void testList() {
        String[] result = callProcessor(new String[]{"list",
            TEST_DIR_OIFITS + "A-CLUSTER__2T3T__1-PHASEREF__SIMPLE_nsr0.05__20160812_193521_1.image-oi.oifits"
        });
        Assert.assertTrue("Bad return message: " + result[OUT_INDEX],
                result[OUT_INDEX] != null && result[OUT_INDEX].startsWith("<oifits>"));
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
