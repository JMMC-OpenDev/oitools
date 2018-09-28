/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.model;

/**
 * Special math functions used with JEL evaluator
 * @author bourgesl
 */
public final class JELFunctions {

    public static double distanceAngle(final double a1, final double a2) {
        final double delta = a1 - a2;
        if (Double.isNaN(delta)) {
            return Double.NaN;
        }
        if (delta > Math.PI) {
            return delta - (2.0 * Math.PI);
        } else if (delta < -Math.PI) {
            return delta + (2.0 * Math.PI);
        }
        return delta;
    }

}
