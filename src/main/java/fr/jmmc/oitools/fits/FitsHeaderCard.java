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

import static fr.jmmc.jmcs.util.NumberUtils.parseDouble;
import static fr.jmmc.jmcs.util.NumberUtils.parseInteger;
import fr.nom.tam.fits.HeaderCard;

/**
 * This class represents one Fits header card (key, value, comment) immutable
 * @author bourgesl
 */
public final class FitsHeaderCard {

    /** header card key */
    private final String key;
    /** optional header card value */
    private final String value;
    /** optional header card comment */
    private final String comment;
    /** flag indicating whether or not this is a string value */
    private boolean isString;
    /** optional parsed card value */
    private Object parsedValue = null;

    /**
     * Protected constructor
     * @param key header card key
     * @param value optional header card value
     * @param isString flag indicating whether or not this is a string value
     * @param comment optional header card comment
     */
    public FitsHeaderCard(final String key, final String value, final boolean isString, final String comment) {
        if (key != null && key.length() > HeaderCard.MAX_KEYWORD_LENGTH && !key.startsWith("HIERARCH.")) {
            throw new IllegalArgumentException("Keyword key is too long (max" + HeaderCard.MAX_KEYWORD_LENGTH + " chars) : " + key);
        }
        if (value != null && value.length() > HeaderCard.MAX_VALUE_LENGTH) {
            throw new IllegalArgumentException("Keyword value is too long (max" + HeaderCard.MAX_VALUE_LENGTH + " chars) : " + value);
        }
        this.key = key;
        this.value = value;
        this.comment = comment;
        this.isString = isString;
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
     * Return the flag indicating whether or not this is a string value
     * @return flag indicating whether or not this is a string value
     */
    public boolean isString() {
        return isString;
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
     * Returns a string representation of this Fits header card
     * @param sb string builder to append to
     */
    public void toString(final StringBuilder sb) {
        sb.append(key);
        if (value != null) {
            sb.append(" = ");
            if (isString) {
                sb.append('\'');
            }
            sb.append(value);
            if (isString) {
                sb.append('\'');
            }
        }
        if (comment != null) {
            sb.append(" // ").append(comment);
        }
    }

    /**
     * Returns an object representation of this Fits header card or null
     * It tries to guess the type (Boolean / Double / Integer) according to the String value
     * @return String / Boolean / Double / Integer instance or null
     */
    public Object parseValue() {
        if (value == null) {
            return null;
        }
        // value is not null:
        // use cached parsed value:
        if (parsedValue != null) {
            return parsedValue;
        }
        // else do parse value:
        if (isString) {
            return parsedValue = value;
        }

        // Logical
        if (value.startsWith("T")) {
            return parsedValue = Boolean.TRUE;
        }
        if (value.startsWith("F")) {
            return parsedValue = Boolean.FALSE;
        }

        // Numeric keywords:
        if (value.indexOf('.') == -1) {
            // check for Integers:
            final Integer i = parseInteger(value);
            if (i != null) {
                return parsedValue = i;
            }
        } else {
            // check for Doubles:
            final Double d = parseDouble(value);
            if (d != null) {
                return parsedValue = d;
            }
        }
        // fallback, return string
        return parsedValue = value;
    }
}
