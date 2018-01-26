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

/**
 * A value-type (immutable) representing a single night in any OI_Data table
 * @author bourgesl
 */
public final class NightId {

    public final static NightId UNDEFINED = new NightId();

    /* nightId = rounded MJD */
    private int nightId;

    public NightId() {
        this(Integer.MAX_VALUE);
    }

    public NightId(final int nightId) {
        set(nightId);
    }

    public void set(final int nightId) {
        this.nightId = nightId;
    }

    public int getNightId() {
        return nightId;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + this.nightId;
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
        final NightId other = (NightId) obj;
        if (this.nightId != other.getNightId()) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "NightId{" + nightId + '}';
    }

}
