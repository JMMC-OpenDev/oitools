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

import fr.jmmc.oitools.model.OIFitsChecker;
import java.util.logging.Level;

/**
 * Container of usefull attribute to manage IMAGE-OI process (image data, params)
 * @author mellag
 */
public final class ImageOiData {

    public final static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(ImageOiData.class.getName());

    /* members */
    private final ImageOiInputParam inputParam = new ImageOiInputParam();
    private ImageOiOutputParam outputParam = null; // lazily created in getOutputParam

    public ImageOiData() {
        super();
    }

    /**
     * Return the list of Fits image HDUs
     * @return list of Fits image HDUs
     */
    public ImageOiInputParam getInputParam() {
        return inputParam;
    }

    /** 
     * @return outputParam but initialize it if null
     */
    public ImageOiOutputParam getOutputParam() {
        if (outputParam == null) {
            outputParam = new ImageOiOutputParam();
        }
        return outputParam;
    }
    
    /**
     * @return outputParam (can be null)
     */
    public ImageOiOutputParam getExistingOutputParam() {
        return outputParam;
    }
    
    /** nullify outputParam. */
    public void removeOutputParam () {
        this.outputParam = null;
    }

    /*
     * --- Checker -------------------------------------------------------------
     */
    /**
     * Do syntactical analysis of the table
     *
     * @param checker checker component
     */
    public void checkSyntax(final OIFitsChecker checker) {
        logger.log(Level.INFO, "Analysing HDU [{0}]:", inputParam.idToString());
        inputParam.checkSyntax(checker);

        if (outputParam != null) {
            logger.log(Level.INFO, "Analysing HDU [{0}]:", outputParam.idToString());
            outputParam.checkSyntax(checker);
        }
    }
}
