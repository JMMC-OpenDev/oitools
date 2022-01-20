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
 * This class is a container for IMAGE-OI OUTPUT PARAM.
 * https://github.com/emmt/OI-Imaging-JRA
 * It is returned be processing software and included in IMAGE-OI compliant files.
 *
 * @author mellag
 */
public final class ImageOiOutputParam extends ImageOiParam {

    // Define Algorithm results keywords
    private final static KeywordMeta KEYWORD_LAST_IMG = new KeywordMeta(ImageOiConstants.KEYWORD_LAST_IMG, "Identifier of the final image", Types.TYPE_CHAR);
    private final static KeywordMeta KEYWORD_NITER = new KeywordMeta(ImageOiConstants.KEYWORD_NITER, "Total iterations done in the current program run", Types.TYPE_INT);
    private final static KeywordMeta KEYWORD_CHISQ = new KeywordMeta(ImageOiConstants.KEYWORD_CHISQ, "Reduced chi-squared", Types.TYPE_DBL);
    private final static KeywordMeta KEYWORD_FLUX = new KeywordMeta(ImageOiConstants.KEYWORD_FLUX, "Total image flux", Types.TYPE_DBL);

    private final static Map<String,KeywordMeta> IMAGE_OI_OUTPUT_STD_KEYWORDS;
    static {
        IMAGE_OI_OUTPUT_STD_KEYWORDS = new LinkedHashMap<>();
        Arrays.asList(
            KEYWORD_LAST_IMG, KEYWORD_NITER, KEYWORD_CHISQ, KEYWORD_FLUX
        ).forEach(keywordMeta -> {
            IMAGE_OI_OUTPUT_STD_KEYWORDS.put(keywordMeta.getName(), keywordMeta);
        });
    }

    // Image parameters
    public ImageOiOutputParam() {
        super(IMAGE_OI_OUTPUT_STD_KEYWORDS, ImageOiConstants.EXTNAME_IMAGE_OI_OUTPUT_PARAM);
    }

    /**
     * copy-constructor.
     *
     * ImageOiParam fields:
     * - isDefaultKeyword: correctly set to true in ImageOiParam.new.
     * - parentKeywordMetas : correctly initialized in ImageOiParam.new, then unmodified.
     * - defaultKeywords: idem as parentKeywordMetas.
     * - specificKeywords: any specific keyword can have been added to source, so we copy them with addKeyword().
     * - stdImgOIKeywords: idem as parentKeywordMetas.
     *
     * FitsTable fields:
     * - columnsDesc: correctly initialized in ImageOiOutputParam.new. There is none at time of writing.
     * --- According to spec, `IMAGE-OI OUTPUT PARAM` hdus could have data in the future.
     * - columnsDerivedDesc: idem as columnsDesc.
     * - columnsAliases: idem as columnsDesc.
     * - columnsValue: copied by copyTable(). Caution: values are Object and thus only references are copied.
     * - columnsDerivedValue: lazy so uncopied.
     * - columnsRangeValue: lazy so uncopied.
     *
     * FitsHDU fields:
     * - applyRules: useless to copy. will be created on demand by inspectRules().
     * - extNb: idem as parentKeywordMetas.
     * - keywordsDesc: idem as specificKeywords.
     * - keywordsValue: copied by copyTable().
     * --- Note: this is intentionally done at last, so that we correctly get
     * --- all keywords that are set during the chain of construction. Note that ImageOiOutputParam is final.
     * - headerCards: idem as keywordsValue.
     *
     * @param source ImageOiOutputParam to copy from (required).
     */
    public ImageOiOutputParam(final ImageOiOutputParam source) {
        this();

        // copy specific keywords
        source.getSpecificKeywords().forEach((String specificKeyword) -> {
            addKeyword(source.getKeywordsDesc(specificKeyword));
        });

        // copy keywords and columns
        copyTable(source);
    }

    /*
     * --- Keywords ------------------------------------------------------------
     */
    public String getLastImg() {
        return getKeyword(ImageOiConstants.KEYWORD_LAST_IMG);
    }

    public void setLastImg(String last_img) {
        setKeyword(ImageOiConstants.KEYWORD_LAST_IMG, last_img);
    }

    public int getNiter() {
        return getKeywordInt(ImageOiConstants.KEYWORD_NITER);
    }

    public void setNiter(int maxiter) {
        setKeywordInt(ImageOiConstants.KEYWORD_NITER, maxiter);
    }

    public double getChiSq() {
        return getKeywordDouble(ImageOiConstants.KEYWORD_CHISQ);
    }

    public void setChiSq(double chisq) {
        setKeywordDouble(ImageOiConstants.KEYWORD_CHISQ, chisq);
    }

    public double getFlux() {
        return getKeywordDouble(ImageOiConstants.KEYWORD_FLUX);
    }

    public void setFlux(double flux) {
        setKeywordDouble(ImageOiConstants.KEYWORD_FLUX, flux);
    }
}
