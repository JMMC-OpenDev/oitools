/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.model;

import fr.jmmc.jmcs.util.NumberUtils;
import fr.jmmc.jmcs.util.ObjectUtils;
import java.util.Objects;

/**
 * This class represents the Index key type ie (target UID, instrument mode UID, mjd, ucoord, vcoord),
 * to group related data. 
 * note: InstrumentMode imply channels are aligned (0..n) => no wavelength nor channel info.
 * @author bourgesl
 */
public final class IndexKeyUV implements IndexKey {

    // members
    final String targetUID;
    final String insModeUID;
    final double mjd;
    final double ucoord;
    final double vcoord;

    @SuppressWarnings("AssignmentToMethodParameter")
    IndexKeyUV(final String targetUID, final String insModeUID, final double mjd, double ucoord, double vcoord) {
        // always defined (UNDEFINED if null):
        this.targetUID = targetUID;
        this.insModeUID = insModeUID;
        this.mjd = mjd;
        /* use convention: ucoord >=0 (like mira, re-orient the coordinates if needed) */
        if ((ucoord < 0.0) || (Double.isNaN(ucoord) && (vcoord < 0.0))) {
            ucoord = -ucoord;
            vcoord = -vcoord;
        }
        this.ucoord = ucoord;
        this.vcoord = vcoord;
    }

    /**
     * Used by TreeMap (must be consistent with equals)
     * @param obj other instance not null
     */
    @Override
    public int compareTo(final IndexKey obj) {
        final IndexKeyUV other = (IndexKeyUV) obj;
        int res = this.targetUID.compareTo(other.targetUID);
        if (res == 0) {
            res = this.insModeUID.compareTo(other.insModeUID);
            if (res == 0) {
                res = Double.compare(this.mjd, other.mjd);
                if (res == 0) {
                    res = Double.compare(this.ucoord, other.ucoord);
                    if (res == 0) {
                        res = Double.compare(this.vcoord, other.vcoord);
                    }
                }
            }
        }
        return res;
    }

    /**
     * Unused, only for HashMap
     * @return hash code
     */
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + Objects.hashCode(this.targetUID);
        hash = 29 * hash + Objects.hashCode(this.insModeUID);
        hash = 29 * hash + (int) (Double.doubleToLongBits(this.mjd) ^ (Double.doubleToLongBits(this.mjd) >>> 32));
        hash = 29 * hash + (int) (Double.doubleToLongBits(this.ucoord) ^ (Double.doubleToLongBits(this.ucoord) >>> 32));
        hash = 29 * hash + (int) (Double.doubleToLongBits(this.vcoord) ^ (Double.doubleToLongBits(this.vcoord) >>> 32));
        return hash;
    }

    /**
     * Used by Map (must be consistent with hashCode/compareTo)
     * @param obj other instance or null
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IndexKeyUV other = (IndexKeyUV) obj;
        if (!Objects.equals(this.targetUID, other.targetUID)) {
            return false;
        }
        if (!Objects.equals(this.insModeUID, other.insModeUID)) {
            return false;
        }
        if (Double.doubleToLongBits(this.mjd) != Double.doubleToLongBits(other.mjd)) {
            return false;
        }
        if (Double.doubleToLongBits(this.ucoord) != Double.doubleToLongBits(other.ucoord)) {
            return false;
        }
        return Double.doubleToLongBits(this.vcoord) == Double.doubleToLongBits(other.vcoord);
    }

    /**
     * toString() implementation wrapper to get complete information
     * Note: prefer using @see #toString(java.lang.StringBuilder) instead
     * @return string representation
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(128);
        toString(sb, false);
        return sb.toString();
    }

    /**
     * toString() implementation using string builder
     * 
     * @param sb string builder to append to
     * @param full true to get complete information; false to get main information (shorter)
     */
    @Override
    public void toString(final StringBuilder sb, final boolean full) {
        ObjectUtils.getObjectType(sb, this);
        sb.append("{targetUID=");
        sb.append(targetUID);
        sb.append(", insModeUID=");
        sb.append(insModeUID);
        sb.append(", mjd=");
        sb.append(NumberUtils.trimTo5Digits(mjd));
        sb.append(", ucoord=");
        sb.append(NumberUtils.trimTo2Digits(ucoord));
        sb.append(", vcoord=");
        sb.append(NumberUtils.trimTo2Digits(vcoord)).append('}');
    }

}
