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

import fr.jmmc.jmcs.util.NumberUtils;
import fr.jmmc.oitools.OIFitsConstants;
import static fr.jmmc.oitools.meta.CellMeta.NO_STR_VALUES;
import fr.jmmc.oitools.meta.KeywordMeta;
import fr.jmmc.oitools.meta.Types;
import fr.jmmc.oitools.model.ModelBase;
import fr.jmmc.oitools.model.OIFitsChecker;
import fr.jmmc.oitools.model.Rule;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;

/**
 * Abstract class to gather methods related to keywords and header cards for any HDU (image or binary table)
 * @author kempsc
 */
public abstract class FitsHDU extends ModelBase {

    /* constants */
    /** assigns a value to undefined extNumber */
    public static final int UNDEFINED_EXT_NB = -1;

    /** minimum year value */
    public static final int YEAR_MIN = 1933;
    /** maximum year value */
    public static final int YEAR_MAX = 2150;

    /** MJD starting at 1-JAN-1933 00:00 UT */
    public final static double MJD_1933 = 27073.5d;
    /** MJD starting at 1-JAN-2000 00:00 UT */
    public final static double MJD_2000 = 51544.5d;
    /** MJD starting at 1-JAN-2150 00:00 UT */
    public final static double MJD_2150 = 106332.5d;

    /**
     * EXTNAME keyword descriptor
     */
    private final static KeywordMeta KEYWORD_EXTNAME = new KeywordMeta(FitsConstants.KEYWORD_EXT_NAME,
            "extension name", Types.TYPE_CHAR, true, NO_STR_VALUES);

    /* members */
    /** Rule list used in the situation to harvest the rules. It is not created if one is not in isInspectRules() 
     */
    private final Set<Rule> applyRules = OIFitsChecker.isInspectRules() ? new HashSet<Rule>() : null;

    /**
     * Fits extension number (-1 means undefined)
     */
    private int extNb = UNDEFINED_EXT_NB;

    /* descriptors */
    /**
     * Map storing keyword definitions ordered according to OIFits specification
     */
    private final Map<String, KeywordMeta> keywordsDesc = new LinkedHashMap<String, KeywordMeta>();

    /* data */
    /**
     * Map storing keyword values
     */
    private final Map<String, Object> keywordsValue = new HashMap<String, Object>();
    /** optional list of header cards */
    private ArrayList<FitsHeaderCard> headerCards = null;

    /**
     * Protected class constructor
     */
    protected FitsHDU() {
        super();

        // since every class constructor of OI table calls super
        // constructor, next keywords will be common to every subclass :
        // EXTNAME keyword definition (optional) without defining accepted values:
        addKeywordMeta(KEYWORD_EXTNAME);
    }

    /* --- Rules --- */
    /**
     * Get the applyRules
     *
     * @return applyRules
     */
    public Set<Rule> getApplyRules() {
        return applyRules;
    }

    /* --- ext number --- */
    /**
     * Get the extension number
     *
     * @return the extension number
     */
    public final int getExtNb() {
        return extNb;
    }

    /**
     * Define the extension number
     *
     * @param extNb extension number
     */
    public final void setExtNb(final int extNb) {
        this.extNb = extNb;
    }

    /*
     * --- Keyword descriptors -------------------------------------------------
     */
    /**
     * Return the Map storing keyword definitions
     *
     * @return Map storing keyword definitions
     */
    public final Map<String, KeywordMeta> getKeywordsDesc() {
        return this.keywordsDesc;
    }

    /**
     * Get the keyword description when we have the name information
     * @param name keyword name
     * @return description for this keyword name
     */
    public KeywordMeta getKeywordsDesc(final String name) {
        return getKeywordsDesc().get(name);
    }

    /**
     * Return the ordered collection of keyword definitions
     *
     * @return ordered collection of keyword definitions
     */
    public final Collection<KeywordMeta> getKeywordDescCollection() {
        return getKeywordsDesc().values();
    }

    /**
     * Return the nth element of the keyword definitions
     * @param index index of the element
     * @return nth element of the keyword definitions or null if out of range
     */
    public final KeywordMeta getKeywordDesc(final int index) {
        // TODO: optimize this method to avoid allocation (used by JTable Model)
        // ie use a new list of keyword names (and column names)
        if (index >= 0 && index < this.keywordsDesc.size()) {
            final Set<Entry<String, KeywordMeta>> entries = this.keywordsDesc.entrySet();

            int i = 0;
            for (Iterator<Entry<String, KeywordMeta>> it = entries.iterator(); it.hasNext(); i++) {
                final Entry<String, KeywordMeta> e = it.next();
                if (i == index) {
                    return e.getValue();
                }
            }
        }
        return null;
    }

    /**
     * Return true if the given keyword descriptor is present
     *
     * @param name keyword name
     * @return true if the given keyword descriptor is present
     */
    public final boolean hasKeywordMeta(final String name) {
        return (getKeywordsDesc(name) != null);
    }

    /**
     * Add the given keyword descriptor
     *
     * @param meta keyword descriptor
     */
    protected final void addKeywordMeta(final KeywordMeta meta) {
        getKeywordsDesc().put(meta.getName(), meta);
    }

    /**
     * Remove the keyword given its name
     *
     * @param name keyword name
     */
    protected final void removeKeywordMeta(final String name) {
        getKeywordsDesc().remove(name);
    }

    /*
     * --- Keyword values ------------------------------------------------------
     */
    /**
     * Return the Map storing keyword values
     *
     * @return Map storing keyword values
     */
    public final Map<String, Object> getKeywordsValue() {
        return this.keywordsValue;
    }

    /**
     * Return the keyword value given its name The returned value can be null if
     * the keyword is optional or has never been defined
     *
     * @param name keyword name
     * @return any object value or null if undefined
     */
    public final Object getKeywordValue(final String name) {
        return getKeywordsValue().get(name);
    }

    /**
     * Return the keyword value given its name as a String
     *
     * @param name keyword name
     * @return String value
     */
    public final String getKeyword(final String name) {
        return (String) getKeywordValue(name);
    }

    /**
     * Return the keyword value given its name as an integer (primitive type)
     *
     * @param name keyword name
     * @return int value or 0 if undefined
     */
    public final int getKeywordInt(final String name) {
        return getKeywordInt(name, 0);
    }

    /**
     * Return the keyword value given its name as an integer (primitive type)
     *
     * @param name keyword name
     * @param def default value
     * @return int value or def if undefined
     */
    public final int getKeywordInt(final String name, final int def) {
        final Number value = (Number) getKeywordValue(name);
        if (value == null) {
            return def;
        }
        return value.intValue();
    }

    /**
     * Return the keyword value given its name as a double (primitive type)
     *
     * @param name keyword name
     * @return double value or 0d if undefined
     */
    public final double getKeywordDouble(final String name) {
        return getKeywordDouble(name, 0d);
    }

    /**
     * Return the keyword value given its name as a double (primitive type)
     *
     * @param name keyword name
     * @param def default value
     * @return double value or 0d if undefined
     */
    public final double getKeywordDouble(final String name, final double def) {
        final Number value = (Number) getKeywordValue(name);
        if (value == null) {
            return def;
        }
        return value.doubleValue();
    }

    /**
     * Return the keyword value given its name as a boolean (primitive type)
     *
     * @param name keyword name
     * @return boolean value or false if undefined
     */
    public final boolean getKeywordLogical(final String name) {
        return getKeywordLogical(name, false);
    }

    /**
     * Return the keyword value given its name as a boolean (primitive type)
     *
     * @param name keyword name
     * @param def default value
     * @return boolean value or 0d if undefined
     */
    public final boolean getKeywordLogical(final String name, final boolean def) {
        final Boolean value = (Boolean) getKeywordValue(name);
        if (value == null) {
            return def;
        }
        return value;
    }

    /**
     * Define the keyword value given its name and value
     *
     * @param name keyword name
     * @param value any object value
     */
    public final void setKeywordValue(final String name, final Object value) {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "KEYWORD [{0}] = ''{1}'' [{2}]", new Object[]{name, value, (value != null) ? value.getClass().getSimpleName() : ""});
        }
        getKeywordsValue().put(name, value);
    }

    /**
     * Define the keyword value given its name and value as a String
     *
     * @param name keyword name
     * @param value a String value
     */
    public final void setKeyword(final String name, final String value) {
        setKeywordValue(name, value);
    }

    /**
     * Define the keyword value given its name and value as a int
     *
     * @param name keyword name
     * @param value a int value
     */
    public final void setKeywordInt(final String name, final int value) {
        setKeywordValue(name, NumberUtils.valueOf(value));
    }

    /**
     * Define the keyword value given its name and value as a double
     *
     * @param name keyword name
     * @param value a double value
     */
    public final void setKeywordDouble(final String name, final double value) {
        setKeywordValue(name, Double.valueOf(value));
    }

    /**
     * Define the keyword value given its name and value as a boolean
     *
     * @param name keyword name
     * @param value a boolean value
     */
    public final void setKeywordLogical(final String name, final boolean value) {
        setKeywordValue(name, Boolean.valueOf(value));
    }

    /**
     * Update the keyword value given its name and value. Stored data is
     * converted from string using the associated meta.
     *
     * @param name keyword name.
     * @param strValue a String value of the object to be stored in keywords.
     */
    public final void updateKeyword(final String name, final String strValue) {
        final Types dataType = getKeywordsDesc(name).getDataType();
        switch (dataType) {
            case TYPE_CHAR:
                setKeywordValue(name, strValue);
                break;
            case TYPE_DBL:
                setKeywordValue(name, Double.valueOf(strValue));
                break;
            case TYPE_INT:
                setKeywordValue(name, NumberUtils.valueOf(strValue));
                break;
            case TYPE_LOGICAL:
                setKeywordValue(name, Boolean.valueOf(strValue));
                break;
            default:
                logger.log(Level.WARNING, "Ignore {0} keyword update of type {1}", new Object[]{name, dataType});
        }
    }

    /* --- Extra keywords --- */
    /**
     * Return true if the optional list of extra FITS header cards is not empty
     *
     * @return true if the optional list of extra FITS header cards is not empty
     */
    public final boolean hasHeaderCards() {
        return (this.headerCards != null && !this.headerCards.isEmpty());
    }

    /**
     * Return the list of header cards, no argument case
     *
     * @return list of header cards
     */
    public final List<FitsHeaderCard> getHeaderCards() {
        return getHeaderCards(10);
    }

    /**
     * Return the list of header cards, with argument
     *
     * @param nCards number of cards to define the initial capacity of the list
     * @return list of header cards
     */
    public final List<FitsHeaderCard> getHeaderCards(final int nCards) {
        if (this.headerCards == null) {
            this.headerCards = new ArrayList<FitsHeaderCard>(nCards);
        }
        return this.headerCards;
    }

    /**
     * Return a string representation of the list of header cards
     *
     * @param separator separator to use after each header card
     * @return string representation of the list of header cards
     */
    public final String getHeaderCardsAsString(final String separator) {
        if (this.headerCards == null) {
            return "";
        }
        final StringBuilder sb = new StringBuilder(1024);
        for (FitsHeaderCard card : this.headerCards) {
            card.toString(sb);
            sb.append(separator);
        }
        return sb.toString();
    }

    /**
     * Trim the list of header cards
     */
    public final void trimHeaderCards() {
        if (this.headerCards != null) {
            if (this.headerCards.size() > 0) {
                this.headerCards.trimToSize();
            } else {
                this.headerCards = null;
            }
        }
    }

    /**
     * Add a new extra keyword (header card)
     * @param key header card key
     * @param value optional header card value
     * @param comment optional header card comment
     */
    public final void addHeaderCard(final String key, final String value, final String comment) {
        getHeaderCards().add(new FitsHeaderCard(key, value, true, comment));
    }

    /**
     * Find the first header card (in the card list) corresponding to the given key
     * @param key header card key
     * @return header card or null if none found
     */
    public final FitsHeaderCard findFirstHeaderCard(final String key) {
        if (this.headerCards != null) {
            for (FitsHeaderCard card : this.headerCards) {
                if (key.equals(card.getKey())) {
                    return card;
                }
            }
        }
        return null;
    }

    /**
     * Add a new extra HISTORY keyword (header card)
     * @param comment header card comment
     */
    public final void addHeaderHistory(final String comment) {
        addHeaderCard(FitsConstants.KEYWORD_HISTORY, null, comment);
    }

    /*
     * --- OIFits standard Keywords --------------------------------------------
     */
    /**
     * Get the EXTNAME keyword value
     *
     * @return value of EXTNAME keyword
     */
    public final String getExtName() {
        return getKeyword(FitsConstants.KEYWORD_EXT_NAME);
    }

    /**
     * Define the EXTNAME keyword value
     *
     * @param extName value of EXTNAME keyword
     */
    public final void setExtName(final String extName) {
        setKeyword(FitsConstants.KEYWORD_EXT_NAME, extName);
    }

    /* --- Other methods --- */
    /**
     * Returns the HDU id[EXT_NAME # EXT_NB] as string
     *
     * @return HDU id[EXT_NAME # EXT_NB] as string
     */
    public final String idToString() {
        return getHDUId(getExtName(), getExtNb());
    }

    /**
     * Returns a string representation of this table
     *
     * @return a string representation of this table
     */
    @Override
    public String toString() {
        return idToString();
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
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "checkSyntax : {0}", this.toString());
        }
        logger.log(Level.INFO, "Analysing HDU [{0}]:", idToString());

        // First analyse keywords
        checkKeywords(checker);
    }

    /**
     * Check syntax of table's keywords. It consists in checking all mandatory
     * keywords are present, with right name, right format and right values (if
     * they do belong to a given set of accepted values).
     *
     * @param checker checker component
     */
    public void checkKeywords(final OIFitsChecker checker) {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "checkKeywords : {0}", this.toString());
        }
        String keywordName;
        Object value;

        /* Get mandatory keywords names */
        for (KeywordMeta keyword : getKeywordDescCollection()) {
            keywordName = keyword.getName();

            // get keyword value :
            value = getKeywordValue(keywordName);

            if ((value == null) || OIFitsChecker.isInspectRules()) {
                if (!keyword.isOptional()) {
                    // rule [GENERIC_KEYWORD_MANDATORY]
                    checker.ruleFailed(Rule.GENERIC_KEYWORD_MANDATORY, this, keywordName);
                }
            }
            if ((value != null) || OIFitsChecker.isInspectRules()) {

                if ((value == null) && OIFitsChecker.isInspectRules()) {
                    // InspectRules: fix null value in following check():
                    value = getDefaultValue(keyword.getDataType());
                }

                /* Check the keyword validity */
                keyword.check(this, value, checker);
            }
        }
    }

    /**
     * Check format for Keyword
     * @param checker checker component
     * @param hduFits FitsHDU
     * @param keyword KeywordMeta
     * @param kDataType Types
     */
    public void checkKeywordFormat(final OIFitsChecker checker, final FitsHDU hduFits, final KeywordMeta keyword, final Types kDataType) {
        if ((kDataType != keyword.getDataType()) || OIFitsChecker.isInspectRules()) {
            // rule [GENERIC_KEYWORD_FORMAT] check if the keyword format matches the expected format (data type)
            checker.ruleFailed(Rule.GENERIC_KEYWORD_FORMAT, hduFits, keyword.getName()).addKeywordValue(kDataType.getRepresentation(), keyword.getType());
        }
    }

    /**
     * Check if the MJD value is within 'normal' range (1933 - 2150)
     * @param checker checker component
     * @param name column/keyword name 
     * @param mjd value
     */
    public void checkMJD(final OIFitsChecker checker, final String name, final double mjd) {
        // rule [GENERIC_MJD_RANGE] check if the MJD value is within 'normal' range (1933 - 2150)
        // mjd can be NaN and then is not checked:
        if (((mjd < MJD_1933) || (mjd > MJD_2150)) || OIFitsChecker.isInspectRules()) {
            checker.ruleFailed(Rule.GENERIC_MJD_RANGE, this, name).addKeywordValue(mjd, MJD_1933 + " - " + MJD_2150);
        }
    }

    /**
     * Check if the DATE-OBS value is a 'normal' date format (yyyy-mm-dd)
     * @param checker checker component
     * @param name keyword name 
     * @param hdu FitsHDU
     */
    public static void checkDateObsKeyword(final OIFitsChecker checker, final String name, final FitsHDU hdu) {
        final String dateObs = hdu.getKeyword(name);

        if ((dateObs != null) || OIFitsChecker.isInspectRules()) {
            final SimpleDateFormat sdf = new SimpleDateFormat(FitsConstants.FORMAT_DATE);
            sdf.setLenient(false);

            boolean valid = false;
            if ((dateObs != null) && !dateObs.isEmpty()) {
                try {
                    final Date date = sdf.parse(dateObs);
                    // convert again to string
                    final String strDate = sdf.format(date);

                    // Check only date present at the beginning:
                    if (dateObs.startsWith(strDate)) {
                        valid = true;
                    }
                } catch (ParseException pe) {
                    logger.log(Level.WARNING, "checkDateObsKeyword: unable to parse date {0}", dateObs);
                }
            }

            if (!valid || OIFitsChecker.isInspectRules()) {
                // rule [GENERIC_DATE_OBS_STANDARD] check if the DATE_OBS keyword is in the format 'YYYY-MM-DD'
                checker.ruleFailed(Rule.GENERIC_DATE_OBS_STANDARD, hdu, OIFitsConstants.KEYWORD_DATE_OBS).addKeywordValue(dateObs);
            }
            if (valid || OIFitsChecker.isInspectRules()) {
                final Calendar cal = sdf.getCalendar();
                final int year = cal.get(Calendar.YEAR);

                if (((year < YEAR_MIN) || (year > YEAR_MAX)) || OIFitsChecker.isInspectRules()) {
                    // rule [GENERIC_DATE_OBS_RANGE] check if the DATE_OBS value is within 'normal' range (1933 - 2150)
                    checker.ruleFailed(Rule.GENERIC_DATE_OBS_RANGE, hdu, OIFitsConstants.KEYWORD_DATE_OBS).addKeywordValue(dateObs, ("" + YEAR_MIN + " - " + YEAR_MAX));
                }
            }
        }
    }

    private static Object getDefaultValue(final Types dataType) {
        switch (dataType) {
            case TYPE_CHAR:
                return "DEFAULT";
            case TYPE_DBL:
                return Double.valueOf(0);
            case TYPE_INT:
                return NumberUtils.valueOf(0);
            case TYPE_LOGICAL:
                return Boolean.TRUE;
            default:
                return null;
        }
    }
}
