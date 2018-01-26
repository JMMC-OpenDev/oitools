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

import static fr.jmmc.oitools.DumpFitsTest.getHduId;
import fr.jmmc.oitools.fits.FitsHDU;
import fr.jmmc.oitools.fits.FitsHeaderCard;
import fr.jmmc.oitools.image.FitsImage;
import fr.jmmc.oitools.image.FitsImageFile;
import fr.jmmc.oitools.image.FitsImageHDU;
import fr.jmmc.oitools.image.FitsImageLoader;
import fr.jmmc.oitools.meta.KeywordMeta;
import fr.nom.tam.fits.FitsException;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Load Fits files from the test/fits folder and load [filename]-img.properties file from test/ref (reference files)
 * to compare the FitsImageFile structure with the stored (key / value) pairs.
 * @author kempsc
 */
public class LoadFitsTest extends AbstractFileBaseTest {

    // members:
    private static FitsImageFile FITS = null;

    @BeforeClass
    public static void setUpClass() {
        initializeTest();
    }

    @AfterClass
    public static void tearDownClass() {
        FITS = null;
        shutdownTest();
    }

    @Test
    public void loadFiles() throws IOException, FitsException {
        try {
            loadDirectory(new File(TEST_DIR_FITS));
            loadDirectory(new File(TEST_DIR_OIFITS));
        } catch (RuntimeException re) {
            logger.log(Level.SEVERE, "Failure: ", re);
            throw re;
        }
    }

    private void loadDirectory(final File directory) throws IOException, FitsException {

        for (String f : getFitsFiles(directory)) {
            // reset properties anyway
            reset();

            compareFits(f);

            checkAssertCount();
        }
    }

    private void compareFits(String f) throws IOException, FitsException {

        FITS = FitsImageLoader.load(f, false, false);

        logger.log(Level.INFO, "FITS:\n{0} \n------", FITS.toString());

        if (FITS.getImageHDUCount() != 0) {

            // Load property file to map
            load(new File(TEST_DIR_REF, FITS.getFileName() + "-img.properties"));

            assertEquals(get("FILENAME"), FITS.getFileName());
            assertEqualsInt(get("IMAGE.HDU.COUNT"), FITS.getImageHDUCount());

            for (FitsImageHDU imageHdu : FITS.getFitsImageHDUs()) {
                compare(imageHdu);
            }
        }
    }

    public static void compare(FitsImageHDU imageHdu) {

        final String hduId = getHduId(imageHdu);

        assertEqualsInt(get(hduId + ".IMAGE.COUNT"), imageHdu.getImageCount());

        compareKeywords(imageHdu, getHduId(imageHdu));
        compareHeaderCards(imageHdu);
        compareImages(imageHdu);
    }

    public static void compareKeywords(FitsHDU hduFits, String hduId) {
        Object expected, value;
        final String prefix = hduId + ".K.";

        for (KeywordMeta keyword : hduFits.getKeywordDescCollection()) {

            expected = get(prefix + keyword.getName());
            value = hduFits.getKeywordValue(keyword.getName());
            if (value == null) {
                value = "null";
            }
            assertEquals(expected, value.toString());
        }
    }

    private static void compareImages(FitsImageHDU imageHdu) {

        final String prefix = getHduId(imageHdu) + ".IMG.";

        for (FitsImage image : imageHdu.getFitsImages()) {
            assertEquals(get(prefix + image.getImageIndex()), image.toString());
        }
    }

    private static void compareHeaderCards(FitsImageHDU imageHdu) {

        if (imageHdu.hasHeaderCards()) {
            final String hduId = getHduId(imageHdu);

            compareHeaderCards(hduId, imageHdu.getHeaderCards());
        }
    }

    public static void compareHeaderCards(final String hduId, final List<FitsHeaderCard> headerCards) {
        if (!headerCards.isEmpty()) {
            final String prefix = hduId + ".HC.";
            final Set<String> usedKeys = new HashSet<String>(headerCards.size());

            for (FitsHeaderCard headerCard : headerCards) {
                final String propKey = prefix + headerCard.getKey();
                final String propValue = headerCard.getValue() + " // " + headerCard.getComment();

                String propKeyFinal;
                if (!usedKeys.contains(propKey)) {
                    propKeyFinal = propKey;
                } else {
                    // key is already used
                    int i = 2;
                    do {
                        propKeyFinal = propKey + i;
                        i++;
                    } while (usedKeys.contains(propKeyFinal));
                }
                assertEquals(get(propKeyFinal), propValue);
                usedKeys.add(propKeyFinal);
            }
        }
    }
}
