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

    @Test
    public void testNoParameter() {
        String result = callProcessor(new String[0]);
        Assert.assertTrue("Bad return message: " + result, result.startsWith("No parameters"));
    }

    @Test
    public void testNoInputFile() {
        String result = callProcessor(new String[] {"merge" });
        Assert.assertTrue("Bad return message: " + result, result.startsWith("No input file"));
    }

    @Test
    public void testNoOutputFile() {
        String result = callProcessor(new String[] {"merge", 
            TEST_DIR_OIFITS + "A-CLUSTER__2T3T__1-PHASEREF__SIMPLE_nsr0.05__20160812_193521_1.image-oi.oifits",
            TEST_DIR_OIFITS + "A-CLUSTER__2T3T__1-PHASEREF__SIMPLE_nsr0.05__20160812_193521_1.oifits", "-o"
        });
        Assert.assertTrue("Bad return message: " + result, result.startsWith("No output file"));
    }

    @Test
    public void testOk() {
        File output = new File("./merge_result.oifits");
        String result = callProcessor(new String[] {"merge", "-o", output.getAbsolutePath(), 
            TEST_DIR_OIFITS + "A-CLUSTER__2T3T__1-PHASEREF__SIMPLE_nsr0.05__20160812_193521_1.image-oi.oifits",
            TEST_DIR_OIFITS + "A-CLUSTER__2T3T__1-PHASEREF__SIMPLE_nsr0.05__20160812_193521_1.oifits"
        });
        Assert.assertEquals("Bad return message: " + result, "", result);
        Assert.assertTrue( "No result file created", output.exists());
    }

    @Test
    public void testList() {
        String result = callProcessor(new String[] {"list", 
            TEST_DIR_OIFITS + "A-CLUSTER__2T3T__1-PHASEREF__SIMPLE_nsr0.05__20160812_193521_1.image-oi.oifits"
        });
        Assert.assertEquals("Bad return message: " + result, "", result);
    }

    
    /**
     * 
     * @param args
     * @return 
     */
    private static String callProcessor(String[] args) {
        // Create a stream to hold the output
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        // IMPORTANT: Save the old System.out!
        PrintStream old = System.out;
        // Tell Java to use your special stream
        System.setOut(ps);
        // Print some output: goes to your special stream
        OIFitsProcessor.main(args);
        System.out.flush();
        System.setOut(old);
        return baos.toString();
    }
    
}
