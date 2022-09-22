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

import fr.jmmc.oitools.model.DataModel;
import fr.jmmc.oitools.model.Granule;
import fr.jmmc.oitools.model.Granule.GranuleField;
import fr.jmmc.oitools.model.InstrumentMode;
import fr.jmmc.oitools.model.NightId;
import fr.jmmc.oitools.model.OIData;
import fr.jmmc.oitools.model.OIDataListHelper;
import fr.jmmc.oitools.model.OIFitsCollection;
import fr.jmmc.oitools.model.OIFitsFile;
import fr.jmmc.oitools.model.StaNamesDir;
import fr.jmmc.oitools.model.Target;
import fr.jmmc.oitools.model.range.Range;
import fr.jmmc.oitools.util.OITableByFileComparator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Basic selector result (OIData and Granule sets)
 */
public class BaseSelectorResult {

    /** members */
    /** OIFits collection (data source) */
    private final OIFitsCollection oiFitsCollection;
    /** granule set (insertion ordered) */
    private final Set<Granule> granules = new HashSet<Granule>();
    /* preserve order in selected data (per file) */
    private final Set<OIData> oiDatas = new LinkedHashSet<OIData>();
    /** selector to get criterias */
    private Selector selector = null;

    /** cached values */
    private List<OIData> sortedOIDatas = null;
    private List<OIFitsFile> sortedOIFitsFiles = null;
    private List<Target> sortedTargets = null;
    private List<InstrumentMode> sortedInstrumentModes = null;
    private List<NightId> sortedNightIds = null;
    private List<String> distinctStaNames = null;
    private List<String> distinctStaConfs = null;
    private Range wavelengthRange = null;
    /** Map of used staNames to StaNamesDir (reference StaNames / orientation) */
    private Map<String, StaNamesDir> usedStaNamesMap = null;
    /* data model on selected data */
    private DataModel dataModel = null;

    public BaseSelectorResult(final OIFitsCollection oiFitsCollection) {
        this.oiFitsCollection = oiFitsCollection;
    }

    public final OIFitsCollection getOiFitsCollection() {
        return oiFitsCollection;
    }

    public final boolean hasSelector() {
        return (selector != null);
    }

    public final Selector getSelector() {
        return selector;
    }

    public final void setSelector(final Selector selector) {
        this.selector = selector;
    }

    public final boolean isEmpty() {
        return granules.isEmpty();
    }

    public final void addOIData(final Granule g, final OIData oiData) {
        granules.add(g);
        oiDatas.add(oiData);
    }

    public final List<OIData> getSortedOIDatas() {
        if (sortedOIDatas == null) {
            final ArrayList<OIData> sorted = new ArrayList<OIData>(oiDatas);
            Collections.sort(sorted, OITableByFileComparator.INSTANCE);
            sortedOIDatas = sorted;
        }
        return sortedOIDatas;
    }

    public final List<OIFitsFile> getSortedOIFitsFiles() {
        if (sortedOIFitsFiles == null) {
            sortedOIFitsFiles = OIDataListHelper.getSortedOIFitsFiles(oiDatas);
        }
        return sortedOIFitsFiles;
    }

    public final List<Target> getDistinctTargets() {
        if (sortedTargets == null) {
            sortedTargets = Granule.getSortedDistinctGranuleField(granules, GranuleField.TARGET);
        }
        return sortedTargets;
    }

    public final List<InstrumentMode> getDistinctInstrumentModes() {
        if (sortedInstrumentModes == null) {
            sortedInstrumentModes = Granule.getSortedDistinctGranuleField(granules, GranuleField.INS_MODE);
        }
        return sortedInstrumentModes;
    }

    public final List<NightId> getDistinctNightIds() {
        if (sortedNightIds == null) {
            sortedNightIds = Granule.getSortedDistinctGranuleField(granules, GranuleField.NIGHT);
        }
        return sortedNightIds;
    }

    /**
     * Return the unique staNames values (sorted by name) from OIData tables
     * @return unique staNames values (sorted by name)
     */
    public List<String> getDistinctStaNames() {
        if (this.distinctStaNames == null) {
            this.distinctStaNames = OIDataListHelper.getDistinctStaNames(oiDatas, getUsedStaNamesMap());
        }
        return this.distinctStaNames;
    }

    /**
     * Return the unique staConfs values (sorted by name) from OIData tables
     * @return unique staConfs values (sorted by name)
     */
    public List<String> getDistinctStaConfs() {
        if (this.distinctStaConfs == null) {
            this.distinctStaConfs = OIDataListHelper.getDistinctStaConfs(oiDatas);
        }
        return this.distinctStaConfs;
    }

    /**
     * Get the wavelength range used by OIData tables
     * @return the wavelength range used by OIData tables
     */
    public Range getWavelengthRange() {
        // lazy computation:
        if (wavelengthRange == null) {
            this.wavelengthRange = OIDataListHelper.getWaveLengthRange(oiDatas);
        }
        return wavelengthRange;
    }

    /**
     * Return the Map of sorted staNames to StaNamesDir
     * @return Map of sorted staNames to StaNamesDir
     */
    public final Map<String, StaNamesDir> getUsedStaNamesMap() {
        return usedStaNamesMap;
    }

    public final void setUsedStaNamesMap(final Map<String, StaNamesDir> usedStaNamesMap) {
        this.usedStaNamesMap = usedStaNamesMap;
    }

    public final DataModel getDataModel() {
        if (dataModel == null) {
            dataModel = DataModel.getInstance(oiDatas);
        }
        return dataModel;
    }

    @Override
    public String toString() {
        return "BaseSelectorResult{" + "granules=" + granules + ", oiDatas=" + oiDatas + '}';
    }

    public static DataModel getDataModel(final BaseSelectorResult selectorResult) {
        return (selectorResult == null) ? DataModel.getInstance() : selectorResult.getDataModel();
    }
}
