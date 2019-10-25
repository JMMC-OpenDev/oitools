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
package fr.jmmc.oitools.test;

import fr.jmmc.oitools.fits.FitsConstants;
import fr.jmmc.oitools.image.FitsUnit;

/**
 * Test FitsUnit conversions
 * @author bourgesl, mellag
 */
public class FitsUnitTest {

    public static void main(String[] args) {
        test(90.0, "deg", FitsUnit.ANGLE_RAD, Math.PI / 2);
        test(1.0, "mas", FitsUnit.ANGLE_DEG, (1.0 * (Math.PI / 180.0) / 3600000.0) / (Math.PI / 180.0));
        test(1.0, "arcsec", FitsUnit.ANGLE_MILLI_ARCSEC, 1e3);
        // Hertz:
        test(1, "hz", FitsUnit.WAVELENGTH_METER, FitsConstants.C_LIGHT);
        test(1, "ghz", FitsUnit.WAVELENGTH_METER, 1e-9 * FitsConstants.C_LIGHT);
        test(1e14, "hz", FitsUnit.WAVELENGTH_METER, 1e-14 * FitsConstants.C_LIGHT);

        test(Math.PI / 2, "rad", FitsUnit.ANGLE_DEG, 90.0);
        test(FitsConstants.C_LIGHT, "m", FitsUnit.WAVELENGTH_HERTZ, 1);

    }

    private static void test(double value, final String unitString, final FitsUnit unitRef, final double expected) {
        final FitsUnit unit = FitsUnit.parseUnit(unitString);

        final double result = unit.convert(value, unitRef);

        if (Math.abs(result - expected) > 1e-6) {
            throw new IllegalStateException("Invalid result: " + result + " expected: " + expected);
        }
        System.out.println("Value[" + value + "] Unit[" + unit.getStandardRepresentation() + "] converted to Unit[" + unitRef.getStandardRepresentation() + "] = " + result);
    }
}
