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
package fr.jmmc.oitools.test.fits;

import fr.jmmc.oitools.TestArrayFuncs;
import fr.jmmc.oitools.fits.FitsUtils;
import fr.jmmc.oitools.test.TestEnv;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import nom.tam.fits.BasicHDU;
import nom.tam.fits.BinaryTable;
import nom.tam.fits.BinaryTableHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.FitsFactory;
import nom.tam.fits.Header;
import nom.tam.fits.HeaderCard;
import nom.tam.fits.LibFitsAdapter;
import nom.tam.util.ArrayFuncs;
import nom.tam.util.BufferedFile;

/**
 * This class makes several tests on nom.tam fits library
 * @author bourgesl
 */
public class TamFitsTest implements TestEnv {

    /** flag to disable infoFile() */
    private final static boolean INFO_ENABLE = false;
    /** flag to dump column content */
    private final static boolean PRINT_COL = false;
    /** flag to compare keyword comments */
    private static boolean COMPARE_KEYWORD_COMMENTS = false;
    /** flag to enable HIERARCH keyword support */
    private final static boolean USE_HIERARCH_FITS_KEYWORDS = true;
    /** flag to enable strict comparison */
    private static boolean STRICT = true;

    static {
        // enable / disable HIERARCH keyword support :
        FitsFactory.setUseHierarch(USE_HIERARCH_FITS_KEYWORDS);
    }

    public static boolean isStrict() {
        return STRICT;
    }

    public static void setStrict(boolean strict) {
        TamFitsTest.STRICT = strict;
    }

    /**
     * Forbidden constructor
     */
    private TamFitsTest() {
        super();
    }

    public static int infoFile(final String absFilePath) {
        if (!INFO_ENABLE) {
            return 0;
        }
        int error = 0;

        try {
            logger.log(Level.INFO, "Reading file : {0}", absFilePath);

            final long start = System.nanoTime();

            final Fits f = new Fits(absFilePath);

            BasicHDU h;

            int i = 0;
            do {
                h = f.readHDU();
                if (h != null) {
                    if (i == 0) {
                        logger.info("\n\nPrimary header:\n");
                    } else {
                        logger.log(Level.INFO, "\n\nExtension {0}:\n", i);
                    }
                    i++;

                    h.info(System.out);
                }
            } while (h != null);

            logger.log(Level.INFO, "infoFile : duration = {0} ms.", 1e-6d * (System.nanoTime() - start));

        } catch (Throwable th) {
            logger.log(Level.SEVERE, "infoFile : IO failure occured while reading file : " + absFilePath, th);
            error = 1;
        }
        return error;
    }

    public static int dumpFile(final String absFilePath) {
        return FitsUtils.dumpFile(absFilePath, PRINT_COL) ? 1 : 0;
    }

    public static int copyFile(final String absSrcPath, final String absDestPath) {
        int error = 0;

        BufferedFile bf = null;
        try {
            logger.log(Level.INFO, "Copying file : {0} to {1}", new Object[]{absSrcPath, absDestPath});

            final long start = System.nanoTime();

            final Fits f = new Fits(absSrcPath);

            // read the complete file in memory :
            f.read();

            bf = new BufferedFile(absDestPath, "rw");

            f.write(bf);
            bf.close();
            bf = null;

            logger.log(Level.INFO, "copyFile : duration = {0} ms.", 1e-6d * (System.nanoTime() - start));

        } catch (Exception e) {
            logger.log(Level.SEVERE, "copyFile : IO failure occured while copying file : " + absSrcPath, e);
            error = 1;
        } finally {
            if (bf != null) {
                try {
                    bf.close();
                } catch (IOException ioe) {
                    logger.log(Level.SEVERE, "copyFile : IO failure occured while closing file : " + absDestPath, ioe);
                    error = 1;
                }
            }
        }
        return error;
    }

    public static boolean compareFile(final String absSrcPath, final String absDestPath) {
        boolean res = true;

        try {
            logger.log(Level.INFO, "Comparing files : {0}, {1}", new Object[]{absSrcPath, absDestPath});

            final Fits s = new Fits(absSrcPath);
            final Fits d = new Fits(absDestPath);

            final BasicHDU[] srcHdus = s.read();
            final BasicHDU[] dstHdus = d.read();

            if (srcHdus.length != dstHdus.length) {
                logger.log(Level.INFO, "ERROR:  different number of hdu {0} <> {1}", new Object[]{srcHdus.length, dstHdus.length});
            } else {
                final int len = srcHdus.length;
                logger.log(Level.INFO, "HDUs = {0}", len);

                BasicHDU srcHdu, dstHdu;
                for (int i = 0; i < len; i++) {
                    srcHdu = srcHdus[i];
                    dstHdu = dstHdus[i];

                    if (srcHdu.getClass() != dstHdu.getClass()) {
                        logger.log(Level.INFO, "ERROR:  different type of hdu {0} <> {1}", new Object[]{srcHdu.getClass(), dstHdu.getClass()});
                    } else {
                        res &= compareHDU(srcHdu, dstHdu);
                    }
                }
            }

        } catch (Throwable th) {
            logger.log(Level.SEVERE, "compareFile : failure occured while comparing files : " + absSrcPath + ", " + absDestPath, th);
            res = false;
        }

        return res;
    }

    private static boolean compareHDU(final BasicHDU srcHdu, final BasicHDU dstHdu) throws FitsException {

        if (!isStrict()) {
            if (!(srcHdu instanceof BinaryTableHDU) || !(dstHdu instanceof BinaryTableHDU)) {
                // if not strict ignore not binary table
                return true;
            }

        }
        // Headers:
        boolean res = compareHeader(srcHdu.getHeader(), dstHdu.getHeader());

        // Datas:
        if (srcHdu instanceof BinaryTableHDU && dstHdu instanceof BinaryTableHDU) {
            res &= compareData((BinaryTableHDU) srcHdu, (BinaryTableHDU) dstHdu);
        } else {
            logger.log(Level.INFO, "Unsupported HDU: {0}", srcHdu.getClass());
        }

        return res;
    }

    private static boolean compareHeader(final Header srcHeader, final Header dstHeader) {
        boolean res = true;

        final String sExtName = srcHeader.getStringValue("EXTNAME");
        final String dExtName = dstHeader.getStringValue("EXTNAME");

        if (sExtName != null && !sExtName.equals(dExtName)) {
            logger.log(Level.INFO, "ERROR:  different extension name {0} <> {1}", new Object[]{sExtName, dExtName});
            res = false;
        } else {
            logger.info("--------------------------------------------------------------------------------");
            logger.log(Level.INFO, "EXTNAME = {0}", sExtName);

            final int sCard = srcHeader.getNumberOfCards();
            final int dCard = dstHeader.getNumberOfCards();

            if (sCard != dCard) {
                logger.log(Level.INFO, "{0} different number of header card {1} <> {2}", new Object[]{errorPrefix(sExtName), sCard, dCard});
                res = false;
            }
            logger.log(Level.INFO, "KEYWORDS = {0}", sCard);

            HeaderCard srcCard, dstCard;
            String key;
            for (Iterator<?> it = srcHeader.iterator(); it.hasNext();) {
                srcCard = (HeaderCard) it.next();

                key = srcCard.getKey();

                if (key.equals("END")) {
                    break;
                }

                dstCard = dstHeader.findCard(key);

                if (dstCard == null) {
                    logger.log(Level.INFO, "{0} Missing header card {1} was = {2}", new Object[]{errorPrefix(sExtName), key, srcCard.getValue()});
                    res = false;
                } else {
                    logger.log(Level.INFO, "KEYWORD {0} = {1}\t// {2}", new Object[]{key, srcCard.getValue() != null ? "'" + srcCard.getValue() + "'" : "", srcCard.getComment()});
                    if (!(srcCard.getValue() != null ? srcCard.getValue().equals(dstCard.getValue()) : dstCard.getValue() == null)) {

                        res = particularCase(res, key, srcCard, dstCard, sExtName);

                    } else if (COMPARE_KEYWORD_COMMENTS && isChanged(srcCard.getComment(), dstCard.getComment())) {
                        logger.log(Level.INFO, "ERROR:  different comment of header card[{0}] ''{1}'' <> ''{2}''", new Object[]{key, srcCard.getComment(), dstCard.getComment()});
                        res = false;
                    }
                }
            }
        }

        return res;
    }

    private static boolean particularCase(boolean res, String key, HeaderCard srcCard, HeaderCard dstCard, String sExtName) {
        if (key.startsWith("TUNIT")) {
            logger.log(Level.INFO, "WARNING:  different value   of header card[{0}] ''{1}'' <> ''{2}''", new Object[]{key, srcCard.getValue(), dstCard.getValue()});
            res = true;
        } else if (key.startsWith("TFORM") && ("1" + srcCard.getValue()).equals(dstCard.getValue())) {
            logger.log(Level.INFO, "INFO:  different value   of header card[{0}] ''{1}'' <> ''{2}''", new Object[]{key, srcCard.getValue(), dstCard.getValue()});
        } else if (!isStrict() && (key.startsWith("ARRAY") || key.startsWith("NAXIS1")
                || key.startsWith("TFORM"))) {
            //if we are not strict, we chose to ignore the following cases: 
            //ARRAY XYZ because the voluntary error comes from a correction of the OIFits format 
            //NAXIS1 never be right if we have modification and correction in the write
            //TFORM because is a correction in the write for respect the OIFits format 
            res = true;
        } else {
            logger.log(Level.INFO, "{0}different value   of header card[{1}] ''{2}'' <> ''{3}", new Object[]{errorPrefix(sExtName), key, srcCard.getValue(), dstCard.getValue()});
            res = false;
        }
        return res;
    }
    
    private static String errorPrefix(final String sExtName) {
        if (sExtName != null) {
            return "ERROR: " + sExtName;
        }
        return "ERROR: ";
    }

    private static boolean isChanged(final String value1, final String value2) {
        return (value1 == null && value2 != null) || (value1 != null && value2 == null) || (value1 != null && value2 != null && !value1.trim().equalsIgnoreCase(value2.trim()));
    }

    private static boolean compareData(final BinaryTableHDU srcHdu, final BinaryTableHDU dstHdu) throws FitsException {

        final BinaryTable sData = (BinaryTable) srcHdu.getData();
        final BinaryTable dData = (BinaryTable) dstHdu.getData();

        boolean res = true;

        final int sCol = sData.getNCols();
        final int dCol = dData.getNCols();

        if (sCol != dCol) {
            logger.log(Level.INFO, "ERROR:  different number of columns {0} <> {1} in {2}", new Object[]{sCol, dCol, srcHdu.getColumnName(dCol)});
            res = false;
        } else {
            logger.info("--------------------------------------------------------------------------------");
            logger.log(Level.INFO, "NCOLS = {0}", sCol);

            final int sRow = sData.getNRows();
            final int dRow = dData.getNRows();

            if (sCol != dCol) {
                logger.log(Level.INFO, "ERROR:  different number of rows {0} <> {1}", new Object[]{sRow, dRow});
                res = false;
            } else {
                logger.log(Level.INFO, "NROWS = {0}", sRow);

                Object sArray, dArray;
                for (int i = 0; i < sCol; i++) {
                    sArray = sData.getColumn(i);
                    dArray = dData.getColumn(i);
                    /*
                     sArray = sData.getFlattenedColumn(i);
                     dArray = dData.getFlattenedColumn(i);
                     */
                    if (!TestArrayFuncs.arrayEquals(sArray, dArray)) {
                        logger.log(Level.INFO, "ERROR:  different values for column[{0}]\nSRC={1}\nDST={2}", new Object[]{srcHdu.getColumnName(i), FitsUtils.arrayToString(sArray), FitsUtils.arrayToString(dArray)});
                        res = false;
                    } else {
                        if (PRINT_COL) {
                            logger.log(Level.INFO, "COLUMN {0}\t{1}\n{2}", new Object[]{srcHdu.getColumnName(i), nom.tam.util.ArrayFuncs.arrayDescription(sArray), FitsUtils.arrayToString(sArray)});
                        } else {
                            logger.log(Level.INFO, "COLUMN {0}\t{1}", new Object[]{srcHdu.getColumnName(i), nom.tam.util.ArrayFuncs.arrayDescription(sArray)});
                        }
                    }
                }
            }
        }

        return res;
    }
}
