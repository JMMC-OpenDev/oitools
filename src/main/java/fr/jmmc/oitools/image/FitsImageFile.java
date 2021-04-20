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

import fr.jmmc.oitools.model.ModelBase;
import fr.jmmc.oitools.model.ModelVisitor;
import java.util.LinkedList;
import java.util.List;

/**
 * This class represents the data model of a Fits standard file containing one or multiple images.
 * @author bourgesl
 */
public class FitsImageFile extends ModelBase {

    /** Storage of file information filename and absolutefilepath */
    private final FileRef fileRef;
    /** Storage of fits image HDU references */
    private final List<FitsImageHDU> fitsImageHDUs = new LinkedList<FitsImageHDU>();

    /**
     * Public constructor
     */
    public FitsImageFile() {
        this((String) null);
    }

    /**
     * Public constructor
     * @param absoluteFilePath absolute file path
     */
    public FitsImageFile(final String absoluteFilePath) {
        this(new FileRef(absoluteFilePath));
    }

    /**
     * Public constructor
     * @param fileRef file reference
     */
    public FitsImageFile(final FileRef fileRef) {
        super();
        this.fileRef = fileRef;
    }

    /**
     * Implements the Visitor pattern
     * @param visitor visitor implementation
     */
    @Override
    public void accept(final ModelVisitor visitor) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** 
     * Return a short description of FitsImageFile content.
     * @return short description of FitsImageFile content
     */
    @Override
    public String toString() {
        return "FitsImageFile[" + getAbsoluteFilePath() + "](" + getImageHDUCount() + ")\n" + getFitsImageHDUs();
    }

    /*
     * Getter - Setter -----------------------------------------------------------
     */
    /**
     * Get the file info.
     * @return the fileRef
     */
    public final FileRef getFileRef() {
        return fileRef;
    }

    /**
     * Get the name of this FitsImageFile file.
     *  @return a string containing the name of the FitsImageFile file.
     */
    public final String getFileName() {
        return getFileRef().getFileName();
    }

    /**
     * Return the absolute file path
     * @return absolute file path or null if the file does not exist
     */
    public final String getAbsoluteFilePath() {
        return getFileRef().getAbsoluteFilePath();
    }

    /**
     * Define the absolute file path
     * @param absoluteFilePath absolute file path
     */
    public final void setAbsoluteFilePath(final String absoluteFilePath) {
        getFileRef().setAbsoluteFilePath(absoluteFilePath);
    }

    /**
     * Return the number of Fits image HDUs present in this FitsImageFile structure
     * @return number of Fits image HDUs
     */
    public final int getImageHDUCount() {
        return getFitsImageHDUs().size();
    }

    /**
     * Return the list of Fits image HDUs
     * @return list of Fits image HDUs
     */
    public final List<FitsImageHDU> getFitsImageHDUs() {
        return this.fitsImageHDUs;
    }

    /**
     * Get the primary ImageHDU if defined.
     * @return the primary image HDU or null
     */
    public final FitsImageHDU getPrimaryImageHDU() {
        return this.getFitsImageHDUs().isEmpty() ? null : getFitsImageHDUs().get(0);
    }

    /**
     * Define the primary image HDU.
     * @param imageHdu image HDU 
     */
    public final void setPrimaryImageHdu(final FitsImageHDU imageHdu) {
        if (this.getFitsImageHDUs().isEmpty()) {
            getFitsImageHDUs().add(imageHdu);
        } else {
            getFitsImageHDUs().remove(imageHdu);
            getFitsImageHDUs().add(0, imageHdu);
        }
    }

}
