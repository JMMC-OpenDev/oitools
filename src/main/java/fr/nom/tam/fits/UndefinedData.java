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
package fr.nom.tam.fits;

import fr.nom.tam.util.ArrayDataInput;
import fr.nom.tam.util.ArrayDataOutput;
import fr.nom.tam.util.ArrayFuncs;
import java.io.EOFException;
import java.io.IOException;
import java.util.RandomAccess;

/** This class provides a simple holder for data which is
 * not handled by other classes.
 */
public final class UndefinedData extends Data {

    /** The size of the data */
    long byteSize;
    byte[] data;

    public UndefinedData(Header h) throws FitsException {

        /** Just get a byte buffer to hold the data.
         */
        // Bug fix by Vincenzo Forzi.
        int naxis = h.getIntValue("NAXIS");

        int size = naxis > 0 ? 1 : 0;
        for (int i = 0; i < naxis; i += 1) {
            size *= h.getIntValue("NAXIS" + (i + 1));
        }
        size += h.getIntValue("PCOUNT");
        if (h.getIntValue("GCOUNT") > 1) {
            size *= h.getIntValue("GCOUNT");
        }
        size *= Math.abs(h.getIntValue("BITPIX") / 8);

        data = new byte[size];
        byteSize = size;
    }

    /** Create an UndefinedData object using the specified object.
     */
    public UndefinedData(Object x) {

        byteSize = ArrayFuncs.computeLSize(x);
        data = new byte[(int) byteSize];
    }

    /** Fill header with keywords that describe data.
     * @param head The FITS header
     */
    protected void fillHeader(Header head) {

        try {
            head.setXtension("UNKNOWN");
            head.setBitpix(8);
            head.setNaxes(1);
            head.addValue("NAXIS1", byteSize, "ntf::undefineddata:naxis1:1");
            head.addValue("PCOUNT", 0, "ntf::undefineddata:pcount:1");
            head.addValue("GCOUNT", 1, "ntf::undefineddata:gcount:1");
            head.addValue("EXTEND", true, "ntf::undefineddata:extend:1");  // Just in case!
        } catch (HeaderCardException e) {
            System.err.println("Unable to create unknown header:" + e);
        }

    }

    public void read(ArrayDataInput i) throws FitsException {
        setFileOffset(i);

        if (i instanceof RandomAccess) {
            try {
                i.skipBytes(byteSize);
            } catch (IOException e) {
                throw new FitsException("Unable to skip over data:" + e);
            }

        } else {
            try {
                i.readFully(data);
            } catch (IOException e) {
                throw new FitsException("Unable to read unknown data:" + e);
            }

        }

        int pad = FitsUtil.padding(getTrueSize());
        try {
            i.skipBytes(pad);
        } catch (EOFException e) {
            throw new PaddingException("EOF skipping padding in undefined data", this);
        } catch (IOException e) {
            throw new FitsException("Error skipping padding in undefined data");
        }
    }

    public void write(ArrayDataOutput o) throws FitsException {

        if (data == null) {
            getData();
        }

        if (data == null) {
            throw new FitsException("Null unknown data");
        }

        try {
            o.write(data);
        } catch (IOException e) {
            throw new FitsException("IO Error on unknown data write" + e);
        }

        FitsUtil.pad(o, getTrueSize());
    }

    /** Get the size in bytes of the data */
    protected long getTrueSize() {
        return byteSize;
    }

    /** Return the actual data.
     *  Note that this may return a null when
     *  the data is not readable.  It might be better
     *  to throw a FitsException, but this is
     *  a very commonly called method and we prefered
     *  not to change how users must invoke it.
     */
    public Object getData() {

        if (data == null) {

            try {
                FitsUtil.reposition(input, fileOffset);
                input.read(data);
            } catch (Exception e) {
                return null;
            }
        }

        return data;
    }
}
