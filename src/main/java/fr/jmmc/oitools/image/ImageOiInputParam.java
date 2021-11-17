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

import fr.jmmc.oitools.meta.KeywordMeta;
import fr.jmmc.oitools.meta.Types;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class is a container for IMAGE-OI INPUT PARAM.
 * https://github.com/emmt/OI-Imaging-JRA
 * It can be associated to an OIFitsFile to produce IMAGE-OI compliant files.
 *
 * @author mellag
 */
public final class ImageOiInputParam extends ImageOiParam {

    // Define Data selection keywords
    private final static KeywordMeta KEYWORD_TARGET = new KeywordMeta(ImageOiConstants.KEYWORD_TARGET, "Identifier of the target object to reconstruct", Types.TYPE_CHAR);
    private final static KeywordMeta KEYWORD_WAVE_MIN = new KeywordMeta(ImageOiConstants.KEYWORD_WAVE_MIN, "Minimum wavelength to select (in meters)", Types.TYPE_DBL);
    private final static KeywordMeta KEYWORD_WAVE_MAX = new KeywordMeta(ImageOiConstants.KEYWORD_WAVE_MAX, "Maximum wavelength to select (in meters)", Types.TYPE_DBL);
// TODO: value can be: ’NONE’, ’ALL’, ’AMP’ or ’PHI’ (string)
    private final static KeywordMeta KEYWORD_USE_VIS = new KeywordMeta(ImageOiConstants.KEYWORD_USE_VIS, "Use complex visibility data if any", Types.TYPE_LOGICAL);
    private final static KeywordMeta KEYWORD_USE_VIS2 = new KeywordMeta(ImageOiConstants.KEYWORD_USE_VIS2, "Use squared visibility data if any", Types.TYPE_LOGICAL);
// TODO: value can be: ’NONE’, ’ALL’, ’AMP’ or ’PHI’ (string)
    private final static KeywordMeta KEYWORD_USE_T3 = new KeywordMeta(ImageOiConstants.KEYWORD_USE_T3, "Use triple product data if any", Types.TYPE_LOGICAL);

    // Define Algorithm settings keywords
    // TODO init-img keyword could be checked like OIDATA.INSNAME or OIDATA.ARRNAME to be sure that one or more HDUs are present with this name.
    private final static KeywordMeta KEYWORD_INIT_IMG = new KeywordMeta(ImageOiConstants.KEYWORD_INIT_IMG, "Identifier of the initial image", Types.TYPE_CHAR);
    private final static KeywordMeta KEYWORD_MAXITER = new KeywordMeta(ImageOiConstants.KEYWORD_MAXITER, "Maximum number of iterations to run", Types.TYPE_INT);
    private final static KeywordMeta KEYWORD_RGL_NAME = new KeywordMeta(ImageOiConstants.KEYWORD_RGL_NAME, "Name of the regularization method", Types.TYPE_CHAR);
    private final static KeywordMeta KEYWORD_RGL_WGT = new KeywordMeta(ImageOiConstants.KEYWORD_RGL_WGT, "Weight of the regularization", Types.TYPE_DBL);
    private final static KeywordMeta KEYWORD_AUTO_WGT = new KeywordMeta(ImageOiConstants.KEYWORD_AUTO_WGT, "Automatic regularization weight", Types.TYPE_LOGICAL);
    private final static KeywordMeta KEYWORD_FLUX = new KeywordMeta(ImageOiConstants.KEYWORD_FLUX, "Total flux (sum of pixels)", Types.TYPE_DBL);
    private final static KeywordMeta KEYWORD_FLUXERR = new KeywordMeta(ImageOiConstants.KEYWORD_FLUXERR, " Assumed standard deviation for the total flux ", Types.TYPE_DBL);
    private final static KeywordMeta KEYWORD_RGL_PRIO = new KeywordMeta(ImageOiConstants.KEYWORD_RGL_PRIO, "Identifier of the HDU with the prior image", Types.TYPE_CHAR);

    public final static double DEF_KEYWORD_WAVE_MIN = -1.0;
    public final static double DEF_KEYWORD_WAVE_MAX = -1.0;
    public final static int DEF_KEYWORD_MAXITER = 50;

    public final static boolean DEF_KEYWORD_AUTO_WGT = true;
    public final static double DEF_KEYWORD_RGL_WGT = 0.0;
    public final static double DEF_KEYWORD_FLUX = 1.0;
    public final static double DEF_KEYWORD_FLUXERR = 0.01; // 1% error on VIS2

    private final static Map<String, KeywordMeta> IMAGE_OI_INPUT_STD_KEYWORDS;

    static {
        IMAGE_OI_INPUT_STD_KEYWORDS = new LinkedHashMap<>();
        Arrays.asList(
                KEYWORD_TARGET, KEYWORD_WAVE_MIN, KEYWORD_WAVE_MAX,
                KEYWORD_USE_VIS, KEYWORD_USE_VIS2, KEYWORD_USE_T3,
                KEYWORD_INIT_IMG, KEYWORD_MAXITER, KEYWORD_RGL_NAME, KEYWORD_RGL_WGT,
                KEYWORD_AUTO_WGT, KEYWORD_FLUX, KEYWORD_FLUXERR, KEYWORD_RGL_PRIO
        ).forEach(keywordMeta -> {
            IMAGE_OI_INPUT_STD_KEYWORDS.put(keywordMeta.getName(), keywordMeta);
        });
    }

    /**
     * Return the keyword description given its name
     * @param name keyword name
     * @return keyword description
     */
    public static String getDescription(final String name) {
        final KeywordMeta meta = IMAGE_OI_INPUT_STD_KEYWORDS.get(name);
        if (meta != null) {
            return meta.getDescription();
        }
        return null;
    }

    /**
     * Public constructor
     */
    public ImageOiInputParam() {
        super(IMAGE_OI_INPUT_STD_KEYWORDS, ImageOiConstants.EXTNAME_IMAGE_OI_INPUT_PARAM);
        defineDefaultKeywordValues();
    }

    public final void defineDefaultKeywordValues() {
        setWaveMin(DEF_KEYWORD_WAVE_MIN);
        setWaveMax(DEF_KEYWORD_WAVE_MAX);
        setMaxiter(DEF_KEYWORD_MAXITER);

        setAutoWgt(DEF_KEYWORD_AUTO_WGT);
        setRglWgt(DEF_KEYWORD_RGL_WGT);
        setFlux(DEF_KEYWORD_FLUX);
        setFluxErr(DEF_KEYWORD_FLUXERR);

        // note: setRglName() not used as it is set by Service later
    }

//TODO: check if this values are the Table defaults => change such defaults by software defaults.

    /*
     * --- Keywords ------------------------------------------------------------
     */
    public String getTarget() {
        return getKeyword(ImageOiConstants.KEYWORD_TARGET);
    }

    public void setTarget(String target) {
        setKeyword(ImageOiConstants.KEYWORD_TARGET, target);
    }

    public double getWaveMin() {
        return getKeywordDouble(ImageOiConstants.KEYWORD_WAVE_MIN);
    }

    public void setWaveMin(double wave_min) {
        setKeywordDouble(ImageOiConstants.KEYWORD_WAVE_MIN, wave_min);
    }

    public double getWaveMax() {
        return getKeywordDouble(ImageOiConstants.KEYWORD_WAVE_MAX);
    }

    public void setWaveMax(double wave_max) {
        setKeywordDouble(ImageOiConstants.KEYWORD_WAVE_MAX, wave_max);
    }

    public boolean useVis() {
        return getKeywordLogical(ImageOiConstants.KEYWORD_USE_VIS);
    }

    public void useVis(boolean use_vis) {
        setKeywordLogical(ImageOiConstants.KEYWORD_USE_VIS, use_vis);
    }

    public boolean useVis2() {
        return getKeywordLogical(ImageOiConstants.KEYWORD_USE_VIS2);
    }

    public void useVis2(boolean use_vis2) {
        setKeywordLogical(ImageOiConstants.KEYWORD_USE_VIS2, use_vis2);
    }

    public boolean useT3() {
        return getKeywordLogical(ImageOiConstants.KEYWORD_USE_T3);
    }

    public void useT3(boolean use_t3) {
        setKeywordLogical(ImageOiConstants.KEYWORD_USE_T3, use_t3);
    }

    public String getInitImg() {
        return getKeyword(ImageOiConstants.KEYWORD_INIT_IMG);
    }

    public void setInitImg(String init_img) {
        setKeyword(ImageOiConstants.KEYWORD_INIT_IMG, init_img);
    }

    public int getMaxiter() {
        return getKeywordInt(ImageOiConstants.KEYWORD_MAXITER);
    }

    public void setMaxiter(int maxiter) {
        setKeywordInt(ImageOiConstants.KEYWORD_MAXITER, maxiter);
    }

    public String getRglName() {
        return getKeyword(ImageOiConstants.KEYWORD_RGL_NAME);
    }

    public void setRglName(String rgl_name) {
        setKeyword(ImageOiConstants.KEYWORD_RGL_NAME, rgl_name);
    }

    public boolean isAutoWgt() {
        return getKeywordLogical(ImageOiConstants.KEYWORD_AUTO_WGT);
    }

    public void setAutoWgt(boolean auto_wgt) {
        setKeywordLogical(ImageOiConstants.KEYWORD_AUTO_WGT, auto_wgt);
    }

    public double getRglWgt() {
        return getKeywordDouble(ImageOiConstants.KEYWORD_RGL_WGT);
    }

    public void setRglWgt(double rgl_wgt) {
        setKeywordDouble(ImageOiConstants.KEYWORD_RGL_WGT, rgl_wgt);
    }

    public double getFlux() {
        return getKeywordDouble(ImageOiConstants.KEYWORD_FLUX);
    }

    public void setFlux(double flux) {
        setKeywordDouble(ImageOiConstants.KEYWORD_FLUX, flux);
    }

    public double getFluxErr() {
        return getKeywordDouble(ImageOiConstants.KEYWORD_FLUXERR);
    }

    public void setFluxErr(double FLUXERR) {
        setKeywordDouble(ImageOiConstants.KEYWORD_FLUXERR, FLUXERR);
    }

    public String getRglPrio() {
        return getKeyword(ImageOiConstants.KEYWORD_RGL_PRIO);
    }

    public void setRglPrio(String rgl_prio) {
        setKeyword(ImageOiConstants.KEYWORD_RGL_PRIO, rgl_prio);
    }

}
