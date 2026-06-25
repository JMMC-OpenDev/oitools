/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.model;

import fr.jmmc.jmcs.util.ObjectUtils;
import fr.jmmc.jmcs.util.ToStringable;
import java.util.Objects;

/**
 * This class represents the Index value type ie (OIData pointer, row index).
 *
 * @author bourgesl
 */
public final class IndexOIData implements ToStringable {

    /** OIData pointer */
    final OIData oiData;
    /** row index */
    final int row;

    public IndexOIData(final OIData oiData, final int row) {
        this.oiData = oiData;
        this.row = row;
    }

    public OIData getOiData() {
        return oiData;
    }

    public int getRow() {
        return row;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.oiData);
        hash = 29 * hash + this.row;
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
        final IndexOIData other = (IndexOIData) obj;
        if (this.row != other.row) {
            return false;
        }
        return fr.jmmc.jmcs.util.ObjectUtils.areEquals(this.oiData, other.oiData);
    }

    /**
     * toString() implementation wrapper to get complete information
     * Note: prefer using @see #toString(java.lang.StringBuilder) instead
     * @return string representation
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(48);
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
        sb.append("{oiData=");
        this.oiData.toString(sb, true);
        sb.append(", row=").append(this.row).append('}');
    }

}
