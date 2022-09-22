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
import fr.jmmc.oitools.model.OIData;
import fr.jmmc.oitools.model.OIFitsCollection;
import fr.jmmc.oitools.model.OIWavelength;
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

    public SelectorResult(final OIFitsCollection oiFitsCollection) {
        super(oiFitsCollection);
    }

    public void resetFilters() {
        filtersUsed.clear();

        if (hasFiltersOIData1D()) {
            FitsTableFilter.resetFilters(getFiltersOIData1D());
        }

        if (hasFiltersOIWavelength()) {
            FitsTableFilter.resetFilters(getFiltersOIWavelength());
        }
        // TODO: use filters to build back the filter criteria (CLI)
        // instead of selector ...
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

    @Override
    public String toString() {
        return "SelectorResult{" + super.toString()
                + ", filtersOIWavelength=" + filtersOIWavelength + ", maskOIWavelengths=" + maskOIWavelengths
                + ", filtersOIData1D=" + filtersOIData1D + ", maskOIDatas1D=" + maskOIDatas1D
                + ", filtersOIData2D=" + filtersOIData2D + ", maskOIDatas2D=" + maskOIDatas2D
                + ", usedColumnsFiltersOIData2D=" + usedColumnsFiltersOIData2D
                + ", relatedColumnsFiltersOIData2D=" + relatedColumnsFiltersOIData2D
                + '}';
    }

}
