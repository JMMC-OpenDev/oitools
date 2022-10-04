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
import fr.jmmc.oitools.util.StationNamesComparator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
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

    public enum GranuleExtraField {
        DISTINCT_STA_NAMES, DISTINCT_STA_CONFS;
    }

    /* members */
    private Target target = null;
    private InstrumentMode insMode = null;
    private NightId night = null;
    /* extra information (filters) */
    /** MJD range */
    private Range mjdRange = null;
    /** Set of distinct staNames */
    private Set<String> distinctStaNames = null;
    /** Set of distinct staConfs */
    private Set<String> distinctStaConfs = null;

    public Granule() {
        super();
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

    public Object getField(final GranuleField field) {
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

    public Set<String> getField(final GranuleExtraField field) {
        switch (field) {
            case DISTINCT_STA_NAMES:
                return getDistinctStaNames();
            case DISTINCT_STA_CONFS:
                return getDistinctStaConfs();
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
        return (this.target == null) && (this.insMode == null) && (this.night == null);
    }

    /* extra information for specific filters (distinct staNames, mjd range) */
    public Range getWavelengthRange() {
        return (getInsMode() != null) ? getInsMode().getWavelengthRange() : null;
    }

    public boolean hasMjdRange() {
        return (mjdRange != null) && mjdRange.isFinite();
    }

    public Range getMjdRange() {
        if (mjdRange == null) {
            mjdRange = new Range(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
        }
        return mjdRange;
    }

    void updateMjdRange(final double mjd) {
        final Range r = getMjdRange();
        if (mjd < r.getMin()) {
            r.setMin(mjd);
        }
        if (mjd > r.getMax()) {
            r.setMax(mjd);
        }
    }

    void updateMjdRange(final Range other) {
        updateMjdRange(other.getMin());
        updateMjdRange(other.getMax());
    }

    public boolean hasDistinctStaNames() {
        return (distinctStaNames != null) && !distinctStaNames.isEmpty();
    }

    public Set<String> getDistinctStaNames() {
        if (distinctStaNames == null) {
            distinctStaNames = new LinkedHashSet<String>();
        }
        return distinctStaNames;
    }

    public boolean hasDistinctStaConfs() {
        return (distinctStaConfs != null) && !distinctStaConfs.isEmpty();
    }

    public Set<String> getDistinctStaConfs() {
        if (distinctStaConfs == null) {
            distinctStaConfs = new LinkedHashSet<String>();
        }
        return distinctStaConfs;
    }

    @Override
    public String toString() {
        return "Granule{" + "target=" + target + ", insMode=" + insMode
                + ", night=" + night + ", mjdRange=" + mjdRange
                + ", distinctStaNames=" + distinctStaNames
                + ", distinctStaConfs=" + distinctStaConfs
                + '}';
    }

    public static <K> HashSet<K> getDistinctGranuleField(final Collection<Granule> granules, final GranuleField field) {
        final HashSet<K> values = new HashSet<K>();
        for (Granule g : granules) {
            values.add((K) g.getField(field));
        }
        return values;
    }

    public static <K> ArrayList<K> getSortedDistinctGranuleField(final Collection<Granule> granules, final GranuleField field) {
        final HashSet<K> values = getDistinctGranuleField(granules, field);
        final ArrayList<K> sortedList = new ArrayList<K>(values);
        Collections.sort(sortedList, GranuleComparator.getComparator(field));
        return sortedList;
    }

    public static HashSet<String> getDistinctGranuleField(final Collection<Granule> granules, final GranuleExtraField field) {
        final HashSet<String> values = new HashSet<String>();
        for (Granule g : granules) {
            final Set<String> set = g.getField(field);
            values.addAll(g.getField(field));
        }
        return values;
    }

    public static ArrayList<String> getSortedDistinctGranuleField(final Collection<Granule> granules, final GranuleExtraField field) {
        final HashSet<String> values = getDistinctGranuleField(granules, field);
        final ArrayList<String> sortedList = new ArrayList<String>(values);
        Collections.sort(sortedList, StationNamesComparator.INSTANCE);
        return sortedList;
    }

}
