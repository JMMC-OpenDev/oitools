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

/** These packages define the methods which indicate that
 *  an i/o stream may be accessed in arbitrary order.
 *  The method signatures are taken from RandomAccessFile
 *  though that class does not implement this interface.
 */
public interface RandomAccess extends ArrayDataInput {

    /** Move to a specified location in the stream. */
    public void seek(long offsetFromStart) throws java.io.IOException;

    /** Get the current position in the stream */
    public long getFilePointer();
}
