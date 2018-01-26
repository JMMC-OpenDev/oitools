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

import fr.jmmc.oitools.fits.FitsTable;
import fr.jmmc.oitools.fits.FitsUtils;
import static fr.jmmc.oitools.meta.CellMeta.NO_STR_VALUES;
import fr.jmmc.oitools.meta.KeywordMeta;
import fr.jmmc.oitools.meta.Types;

/**
 * This class is a container for IMAGE-OI INPUT PARAM.
 * https://github.com/emmt/OI-Imaging-JRA
 * It can be associated to an OIFitsFile to produce IMAGE-OI compliant files.
 *
 * @author mellag
 */
public final class ImageOiInputParam extends FitsTable {

    // Define Data selection keywords
    private final static KeywordMeta KEYWORD_TARGET = new KeywordMeta(ImageOiConstants.KEYWORD_TARGET, "Identifier of the target object to reconstruct", Types.TYPE_CHAR);
    private final static KeywordMeta KEYWORD_WAVE_MIN = new KeywordMeta(ImageOiConstants.KEYWORD_WAVE_MIN, "Minimum wavelentgh to select (in meters)", Types.TYPE_DBL);
    private final static KeywordMeta KEYWORD_WAVE_MAX = new KeywordMeta(ImageOiConstants.KEYWORD_WAVE_MAX, "Maximum wavelentgh to select (in meters)", Types.TYPE_DBL);
    private final static KeywordMeta KEYWORD_USE_VIS = new KeywordMeta(ImageOiConstants.KEYWORD_USE_VIS, "Use complex visibility data if any", Types.TYPE_LOGICAL);
    private final static KeywordMeta KEYWORD_USE_VIS2 = new KeywordMeta(ImageOiConstants.KEYWORD_USE_VIS2, "Use squared visibility data if any", Types.TYPE_LOGICAL);
    private final static KeywordMeta KEYWORD_USE_T3 = new KeywordMeta(ImageOiConstants.KEYWORD_USE_T3, "Use triple product data if any", Types.TYPE_LOGICAL);

    // Define Algorithm settings keywords
    // TODO init-img keyword could be checked like OIDATA.INSNAME or OIDATA.ARRNAME to be sure that one or more HDUs are present with this name.
    private final static KeywordMeta KEYWORD_INIT_IMG = new KeywordMeta(ImageOiConstants.KEYWORD_INIT_IMG, "Identifier of the initial image", Types.TYPE_CHAR);
    private final static KeywordMeta KEYWORD_MAXITER = new KeywordMeta(ImageOiConstants.KEYWORD_MAXITER, "Maximum number of iterations to run", Types.TYPE_INT);
    private final static KeywordMeta KEYWORD_RGL_NAME = new KeywordMeta(ImageOiConstants.KEYWORD_RGL_NAME, "Name of the regularization method", Types.TYPE_CHAR);
    private final static KeywordMeta KEYWORD_RGL_WGT = new KeywordMeta(ImageOiConstants.KEYWORD_RGL_WGT, "Weight of the regularization", Types.TYPE_DBL);
    private final static KeywordMeta KEYWORD_RGL_ALPH = new KeywordMeta(ImageOiConstants.KEYWORD_RGL_ALPH, "Parameter alpha of the regularization", Types.TYPE_DBL);
    private final static KeywordMeta KEYWORD_RGL_BETA = new KeywordMeta(ImageOiConstants.KEYWORD_RGL_BETA, "Parameter beta of the regularization", Types.TYPE_DBL);
    private final static KeywordMeta KEYWORD_RGL_PRIO = new KeywordMeta(ImageOiConstants.KEYWORD_RGL_PRIO, "Identifier of the HDU with the prior image", Types.TYPE_CHAR);

    // Bsmem specific
    private final static KeywordMeta KEYWORD_AUTO_WGT = new KeywordMeta(ImageOiConstants.KEYWORD_AUTO_WGT,
            "Automatic regularization weight", Types.TYPE_LOGICAL, true, NO_STR_VALUES);
    private final static KeywordMeta KEYWORD_FLUXERR = new KeywordMeta(ImageOiConstants.KEYWORD_FLUXERR,
            "Error on zero-baseline V^2 point", Types.TYPE_DBL, true, NO_STR_VALUES);

    // Optional subtable
    private FitsTable subTable = null;

    // Image parameters
    public ImageOiInputParam() {
        super();

        // Register Data selection keywords
        addKeywordMeta(KEYWORD_TARGET);
        addKeywordMeta(KEYWORD_WAVE_MIN);
        addKeywordMeta(KEYWORD_WAVE_MAX);
        addKeywordMeta(KEYWORD_USE_VIS);
        addKeywordMeta(KEYWORD_USE_VIS2);
        addKeywordMeta(KEYWORD_USE_T3);

        // Register Algorithm settings keywords
        addKeywordMeta(KEYWORD_INIT_IMG);
        addKeywordMeta(KEYWORD_MAXITER);
        addKeywordMeta(KEYWORD_RGL_NAME);
        addKeywordMeta(KEYWORD_RGL_WGT);
        addKeywordMeta(KEYWORD_RGL_ALPH);
        addKeywordMeta(KEYWORD_RGL_BETA);
        addKeywordMeta(KEYWORD_RGL_PRIO);

        addKeywordMeta(KEYWORD_AUTO_WGT);
        addKeywordMeta(KEYWORD_FLUXERR);

        // Set default values
        setExtName(ImageOiConstants.EXTNAME_IMAGE_OI_INPUT_PARAM);

        // TODO make it dynamic and software dependant
        setWaveMin(-1);
        setWaveMax(-1);
        setMaxiter(200);
        setRglWgt(0);
        setRglAlph(0);
        setRglBeta(0);
        //setRglName("mem_prior"); // set by service later
        useAutoWgt(true);
        setFluxErr(0.1);
    }

    /**
     * Extand current table with given table elements ( keyword support only, limited to one single table )
     * @param subTable table to expose, null remove previous tables
     */
    public void addSubTable(final FitsTable subTable) {

        // clear previous table
        removeSubTable(this.subTable);
        this.subTable = subTable;
        if (subTable == null) {
            return;
        }

        // TODO perform some tests to avoid duplicates
        // add keyword descriptions
        getKeywordsDesc().putAll(subTable.getKeywordsDesc());
        // complete with values if no data is present
        for (String key : subTable.getKeywordsDesc().keySet()) {

            if (getKeywordValue(key) == null) {
                setKeywordValue(key, subTable.getKeywordValue(key));
            }
        }

        // TODO support columns ? not sure....
    }

    /**
     * Get table of specific keywords if any.
     * @return the table of specific keywords or null
     */
    public FitsTable getSubTable() {
        return this.subTable;
    }

    /**
     * Remove inclusion of given table.
     * @param subTable
     @
     */
    private void removeSubTable(final FitsTable subTable) {

        if (subTable == null) {
            return;
        }

        for (String key : subTable.getKeywordsDesc().keySet()) {
            // don't remove standard keywords!
            if (!FitsUtils.isStandardKeyword(key)) {
                getKeywordsDesc().remove(key);
                //getKeywordsValue().remove(key);
            }
        }
    }

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

    public double getRglWgt() {
        return getKeywordDouble(ImageOiConstants.KEYWORD_RGL_WGT);
    }

    public void setRglWgt(double rgl_wgt) {
        setKeywordDouble(ImageOiConstants.KEYWORD_RGL_WGT, rgl_wgt);
    }

    public double getRglAlph() {
        return getKeywordDouble(ImageOiConstants.KEYWORD_RGL_ALPH);
    }

    public void setRglAlph(double rgl_alph) {
        setKeywordDouble(ImageOiConstants.KEYWORD_RGL_ALPH, rgl_alph);
    }

    public double getRglBeta() {
        return getKeywordDouble(ImageOiConstants.KEYWORD_RGL_BETA);
    }

    public void setRglBeta(double rgl_beta) {
        setKeywordDouble(ImageOiConstants.KEYWORD_RGL_BETA, rgl_beta);
    }

    public String getRglPrio() {
        return getKeyword(ImageOiConstants.KEYWORD_RGL_PRIO);
    }

    public void setRglPrio(String rgl_prio) {
        setKeyword(ImageOiConstants.KEYWORD_RGL_PRIO, rgl_prio);
    }

    // bsmem specific
    public double getFluxErr() {
        return getKeywordDouble(ImageOiConstants.KEYWORD_FLUXERR);

    }

    public void setFluxErr(double fluxErr) {
        setKeywordDouble(ImageOiConstants.KEYWORD_FLUXERR, fluxErr);

    }

    public boolean useAutoWgt() {
        return getKeywordLogical(ImageOiConstants.KEYWORD_AUTO_WGT);
    }

    public void useAutoWgt(boolean auto_rgl) {
        setKeywordLogical(ImageOiConstants.KEYWORD_AUTO_WGT, auto_rgl);
    }

}
