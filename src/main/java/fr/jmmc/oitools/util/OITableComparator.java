/* 
 * Copyright (C) 2020 CNRS - JMMC project ( http://www.jmmc.fr )
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
package fr.jmmc.oitools.util;

import fr.jmmc.oitools.fits.FitsConstants;
import fr.jmmc.oitools.fits.FitsUtils;
import fr.jmmc.oitools.meta.ColumnMeta;
import fr.jmmc.oitools.meta.KeywordMeta;
import fr.jmmc.oitools.model.OITable;
import fr.nom.tam.util.ArrayFuncs;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

/**
 *
 * @author bourgesl
 */
public final class OITableComparator {

    /** Logger associated to test classes */
    private final static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(OITableComparator.class.getName());

    /** log level to use */
    private final static Level LEVEL = Level.FINE;
    /** flag to dump keyword / column content */
    private final static boolean DO_PRINT_COLS = false;
    /** keyword names to ignore in comparison */
    private final List<String> IGNORE_KEYWORDS = Arrays.asList(new String[]{FitsConstants.KEYWORD_EXT_VER});

    /** strict comparator */
    public final static OITableComparator STRICT_COMPARATOR = new OITableComparator();

    /* members */
    /** flag to use tolerance when comparing floating numbers */
    private final boolean useTolerance;
    /* tolerance on float values */
    private final double tolf;
    /* tolerance on double values */
    private final double told;

    /**
     * Strict comparator (no tolerance)
     */
    private OITableComparator() {
        this(false, Double.NaN, Double.NaN);
    }

    /**
     * Strict comparator (no tolerance)
     */
    private OITableComparator(final boolean useTolerance, final double tolf, final double told) {
        this.useTolerance = useTolerance;
        this.tolf = tolf;
        this.told = told;
    }

    public boolean compareTable(final OITable srcTable, final OITable destTable) {
        final boolean doLog = logger.isLoggable(LEVEL);
        // Headers :
        boolean res = compareHeader(srcTable, destTable, doLog);
        if (res) {
            res = compareData(srcTable, destTable, doLog);
        }
        return res;
    }

    private boolean compareHeader(final OITable srcTable, final OITable destTable, final boolean doLog) {
        boolean res = true;

        final String sExtName = srcTable.getExtName();
        final String dExtName = destTable.getExtName();

        if (sExtName != null && !sExtName.equals(dExtName)) {
            if (doLog) {
                logger.log(LEVEL, "ERROR:  different extension name {0} <> {1}", new Object[]{sExtName, dExtName});
            }
            res = false;
        } else {
            if (doLog) {
                logger.log(LEVEL, "--------------------------------------------------------------------------------");
                logger.log(LEVEL, "EXTNAME = {0}", sExtName);
            }

            Object srcVal, destVal;
            String key;
            for (KeywordMeta keyword : srcTable.getKeywordDescCollection()) {
                key = keyword.getName();

                // skip optional ? no
                // but skip keyword EXT_VER (not always set):
                if (IGNORE_KEYWORDS.contains(key)) {
                    continue;
                }

                srcVal = srcTable.getKeywordValue(key);
                destVal = destTable.getKeywordValue(key);

                if (doLog) {
                    logger.log(LEVEL, "KEYWORD {0} = {1}\t// {2}", new Object[]{key, srcVal, keyword.getDescription()});
                }

                if (isChanged(srcVal, destVal)) {
                    if (doLog) {
                        logger.log(LEVEL, "ERROR:  different value   of header card[{0}] ''{1}'' <> ''{2}''", new Object[]{key, srcVal, destVal});
                    }
                    res = false;
                }
                if (!res && !doLog) {
                    // shortcut
                    break;
                }
            }
        }
        return res;
    }

    private boolean isChanged(final Object value1, final Object value2) {
        return ((value1 == null) && (value2 != null)) || ((value1 != null) && (value2 == null)) || ((value1 != null) && (value2 != null) && !value1.equals(value2));
    }

    private boolean compareData(final OITable srcTable, final OITable destTable, final boolean doLog) {
        boolean res = true;

        final int sCol = srcTable.getColumnsValue().size();
        final int dCol = destTable.getColumnsValue().size();

        if (sCol != dCol) {
            if (doLog) {
                logger.log(LEVEL, "ERROR:  different number of columns {0} <> {1}", new Object[]{sCol, dCol});
            }
            res = false;
        } else {
            if (doLog) {
                logger.log(LEVEL, "--------------------------------------------------------------------------------");
                logger.log(LEVEL, "NCOLS = {0}", sCol);
            }

            final int sRow = srcTable.getNbRows();
            final int dRow = destTable.getNbRows();

            if (sCol != dCol) {
                if (doLog) {
                    logger.log(LEVEL, "ERROR:  different number of rows {0} <> {1}", new Object[]{sRow, dRow});
                }
                res = false;
            } else {
                if (doLog) {
                    logger.log(LEVEL, "NROWS = {0}", sRow);
                }

                final DoubleWrapper maxAbsErrWrapper = new DoubleWrapper();
                final DoubleWrapper maxRelErrWrapper = new DoubleWrapper();

                Object sArray, dArray;
                String key;
                for (ColumnMeta column : srcTable.getColumnDescCollection()) {
                    key = column.getName();

                    sArray = srcTable.getColumnValue(key);
                    dArray = destTable.getColumnValue(key);

                    if (doLog) {
                        if (DO_PRINT_COLS) {
                            logger.log(LEVEL, "COLUMN {0}\t{1}\n{2}", new Object[]{key, ArrayFuncs.arrayDescription(sArray), FitsUtils.arrayToString(sArray)});
                        } else {
                            logger.log(LEVEL, "COLUMN {0}\t{1}", new Object[]{key, ArrayFuncs.arrayDescription(sArray)});
                        }
                    }

                    if (!arrayEquals(sArray, dArray, useTolerance, tolf, told, maxAbsErrWrapper, maxRelErrWrapper)) {
                        if (doLog) {
                            logger.log(LEVEL, "ERROR:  different values for column[{0}]\nSRC={1}\nDST={2}", new Object[]{key, FitsUtils.arrayToString(sArray), FitsUtils.arrayToString(dArray)});
                        }
                        res = false;
                    }
                    if (maxAbsErrWrapper.value > 0d || maxRelErrWrapper.value > 0d) {
                        if (doLog) {
                            logger.log(Level.WARNING, "WARN:  Column[{0}]\tMax Absolute Error={1}\tMax Relative Error={2}", new Object[]{key, maxAbsErrWrapper.value, maxRelErrWrapper.value});
                        }
                        // reset:
                        maxAbsErrWrapper.value = 0d;
                        maxRelErrWrapper.value = 0d;
                    }
                    if (!res && !doLog) {
                        // shortcut
                        break;
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
    private static boolean arrayEquals(final Object x, final Object y, final boolean useTol, final double tolf, final double told,
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

        final Class<?> xClass = x.getClass();
        final Class<?> yClass = y.getClass();

        if (xClass != yClass) {
            return false;
        }

        if (!xClass.isArray()) {
            return x.equals(y);
        } else {
            if (xClass.equals(int[].class)) {
                return Arrays.equals((int[]) x, (int[]) y);
            } else if (xClass.equals(long[].class)) {
                return Arrays.equals((long[]) x, (long[]) y);
            } else if (xClass.equals(double[].class)) {
                if (useTol) {
                    return doubleArrayEquals((double[]) x, (double[]) y, told, maxAbsErrWrapper, maxRelErrWrapper);
                } else {
                    return Arrays.equals((double[]) x, (double[]) y);
                }
            } else if (xClass.equals(float[].class)) {
                if (useTol) {
                    return floatArrayEquals((float[]) x, (float[]) y, tolf, maxAbsErrWrapper, maxRelErrWrapper);
                } else {
                    return Arrays.equals((float[]) x, (float[]) y);
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
                final Object[] xo = (Object[]) x;
                final Object[] yo = (Object[]) y;
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
    private static boolean doubleArrayEquals(final double[] x, final double[] y, final double tol,
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
    private static boolean floatArrayEquals(final float[] x, final float[] y, final double tol,
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
