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
package fr.jmmc.oitools.model;

/**
 * This is the base class for OIFitsFile and OITable classes
 * @author bourgesl
 */
public abstract class ModelBase {

    /** Logger associated to model classes */
    protected final static java.util.logging.Logger logger = java.util.logging.Logger.getLogger("fr.jmmc.oitools.model");

    /* constants */
    /** undefined short value = -32768 */
    public final static short UNDEFINED_SHORT = Short.MIN_VALUE;
    /** undefined int value = Integer.MIN_VALUE */
    public final static int UNDEFINED_INT = Integer.MIN_VALUE;
    /** undefined float value = NaN */
    public final static float UNDEFINED_FLOAT = Float.NaN;
    /** undefined float value = NaN */
    public final static double UNDEFINED_DBL = Double.NaN;
    /** undefined String value = "" */
    public final static String UNDEFINED_STRING = "";
    /** "UNDEFINED' String value */
    public static final String UNDEFINED = "UNDEFINED";
    /** empty int array */
    protected final static short[] EMPTY_SHORT_ARRAY = new short[0];
    /** empty float array */
    protected final static float[] EMPTY_FLOAT_ARRAY = new float[0];
    /** empty double array */
    protected final static double[] EMPTY_DBL_ARRAY = new double[0];
    /** empty String array */
    protected final static String[] EMPTY_STRING = new String[0];

    /**
     * Public constructor
     */
    public ModelBase() {
        super();
    }

    /**
     * From Object.equals():
     *
     * @implSpec
     * The {@code equals} method for class {@code Object} implements
     * the most discriminating possible equivalence relation on objects;
     * that is, for any non-null reference values {@code x} and
     * {@code y}, this method returns {@code true} if and only
     * if {@code x} and {@code y} refer to the same object
     * ({@code x == y} has the value {@code true}).
     *
     * In other words, under the reference equality equivalence
     * relation, each equivalence class only has a single element.
     *
     * @apiNote
     * It is generally necessary to override the {@link #hashCode() hashCode}
     * method whenever this method is overridden, so as to maintain the
     * general contract for the {@code hashCode} method, which states
     * that equal objects must have equal hash codes.
     *
     * @param   obj   the reference object with which to compare.
     * @return  {@code true} if this object is the same as the obj
     *          argument; {@code false} otherwise.
     * @see     #hashCode()
     * @see     java.util.HashMap
     *
        @Override
        public final boolean equals(final Object obj) {
            return super.equals(obj);
        }
     */
    /**
     * From Object.hashCode():
     *
     * @implSpec
     * As far as is reasonably practical, the {@code hashCode} method defined
     * by class {@code Object} returns distinct integers for distinct objects.
     *
     * @return  a hash code value for this object.
     * @see     java.lang.Object#equals(java.lang.Object)
     * @see     java.lang.System#identityHashCode
     *
        @Override
        public final int hashCode() {
            return super.hashCode();
        }
     */
    /**
     * Implements the Visitor pattern 
     * @param visitor visitor implementation
     */
    public abstract void accept(final ModelVisitor visitor);

    /**
     * Utility method for <code>equals()</code> methods.
     *
     * @param o1 one object
     * @param o2 another object
     *
     * @return <code>true</code> if they're both <code>null</code> or both equal
     */
    public static boolean areEquals(final Object o1, final Object o2) {
        return !((o1 != o2) && ((o1 == null) || !o1.equals(o2)));
    }

    /**
     * Return true if the given value is the undefined short value (blanking)
     * @param val value to check
     * @return true if the given value is the undefined short value (blanking)
     */
    public static boolean isUndefined(final short val) {
        return (val == UNDEFINED_SHORT);
    }

    /**
     * Convert the int value to a short value
     * 
     * @param iValue int value to be converted
     * @return value short value
     * @throws IllegalArgumentException if the integer value exceeds the value range of shorts
     */
    public static short toShort(final int iValue) throws IllegalArgumentException {
        // downcast
        short value = (short) iValue;
        // upcast to int
        if (value != iValue) {
            throw new IllegalArgumentException("Invalid short value: " + iValue);
        }
        return value;
    }

    /**
     * Returns the HDU id [EXT_NAME#EXT_NB] as string
     *
     * @param extName extension name
     * @param extNb extension number
     * @return HDU id [EXT_NAME # EXT_NB] as string
     */
    public static String getHDUId(final String extName, final int extNb) {
        return getHDUId(new StringBuilder(16), extName, extNb).toString();
    }

    /**
     * Appends the HDU id [EXT_NAME#EXT_NB] in the given buffer
     *
     * @param sb buffer to append into
     * @param extName extension name
     * @param extNb extension number
     * @return given buffer
     */
    public static StringBuilder getHDUId(final StringBuilder sb, final String extName, final int extNb) {
        return sb.append((extName == null || extName.isEmpty()) ? "HDU" : extName).append('#').append(extNb);
    }
}
