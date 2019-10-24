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

/**
 * This interface contains useful constants of the IMAGE-OI
 * @author mella
 */
public interface ImageOiConstants {

    /* IMAGE-OI standard (WIP) */
    /** IMAGE-OI_INPUT_PARAM : extension name for IMAGE-OI parameters  */
    public final static String EXTNAME_IMAGE_OI_INPUT_PARAM = "IMAGE-OI INPUT PARAM";
    /** IMAGE-OI_OUTPUT_PARAM : extension name for IMAGE-OI parameters  */
    public final static String EXTNAME_IMAGE_OI_OUTPUT_PARAM = "IMAGE-OI OUTPUT PARAM";

    // Data Selection keywords
    /** TARGET keyword */
    public final static String KEYWORD_TARGET = "TARGET";
    /** WAVE_MIN keyword */
    public final static String KEYWORD_WAVE_MIN = "WAVE_MIN";
    /** WAVE_MAX keyword */
    public final static String KEYWORD_WAVE_MAX = "WAVE_MAX";
    /** USE_VIS keyword */
    public final static String KEYWORD_USE_VIS = "USE_VIS";
    /** USE_VIS2 keyword */
    public final static String KEYWORD_USE_VIS2 = "USE_VIS2";
    /** USE_T3 keyword */
    public final static String KEYWORD_USE_T3 = "USE_T3";

    // Algorithm setting keywords
    /** INIT_IMG keyword */
    public final static String KEYWORD_INIT_IMG = "INIT_IMG";
    /** MAXITER keyword */
    public final static String KEYWORD_MAXITER = "MAXITER";
    /** RGL_NAME keyword */
    public final static String KEYWORD_RGL_NAME = "RGL_NAME";
    /** AUTO_WGT keyword */
    public final static String KEYWORD_AUTO_WGT = "AUTO_WGT";
    /** RGL_WGT keyword */
    public final static String KEYWORD_RGL_WGT = "RGL_WGT";
    /** FLUX keyword */
    public final static String KEYWORD_FLUX = "FLUX";
    /** FLUXERR keyword */
    public final static String KEYWORD_FLUXERR = "FLUXERR";
    /** RGL_PRIO keyword */
    public final static String KEYWORD_RGL_PRIO = "RGL_PRIO";

    // Image parameters keywords
    /** HDUNAME : keyword */
    public final static String KEYWORD_HDUNAME = "HDUNAME";
    /** HDUNAME : keyword */
    public final static String KEYWORD_DESCRIPTION_HDUNAME = "Unique name for the image within the FITS file";

    // Algorithm results keywords
    /** LAST_IMG keyword */
    public final static String KEYWORD_LAST_IMG = "LAST_IMG";
    /** NITER keyword */
    public final static String KEYWORD_NITER = "NITER";
    /** CHISQ keyword */
    public final static String KEYWORD_CHISQ = "CHISQ";
}
