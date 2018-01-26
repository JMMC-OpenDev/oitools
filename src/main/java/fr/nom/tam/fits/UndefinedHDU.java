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

import fr.nom.tam.util.ArrayFuncs;

/** Holder for unknown data types. */
public final class UndefinedHDU
        extends BasicHDU {

    /** Build an image HDU using the supplied data.
     * @param h the header for this HDU
     * @param d the data used to build the image.
     * @exception FitsException if there was a problem with the data.
     */
    public UndefinedHDU(Header h, Data d)
            throws FitsException {
        myData = d;
        myHeader = h;

    }

    /* Check if we can find the length of the data for this
     * header.
     * @return <CODE>true</CODE> if this HDU has a valid header.
     */
    public static boolean isHeader(Header hdr) {
        if (hdr.getStringValue("XTENSION") != null
                && hdr.getIntValue("NAXIS", -1) >= 0) {
            return true;
        }
        return false;
    }

    /** Check if we can use the following object as
     *  in an Undefined FITS block.  We allow this
     *  so long as computeLSize can get a size.  Note
     *  that computeLSize may be wrong!
     *  @param o    The Object being tested.
     */
    public static boolean isData(Object o) {
        return ArrayFuncs.computeLSize(o) > 0;
    }

    /** Create a Data object to correspond to the header description.
     * @return An unfilled Data object which can be used to read
     *         in the data for this HDU.
     * @exception FitsException if the image extension could not be created.
     */
    public Data manufactureData()
            throws FitsException {
        return manufactureData(myHeader);
    }

    public static Data manufactureData(Header hdr)
            throws FitsException {
        return new UndefinedData(hdr);
    }

    /** Create a  header that describes the given
     * image data.
     * @param d The image to be described.
     * @exception FitsException if the object does not contain
     *		valid image data.
     */
    public static Header manufactureHeader(Data d)
            throws FitsException {

        Header h = new Header();
        d.fillHeader(h);

        return h;
    }

    /** Encapsulate an object as an ImageHDU. */
    public static Data encapsulate(Object o) throws FitsException {
        return new UndefinedData(o);
    }

    /** Print out some information about this HDU.
     */
    public void info() {

        System.out.println("  Unhandled/Undefined/Unknown Type");
        // LAURENT : use getTrimmedStringValue instead of getStringValue :
        System.out.println("  XTENSION=" + myHeader.getTrimmedStringValue("XTENSION"));
        System.out.println("  Apparent size:" + myData.getTrueSize());
    }
}
