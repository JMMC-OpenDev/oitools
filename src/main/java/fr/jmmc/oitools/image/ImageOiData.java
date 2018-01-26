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

import fr.jmmc.oitools.model.OIFitsFile;

/**
 * Container of usefull attribute to manage IMAGE-OI process (image data, params)
 * @author mellag
 */
public final class ImageOiData {

    /* members */
    /** Main OIFitsFile */
    private final OIFitsFile oifitsFile;

    private final ImageOiInputParam inputParam = new ImageOiInputParam();
    private final ImageOiOutputParam outputParam = new ImageOiOutputParam();

    public ImageOiData(final OIFitsFile oifitsFile) {
        super();
        this.oifitsFile = oifitsFile;
    }

    /**
     * Return the list of Fits image HDUs
     * @return list of Fits image HDUs
     */
    public ImageOiInputParam getInputParam() {
        return inputParam;
    }

    public ImageOiOutputParam getOutputParam() {
        return outputParam;
    }

}
