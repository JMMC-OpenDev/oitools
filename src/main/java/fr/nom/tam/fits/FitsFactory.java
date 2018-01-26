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

/** This class contains the code which
 *  associates particular FITS types with header
 *  and data configurations.  It comprises
 *  a set of Factory methods which call
 *  appropriate methods in the HDU classes.
 *  If -- God forbid -- a new FITS HDU type were
 *  created, then the XXHDU, XXData classes would
 *  need to be added and this file modified but
 *  no other changes should be needed in the FITS libraries.
 * 
 */
public final class FitsFactory {

    private static boolean useAsciiTables = true;
    private static boolean useHierarch = false;
    private static boolean checkAsciiStrings = false;
    private static boolean allowTerminalJunk = false;

    /** Indicate whether ASCII tables should be used
     *  where feasible.
     */
    public static void setUseAsciiTables(boolean flag) {
        useAsciiTables = flag;
    }

    /** Get the current status of ASCII table writing */
    static boolean getUseAsciiTables() {
        return useAsciiTables;
    }

    /** Enable/Disable hierarchical keyword processing. */
    public static void setUseHierarch(boolean flag) {
        useHierarch = flag;
    }

    /** Enable/Disable checking of strings values used in tables
     *  to ensure that they are within the range specified by the
     *  FITS standard.  The standard only allows the values 0x20 - 0x7E
     *  with null bytes allowed in one limited context.
     *  Disabled by default.
     */
    public static void setCheckAsciiStrings(boolean flag) {
        checkAsciiStrings = flag;
    }

    /** Get the current status for string checking. */
    static boolean getCheckAsciiStrings() {
        return checkAsciiStrings;
    }

    /** Are we processing HIERARCH style keywords */
    public static boolean getUseHierarch() {
        return useHierarch;
    }

    /** Do we allow junk after a valid FITS file? */
    public static void setAllowTerminalJunk(boolean flag) {
        allowTerminalJunk = flag;
    }

    /** Is terminal junk (i.e., non-FITS data following a valid HDU)
     *  allowed.
     */
    public static boolean getAllowTerminalJunk() {
        return allowTerminalJunk;
    }

    /** Given a Header return an appropriate datum.
     */
    public static Data dataFactory(Header hdr) throws FitsException {

        if (ImageHDU.isHeader(hdr)) {
            Data d = ImageHDU.manufactureData(hdr);
            hdr.afterExtend();  // Fix for positioning error noted by V. Forchi
            return d;
        } else if (RandomGroupsHDU.isHeader(hdr)) {
            return RandomGroupsHDU.manufactureData(hdr);
        } else if (useAsciiTables && AsciiTableHDU.isHeader(hdr)) {
            return AsciiTableHDU.manufactureData(hdr);
        } else if (BinaryTableHDU.isHeader(hdr)) {
            return BinaryTableHDU.manufactureData(hdr);
        } else if (UndefinedHDU.isHeader(hdr)) {
            return UndefinedHDU.manufactureData(hdr);
        } else {
            throw new FitsException("Unrecognizable header in dataFactory");
        }

    }

    /** Given an object, create the appropriate
     *  FITS header to describe it.
     *  @param	o 	The object to be described.
     */
    public static BasicHDU HDUFactory(Object o) throws FitsException {
        Data d;
        Header h;
        if (o instanceof Header) {
            h = (Header) o;
            d = dataFactory(h);

        } else if (ImageHDU.isData(o)) {
            d = ImageHDU.encapsulate(o);
            h = ImageHDU.manufactureHeader(d);
        } else if (RandomGroupsHDU.isData(o)) {
            d = RandomGroupsHDU.encapsulate(o);
            h = RandomGroupsHDU.manufactureHeader(d);
        } else if (useAsciiTables && AsciiTableHDU.isData(o)) {
            d = AsciiTableHDU.encapsulate(o);
            h = AsciiTableHDU.manufactureHeader(d);
        } else if (BinaryTableHDU.isData(o)) {
            d = BinaryTableHDU.encapsulate(o);
            h = BinaryTableHDU.manufactureHeader(d);
        } else if (UndefinedHDU.isData(o)) {
            d = UndefinedHDU.encapsulate(o);
            h = UndefinedHDU.manufactureHeader(d);
        } else {
            throw new FitsException("Invalid data presented to HDUFactory");
        }

        return HDUFactory(h, d);

    }

    /** Given Header and data objects return
     *  the appropriate type of HDU.
     */
    public static BasicHDU HDUFactory(Header hdr, Data d) throws
            FitsException {

        if (d instanceof ImageData) {
            return new ImageHDU(hdr, d);
        } else if (d instanceof RandomGroupsData) {
            return new RandomGroupsHDU(hdr, d);
        } else if (d instanceof AsciiTable) {
            return new AsciiTableHDU(hdr, d);
        } else if (d instanceof BinaryTable) {
            return new BinaryTableHDU(hdr, d);
        } else if (d instanceof UndefinedData) {
            return new UndefinedHDU(hdr, d);
        }

        return null;
    }
}
