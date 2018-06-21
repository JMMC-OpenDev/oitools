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
package nom.tam.fits.test;

import java.lang.reflect.Array;
import java.util.Arrays;

/** This is a package of static functions which perform
 * computations on arrays.  Generally these routines attempt
 * to complete without throwing errors by ignoring data
 * they cannot understand.
 */
public final class ArrayFuncs {

    /** 1-D dimension */
    public final static int[] DIM_1 = new int[]{1};
    /** 2-D dimension */
    public final static int[] DIM_2 = new int[]{2};

    /** Count the number of elements in an array.
     *  @deprecated  May silently underestimate
     *               size if number is > 2 G.
     */
    public static int nElements(Object o) {
        return (int) nLElements(o);
    }

    /** Count the number of elements in an array.
     *  @deprecated  May silently underestimate
     *               size if number is > 2 G.
     */
    public static long nLElements(Object o) {
        if (o == null) {
            return 0;
        }

        String cname = o.getClass().getName();
        if (cname.charAt(1) == '[') {
            int count = 0;
            Object[] oo = (Object[]) o;
            for (int i = 0; i < oo.length; i += 1) {
                count += nLElements(oo[i]);
            }
            return count;

        } else if (cname.charAt(0) == '[') {
            return Array.getLength(o);

        } else {
            return 1;
        }
    }


    /** Copy one array into another.
     * This function copies the contents of one array
     * into a previously allocated array.
     * The arrays must agree in type and size.
     * @param original The array to be copied.
     * @param copy     The array to be copied into.  This
     *                 array must already be fully allocated.
     */
    public static void copyArray(Object original, Object copy) {
        String oname = original.getClass().getName();
        String cname = copy.getClass().getName();

        if (!oname.equals(cname)) {
            return;
        }

        if (oname.charAt(0) != '[') {
            return;
        }

        if (oname.charAt(1) == '[') {
            Object[] x = (Object[]) original;
            Object[] y = (Object[]) copy;
            if (x.length != y.length) {
                return;
            }
            for (int i = 0; i < x.length; i += 1) {
                copyArray(x, y);
            }
        }
        int len = Array.getLength(original);

        System.arraycopy(original, 0, copy, 0, len);
    }

    /** Find the dimensions of an object.
     *
     * This method returns an integer array with the dimensions
     * of the object o which should usually be an array.
     *
     * It returns an array of dimension 0 for scalar objects
     * and it returns -1 for dimension which have not been allocated,
     *  e.g., int[][][] x = new int[100][][]; should return [100,-1,-1].
     *
     * @param o The object to get the dimensions of.
     */
    public static int[] getDimensions(Object o) {
        if (o == null) {
            return null;
        }

        String cname = o.getClass().getName();

        int ndim = 0;

        while (cname.charAt(ndim) == '[') {
            ndim += 1;
        }

        int[] dimens = new int[ndim];

        for (int i = 0; i < ndim; i += 1) {
            dimens[i] = -1;  // So that we can distinguish a null from a 0 length.
        }

        for (int i = 0; i < ndim; i += 1) {
            dimens[i] = java.lang.reflect.Array.getLength(o);
            if (dimens[i] == 0) {
                return dimens;
            }
            if (i != ndim - 1) {
                o = ((Object[]) o)[0];
                if (o == null) {
                    return dimens;
                }
            }
        }
        return dimens;
    }

    /** This routine returns the base array of a multi-dimensional
     *  array.  I.e., a one-d array of whatever the array is composed
     *  of.  Note that arrays are not guaranteed to be rectangular,
     *  so this returns o[0][0]....
     */
    public static Object getBaseArray(Object o) {
        String cname = o.getClass().getName();
        if (cname.charAt(1) == '[') {
            return getBaseArray(((Object[]) o)[0]);
        } else {
            return o;
        }
    }


    /** Create an array and populate it with a test pattern.
     *
     * @param baseType  The base type of the array.  This is expected to
     *                  be a numeric type, but this is not checked.
     * @param dims      The desired dimensions.
     * @return An array object populated with a simple test pattern.
     */
    public static Object generateArray(Class<?> baseType, int[] dims) {
        // Generate an array and populate it with a test pattern of
        // data.
        Object x = ArrayFuncs.newInstance(baseType, dims);
        testPattern(x, (byte) 0);
        return x;
    }

    /** Just create a simple pattern cycling through valid byte values.
     * We use bytes because they can be cast to any other numeric type.
     * @param o      The array in which the test pattern is to be set.
     * @param start  The value for the first element.
     */
    public static byte testPattern(Object o, byte start) {
        int[] dims = getDimensions(o);
        if (dims.length > 1) {
            Object[] oo = (Object[]) o;
            for (int i = 0; i < oo.length; i += 1) {
                start = testPattern(oo[i], start);
            }

        } else if (dims.length == 1) {
            for (int i = 0; i < dims[0]; i += 1) {
                java.lang.reflect.Array.setByte(o, i, start);
                start += 1;
            }
        }
        return start;
    }


    /** Examine the structure of an array in detail.
     * @param o The array to be examined.
     */
    public static void examinePrimitiveArray(Object o) {
        String cname = o.getClass().getName();

        // If we have a two-d array, or if the array is a one-d array
        // of Objects, then recurse over the next dimension.  We handle
        // Object specially because each element could itself be an array.
        if (cname.substring(0, 2).equals("[[")
                || cname.equals("[Ljava.lang.Object;")) {
            System.out.println("[");
            Object[] oo = (Object[]) o;
            for (int i = 0; i < oo.length; i += 1) {
                examinePrimitiveArray(oo[i]);
            }
            System.out.print("]");
        } else if (cname.charAt(0) != '[') {
            System.out.println(cname);
        } else {
            System.out.println("[" + java.lang.reflect.Array.getLength(o) + "]"
                    + cname.substring(1));
        }
    }


    /** This routine does the actually flattening of multi-dimensional
     * arrays.
     * @param input  The input array to be flattened.
     * @param output The flattened array.
     * @param offset The current offset within the output array.
     * @return       The number of elements within the array.
     */
    protected static int doFlatten(Object input, Object output, int offset) {
        String cname = input.getClass().getName();
        if (cname.charAt(0) != '[') {
            throw new RuntimeException("Attempt to flatten non-array");
        }
        int size = Array.getLength(input);

        if (cname.charAt(1) != '[') {
            System.arraycopy(input, 0, output, offset, size);
            return size;
        }
        int total = 0;
        Object[] xx = (Object[]) input;
        for (int i = 0; i < size; i += 1) {
            total += doFlatten(xx[i], output, offset + total);
        }
        return total;
    }


    /** Do the curling of the 1-d to multi-d array.
     * @param input  The 1-d array to be curled.
     * @param output The multi-dimensional array to be filled.
     * @param dimens array of output dimensions.
     * @param index the index in dimens (current level)
     * @param offset The current offset in the input array.
     * @return       The number of elements curled.
     */
    protected static int doCurl(Object input, Object output,
                                int[] dimens, int index, int offset) {

        int len = dimens[index];
        int xindex = index + 1;

        if (xindex == dimens.length) {
            System.arraycopy(input, offset, output, 0, len);
            return len;
        }

        int total = 0;
        Object[] oo = (Object[]) output;
        for (int i = 0; i < len; i += 1) {
            total += doCurl(input, oo[i], dimens, xindex, offset + total);
        }
        return total;
    }

    /** Create an array of a type given by new type with
     * the dimensionality given in array.
     * @param array   A possibly multidimensional array to be converted.
     * @param newType The desired output type.  This should be one of the
     *                class descriptors for primitive numeric data, e.g., double.type.
     */
    public static Object mimicArray(Object array, Class<?> newType) {
        String cname = array.getClass().getName();
        if (cname.charAt(0) != '[') {
            return null;
        }

        int dims = 1;

        while (cname.charAt(dims) == '[') {
            dims += 1;
        }

        Object mimic;

        if (dims > 1) {

            Object[] xarray = (Object[]) array;
            int[] dimens = new int[dims];
            dimens[0] = xarray.length;  // Leave other dimensions at 0.

            mimic = ArrayFuncs.newInstance(newType, dimens);

            Object[] mo = (Object[]) mimic;
            for (int i = 0; i < xarray.length; i += 1) {
                Object temp = mimicArray(xarray[i], newType);
                mo[i] = temp;
            }

        } else {
            mimic = ArrayFuncs.newInstance(newType, Array.getLength(array));
        }

        return mimic;
    }

    /** Allocate an array dynamically. The Array.newInstance method
     *  does not throw an error when there is insufficient memory
     *  and silently returns a null.
     *  @param cl	The class of the array.
     *  @param dim      The dimension of the array.
     *  @return The allocated array.
     *  @throws An OutOfMemoryError if insufficient space is available.
     */
    public static Object newInstance(Class<?> cl, int dim) {
        Object o = Array.newInstance(cl, dim);
        if (o == null) {
            String desc = cl + "[" + dim + "]";
            throw new OutOfMemoryError("Unable to allocate array: " + desc);
        }
        return o;
    }

    /** Allocate an array dynamically. The Array.newInstance method
     *  does not throw an error and silently returns a null.
     *
     *  @param cl	The class of the array.
     *  @param dims     The dimensions of the array.
     *  @return The allocated array.
     *  @throws An OutOfMemoryError if insufficient space is available.
     */
    public static Object newInstance(Class<?> cl, int[] dims) {
        if (dims.length == 0) {
            // Treat a scalar as a 1-d array of length 1
            dims = DIM_1;
        }

        Object o = Array.newInstance(cl, dims);
        if (o == null) {
            String desc = cl + "[";
            String comma = "";
            for (int i = 0; i < dims.length; i += 1) {
                desc += comma + dims[i];
                comma = ",";
            }
            desc += "]";
            throw new OutOfMemoryError("Unable to allocate array: " + desc);
        }
        return o;
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
    public static boolean arrayEquals(Object x, Object y) {
        return arrayEquals(x, y, 0, 0);
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
    public static boolean arrayEquals(Object x, Object y, double tolf, double told) {
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
                if (told == 0) {
                    return Arrays.equals((double[]) x, (double[]) y);
                } else {
                    return doubleArrayEquals((double[]) x, (double[]) y, told);
                }

            } else if (xClass.equals(long[].class)) {
                return Arrays.equals((long[]) x, (long[]) y);

            } else if (xClass.equals(float[].class)) {
                if (tolf == 0) {
                    return Arrays.equals((float[]) x, (float[]) y);
                } else {
                    return floatArrayEquals((float[]) x, (float[]) y, (float) tolf);
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
                for (int i = 0; i < xo.length; i += 1) {
                    if (!arrayEquals(xo[i], yo[i], tolf, told)) {
                        return false;
                    }
                }

                return true;

            }
        }
    }

    /** Compare two double arrays using a given tolerance */
    public static boolean doubleArrayEquals(double[] x, double[] y, double tol) {
        for (int i = 0; i < x.length; i += 1) {
            if (x[i] == 0) {
                return y[i] == 0;
            }
            if (Math.abs((y[i] - x[i]) / x[i]) > tol) {
                return false;
            }
        }
        return true;
    }

    /** Compare two float arrays using a given tolerance */
    public static boolean floatArrayEquals(float[] x, float[] y, float tol) {
        for (int i = 0; i < x.length; i += 1) {
            if (x[i] == 0) {
                return y[i] == 0;
            }
            if (Math.abs((y[i] - x[i]) / x[i]) > tol) {
                return false;
            }
        }
        return true;
    }

    /** Dump an array on the given print steam */
    public static void dumpArray(java.io.PrintStream p, Object arr) {
        // Get the dimensionality and then dump.
        if (arr == null) {
            p.print("null ");
        } else {
            Class nm = arr.getClass();
            if (nm.isArray()) {
                p.print("[");
                for (int i = 0; i < java.lang.reflect.Array.getLength(arr); i += 1) {
                    dumpArray(p, java.lang.reflect.Array.get(arr, i));
                }
                p.print("]\n");
            } else {
                p.print(" " + arr.toString() + " ");
            }
        }
    }

    /** Reverse an integer array.  This can be especially
     *  useful when dealing with an array of indices in FITS order
     *  that you wish to have in Java order.
     */
    public static int[] reverseIndices(int[] indices) {
        int[] result = indices.clone();
        int len = indices.length;
        for (int i = 0; i < indices.length; i += 1) {
            result[len - i - 1] = indices[i];
        }
        return result;
    }
}
