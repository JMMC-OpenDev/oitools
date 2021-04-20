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
import static fr.jmmc.oitools.model.ModelBase.UNDEFINED_DBL;
import static fr.jmmc.oitools.model.ModelBase.UNDEFINED_INT;
import fr.jmmc.oitools.model.range.Range;
import java.util.Comparator;

/**
 * A value-type (immutable) representing a single backend (INSNAME) = 1 OI_Wavelength table
 * @author bourgesl
 */
public final class InstrumentMode {

    /** INSNAME prefix for GRAVITY */
    public final static String INSNAME_PREFIX_GRAVITY = "GRAVITY";

    public final static Matcher<InstrumentMode> MATCHER_INSNAME = new Matcher<InstrumentMode>() {

        @Override
        public boolean match(final InstrumentMode src, final InstrumentMode other) {
            if (src == other) {
                return true;
            }
            // Compare only INSNAME values:
            return ModelBase.areEquals(src.getInsName(), other.getInsName());
        }
    };

    public final static Matcher<InstrumentMode> MATCHER_LIKE = new Matcher<InstrumentMode>() {

        /** smallest precision on wavelength */
        public final static double LAMBDA_PREC_MAX = 1e-10;

        /** smallest precision on wavelength */
        public final static double LAMBDA_PREC_MIN = 1e-7;

        @Override
        public boolean match(final InstrumentMode src, final InstrumentMode other) {
            if (src == other) {
                return true;
            }
            // Compare all values:
            if (NumberUtils.compare(src.getNbChannels(), other.getNbChannels()) != 0) {
                return false;
            }

            // precision = 1/2 channel width ie min(eff_band)/2
            double prec = 0.5f * Math.min(src.getBandMin(), other.getBandMin());

            if (Double.isNaN(prec) || (prec < LAMBDA_PREC_MAX)) {
                prec = LAMBDA_PREC_MAX;
            } else if (prec > LAMBDA_PREC_MIN) {
                prec = LAMBDA_PREC_MIN;
            }

            // precision = 1e-10 ie 3 digits in nm:
            if (!NumberUtils.equals(src.getLambdaMin(), other.getLambdaMin(), prec)) {
                return false;
            }
            if (!NumberUtils.equals(src.getLambdaMax(), other.getLambdaMax(), prec)) {
                return false;
            }

            if (src.getInsName().startsWith(INSNAME_PREFIX_GRAVITY)) {
                // GRAVITY: do not match different polarizations (P1/P2 ...)
                // i.e. compare INSNAME values:
                return ModelBase.areEquals(src.getInsName(), other.getInsName());
            }
            return true;
        }
    };

    public final static Comparator<InstrumentMode> CMP_INS_MODE = new Comparator<InstrumentMode>() {
        @Override
        public int compare(final InstrumentMode i1, final InstrumentMode i2) {
            // NAME
            int cmp = String.CASE_INSENSITIVE_ORDER.compare(i1.getInsName(), i2.getInsName());
            if (cmp != 0) {
                return cmp;
            }
            // nb channels
            cmp = NumberUtils.compare(i1.getNbChannels(), i2.getNbChannels());
            if (cmp != 0) {
                return cmp;
            }
            cmp = Double.compare(i1.getResPower(), i2.getResPower());

            return cmp;
        }
    };

    public final static InstrumentMode UNDEFINED = new InstrumentMode(ModelBase.UNDEFINED, UNDEFINED_INT,
            new Range(UNDEFINED_DBL, UNDEFINED_DBL), UNDEFINED_DBL, UNDEFINED_DBL);

    /** members */
    // should add EFF_WAVE/EFF_BAND arrays ?
    private final String insName;
    private final int nbChannels;
    private final Range wavelengthRange;
    private final double resPower;
    private final double bandMin;
    // cached precomputed hashcode:
    private int hashcode = 0;

    public InstrumentMode(final String insName,
                          final int nbChannels,
                          final Range wavelengthRange,
                          final double resPower,
                          final double bandMin) {
        this.insName = insName;
        this.nbChannels = nbChannels;
        this.wavelengthRange = wavelengthRange;
        this.resPower = resPower;
        this.bandMin = bandMin;
    }

    public InstrumentMode(final InstrumentMode im, final String insName) {
        this(insName, im.getNbChannels(), im.getWavelengthRange(), im.getResPower(), im.getBandMin());
    }

    public String getInsName() {
        return insName;
    }

    public int getNbChannels() {
        return nbChannels;
    }

    public Range getWavelengthRange() {
        return wavelengthRange;
    }

    public double getLambdaMin() {
        return wavelengthRange.getMin();
    }

    public double getLambdaMax() {
        return wavelengthRange.getMax();
    }

    public double getResPower() {
        return resPower;
    }

    public double getBandMin() {
        return bandMin;
    }

    @Override
    public int hashCode() {
        if (hashcode != 0) {
            // use precomputed hash code (or equals 0 but low probability):
            return hashcode;
        }
        int hash = 7;
        hash = 59 * hash + (this.insName != null ? this.insName.hashCode() : 0);
        hash = 59 * hash + this.nbChannels;
        hash = 59 * hash + (int) (Double.doubleToLongBits(this.getLambdaMin()) ^ (Double.doubleToLongBits(this.getLambdaMin()) >>> 32));
        hash = 59 * hash + (int) (Double.doubleToLongBits(this.getLambdaMax()) ^ (Double.doubleToLongBits(this.getLambdaMax()) >>> 32));
        hash = 59 * hash + (int) (Double.doubleToLongBits(this.resPower) ^ (Double.doubleToLongBits(this.resPower) >>> 32));
        hash = 59 * hash + (int) (Double.doubleToLongBits(this.bandMin) ^ (Double.doubleToLongBits(this.bandMin) >>> 32));
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
        final InstrumentMode other = (InstrumentMode) obj;
        if (this.nbChannels != other.getNbChannels()) {
            return false;
        }
        if (Double.doubleToLongBits(this.getLambdaMin()) != Double.doubleToLongBits(other.getLambdaMin())) {
            return false;
        }
        if (Double.doubleToLongBits(this.getLambdaMax()) != Double.doubleToLongBits(other.getLambdaMax())) {
            return false;
        }
        if (Double.doubleToLongBits(this.resPower) != Double.doubleToLongBits(other.getResPower())) {
            return false;
        }
        if (Double.doubleToLongBits(this.bandMin) != Double.doubleToLongBits(other.getBandMin())) {
            return false;
        }
        if ((this.insName == null) ? (other.getInsName() != null) : !this.insName.equals(other.getInsName())) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "InstrumentMode{" + "insName=" + insName + ", nbChannels=" + nbChannels + ", lambdaMin=" + getLambdaMin() + ", lambdaMax=" + getLambdaMax()
                + ", resPower=" + resPower + ", bandMin=" + bandMin + '}';
    }

}
