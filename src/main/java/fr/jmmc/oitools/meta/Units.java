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

/**
 * This class describes allowed units in the OIFits standard :
 *      - noUnit             no Unit associated
 *      - unitInMeters       must be 'm' or 'meters'
 *      - unitInDegrees      must be 'deg' or 'degrees'
 *      - unitInSeconds      must be 's', 'sec' or 'seconds'
 *      - unitInMJD          must be 'day'
 *      - unitInYears        must be 'yr', 'year' or 'years'
 *      - unitInMetersPerSecond
 *                           must be 'm/s', 'm / s', 'meters per second',
 *                           'meters/second', 'meters / second'
 *      - unitInArcsec       must be 'arcsec', 'as', 'arcsecond' or 'arcseconds'
 *      - unitInDegreesPerYear
 *                           must be 'deg/yr', 'deg/year', 'deg / year' or
 *                           'deg / yr'
 *      - unitInHour         must be 'h', 'hour' or 'hours'
 * @author bourgesl
 */
public class Units {

    /* CONSTANT Units (shared among keyword / columns) */
    /** undefined Units */
    public final static Units NO_UNIT = new Units("NO_UNIT", "");
    /** Units are expressed in meters */
    public final static Units UNIT_METER = new Units("UNIT_METER", "m|meter|meters");
    /** Units are expressed in degrees */
    public final static Units UNIT_DEGREE = new Units("UNIT_DEGREE", "deg|degree|degrees");
    /** Units are expressed in seconds */
    public final static Units UNIT_SECOND = new Units("UNIT_SECOND", "s|sec|second|seconds");
    /** Units are expressed in julian day */
    public final static Units UNIT_MJD = new Units("UNIT_MJD", "day|days");
    /** Units are expressed in years */
    public final static Units UNIT_YEAR = new Units("UNIT_YEAR", "yr|year|years");
    /** Units are expressed in meters per second */
    public final static Units UNIT_METER_PER_SECOND = new Units("UNIT_METER_PER_SECOND", "m/s|m / s|meter per second|meters per second|meter/second|meters/second|meter / second|meters / second");
    /** Units are expressed in arcsec */
    public final static Units UNIT_ARCSEC = new Units("UNIT_ARCSEC", "arcsec|as|arcsecond|arcseconds");
    /** Units are expressed in degrees per year */
    public final static Units UNIT_DEGREE_PER_YEAR = new Units("UNIT_DEGREE_PER_YEAR", "deg/yr|deg / yr|degree/yr|degree / yr|degrees/yr|degrees / yr|deg/year|deg / year|degree/year|degree / year|degrees/year|degrees / year");
    /** Units are expressed in hours */
    public final static Units UNIT_HOUR = new Units("UNIT_HOUR", "h|hour|hours");

    /* members */
    /** units name */
    private final String name;
    /** string representation separated by '|' */
    private String representation;
    /** representation array */
    private String[] tokens;

    /**
     * Protected constructor
     * @param name name parameter
     * @param allowedValues string containing several representation separated by '|'
     */
    protected Units(final String name, final String allowedValues) {
        this.name = name;
        set(allowedValues);
    }

    /**
     * Protected method to split information between '|' and create tokens
     * @param allowedValues string containing several representation separated by '|'
     */
    protected final void set(final String allowedValues) {
        this.representation = allowedValues;
        this.tokens = allowedValues.split("\\|");
    }

    /**
     * Return the unit representations
     * @return string containing several representation separated by '|'
     */
    public final String getRepresentation() {
        return representation;
    }

    /**
     * Return the standard unit representation i.e. the first token
     * @return standard unit representation or ""
     */
    public final String getStandardRepresentation() {
        if (tokens != null && tokens.length > 0) {
            return tokens[0];
        }
        return "";
    }

    /**
     * Check if the given unit is present in the tokens
     * @param unit unit to check
     * @return true if the given unit is present in the tokens
     */
    private boolean checkTokens(final String unit) {
        for (String token : tokens) {
            if (token.equalsIgnoreCase(unit)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Unit analysis.
     *
     * @param unit unit to check
     * @return matching Units according to its tokens or null if no unit matches
     */
    public static Units parseUnit(final String unit) {
        if (unit == null || unit.length() == 0) {
            return NO_UNIT;
        } else if (UNIT_METER.checkTokens(unit)) {
            return UNIT_METER;
        } else if (UNIT_DEGREE.checkTokens(unit)) {
            return UNIT_DEGREE;
        } else if (UNIT_SECOND.checkTokens(unit)) {
            return UNIT_SECOND;
        } else if (UNIT_MJD.checkTokens(unit)) {
            return UNIT_MJD;
        } else if (UNIT_YEAR.checkTokens(unit)) {
            return UNIT_YEAR;
        } else if (UNIT_METER_PER_SECOND.checkTokens(unit)) {
            return UNIT_METER_PER_SECOND;
        } else if (UNIT_ARCSEC.checkTokens(unit)) {
            return UNIT_ARCSEC;
        } else if (UNIT_DEGREE_PER_YEAR.checkTokens(unit)) {
            return UNIT_DEGREE_PER_YEAR;
        } else if (UNIT_HOUR.checkTokens(unit)) {
            return UNIT_HOUR;
        }

        return null;
    }

    /**
     * Returns a string representation of name 
     * @return the name parameter
     */
    @Override
    public String toString() {
        return name;
    }

}
