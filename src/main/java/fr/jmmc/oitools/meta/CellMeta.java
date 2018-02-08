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

import fr.jmmc.oitools.model.OIFitsChecker;
import fr.jmmc.oitools.model.Rule;
import java.util.HashSet;
import java.util.Set;

/**
 * Base class to describe table keyword and column.
 */
public class CellMeta {

    /* constants */
    /** Logger associated to meta model classes */
    protected final static java.util.logging.Logger logger = java.util.logging.Logger.getLogger("fr.jmmc.oitools.meta");
    /** no integer accepted values */
    public final static short[] NO_INT_VALUES = new short[0];
    /** no string accepted values */
    public final static String[] NO_STR_VALUES = new String[0];

    /** keyword or column meta type */
    public enum MetaType {

        /** keyword meta data */
        KEYWORD,
        /** column meta data */
        COLUMN;
    }

    /* members */
    /** Keyword or column type */
    private final MetaType type;
    /** Keyword/column name */
    private final String name;
    /** Keyword/column descriptive comment */
    private final String desc;
    /** Keyword/column data type */
    private final Types dataType;
    /** Keyword/column flag optional */
    final boolean optional;
    /** Keyword/column unit */
    private final Units unit;
    /** Stored integer possible values for column/keyword */
    private final short[] acceptedValuesInteger;
    /** Stored string possible values for column/keyword */
    private final String[] acceptedValuesString;
    /** Set to use when in the situation to harvest the rules. It is not created if one is not in isInspectRules() */
    private final Set<Rule> applyRules = OIFitsChecker.isInspectRules() ? new HashSet<Rule>() : null;

    /**
     * Get the applyRules for DataModel
     * @return applyRules
     */
    public Set<Rule> getApplyRules() {
        return applyRules;
    }

    /** 
     * CellMeta class protected constructor
     *
     * @param type keyword or column type
     * @param name keyword/column name
     * @param desc keyword/column descriptive comment
     * @param dataType keyword/column data type
     * @param optional keyword/column optional
     * @param intAcceptedValues integer possible values for column/keyword
     * @param stringAcceptedValues string possible values for column/keyword
     * @param unit keyword/column unit
     */
    protected CellMeta(final MetaType type, final String name, final String desc,
                       final Types dataType, final boolean optional,
                       final short[] intAcceptedValues,
                       final String[] stringAcceptedValues, final Units unit) {

        if (name == null || name.isEmpty()) {
            throw new IllegalStateException("name can not be null");
        }
        if (intAcceptedValues == null) {
            throw new IllegalStateException("intAcceptedValues can not be null");
        }
        if (stringAcceptedValues == null) {
            throw new IllegalStateException("stringAcceptedValues can not be null");
        }
        if (unit == null) {
            throw new IllegalStateException("unit can not be null");
        }
        if (type == MetaType.KEYWORD) {
            // ensure keyword data type is (CHAR, INT, DOUBLE, LOGICAL)
            if ((dataType == Types.TYPE_SHORT)
                    || (dataType == Types.TYPE_REAL)
                    || (dataType == Types.TYPE_COMPLEX)) {
                throw new IllegalStateException("Invalid datatype [" + dataType + "] for keyword " + name);
            }
        }
        this.type = type;
        this.name = name;
        this.desc = desc;
        this.dataType = dataType;
        this.optional = optional;
        this.acceptedValuesInteger = intAcceptedValues;
        this.acceptedValuesString = stringAcceptedValues;
        this.unit = unit;
    }

    /**
     * Return true if the column is optional
     * Can be overriden
     * @return true if the column is optional
     */
    public boolean isOptional() {
        return optional;
    }

    @Override
    public final boolean equals(final Object anObject) {
        if (anObject == null) {
            return false;
        }
        if (getClass() != anObject.getClass()) {
            return false;
        }
        final CellMeta other = (CellMeta) anObject;
        if ((this.name == null) ? (other.getName() != null) : !this.name.equals(other.getName())) {
            return false;
        }
        return true;
    }

    @Override
    public final int hashCode() {
        if (this.name != null) {
            return this.name.hashCode();
        }
        return 3;
    }

    @Override
    public final String toString() {
        final StringBuilder sb = new StringBuilder(64);
        switch (getMetaType()) {
            case KEYWORD:
                sb.append("KEYWORD ");
                break;
            case COLUMN:
                sb.append("COLUMN ");
                break;

            default:
        }
        sb.append('\'').append(getName()).append("' ");
        sb.append('[').append(getDataType()).append("] ");
        if (getUnits() != Units.NO_UNIT) {
            sb.append('(').append(getUnit()).append(')');
        }
        return sb.toString();
    }

    /**
     * Get meta type.
     *
     * @return the meta type.
     */
    public final MetaType getMetaType() {
        return this.type;
    }

    /**
     * Get name.
     *
     * @return the name.
     */
    public final String getName() {
        return this.name;
    }

    /**
     * Return cell description.
     *
     * @return the cell description.
     */
    public final String getDescription() {
        return this.desc;
    }

    /**
     * Get type.
     *
     * @return the type.
     */
    public final Types getDataType() {
        return this.dataType;
    }

    /**
     * Get type.
     *
     * @return the type.
     */
    public final char getType() {
        return this.dataType.getRepresentation();
    }

    /**
     * Return units.
     *
     * @return the units.
     */
    public final Units getUnits() {
        return this.unit;
    }

    /**
     * Return all accepted cell units.
     *
     * @return all accepted cell units.
     */
    public final String getUnit() {
        return this.unit.getRepresentation();
    }

    /**
     * Return integer possible values for column/keyword
     * Can be override to represent cross - references
     * @return integer possible values
     */
    public short[] getIntAcceptedValues() {
        return acceptedValuesInteger;
    }

    /**
     * Return a string representation of the integer possible values separated by '|'
     * @return string representation of the integer possible values
     */
    protected final String getIntAcceptedValuesAsString() {
        final short[] intAcceptedValues = getIntAcceptedValues();

        final StringBuilder sb = new StringBuilder(32);

        for (int i = 0, len = intAcceptedValues.length; i < len; i++) {
            if (i > 0) {
                sb.append('|');
            }
            sb.append(intAcceptedValues[i]);
        }

        return sb.toString();
    }

    /**
     * Return string possible values for column/keyword
     * Can be override to represent cross - references
     * @return string possible values for column/keyword
     */
    public String[] getStringAcceptedValues() {
        return acceptedValuesString;
    }

    /**
     * Return a string representation of the string possible values separated by '|'
     * @return string representation of the string possible values
     */
    protected final String getStringAcceptedValuesAsString() {
        final String[] stringAcceptedValues = getStringAcceptedValues();

        final StringBuilder sb = new StringBuilder(32);

        for (int i = 0; i < stringAcceptedValues.length; i++) {
            if (i > 0) {
                sb.append('|');
            }
            sb.append(stringAcceptedValues[i]);
        }

        return sb.toString();
    }

    /**
     * Unit analysis.
     *
     * @param unit unit to check
     * @return true if input unit is the same as what was defined, false
     * otherwise.
     */
    public final boolean checkUnit(final String unit) {
        return this.unit == Units.parseUnit(unit);
    }
}
/*___oOo___*/
