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

import fr.jmmc.oitools.fits.FitsConstants;

/**
 * This enumeration provides minimal unit conversion to standard units (rad, m)
 * @author bourgesl
 */
public enum FitsUnit {

    /** undefined Units */
    NO_UNIT(""),
    /** angle expressed in radians */
    ANGLE_RAD("rad|radian|radians"),
    /** angle expressed in degrees */
    ANGLE_DEG("deg|degree|degrees", ANGLE_RAD, (Math.PI / 180.0)),
    /** angle expressed in arcminutes */
    ANGLE_ARCMIN("arcmin|arcmins", ANGLE_RAD, (Math.PI / 180.0) / 60.0),
    /** angle expressed in arcseconds */
    ANGLE_ARCSEC("arcsec|arcsecs", ANGLE_RAD, (Math.PI / 180.0) / 3600.0),
    /** angle expressed in milli-arcseconds */
    ANGLE_MILLI_ARCSEC("mas|milliarcsec", ANGLE_RAD, (Math.PI / 180.0) / 3600000.0),
    /** wavelength expressed in meters */
    WAVELENGTH_METER("m|meter|meters"),
    /** wavelength expressed in micro meters */
    WAVELENGTH_MICRO_METER("micron|microns|micrometer|micrometers", WAVELENGTH_METER, 1e-6),
    /** wavelength expressed in nano meters */
    WAVELENGTH_NANO_METER("nm|nanometer|nanometers", WAVELENGTH_METER, 1e-9),
    /** wavelength expressed in hertz */
    WAVELENGTH_HERTZ("hz|hertz", WAVELENGTH_METER, FitsConstants.C_LIGHT, -1.0, Double.NaN),
    /** wavelength expressed in kilohertz */
    WAVELENGTH_KILOHERTZ("khz|kilohertz", WAVELENGTH_METER, FitsConstants.C_LIGHT, -1.0, 1e3),
    /** wavelength expressed in megahertz */
    WAVELENGTH_MEGAHERTZ("mhz|megahertz", WAVELENGTH_METER, FitsConstants.C_LIGHT, -1.0, 1e6),
    /** wavelength expressed in gigahertz */
    WAVELENGTH_GIGAHERTZ("ghz|gigahertz", WAVELENGTH_METER, FitsConstants.C_LIGHT, -1.0, 1e9);

    /**
     * Unit parsing
     * @param unit unit to parse
     * @return matching FitsUnit according to its tokens
     * @throws IllegalArgumentException if unsupported unit
     */
    public static FitsUnit parseUnit(final String unit) throws IllegalArgumentException {
        if (unit == null || unit.length() == 0) {
            return NO_UNIT;
        }
        for (FitsUnit u : FitsUnit.values()) {
            if (u.checkTokens(unit)) {
                return u;
            }
        }
        throw new IllegalArgumentException("Unsupported unit: [" + unit + "] !");
    }

    /**
     * Return the appropriate FitsUnit to convert the given angle in radians
     * @param angle angle in radians
     * @return a FitsUnit for angles
     */
    public static FitsUnit getAngleUnit(final double angle) {
        if (Double.isNaN(angle)) {
            return FitsUnit.ANGLE_RAD;
        }
        double tmp = Math.toDegrees(angle);
        if (tmp > 1e-1d) {
            return FitsUnit.ANGLE_DEG;
        }
        tmp *= 60d;
        if (tmp > 1e-1d) {
            return FitsUnit.ANGLE_ARCMIN;
        }
        tmp *= 60d;
        if (tmp > 1e-1d) {
            return FitsUnit.ANGLE_ARCSEC;
        }
        return FitsUnit.ANGLE_MILLI_ARCSEC;
    }

    /* members */
    /** string representation separated by '|' */
    private final String representation;
    /** representation array */
    private final String[] tokens;
    /** unit reference */
    private final FitsUnit reference;
    /** conversion factor to the unit reference factor*(value) */
    private final double factor;
    /** conversion power factor*(value^power) to the unit reference */
    private final double power;
    /** conversion factor of the power value input^(power * factor_power) to the unit reference */
    private final double factor_power;

    /**
     * Custom constructor
     * @param allowedValues string containing several representation separated by '|'
     */
    private FitsUnit(final String allowedValues) {
        this(allowedValues, null, Double.NaN, Double.NaN, Double.NaN);
    }

    /**
     * Custom constructor
     * @param allowedValues string containing several representation separated by '|'
     * @param reference unit reference
     * @param factor conversion factor to the unit reference
     */
    private FitsUnit(final String allowedValues, final FitsUnit reference, final double factor) {
        this(allowedValues, reference, factor, Double.NaN, Double.NaN);
    }

    /**
     * Custom constructor
     * @param allowedValues string containing several representation separated by '|'
     * @param reference unit reference
     * @param factor conversion factor to the unit reference
     * @param power
     * @param factor_power
     */
    private FitsUnit(final String allowedValues, final FitsUnit reference, final double factor, final double power, final double factor_power) {
        this.representation = allowedValues;
        this.tokens = this.representation.split("\\|");
        this.reference = reference;
        this.factor = factor;
        this.power = power;
        this.factor_power = factor_power;
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
     * Return the unit reference
     * @return unit reference
     */
    public final FitsUnit getReference() {
        return this.reference;
    }

    /**
     * Convert the given value expressed in this unit to the given unit
     * @param value value to convert expressed in this unit
     * @param unit unit to convert into
     * @return converted value or given value if no conversion
     * @throws IllegalArgumentException if unit conversion is not allowed
     */
    public double convert(final double value, final FitsUnit unit) throws IllegalArgumentException {
        // convert to associated reference
        if (this.reference != null) {
            if (this.reference == unit) {
                // conversion is possible:
                double output = value;

                if (!Double.isNaN(this.power)) {
                    if (!Double.isNaN(this.factor_power)) {
                        output *= this.factor_power;
                    }
                    output = Math.pow(output, this.power);
                }

                return this.factor * output;
            }
            throw new IllegalArgumentException("Unit conversion not allowed from [" + getStandardRepresentation()
                    + "] to [" + unit.getStandardRepresentation() + "] !");
        }
        // invert conversion if final unit has this unit as reference
        if (unit.reference != null) {
            if (unit.reference == this) {
                // conversion is possible:
                double output = value;

                if (!Double.isNaN(unit.factor_power)) {
                    output /= unit.factor_power;
                    if (!Double.isNaN(unit.power)) {
                        output = Math.pow(output, 1 / unit.power);
                    }
                }

                return output / unit.factor;
            }
            throw new IllegalArgumentException("Unit conversion not allowed from [" + getStandardRepresentation()
                    + "] to [" + unit.getStandardRepresentation() + "] !");
        }

        // or no conversion:
        return value;
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
}
