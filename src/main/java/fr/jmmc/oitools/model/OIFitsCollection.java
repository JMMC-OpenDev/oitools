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

import fr.jmmc.jmcs.util.ObjectUtils;
import fr.jmmc.jmcs.util.ToStringable;
import fr.jmmc.oitools.util.GranuleComparator;
import fr.jmmc.oitools.util.OIFitsFileComparator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
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
    /** Target manager */
    private final static TargetManager tm = TargetManager.getInstance();
    /** instrument mode manager */
    private final static InstrumentModeManager imm = InstrumentModeManager.getInstance();
    /* members */
    /** OIFits file collection keyed by absolute file path (unordered) */
    private final Map<String, OIFitsFile> oiFitsPerPath = new HashMap<String, OIFitsFile>();
    /** Set of OIData tables keyed by Granule */
    private final Map<Granule, Set<OIData>> oiDataPerGranule = new HashMap<Granule, Set<OIData>>();

    /**
     * Public constructor
     */
    public OIFitsCollection() {
        super();
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
        // clear Target mappings:
        tm.clear();
        // clear insMode mappings:
        imm.clear();
        // clear granules:
        oiDataPerGranule.clear();
    }

    public boolean isEmpty() {
        return oiFitsPerPath.isEmpty();
    }

    public int size() {
        return oiFitsPerPath.size();
    }

    public List<OIFitsFile> getSortedOIFitsFiles() {
        return getSortedOIFitsFiles(OIFitsFileComparator.INSTANCE);
    }

    public List<OIFitsFile> getSortedOIFitsFiles(final Comparator<OIFitsFile> comparator) {
        final List<OIFitsFile> oiFitsFiles = new ArrayList<OIFitsFile>(oiFitsPerPath.values());
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

        // analyze instrument modes & targets:
        for (OIFitsFile oiFitsFile : oiFitsFiles) {
            for (OIWavelength oiTable : oiFitsFile.getOiWavelengths()) {
                imm.register(oiTable.getInstrumentMode());
            }

            if (oiFitsFile.hasOiTarget()) {
                for (Target target : oiFitsFile.getOiTarget().getTargetSet()) {
                    tm.register(target);
                }
            }
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

                // TODO: keep mapping between global granule and OIFits Granules ?
                Set<OIData> oiDataTables = oiDataPerGranule.get(gg);
                if (oiDataTables == null) {
                    oiDataTables = new LinkedHashSet<OIData>();
                    oiDataPerGranule.put(gg, oiDataTables);
                    gg = new Granule();
                }

                for (OIData data : entry.getValue()) {
                    oiDataTables.add(data);
                }
            }
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "analyzeCollection: Granules: {0}", oiDataPerGranule.keySet());
        }
    }

    /**
     * Return the Set of OIData tables keyed by Granule
     * @return Set of OIData tables keyed by Granule
     */
    public Map<Granule, Set<OIData>> getOiDataPerGranule() {
        return oiDataPerGranule;
    }

    public List<Granule> getSortedGranules(final Comparator<Granule> comparator) {
        final List<Granule> granules = new ArrayList<Granule>(oiDataPerGranule.keySet());
        Collections.sort(granules, comparator);

        logger.log(Level.FINE, "granules sorted: {0}", granules);

        return granules;
    }

    public List<OIData> findOIData(final String targetUID, final String insModeUID, final NightId nightID,
                                   final String oiFitsPath, final Integer extNb,
                                   List<OIData> inputList) {

        List<OIData> oiDataList = inputList;

        if (!this.isEmpty()) {
            // Find matching Granules:
            final List<Granule> granules = findGranules(targetUID, insModeUID, nightID);

            if (!granules.isEmpty()) {
                oiDataList = (oiDataList != null) ? oiDataList : new ArrayList<OIData>();

                for (Granule g : granules) {
                    final Set<OIData> oiDatas = oiDataPerGranule.get(g);

                    if (oiDatas != null) {
                        // Apply table selection:

                        // TODO: handle extra filters on OIData ?
                        if (oiFitsPath == null) {
                            // add all tables:
                            for (OIData oiData : oiDatas) {
                                if (!oiDataList.contains(oiData)) {
                                    oiDataList.add(oiData);
                                }
                            }
                        } else {
                            final OIFitsFile oiFitsFile = getOIFitsFile(oiFitsPath);

                            if (oiFitsFile != null) {
                                // test all tables:
                                for (OIData oiData : oiDatas) {
                                    // file path comparison:
                                    if (oiData.getOIFitsFile().equals(oiFitsFile)) {

                                        // extNb is null means add all tables from file
                                        if (extNb == null || oiData.getExtNb() == extNb.intValue()) {
                                            if (!oiDataList.contains(oiData)) {
                                                oiDataList.add(oiData);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        logger.log(Level.FINE, "findOIData: {0}", oiDataList);

        return oiDataList;
    }

    public List<Granule> findGranules(final String targetUID, final String insModeUID, final NightId nightID) {
        final List<Granule> granules = getSortedGranules(GranuleComparator.DEFAULT);

        // null if no match or targetUID / insModeUID is undefined :
        final Target target = tm.getGlobalByUID(targetUID);
        final InstrumentMode insMode = imm.getGlobalByUID(insModeUID);

        final Granule pattern = new Granule(target, insMode, nightID);

        for (Iterator<Granule> it = granules.iterator(); it.hasNext();) {
            final Granule candidate = it.next();

            if (!Granule.MATCHER_LIKE.match(pattern, candidate)) {
                it.remove();
            }
        }
        return granules;
    }
}
