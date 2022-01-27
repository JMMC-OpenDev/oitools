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
package fr.jmmc.oitools.image;

import fr.jmmc.oitools.fits.ChecksumHelper;
import fr.jmmc.oitools.fits.FitsHDU;
import fr.jmmc.oitools.fits.FitsHeaderCard;
import static fr.jmmc.oitools.meta.CellMeta.NO_STR_VALUES;
import fr.jmmc.oitools.meta.KeywordMeta;
import fr.jmmc.oitools.meta.Types;
import fr.jmmc.oitools.model.ModelVisitor;
import fr.jmmc.oitools.model.OIPrimaryHDU;
import fr.nom.tam.fits.BasicHDU;
import fr.nom.tam.fits.FitsException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

/**
 * This class is a container (HDU) for Fits Image (single or Fits cube)
 * @author bourgesl, mellag
 */
public class FitsImageHDU extends FitsHDU {

    /**
     * optional HDUNAME keyword descriptor for IMAGE-OI
     */
    private final static KeywordMeta KEYWORD_HDUNAME = new KeywordMeta(ImageOiConstants.KEYWORD_HDUNAME,
            "Unique name for the image within the FITS file", Types.TYPE_CHAR, true, NO_STR_VALUES);
    /* members */
    /** CRC checksum of the complete HDU */
    private long checksum = 0l;
    /** Storage of fits image references */
    private final List<FitsImage> fitsImages = new LinkedList<FitsImage>();

    /**
     * Public FitsImageHDU class constructor
     */
    public FitsImageHDU() {
        super();

        // HDUNAME keyword definition (optional)
        addKeywordMeta(KEYWORD_HDUNAME);
    }

    /**
     * Factory method to copy the given FitsImageHDU instance 
     * @param hdu hdu to copy
     * @return copied instance
     */
    public static FitsImageHDU copyImageHDU(final FitsImageHDU hdu) {
        if (hdu instanceof OIPrimaryHDU) {
            return new OIPrimaryHDU((OIPrimaryHDU) hdu);
        } else if (hdu instanceof FitsImageHDU) {
            return new FitsImageHDU(hdu);
        }
        return null;
    }

    /**
     * Internal FitsImageHDU class constructor to copy the given hdu (structure only).
     * Use FitsImageHDU.copyImageHDU(src) instead.
     * each FitsImage is copied: their data is shallow-copied, and their FitsImageHDU is updated.
     * @param src hdu to copy
     * @deprecated use FitsImageHDU.copyImageHDU(src) instead
     */
    public FitsImageHDU(final FitsImageHDU src) {
        this();

        this.copyHdu(src);
    }

    /**
     * Copy method for the given hdu (keyword values, header cards and images)
     * @param src hdu to copy
     */
    protected final void copyHdu(final FitsImageHDU src) throws IllegalArgumentException {
        // Copy keyword values and header cards:
        super.copyHdu(src);

        if (src.hasImages()) {
            // copy fitsImages (the data is shallow-copied)
            src.getFitsImages().forEach(
                    fitsImageSrc -> getFitsImages().add(new FitsImage(this, fitsImageSrc))
            );
        }
        // checksum is undefined
    }

    /**
     * Compute the checksum on current images (FITS header + data)
     * Note: costly operation: it creates a new nom.tam.fits.HDU (structure + data copy) to compute checksum.
     */
    public void updateChecksum() {
        if (hasImages()) {
            try {
                // Costly operation:
                final BasicHDU imgHdu = FitsImageWriter.createHDUnit(this);
                // update checksum:
                setChecksum(ChecksumHelper.updateChecksum(imgHdu));
            } catch (FitsException fe) {
                logger.log(Level.SEVERE, "Checksum failure on HDU: {0}", this.toString(true));
                logger.log(Level.SEVERE, "Checksum exception:", fe);
            }
        }
    }

    /* image meta data */
    /**
     * Get the optional HDUNAME keyword for IMAGE-OI model.
     * @return  the hduName value of null
     */
    public final String getHduName() {
        return getKeyword(ImageOiConstants.KEYWORD_HDUNAME);
    }

    /**
     * Define the HDUNAME keyword for IMAGE-OI model.
     *
     * @param hduName value of keyword or null to remove it.
     */
    public final void setHduName(final String hduName) {
        setKeyword(ImageOiConstants.KEYWORD_HDUNAME, hduName);
    }

    /**
     * Return true if this HDU contains images
     * @return true if this HDU contains images
     */
    public final boolean hasImages() {
        return !this.fitsImages.isEmpty();
    }

    /**
     * Return the number of Fits images present in this HDU
     * @return number of Fits images present in this HDU
     */
    public final int getImageCount() {
        return this.fitsImages.size();
    }

    /**
     * Return the list of Fits images
     * @return list of Fits images
     */
    public final List<FitsImage> getFitsImages() {
        return this.fitsImages;
    }

    /**
     * Return true if this HDU corresponds to a Fits cube
     * @return true if this HDU corresponds to a Fits cube
     */
    public final boolean isFitsCube() {
        return getImageCount() > 1;
    }

    /**
     * Return the CRC checksum of the complete HDU
     * @return CRC checksum of the complete HDU
     */
    public final long getChecksum() {
        return checksum;
    }

    /**
     * Define the CRC checksum of the complete HDU
     * @param checksum CRC checksum of the complete HDU
     */
    public final void setChecksum(final long checksum) {
        this.checksum = checksum;
    }

    /**
     * Returns a string representation of this HDU
     * @return a string representation of this HDU
     */
    @Override
    public final String toString() {
        return toString(false);
    }

    /**
     * Returns a string representation of this HDU
     * @param detailled true to dump also header cards
     * @return a string representation of this HDU
     */
    public final String toString(final boolean detailled) {
        return idToString() + "[" + getImageCount() + ']'
                + ((detailled) ? "{\n" + getHeaderAsString("\n") + '}' + '\n' + getFitsImages() : "");
    }

    /**
     * Return an HTML representation of the HDU used by tooltips.
     * @param sb string buffer to use (cleared)
     * @return HTML representation as String
     */
    public final String toHtml(final StringBuffer sb) {
        sb.setLength(0); // clear
        sb.append("<html>");
        sb.append("<b>FitsImageHDU[HDU#").append(getExtNb()).append("][").append(getImageCount()).append(']');
        sb.append("</b><br>Header:<ul>");
        for (FitsHeaderCard h : getHeaderCards()) {
            sb.append("<li>").append(h.toString()).append("</li>");
        }
        sb.append("</ul>");
        sb.append("</html>");
        return sb.toString();
    }

    /**
     * Implements the Visitor pattern
     * @param visitor visitor implementation
     */
    @Override
    public final void accept(final ModelVisitor visitor) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
