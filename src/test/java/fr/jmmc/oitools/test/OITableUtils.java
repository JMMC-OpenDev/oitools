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
package fr.jmmc.oitools.test;

import fr.jmmc.oitools.fits.FitsHDU;
import fr.jmmc.oitools.fits.FitsTable;
import fr.jmmc.oitools.fits.FitsUtils;
import fr.jmmc.oitools.image.FitsImage;
import fr.jmmc.oitools.image.FitsImageHDU;
import fr.jmmc.oitools.image.ImageOiData;
import fr.jmmc.oitools.meta.ColumnMeta;
import fr.jmmc.oitools.meta.KeywordMeta;
import fr.jmmc.oitools.model.OIFitsFile;
import fr.jmmc.oitools.model.OITable;
import fr.nom.tam.util.ArrayFuncs;
import java.util.Arrays;
import java.util.logging.Level;

/**
 *
 * @author bourgesl
 */
public final class OITableUtils implements TestEnv {

    /** flag to dump column content */
    private final static boolean PRINT_COL = false;
    /** flag to use tolerance when comparing floating numbers */
    private final static boolean USE_TOLERANCE = true;
    /** flag to dump column content */
    private final static double TOLERANCE_FLOAT = 1e-30d;
    /** flag to dump column content */
    private final static double TOLERANCE_DOUBLE = 1e-30d;

    /**
     * Forbidden constructor
     */
    private OITableUtils() {
        super();
    }

    public static boolean compareOIFitsFile(final OIFitsFile srcOIFitsFile, final OIFitsFile dstOIFitsFile) {
        return compareOIFitsFile(srcOIFitsFile, dstOIFitsFile, false);
    }

    public static boolean compareOIFitsFile(final OIFitsFile srcOIFitsFile, final OIFitsFile dstOIFitsFile, final boolean compareImages) {
        boolean res = true;

        try {
            logger.log(Level.INFO, "Comparing files : {0}, {1}", new Object[]{srcOIFitsFile.getAbsoluteFilePath(), dstOIFitsFile.getAbsoluteFilePath()});

            if (compareImages) {
                if (srcOIFitsFile.getImageHDUCount() != dstOIFitsFile.getImageHDUCount()) {
                    logger.log(Level.INFO, "ERROR:  different number of Image hdu {0} <> {1}", new Object[]{srcOIFitsFile.getImageHDUCount(), dstOIFitsFile.getImageHDUCount()});
                } else {
                    final int len = srcOIFitsFile.getImageHDUCount();
                    logger.log(Level.INFO, "ImageHDUs = {0}", len);

                    FitsImageHDU srcImage, dstImage;
                    for (int i = 0; i < len; i++) {
                        srcImage = srcOIFitsFile.getFitsImageHDUs().get(i);
                        dstImage = dstOIFitsFile.getFitsImageHDUs().get(i);

                        if (srcImage.getClass() != dstImage.getClass()) {
                            logger.log(Level.INFO, "ERROR:  different type of FitsImageHDU {0} <> {1}", new Object[]{srcImage.getClass(), dstImage.getClass()});
                        } else {
                            res &= compareImage(srcImage, dstImage);
                        }
                    }
                }
            }

            if (srcOIFitsFile.getNbOiTables() != dstOIFitsFile.getNbOiTables()) {
                logger.log(Level.INFO, "ERROR:  different number of hdu {0} <> {1}", new Object[]{srcOIFitsFile.getNbOiTables(), dstOIFitsFile.getNbOiTables()});
            } else {
                final int len = srcOIFitsFile.getNbOiTables();
                logger.log(Level.INFO, "OITables = {0}", len);

                OITable srcTable, dstTable;
                for (int i = 0; i < len; i++) {
                    srcTable = srcOIFitsFile.getOiTable(i);
                    dstTable = dstOIFitsFile.getOiTable(i);

                    if (srcTable.getClass() != dstTable.getClass()) {
                        logger.log(Level.INFO, "ERROR:  different type of OI_Table {0} <> {1}", new Object[]{srcTable.getClass(), dstTable.getClass()});
                    } else {
                        res &= compareTable(srcTable, dstTable);
                    }
                }
            }

            // Compare IMAGE-OI tables:
            if (srcOIFitsFile.getExistingImageOiData() != null) {
                final ImageOiData srcImageOiData = srcOIFitsFile.getImageOiData();
                final ImageOiData dstImageOiData = dstOIFitsFile.getImageOiData();

                FitsTable srcTable, dstTable;

                srcTable = srcImageOiData.getInputParam();
                dstTable = dstImageOiData.getInputParam();

                res &= compareTable(srcTable, dstTable);

                srcTable = srcImageOiData.getExistingOutputParam();
                dstTable = dstImageOiData.getExistingOutputParam();

                if (srcTable != null) {
                    if (dstTable == null) {
                        logger.log(Level.INFO, "ERROR:  missing OutputParam table {0} <> {1}", new Object[]{srcTable, dstTable});
                        res = false;
                    } else {
                        res &= compareTable(srcTable, dstTable);
                    }
                }
            }

        } catch (Throwable th) {
            logger.log(Level.SEVERE, "compareFile : failure occured while comparing files : " + srcOIFitsFile.getAbsoluteFilePath() + ", " + dstOIFitsFile.getAbsoluteFilePath(), th);
            res = false;
        }

        return res;
    }

    public static boolean compareImage(final FitsImageHDU srcImageHdu, final FitsImageHDU dstImageHdu) {

        // Headers :
        boolean res = compareHeader(srcImageHdu, dstImageHdu);

        final String srcHeader = srcImageHdu.getHeaderAsString("\n");
        final String dstHeader = srcImageHdu.getHeaderAsString("\n");

        logger.log(Level.INFO, "Image header src:\n---\n{0}---", srcHeader);

        if (isChanged(srcHeader, dstHeader)) {
            logger.log(Level.INFO, "ERROR:  different value of headers \n---\n{0}--- <=> ---\n{1}---", new Object[]{srcHeader, dstHeader});
            res = false;
        }

        // compare all FitsImage data
        res &= compareData(srcImageHdu, dstImageHdu);

        return res;
    }

    private static boolean compareData(final FitsImageHDU srcImageHdu, final FitsImageHDU dstImageHdu) {
        boolean res = true;

        final int sImg = srcImageHdu.getImageCount();
        final int dImg = dstImageHdu.getImageCount();

        if (sImg != dImg) {
            logger.log(Level.INFO, "ERROR:  different number of images {0} <> {1}", new Object[]{sImg, dImg});
            res = false;
        } else {
            logger.info("--------------------------------------------------------------------------------");
            logger.log(Level.INFO, "ImageCount = {0}", sImg);

            final DoubleWrapper maxAbsErrWrapper = new DoubleWrapper();
            final DoubleWrapper maxRelErrWrapper = new DoubleWrapper();

            Object sArray, dArray;

            for (int i = 0; i < sImg; i++) {
                final FitsImage srcFitsImage = srcImageHdu.getFitsImages().get(i);
                final FitsImage dstFitsImage = dstImageHdu.getFitsImages().get(i);

                logger.log(Level.INFO, "Image dims = {0}", sImg);

                sArray = srcFitsImage.getData();
                dArray = dstFitsImage.getData();

                if (PRINT_COL) {
                    logger.log(Level.INFO, "IMAGE {0}\t{1}\n{2}",
                            new Object[]{i, ArrayFuncs.arrayDescription(sArray), FitsUtils.arrayToString(sArray)});
                } else {
                    logger.log(Level.INFO, "IMAGE {0}\t{1}",
                            new Object[]{i, ArrayFuncs.arrayDescription(sArray)});
                }

                if (sArray == dArray) {
                    logger.log(Level.INFO, "IMAGE {0} data (sArray == dArray)", i);
                } else {
                    if (!arrayEquals(sArray, dArray, USE_TOLERANCE, TOLERANCE_FLOAT, TOLERANCE_DOUBLE, maxAbsErrWrapper, maxRelErrWrapper)) {
                        logger.log(Level.INFO, "ERROR:  different values for column[{0}]\nSRC={1}\nDST={2}",
                                new Object[]{i, FitsUtils.arrayToString(sArray), FitsUtils.arrayToString(dArray)});
                        res = false;
                    }
                    if (maxAbsErrWrapper.value > 0d || maxRelErrWrapper.value > 0d) {
                        logger.log(Level.WARNING, "WARN:  Column[{0}]\tMax Absolute Error={1}\tMax Relative Error={2}",
                                new Object[]{i, maxAbsErrWrapper.value, maxRelErrWrapper.value});
                        // reset:
                        maxAbsErrWrapper.value = 0d;
                        maxRelErrWrapper.value = 0d;
                    }
                }
            }
        }
        return res;
    }

    public static boolean compareTable(final FitsTable srcTable, final FitsTable dstTable) {

        // Headers :
        boolean res = compareHeader(srcTable, dstTable);

        res &= compareData(srcTable, dstTable);

        return res;
    }

    private static boolean compareHeader(final FitsHDU srcHdu, final FitsHDU dstHdu) {
        boolean res = true;

        final String sExtName = srcHdu.getExtName();
        final String dExtName = dstHdu.getExtName();

        if (sExtName != null && !sExtName.equals(dExtName)) {
            logger.log(Level.INFO, "ERROR:  different extension name {0} <> {1}", new Object[]{sExtName, dExtName});
            res = false;
        } else {
            logger.info("--------------------------------------------------------------------------------");
            logger.log(Level.INFO, "EXTNAME = {0}", sExtName);

            final int sCard = srcHdu.getKeywordsValue().size();
            final int dCard = dstHdu.getKeywordsValue().size();

            if (sCard != dCard) {
                logger.log(Level.INFO, "ERROR:  different number of header card {0} <> {1}", new Object[]{sCard, dCard});
                res = false;
            }
            logger.log(Level.INFO, "KEYWORDS = {0}", sCard);

            Object srcVal, dstVal;
            String key;
            for (KeywordMeta keyword : srcHdu.getKeywordDescCollection()) {
                key = keyword.getName();

                srcVal = srcHdu.getKeywordValue(key);
                dstVal = dstHdu.getKeywordValue(key);

                logger.log(Level.INFO, "KEYWORD {0} = {1}\t// {2}", new Object[]{key, srcVal, keyword.getDescription()});

                if (isChanged(srcVal, dstVal)) {
                    logger.log(Level.INFO, "ERROR:  different value   of header card[{0}] ''{1}'' <> ''{2}''", new Object[]{key, srcVal, dstVal});
                    res = false;
                }
            }
        }
        return res;
    }

    private static boolean isChanged(final Object value1, final Object value2) {
        return (value1 == null && value2 != null) || (value1 != null && value2 == null) || (value1 != null && value2 != null && !value1.equals(value2));
    }

    private static boolean compareData(final FitsTable srcTable, final FitsTable dstTable) {
        boolean res = true;

        final int sCol = srcTable.getColumnsValue().size();
        final int dCol = dstTable.getColumnsValue().size();

        if (sCol != dCol) {
            logger.log(Level.INFO, "ERROR:  different number of columns {0} <> {1}", new Object[]{sCol, dCol});
            res = false;
        } else {
            logger.info("--------------------------------------------------------------------------------");
            logger.log(Level.INFO, "NCOLS = {0}", sCol);

            final int sRow = srcTable.getNbRows();
            final int dRow = dstTable.getNbRows();

            if (sCol != dCol) {
                logger.log(Level.INFO, "ERROR:  different number of rows {0} <> {1}", new Object[]{sRow, dRow});
                res = false;
            } else {
                logger.log(Level.INFO, "NROWS = {0}", sRow);

                final DoubleWrapper maxAbsErrWrapper = new DoubleWrapper();
                final DoubleWrapper maxRelErrWrapper = new DoubleWrapper();

                Object sArray, dArray;
                String key;
                for (ColumnMeta column : srcTable.getColumnDescCollection()) {
                    key = column.getName();

                    sArray = srcTable.getColumnValue(key);
                    dArray = dstTable.getColumnValue(key);

                    if (PRINT_COL) {
                        logger.log(Level.INFO, "COLUMN {0}\t{1}\n{2}",
                                new Object[]{key, ArrayFuncs.arrayDescription(sArray), FitsUtils.arrayToString(sArray)});
                    } else {
                        logger.log(Level.INFO, "COLUMN {0}\t{1}",
                                new Object[]{key, ArrayFuncs.arrayDescription(sArray)});
                    }

                    if (sArray == dArray) {
                        logger.log(Level.INFO, "COLUMN {0} data (sArray == dArray)", key);
                    } else {
                        if (!arrayEquals(sArray, dArray, USE_TOLERANCE, TOLERANCE_FLOAT, TOLERANCE_DOUBLE, maxAbsErrWrapper, maxRelErrWrapper)) {
                            logger.log(Level.INFO, "ERROR:  different values for column[{0}]\nSRC={1}\nDST={2}",
                                    new Object[]{key, FitsUtils.arrayToString(sArray), FitsUtils.arrayToString(dArray)});
                            res = false;
                        }
                        if (maxAbsErrWrapper.value > 0d || maxRelErrWrapper.value > 0d) {
                            logger.log(Level.WARNING, "WARN:  Column[{0}]\tMax Absolute Error={1}\tMax Relative Error={2}",
                                    new Object[]{key, maxAbsErrWrapper.value, maxRelErrWrapper.value});
                            // reset:
                            maxAbsErrWrapper.value = 0d;
                            maxRelErrWrapper.value = 0d;
                        }
                    }
                }
            }
        }
        return res;
    }

    /** Are two objects equal?  Arrays have the standard object equals
     *  method which only returns true if the two object are the same.
     *  This method returns true if every element of the arrays match.
     *  The inputs may be of any dimensionality.  The dimensionality
     *  and dimensions of the arrays must match as well as any elements.
     *  If the elements are non-primitive. non-array objects, then the
     *  equals method is called for each element.
     *  If both elements are multi-dimensional arrays, then
     *  the method recurses.
     */
    public static boolean arrayEquals(Object x, Object y, boolean useTol, double tolf, double told,
                                      final DoubleWrapper maxAbsErrWrapper, final DoubleWrapper maxRelErrWrapper) {

        // Handle the special cases first.
        // We treat null == null so that two object arrays
        // can match if they have matching null elements.
        if (x == null && y == null) {
            return true;
        }

        if (x == null || y == null) {
            return false;
        }

        Class<?> xClass = x.getClass();
        Class<?> yClass = y.getClass();

        if (xClass != yClass) {
            return false;
        }

        if (!xClass.isArray()) {
            return x.equals(y);

        } else {
            if (xClass.equals(int[].class)) {
                return Arrays.equals((int[]) x, (int[]) y);

            } else if (xClass.equals(double[].class)) {
                if (!useTol) {
                    return Arrays.equals((double[]) x, (double[]) y);
                } else {
                    return doubleArrayEquals((double[]) x, (double[]) y, told, maxAbsErrWrapper, maxRelErrWrapper);
                }

            } else if (xClass.equals(long[].class)) {
                return Arrays.equals((long[]) x, (long[]) y);

            } else if (xClass.equals(float[].class)) {
                if (!useTol) {
                    return Arrays.equals((float[]) x, (float[]) y);
                } else {
                    return floatArrayEquals((float[]) x, (float[]) y, tolf, maxAbsErrWrapper, maxRelErrWrapper);
                }

            } else if (xClass.equals(byte[].class)) {
                return Arrays.equals((byte[]) x, (byte[]) y);

            } else if (xClass.equals(short[].class)) {
                return Arrays.equals((short[]) x, (short[]) y);

            } else if (xClass.equals(char[].class)) {
                return Arrays.equals((char[]) x, (char[]) y);

            } else if (xClass.equals(boolean[].class)) {
                return Arrays.equals((boolean[]) x, (boolean[]) y);

            } else {
                // Non-primitive and multidimensional arrays can be
                // cast to Object[]
                Object[] xo = (Object[]) x;
                Object[] yo = (Object[]) y;
                if (xo.length != yo.length) {
                    return false;
                }
                boolean res = true;
                for (int i = 0; i < xo.length; i++) {
                    res &= arrayEquals(xo[i], yo[i], useTol, tolf, told, maxAbsErrWrapper, maxRelErrWrapper);
                }

                return res;
            }
        }
    }

    /** Compare two double arrays using a given tolerance */
    public static boolean doubleArrayEquals(final double[] x, final double[] y, final double tol,
                                            final DoubleWrapper maxAbsErrWrapper, final DoubleWrapper maxRelErrWrapper) {
        boolean res = true;
        double maxAbsErr = 0d;
        double maxRelErr = 0d;
        double absErr, relErr;

        for (int i = 0; i < x.length; i++) {
            // Test NaN:
            if (Double.isNaN(x[i]) || Double.isNaN(y[i])) {
                res &= (Double.isNaN(x[i]) && Double.isNaN(y[i]));
            } else {
                if (x[i] == 0) {
                    res &= (y[i] == 0);
                } else {
                    absErr = Math.abs(y[i] - x[i]);
                    relErr = absErr / Math.abs(Math.max(x[i], y[i]));

                    if (absErr > maxAbsErr) {
                        maxAbsErr = absErr;
                    }
                    if (relErr > maxRelErr) {
                        maxRelErr = relErr;
                        /*                    
                         if (SHOW_MAX_REL_ERROR) {
                         logger.info("doubleArrayEquals : relative error = " + relErr + " for x=" + x[i] + " vs y=" + y[i]);
                         }
                         */
                    }

                    res &= (Math.min(absErr, relErr) <= tol);
                }
            }
        }
        if (!res) {
            if (maxAbsErr > maxAbsErrWrapper.value) {
                maxAbsErrWrapper.value = maxAbsErr;
            }
            if (maxRelErr > maxRelErrWrapper.value) {
                maxRelErrWrapper.value = maxRelErr;
            }
        }
        return res;
    }

    /** Compare two float arrays using a given tolerance */
    public static boolean floatArrayEquals(final float[] x, final float[] y, final double tol,
                                           final DoubleWrapper maxAbsErrWrapper, final DoubleWrapper maxRelErrWrapper) {
        boolean res = true;
        double maxAbsErr = 0d;
        double maxRelErr = 0d;
        double absErr, relErr;

        for (int i = 0; i < x.length; i++) {
            // Test NaN:
            if (Float.isNaN(x[i]) || Float.isNaN(y[i])) {
                res &= (Float.isNaN(x[i]) && Float.isNaN(y[i]));
            } else {
                if (x[i] == 0) {
                    res &= (y[i] == 0);
                } else {
                    absErr = Math.abs(y[i] - x[i]);
                    relErr = absErr / Math.abs(Math.max(x[i], y[i]));

                    if (absErr > maxAbsErr) {
                        maxAbsErr = absErr;
                    }
                    if (relErr > maxRelErr) {
                        maxRelErr = relErr;
                        /*                    
                         if (SHOW_MAX_REL_ERROR) {
                         logger.info("floatArrayEquals : relative error = " + relErr + " for x=" + x[i] + " vs y=" + y[i]);
                         }
                         * */
                    }

                    res &= (Math.min(absErr, relErr) <= tol);
                }
            }
        }
        if (!res) {
            if (maxAbsErr > maxAbsErrWrapper.value) {
                maxAbsErrWrapper.value = maxAbsErr;
            }
            if (maxRelErr > maxRelErrWrapper.value) {
                maxRelErrWrapper.value = maxRelErr;
            }
        }
        return res;
    }

    private static final class DoubleWrapper {

        /** wrapped double value */
        double value = 0d;

        @Override
        public String toString() {
            return Double.toString(this.value);
        }
    }
}
