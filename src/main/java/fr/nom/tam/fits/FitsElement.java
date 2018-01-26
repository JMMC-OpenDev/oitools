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
/** This inteface describes allows uses to easily perform
 *  basic I/O operations
 *  on a FITS element.
 */
package fr.nom.tam.fits;

import fr.nom.tam.util.ArrayDataInput;
import fr.nom.tam.util.ArrayDataOutput;
import java.io.IOException;

public interface FitsElement {

    /** Read the contents of the element from an input source.
     *  @param in	The input source.
     */
    public void read(ArrayDataInput in) throws FitsException, IOException;

    /** Write the contents of the element to a data sink.
     *  @param out      The data sink.
     */
    public void write(ArrayDataOutput out) throws FitsException, IOException;

    /** Rewrite the contents of the element in place.
     *  The data must have been orignally read from a random
     *  access device, and the size of the element may not have changed.
     */
    public void rewrite() throws FitsException, IOException;

    /** Get the byte at which this element begins.
     *  This is only available if the data is originally read from
     *  a random access medium.
     */
    public long getFileOffset();

    /** Can this element be rewritten? */
    public boolean rewriteable();

    /** The size of this element in bytes */
    public long getSize();

    /** Reset the input stream to point to the beginning of this element
     * @return True if the reset succeeded.
     */
    public boolean reset();
}
