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
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * A value-type (immutable) representing a single night in any OI_Data table
 * @author bourgesl
 */
public final class NightId {

    public final static NightId UNDEFINED = new NightId();

    public static final Comparator<NightId> CMP_NIGHT = new Comparator<NightId>() {
        @Override
        public int compare(NightId n1, NightId n2) {
            return NumberUtils.compare(n1.getNightId(), n2.getNightId());
        }
    };

    /** cached NightId instances */
    private final static Map<NightId, NightId> CACHE = new HashMap<NightId, NightId>(64);

    public static NightId getCachedInstance(final int nightId) {
        NightId n = new NightId(nightId);
        NightId night = getCachedInstance(n);
        if (night == null) {
            night = putCachedInstance(n);
        }
        return night;
    }

    public static NightId getCachedInstance(final NightId nightId) {
        return CACHE.get(nightId);
    }

    public static NightId putCachedInstance(final NightId nightId) {
        CACHE.put(nightId, nightId);
        return nightId;
    }

    /* members */
    /** nightId = rounded MJD */
    private int nightId;

    public NightId() {
        this(Integer.MAX_VALUE);
    }

    private NightId(final int nightId) {
        set(nightId);
    }

    /**
     * Warning: never alter a cached instance !
     * @param nightId nightId value
     */
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

    public void toString(final StringBuilder sb) {
        sb.append(nightId);
    }
}
