/* 
 * Copyright (C) 2022 CNRS - JMMC project ( http://www.jmmc.fr )
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

import fr.jmmc.oitools.model.IndexMask;
import fr.jmmc.oitools.model.NightId;
import fr.jmmc.oitools.model.OIData;
import fr.jmmc.oitools.model.OIFitsCollection;
import fr.jmmc.oitools.model.OIWavelength;
import fr.jmmc.oitools.model.range.Range;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Basic selector result (OIData and Granule sets)
 */
public final class SelectorResult extends BaseSelectorResult {

    /** members */
    private final BaseSelectorResult targetResult;
    /* filters: use ArrayList for performance */
    private final ArrayList<FitsTableFilter<?>> filtersOIWavelength = new ArrayList<>();
    private final ArrayList<FitsTableFilter<?>> filtersOIData1D = new ArrayList<>();
    private final ArrayList<FitsTableFilter<?>> filtersOIData2D = new ArrayList<>();
    private final ArrayList<FitsTableFilter<?>> filtersUsed = new ArrayList<>();
    /* applied filters */
    private final Set<String> usedColumnsFiltersOIData2D = new LinkedHashSet<>();
    private final Set<String> relatedColumnsFiltersOIData2D = new LinkedHashSet<>();

    /* masks */
    /** Map between OIWavelength table to BitSet (mask 1D) */
    private final Map<OIWavelength, IndexMask> maskOIWavelengths = new IdentityHashMap<OIWavelength, IndexMask>();
    /** Map between OIData table to BitSet (mask 1D) */
    private final Map<OIData, IndexMask> maskOIDatas1D = new IdentityHashMap<OIData, IndexMask>();
    /** Map between OIData table to BitSet (mask 2D) */
    private final Map<OIData, IndexMask> maskOIDatas2D = new IdentityHashMap<OIData, IndexMask>();

    public SelectorResult(final OIFitsCollection oiFitsCollection, final BaseSelectorResult targetResult) {
        super(oiFitsCollection);
        this.targetResult = targetResult;
    }

    public boolean hasTargetResult() {
        return (targetResult != null);
    }

    public BaseSelectorResult getTargetResult() {
        return targetResult;
    }

    public void resetFilters() {
        filtersUsed.clear();

        if (hasFiltersOIWavelength()) {
            FitsTableFilter.resetFilters(getFiltersOIWavelength());
        }
        if (hasFiltersOIData1D()) {
            FitsTableFilter.resetFilters(getFiltersOIData1D());
        }
        if (hasFiltersOIData2D()) {
            FitsTableFilter.resetFilters(getFiltersOIData2D());
        }
        // TODO: use filters to build back the filter criteria (CLI)
        // instead of selector ...
    }

    public boolean hasFilters() {
        return hasFiltersOIWavelength() || hasFiltersOIData1D() || hasFiltersOIData2D();
    }

    public boolean hasFiltersOIWavelength() {
        return !filtersOIWavelength.isEmpty();
    }

    public ArrayList<FitsTableFilter<?>> getFiltersOIWavelength() {
        return filtersOIWavelength;
    }

    public boolean hasFiltersOIData1D() {
        return !filtersOIData1D.isEmpty();
    }

    public ArrayList<FitsTableFilter<?>> getFiltersOIData1D() {
        return filtersOIData1D;
    }

    public boolean hasFiltersOIData2D() {
        return !filtersOIData2D.isEmpty();
    }

    public ArrayList<FitsTableFilter<?>> getFiltersOIData2D() {
        return filtersOIData2D;
    }

    public ArrayList<FitsTableFilter<?>> getFiltersUsed() {
        return filtersUsed;
    }

    // --- masks ---
    /**
     * Retrieves the IndexMask for the given OIWavelength.
     * @param oiWavelength May be null.
     * @return the IndexMask, or null or FULL
     */
    public IndexMask getWavelengthMask(final OIWavelength oiWavelength) {
        return this.maskOIWavelengths.get(oiWavelength);
    }

    public IndexMask getWavelengthMaskNotFull(final OIWavelength oiWavelength) {
        final IndexMask maskWavelength = getWavelengthMask(oiWavelength);
        return (IndexMask.isNotFull(maskWavelength)) ? maskWavelength : null;
    }

    /**
     * Registers the IndexMask for the given OIWavelength.
     * @param oiWavelength Must not be null.
     * @param mask IndexMask not null or FULL
     */
    public void putWavelengthMask(final OIWavelength oiWavelength, final IndexMask mask) {
        this.maskOIWavelengths.put(oiWavelength, mask);
    }

    /**
     * Retrieves the IndexMask for the given OIData.
     * @param oiData May be null.
     * @return the IndexMask, or null or FULL
     */
    public IndexMask getDataMask1D(final OIData oiData) {
        return this.maskOIDatas1D.get(oiData);
    }

    public IndexMask getDataMask1DNotFull(final OIData oiData) {
        final IndexMask maskOIData1D = getDataMask1D(oiData);
        return (IndexMask.isNotFull(maskOIData1D)) ? maskOIData1D : null;
    }

    /**
     * Registers the IndexMask for the given OIData.
     * @param oiData Must not be null.
     * @param mask IndexMask not null or FULL
     */
    public void putDataMask1D(final OIData oiData, final IndexMask mask) {
        this.maskOIDatas1D.put(oiData, mask);
    }

    /**
     * Retrieves the IndexMask for the given OIData.
     * @param oiData May be null.
     * @return the IndexMask, or null or FULL
     */
    public IndexMask getDataMask2D(final OIData oiData) {
        return this.maskOIDatas2D.get(oiData);
    }

    public IndexMask getDataMask2DNotFull(final OIData oiData) {
        final IndexMask maskOIData2D = getDataMask2D(oiData);
        return (IndexMask.isNotFull(maskOIData2D)) ? maskOIData2D : null;
    }

    /**
     * Registers the IndexMask for the given OIData.
     * @param oiData Must not be null.
     * @param mask IndexMask not null or FULL
     */
    public void putDataMask2D(final OIData oiData, final IndexMask mask) {
        this.maskOIDatas2D.put(oiData, mask);
    }

    public Set<String> getUsedColumnsFiltersOIData2D() {
        return usedColumnsFiltersOIData2D;
    }

    public Set<String> getRelatedColumnsFiltersOIData2D() {
        return relatedColumnsFiltersOIData2D;
    }

    // --- statistics on masked OIData ---
    private final static int COUNT_NB_ROWS = 0;
    private final static int COUNT_POINTS = 1;
    private final static int COUNT_POINTS_NOT_FLAGGED = 2;

    /**
     * @return total number of measurements in oidata tables (masked)
     */
    @Override
    public int getNbMeasurements() {
        return getCount(COUNT_NB_ROWS);
    }

    /**
     * @return total number of data points in all oidata tables
     */
    @Override
    public int getNbDataPoints() {
        return getCount(COUNT_POINTS);
    }

    /**
     * @return total number of non-flagged data points in all oidata tables
     */
    @Override
    public int getNbDataPointsNotFlagged() {
        return getCount(COUNT_POINTS_NOT_FLAGGED);
    }

    private int getCount(final int type) {
        int count = 0;

        // Process selected OIData tables:
        for (OIData oiData : getOIDatas()) {
            // get the optional masks for this OIData table:
            final IndexMask maskOIData1D = getDataMask1DNotFull(oiData);
            final IndexMask maskOIData2D = getDataMask2DNotFull(oiData);
            // get the optional wavelength mask for the OIData's wavelength table:
            final IndexMask maskWavelength = (type >= COUNT_POINTS) ? getWavelengthMaskNotFull(oiData.getOiWavelength()) : null;

            final int idxNone = (maskOIData2D != null) ? maskOIData2D.getIndexNone() : -1;
            final int idxFull = (maskOIData2D != null) ? maskOIData2D.getIndexFull() : -1;

            final int nRows = oiData.getNbRows();
            final int nWaves = oiData.getNWave();

            final boolean[][] flags = (type == COUNT_POINTS_NOT_FLAGGED) ? oiData.getFlag() : null;

            IndexMask maskOIData2DRow = null;

            // Iterate on table rows (i):
            for (int i = 0; i < nRows; i++) {

                // check optional data mask 1D:
                if ((maskOIData1D != null) && !maskOIData1D.accept(i)) {
                    // if bit is false for this row, we hide this row
                    continue;
                }

                // check mask 2D for row None flag:
                if (maskOIData2D != null) {
                    if (maskOIData2D.accept(i, idxNone)) {
                        // row flagged as None:
                        continue;
                    }
                    // check row flagged as Full:
                    maskOIData2DRow = (maskOIData2D.accept(i, idxFull)) ? null : maskOIData2D;
                }

                if (type == COUNT_NB_ROWS) {
                    if (maskOIData2DRow != null) {
                        count++; // row is valid
                    }
                    continue;
                }
                
                final boolean[] rowFlags = (flags != null) ? flags[i] : null;

                // Iterate on wave channels (l):
                for (int l = 0; l < nWaves; l++) {

                    // check optional wavelength mask:
                    if ((maskWavelength != null) && !maskWavelength.accept(l)) {
                        // if bit is false for this row, we hide this row
                        continue;
                    }

                    // check optional data mask 2D (and its Full flag):
                    if ((maskOIData2DRow != null) && !maskOIData2DRow.accept(i, l)) {
                        // if bit is false for this row, we hide this row
                        continue;
                    }

                    // data point is valid:
                    if (type == COUNT_POINTS) {
                        count++;
                        continue;
                    }

                    if ((rowFlags != null) && rowFlags[l]) {
                        // data point is flagged so skip it:
                        continue;
                    }

                    // data point is valid and not flagged:
                    /* if (type == COUNT_POINTS_NOT_FLAGGED) is always true */
                    count++;
                }
            }
        }
        return count;
    }

    @Override
    public String toString() {
        return "SelectorResult{" + super.toString()
                + ", targetResult=" + targetResult
                + ", filtersOIWavelength=" + filtersOIWavelength + ", maskOIWavelengths=" + maskOIWavelengths
                + ", filtersOIData1D=" + filtersOIData1D + ", maskOIDatas1D=" + maskOIDatas1D
                + ", filtersOIData2D=" + filtersOIData2D + ", maskOIDatas2D=" + maskOIDatas2D
                + ", usedColumnsFiltersOIData2D=" + usedColumnsFiltersOIData2D
                + ", relatedColumnsFiltersOIData2D=" + relatedColumnsFiltersOIData2D
                + '}';
    }

    public String dumpFiltersAsString() {
        if (hasFilters()) {
            final StringBuilder sb = new StringBuilder(256);
            sb.append('[');
            if (hasFiltersOIWavelength()) {
                SelectorResult.toString(getFiltersOIWavelength(), sb);
            }
            if (hasFiltersOIData1D()) {
                SelectorResult.toString(getFiltersOIData1D(), sb);
            }
            if (hasFiltersOIData2D()) {
                SelectorResult.toString(getFiltersOIData2D(), sb);
            }
            if (getSelector() != null) {
                boolean strip = false;
                if (getSelector().getInsModeUIDs() != null) {
                    if (sb.length() > 1) {
                        sb.append("), ");
                    }
                    sb.append("InsModeUID IN (");
                    for (String value : getSelector().getInsModeUIDs()) {
                        sb.append("'").append(value).append("'").append(",");
                    }
                    sb.delete(sb.length() - 1, sb.length());
                    sb.append("), ");
                    strip = true;
                }
                if (getSelector().getNightIDs() != null) {
                    if (sb.length() > 1) {
                        sb.append("), ");
                    }
                    sb.append("NightID IN (");
                    for (Integer value : getSelector().getNightIDs()) {
                        sb.append("'").append(value).append("'").append(",");
                    }
                    sb.delete(sb.length() - 1, sb.length());
                    sb.append("), ");
                    strip = true;
                }
                if (strip) {
                    sb.delete(sb.length() - 2, sb.length());
                }
            }
            sb.append(']');
            return sb.toString();
        }
        return "[]";
    }

    public static void toString(final ArrayList<FitsTableFilter<?>> filters, final StringBuilder sb) {
        if ((filters != null) && !filters.isEmpty()) {
            if (sb.length() > 1) {
                sb.append("), ");
            }
            for (FitsTableFilter<?> filter : filters) {
                sb.append(filter.getColumnName());
                if (!filter.isInclude()) {
                    sb.append(" NOT");
                }
                sb.append(" IN (");

                for (Object value : filter.getAcceptedValues()) {
                    if (value instanceof Range) {
                        ((Range) value).toString(sb);
                    } else if (value instanceof String) {
                        sb.append("'").append(value).append("'");
                    } else if (value instanceof NightId) {
                        ((NightId) value).toString(sb);
                    } else {
                        sb.append(value);
                    }
                    sb.append(",");
                }
                sb.delete(sb.length() - 1, sb.length());
                sb.append("), ");
            }
            sb.delete(sb.length() - 2, sb.length());
        }
    }
}
