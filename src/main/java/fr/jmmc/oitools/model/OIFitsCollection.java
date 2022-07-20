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
package fr.jmmc.oitools.model;

import fr.jmmc.jmcs.util.NumberUtils;
import fr.jmmc.jmcs.util.ObjectUtils;
import fr.jmmc.jmcs.util.ToStringable;
import fr.jmmc.oitools.OIFitsConstants;
import fr.jmmc.oitools.fits.FitsTable;
import fr.jmmc.oitools.meta.ColumnMeta;
import fr.jmmc.oitools.meta.WaveColumnMeta;
import fr.jmmc.oitools.model.Granule.GranuleField;
import fr.jmmc.oitools.model.range.Range;
import fr.jmmc.oitools.processing.Double1DFilter;
import fr.jmmc.oitools.processing.Double2DFilter;
import fr.jmmc.oitools.processing.FitsTableFilter;
import fr.jmmc.oitools.processing.FitsTableFilter.FilterState;
import fr.jmmc.oitools.processing.NightIdFilter;
import fr.jmmc.oitools.processing.Selector;
import fr.jmmc.oitools.processing.SelectorResult;
import fr.jmmc.oitools.processing.StaConfFilter;
import fr.jmmc.oitools.processing.StaIndexFilter;
import fr.jmmc.oitools.processing.TargetUIDFilter;
import fr.jmmc.oitools.util.GranuleComparator;
import fr.jmmc.oitools.util.OIFitsFileComparator;
import fr.nom.tam.fits.FitsException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manage data collection and provide utility methods.
 */
public final class OIFitsCollection implements ToStringable {

    /** logger */
    protected final static Logger logger = Logger.getLogger(OIFitsCollection.class.getName());

    /** flag to allow fixing bad UID for single matches */
    private final static boolean FIX_BAD_UID_FOR_SINGLE_MATCH;

    static {
        FIX_BAD_UID_FOR_SINGLE_MATCH = System.getProperty("fix.bad.uid", "false").equalsIgnoreCase("true");

        if (FIX_BAD_UID_FOR_SINGLE_MATCH) {
            logger.warning("OIFitsCollection: FIX_BAD_UID_FOR_SINGLE_MATCH enabled !");
        }
    }
    /* members */
    /** InstrumentMode manager */
    private final InstrumentModeManager imm;
    /** Target manager */
    private final TargetManager tm;
    /** OIFits file collection keyed by absolute file path (unordered) */
    private final Map<String, OIFitsFile> oiFitsPerPath = new HashMap<String, OIFitsFile>();
    /** Set of all OIData tables */
    private final Set<OIData> allOiDatas = new LinkedHashSet<OIData>();
    /** Distinct Granules */
    private final Map<Granule, Granule> distinctGranules = new HashMap<Granule, Granule>();
    /** Set of OIData tables keyed by Granule */
    private final Map<Granule, Set<OIData>> oiDataPerGranule = new HashMap<Granule, Set<OIData>>();
    /** Map of used staNames to StaNamesDir (reference StaNames / orientation) */
    private final Map<String, StaNamesDir> usedStaNamesMap = new LinkedHashMap<String, StaNamesDir>();
    /** cached values */
    private List<String> distinctStaNames = null;
    private List<String> distinctStaConfs = null;
    private final Map<String, Range> columnRanges = new HashMap<>(32);

    public static OIFitsCollection create(final OIFitsChecker checker, final List<String> fileLocations) throws IOException, MalformedURLException, FitsException {
        final OIFitsCollection oiFitsCollection = new OIFitsCollection();

        // load files:
        for (String fileLocation : fileLocations) {
            oiFitsCollection.addOIFitsFile(OIFitsLoader.loadOIFits(checker, fileLocation));
        }
        oiFitsCollection.analyzeCollection();

        return oiFitsCollection;
    }

    public static OIFitsCollection create(final OIFitsFile... oiFitsFiles) {
        final OIFitsCollection oiFitsCollection = new OIFitsCollection();

        // add files:
        for (int i = 0; i < oiFitsFiles.length; i++) {
            oiFitsCollection.addOIFitsFile(oiFitsFiles[i]);
        }
        oiFitsCollection.analyzeCollection();

        return oiFitsCollection;
    }

    /**
     * Public constructor
     */
    public OIFitsCollection() {
        this.imm = InstrumentModeManager.newInstance();
        this.tm = TargetManager.newInstance();
    }

    /**
     * Return the instrument mode manager
     * @return InstrumentModeManager instance
     */
    public InstrumentModeManager getInstrumentModeManager() {
        return imm;
    }

    /**
     * Return the Target manager
     * @return TargetManager instance
     */
    public TargetManager getTargetManager() {
        return tm;
    }

    /**
     * Clear the OIFits file collection
     */
    public void clear() {
        // clear all loaded OIFitsFile (in memory):
        oiFitsPerPath.clear();

        clearCache();
    }

    /**
     * Clear the cached meta-data
     */
    public void clearCache() {
        // clear InstrumentMode mappings:
        imm.clear();
        // clear Target mappings:
        tm.clear();
        // clear granules:
        allOiDatas.clear();
        distinctGranules.clear();
        oiDataPerGranule.clear();
        usedStaNamesMap.clear();

        distinctStaNames = null;
        distinctStaConfs = null;
        columnRanges.clear();
    }

    public boolean isEmpty() {
        return oiFitsPerPath.isEmpty();
    }

    public int size() {
        return oiFitsPerPath.size();
    }

    public Collection<OIFitsFile> getOIFitsFiles() {
        return oiFitsPerPath.values();
    }

    public List<OIFitsFile> getSortedOIFitsFiles() {
        return getSortedOIFitsFiles(OIFitsFileComparator.INSTANCE);
    }

    public List<OIFitsFile> getSortedOIFitsFiles(final Comparator<OIFitsFile> comparator) {
        final List<OIFitsFile> oiFitsFiles = new ArrayList<OIFitsFile>(getOIFitsFiles());
        Collections.sort(oiFitsFiles, comparator);
        return oiFitsFiles;
    }

    /**
     * Add or replace the given OIFits file to this collection given its file path
     * @param oifitsFile OIFits file
     * @return previous OIFits file or null if not present
     */
    public OIFitsFile addOIFitsFile(final OIFitsFile oifitsFile) {
        if (oifitsFile != null) {
            // analyze the given file:
            oifitsFile.analyze();

            final String key = getFilePath(oifitsFile);
            final OIFitsFile previous = getOIFitsFile(key);

            // update loaded OIFitsFile (in memory):
            oiFitsPerPath.put(key, oifitsFile);

            logger.log(Level.FINE, "addOIFitsFile: {0}", oifitsFile);

            return previous;
        }
        return null;
    }

    public OIFitsFile getOIFitsFile(final String absoluteFilePath) {
        if (absoluteFilePath != null) {
            return oiFitsPerPath.get(absoluteFilePath);
        }
        return null;
    }

    public OIFitsFile removeOIFitsFile(final OIFitsFile oifitsFile) {
        if (oifitsFile != null) {
            final String key = getFilePath(oifitsFile);
            final OIFitsFile previous = oiFitsPerPath.remove(key);

            return previous;
        }
        return null;
    }

    public static String getFilePath(final OIFitsFile oifitsFile) {
        if (oifitsFile.getAbsoluteFilePath() == null) {
            // TODO: remove asap
            throw new IllegalStateException("Undefined OIFitsFile.absoluteFilePath !");
        }
        return oifitsFile.getAbsoluteFilePath();
    }

    /**
     * toString() implementation wrapper to get complete information
     * Note: prefer using @see #toString(java.lang.StringBuilder) instead
     * @return string representation
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(256);
        toString(sb, false);
        return sb.toString();
    }

    /**
     * toString() implementation using string builder
     * 
     * @param sb string builder to append to
     * @param full true to get complete information; false to get main information (shorter)
     */
    @Override
    public void toString(final StringBuilder sb, final boolean full) {
        ObjectUtils.getObjectInfo(sb, this);

        sb.append("{files= ").append(this.oiFitsPerPath.keySet());

        if (full) {
            if (this.oiDataPerGranule != null) {
                sb.append(", oiFitsPerGranule= ");
                ObjectUtils.toString(sb, full, this.oiDataPerGranule);
            }
        }
        sb.append('}');
    }

    /* --- data analysis --- */
    /**
     * Analyze the complete OIFits collection to provide OIFits structure per unique target (name)
     */
    public void analyzeCollection() {
        clearCache();

        final List<OIFitsFile> oiFitsFiles = getSortedOIFitsFiles();

        // analyze instrument modes & targets & StaNames:
        for (OIFitsFile oiFitsFile : oiFitsFiles) {
            for (OIWavelength oiTable : oiFitsFile.getOiWavelengths()) {
                imm.register(oiTable.getInstrumentMode());
            }

            if (oiFitsFile.hasOiTarget()) {
                for (Target target : oiFitsFile.getOiTarget().getTargetSet()) {
                    tm.register(target);
                }
            }

            // Merge usedStaNamesMap:
            usedStaNamesMap.putAll(oiFitsFile.getUsedStaNamesMap());
        }

        imm.dump();
        tm.dump();

        // Build the index between global Granule and a fake OIFitsFile structure (to gather OIData) 
        for (OIFitsFile oiFitsFile : oiFitsFiles) {
            // reused Granule:
            Granule gg = new Granule();

            for (Map.Entry<Granule, Set<OIData>> entry : oiFitsFile.getOiDataPerGranule().entrySet()) {

                // Relations between global Granule and OIFits Granules ?
                final Granule g = entry.getKey();

                // create global granule with matching global target & instrument mode:
                final Target globalTarget = tm.getGlobal(g.getTarget());
                final InstrumentMode globalInsMode = imm.getGlobal(g.getInsMode());

                gg.set(globalTarget, globalInsMode, g.getNight());

                Granule globalGranule = distinctGranules.get(gg);
                if (globalGranule == null) {
                    distinctGranules.put(gg, gg);
                    globalGranule = gg;
                    gg = new Granule();
                }

                // Update distinct StaNames on shared granule:
                globalGranule.getDistinctStaNames().addAll(g.getDistinctStaNames());

                // Update MJD Range on shared granule:
                globalGranule.updateMjdRange(g.getMjdRange());

                // keep mapping between global granule and OIData tables:
                Set<OIData> oiDataTables = oiDataPerGranule.get(globalGranule);
                if (oiDataTables == null) {
                    oiDataTables = new LinkedHashSet<OIData>();
                    oiDataPerGranule.put(globalGranule, oiDataTables);
                }

                for (OIData data : entry.getValue()) {
                    allOiDatas.add(data);
                    oiDataTables.add(data);
                }
            }
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "analyzeCollection: allOiDatas: {0}", allOiDatas);
            logger.log(Level.FINE, "analyzeCollection: usedStaNamesMap: {0}", usedStaNamesMap);
            logger.log(Level.FINE, "analyzeCollection: Granule / OIData tables: {0}", distinctGranules.keySet());
            logger.log(Level.FINE, "analyzeCollection: Sorted Granules:");

            for (Granule granule : getSortedGranules()) {
                logger.log(Level.FINE, "analyzeCollection: {0}", detailedGranuletoString(granule));
            }
        }
    }

    public String detailedGranuletoString(Granule granule) {
        return "Granule{target=" + granule.getTarget()
                + " [aliases: " + tm.getSortedUniqueAliases(granule.getTarget()) + "]"
                + ", insMode=" + granule.getInsMode()
                + " [aliases: " + imm.getSortedUniqueAliases(granule.getInsMode()) + "]"
                + ", night=" + granule.getNight()
                + ", mjdRange=" + granule.getMjdRange()
                + ", distinctStaNames=" + granule.getDistinctStaNames()
                + '}';
    }

    /**
     * Return all OIData tables
     * @return all OIData tables
     */
    public Set<OIData> getAllOiDatas() {
        return allOiDatas;
    }

    /**
     * Return the Distinct Granules
     * @return Distinct Granules
     */
    public Map<Granule, Granule> getDistinctGranules() {
        return distinctGranules;
    }

    /**
     * Return the Set of OIData tables keyed by Granule
     * @return Set of OIData tables keyed by Granule
     */
    public Map<Granule, Set<OIData>> getOiDataPerGranule() {
        return oiDataPerGranule;
    }

    /**
     * Return the Map of sorted staNames to StaNamesDir
     * @return Map of sorted staNames to StaNamesDir
     */
    public Map<String, StaNamesDir> getUsedStaNamesMap() {
        return usedStaNamesMap;
    }

    public List<Granule> getSortedGranules() {
        return getSortedGranules(GranuleComparator.DEFAULT);
    }

    public List<Granule> getSortedGranules(final Comparator<Granule> comparator) {
        final List<Granule> granules = new ArrayList<Granule>(distinctGranules.keySet());
        Collections.sort(granules, comparator);

        logger.log(Level.FINE, "granules sorted: {0}", granules);

        return granules;
    }

    // --- statistics on all OIFITS collection ---
    /**
     * Return the unique staNames values (sorted by name) from all OIData tables
     * @return unique staNames values (sorted by name)
     */
    public List<String> getDistinctStaNames() {
        if (this.distinctStaNames == null) {
            this.distinctStaNames = OIDataListHelper.getDistinctStaNames(getAllOiDatas(), getUsedStaNamesMap());
        }
        return this.distinctStaNames;
    }

    /**
     * Return the unique staConfs values (sorted by name) from all OIData tables
     * @return unique staConfs values (sorted by name)
     */
    public List<String> getDistinctStaConfs() {
        if (this.distinctStaConfs == null) {
            this.distinctStaConfs = OIDataListHelper.getDistinctStaConfs(getAllOiDatas());
        }
        return this.distinctStaConfs;
    }

    /**
     * Return the unique values (sorted) from all OIData tables
     * @param name column name to extract values
     * @return unique values (sorted)
     */
    public List<String> getDistinctValues(final String name) {
        if (OIFitsConstants.COLUMN_STA_INDEX.equals(name)) {
            return getDistinctStaNames();
        }
        if (OIFitsConstants.COLUMN_STA_CONF.equals(name)) {
            return getDistinctStaConfs();
        }
        return null;
    }

    /**
     * Return the global column range from all OIData tables
     * @param name column name to extract values
     * @return global column range or Range.UNDEFINED_RANGE if no data
     */
    public Range getColumnRange(final String name) {
        Range r = columnRanges.get(name);
        if (r == null) {
            r = OIDataListHelper.getColumnRange(getAllOiDatas(), name);
            columnRanges.put(name, r);
        }
        return r;
    }

    // --- Query API ---
    /**
     * Query this collection with the given query criteria:
     * - find Granules (target UID, insmode UID, night ID)
     * - use selector to perform advanced filters (tables ...)
     * - compute index masks to easily identify matching table data (rows, cols)
     * The returned SelectorResult instance contains matching granules and OIData (on all criteria)
     * 
     * @param selector query criteria
     * @return SelectorResult instance
     */
    public SelectorResult findOIData(final Selector selector) {
        return findOIData(selector, null);
    }

    /**
     * Query this collection with the given query criteria:
     * - find Granules (target UID, insmode UID, night ID)
     * - use selector to perform advanced filters (tables ...)
     * - compute index masks to easily identify matching table data (rows, cols)
     * The returned SelectorResult instance contains matching granules and OIData (on all criteria)
     * 
     * @param selector query criteria
     * @param inputResult optional SelectorResult instance to store query results
     * @return SelectorResult instance
     */
    public SelectorResult findOIData(final Selector selector, final SelectorResult inputResult) {
        SelectorResult result = inputResult;

        logger.log(Level.FINE, "findOIData: selector = {0}", selector);

        if (!this.isEmpty()) {
            final long start = System.nanoTime();

            // Find matching Granules:
            final List<Granule> granules = findGranules(selector);

            if (granules != null && !granules.isEmpty()) {
                result = (result != null) ? result : new SelectorResult(this);

                // Prepare filters (once) in selectorResult:
                if (selector != null) {
                    // Create the filter chain:
                    {
                        // OIData filters:
                        final List<FitsTableFilter<?>> filtersData1D = result.getFiltersOIData1D();
                        filtersData1D.clear();
                        // subset filters:
                        if (selector.getTargetUID() != null) {
                            filtersData1D.add(new TargetUIDFilter(tm, selector.getTargetUID()));
                        }
                        // insModeUID: useless as granule already handled this criteria
                        if (selector.getNightID() != null) {
                            filtersData1D.add(new NightIdFilter(selector.getNightID()));
                        }
                        // selector.tables: see OIData filtering below

                        // generic filters:
                        if (selector.hasFilter(Selector.FILTER_STAINDEX)) {
                            filtersData1D.add(new StaIndexFilter(getUsedStaNamesMap(),
                                    selector.getFilter(Selector.FILTER_STAINDEX)));
                        }
                        if (selector.hasFilter(Selector.FILTER_STACONF)) {
                            filtersData1D.add(new StaConfFilter(
                                    selector.getFilter(Selector.FILTER_STACONF)));
                        }
                        if (selector.hasFilter(Selector.FILTER_MJD)) {
                            filtersData1D.add(new Double1DFilter(Selector.FILTER_MJD,
                                    selector.getFilter(Selector.FILTER_MJD)));
                        }

                        // convert generic filters from selector.filters (1D or 2D):
                        final List<FitsTableFilter<?>> filtersData2D = result.getFiltersOIData2D();
                        filtersData2D.clear();

                        // Use shared data model (OIFITS2):
                        // chicken & egg problem to use custom expression columns (how to get them):
                        final DataModel dataModel = DataModel.getInstance();

                        for (Map.Entry<String, List<?>> e : selector.getFiltersMap().entrySet()) {
                            if (!Selector.isCustomFilter(e.getKey())) {
                                if (dataModel.isNumericalColumn1D(e.getKey())) {
                                    filtersData1D.add(new Double1DFilter(e.getKey(), (List<Range>) e.getValue()));
                                } else {
                                    filtersData2D.add(new Double2DFilter(e.getKey(), (List<Range>) e.getValue()));
                                }
                            }
                        }
                        logger.log(Level.FINE, "filtersData1D: {0} ", filtersData1D);
                        logger.log(Level.FINE, "filtersData2D: {0} ", filtersData2D);
                    }
                    {
                        // Wavelength filters:
                        final List<FitsTableFilter<?>> filtersWL = result.getFiltersOIWavelength();
                        filtersWL.clear();

                        // convert generic filters from selector.filters (WAVELENGTH)
                        for (Map.Entry<String, List<?>> e : selector.getFiltersMap().entrySet()) {
                            if (Selector.isCustomFilterOnWavelengths(e.getKey())) {
                                filtersWL.add(new Double1DFilter(e.getKey(), (List<Range>) e.getValue()));
                            }
                        }
                        logger.log(Level.FINE, "filtersWL:     {0} ", filtersWL);
                    }
                }

                for (Granule g : granules) {
                    final Set<OIData> oiDatas = oiDataPerGranule.get(g);

                    if (oiDatas != null) {
                        // Apply table selection:
                        if ((selector == null) || !selector.hasTable()) {
                            // add all tables:
                            for (OIData oiData : oiDatas) {
                                filterOIData(result, g, oiData);
                            }
                        } else {
                            // test all data tables:
                            for (OIData oiData : oiDatas) {
                                // file path comparison:
                                final String oiFitsPath = oiData.getOIFitsFile().getAbsoluteFilePath();
                                if (oiFitsPath != null) {
                                    final List<Integer> extNbs = selector.getTables(oiFitsPath);
                                    // null means the path does not match:
                                    if (extNbs != null) {
                                        // extNb is null means add all tables from file
                                        if (extNbs.isEmpty()
                                                || extNbs.contains(NumberUtils.valueOf(oiData.getExtNb()))) {
                                            filterOIData(result, g, oiData);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (result.isEmpty()) {
                    result = null;
                } else {
                    // Cleanup result:
                    result.resetFilters();
                }
            }
            if (logger.isLoggable(Level.INFO)) {
                logger.log(Level.INFO, "findOIData: duration = {0} ms.", 1e-6d * (System.nanoTime() - start));
            }
            if (result == null) {
                logger.log(Level.FINE, "findOIData: no result matching {0}", selector);
            }
        }
        if (result != null) {
            result.setSelector(selector);
        }
        logger.log(Level.FINE, "findOIData: {0}", result);

        return result;
    }

    /**
     * Adds the OIData and the Granule to the SelectorResults once filtered:
     * computes eventual masks (IndexMask) from the given SelectorResult
     *
     * @param result SelectorResult to store OIData, Granule and IndexMask instances
     * @param g the granule to add
     * @param oiData the oiData table to filter
     */
    private void filterOIData(final SelectorResult result, final Granule g, final OIData oiData) {
        logger.log(Level.FINE, "filterOIData: oiData = {0}", oiData);

        // apply filters on OIData:
        // 1. OIWavelength filters:
        final OIWavelength oiWavelength = oiData.getOiWavelength();
        IndexMask maskWavelength;

        if (result.hasFiltersOIWavelength()) {
            if (oiWavelength == null) {
                // no related OIWavelength table: 
                logger.log(Level.FINE, "No OIWavelength for table {0}", oiData);
                // skip OIData (no match):
                return;
            }
            // oiWavelength already processed ?
            maskWavelength = result.getWavelengthMask(oiWavelength);
            if (maskWavelength == null) {
                maskWavelength = computeMask1D(oiWavelength,
                        result.getFiltersOIWavelength(), result.getFiltersUsed()
                );
                if (maskWavelength == null) {
                    // skip OIData (no remaining row):
                    return;
                }
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, "wlen filters: {0}", result.getFiltersUsed());
                    logger.log(Level.FINE, "wlenMask: {0}", maskWavelength);
                }
                result.putWavelengthMask(oiWavelength, maskWavelength);
            }
        } else {
            // if selector has no wavelength ranges, use FULL mask
            if (oiWavelength == null) {
                maskWavelength = IndexMask.FULL;
            } else {
                // oiWavelength already processed ?
                maskWavelength = result.getWavelengthMask(oiWavelength);
                if (maskWavelength == null) {
                    maskWavelength = IndexMask.FULL;
                    result.putWavelengthMask(oiWavelength, maskWavelength);
                }
            }
        }

        // 2. OIData filters:
        IndexMask maskRows = null;

        if (result.hasFiltersOIData1D()) {
            maskRows = computeMask1D(oiData,
                    result.getFiltersOIData1D(), result.getFiltersUsed()
            );
            if (maskRows == null) {
                // skip OIData (no remaining row):
                return;
            }
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "oidata filters: {0}", result.getFiltersUsed());
                logger.log(Level.FINE, "maskRows: {0}", maskRows);
            }
            result.putDataMask1D(oiData, maskRows);
        }
        if (result.hasFiltersOIData2D()) {
            final IndexMask mask2D = computeMask2D(oiData,
                    IndexMask.isNotFull(maskRows) ? maskRows : null,
                    IndexMask.isNotFull(maskWavelength) ? maskWavelength : null,
                    result.getFiltersOIData2D(), result.getFiltersUsed()
            );
            if (mask2D == null) {
                // skip OIData (no remaining row):
                return;
            }

            // Get column dependency (expression dynamic columns):
            final Set<String> usedColumnsFiltersOIData2D = result.getUsedColumnsFiltersOIData2D();
            final Set<String> relatedColumnsFiltersOIData2D = result.getRelatedColumnsFiltersOIData2D();

            boolean changed = false;

            for (FitsTableFilter<?> usedFilter : result.getFiltersUsed()) {
                final String colName = usedFilter.getColumnName();

                if (usedColumnsFiltersOIData2D.add(colName)) {
                    changed = true;

                    ColumnMeta colMeta = oiData.getColumnDesc(colName);
                    if (logger.isLoggable(Level.FINE)) {
                        logger.log(Level.FINE, "colMeta: {0}", colMeta);
                    }

                    if (colMeta != null) {
                        relatedColumnsFiltersOIData2D.add(colMeta.getName());
                        if (colMeta.getDataColumnName() != null) {
                            relatedColumnsFiltersOIData2D.add(colMeta.getDataColumnName());
                        }
                    } else {
                        colMeta = oiData.getColumnDerivedDesc(usedFilter.getColumnName());
                        if (logger.isLoggable(Level.FINE)) {
                            logger.log(Level.FINE, "derived colMeta: {0}", colMeta);
                        }

                        if (colMeta instanceof WaveColumnMeta) {
                            final WaveColumnMeta colMetaExpr = ((WaveColumnMeta) colMeta);

                            if (colMetaExpr.hasRelatedColumnNames()) {
                                final Set<String> relatedColumnNames = ((WaveColumnMeta) colMeta).getRelatedColumnNames();
                                if (logger.isLoggable(Level.FINE)) {
                                    logger.log(Level.FINE, "relatedColumnNames: {0}", relatedColumnNames);
                                }

                                for (String relatedColName : relatedColumnNames) {
                                    // resolve column:
                                    ColumnMeta colMetaRel = oiData.getColumnDesc(relatedColName);
                                    if (colMetaRel != null) {
                                        relatedColumnsFiltersOIData2D.add(colMetaRel.getName());
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (logger.isLoggable(Level.FINE)) {
                if (changed) {
                    logger.log(Level.FINE, "usedColumnsFiltersOIData2D: {0}", usedColumnsFiltersOIData2D);
                    logger.log(Level.FINE, "relatedColumnsFiltersOIData2D: {0}", relatedColumnsFiltersOIData2D);
                }
                logger.log(Level.FINE, "oidata filters: {0}", result.getFiltersUsed());
                logger.log(Level.FINE, "mask2D: {0}", mask2D);
            }
            result.putDataMask2D(oiData, mask2D);
        }
        result.addOIData(g, oiData);
    }

    private IndexMask computeMask1D(final FitsTable fitsTable,
                                    final List<FitsTableFilter<?>> filters,
                                    final List<FitsTableFilter<?>> usedFilters) {

        logger.log(Level.FINE, "computeMask1D: filters = {0}", filters);

        FilterState chainState = FilterState.FULL;
        usedFilters.clear();

        // optimized loop:
        for (int f = 0, len = filters.size(); f < len; f++) {
            final FitsTableFilter<?> filter = filters.get(f);
            // Prepare filter:
            final FilterState state = filter.prepare(fitsTable);

            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "{0} => {1}", new Object[]{filter, state});
            }

            if (state.ordinal() < chainState.ordinal()) {
                // min:
                chainState = state;

                // shortcut if invalid found:
                if (chainState == FilterState.INVALID) {
                    logger.log(Level.FINE, "Skip {0}, not matching filters", fitsTable);
                    // skip OIData (no match):
                    return null;
                }
            }
            if (state == FilterState.MASK) {
                usedFilters.add(filter);
            }
        }

        if (chainState == FilterState.FULL) {
            // skip filter later in OIData:
            return IndexMask.FULL;
        }

        // chainState is MASK:
        final int nFilters = usedFilters.size();
        final int nRows = fitsTable.getNbRows();

        // prepare 1D mask to indicate rows to keep in the table:
        final IndexMask maskRows = new IndexMask(nRows); // bits set to false by default
        boolean doFilter = false;

        // Iterate on table rows (i):
        for (int i = 0; i < nRows; i++) {
            boolean match = true;

            for (int f = 0; f < nFilters; f++) {
                // Process filter:
                if (!usedFilters.get(f).accept(i, 0)) {
                    match = false;
                    break;
                }
            }
            // update mask:
            if (match) {
                maskRows.setAccept(i, true);
            } else {
                // data (row) does not correspond to selected ranges: 
                doFilter = true;
            }
        }
        if (doFilter) {
            final int nKeepRows = maskRows.cardinality();

            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "computeMask1D: nKeepRows: {0} / {1}",
                        new Object[]{nKeepRows, nRows});
            }

            if (nKeepRows <= 0) {
                // skip OIData (no remaining row):
                return null;
            } else if (nKeepRows == nRows) {
                // skip filter later in OIData:
                return IndexMask.FULL;
            }
            return maskRows;
        }
        // skip filter later in OIData:
        return IndexMask.FULL;
    }

    private IndexMask computeMask2D(final OIData oiData,
                                    final IndexMask maskRows, final IndexMask maskWavelength,
                                    final List<FitsTableFilter<?>> filters,
                                    final List<FitsTableFilter<?>> usedFilters) {

        logger.log(Level.FINE, "computeMask2D: filters = {0}", filters);

        FilterState chainState = FilterState.FULL;
        usedFilters.clear();

        // optimized loop:
        for (int f = 0, len = filters.size(); f < len; f++) {
            final FitsTableFilter<?> filter = filters.get(f);
            // Prepare filter:
            final FilterState state = filter.prepare(oiData);

            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "{0} => {1}", new Object[]{filter, state});
            }

            if (state.ordinal() < chainState.ordinal()) {
                // min:
                chainState = state;

                // shortcut if invalid found:
                if (chainState == FilterState.INVALID) {
                    logger.log(Level.FINE, "Skip {0}, not matching filters", oiData);
                    // skip OIData (no match):
                    return null;
                }
            }
            if (state == FilterState.MASK) {
                usedFilters.add(filter);
            }
        }

        final int nWaves = oiData.getNWave();

        if ((chainState == FilterState.FULL) || (nWaves == 0)) {
            // missing column or no data, ignore filter:
            return IndexMask.FULL;
        }

        // chainState is MASK:
        final int nFilters = usedFilters.size();
        final int nRows = oiData.getNbRows();

        final int acceptedRows = (maskRows != null) ? maskRows.cardinality() : nRows;
        final int acceptedWaves = (maskWavelength != null) ? maskWavelength.cardinality() : nWaves;

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "computeMask2D: acceptedRows = {0}", acceptedRows);
            logger.log(Level.FINE, "computeMask2D: acceptedWaves = {0}", acceptedWaves);
        }

        // prepare 2D mask to indicate rows to keep in the table:
        // each mask row has 2 more bits to encode NONE/FULL row:
        final int nCols = nWaves + 2;
        final IndexMask mask2D = new IndexMask(nRows, nCols); // bits set to false by default

        final int idxNone = mask2D.getIndexNone();
        final int idxFull = mask2D.getIndexFull();

        boolean doFilter = false;
        int nKeepCells = 0;

        // Iterate on table rows (i):
        for (int i = 0; i < nRows; i++) {

            // check optional data mask 1D:
            if ((maskRows != null) && !maskRows.accept(i)) {
                // if bit is false for this row, we hide this row
                continue;
            }

            int nKeepWaves = 0;

            // Iterate on wave channels (l):
            for (int l = 0; l < nWaves; l++) {

                // check optional wavelength mask:
                if ((maskWavelength != null) && !maskWavelength.accept(l)) {
                    // if bit is false for this row, we hide this row
                    continue;
                }

                boolean match = true;

                for (int f = 0; f < nFilters; f++) {
                    // Process filter:
                    if (!usedFilters.get(f).accept(i, l)) {
                        match = false;
                        break;
                    }
                }
                // update mask:
                if (match) {
                    nKeepWaves++;
                    mask2D.setAccept(i, l, true);
                } else {
                    // data (row, col) does not correspond to selected ranges:
                    doFilter = true;
                }
            } // wave channels

            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "computeMask2D: nKeepWaves row[{0}] = {1}",
                        new Object[]{i, nKeepWaves});
            }

            // set global flag on row:
            if (nKeepWaves <= 0) {
                // skip all Row (no remaining col):
                mask2D.setAccept(i, idxNone, true);
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, "computeMask2D: row[{0}] = NONE", i);
                }
            } else if (nKeepWaves == acceptedWaves) {
                // keep all Row (all remaining col):
                mask2D.setAccept(i, idxFull, true);
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, "computeMask2D: row[{0}] = FULL", i);
                }
            }
            nKeepCells += nKeepWaves;
        } // rows

        if (doFilter) {
            final int allCells = acceptedRows * acceptedWaves;

            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "computeMask2D: nKeepCells: {0} / {1}",
                        new Object[]{nKeepCells, allCells});
            }

            if (nKeepCells <= 0) {
                // skip OIData (no remaining row):
                return null;
            } else if (nKeepCells == allCells) {
                // skip filter later in OIData:
                return IndexMask.FULL;
            }
            return mask2D;
        }
        // skip filter later in OIData:
        return IndexMask.FULL;
    }

    private List<Granule> findGranules(final Selector selector) {
        final List<Granule> granules = getSortedGranules();

        if (selector != null && !selector.isEmpty()) {
            boolean badTargetUID = false;
            boolean badInsModeUID = false;

            // null if no match or targetUID / insModeUID is undefined :
            final Target target;
            if (selector.getTargetUID() != null) {
                target = tm.getGlobalByUID(selector.getTargetUID());

                if (target == null) {
                    if (FIX_BAD_UID_FOR_SINGLE_MATCH) {
                        badTargetUID = true;
                        logger.log(Level.WARNING, "Bad UID, discarding targetUID: [{0}]", selector.getTargetUID());
                    } else {
                        // no match means no granule matching
                        return null;
                    }
                }
            } else {
                target = null;
            }
            final InstrumentMode insMode;
            if (selector.getInsModeUID() != null) {
                insMode = imm.getGlobalByUID(selector.getInsModeUID());

                if (insMode == null) {
                    if (FIX_BAD_UID_FOR_SINGLE_MATCH) {
                        badInsModeUID = true;
                        logger.log(Level.WARNING, "Bad UID, discarding insModeUID: [{0}]", selector.getInsModeUID());
                    } else {
                        // no match means no granule matching
                        return null;
                    }
                }
            } else {
                insMode = null;
            }

            final NightId nightId;
            if (selector.getNightID() != null) {
                nightId = NightId.getCachedInstance(selector.getNightID());
            } else {
                nightId = null;
            }

            final Granule pattern = new Granule(target, insMode, nightId);

            // Baselines criteria:
            if (selector.hasFilter(Selector.FILTER_STAINDEX)) {
                pattern.getDistinctStaNames().addAll(selector.getFilter(Selector.FILTER_STAINDEX));
            }

            // MJD & Wavelength ranges criteria:
            final GranuleMatcher granuleMatcher = GranuleMatcher.getInstance(
                    (selector.hasFilter(Selector.FILTER_MJD))
                    ? new LinkedHashSet<Range>(selector.getFilter(Selector.FILTER_MJD)) : null,
                    (selector.hasFilter(Selector.FILTER_EFFWAVE))
                    ? new LinkedHashSet<Range>(selector.getFilter(Selector.FILTER_EFFWAVE)) : null
            );

            if (!pattern.isEmpty() || !granuleMatcher.isEmpty()) {
                // Match Granules:
                for (Iterator<Granule> it = granules.iterator(); it.hasNext();) {
                    final Granule candidate = it.next();

                    if (!granuleMatcher.match(pattern, candidate)) {
                        it.remove();
                    }
                }
            }

            if (!granules.isEmpty()) {
                if (badTargetUID) {
                    // check if selected Granules only have 1 target:
                    final Set<Target> targets = Granule.getDistinctGranuleField(granules, GranuleField.TARGET);
                    if (targets.size() != 1) {
                        logger.log(Level.WARNING, "Multiple target match (incompatible with targetUID: {0}): {1}",
                                new Object[]{selector.getTargetUID(), targets});
                        // ambiguous match => no granule matching
                        return null;
                    }
                }
                if (badInsModeUID) {
                    // check if selected Granules only have 1 instrument mode:
                    final Set<InstrumentMode> insModes = Granule.getDistinctGranuleField(granules, GranuleField.INS_MODE);
                    if (insModes.size() != 1) {
                        logger.log(Level.WARNING, "Multiple instrument mode match (incompatible with insModeUID: {0}): {1}",
                                new Object[]{selector.getInsModeUID(), insModes});
                        // ambiguous match => no granule matching
                        return null;
                    }
                }
            }
        }
        return granules;
    }
}
