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
import fr.jmmc.oitools.model.Granule.GranuleField;
import fr.jmmc.oitools.processing.Selector;
import fr.jmmc.oitools.processing.SelectorResult;
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
    /** Distinct Granules */
    private final Map<Granule, Granule> distinctGranules = new HashMap<Granule, Granule>();
    /** Set of OIData tables keyed by Granule */
    private final Map<Granule, Set<OIData>> oiDataPerGranule = new HashMap<Granule, Set<OIData>>();
    /** Map of used staNames to StaNamesDir (reference StaNames / orientation) */
    private final Map<String, StaNamesDir> usedStaNamesMap = new LinkedHashMap<String, StaNamesDir>();

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
        distinctGranules.clear();
        oiDataPerGranule.clear();
        usedStaNamesMap.clear();
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
     * Add the given OIFits file to this collection
     * @param oifitsFile OIFits file
     * @return previous OIFits file or null if not present
     */
    public OIFitsFile addOIFitsFile(final OIFitsFile oifitsFile) {
        if (oifitsFile != null) {
            final String key = getFilePath(oifitsFile);

            final OIFitsFile previous = getOIFitsFile(key);

            if (previous != null) {
                logger.log(Level.WARNING, "TODO: handle overwriting OIFitsFile : {0}", key);
                removeOIFitsFile(previous);
            }

            // analyze the given file:
            oifitsFile.analyze();

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

    private String getFilePath(final OIFitsFile oifitsFile) {
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

                // Update distinct MJD Ranges on shared granule:
                globalGranule.getDistinctMjdRanges().addAll(g.getDistinctMjdRanges());

                // Update distinct StaNames on shared granule:
                globalGranule.getDistinctStaNames().addAll(g.getDistinctStaNames());

                // TODO: keep mapping between global granule and OIFits Granules ?
                Set<OIData> oiDataTables = oiDataPerGranule.get(globalGranule);
                if (oiDataTables == null) {
                    oiDataTables = new LinkedHashSet<OIData>();
                    oiDataPerGranule.put(globalGranule, oiDataTables);
                }

                for (OIData data : entry.getValue()) {
                    oiDataTables.add(data);
                }
            }
        }

        if (logger.isLoggable(Level.FINE)) {
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
                + ", distinctMjdRanges=" + granule.getDistinctMjdRanges()
                + ", distinctStaNames=" + granule.getDistinctStaNames()
                + '}';
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

    public SelectorResult findOIData(final Selector selector) {
        return findOIData(selector, null);
    }

    public SelectorResult findOIData(final Selector selector, final SelectorResult inputResult) {
        SelectorResult result = inputResult;

        if (!this.isEmpty()) {
            // Find matching Granules:
            final List<Granule> granules = findGranules(selector);

            if (granules != null && !granules.isEmpty()) {
                result = (result != null) ? result : new SelectorResult(this);

                for (Granule g : granules) {
                    final Set<OIData> oiDatas = oiDataPerGranule.get(g);

                    if (oiDatas != null) {
                        // Apply table selection:
                        if ((selector == null) || !selector.hasTable()) {
                            // add all tables:
                            for (OIData oiData : oiDatas) {
                                result.addOIData(g, oiData);
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
                                        if (extNbs.isEmpty() || extNbs.contains(NumberUtils.valueOf(oiData.getExtNb()))) {
                                            result.addOIData(g, oiData);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (result.isEmpty()) {
                    result = null;
                }
            }

            if (result == null) {
                logger.log(Level.WARNING, "findOIData: no result matching {0}", selector);
            }
        }

        if (result != null) {
            result.setSelector(selector);
        }
        logger.log(Level.FINE, "findOIData: {0}", result);

        return result;
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
            if (selector.hasBaselines()) {
                pattern.getDistinctStaNames().addAll(selector.getBaselines());
            }

            // MJD ranges criteria:
            if (selector.hasMJDRanges()) {
                pattern.getDistinctMjdRanges().addAll(selector.getMJDRanges());
            }

            // Wavelength ranges criteria:
            if (selector.hasWavelengthRanges()) {
                pattern.getDistinctWavelengthRanges().addAll(selector.getWavelengthRanges());
            }

            if (!pattern.isEmpty()) {
                // Match Granules:
                for (Iterator<Granule> it = granules.iterator(); it.hasNext();) {
                    final Granule candidate = it.next();

                    if (!Granule.MATCHER_LIKE.match(pattern, candidate)) {
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

    static boolean match(final List<String> selected, final Set<String> candidates) {
        for (String sel : selected) {
            if (candidates.contains(sel)) {
                return true;
            }
        }
        return false;
    }
}
