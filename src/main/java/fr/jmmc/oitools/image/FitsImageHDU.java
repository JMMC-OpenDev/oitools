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

import fr.jmmc.oitools.fits.FitsHDU;
import fr.jmmc.oitools.fits.FitsHeaderCard;
import static fr.jmmc.oitools.meta.CellMeta.NO_STR_VALUES;
import fr.jmmc.oitools.meta.KeywordMeta;
import fr.jmmc.oitools.meta.Types;
import fr.jmmc.oitools.model.ModelVisitor;
import java.util.LinkedList;
import java.util.List;

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
        return toString(true);
    }

    /**
     * Returns a string representation of this HDU
     * @param dumpHeaderCards true to dump also header cards
     * @return a string representation of this HDU
     */
    public final String toString(final boolean dumpHeaderCards) {
        return idToString() + "[" + getImageCount() + ']'
                + ((dumpHeaderCards) ? "{\n" + getHeaderCardsAsString("\n") + '}' : "")
                + '\n' + getFitsImages();
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
