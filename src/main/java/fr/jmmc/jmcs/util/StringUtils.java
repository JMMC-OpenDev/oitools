/*******************************************************************************
 *                 jMCS project ( http://www.jmmc.fr/dev/jmcs )
 *******************************************************************************
 * Copyright (c) 2013, CNRS. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     - Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     - Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the name of the CNRS nor the names of its contributors may be
 *       used to endorse or promote products derived from this software without
 *       specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL CNRS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package fr.jmmc.jmcs.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * This class is copied from Jmcs (same package) in order to let OITools compile properly 
 * but at runtime only one implementation will be loaded (by class loader)
 * 
 * Note: Jmcs Changes must be reported here to avoid runtime issues !
 *
 * This class provides several helper methods related to String handling
 * @author Laurent BOURGES.
 */
public final class StringUtils {

    /** Empty String constant '' */
    public final static String STRING_EMPTY = "";
    /** String constant containing 1 space character ' ' */
    public final static String STRING_SPACE = " ";
    /** String constant containing 1 underscore character '_' */
    public final static String STRING_UNDERSCORE = "_";
    /** String constant containing 1 minus sign character '-' */
    public final static String STRING_MINUS_SIGN = "-";
    /** RegExp expression to match the underscore character '_' */
    private final static Pattern PATTERN_UNDERSCORE = Pattern.compile(STRING_UNDERSCORE);
    /** RegExp expression to match white spaces (1..n) */
    private final static Pattern PATTERN_WHITE_SPACE_MULTIPLE = Pattern.compile("\\s+");
    /** regular expression used to match characters different than alpha/numeric/_/+/- (1..n) */
    private final static Pattern PATTERN_NON_ALPHA_NUM = Pattern.compile("[^a-zA-Z_\\+\\-0-9]+");
    /** regular expression used to match characters different than alpha/numeric/_/-/. (1..n) */
    private final static Pattern PATTERN_NON_FILE_NAME = Pattern.compile("[^a-zA-Z0-9\\-_\\.]");
    /** regular expression used to match characters with accents */
    private final static Pattern PATTERN_ACCENT_CHARS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    /** regular expression used to match characters different than numeric (1..n) */
    private final static Pattern PATTERN_NON_NUM = Pattern.compile("[^0-9]+");
    /** RegExp expression to match carriage return */
    private final static Pattern PATTERN_CR = Pattern.compile("\n");
    /** RegExp expression to match tags */
    private final static Pattern PATTERN_TAGS = Pattern.compile("\\<.*?\\>");
    /** RegExp expression to SGML entities */
    private final static Pattern PATTERN_AMP = Pattern.compile("&");
    /** RegExp expression to start tag */
    private final static Pattern PATTERN_LT = Pattern.compile("<");
    /** RegExp expression to end tag */
    private final static Pattern PATTERN_GT = Pattern.compile(">");

    /**
     * Forbidden constructor
     */
    private StringUtils() {
        super();
    }

    /**
     * Test if value is set ie not empty
     *
     * @param value string value
     * @return true if value is NOT empty
     */
    public static boolean isSet(final String value) {
        return !isEmpty(value);
    }

    /**
     * Test if value is empty (null or no chars)
     * 
     * @param value string value
     * @return true if value is empty (null or no chars)
     */
    public static boolean isEmpty(final String value) {
        return value == null || value.length() == 0;
    }

    /**
     * Test if value is empty (null or no chars after trim)
     * 
     * @param value string value
     * @return true if value is empty (null or no chars after trim)
     */
    public static boolean isTrimmedEmpty(final String value) {
        return isEmpty(value) || value.trim().length() == 0;
    }

    /* --- accent handling -------------------------------------------------- */
    /**
     * Remove accents from any character i.e. remove diacritical marks
     * @param value input value
     * @return string value
     */
    public static String removeAccents(final String value) {
        // Remove accent from characters (if any) (Java 1.6)
        final String normalized = Normalizer.normalize(value, Normalizer.Form.NFD);

        return PATTERN_ACCENT_CHARS.matcher(normalized).replaceAll(STRING_EMPTY);
    }

    /* --- common white space helper methods -------------------------------- */

    /**
     * Trim and remove redundant white space characters
     * @param value input value
     * @return string value
     */
    public static String cleanWhiteSpaces(final String value) {
        return isEmpty(value) ? STRING_EMPTY : replaceWhiteSpaces(value.trim(), STRING_SPACE);
    }

    /**
     * Remove any white space character
     * @param value input value
     * @return string value
     */
    public static String removeWhiteSpaces(final String value) {
        return replaceWhiteSpaces(value, STRING_EMPTY);
    }

    /**
     * Remove any underscore character
     * @param value input value
     * @return string value
     */
    public static String removeUnderscores(final String value) {
        return PATTERN_UNDERSCORE.matcher(value).replaceAll(STRING_EMPTY);
    }
    
    /**
     * Remove redundant white space characters
     * @param value input value
     * @return string value
     */
    public static String removeRedundantWhiteSpaces(final String value) {
        return replaceWhiteSpaces(value, STRING_SPACE);
    }

    /**
     * Replace white space characters (1..n) by the underscore character
     * @param value input value
     * @return string value
     */
    public static String replaceWhiteSpacesByUnderscore(final String value) {
        return replaceWhiteSpaces(value, STRING_UNDERSCORE);
    }

    /**
     * Replace white space characters (1..n) by the minus sign character
     * @param value input value
     * @return string value
     */
    public static String replaceWhiteSpacesByMinusSign(final String value) {
        return replaceWhiteSpaces(value, STRING_MINUS_SIGN);
    }

    /**
     * Replace white space characters (1..n) by the given replacement string
     * @param value input value
     * @param replaceBy replacement string
     * @return string value
     */
    public static String replaceWhiteSpaces(final String value, final String replaceBy) {
        return PATTERN_WHITE_SPACE_MULTIPLE.matcher(value).replaceAll(replaceBy);
    }

    /* --- common alpha numeric helper methods ------------------------------ */
    /**
     * Remove any non alpha numeric character
     * @param value input value
     * @return string value
     */
    public static String removeNonAlphaNumericChars(final String value) {
        return replaceNonAlphaNumericChars(value, STRING_EMPTY);
    }

    /**
     * Replace non alpha numeric characters (1..n) by the underscore character
     * @param value input value
     * @return string value
     */
    public static String replaceNonAlphaNumericCharsByUnderscore(final String value) {
        return replaceNonAlphaNumericChars(value, STRING_UNDERSCORE);
    }

    /**
     * Replace non alpha numeric characters by the given replacement string
     * @param value input value
     * @param replaceBy replacement string
     * @return string value
     */
    public static String replaceNonAlphaNumericChars(final String value, final String replaceBy) {
        return PATTERN_NON_ALPHA_NUM.matcher(value).replaceAll(replaceBy);
    }

    /**
     * Replace non numeric characters by the given replacement string
     * @param value input value
     * @param replaceBy replacement string
     * @return string value
     */
    public static String replaceNonNumericChars(final String value, final String replaceBy) {
        return PATTERN_NON_NUM.matcher(value).replaceAll(replaceBy);
    }

    /**
     * Split the given value at non numeric characters
     * @param value input value
     * @return numeric string values
     */
    public static String[] splitNonNumericChars(final String value) {
        return PATTERN_NON_NUM.split(value);
    }

    /* --- common file name helper methods ------------------------------ */
    /**
     * Replace invalid file name characters (1..n) by the underscore character
     * @param value input value
     * @return string value
     */
    public static String replaceNonFileNameCharsByUnderscore(final String value) {
        return replaceNonFileNameChars(value, STRING_UNDERSCORE);
    }

    /**
     * Replace invalid file name characters (1..n) by the given replacement string
     * @param value input value
     * @param replaceBy replacement string
     * @return string value
     */
    public static String replaceNonFileNameChars(final String value, final String replaceBy) {
        return PATTERN_NON_FILE_NAME.matcher(value).replaceAll(replaceBy);
    }

    /* --- common helper methods ------------------------------ */
    /**
     * Replace carriage return characters by the given replacement string
     * @param value input value
     * @param replaceBy replacement string
     * @return string value
     */
    public static String replaceCR(final String value, final String replaceBy) {
        return PATTERN_CR.matcher(value).replaceAll(replaceBy);
    }

    /**
     * Remove any tag
     * @param value input value
     * @return string value
     */
    public static String removeTags(final String value) {
        return PATTERN_TAGS.matcher(value).replaceAll(STRING_EMPTY);
    }

    /**
     * Encode special characters to entities
     * @param src input string
     * @return encoded value
     */
    public static String encodeTagContent(final String src) {
        String out = PATTERN_AMP.matcher(src).replaceAll("&amp;"); // Character [&] (xml restriction)
        out = PATTERN_LT.matcher(out).replaceAll("&lt;"); // Character [<] (xml restriction)
        out = PATTERN_GT.matcher(out).replaceAll("&gt;"); // Character [>] (xml restriction)
        return out;
    }
}
