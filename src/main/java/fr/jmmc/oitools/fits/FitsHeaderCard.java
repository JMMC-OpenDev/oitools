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
package fr.jmmc.oitools.fits;

import nom.tam.fits.HeaderCard;

/**
 * This class represents one Fits header card (key, value, comment) immutable
 * @author bourgesl
 */
public final class FitsHeaderCard {

    /** header card key */
    private final String key;
    /** optional header card value */
    private String value;
    /** optional header card comment */
    private String comment;

    /**
     * Protected constructor
     * @param key header card key
     * @param value optional header card value
     * @param comment optional header card comment
     */
    public FitsHeaderCard(final String key, final String value, final String comment) {
        if (key != null && key.length() > HeaderCard.MAX_KEYWORD_LENGTH && !key.startsWith("HIERARCH.")) {
            throw new IllegalArgumentException("Keyword key is too long (max" + HeaderCard.MAX_KEYWORD_LENGTH + " chars) : " + key);
        }
        if (value != null && value.length() > HeaderCard.MAX_VALUE_LENGTH) {
            throw new IllegalArgumentException("Keyword value is too long (max" + HeaderCard.MAX_VALUE_LENGTH + " chars) : " + value);
        }
        this.key = key;
        this.value = value;
        this.comment = comment;
    }

    /**
     * Return the header card key
     * @return header card key
     */
    public String getKey() {
        return key;
    }

    /**
     * Return the optional header card value
     * @return optional header card value
     */
    public String getValue() {
        return value;
    }

    /**
     * Return the optional header card comment
     * @return optional header card comment
     */
    public String getComment() {
        return comment;
    }

    /**
     * Returns a string representation of this Fits header card
     * @return a string representation of this Fits header card
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(80);
        toString(sb);
        return sb.toString();
    }

    /**
     * Returns a string representation of this table
     * @param sb string builder to append to
     */
    public void toString(final StringBuilder sb) {
        sb.append(key);
        if (value != null) {
            sb.append(" = ").append(value);
        }
        if (comment != null) {
            sb.append(" // ").append(comment);
        }
    }
}
