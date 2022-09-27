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
package fr.jmmc.oitools.util;

/**
 * Astronomical Coordinate utilities (distance ...)
 * @author bourgesl
 */
public final class CoordUtils {

    /** Specify the value of one arcsecond in degrees */
    public static final double ARCSEC_IN_DEGREES = (1d / 3600d);
    /** Specify the value of one arcsecond in degrees */
    public static final double DEG_IN_ARCSEC = 3600d;

    /**
     * Forbidden constructor : utility class
     */
    private CoordUtils() {
        super();
    }

    /**
     * Compute the distance between to ra/dec coordinates.
     *
     * @param raDeg1 first right acsension in degrees
     * @param decDeg1 first declinaison in degrees
     * @param raDeg2 second right acsension in degrees
     * @param decDeg2 second declinaison in degrees
     * @return distance in degrees
     */
    public static double computeDistanceInDegrees(final double raDeg1, final double decDeg1,
                                                  final double raDeg2, final double decDeg2) {

        if (Double.isNaN(raDeg1 + raDeg2 + decDeg1 + decDeg2)) {
            return Double.NaN;
        }

        /* Convert all the given angle from degrees to rad */
        final double ra1 = Math.toRadians(raDeg1);
        final double dec1 = Math.toRadians(decDeg1);

        final double ra2 = Math.toRadians(raDeg2);
        final double dec2 = Math.toRadians(decDeg2);

        /*
         * This implementation derives from Bob Chamberlain's contribution
         * to the comp.infosystems.gis FAQ; he cites
         * R.W.Sinnott, "Virtues of the Haversine", Sky and Telescope vol.68,
         * no.2, 1984, p159.
         */

 /* haversine formula: better precision than cosinus law */
        final double sd2 = Math.sin(0.5d * (dec2 - dec1));
        final double sr2 = Math.sin(0.5d * (ra2 - ra1));

        final double angle = sd2 * sd2 + sr2 * sr2 * Math.cos(dec1) * Math.cos(dec2);

        /* check angle ranges [0;1] */
        if (angle <= 0d) {
            return 0d;
        }
        if (angle < 1d) {
            return 2d * Math.toDegrees(Math.asin(Math.sqrt(angle)));
        }
        return 180d;
    }

}
