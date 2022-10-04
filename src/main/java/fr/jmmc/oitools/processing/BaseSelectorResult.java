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
import fr.jmmc.oitools.model.Granule.GranuleExtraField;
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
import java.util.logging.Logger;

/**
 * Basic selector result (OIData and Granule sets)
 */
public class BaseSelectorResult {

    /** logger */
    protected final static Logger logger = Logger.getLogger(BaseSelectorResult.class.getName());

    /** members */
    /** OIFits collection (data source) */
    private final OIFitsCollection oiFitsCollection;
    /** granule set (insertion ordered) */
    private final HashSet<Granule> granules = new HashSet<Granule>();
    /* preserve order in selected data (per file) */
    private final LinkedHashSet<OIData> oiDatas = new LinkedHashSet<OIData>();
    /* preserve order in discarded (i.e. not selected) data (per file) */
    private LinkedHashSet<OIData> oiDatasDiscarded = null;
    /** selector to get criterias */
    private Selector selector = null;

    /** cached values */
    private ArrayList<OIData> sortedOIDatas = null;
    private List<OIData> sortedOIDatasDiscarded = null;
    private ArrayList<OIFitsFile> sortedOIFitsFiles = null;
    private ArrayList<Target> sortedTargets = null;
    private ArrayList<InstrumentMode> sortedInstrumentModes = null;
    private ArrayList<NightId> sortedNightIds = null;
    private ArrayList<String> distinctStaNames = null;
    private ArrayList<String> distinctStaConfs = null;
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

    // --- granules ---
    public final Set<Granule> getGranules() {
        return granules;
    }

    public final ArrayList<Target> getDistinctTargets() {
        if (sortedTargets == null) {
            sortedTargets = Granule.getSortedDistinctGranuleField(granules, GranuleField.TARGET);
        }
        return sortedTargets;
    }

    public final ArrayList<InstrumentMode> getDistinctInstrumentModes() {
        if (sortedInstrumentModes == null) {
            sortedInstrumentModes = Granule.getSortedDistinctGranuleField(granules, GranuleField.INS_MODE);
        }
        return sortedInstrumentModes;
    }

    public final ArrayList<NightId> getDistinctNightIds() {
        if (sortedNightIds == null) {
            sortedNightIds = Granule.getSortedDistinctGranuleField(granules, GranuleField.NIGHT);
        }
        return sortedNightIds;
    }

    /**
     * Return the unique staNames values (sorted by name) from Granules
     * @return unique staNames values (sorted by name)
     */
    public final ArrayList<String> getDistinctStaNames() {
        if (this.distinctStaNames == null) {
            this.distinctStaNames = Granule.getSortedDistinctGranuleField(granules, GranuleExtraField.DISTINCT_STA_NAMES);
        }
        return this.distinctStaNames;
    }

    /**
     * Return the unique staConfs values (sorted by name) from Granules
     * @return unique staConfs values (sorted by name)
     */
    public final ArrayList<String> getDistinctStaConfs() {
        if (this.distinctStaConfs == null) {
            this.distinctStaConfs = Granule.getSortedDistinctGranuleField(granules, GranuleExtraField.DISTINCT_STA_CONFS);
        }
        return this.distinctStaConfs;
    }

    /**
     * Get the wavelength range from Granules
     * @return the wavelength range from Granules
     */
    public final Range getWavelengthRange() {
        // lazy computation:
        if (wavelengthRange == null) {
            final Set<InstrumentMode> insModes = Granule.getDistinctGranuleField(granules, GranuleField.INS_MODE);

            this.wavelengthRange = InstrumentMode.getWavelengthRange(insModes);
        }
        return wavelengthRange;
    }

    // --- selected oidatas ---
    public final LinkedHashSet<OIData> getOIDatas() {
        return oiDatas;
    }

    public final void addSelectedOIData(final Granule g, final OIData oiData) {
        granules.add(g);
        oiDatas.add(oiData);
    }

    public final ArrayList<OIData> getSortedOIDatas() {
        if (sortedOIDatas == null) {
            final ArrayList<OIData> sortedList = new ArrayList<OIData>(oiDatas);
            Collections.sort(sortedList, OITableByFileComparator.INSTANCE);
            sortedOIDatas = sortedList;
        }
        return sortedOIDatas;
    }

    public final ArrayList<OIFitsFile> getSortedOIFitsFiles() {
        if (sortedOIFitsFiles == null) {
            sortedOIFitsFiles = OIDataListHelper.getSortedOIFitsFiles(oiDatas);
        }
        return sortedOIFitsFiles;
    }

    // --- discarded oidatas ---
    public final boolean isOIDatasDiscardedEmpty() {
        return (oiDatasDiscarded == null);
    }

    public final LinkedHashSet<OIData> getOIDatasDiscarded() {
        return oiDatasDiscarded;
    }

    public final void addDiscardedOIData(final OIData oiData) {
        if (isOIDatasDiscardedEmpty()) {
            oiDatasDiscarded = new LinkedHashSet<OIData>();
        }
        oiDatasDiscarded.add(oiData);
    }

    public final boolean isOIDataDiscarded(final OIData oiData) {
        return (oiDatasDiscarded == null) ? false : oiDatasDiscarded.contains(oiData);
    }

    /* all = selected + discarded */
    public final List<OIData> getSortedOIDatasDiscarded() {
        if (sortedOIDatasDiscarded == null) {
            final List<OIData> sortedList;
            if (isOIDatasDiscardedEmpty()) {
                sortedList = Collections.emptyList();
            } else {
                sortedList = new ArrayList<OIData>(oiDatasDiscarded);
                Collections.sort(sortedList, OITableByFileComparator.INSTANCE);
            }
            sortedOIDatasDiscarded = sortedList;
        }
        return sortedOIDatasDiscarded;
    }

    // --- statistics on oidata tables ---
    /**
     * @return total number of measurements in oidata tables
     */
    public int getNbMeasurements() {
        return OIDataListHelper.getNbMeasurements(oiDatas);
    }

    /**
     * @return total number of data points in oidata tables
     */
    public int getNbDataPoints() {
        return OIDataListHelper.getNbDataPoints(oiDatas);
    }

    /**
     * @return total number of non-flagged data points in oidata tables
     */
    public int getNbDataPointsNotFlagged() {
        return OIDataListHelper.getNbDataPointsNotFlagged(oiDatas);
    }

    // --- global state ---
    /**
     * Return the Map of sorted staNames to StaNamesDir (from OIFitsCollection)
     * @return Map of sorted staNames to StaNamesDir (from OIFitsCollection)
     */
    public final Map<String, StaNamesDir> getUsedStaNamesMap() {
        return usedStaNamesMap;
    }

    /**
     * @param usedStaNamesMap Map of sorted staNames to StaNamesDir (from OIFitsCollection)
     */
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
