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
package fr.jmmc.oitools.model;

import fr.jmmc.jmcs.util.NumberUtils;
import fr.jmmc.oitools.util.CoordUtils;
import java.util.Comparator;
import java.util.logging.Level;

/**
 * A value-type (immutable) representing a single target in any OI_Target table
 * @author bourgesl
 */
public final class Target {

    /** Logger */
    final static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Target.class.getName());

    public final static Matcher<Target> MATCHER_NAME = new Matcher<Target>() {

        @Override
        public boolean match(final Target src, final Target other) {
            if (src == other) {
                return true;
            }
            // Compare only NAME values:
            return ModelBase.areEquals(src.getTarget(), other.getTarget());
        }
    };

    public final static Matcher<Target> MATCHER_LIKE = new Matcher<Target>() {

        /** distance in degrees to consider same targets = 1 arcsecs */
        public final static double SAME_TARGET_DISTANCE = 1d * CoordUtils.ARCSEC_IN_DEGREES;

        @Override
        public boolean match(final Target src, final Target other) {
            if (src == other) {
                return true;
            }
            // only check ra/dec:
            final double distance = CoordUtils.computeDistanceInDegrees(src.getRaEp0(), src.getDecEp0(),
                    other.getRaEp0(), other.getDecEp0());

            final boolean match = (distance <= SAME_TARGET_DISTANCE);

            if (match) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, "match [{0} vs {1}] = {2} arcsec",
                            new Object[]{src.getTarget(), other.getTarget(),
                                         NumberUtils.trimTo3Digits(distance * CoordUtils.DEG_IN_ARCSEC)});
                }
            }
            return match;
        }
    };

    public static final Comparator<Target> CMP_TARGET = new Comparator<Target>() {
        @Override
        public int compare(final Target t1, final Target t2) {
            return String.CASE_INSENSITIVE_ORDER.compare(t1.getTarget(), t2.getTarget());
        }
    };

    public final static Target UNDEFINED = new Target("UNDEFINED", Double.NaN, Double.NaN,
            Float.NaN, Double.NaN, Double.NaN,
            Double.NaN, "", "", Double.NaN, Double.NaN, Double.NaN, Double.NaN,
            Float.NaN, Float.NaN, "");

    /** members */
    private final String target;
    private final double raEp0;
    private final double decEp0;
    private final float equinox;
    private final double raErr;
    private final double decErr;
    private final double sysvel;
    private final String velTyp;
    private final String velDef;
    private final double pmRa;
    private final double pmDec;
    private final double pmRaErr;
    private final double pmDecErr;
    private final float parallax;
    private final float paraErr;
    private final String specTyp;
    // cached precomputed hashcode:
    private int hashcode = 0;

    public Target(final String target,
                  final double raEp0,
                  final double decEp0,
                  final float equinox,
                  final double raErr,
                  final double decErr,
                  final double sysvel,
                  final String velTyp,
                  final String velDef,
                  final double pmRa,
                  final double pmDec,
                  final double pmRaErr,
                  final double pmDecErr,
                  final float parallax,
                  final float paraErr,
                  final String specTyp) {
        this.target = target;
        this.raEp0 = raEp0;
        this.decEp0 = decEp0;
        this.equinox = equinox;
        this.raErr = raErr;
        this.decErr = decErr;
        this.sysvel = sysvel;
        this.velTyp = velTyp;
        this.velDef = velDef;
        this.pmRa = pmRa;
        this.pmDec = pmDec;
        this.pmRaErr = pmRaErr;
        this.pmDecErr = pmDecErr;
        this.parallax = parallax;
        this.paraErr = paraErr;
        this.specTyp = specTyp;
    }

    public Target(final Target t) {
        this(t.getTarget(), t.getRaEp0(), t.getDecEp0(),
                t.getEquinox(), t.getRaErr(), t.getDecErr(),
                t.getSysVel(), t.getVelTyp(), t.getVelDef(),
                t.getPmRa(), t.getPmDec(),
                t.getPmRaErr(), t.getPmDecErr(),
                t.getParallax(), t.getParaErr(),
                t.getSpecTyp());
    }

    /**
     * Get TARGET value.
     * 
     * @return TARGET
     */
    public String getTarget() {
        return target;
    }

    /**
     * Get RAEP0 value.
     * @return RAEP0.
     */
    public double getRaEp0() {
        return raEp0;
    }

    /**
     * Get DECEP0 value.
     * @return DECEP0.
     */
    public double getDecEp0() {
        return decEp0;
    }

    /**
     * Get EQUINOX value.
     * @return EQUINOX.
     */
    public float getEquinox() {
        return equinox;
    }

    /**
     * Get RA_ERR value.
     * @return RA_ERR.
     */
    public double getRaErr() {
        return raErr;
    }

    /**
     * Get DEC_ERR value.
     * @return DEC_ERR.
     */
    public double getDecErr() {
        return decErr;
    }

    /**
     * Get SYSVEL value.
     * @return SYSVEL.
     */
    public double getSysVel() {
        return sysvel;
    }

    /**
     * Get VELTYP value.
     * @return VELTYP
     */
    public String getVelTyp() {
        return velTyp;
    }

    /**
     * Get VELDEF value.
     * @return VELDEF
     */
    public String getVelDef() {
        return velDef;
    }

    /**
     * Get PMRA value.
     * @return PMRA.
     */
    public double getPmRa() {
        return pmRa;
    }

    /**
     * Get PMDEC value.
     * @return PMDEC.
     */
    public double getPmDec() {
        return pmDec;
    }

    /**
     * Get PMRA_ERR value.
     * @return PMRA_ERR.
     */
    public double getPmRaErr() {
        return pmRaErr;
    }

    /**
     * Get PMDEC_ERR value.
     * @return PMDEC_ERR.
     */
    public double getPmDecErr() {
        return pmDecErr;
    }

    /**
     * Get PARALLAX value.
     * @return PARALLAX.
     */
    public float getParallax() {
        return parallax;
    }

    /**
     * Get PARA_ERR value.
     * @return PARA_ERR.
     */
    public float getParaErr() {
        return paraErr;
    }

    /**
     * Get SPECTYP value.
     * @return SPECTYP
     */
    public String getSpecTyp() {
        return specTyp;
    }

    @Override
    public int hashCode() {
        if (hashcode != 0) {
            // use precomputed hash code (or equals 0 but low probability):
            return hashcode;
        }
        int hash = 7;
        hash = 11 * hash + (this.target != null ? this.target.hashCode() : 0);
        hash = 11 * hash + (int) (Double.doubleToLongBits(this.raEp0) ^ (Double.doubleToLongBits(this.raEp0) >>> 32));
        hash = 11 * hash + (int) (Double.doubleToLongBits(this.decEp0) ^ (Double.doubleToLongBits(this.decEp0) >>> 32));
        hash = 11 * hash + Float.floatToIntBits(this.equinox);
        hash = 11 * hash + (int) (Double.doubleToLongBits(this.raErr) ^ (Double.doubleToLongBits(this.raErr) >>> 32));
        hash = 11 * hash + (int) (Double.doubleToLongBits(this.decErr) ^ (Double.doubleToLongBits(this.decErr) >>> 32));
        hash = 11 * hash + (int) (Double.doubleToLongBits(this.sysvel) ^ (Double.doubleToLongBits(this.sysvel) >>> 32));
        hash = 11 * hash + (this.velTyp != null ? this.velTyp.hashCode() : 0);
        hash = 11 * hash + (this.velDef != null ? this.velDef.hashCode() : 0);
        hash = 11 * hash + (int) (Double.doubleToLongBits(this.pmRa) ^ (Double.doubleToLongBits(this.pmRa) >>> 32));
        hash = 11 * hash + (int) (Double.doubleToLongBits(this.pmDec) ^ (Double.doubleToLongBits(this.pmDec) >>> 32));
        hash = 11 * hash + (int) (Double.doubleToLongBits(this.pmRaErr) ^ (Double.doubleToLongBits(this.pmRaErr) >>> 32));
        hash = 11 * hash + (int) (Double.doubleToLongBits(this.pmDecErr) ^ (Double.doubleToLongBits(this.pmDecErr) >>> 32));
        hash = 11 * hash + Float.floatToIntBits(this.parallax);
        hash = 11 * hash + Float.floatToIntBits(this.paraErr);
        hash = 11 * hash + (this.specTyp != null ? this.specTyp.hashCode() : 0);
        // cache hash code:
        hashcode = hash;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Target other = (Target) obj;
        if (Double.doubleToLongBits(this.raEp0) != Double.doubleToLongBits(other.getRaEp0())) {
            return false;
        }
        if (Double.doubleToLongBits(this.decEp0) != Double.doubleToLongBits(other.getDecEp0())) {
            return false;
        }
        if (Float.floatToIntBits(this.equinox) != Float.floatToIntBits(other.getEquinox())) {
            return false;
        }
        if (Double.doubleToLongBits(this.raErr) != Double.doubleToLongBits(other.getRaErr())) {
            return false;
        }
        if (Double.doubleToLongBits(this.decErr) != Double.doubleToLongBits(other.getDecErr())) {
            return false;
        }
        if (Double.doubleToLongBits(this.sysvel) != Double.doubleToLongBits(other.getSysVel())) {
            return false;
        }
        if (Double.doubleToLongBits(this.pmRa) != Double.doubleToLongBits(other.getPmRa())) {
            return false;
        }
        if (Double.doubleToLongBits(this.pmDec) != Double.doubleToLongBits(other.getPmDec())) {
            return false;
        }
        if (Double.doubleToLongBits(this.pmRaErr) != Double.doubleToLongBits(other.getPmRaErr())) {
            return false;
        }
        if (Double.doubleToLongBits(this.pmDecErr) != Double.doubleToLongBits(other.getPmDecErr())) {
            return false;
        }
        if (Float.floatToIntBits(this.parallax) != Float.floatToIntBits(other.getParallax())) {
            return false;
        }
        if (Float.floatToIntBits(this.paraErr) != Float.floatToIntBits(other.getParaErr())) {
            return false;
        }
        if ((this.target == null) ? (other.getTarget() != null) : !this.target.equals(other.getTarget())) {
            return false;
        }
        if ((this.velTyp == null) ? (other.getVelTyp() != null) : !this.velTyp.equals(other.getVelTyp())) {
            return false;
        }
        if ((this.velDef == null) ? (other.getVelDef() != null) : !this.velDef.equals(other.getVelDef())) {
            return false;
        }
        if ((this.specTyp == null) ? (other.getSpecTyp() != null) : !this.specTyp.equals(other.getSpecTyp())) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Target{" + "target=" + target + ", raEp0=" + raEp0 + ", decEp0=" + decEp0 + ", equinox=" + equinox
                + ", raErr=" + raErr + ", decErr=" + decErr + ", sysvel=" + sysvel
                + ", velTyp=" + velTyp + ", velDef=" + velDef + ", pmRa=" + pmRa + ", pmDec=" + pmDec
                + ", pmRaErr=" + pmRaErr + ", pmDecErr=" + pmDecErr + ", parallax=" + parallax + ", paraErr=" + paraErr
                + ", specTyp=" + specTyp + '}';
    }

}
