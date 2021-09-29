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

import fr.jmmc.oitools.fits.FitsHeaderCard;
import fr.jmmc.oitools.fits.FitsTable;
import fr.jmmc.oitools.meta.KeywordMeta;
import fr.jmmc.oitools.meta.Types;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/** Common behaviour between ImageOIIntputParam & ImageOIOutputParam. */
public abstract class ImageOiParam extends FitsTable {

    /* members */
    /** flag indicating if the default keywords are being defined */
    private boolean isDefaultKeyword = true;
    /** parent keyword metas */
    private final Set<KeywordMeta> parentKeywordMetas = new LinkedHashSet<>();
    /** default keyword names */
    private final Set<String> defaultKeywords = new LinkedHashSet<>();
    /** specific keyword names */
    private final Set<String> specificKeywords = new LinkedHashSet<>();

    /** keyword mapping to quickly reset keywords and give their description.
     * Filled in statically in subclasses.
     */
    protected final static Map<String, KeywordMeta> KEYWORD_METAS;
    static { KEYWORD_METAS = new LinkedHashMap<>(8); }

    /**
     * Public constructor
     */
    public ImageOiParam () {
        super();

        // preserve keywords defined in parents:
        parentKeywordMetas.addAll(getKeywordsDesc().values());

        // Set default values
        setNbRows(0);
        setExtVer(1);

        // KEYWORD_METAS is modified in subclasses but statically
        // so there is no problem with this call in constructor
        resetDefaultKeywords();
    }

    /**
     * Register all default keywords
     */
    public final void resetDefaultKeywords() {
        getKeywordsDesc().clear();
        // reset keyword names:
        defaultKeywords.clear();
        specificKeywords.clear();

        try {
            isDefaultKeyword = true;

            // Register keywords into Fits table:
            for (KeywordMeta meta : parentKeywordMetas) {
                addKeyword(meta);
            }
            for (KeywordMeta meta : KEYWORD_METAS.values()) {
                addKeyword(meta);
            }
        } finally {
            isDefaultKeyword = false;
        }
    }

    /**
     * Add the given keyword descriptor
     *
     * @param meta keyword descriptor
     */
    public final void addKeyword(final KeywordMeta meta) {
        super.addKeywordMeta(meta);

        final String name = meta.getName();
        if (isDefaultKeyword) {
            defaultKeywords.add(name);
        } else {
            specificKeywords.add(name);

            // convert FitsHeaderCards (extra keywords including specific keyword values):
            if (hasHeaderCards()) {
                convertHeaderCards(name);
            }
        }
    }

    private void convertHeaderCards(final String name) {
        for (Iterator<FitsHeaderCard> it = getHeaderCards().iterator(); it.hasNext();) {
            final FitsHeaderCard card = it.next();
            if (name.equals(card.getKey())) {
                setKeywordDefaultFromCard(name, card.getValue());

                // remove to avoid any duplicated keyword:
                it.remove();
                break;
            }
        }
    }

    private void setKeywordDefaultFromCard(final String name, final String value) {
        if (!hasKeywordValue(name)) {
            // Fix Logical (T|F) to Boolean:
            final String strValue;
            if (getKeywordsDesc(name).getDataType() == Types.TYPE_LOGICAL) {
                strValue = "T".equals(value) ? "true" : "false";
            } else {
                strValue = value;
            }
            updateKeyword(name, strValue);
        }
    }

    private boolean hasKeywordValue(final String name) {
        return getKeywordValue(name) != null;
    }

    /**
     * Remove the keyword descriptor given its name
     *
     * @param name keyword name
     */
    public final void removeKeyword(final String name) {
        super.removeKeywordMeta(name);
        // note: it does not remove its value !

        defaultKeywords.remove(name);
        specificKeywords.remove(name);
    }

    public Set<String> getDefaultKeywords() {
        return defaultKeywords;
    }

    public Set<String> getSpecificKeywords() {
        return specificKeywords;
    }

    public final void setKeywordDefault(final String name, final Object value) {
        if (!hasKeywordValue(name)) {
            setKeywordValue(name, value);
        }
    }

    public final void setKeywordDefaultInt(final String name, final int value) {
        if (!hasKeywordValue(name)) {
            setKeywordInt(name, value);
        }
    }

    public final void setKeywordDefaultDouble(final String name, final double value) {
        if (!hasKeywordValue(name)) {
            setKeywordDouble(name, value);
        }
    }

    public final void setKeywordDefaultLogical(final String name, final boolean value) {
        if (!hasKeywordValue(name)) {
            setKeywordLogical(name, value);
        }
    }
}
