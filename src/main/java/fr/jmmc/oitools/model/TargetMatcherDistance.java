/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.model;

import fr.jmmc.jmcs.util.NumberUtils;
import fr.jmmc.oitools.util.CoordUtils;
import java.util.logging.Level;

/**
 * Matcher<Target> implementation based on distance
 * @author bourgesl
 */
public final class TargetMatcherDistance implements Matcher<Target> {

    /** Logger */
    private final static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(TargetMatcherDistance.class.getName());

    private final static String KEY = "target.matcher.dist";

    /** distance in degrees to consider same targets (default: 1 arcsec) */
    public final static double SAME_TARGET_DISTANCE;

    static {
        double distance = 1.0;

        final String dist = System.getProperty(KEY);
        if (dist != null) {
            try {
                distance = Double.parseDouble(dist);
            } catch (NumberFormatException nfe) {
                logger.log(Level.WARNING, "Invalid System property '{0}': {1}", new Object[]{KEY, System.getProperty(KEY)});
            }
        }
        SAME_TARGET_DISTANCE = distance;
    }

    /* members */
    /** separation in degrees */
    private double separation;

    public TargetMatcherDistance() {
        setSeparationInArcsec(SAME_TARGET_DISTANCE);
    }

    /**
     * Return the separation in degrees
     * @return separation in degrees
     */
    public double getSeparation() {
        return separation;
    }

    /**
     * Define the separation in degrees
     * @param separation separation in degrees
     * @return true if the separation is changed
     */
    public boolean setSeparation(final double separation) {
        if (this.separation != separation) {
            logger.log(Level.INFO, "Matcher<Target>: separation: {0} as", separation * CoordUtils.DEG_IN_ARCSEC);
            this.separation = separation;
            return true;
        }
        return false;
    }

    /**
     * Define the separation in degrees
     * @param separation separation in degrees
     * @return true if the separation is changed
     */
    public boolean setSeparationInArcsec(final double separation) {
        return setSeparation(separation * CoordUtils.ARCSEC_IN_DEGREES);
    }

    public static double parseSeparationInArcsec(final String separation) {
        try {
            return Double.parseDouble(separation);
        } catch (NumberFormatException nfe) {
            logger.log(Level.WARNING, "Invalid separation: {0} as", separation);
        }
        return Double.NaN;
    }

    @Override
    public boolean match(final Target src, final Target other) {
        if (src == other) {
            return true;
        }
        // only check ra/dec:
        final double distance = CoordUtils.computeDistanceInDegrees(
                src.getRaEp0(), src.getDecEp0(),
                other.getRaEp0(), other.getDecEp0());

        final boolean match = (distance <= separation);

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "match = {0} [{1} vs {2}] = {3} arcsec",
                    new Object[]{match, src.getTarget(), other.getTarget(),
                                 NumberUtils.trimTo3Digits(distance * CoordUtils.DEG_IN_ARCSEC)});
        }
        return match;
    }

}
