/*
 * This code is part of the Java FITS library developed 1996-2012 by T.A. McGlynn (NASA/GSFC)
 * The code is available in the public domain and may be copied, modified and used
 * by anyone in any fashion for any purpose without restriction. 
 * 
 * No warranty regarding correctness or performance of this code is given or implied.
 * Users may contact the author if they have questions or concerns.
 * 
 * The author would like to thank many who have contributed suggestions, 
 * enhancements and bug fixes including:
 * David Glowacki, R.J. Mathar, Laurent Michel, Guillaume Belanger,
 * Laurent Bourges, Rose Early, Fred Romelfanger, Jorgo Baker, A. Kovacs, V. Forchi, J.C. Segovia,
 * Booth Hartley and Jason Weiss.  
 * I apologize to any contributors whose names may have been inadvertently omitted.
 * 
 *      Tom McGlynn
 */
package fr.nom.tam.util;

/** This interface collects some information about Java primitives.
 */
public interface PrimitiveInfo {

    /** Suffixes used for the classnames for primitive arrays. */
    char[] suffixes = new char[]{'B', 'S', 'C', 'I', 'J', 'F', 'D', 'Z'};
    /** Classes of the primitives. These should be in widening order
     * (char is as always a problem).
     */
    Class<?>[] classes = new Class<?>[]{
        byte.class, short.class, char.class, int.class,
        long.class, float.class, double.class, boolean.class};
    /** Is this a numeric class */
    boolean[] isNumeric = new boolean[]{true, true, true, true, true, true, true, false};
    /** Full names */
    String[] types = new String[]{
        "byte", "short", "char", "int",
        "long", "float", "double", "boolean"
    };
    /** Sizes */
    int[] sizes = new int[]{1, 2, 2, 4, 8, 4, 8, 1};
    /** Index of first element of above arrays referring to a numeric type */
    int FIRST_NUMERIC = 0;
    /** Index of last element of above arrays referring to a numeric type */
    int LAST_NUMERIC = 6;
    int BYTE_INDEX = 0;
    int SHORT_INDEX = 1;
    int CHAR_INDEX = 2;
    int INT_INDEX = 3;
    int LONG_INDEX = 4;
    int FLOAT_INDEX = 5;
    int DOUBLE_INDEX = 6;
    int BOOLEAN_INDEX = 7;
}
