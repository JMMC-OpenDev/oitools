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
import fr.jmmc.oitools.fits.FitsHDU;
import fr.jmmc.oitools.fits.FitsHeaderCard;
import fr.jmmc.oitools.image.FitsImage;
import fr.jmmc.oitools.image.FitsImageFile;
import fr.jmmc.oitools.image.FitsImageHDU;
import fr.jmmc.oitools.image.FitsImageLoader;
import fr.jmmc.oitools.meta.CellMeta;
import fr.jmmc.oitools.meta.KeywordMeta;
import fr.nom.tam.fits.FitsException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Load Fits files from the test/fits folder to dump the FitsImageFile structure 
 * into a [filename]-img.properties file (key / value pairs) in the test/test folder.
 * 
 * @see LoadOIFitsTest
 * @author bourgesl
 */
public class DumpFitsTest extends AbstractFileBaseTest {

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
    public void dumpFiles() throws IOException, FitsException {
        try {
            dumpDirectory(new File(TEST_DIR_FITS));
            dumpDirectory(new File(TEST_DIR_OIFITS));
        } catch (RuntimeException re) {
            logger.log(Level.SEVERE, "Failure: ", re);
            throw re;
        }
    }

    private void dumpDirectory(final File directory) throws IOException, FitsException {

        for (String f : getFitsFiles(directory)) {
            // reset properties anyway
            reset();

            dumpFits(f);

            save(new File(TEST_DIR_TEST, FITS.getFileName() + "-img.properties"));
        }
    }

    private void dumpFits(String f) throws IOException, FitsException {

        FITS = FitsImageLoader.load(f, false, false);

        logger.log(Level.INFO, "FITS:\n{0} \n------", FITS.toString());

        if (FITS.getImageHDUCount() != 0) {
            // Only dump FITS files having images
            boolean hasImage = false;
            for (FitsImageHDU imageHdu : FITS.getFitsImageHDUs()) {
                if (imageHdu.hasImages()) {
                    hasImage = true;
                    break;
                }
            }

            if (hasImage) {
                put("FILENAME", FITS.getFileName());
                putInt("IMAGE.HDU.COUNT", FITS.getImageHDUCount());

                for (FitsImageHDU imageHdu : FITS.getFitsImageHDUs()) {
                    dump(imageHdu);
                }
            }
        }
    }

    public static void dump(FitsImageHDU imageHdu) {
        logger.log(Level.INFO, "HduIndex: {0}", imageHdu.getExtNb());
        logger.log(Level.INFO, "Checksum: {0}", imageHdu.getChecksum());
        logger.log(Level.INFO, "ImageCount: {0}", imageHdu.getImageCount());

        final String hduId = getHduId(imageHdu);

        putInt(hduId + ".IMAGE.COUNT", imageHdu.getImageCount());

        dumpKeywords(imageHdu, getHduId(imageHdu));
        dumpHeaderCards(imageHdu);
        dumpImages(imageHdu);
    }

    public static void dumpKeywords(FitsHDU hduFits, String hduId) {
        final String prefix = hduId + ".K.";

        for (KeywordMeta keywordMeta : hduFits.getKeywordDescCollection()) {
            dumpMeta(keywordMeta);

            Object value = hduFits.getKeywordValue(keywordMeta.getName());

            put(prefix + keywordMeta.getName(), value != null
                    ? value.toString() : "null");

            logger.log(Level.INFO, "keyword: {0} = {1} :: {2}", new Object[]{keywordMeta.getName(), value, value != null ? value.getClass().getSimpleName() : "!NULL!"});
        }
    }

    public static void dumpMeta(CellMeta meta) {
        logger.log(Level.INFO, "Name       : {0}", meta.getName());

        logger.log(Level.INFO, "Data type  : {0}", meta.getDataType());
        logger.log(Level.INFO, "Type       : {0}", meta.getType());

        logger.log(Level.INFO, "Units      : {0}", meta.getUnits());
        logger.log(Level.INFO, "Unit       : {0}", meta.getUnit());

        logger.log(Level.INFO, "Description: {0}", meta.getDescription());
    }

    private static void dumpImages(FitsImageHDU imageHdu) {

        final String prefix = getHduId(imageHdu) + ".IMG.";

        for (FitsImage image : imageHdu.getFitsImages()) {
            put(prefix + image.getImageIndex(), image.toString());

            logger.log(Level.INFO, "image:\n{0}", image.toString());
        }
    }

    private static void dumpHeaderCards(FitsImageHDU imageHdu) {
        if (imageHdu.hasHeaderCards()) {
            final String hduId = getHduId(imageHdu);

            dumpHeaderCards(hduId, imageHdu.getHeaderCards());
        }
    }

    public static void dumpHeaderCards(final String hduId, final List<FitsHeaderCard> headerCards) {
        if (!headerCards.isEmpty()) {
            final String prefix = hduId + ".HC.";

            for (FitsHeaderCard headerCard : headerCards) {
                final String propKey = prefix + headerCard.getKey();
                final String propValue = headerCard.getValue() + " // " + headerCard.getComment();

                String propKeyFinal;
                if (!contains(propKey)) {
                    propKeyFinal = propKey;
                } else {
                    // key is already used
                    int i = 2;
                    do {
                        propKeyFinal = propKey + i;
                        i++;
                    } while (contains(propKeyFinal));
                }

                logger.log(Level.INFO, "{0} = {1}", new Object[]{propKeyFinal, propValue});
                put(propKeyFinal, propValue);
            }
        }
    }

    public static String getHduId(FitsImageHDU imageHdu) {
        return "ImageHDU." + imageHdu.getExtNb();
    }

}
