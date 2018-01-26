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

import fr.jmmc.oitools.model.DataModel;
import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;

/**
 * Base class for JUnit tests to share utility methods
 *
 * @author bourgesl
 */
public class JUnitBaseTest {

    /* constants */
    /**
     * Logger associated to test classes
     */
    public final static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(JUnitBaseTest.class.getName());

    /** US number format symbols */
    private final static DecimalFormatSymbols US_SYMBOLS = new DecimalFormatSymbols(Locale.US);
    /** number formatter for float values (6 digits) */
    private final static NumberFormat DF_FULL_FLOAT = new DecimalFormat("0.######E0", US_SYMBOLS);
    /** number formatter for double values (15 digits) */
    private final static NumberFormat DF_FULL_DOUBLE = new DecimalFormat("0.###############E0", US_SYMBOLS);

    /**
     * absolute path to test folder to load test resources
     */
    public final static String TEST_DIR = getProjectFolderPath() + "src/test/resources/";

    /**
     * absolute path to test folder to load FITS test resources
     */
    public final static String TEST_DIR_FITS = TEST_DIR + "fits/";

    /**
     * absolute path to test folder to load OIFITS test resources
     */
    public final static String TEST_DIR_OIFITS = TEST_DIR + "oifits/";

    /**
     * absolute path to test folder to load reference files
     */
    public final static String TEST_DIR_REF = TEST_DIR + "ref/";

    /**
     * absolute path to test folder to save test files
     */
    public final static String TEST_DIR_TEST = TEST_DIR + "test/";

    static {
        Locale.setDefault(Locale.US);

        /** fix rounding mode to avoid inconsistencies between JDK 1.6 and JDK 1.8 */
        DF_FULL_FLOAT.setRoundingMode(RoundingMode.FLOOR);
        DF_FULL_DOUBLE.setRoundingMode(RoundingMode.FLOOR);

        /*
        * Enable support for OI_VIS Complex visibility columns
         */
        DataModel.setOiVisComplexSupport(true);
    }

    public JUnitBaseTest() {
        super();
    }

    /**
     * Return the project folder path
     *
     * @return project folder path
     */
    public static String getProjectFolderPath() {
        try {
            String projectFolder = new File(".").getCanonicalFile().getCanonicalPath() + File.separatorChar;

            logger.log(Level.INFO, "project folder = {0}", projectFolder);

            return projectFolder;

        } catch (IOException ioe) {
            throw new RuntimeException("unable to get project folder: ", ioe);
        }
    }

    /**
     * Scan recusively the given folder to locate all fits files
     * @param directory folder to scan
     * @return list of absolute file paths
     */
    public static List<String> getFitsFiles(final File directory) {
        final List<String> listFiles = new ArrayList<String>(10);
        scanDirectory(directory, listFiles);
        // Sort absolute file paths to ensure consistency accross machines:
        Collections.sort(listFiles);
        return listFiles;
    }

    private static void scanDirectory(final File directory, final List<String> listFiles) {
        if (directory.exists() && directory.isDirectory()) {
            for (File f : directory.listFiles()) {
                if (f.isDirectory()) {
                    scanDirectory(f, listFiles);
                } else if (isFitsFile(f)) {
                    listFiles.add(f.getAbsolutePath());
                }
            }
        }
    }

    /**
     * Return true if the given file is a FITS file i.e. its file name ends with 'fits' or 'fits.gz'
     * @param file file to test
     * @return true if the given file is a FITS file
     */
    public static boolean isFitsFile(final File file) {
        return file.isFile() && (file.getName().endsWith("fits") || file.getName().endsWith("fits.gz"));
    }

    /** --- Copied from Arrays class --- */
    /**
     * Returns a string representation of the "deep contents" of the specified
     * array.  If the array contains other arrays as elements, the string
     * representation contains their contents and so on.  This method is
     * designed for converting multidimensional arrays to strings.
     *
     * <p>The string representation consists of a list of the array's
     * elements, enclosed in square brackets (<tt>"[]"</tt>).  Adjacent
     * elements are separated by the characters <tt>", "</tt> (a comma
     * followed by a space).  Elements are converted to strings as by
     * <tt>String.valueOf(Object)</tt>, unless they are themselves
     * arrays.
     *
     * <p>If an element <tt>e</tt> is an array of a primitive type, it is
     * converted to a string as by invoking the appropriate overloading of
     * <tt>Arrays.toString(e)</tt>.  If an element <tt>e</tt> is an array of a
     * reference type, it is converted to a string as by invoking
     * this method recursively.
     *
     * <p>To avoid infinite recursion, if the specified array contains itself
     * as an element, or contains an indirect reference to itself through one
     * or more levels of arrays, the self-reference is converted to the string
     * <tt>"[...]"</tt>.  For example, an array containing only a reference
     * to itself would be rendered as <tt>"[[...]]"</tt>.
     *
     * <p>This method returns <tt>"null"</tt> if the specified array
     * is <tt>null</tt>.
     *
     * @param a the array whose string representation to return
     * @return a string representation of <tt>a</tt>
     * @see #toString(Object[])
     * @since 1.5
     */
    public static String deepToString(Object[] a) {
        if (a == null) {
            return "null";
        }
        int bufLen = 20 * a.length;
        if (a.length != 0 && bufLen <= 0) {
            bufLen = Integer.MAX_VALUE;
        }
        StringBuilder buf = new StringBuilder(bufLen);
        deepToString(a, buf, new HashSet<Object[]>());
        return buf.toString();
    }

    private static void deepToString(Object[] a, StringBuilder buf, Set<Object[]> dejaVu) {
        if (a == null) {
            buf.append("null");
            return;
        }
        int iMax = a.length - 1;
        if (iMax == -1) {
            buf.append("[]");
            return;
        }

        dejaVu.add(a);
        buf.append('[');
        for (int i = 0;; i++) {

            Object element = a[i];
            if (element == null) {
                buf.append("null");
            } else {
                Class<?> eClass = element.getClass();

                if (eClass.isArray()) {
                    if (eClass == byte[].class) {
                        buf.append(Arrays.toString((byte[]) element));
                    } else if (eClass == short[].class) {
                        buf.append(Arrays.toString((short[]) element));
                    } else if (eClass == int[].class) {
                        buf.append(Arrays.toString((int[]) element));
                    } else if (eClass == long[].class) {
                        buf.append(Arrays.toString((long[]) element));
                    } else if (eClass == char[].class) {
                        buf.append(Arrays.toString((char[]) element));
                    } else if (eClass == float[].class) {
                        buf.append(toString((float[]) element)); // FIXED FORMAT
                    } else if (eClass == double[].class) {
                        buf.append(toString((double[]) element)); // FIXED FORMAT
                    } else if (eClass == boolean[].class) {
                        buf.append(Arrays.toString((boolean[]) element));
                    } else { // element is an array of object references
                        if (dejaVu.contains(element)) {
                            buf.append("[...]");
                        } else {
                            deepToString((Object[]) element, buf, dejaVu);
                        }
                    }
                } else {  // element is non-null and not an array
                    buf.append(element.toString());
                }
            }
            if (i == iMax) {
                break;
            }
            buf.append(", ");
        }
        buf.append(']');
        dejaVu.remove(a);
    }

    /**
     * Returns a string representation of the contents of the specified array.
     * The string representation consists of a list of the array's elements,
     * enclosed in square brackets (<tt>"[]"</tt>).  Adjacent elements are
     * separated by the characters <tt>", "</tt> (a comma followed by a
     * space).  Elements are converted to strings as by
     * <tt>String.valueOf(float)</tt>.  Returns <tt>"null"</tt> if <tt>a</tt>
     * is <tt>null</tt>.
     *
     * @param a the array whose string representation to return
     * @return a string representation of <tt>a</tt>
     * @since 1.5
     */
    public static String toString(float[] a) {
        if (a == null) {
            return "null";
        }

        int iMax = a.length - 1;
        if (iMax == -1) {
            return "[]";
        }

        StringBuilder b = new StringBuilder();
        b.append('[');
        for (int i = 0;; i++) {
            b.append(format(a[i]));
            if (i == iMax) {
                return b.append(']').toString();
            }
            b.append(", ");
        }
    }

    /**
     * Returns a string representation of the contents of the specified array.
     * The string representation consists of a list of the array's elements,
     * enclosed in square brackets (<tt>"[]"</tt>).  Adjacent elements are
     * separated by the characters <tt>", "</tt> (a comma followed by a
     * space).  Elements are converted to strings as by
     * <tt>String.valueOf(double)</tt>.  Returns <tt>"null"</tt> if <tt>a</tt>
     * is <tt>null</tt>.
     *
     * @param a the array whose string representation to return
     * @return a string representation of <tt>a</tt>
     * @since 1.5
     */
    public static String toString(double[] a) {
        if (a == null) {
            return "null";
        }
        int iMax = a.length - 1;
        if (iMax == -1) {
            return "[]";
        }

        StringBuilder b = new StringBuilder();
        b.append('[');
        for (int i = 0;; i++) {
            b.append(format(a[i]));
            if (i == iMax) {
                return b.append(']').toString();
            }
            b.append(", ");
        }
    }

    /**
     * Format the given number using the beautifier formatter
     * @param value any float or double value
     * @return string representation
     */
    private static String format(final float value) {
        final float v = (value >= 0f) ? value : -value;
        if (v == 0f) {
            return "0";
        }
        if (Float.isNaN(value)) {
            return "NaN";
        }
        synchronized (DF_FULL_FLOAT) {
            return trimTrailingZeros(DF_FULL_FLOAT.format(value));
        }
    }

    /**
     * Format the given number using the beautifier formatter
     * @param value any float or double value
     * @return string representation
     */
    private static String format(final double value) {
        final double v = (value >= 0d) ? value : -value;
        if (v == 0d) {
            return "0";
        }
        if (Double.isNaN(value)) {
            return "NaN";
        }
        synchronized (DF_FULL_DOUBLE) {
            return trimTrailingZeros(DF_FULL_DOUBLE.format(value));
        }
    }
    
    private static String trimTrailingZeros(final String number) {
        if(number.contains(".")) {
            final int pos = number.lastIndexOf('E');
            if (pos != -1) {
                String num = number.substring(0, pos);
                final String exp = number.substring(pos, number.length());

                if (num.endsWith("0")) {
                    num = num.replaceAll("\\.?0*$", "");
                    return num + exp;
                }
            }
        }
        return number;
    }
}
