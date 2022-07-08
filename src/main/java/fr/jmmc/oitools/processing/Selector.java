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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Basic selector (target UID, instrument mode UID, night Id)
 */
public final class Selector {

    public final static String FILTER_WAVELENGTH = OIFitsConstants.COLUMN_EFF_WAVE;
    public final static String FILTER_BASELINE = OIFitsConstants.COLUMN_STA_INDEX;
    public final static String FILTER_MJD = OIFitsConstants.COLUMN_MJD;

    private String targetUID = null;
    private String insModeUID = null;
    private Integer nightID = null;
    // table selection filter expressed as extNb (integer) values + OIFits file (id)
    private final Map<String, List<Integer>> extNbsPerOiFitsPath = new HashMap<String, List<Integer>>();
    // Extra criteria:
    private final Map<String, List<?>> filtersMap = new LinkedHashMap<>();

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

    Map<String, List<?>> getFiltersMap() {
        return filtersMap;
    }

    public boolean hasFilters() {
        return !filtersMap.isEmpty();
    }

    public boolean hasFilter(final String columnName) {
        return filtersMap.containsKey(columnName);
    }

    public <K> List<K> getFilter(final String columnName) {
        return (List<K>) filtersMap.get(columnName);
    }

    public void addFilter(final String columnName, final List<?> values) {
        filtersMap.put(columnName, values);
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

}
