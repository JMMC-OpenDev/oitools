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
package fr.jmmc.oitools.processing;

import fr.jmmc.jmcs.util.NumberUtils;
import fr.jmmc.oitools.OIFitsConstants;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Basic selector (target UID, instrument mode UID, night Id)
 */
public final class Selector {

    public final static String FILTER_TARGET_ID = OIFitsConstants.COLUMN_TARGET_ID;
    public final static String FILTER_NIGHT_ID = OIFitsConstants.COLUMN_NIGHT_ID;

    public final static String FILTER_EFFWAVE = OIFitsConstants.COLUMN_EFF_WAVE;
    public final static String FILTER_EFFBAND = OIFitsConstants.COLUMN_EFF_BAND;

    public final static String FILTER_MJD = OIFitsConstants.COLUMN_MJD;

    public final static String FILTER_STAINDEX = OIFitsConstants.COLUMN_STA_INDEX;
    public final static String FILTER_STACONF = OIFitsConstants.COLUMN_STA_CONF;

    public static final List<String> SPECIAL_COLUMN_NAMES = Arrays.asList(new String[]{
        Selector.FILTER_EFFWAVE,
        Selector.FILTER_EFFBAND,
        // uncomment once supported (String values)
        // Selector.FILTER_NIGHT_ID,
        Selector.FILTER_STAINDEX,
        Selector.FILTER_STACONF
    });

    // members:
    private List<String> targetUIDs = null;
    private List<String> insModeUIDs = null;
    private List<Integer> nightIDs = null;
    // table selection filter expressed as extNb (integer) values + OIFits file (id)
    private Map<String, List<Integer>> extNbsPerOiFitsPath = null;
    // Extra criteria:
    private Map<String, FilterValues<?>> filtersMap = null;

    public Selector() {
        reset();
    }

    public void reset() {
        targetUIDs = null;
        insModeUIDs = null;
        nightIDs = null;

        if (extNbsPerOiFitsPath != null) {
            extNbsPerOiFitsPath.clear();
        }
        if (filtersMap != null) {
            filtersMap.clear();
        }
    }

    public boolean isEmpty() {
        return (targetUIDs == null) && (insModeUIDs == null) && (nightIDs == null)
                && !hasTable() && !hasFilters();
    }

    @Override
    public String toString() {
        return "Selector["
                + ((targetUIDs != null) ? " targetUIDs: " + targetUIDs : "")
                + ((insModeUIDs != null) ? " insModeUIDs: " + insModeUIDs : "")
                + ((nightIDs != null) ? " nightIDs: " + nightIDs : "")
                + (hasTable() ? " extNbsPerOiFitsPath: " + extNbsPerOiFitsPath : "")
                + (hasFilters() ? " filters: " + filtersMap : "")
                + ']';
    }

    // --- targetUIDs ---
    public List<String> getTargetUIDs() {
        return targetUIDs;
    }

    private List<String> getOrCreateTargetUIDs() {
        if (this.targetUIDs == null) {
            this.targetUIDs = new ArrayList<>();
        }
        return this.targetUIDs;
    }

    public void setTargetUIDs(final Collection<String> targetUIDs) {
        if ((targetUIDs != null) && !targetUIDs.isEmpty()) {
            final List<String> ids = getOrCreateTargetUIDs();
            copy(ids, targetUIDs);
            if (ids.isEmpty()) {
                this.targetUIDs = null;
            }
        }
    }

    public void setTargetUID(final String targetUID) {
        if ((targetUID != null) && targetUID.trim().isEmpty()) {
            this.targetUIDs = null;
        } else {
            setTargetUIDs(Arrays.asList(new String[]{targetUID}));
        }
    }

    // --- insModeUIDs ---
    public List<String> getInsModeUIDs() {
        return insModeUIDs;
    }

    private List<String> getOrCreateInsModeUIDs() {
        if (this.insModeUIDs == null) {
            this.insModeUIDs = new ArrayList<>();
        }
        return this.insModeUIDs;
    }

    public void setInsModeUIDs(final Collection<String> insModeUIDs) {
        if ((insModeUIDs != null) && !insModeUIDs.isEmpty()) {
            final List<String> ids = getOrCreateInsModeUIDs();
            copy(ids, insModeUIDs);
            if (ids.isEmpty()) {
                this.insModeUIDs = null;
            }
        }
    }

    public void setInsModeUID(final String insModeUID) {
        if ((insModeUID != null) && insModeUID.trim().isEmpty()) {
            this.insModeUIDs = null;
        } else {
            setInsModeUIDs(Arrays.asList(new String[]{insModeUID}));
        }
    }

    // --- nightIDs ---
    public List<Integer> getNightIDs() {
        return nightIDs;
    }

    private List<Integer> getOrCreateNightIDs() {
        if (this.nightIDs == null) {
            this.nightIDs = new ArrayList<>();
        }
        return this.nightIDs;
    }

    public void setNightIDs(final Collection<Integer> nightIDs) {
        if ((nightIDs != null) && !nightIDs.isEmpty()) {
            final List<Integer> ids = getOrCreateNightIDs();
            copyInts(ids, nightIDs);
            if (ids.isEmpty()) {
                this.nightIDs = null;
            }
        }
    }

    public void parseNightIDs(final Collection<String> nightIDs) {
        if ((nightIDs != null) && !nightIDs.isEmpty()) {
            final List<Integer> ids = getOrCreateNightIDs();
            convertInts(ids, nightIDs);
            if (ids.isEmpty()) {
                this.nightIDs = null;
            }
        }
    }

    public void setNightID(final Integer nightID) {
        if (nightID == null) {
            this.nightIDs = null;
        } else {
            setNightIDs(Arrays.asList(new Integer[]{nightID}));
        }
    }

    // --- extNbsPerOiFitsPath ---
    public boolean hasTable() {
        return (extNbsPerOiFitsPath != null) && !extNbsPerOiFitsPath.isEmpty();
    }

    public List<Integer> getTables(final String oiFitsPath) {
        return (extNbsPerOiFitsPath != null) ? extNbsPerOiFitsPath.get(oiFitsPath) : null;
    }

    public void addTable(final String oiFitsPath, final Integer extNb) {
        if (extNbsPerOiFitsPath == null) {
            extNbsPerOiFitsPath = new HashMap<String, List<Integer>>();
        }
        List<Integer> extNbs = extNbsPerOiFitsPath.get(oiFitsPath);
        if (extNbs == null) {
            extNbs = new ArrayList<Integer>(1);
            extNbsPerOiFitsPath.put(oiFitsPath, extNbs);
        }
        if (extNb != null) {
            extNbs.add(extNb);
        }
    }

    // --- filters ---
    public Map<String, FilterValues<?>> getFiltersMap() {
        return filtersMap;
    }

    private Map<String, FilterValues<?>> getOrCreateFiltersMap() {
        if (filtersMap == null) {
            filtersMap = new LinkedHashMap<>();
        }
        return filtersMap;
    }

    public boolean hasFilters() {
        return (filtersMap != null) && !filtersMap.isEmpty();
    }

    public boolean hasFilter(final String columnName) {
        return (filtersMap != null) && filtersMap.containsKey(columnName);
    }

    public List getFilterIncludeValues(final String columnName) {
        FilterValues filterValues = getFilterValues(columnName);
        return (filterValues != null) ? filterValues.getIncludeValues() : null;
    }

    public List getFilterExcludeValues(final String columnName) {
        FilterValues filterValues = getFilterValues(columnName);
        return (filterValues != null) ? filterValues.getExcludeValues() : null;
    }

    public <K> FilterValues<K> getFilterValues(final String columnName) {
        return (filtersMap != null) ? (FilterValues<K>) filtersMap.get(columnName) : null;
    }

    public <K> FilterValues<K> removeFilterValues(final String columnName) {
        return (filtersMap != null) ? (FilterValues<K>) filtersMap.remove(columnName) : null;
    }

    private <K> FilterValues<K> getOrCreateFilterValues(final String columnName) {
        FilterValues<K> filterValues = getFilterValues(columnName);
        if (filterValues == null) {
            filterValues = new FilterValues<>(columnName);
            getOrCreateFiltersMap().put(columnName, filterValues);
        }
        return filterValues;
    }

    public boolean addFilter(final String columnName, final FilterValues<?> filterValues) {
        if ((filterValues != null) && !filterValues.isEmpty()) {
            getOrCreateFiltersMap().put(columnName, filterValues);
            return true;
        }
        return false;
    }

    public boolean addFilter(final String columnName, final List<?> includeValues) {
        return addIncludingFilter(columnName, includeValues);
    }

    public boolean addIncludingFilter(final String columnName, final List<?> includeValues) {
        if ((includeValues != null) && !includeValues.isEmpty()) {
            final FilterValues filterValues = getOrCreateFilterValues(columnName);
            filterValues.setIncludeValues(includeValues);
            return true;
        }
        return false;
    }

    public boolean addExcludingFilter(final String columnName, final List<?> excludeValues) {
        if ((excludeValues != null) && !excludeValues.isEmpty()) {
            final FilterValues filterValues = getOrCreateFilterValues(columnName);
            filterValues.setExcludeValues(excludeValues);
            return true;
        }
        return false;
    }

    public boolean removeFilter(final String columnName) {
        return (removeFilterValues(columnName) != null);
    }

    public static boolean isRangeFilter(final String name) {
        switch (name) {
            case Selector.FILTER_TARGET_ID:
            case Selector.FILTER_NIGHT_ID:
            case Selector.FILTER_STAINDEX:
            case Selector.FILTER_STACONF:
                return false;
            default:
                return true;
        }
    }

    public static boolean isCustomFilter(final String name) {
        switch (name) {
            case Selector.FILTER_TARGET_ID:
            case Selector.FILTER_NIGHT_ID:
            case Selector.FILTER_STAINDEX:
            case Selector.FILTER_STACONF:
            case Selector.FILTER_MJD:
                return true;
            default:
                return isCustomFilterOnWavelengths(name);
        }
    }

    public static boolean isCustomFilterOnWavelengths(final String name) {
        switch (name) {
            case Selector.FILTER_EFFWAVE:
            case Selector.FILTER_EFFBAND:
                return true;
            default:
                return false;
        }
    }

    public final static class FilterValues<K> {

        /** column name (debugging) */
        private final String columnName;
        /** filter values (string or range) to include matching values */
        private List<K> includeValues = null;
        /** filter values (string or range) to exclude matching values */
        private List<K> excludeValues = null;

        public FilterValues(final String columnName) {
            this.columnName = columnName;
        }

        public String getColumnName() {
            return columnName;
        }

        public boolean isEmpty() {
            return (includeValues == null) && (excludeValues == null);
        }

        public List<K> getIncludeValues() {
            return includeValues;
        }

        public List<K> getOrCreateIncludeValues() {
            if (includeValues == null) {
                includeValues = new ArrayList<>(4);
            }
            return includeValues;
        }

        public void setIncludeValues(final List<K> includeValues) {
            this.includeValues = includeValues;
        }

        public List<K> getExcludeValues() {
            return excludeValues;
        }

        public List<K> getOrCreateExcludeValues() {
            if (excludeValues == null) {
                excludeValues = new ArrayList<>(2);
            }
            return excludeValues;
        }

        public void setExcludeValues(final List<K> excludeValues) {
            this.excludeValues = excludeValues;
        }

        @Override
        public String toString() {
            return "FilterValues{" + "columnName=" + columnName + ", includeValues=" + includeValues + ", excludeValues=" + excludeValues + '}';
        }
    }

    private static void copy(final List<String> ids, final Collection<String> input) {
        ids.clear();

        for (String id : input) {
            if ((id != null) && id.trim().isEmpty()) {
                id = null;
            }
            if (id != null) {
                ids.add(id);
            }
        }
    }

    private static void copyInts(final List<Integer> ids, final Collection<Integer> input) {
        ids.clear();

        for (Integer id : input) {
            if (id != null) {
                ids.add(id);
            }
        }
    }

    private static void convertInts(final List<Integer> ids, final Collection<String> input) {
        ids.clear();

        for (String id : input) {
            if (id != null) {
                ids.add(NumberUtils.valueOf(id));
            }
        }
    }
}
