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
package fr.jmmc.oitools.meta;

import fr.jmmc.oitools.fits.FitsHDU;
import fr.jmmc.oitools.model.OIFitsChecker;
import fr.jmmc.oitools.model.Rule;
import java.util.logging.Level;

/**
 * This class describes a FITS keyword.
 * 
 * Notes :
 * - OIFits uses only 'A', 'I', 'D' types for keywords. Other types are not supported for keywords.
 * - Keyword units are only useful for XML representation of an OIFits file (not defined in FITS)
 *
 * @author bourgesl
 */
public class KeywordMeta extends CellMeta {

    /**
     * KeywordMeta class constructor
     *
     * @param name keyword name
     * @param desc keyword descriptive comment
     * @param dataType keyword data type
     */
    public KeywordMeta(final String name, final String desc, final Types dataType) {
        super(MetaType.KEYWORD, name, desc, dataType, false, NO_INT_VALUES, NO_STR_VALUES, Units.NO_UNIT);
    }

    /**
     * KeywordMeta class constructor
     *
     * @param name keyword name
     * @param desc keyword descriptive comment
     * @param dataType keyword data type
     * @param unit keyword unit
     */
    public KeywordMeta(final String name, final String desc, final Types dataType, final Units unit) {
        super(MetaType.KEYWORD, name, desc, dataType, false, NO_INT_VALUES, NO_STR_VALUES, unit);
    }

    /**
     * KeywordMeta class constructor
     *
     * @param name keyword name
     * @param desc keyword descriptive comment
     * @param dataType keyword data type
     * @param optional true if keyword is optional
     * @param acceptedValues possible values for keyword
     */
    public KeywordMeta(final String name, final String desc, final Types dataType, final boolean optional, final String[] acceptedValues) {
        super(MetaType.KEYWORD, name, desc, dataType, optional, NO_INT_VALUES, acceptedValues, Units.NO_UNIT);
    }

    /**
     * KeywordMeta class constructor
     *
     * @param name keyword name
     * @param desc keyword descriptive comment
     * @param dataType keyword data type
     * @param unit keyword unit
     * @param optional true if keyword is optional
     */
    public KeywordMeta(final String name, final String desc, final Types dataType, final Units unit, final boolean optional) {
        super(MetaType.KEYWORD, name, desc, dataType, optional, NO_INT_VALUES, NO_STR_VALUES, unit);
    }

    /**
     * KeywordMeta class constructor with integer possible values
     *
     * @param name keyword name
     * @param desc keyword descriptive comment
     * @param dataType keyword data type
     * @param acceptedValues integer possible values
     */
    public KeywordMeta(final String name, final String desc, final Types dataType, final short[] acceptedValues) {
        super(MetaType.KEYWORD, name, desc, dataType, false, acceptedValues, NO_STR_VALUES, Units.NO_UNIT);
    }

    /**
     * KeywordMeta class constructor with string possible values
     *
     * @param name keyword name
     * @param desc keyword descriptive comment
     * @param dataType keyword data type
     * @param acceptedValues string possible values
     */
    public KeywordMeta(final String name, final String desc, final Types dataType, final String[] acceptedValues) {
        super(MetaType.KEYWORD, name, desc, dataType, false, NO_INT_VALUES, acceptedValues, Units.NO_UNIT);
    }

    /**
     * Check if the given keyword value is valid.
     * @param hdu FitsHDU
     * @param value keyword value to check
     * @param checker checker component
     */
    public final void check(final FitsHDU hdu, final Object value, final OIFitsChecker checker) {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "check : {0} = {1}", new Object[]{getName(), value});
        }

        // Check type
        final Types kDataType = Types.getDataType(value.getClass());

        if (checker != null && !checker.isSkipFormat()) {
            hdu.checkKeywordFormat(checker, hdu, this, kDataType);
        }

        // Check accepted value
        if (kDataType == this.getDataType() || OIFitsChecker.isInspectRules()) {
            checkAcceptedValues(checker, hdu, value);
        }
    }

    /**
     * If any are mentioned, check keyword values are fair.
     * @param checker checker component
     * @param hdu FitsHDU
     * @param value keyword value to check
     */
    private void checkAcceptedValues(final OIFitsChecker checker, final FitsHDU hdu, final Object value) {
        final short[] intAcceptedValues = getIntAcceptedValues();

        if (intAcceptedValues.length != 0) {
            final short val = ((Number) value).shortValue();

            if (!OIFitsChecker.isInspectRules()) {
                for (int i = 0, len = intAcceptedValues.length; i < len; i++) {
                    if (val == intAcceptedValues[i]) {
                        return;
                    }
                }
            }
            // rule [GENERIC_KEYWORD_VAL_ACCEPTED_INT] check if the keyword value matches the 'accepted' values (integer)
            if (checker != null) {
                checker.ruleFailed(Rule.GENERIC_KEYWORD_VAL_ACCEPTED_INT, hdu, this.getName()).addKeywordValue(val, getIntAcceptedValuesAsString());

                if (OIFitsChecker.FIX_BAD_ACCEPTED_FOR_SINGLE_MATCH && (intAcceptedValues.length == 1)) {
                    // TODO: use FIX RULE to log change ...
                    hdu.setKeywordInt(this.getName(), intAcceptedValues[0]);
                }
            }
        }

        final String[] stringAcceptedValues = getStringAcceptedValues();

        if (stringAcceptedValues.length != 0) {
            final String val = (String) value;

            if (!OIFitsChecker.isInspectRules()) {
                for (int i = 0, len = stringAcceptedValues.length; i < len; i++) {
                    if (val.equals(stringAcceptedValues[i])) {
                        return;
                    }
                }
            }
            // rule [GENERIC_KEYWORD_VAL_ACCEPTED_STR] check if the keyword value matches the 'accepted' values (string)
            if (checker != null) {
                checker.ruleFailed(Rule.GENERIC_KEYWORD_VAL_ACCEPTED_STR, hdu, this.getName()).addKeywordValue(val, getStringAcceptedValuesAsString());

                if (OIFitsChecker.FIX_BAD_ACCEPTED_FOR_SINGLE_MATCH && (stringAcceptedValues.length == 1)) {
                    // TODO: use FIX RULE to log change ...
                    hdu.setKeyword(this.getName(), stringAcceptedValues[0]);
                }
            }
        }
    }
}
