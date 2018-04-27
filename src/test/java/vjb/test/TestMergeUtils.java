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
package vjb.test;

import fr.jmmc.oitools.JUnitBaseTest;
import fr.jmmc.oitools.model.OIFitsFile;
import fr.jmmc.oitools.model.OIFitsLoader;
import fr.jmmc.oitools.model.OITarget;
import fr.jmmc.oitools.model.Target;
import fr.jmmc.oitools.util.MergeUtils;
import fr.nom.tam.fits.FitsException;
import java.io.IOException;
import java.net.MalformedURLException;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author jammetv
 */
public class TestMergeUtils extends JUnitBaseTest {

    @Test
    public void testBasic() throws IOException, MalformedURLException, FitsException {

        OIFitsFile f1 = OIFitsLoader.loadOIFits(
                TEST_DIR_OIFITS + "A-CLUSTER__2T3T__1-PHASEREF__SIMPLE_nsr0.05__20160812_193521_1.image-oi.oifits");
        try {
            OIFitsFile merge = MergeUtils.mergeOIFitsFile(f1, OIFitsLoader.loadOIFits(
                    TEST_DIR_OIFITS + "NGC5128_2005.oifits"));
            
            // validate + write
            
            Assert.fail("Merge of two files with different target should raise an exception");
        } catch (IllegalArgumentException iae) {
            Assert.assertTrue(String.format("Message should contains 'not the same target' (is '%s')", iae.getMessage()),
                    iae.getMessage().contains("not the same target"));
        }


    }

}
