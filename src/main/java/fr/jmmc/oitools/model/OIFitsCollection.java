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
import fr.jmmc.oitools.meta.OIFitsStandard;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manage data collection and provide utility methods.
 */
public class OIFitsCollection implements ToStringable {

    /** logger */
    protected final static Logger logger = Logger.getLogger(OIFitsCollection.class.getName());
    /** Target manager */
    private final static TargetManager tm = TargetManager.getInstance();
    /** instrument mode manager */
    private final static InstrumentModeManager imm = InstrumentModeManager.getInstance();
    /* members */
    /** OIFits file collection keyed by absolute file path */
    protected final Map<String, OIFitsFile> oiFitsCollection = new HashMap<String, OIFitsFile>();
    /** fake OIFitsFile structure (to gather OIData) keyed by global Granule */
    private final Map<Granule, OIFitsFile> oiFitsPerGranule = new HashMap<Granule, OIFitsFile>();

    /**
     * Protected constructor
     */
    protected OIFitsCollection() {
        super();
    }

    /**
     * Clear the OIFits file collection
     */
    public final void clear() {
        // clear all loaded OIFitsFile (in memory):
        oiFitsCollection.clear();

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
        oiFitsPerGranule.clear();
    }

    public final boolean isEmpty() {
        return oiFitsCollection.isEmpty();
    }

    public final List<OIFitsFile> getOIFitsFiles() {
        return new ArrayList<OIFitsFile>(oiFitsCollection.values());
    }

    /**
     * Add the given OIFits file to this collection
     * @param oifitsFile OIFits file
     * @return previous OIFits file or null if not present
     */
    public final OIFitsFile addOIFitsFile(final OIFitsFile oifitsFile) {
        if (oifitsFile != null) {
            final String key = getFilePath(oifitsFile);

            final OIFitsFile previous = getOIFitsFile(key);

            if (previous != null) {
                logger.log(Level.WARNING, "TODO: handle overwriting OIFitsFile : {0}", key);
                removeOIFitsFile(previous);
            }

            // analyze the given file:
            oifitsFile.analyze();

            oiFitsCollection.put(key, oifitsFile);

            logger.log(Level.FINE, "addOIFitsFile: {0}", oifitsFile);

            return previous;
        }
        return null;
    }

    public final OIFitsFile getOIFitsFile(final String absoluteFilePath) {
        if (absoluteFilePath != null) {
            return oiFitsCollection.get(absoluteFilePath);
        }
        return null;
    }

    public final OIFitsFile removeOIFitsFile(final OIFitsFile oifitsFile) {
        if (oifitsFile != null) {
            final String key = getFilePath(oifitsFile);
            final OIFitsFile previous = oiFitsCollection.remove(key);

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

        sb.append("{files=").append(this.oiFitsCollection.keySet());

        if (full) {
            if (this.oiFitsPerGranule != null) {
                sb.append(", oiFitsPerGranule=");
                ObjectUtils.toString(sb, full, this.oiFitsPerGranule);
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

        // analyze instrument modes & targets:
        for (OIFitsFile oiFitsFile : oiFitsCollection.values()) {
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

        // This can be replaced by an OIFits merger / filter that creates a new consistent OIFitsFile structure
        // from criteria (target / insname ...)
        for (OIFitsFile oiFitsFile : oiFitsCollection.values()) {
            // Granules:
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
                OIFitsFile oiFitsGranule = oiFitsPerGranule.get(gg);
                if (oiFitsGranule == null) {
                    oiFitsGranule = new OIFitsFile(OIFitsStandard.VERSION_1);

                    oiFitsPerGranule.put(gg, oiFitsGranule);
                    gg = new Granule();
                }

                for (OIData data : entry.getValue()) {
                    oiFitsGranule.addOiTable(data);
                }
            }
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "analyzeCollection: Granules: {0}", oiFitsPerGranule.keySet());
        }
    }

    /** 
     * Return the OIFitsFile structure per Granule found in loaded files.
     * @return OIFitsFile structure per Granule
     */
    public final Map<Granule, OIFitsFile> getOiFitsPerGranule() {
        return oiFitsPerGranule;
    }
}
