/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.model;

import fr.jmmc.jmcs.util.NumberUtils;
import fr.jmmc.jmcs.util.ObjectUtils;
import java.util.Objects;

/**
 * This class represents the Index key type ie (target UID, instrument mode UID, mjd, BL),
 * to group related data. 
 * note: InstrumentMode imply channels are aligned (0..n) => no wavelength nor channel info.
 * @author bourgesl
 */
public final class IndexKeyBL implements IndexKey {

    // members
    final String targetUID;
    final String insModeUID;
    final double mjd;
    final String staNames;

    @SuppressWarnings("AssignmentToMethodParameter")
    IndexKeyBL(final String targetUID, final String insModeUID, final double mjd, final String staNames) {
        // always defined (UNDEFINED if null):
        this.targetUID = targetUID;
        this.insModeUID = insModeUID;
        this.mjd = mjd;
        this.staNames = staNames;
    }

    /**
     * Used by TreeMap (must be consistent with equals)
     * @param obj other instance not null
     */
    @Override
    public int compareTo(final IndexKey obj) {
        final IndexKeyBL other = (IndexKeyBL) obj;
        int res = this.targetUID.compareTo(other.targetUID);
        if (res == 0) {
            res = this.insModeUID.compareTo(other.insModeUID);
            if (res == 0) {
                res = Double.compare(this.mjd, other.mjd);
                if (res == 0) {
                    res = this.staNames.compareTo(other.staNames);
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
        hash = 29 * hash + Objects.hashCode(this.staNames);
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
        final IndexKeyBL other = (IndexKeyBL) obj;
        if (!Objects.equals(this.targetUID, other.targetUID)) {
            return false;
        }
        if (!Objects.equals(this.insModeUID, other.insModeUID)) {
            return false;
        }
        if (Double.doubleToLongBits(this.mjd) != Double.doubleToLongBits(other.mjd)) {
            return false;
        }
        if (!Objects.equals(this.targetUID, other.targetUID)) {
            return false;
        }
        return Objects.equals(this.staNames, other.staNames);
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
        sb.append(", staName=");
        sb.append(staNames).append('}');
    }

}
