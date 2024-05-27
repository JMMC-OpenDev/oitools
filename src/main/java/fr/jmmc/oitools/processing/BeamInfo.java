/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.processing;

import fr.jmmc.jmcs.util.NumberUtils;

/**
 *
 * @author bourgesl
 */
public final class BeamInfo {

    // Principal components:
    /** major-axis Gaussian HW-HM in image plane (mas) */
    public final double rx;
    /** minor-axis Gaussian HW-HM in image plane (mas) */
    public final double ry;
    /** position-angle (convention north) (deg) */
    public final double angle;

    public BeamInfo(final double rx, final double ry, final double angle) {
        this.rx = rx;
        this.ry = ry;
        this.angle = angle;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 13 * hash + (int) (Double.doubleToLongBits(this.rx) ^ (Double.doubleToLongBits(this.rx) >>> 32));
        hash = 13 * hash + (int) (Double.doubleToLongBits(this.ry) ^ (Double.doubleToLongBits(this.ry) >>> 32));
        hash = 13 * hash + (int) (Double.doubleToLongBits(this.angle) ^ (Double.doubleToLongBits(this.angle) >>> 32));
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
        final BeamInfo other = (BeamInfo) obj;
        if (Double.doubleToLongBits(this.rx) != Double.doubleToLongBits(other.rx)) {
            return false;
        }
        if (Double.doubleToLongBits(this.ry) != Double.doubleToLongBits(other.ry)) {
            return false;
        }
        return Double.doubleToLongBits(this.angle) == Double.doubleToLongBits(other.angle);
    }

    @Override
    public String toString() {
        return "BeamInfo{" + "rx=" + rx + ", ry=" + ry + ", angle=" + angle + '}';
    }

    public String getDisplayString() {
        return "(rx = " + NumberUtils.trimTo3Digits(rx)
                + " mas, ry = " + NumberUtils.trimTo3Digits(ry)
                + " mas, angle = " + NumberUtils.trimTo2Digits(angle) + " deg)";
    }
}
