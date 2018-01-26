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

/**
 * This exception is thrown if an error is found 
 * reading the padding following a valid FITS HDU.
 * This padding is required by the FITS standard, but
 * some FITS writes forego writing it.  To access such data
 * users can use something like:
 * 
 * <code>
 *     Fits f = new Fits("somefile");
 *     try {
 *          f.read();
 *     } catch (PaddingException e) {
 *          f.addHDU(e.getHDU());
 *     }
 * </code>
 * to ensure that a truncated HDU is included in the FITS object.
 * Generally the FITS file have already added any HDUs prior
 * to the truncatd one.
 */
public class PaddingException extends FitsException {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1;

    /** The HDU where the error happened.
     */
    private BasicHDU truncatedHDU;

    /**
     * When the error is thrown, the data object being
     * read must be supplied.  We initially create a dummy
     * header for this.  If someone is reading the entire
     * HDU, then they can trap the exception and set the header
     * to the appropriate value.
     */
    public PaddingException(Data datum) throws FitsException {
        truncatedHDU = FitsFactory.HDUFactory(datum.getKernel());
        // We want to use the original Data object... so
        truncatedHDU = FitsFactory.HDUFactory(truncatedHDU.getHeader(), datum);
    }

    public PaddingException(String msg, Data datum) throws FitsException {
        super(msg);
        truncatedHDU = FitsFactory.HDUFactory(datum.getKernel());
        truncatedHDU = FitsFactory.HDUFactory(truncatedHDU.getHeader(), datum);
    }

    void updateHeader(Header hdr) throws FitsException {
        truncatedHDU = FitsFactory.HDUFactory(hdr, truncatedHDU.getData());
    }

    public BasicHDU getTruncatedHDU() {
        return truncatedHDU;
    }
}
