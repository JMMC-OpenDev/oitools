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

import fr.jmmc.oitools.OIFitsConstants;
import java.util.ArrayList;
import java.util.Arrays;
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
    private String targetUID = null;
    private String insModeUID = null;
    private Integer nightID = null;
    // table selection filter expressed as extNb (integer) values + OIFits file (id)
    private final Map<String, List<Integer>> extNbsPerOiFitsPath = new HashMap<String, List<Integer>>();
    // Extra criteria:
    private final Map<String, FilterValues<?>> filtersMap = new LinkedHashMap<>();

    public Selector() {
        reset();
    }

    public void reset() {
        targetUID = null;
        insModeUID = null;
        nightID = null;
        extNbsPerOiFitsPath.clear();
        filtersMap.clear();
    }

    public String getTargetUID() {
        return targetUID;
    }

    public void setTargetUID(String targetUID) {
        if ((targetUID != null) && targetUID.trim().isEmpty()) {
            targetUID = null;
        }
        this.targetUID = targetUID;
    }

    public String getInsModeUID() {
        return insModeUID;
    }

    public void setInsModeUID(String insModeUID) {
        if ((insModeUID != null) && insModeUID.trim().isEmpty()) {
            insModeUID = null;
        }
        this.insModeUID = insModeUID;
    }

    public Integer getNightID() {
        return nightID;
    }

    public void setNightID(final Integer nightID) {
        this.nightID = nightID;
    }

    public boolean hasTable() {
        return !extNbsPerOiFitsPath.isEmpty();
    }

    public List<Integer> getTables(final String oiFitsPath) {
        return extNbsPerOiFitsPath.get(oiFitsPath);
    }

    public void addTable(final String oiFitsPath, final Integer extNb) {
        List<Integer> extNbs = extNbsPerOiFitsPath.get(oiFitsPath);
        if (extNbs == null) {
            extNbs = new ArrayList<Integer>(1);
            extNbsPerOiFitsPath.put(oiFitsPath, extNbs);
        }
        if (extNb != null) {
            extNbs.add(extNb);
        }
    }

    public Map<String, FilterValues<?>> getFiltersMap() {
        return filtersMap;
    }

    public boolean hasFilters() {
        return !filtersMap.isEmpty();
    }

    public boolean hasFilter(final String columnName) {
        return filtersMap.containsKey(columnName);
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
        return (FilterValues<K>) filtersMap.get(columnName);
    }

    public <K> FilterValues<K> removeFilterValues(final String columnName) {
        return (FilterValues<K>) filtersMap.remove(columnName);
    }

    private <K> FilterValues<K> getOrCreateFilterValues(final String columnName) {
        FilterValues<K> filterValues = getFilterValues(columnName);
        if (filterValues == null) {
            filterValues = new FilterValues<>(columnName);
            filtersMap.put(columnName, filterValues);
        }
        return filterValues;
    }

    public boolean addFilter(final String columnName, final FilterValues<?> filterValues) {
        if ((filterValues != null) && !filterValues.isEmpty()) {
            filtersMap.put(columnName, filterValues);
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

    public boolean isEmpty() {
        return (targetUID == null) && (insModeUID == null) && (nightID == null)
                && !hasTable() && !hasFilters();
    }

    @Override
    public String toString() {
        return "Selector["
                + ((targetUID != null) ? " targetUID: " + targetUID : "")
                + ((insModeUID != null) ? " insModeUID: " + insModeUID : "")
                + ((nightID != null) ? " nightID: " + nightID : "")
                + (hasTable() ? " extNbsPerOiFitsPath: " + extNbsPerOiFitsPath : "")
                + (hasFilters() ? " filters: " + filtersMap : "")
                + ']';
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
}
