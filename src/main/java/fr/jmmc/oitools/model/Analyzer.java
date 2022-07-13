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
/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.model;

import fr.jmmc.jmcs.util.NumberUtils;
import static fr.jmmc.oitools.model.ModelBase.UNDEFINED_DBL;
import fr.jmmc.oitools.model.range.Range;
import fr.jmmc.oitools.util.CombUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;

/**
 * This class visit the table for a given oifits file to process some computation on them. 
 */
public final class Analyzer implements ModelVisitor {

    /** Logger */
    private final static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Analyzer.class.getName());
    /* members */
    /** Singleton pattern */
    private final static Analyzer INSTANCE = new Analyzer();

    /** cached log debug flag */
    private final boolean isLogDebug = logger.isLoggable(Level.FINE);
    /** cached combinations for baselines (staLen, combLen, combinations) */
    private final Map<Integer, Map<Integer, List<int[]>>> combsCache = new HashMap<Integer, Map<Integer, List<int[]>>>(8);

    /**
     * Return the Manager singleton
     * @return singleton instance
     */
    public static Analyzer getInstance() {
        return INSTANCE;
    }

    /**
     * Prevent instanciation of singleton.
     * Manager instance should be obtained using getInstance().
     */
    private Analyzer() {
        super();
    }

    /**
     * Process the given OIFitsFile element with this visitor implementation :
     * fill the internal buffer with file information
     * @param oiFitsFile OIFitsFile element to visit
     */
    @Override
    public void visit(final OIFitsFile oiFitsFile) {
        final long start = System.nanoTime();

        // reset cached analyzed data:
        oiFitsFile.setChanged();

        // TODO: process keywords in primary HDU (image) ?
        // process OITarget table (mandatory but incorrect files can happen):
        if (oiFitsFile.hasOiTarget()) {
            process(oiFitsFile.getOiTarget());
        }

        // process OIWavelength tables:
        for (final OIWavelength oiWavelength : oiFitsFile.getOiWavelengths()) {
            process(oiWavelength);
        }

        // process OIArray tables:
        for (final OIArray oiArray : oiFitsFile.getOiArrays()) {
            process(oiArray);
        }

        // finally: process OIData tables:
        for (final OIData oiData : oiFitsFile.getOiDataList()) {
            process(oiData);
        }

        if (isLogDebug) {
            logger.log(Level.FINE, "process: OIFitsFile[{0}] usedStaNamesMap: {1}",
                    new Object[]{oiFitsFile.getAbsoluteFilePath(), oiFitsFile.getUsedStaNamesMap().entrySet()});
            logger.log(Level.FINE, "process: OIFitsFile[{0}] granules: {1}",
                    new Object[]{oiFitsFile.getAbsoluteFilePath(), oiFitsFile.getDistinctGranules().keySet()});
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "Analyzer.visit : duration = {0} ms.", 1e-6d * (System.nanoTime() - start));
        }
    }

    /**
     * Process the given OITable element with this visitor implementation
     * @param oiTable OITable element to visit
     */
    @Override
    public void visit(final OITable oiTable) {
        if (oiTable instanceof OIData) {
            process((OIData) oiTable);
        } else if (oiTable instanceof OIWavelength) {
            process((OIWavelength) oiTable);
        } else if (oiTable instanceof OIArray) {
            process((OIArray) oiTable);
        } else if (oiTable instanceof OITarget) {
            process((OITarget) oiTable);
        }
    }

    /* --- process OITable --- */
    /**
     * Process the given OIData table
     * @param oiData OIData table to process
     */
    private void process(final OIData oiData) {
        if (isLogDebug) {
            logger.log(Level.FINE, "process: OIData[{0}] OIWavelength range: {1}", new Object[]{oiData, oiData.getEffWaveRange()});
        }

        // reset cached analyzed data:
        oiData.setChanged();

        // First: station indexes & configurations:
        if (oiData.getStaIndex() != null) {
            processStaIndex(oiData);
        }

        // dimensions:
        final int nRows = oiData.getNbRows();

        // Count Flags:
        int nFlagged = 0;

        if (oiData.getFlag() != null) {
            final boolean[][] flags = oiData.getFlag();

            final int nWaves = oiData.getNWave();

            boolean[] row;
            for (int i = 0, j; i < nRows; i++) {
                row = flags[i];
                for (j = 0; j < nWaves; j++) {
                    if (row[j]) {
                        nFlagged++;
                    }
                }
            }
        }
        oiData.setNFlagged(nFlagged);

        // Extract Granules of this table (targetId, nightId, insMode, mjd):
        // Get referenced tables:
        final OITarget oiTarget = oiData.getOiTarget();
        final OIWavelength oiWavelength = oiData.getOiWavelength();

        // Get targetId column:
        final short[] targetIds = oiData.getTargetId();
        // compute night ids:
        final int[] nightIds = oiData.getNightId();
        // StaIndex column:
        final short[][] staIndexes = oiData.getStaIndex();
        // Get MJD column:
        final double[] mjds = oiData.getMJD();

        // note: if no OITarget table then the target will be Target.UNDEFINED
        final Map<Short, Target> targetIdToTarget = (oiTarget != null) ? oiTarget.getTargetIdToTarget() : null;

        // Resolve instrument mode:
        // Note: if no OIWaveLength but have insname => may create an InstrumentMode("Missing<insname>"):
        final InstrumentMode insMode = (oiWavelength != null) ? oiWavelength.getInstrumentMode() : InstrumentMode.UNDEFINED;

        // Outputs:
        // Fill distinct Target Id:
        final Set<Short> distinctTargetId = oiData.getDistinctTargetId();
        // Fill distinct Night Id:
        final Set<NightId> distinctNightId = oiData.getDistinctNightId();
        // Fill distinct Granule:
        final OIFitsFile oiFitsFile = oiData.getOIFitsFile();
        final Map<Granule, Granule> distinctGranules = oiFitsFile.getDistinctGranules();
        // Fill oidata tables per (distinct) Granule:
        final Map<Granule, Set<OIData>> oiDataPerGranule = oiFitsFile.getOiDataPerGranule();
        // Fill used staNames to StaNamesDir (reference StaNames / orientation):
        final Map<String, StaNamesDir> usedStaNamesMap = oiFitsFile.getUsedStaNamesMap();

        // reused NightId:
        NightId n = new NightId();

        // reused Granule:
        Granule g = new Granule();

        // Process all rows to identify Granule and its associated fields:
        for (int i = 0; i < nRows; i++) {
            final Short targetId = Short.valueOf(targetIds[i]);
            distinctTargetId.add(targetId);

            // create Granules:
            // Get target:
            Target target = (targetIdToTarget != null) ? targetIdToTarget.get(targetId) : null;
            if (target == null) {
                target = Target.UNDEFINED;
            }

            // Get Night instance:
            n.set(nightIds[i]);

            NightId night = NightId.getCachedInstance(n);
            if (night == null) {
                night = NightId.putCachedInstance(n);
                n = new NightId();
            }
            distinctNightId.add(night);

            // Get corresponding StaName:
            String staNames = null;
            if (staIndexes != null) {
                final short[] staIndex = staIndexes[i];

                // resolve sorted StaNames (reference) to get its orientation:
                final StaNamesDir sortedStaNamesDir = oiData.getSortedStaNamesDir(staIndex);

                if (sortedStaNamesDir != null) {
                    // find the previous (real) baseline corresponding to the sorted StaNames (stable):
                    final StaNamesDir refStaNamesDir = usedStaNamesMap.get(sortedStaNamesDir.getStaNames());

                    if (refStaNamesDir == null) {
                        logger.log(Level.WARNING, "bad usedStaNamesMap: missing {0}", sortedStaNamesDir.getStaNames());
                    } else {
                        staNames = refStaNamesDir.getStaNames();
                    }
                }
            }

            // Get MJD value:
            final double mjd = mjds[i];

            // Update / Resolve Granule:
            g.set(target, insMode, night);

            Granule granule = distinctGranules.get(g);
            if (granule == null) {
                distinctGranules.put(g, g);
                granule = g;
                g = new Granule();
            }

            // Update distinct StaNames on shared granule:
            if (staNames != null) {
                granule.getDistinctStaNames().add(staNames);
            }

            // Update MJD Ranges on shared granule:
            granule.updateMjdRange(mjd);

            // Lookup pre-existing Granule (same granule fields):
            Set<OIData> oiDataTables = oiDataPerGranule.get(granule);
            if (oiDataTables == null) {
                oiDataTables = new LinkedHashSet<OIData>();
                oiDataPerGranule.put(granule, oiDataTables);
            }
            // update tables associated to the current granule:
            oiDataTables.add(oiData);
        }

        if (isLogDebug) {
            logger.log(Level.FINE, "process: OIData[{0}] nFlagged: {1}", new Object[]{oiData, nFlagged});
            logger.log(Level.FINE, "process: OIData[{0}] distinctTargetId {1}", new Object[]{oiData, distinctTargetId});
            logger.log(Level.FINE, "process: OIData[{0}] distinctNightId  {1}", new Object[]{oiData, distinctNightId});
        }
    }

    /**
     * Process the given OIArray table
     * @param oiArray OIArray table to process
     */
    private void process(final OIArray oiArray) {
        if (isLogDebug) {
            logger.log(Level.FINE, "process: OIArray[{0}]", oiArray);
        }

        // reset cached analyzed data:
        oiArray.setChanged();

        final Map<Short, Integer> staIndexToRowIndex = oiArray.getStaIndexToRowIndex();

        final short[] staIndexes = oiArray.getStaIndex();

        Short staIndex;
        for (int i = 0, len = oiArray.getNbRows(); i < len; i++) {
            staIndex = Short.valueOf(staIndexes[i]);
            staIndexToRowIndex.put(staIndex, NumberUtils.valueOf(i));
        }

        // TODO: analyze the stations 
        // ie create value objects (comparable) to be able 
        // to merge OI_ARRAY tables (and use uniform Stations => staIndex)
        if (isLogDebug) {
            logger.log(Level.FINE, "process: OIArray[{0}] staIndexToRowIndex: {1}", new Object[]{oiArray, staIndexToRowIndex});
        }
    }

    /**
     * Process the given OIWavelength table
     * @param oiWavelength OIWavelength table to process
     */
    private void process(final OIWavelength oiWavelength) {
        if (isLogDebug) {
            logger.log(Level.FINE, "process: OIWavelength[{0}]", oiWavelength);
        }

        // reset cached analyzed data:
        oiWavelength.setChanged();

        // compute lazily the wavelength min/max:
        final Range effWaveRange = oiWavelength.getEffWaveRange();
        // compute lazily the bandwidth min/max:
        final Range effBandRange = oiWavelength.getEffBandRange();

        // Mean Resolution = mean(lambda / delta_lambda)
        final double resPower = oiWavelength.getResolution();

        // Compute wavelength rank index (disabled as unused):
        /*
        oiWavelength.getEffWaveRankIndex();
         */
        // Create the instrument mode:
        final String insName = oiWavelength.getInsName();
        final int nbChannels = oiWavelength.getNWave();

        double bandMin = effBandRange.getMin();

        if (!NumberUtils.isFinitePositive(bandMin)) {
            bandMin = UNDEFINED_DBL;
        }

        // TODO: extract only instrument Name ie parse first alpha characters to cleanup weird INSNAME values
        final InstrumentMode insMode = new InstrumentMode(insName, nbChannels, effWaveRange, resPower, bandMin);

        // Associate the InstrumentMode instance to the OIWavelength table (locally)
        oiWavelength.setInstrumentMode(insMode);

        if (isLogDebug) {
            logger.log(Level.FINE, "process: file: {0}", oiWavelength.getOIFitsFile().getAbsoluteFilePath());
            logger.log(Level.FINE, "process: {0}", insName);
            logger.log(Level.FINE, "process: OIWavelength[{0}] range: {1}]", new Object[]{oiWavelength, effWaveRange});
            logger.log(Level.FINE, "process: OIWavelength[{0}]\ninsMode: {1}", new Object[]{oiWavelength, insMode});
        }
    }

    /**
     * Process the given OITarget table
     * @param oiTarget OITarget table to process
     */
    private void process(final OITarget oiTarget) {
        if (isLogDebug) {
            logger.log(Level.FINE, "process: OITarget[{0}]", oiTarget);
        }

        // reset cached analyzed data:
        oiTarget.setChanged();

        // TargetId indexes:
        final Map<Short, Integer> targetIdToRowIndex = oiTarget.getTargetIdToRowIndex();
        // Target indexes:
        final Map<Short, Target> targetIdToTarget = oiTarget.getTargetIdToTarget();
        final Map<Target, Short> targetObjToTargetId = oiTarget.getTargetObjToTargetId();
        // Columns
        final short[] targetIds = oiTarget.getTargetId();

        for (int i = 0, len = oiTarget.getNbRows(); i < len; i++) {
            final Short targetId = Short.valueOf(targetIds[i]);

            targetIdToRowIndex.put(targetId, NumberUtils.valueOf(i));

            // may create several identical Target objects if the target record is duplicated:
            final Target target = oiTarget.createTarget(i);

            // Mapping between target id <-> Target instance (locally)
            targetIdToTarget.put(targetId, target);
            targetObjToTargetId.put(target, targetId);
        }

        if (isLogDebug) {
            logger.log(Level.FINE, "process: OITarget[{0}] targetIdToRowIndex: {1}", new Object[]{oiTarget, targetIdToRowIndex});
            logger.log(Level.FINE, "process: OITarget[{0}] targetIdToTarget: {1}", new Object[]{oiTarget, targetIdToTarget});
            logger.log(Level.FINE, "process: OITarget[{0}] targetObjToTargetId: {1}", new Object[]{oiTarget, targetObjToTargetId});
        }
    }

    // --- baseline / configuration processing
    /**
     * Process station indexes on the given OIData table
     * @param oiData OIData table to process
     */
    private void processStaIndex(final OIData oiData) {
        final int nRows = oiData.getNbRows();

        // StaIndex column:
        final short[][] staIndexes = oiData.getStaIndex();

        // distinct staIndex arrays:
        final Set<short[]> distinctStaIndex = oiData.getDistinctStaIndex();
        // mapping to sorted staNames:
        final Map<short[], StaNamesDir> staIndexesToSortedStaNamesDir = oiData.getStaIndexesToSortedStaNamesDir();

        if (nRows != 0) {
            // Get size of StaIndex arrays once:
            final int staLen = staIndexes[0].length;
            {
                final StationIndex staList = new StationIndex(staLen);
                final Map<StationIndex, short[]> mappingStaList = new HashMap<StationIndex, short[]>(128);

                for (int i = 0, j; i < nRows; i++) {
                    final short[] staIndex = staIndexes[i];

                    // prepare Station index:
                    staList.clear();

                    for (j = 0; j < staLen; j++) {
                        staList.add(Short.valueOf(staIndex[j]));
                    }

                    // Find existing array:
                    final short[] uniqueStaIndex = mappingStaList.get(staList);

                    if (uniqueStaIndex == null) {
                        // not found:
                        // store same array instance present in the row:
                        distinctStaIndex.add(staIndex);

                        // store mapping:
                        mappingStaList.put(new StationIndex(staList), staIndex);
                    } else {
                        // store distinct instance (minimize array instances):
                        staIndexes[i] = uniqueStaIndex;
                    }
                }
            }

            // Fill used staNames to StaNamesDir (reference StaNames / orientation):
            final Map<String, StaNamesDir> usedStaNamesMap = oiData.getOIFitsFile().getUsedStaNamesMap();

            // 2nd step: fill mapping:
            if (staLen >= 2) {
                final String[] staIndexNamesSorted = new String[staLen];

                for (final short[] staIndex : distinctStaIndex) {
                    // Note: use another array instance as Data.getStaNames(staIndexSorted) uses identity map
                    final short[] staIndexSorted = new short[staLen];

                    // prepare Station names:
                    for (int j = 0; j < staLen; j++) {
                        staIndexSorted[j] = staIndex[j];
                        staIndexNamesSorted[j] = oiData.getStaName(staIndexSorted[j]);
                    }

                    // Sort at least: 2 items:
                    int perm = testAndSwap(staIndexSorted, staIndexNamesSorted, 0, 1);

                    // now: indices (0 1) sorted
                    if (staLen == 3) {
                        // triplet:
                        perm += testAndSwap(staIndexSorted, staIndexNamesSorted, 0, 2);
                        perm += testAndSwap(staIndexSorted, staIndexNamesSorted, 1, 2);
                    }

                    final boolean orientation = (perm % 2 == 0);

                    final String staNames = oiData.getStaNames(staIndex);
                    final String sortedStaNames = oiData.getStaNames(staIndexSorted);

                    if (isLogDebug) {
                        logger.log(Level.FINE, "Baseline: [{0} = {1}], sorted: [{2} = {3}] perm: {4} orientation: {5}",
                                new Object[]{Arrays.toString(staIndex), staNames,
                                             Arrays.toString(staIndexSorted), Arrays.toString(staIndexNamesSorted),
                                             perm, orientation});
                    }

                    final StaNamesDir sortedStaNamesDir = new StaNamesDir(sortedStaNames, orientation);

                    staIndexesToSortedStaNamesDir.put(staIndex, sortedStaNamesDir);

                    // global level (OIFits):
                    // find the previous (real) baseline corresponding to the sorted StaNames (stable):
                    final StaNamesDir refStaNamesDir = usedStaNamesMap.get(sortedStaNames);
                    if (refStaNamesDir == null) {
                        // store this (real) baseline corresponding to the sorted StaNames (stable) with the reference orientation flag:
                        usedStaNamesMap.put(sortedStaNames, new StaNamesDir(staNames, sortedStaNamesDir.isOrientation()));
                    }
                }
            } else {
                // 1T (OI_FLUX):
                for (final short[] staIndex : distinctStaIndex) {
                    final String staNames = oiData.getStaNames(staIndex);
                    final StaNamesDir sortedStaNamesDir = new StaNamesDir(staNames, true);

                    staIndexesToSortedStaNamesDir.put(staIndex, sortedStaNamesDir);
                    usedStaNamesMap.put(staNames, sortedStaNamesDir);
                }
            }
        }

        if (isLogDebug) {
            logger.log(Level.FINE, "processStaIndex: OIData[{0}] distinctStaIndex:", oiData.idToString());
            for (short[] staIndex : distinctStaIndex) {
                logger.log(Level.FINE, "Baseline: {0} = {1}", new Object[]{Arrays.toString(staIndex), oiData.getStaNames(staIndex)});
            }

            logger.log(Level.FINE, "processStaIndex: OIData[{0}] staIndexesToSortedStaNamesDir:", oiData.idToString());
            for (Map.Entry<short[], StaNamesDir> e : staIndexesToSortedStaNamesDir.entrySet()) {
                logger.log(Level.FINE, "{0} : {1}", new Object[]{Arrays.toString(e.getKey()), e.getValue()});
            }
        }
        processStaConf(oiData);
    }

    private static int testAndSwap(final short[] staIndexSorted, final String[] staIndexNamesSorted, final int i1, final int i2) {
        if (staIndexNamesSorted[i1].compareTo(staIndexNamesSorted[i2]) >= 0) {
            // swap both arrays:
            final short i = staIndexSorted[i1];
            staIndexSorted[i1] = staIndexSorted[i2];
            staIndexSorted[i2] = i;

            final String s = staIndexNamesSorted[i1];
            staIndexNamesSorted[i1] = staIndexNamesSorted[i2];
            staIndexNamesSorted[i2] = s;
            return 1;
        }
        // already sorted
        return 0;
    }

    /**
     * Process station configurations on the given OIData table
     * @param oiData OIData table to process
     */
    private void processStaConf(final OIData oiData) {

        final int nRows = oiData.getNbRows();

        if (nRows == 0) {
            return;
        }

        // local vars for performance:
        final boolean isDebug = isLogDebug;

        // Derived StaConf column:
        final short[][] staConfs = oiData.getStaConf();

        // distinct staIndex arrays:
        final Set<short[]> distinctStaIndex = oiData.getDistinctStaIndex();

        // distinct staConf arrays:
        final Set<short[]> distinctStaConf = oiData.getDistinctStaConf();

        // distinct Sorted staIndex arrays:
        final Set<List<Short>> sortedStaIndex = new HashSet<List<Short>>(distinctStaIndex.size()); // LinkedHashSet ??

        // Sorted sta index mapping (unique instances) to distinct station indexes (maybe more than 1 due to permutations):
        // note: allocate twice because of missing StaIndex arrays:
        final Map<List<Short>, List<short[]>> mappingSortedStaIndex = new HashMap<List<Short>, List<short[]>>(distinctStaIndex.size());

        // Get size of StaIndex arrays once:
        final int staLen = distinctStaIndex.iterator().next().length;

        // Use List<Short> for easier manipulation ie List.equals() use also Short.equals on all items (missing baselines) !
        List<short[]> equivStaIndexes;

        for (short[] staIndex : distinctStaIndex) {
            final List<Short> staList = new StationIndex(staLen);

            for (int i = 0; i < staLen; i++) {
                staList.add(Short.valueOf(staIndex[i]));
            }

            // sort station list:
            Collections.sort(staList);

            if (sortedStaIndex.add(staList)) {
                // Allocate 2 slots (for only 2 permutations mainly):
                equivStaIndexes = new ArrayList<short[]>(2);
                mappingSortedStaIndex.put(staList, equivStaIndexes);
            } else {
                equivStaIndexes = mappingSortedStaIndex.get(staList);

            }
            // add staIndex corresponding to sorted station list:
            equivStaIndexes.add(staIndex);
        }

        if (isDebug) {
            logger.log(Level.FINE, "processStaConf: OIData[{0}] sortedStaIndex:", oiData);

            for (List<Short> item : sortedStaIndex) {
                logger.log(Level.FINE, "StaIndex: {0}", item);
            }
        }

        if (sortedStaIndex.size() == 1) {
            final short[] staConf = toArray(sortedStaIndex.iterator().next());

            // single staIndex array = single configuration
            distinctStaConf.add(staConf);

            // Fill StaConf derived column:
            for (int i = 0; i < nRows; i++) {
                // store single station configuration:
                staConfs[i] = staConf;
            }
        } else {
            // StaIndex mapping (distinct present StaIndex arrays) to station configuration:
            final Map<short[], short[]> mappingStaConf = new HashMap<short[], short[]>(distinctStaIndex.size());

            // Guess configurations:
            // simple algorithm works only on distinct values (Aspro2 for now)
            // but advanced one should in fact use baselines having same MJD (same time) !
            // mapping between staId (1 station) and its station node:
            final Map<Short, StationNode> staIndexNodes = new HashMap<Short, StationNode>(32);

            // loop on sorted StaxIndex Set:
            for (List<Short> staList : sortedStaIndex) {
                for (Short staId : staList) {
                    StationNode node = staIndexNodes.get(staId);

                    // create missing node:
                    if (node == null) {
                        node = new StationNode(staId);
                        staIndexNodes.put(staId, node);
                    }
                    // add present StaIndex array:
                    node.addStaList(staList);
                }
            }

            // convert map into list of nodes:
            final List<StationNode> nodes = new ArrayList<StationNode>(staIndexNodes.values());

            // sort station node on its related staIndex counts (smaller count first):
            Collections.sort(nodes);

            final StationIndex sortedConf = new StationIndex(10);

            if (staLen == 1) {
                // try using all stations to form a conf:
                for (StationNode node : nodes) {
                    sortedConf.add(node.staId);
                }

                // consider conf always valid:
                final short[] staConf = toArray(sortedConf);

                // add this configuration:
                distinctStaConf.add(staConf);

                // add mappings:
                for (short[] staIndex : distinctStaIndex) {
                    mappingStaConf.put(staIndex, staConf);
                }
            } else {
                if (isDebug) {
                    logger.fine("Initial StationNodes --------------------------------------");

                    for (StationNode n : nodes) {
                        logger.log(Level.FINE, "Station: {0}\t({1}):\t{2}", new Object[]{n.staId, n.count, n.staLists});
                    }
                    logger.fine("--------------------------------------");
                }

                // Missing StaIndex (removed baseline or triplets) ordered by natural StationIndex comparator:
                final Set<List<Short>> missingStaIndexes = new TreeSet<List<Short>>();

                // current guessed station configuration:
                final Set<Short> guessConf = new HashSet<Short>(8);

                final List<List<Short>> combStaLists = new ArrayList<List<Short>>();
                // StaIndex from combination:
                final StationIndex cStaList = new StationIndex(staLen);

                List<int[]> iCombs;
                int confLen;

                StationNode node, other;
                List<Short> item;

                int[] combination;
                StationIndex newStaIndex;
                Short staId;

                int j, k, n, len;
                Iterator<List<Short>> itStaList;

                boolean doProcess = true;
                int nPass = 0;

                final int nodeLen = nodes.size();

                // Grow pass: add missing baseline or triplets:
                final int maxStaIndex = CombUtils.comb(nodeLen, staLen);

                if (isDebug) {
                    logger.log(Level.FINE, "nodes= {0} - maxStaIndex= {1}", new Object[]{nodeLen, maxStaIndex});
                }

                // distinct Sorted staIndex arrays from combinations (present + all missing StaIndex):
                final Set<List<Short>> sortedCombStaIndex = new HashSet<List<Short>>(maxStaIndex);
                // add all present StaIndex:
                sortedCombStaIndex.addAll(sortedStaIndex);

                // distinct processed sorted staConf:
                final Set<List<Short>> distinctCombStaConf = new HashSet<List<Short>>(64);

                while (doProcess) {
                    nPass++;
                    doProcess = false;

                    // Process first the last node (sorted so has top most relations):
                    for (n = nodeLen - 1; n >= 0; n--) {
                        node = nodes.get(n);

                        // skip marked nodes:
                        if (!node.mark) {
                            // try another node ?
                            doProcess = true;

                            guessConf.add(node.staId);

                            // try using all stations to form a conf:
                            for (itStaList = node.staLists.iterator(); itStaList.hasNext();) {
                                item = itStaList.next();

                                for (k = 0; k < staLen; k++) {
                                    guessConf.add(item.get(k));
                                }
                            }

                            // compute missing staIndexes:
                            sortedConf.clear();
                            sortedConf.addAll(guessConf);
                            guessConf.clear();
                            Collections.sort(sortedConf);

                            // check that this conf is different than other already processed:
                            if (!distinctCombStaConf.contains(sortedConf)) {
                                distinctCombStaConf.add(new StationIndex(sortedConf));

                                if (isDebug) {
                                    logger.log(Level.FINE, "Growing node: {0} - sortedConf = {1}", new Object[]{node.staId, sortedConf});
                                }

                                // see CombUtils for generics
                                confLen = sortedConf.size();

                                // get permutations:
                                iCombs = getCombinations(staLen, confLen);

                                combStaLists.clear();

                                // iCombs is sorted so should keep array sorted as otherStaIds is also sorted !
                                for (j = 0, len = iCombs.size(); j < len; j++) {
                                    combination = iCombs.get(j);

                                    cStaList.clear();

                                    for (k = 0; k < staLen; k++) {
                                        cStaList.add(sortedConf.get(combination[k]));
                                    }

                                    // only keep new StaIndex arrays (not present nor already handled):
                                    if (!sortedCombStaIndex.contains(cStaList)) {
                                        newStaIndex = new StationIndex(cStaList);

                                        // add new StaIndex array in processed StaIndex:
                                        sortedCombStaIndex.add(newStaIndex);

                                        combStaLists.add(newStaIndex);
                                    }
                                }

                                if (isDebug) {
                                    logger.log(Level.FINE, "node: {0} - combStaLists = {1}", new Object[]{node.staId, combStaLists});
                                }

                                // Test missing staIndex:
                                for (j = 0, len = combStaLists.size(); j < len; j++) {
                                    item = combStaLists.get(j);

                                    // test if present:
                                    if (!sortedStaIndex.contains(item)) {
                                        missingStaIndexes.add(item);
                                    }
                                }

                                // process all possible staList:
                                // note: node needs not to be modified as all its stations were used to form the combination (sortedconf)
                                // add missing baselines in other nodes:
                                for (j = 0, len = combStaLists.size(); j < len; j++) {
                                    item = combStaLists.get(j);

                                    for (k = 0; k < staLen; k++) {
                                        staId = item.get(k);

                                        // skip current node:
                                        if (staId != node.staId) {
                                            // add baseline:
                                            other = staIndexNodes.get(staId);

                                            // ensure other is not null:
                                            if (other.addStaList(item)) {
                                                // mark the other node to be processed (again):
                                                other.mark = false;
                                            }
                                        }
                                    }
                                }
                            }

                            // mark this node as done:
                            node.mark = true;

                            // exit from this loop:
                            break;
                        }
                    } // nodes

                    if (doProcess) {
                        // sort station node on its related staIndex counts (smaller count first) after each pass:
                        Collections.sort(nodes);

                        if (isDebug) {
                            logger.fine("Current StationNodes --------------------------------------");
                            for (StationNode s : nodes) {
                                logger.log(Level.FINE, "Station[{0}]: {1}\t({2}):\t{3}", new Object[]{s.mark, s.staId, s.count, s.staLists});
                            }
                            logger.fine("--------------------------------------");
                        }
                    }

                } // until doProcess

                if (isDebug) {
                    logger.log(Level.FINE, "grow {0} pass: done", nPass);
                }

                // Process clusters pass:
                doProcess = true;
                nPass = 0;

                while (doProcess) {
                    nPass++;
                    doProcess = false;

                    // Process only the first node (sorted so less relations):
                    for (n = 0; n < nodeLen; n++) {
                        node = nodes.get(n);

                        // skip empty nodes:
                        if (node.count > 0) {
                            // try another node ?
                            doProcess = true;

                            guessConf.add(node.staId);

                            // try using all stations to form a conf (even with missing baselines):
                            for (itStaList = node.staLists.iterator(); itStaList.hasNext();) {
                                item = itStaList.next();

                                for (k = 0; k < staLen; k++) {
                                    guessConf.add(item.get(k));
                                }
                            }

                            // compute missing staIndexes:
                            sortedConf.clear();
                            sortedConf.addAll(guessConf);
                            guessConf.clear();
                            Collections.sort(sortedConf);

                            if (isDebug) {
                                logger.log(Level.FINE, "Processing node: {0} - sortedConf = {1}", new Object[]{node.staId, sortedConf});
                            }

                            combStaLists.clear();

                            // see CombUtils for generics
                            confLen = sortedConf.size();

                            // TODO: remove all that code as grow pass has completed everything so don't test combination anymore ?
                            // get permutations:
                            iCombs = getCombinations(staLen, confLen);

                            // iCombs is sorted so should keep array sorted as otherStaIds is also sorted !
                            for (j = 0, len = iCombs.size(); j < len; j++) {
                                combination = iCombs.get(j);

                                newStaIndex = new StationIndex(staLen);

                                for (k = 0; k < staLen; k++) {
                                    newStaIndex.add(sortedConf.get(combination[k]));
                                }

                                combStaLists.add(newStaIndex);
                            }

                            if (isDebug) {
                                logger.log(Level.FINE, "node: {0} - combStaLists = {1}", new Object[]{node.staId, combStaLists});
                            }

                            // consider conf always valid:
                            final short[] staConf = toArray(sortedConf);

                            // add this configuration:
                            distinctStaConf.add(staConf);

                            // add mappings:
                            for (j = 0, len = combStaLists.size(); j < len; j++) {
                                item = combStaLists.get(j);

                                equivStaIndexes = mappingSortedStaIndex.get(item);

                                if (equivStaIndexes != null) {
                                    // only store staConf for present baselines / triplets (some may be missing):
                                    for (short[] staIndex : equivStaIndexes) {
                                        mappingStaConf.put(staIndex, staConf);
                                    }
                                }
                            }

                            // remove all staList in node:
                            node.clear();

                            // remove all possible staIndex in other nodes:
                            for (j = 0, len = combStaLists.size(); j < len; j++) {
                                item = combStaLists.get(j);

                                for (k = 0; k < staLen; k++) {
                                    staId = item.get(k);

                                    if (staId != node.staId) {
                                        // remove baseline:
                                        other = staIndexNodes.get(staId);

                                        if (other != null) {
                                            other.removeStaList(item);
                                        }
                                    }
                                }
                            }

                            // exit from this loop:
                            break;
                        }
                    } // nodes

                    if (doProcess) {
                        // sort station node on its related staIndex counts (smaller count first) after each pass:
                        Collections.sort(nodes);

                        if (isDebug) {
                            logger.fine("Current StationNodes --------------------------------------");
                            for (StationNode s : nodes) {
                                logger.log(Level.FINE, "Station: {0}\t({1}):\t{2}", new Object[]{s.staId, s.count, s.staLists});
                            }
                            logger.fine("--------------------------------------");
                        }
                    }

                } // until doProcess

                if (isDebug) {
                    logger.log(Level.FINE, "process {0} pass: done", nPass);
                }

                // Report missing StaIndex arrays:
                if (!missingStaIndexes.isEmpty() && logger.isLoggable(Level.FINE)) {
                    len = missingStaIndexes.size();

                    final int itLen = (staLen * 3 + 2);
                    final int nPerLine = 120 / itLen;
                    final StringBuilder sb = new StringBuilder(len * itLen + len / nPerLine + 100);

                    sb.append("processStaConf: OIData[").append(oiData.toString()).append("]:\n Missing ");
                    sb.append(len).append(" / ").append(sortedStaIndex.size()).append(" StaIndex arrays:\n");

                    sb.append('[');

                    for (n = 0, k = 0, itStaList = missingStaIndexes.iterator(); itStaList.hasNext(); n++) {
                        item = itStaList.next();

                        sb.append('[');
                        for (j = 0; j < staLen; j++) {
                            sb.append(item.get(j)).append(", ");
                        }
                        sb.setLength(sb.length() - 2);
                        sb.append(']');

                        if (n != len) {
                            sb.append(", ");
                        }

                        k++;
                        if (k == nPerLine) {
                            sb.append('\n');
                            k = 0;
                        }
                    }
                    sb.append(']');

                    // TODO: sort missing StaIndex arrays (+ formatting ?)
                    logger.info(sb.toString());
                }
            }

            if (isDebug) {
                logger.log(Level.FINE, "mappingStaConf({0}):", staLen);

                for (Map.Entry<short[], short[]> entry : mappingStaConf.entrySet()) {
                    logger.log(Level.FINE, "{0} : {1}", new Object[]{Arrays.toString(entry.getKey()), Arrays.toString(entry.getValue())});
                }
            }

            // FINALLY: Fill StaConf derived column:
            // StaIndex column:
            final short[][] staIndexes = oiData.getStaIndex();

            short[] staIndex;
            for (int i = 0; i < nRows; i++) {
                staIndex = staIndexes[i];

                // store station configuration according to mapping (should be not null):
                staConfs[i] = mappingStaConf.get(staIndex);

                if (staConfs[i] == null) {
                    logger.log(Level.WARNING, "MISSING station configuration for station index:{0} !", oiData.getStaNames(staIndex));

                }
            }
        }

        if (isDebug) {
            logger.log(Level.FINE, "processStaConf: OIData[{0}] distinctStaConf:", oiData);

            for (short[] item : distinctStaConf) {
                logger.log(Level.FINE, "StaConf: {0} = {1}", new Object[]{Arrays.toString(item), oiData.getStaNames(item)});
            }
        }
    }

    private List<int[]> getCombinations(final int staLen, final int confLen) {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "Get iCombs with len = {0}", confLen);
        }

        final Integer staKey = NumberUtils.valueOf(staLen);

        Map<Integer, List<int[]>> combsByConfLen = combsCache.get(staKey);
        if (combsByConfLen == null) {
            combsByConfLen = new HashMap<Integer, List<int[]>>(8);
            combsCache.put(staKey, combsByConfLen);
        }

        final Integer confKey = NumberUtils.valueOf(confLen);

        List<int[]> iCombs = combsByConfLen.get(confKey);

        if (iCombs == null) {
            iCombs = CombUtils.generateCombinations(confKey.intValue(), staLen); // 1T or 2T or 3T
            combsByConfLen.put(confKey, iCombs);
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "getCombinations({0},{1}): {2}", new Object[]{staLen, confLen, iCombs});
        }

        return iCombs;
    }

    /**
     * Convert a station list to array
     * @param staList station list to convert
     * @return station index array
     */
    private static short[] toArray(final List<Short> staList) {
        final short[] staIndex = new short[staList.size()];

        int i = 0;
        for (Short staId : staList) {
            staIndex[i++] = staId.shortValue();
        }

        return staIndex;

    }

    /**
     * StationNode represents a graph node (station id) with its relations (staIndex arrays)
     */
    private static final class StationNode implements Comparable<StationNode> {

        /* members */
        /** station id */
        final Short staId;
        /** relation count (in sync with list) */
        int count = 0;
        /** 
         * distinct station lists (relations)
         * note: it always contains StationIndex implementations for performance
         */
        final Set<List<Short>> staLists = new HashSet<List<Short>>();
        /** mark flag used by grow pass */
        boolean mark = false;

        /**
         * Default constructor
         * @param staId station id
         */
        StationNode(final Short staId) {
            this.staId = staId;
        }

        /**
         * Clear that station node (recycle)
         */
        void clear() {
            staLists.clear();
            count = 0;
        }

        /**
         * Add a station list to this station node (relation) if missing and update relation count
         * @param staIndex station list to add
         * @return true if added; false if already present
         */
        boolean addStaList(final List<Short> staIndex) {
            if (staLists.add(staIndex)) {
                count++;
                return true;
            }
            return false;
        }

        /**
         * Remove the given station list from this station node (relation) if present and update relation count
         * @param staIndex station list to remove
         */
        void removeStaList(final List<Short> staIndex) {
            if (staLists.remove(staIndex)) {
                // note: same baseline can be removed several times because it belongs to several configurations ?
                count--;
            }
        }

        /**
         * Compare this station node with another station node
         * @param other another station node
         * @return comparison result (based on count and station id)
         */
        @Override
        public int compareTo(final StationNode other) {
            int res = count - other.count;
            if (res == 0) {
                res = staId.compareTo(other.staId);
            }
            return res;
        }
    }

    /**
     * StationIndex represents a station list (baseline or triplets) as List&lt;Short&gt; to fix AbstractList performance issues on hashcode() and equals()
     * i.e. avoid allocating Iterator or ListIterator and cache hashcode
     */
    private static final class StationIndex extends ArrayList<Short> implements Comparable<StationIndex> {

        /** default serial UID for Serializable interface */
        private static final long serialVersionUID = 1;

        /* members */
        /** cached hashcode */
        int hashCode = -1;

        /**
         * Default constructor
         * @param initialCapacity initial capacity
         */
        StationIndex(final int initialCapacity) {
            super(initialCapacity);
        }

        /**
         * Copy constructor
         * @param stationIndex station index to copy
         */
        StationIndex(final StationIndex stationIndex) {
            super(stationIndex);
            // copy hashcode:
            this.hashCode = stationIndex.hashCode;
        }

        /**
         * Clear that station index (list) and hashcode
         */
        @Override
        public void clear() {
            super.clear();
            // reset hashcode:
            hashCode = -1;
        }

        /**
         * Optimized hash code value for this list.
         *
         * @return the hash code value for this list
         */
        @Override
        public int hashCode() {
            // cache the computed value
            if (hashCode == -1) {
                // from super hashcode()
                int hash = 1;
                for (int i = 0, len = size(); i < len; i++) {
                    // note: short is never null
                    hash = 31 * hash + get(i).intValue();
                }
                hashCode = hash;
            }
            return hashCode;
        }

        /**
         * Optimized equals implementation for station indexes
         * @param o another StationIndex instance
         * @return true if equals (same stations and same ordering)
         */
        @Override
        public boolean equals(final Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof StationIndex)) {
                return false;
            }
            final StationIndex other = (StationIndex) o;

            for (int i = 0, len = size(); i < len; i++) {
                if (get(i).shortValue() != other.get(i).shortValue()) {
                    return false;
                }
            }
            return true;
        }

        /**
         * Compare this station index with another station index
         * @param other another station index
         * @return comparison result
         */
        @Override
        public int compareTo(final StationIndex other) {
            int res = 0;
            for (int i = 0, len = size(); i < len; i++) {
                res = get(i).intValue() - other.get(i).intValue();
                if (res != 0) {
                    break;
                }
            }
            return res;
        }
    }
}
