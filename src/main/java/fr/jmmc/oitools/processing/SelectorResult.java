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

import fr.jmmc.oitools.model.Granule;
import fr.jmmc.oitools.model.Granule.GranuleField;
import fr.jmmc.oitools.model.IndexMask;
import fr.jmmc.oitools.model.InstrumentMode;
import fr.jmmc.oitools.model.NightId;
import fr.jmmc.oitools.model.OIData;
import fr.jmmc.oitools.model.OIFitsCollection;
import fr.jmmc.oitools.model.OIFitsFile;
import fr.jmmc.oitools.model.OIWavelength;
import fr.jmmc.oitools.model.StaNamesDir;
import fr.jmmc.oitools.model.Target;
import fr.jmmc.oitools.util.OIFitsFileComparator;
import fr.jmmc.oitools.util.OITableByFileComparator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Basic selector result (OIData and Granule sets)
 */
public final class SelectorResult {

    /** members */
    /** selector to get criterias */
    private Selector selector = null;
    /** OIFits collection (data source) */
    private final OIFitsCollection oiFitsCollection;
    /** granule set (insertion ordered) */
    private final Set<Granule> granules = new HashSet<Granule>();
    /* preserve order in selected data (per file) */
    private final Set<OIData> oiDatas = new LinkedHashSet<OIData>();
    /* filters */
    private final List<FitsTableFilter<?>> filtersUsed = new ArrayList<>();
    private final List<FitsTableFilter<?>> filtersOIData = new ArrayList<>();
    private final List<FitsTableFilter<?>> filtersOIWavelength = new ArrayList<>();
    /* masks */
    /** Map between OIData table to BitSet (mask 1D) */
    private final Map<OIData, IndexMask> maskOIDatas1D = new IdentityHashMap<OIData, IndexMask>();
    /** Map between OIWavelength table to BitSet (mask 1D) */
    private final Map<OIWavelength, IndexMask> maskOIWavelengths = new IdentityHashMap<OIWavelength, IndexMask>();
    /** cached values */
    private List<Target> sortedTargets = null;
    private List<InstrumentMode> sortedInstrumentModes = null;
    private List<NightId> sortedNightIds = null;
    private List<OIData> sortedOIDatas = null;
    /** Map of used staNames to StaNamesDir (reference StaNames / orientation) */
    private Map<String, StaNamesDir> usedStaNamesMap = null;

    public SelectorResult(final OIFitsCollection oiFitsCollection) {
        this.oiFitsCollection = oiFitsCollection;
        reset();
    }

    public void reset() {
        selector = null;
        granules.clear();
        oiDatas.clear();
        filtersUsed.clear();
        filtersOIData.clear();
        filtersOIWavelength.clear();
        maskOIDatas1D.clear();
        maskOIWavelengths.clear();
        sortedTargets = null;
        sortedInstrumentModes = null;
        sortedNightIds = null;
        sortedOIDatas = null;
        usedStaNamesMap = null;
    }

    public void resetFilters() {
        filtersUsed.clear();

        if (hasFiltersOIData()) {
            FitsTableFilter.resetFilters(getFiltersOIData());
        }

        if (hasFiltersOIWavelength()) {
            FitsTableFilter.resetFilters(getFiltersOIWavelength());
        }
        // TODO: use filters to build back the filter criteria (CLI)
        // instead of selector ...
    }

    public boolean hasSelector() {
        return (selector != null);
    }

    public Selector getSelector() {
        return selector;
    }

    public void setSelector(final Selector selector) {
        this.selector = selector;
    }

    public OIFitsCollection getOiFitsCollection() {
        return oiFitsCollection;
    }

    public boolean isEmpty() {
        return granules.isEmpty();
    }

    public void addOIData(final Granule g, final OIData oiData) {
        granules.add(g);
        oiDatas.add(oiData);
    }

    public List<FitsTableFilter<?>> getFiltersUsed() {
        return filtersUsed;
    }

    public boolean hasFiltersOIData() {
        return !filtersOIData.isEmpty();
    }

    public List<FitsTableFilter<?>> getFiltersOIData() {
        return filtersOIData;
    }

    public boolean hasFiltersOIWavelength() {
        return !filtersOIWavelength.isEmpty();
    }

    public List<FitsTableFilter<?>> getFiltersOIWavelength() {
        return filtersOIWavelength;
    }

    public List<Target> getDistinctTargets() {
        if (sortedTargets == null) {
            sortedTargets = Granule.getSortedDistinctGranuleField(granules, GranuleField.TARGET);
        }
        return sortedTargets;
    }

    public List<InstrumentMode> getDistinctInstrumentModes() {
        if (sortedInstrumentModes == null) {
            sortedInstrumentModes = Granule.getSortedDistinctGranuleField(granules, GranuleField.INS_MODE);
        }
        return sortedInstrumentModes;
    }

    public List<NightId> getDistinctNightIds() {
        if (sortedNightIds == null) {
            sortedNightIds = Granule.getSortedDistinctGranuleField(granules, GranuleField.NIGHT);
        }
        return sortedNightIds;
    }

    public List<OIData> getSortedOIDatas() {
        if (sortedOIDatas == null) {
            final List<OIData> sorted = new ArrayList<OIData>(oiDatas);
            Collections.sort(sorted, OITableByFileComparator.INSTANCE);
            sortedOIDatas = sorted;
        }
        return sortedOIDatas;
    }

    public List<OIFitsFile> getSortedOIFitsFiles() {
        final Set<OIFitsFile> oiFitsFiles = new HashSet<OIFitsFile>();
        for (OIData oiData : oiDatas) {
            oiFitsFiles.add(oiData.getOIFitsFile());
        }
        final List<OIFitsFile> sorted = new ArrayList<OIFitsFile>(oiFitsFiles);
        Collections.sort(sorted, OIFitsFileComparator.INSTANCE);
        return sorted;
    }

    /**
     * Retrieves the IndexMask for the given OIData.
     * @param oiData Must not be null.
     * @return the IndexMask, or null or FULL
     */
    public IndexMask getDataMask1D(final OIData oiData) {
        return this.maskOIDatas1D.get(oiData);
    }

    public IndexMask getDataMask1DNotFull(final OIData oiData) {
        final IndexMask maskOIData1D = getDataMask1D(oiData);
        return (maskOIData1D == null || maskOIData1D.isFull()) ? null : maskOIData1D;
    }

    /**
     * Registers the IndexMask for the given OIData.
     * @param oiData Must not be null.
     * @param mask IndexMask or FULL
     */
    public void putDataMask1D(final OIData oiData, final IndexMask mask) {
        this.maskOIDatas1D.put(oiData, mask);
    }

    /**
     * Retrieves the IndexMask for the given OIWavelength.
     * @param oiWavelength Must not be null.
     * @return the IndexMask, or null or FULL
     */
    public IndexMask getWavelengthMask(final OIWavelength oiWavelength) {
        return this.maskOIWavelengths.get(oiWavelength);
    }

    public IndexMask getWavelengthMaskNotFull(final OIWavelength oiWavelength) {
        final IndexMask maskWavelength = getWavelengthMask(oiWavelength);
        return (maskWavelength == null || maskWavelength.isFull()) ? null : maskWavelength;
    }

    /**
     * Registers the IndexMask for the given OIWavelength.
     * @param oiWavelength Must not be null.
     * @param mask IndexMask or FULL
     */
    public void putWavelengthMask(final OIWavelength oiWavelength, final IndexMask mask) {
        this.maskOIWavelengths.put(oiWavelength, mask);
    }

    /**
     * Return the Map of sorted staNames to StaNamesDir
     * @return Map of sorted staNames to StaNamesDir
     */
    public Map<String, StaNamesDir> getUsedStaNamesMap() {
        return usedStaNamesMap;
    }

    public void setUsedStaNamesMap(final Map<String, StaNamesDir> usedStaNamesMap) {
        this.usedStaNamesMap = usedStaNamesMap;
    }

    @Override
    public String toString() {
        return "SelectorResult{" + "granules=" + granules
                + ", oiDatas=" + oiDatas
                + ", maskOIWavelengths=" + maskOIWavelengths + '}';
    }
}
