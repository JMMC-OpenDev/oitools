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

import java.io.IOException;

public interface ArrayDataInput extends java.io.DataInput {

    /** Read a generic (possibly multidimensional) primitive array.
     * An  Object[] array is also a legal argument if each element
     * of the array is a legal.
     * <p>
     * The ArrayDataInput classes do not support String input since
     * it is unclear how one would read in an Array of strings.
     * @param o   A [multidimensional] primitive (or Object) array.
     * @deprecated See readLArray(Object o).
     */
    public int readArray(Object o) throws IOException;

    /** Read an array. This version works even if the
     * underlying data is more than 2 Gigabytes.
     */
    public long readLArray(Object o) throws IOException;

    /* Read a complete primitive array */
    public int read(byte[] buf) throws IOException;

    public int read(boolean[] buf) throws IOException;

    public int read(short[] buf) throws IOException;

    public int read(char[] buf) throws IOException;

    public int read(int[] buf) throws IOException;

    public int read(long[] buf) throws IOException;

    public int read(float[] buf) throws IOException;

    public int read(double[] buf) throws IOException;

    /* Read a segment of a primitive array. */
    public int read(byte[] buf, int offset, int size) throws IOException;

    public int read(boolean[] buf, int offset, int size) throws IOException;

    public int read(char[] buf, int offset, int size) throws IOException;

    public int read(short[] buf, int offset, int size) throws IOException;

    public int read(int[] buf, int offset, int size) throws IOException;

    public int read(long[] buf, int offset, int size) throws IOException;

    public int read(float[] buf, int offset, int size) throws IOException;

    public int read(double[] buf, int offset, int size) throws IOException;

    /* Skip (forward) in a file */
    public long skip(long distance) throws IOException;

    /* Skip and require that the data be there. */
    public long skipBytes(long toSkip) throws IOException;

    /* Close the file. */
    public void close() throws IOException;
}
