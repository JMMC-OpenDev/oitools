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

import fr.jmmc.oitools.model.range.Range;
import fr.jmmc.oitools.util.GranuleComparator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * A value-type representing a granule = INSNAME (backend) + TARGET + NIGHT
 *
 * @author bourgesl
 */
public final class Granule {

    /** logger */
    protected final static Logger logger = Logger.getLogger(Granule.class.getName());

    public enum GranuleField {
        TARGET, INS_MODE, NIGHT;
    }

    public final static Matcher<Granule> MATCHER_LIKE = new Matcher<Granule>() {

        @Override
        public boolean match(final Granule pattern, final Granule candidate) {
            if (pattern == candidate) {
                return true;
            }
            if ((pattern.getTarget() != null) && (candidate.getTarget() != null)) {
                if (!pattern.getTarget().equals(candidate.getTarget())) {
                    return false;
                }
            }
            if ((pattern.getInsMode() != null) && (candidate.getInsMode() != null)) {
                if (!pattern.getInsMode().equals(candidate.getInsMode())) {
                    return false;
                }
            }
            if ((pattern.getNight() != null) && (candidate.getNight() != null)) {
                if (!pattern.getNight().equals(candidate.getNight())) {
                    return false;
                }
            }
            if (pattern.hasDistinctStaNames() && candidate.hasDistinctStaNames()) {
                if (!Granule.match(pattern.getDistinctStaNames(), candidate.getDistinctStaNames())) {
                    return false;
                }
            }
            if (pattern.hasDistinctMjdRanges() && candidate.hasDistinctMjdRanges()) {
                if (!Range.matchRanges(pattern.getDistinctMjdRanges(), candidate.getDistinctMjdRanges())) {
                    return false;
                }
            }
            if (pattern.hasDistinctWavelengthRanges() && (candidate.getInsMode() != null)) {
                if (!Range.matchRange(pattern.getDistinctWavelengthRanges(), candidate.getInsMode().getWavelengthRange())) {
                    return false;
                }
            }
            return true;
        }
    };

    /* members */
    private Target target;
    private InstrumentMode insMode;
    private NightId night;
    /* extra information (filters) */
    /** Set of distinct staNames */
    private Set<String> distinctStaNames = null;
    /** distinct MJD values */
    private Set<Range> distinctMjdRanges = null;
    /** distinct Wavelength values */
    private Set<Range> distinctWavelengthRanges = null;

    public Granule() {
        this(null, null, null);
    }

    public Granule(final Target target, final InstrumentMode insMode, final NightId night) {
        set(target, insMode, night);
    }

    public void set(final Target target, final InstrumentMode insMode, final NightId night) {
        this.target = target;
        this.insMode = insMode;
        this.night = night;
    }

    public Target getTarget() {
        return target;
    }

    public InstrumentMode getInsMode() {
        return insMode;
    }

    public NightId getNight() {
        return night;
    }

    public Object getField(GranuleField field) {
        switch (field) {
            case TARGET:
                return getTarget();
            case INS_MODE:
                return getInsMode();
            case NIGHT:
                return getNight();
            default:
                return null;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + (this.target != null ? this.target.hashCode() : 0);
        hash = 67 * hash + (this.insMode != null ? this.insMode.hashCode() : 0);
        hash = 67 * hash + (this.night != null ? this.night.hashCode() : 0);
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
        final Granule other = (Granule) obj;
        if (this.target != other.getTarget() && (this.target == null || !this.target.equals(other.getTarget()))) {
            return false;
        }
        if (this.insMode != other.getInsMode() && (this.insMode == null || !this.insMode.equals(other.getInsMode()))) {
            return false;
        }
        if (this.night != other.getNight() && (this.night == null || !this.night.equals(other.getNight()))) {
            return false;
        }
        return true;
    }

    public boolean isEmpty() {
        return (this.target == null) && (this.insMode == null) && (this.night == null)
                && !hasDistinctStaNames() && !hasDistinctMjdRanges() && !hasDistinctWavelengthRanges();
    }

    /* extra information (filters) */
    public boolean hasDistinctStaNames() {
        return (distinctStaNames != null) && !distinctStaNames.isEmpty();
    }

    public Set<String> getDistinctStaNames() {
        if (distinctStaNames == null) {
            distinctStaNames = new LinkedHashSet<String>();
        }
        return distinctStaNames;
    }

    public boolean hasDistinctMjdRanges() {
        return (distinctMjdRanges != null) && !distinctMjdRanges.isEmpty();
    }

    public Set<Range> getDistinctMjdRanges() {
        if (distinctMjdRanges == null) {
            distinctMjdRanges = new LinkedHashSet<Range>();
        }
        return distinctMjdRanges;
    }

    public boolean hasDistinctWavelengthRanges() {
        return (distinctWavelengthRanges != null) && !distinctWavelengthRanges.isEmpty();
    }

    public Set<Range> getDistinctWavelengthRanges() {
        if (distinctWavelengthRanges == null) {
            distinctWavelengthRanges = new LinkedHashSet<Range>();
        }
        return distinctWavelengthRanges;
    }

    @Override
    public String toString() {
        return "Granule{" + "target=" + target + ", insMode=" + insMode + ", night=" + night
                + ", distinctStaNames=" + distinctStaNames
                + ", distinctMjdRanges=" + distinctMjdRanges
                + ", distinctWavelengthRanges=" + distinctWavelengthRanges
                + '}';
    }

    public static <K> Set<K> getDistinctGranuleField(final Collection<Granule> granules, final GranuleField field) {
        final Set<K> values = new HashSet<K>();
        for (Granule g : granules) {
            values.add((K) g.getField(field));
        }
        return values;
    }

    public static <K> List<K> getSortedDistinctGranuleField(final Collection<Granule> granules, final GranuleField field) {
        final Set<K> values = getDistinctGranuleField(granules, field);
        final List<K> sorted = new ArrayList<K>(values);
        Collections.sort(sorted, GranuleComparator.getComparator(field));
        return sorted;
    }

    public static boolean match(final Set<String> selected, final Set<String> candidates) {
        for (String sel : selected) {
            if (candidates.contains(sel)) {
                return true;
            }
        }
        return false;
    }
}
