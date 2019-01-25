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
import fr.jmmc.oitools.model.InstrumentMode;
import fr.jmmc.oitools.model.NightId;
import fr.jmmc.oitools.model.OIData;
import fr.jmmc.oitools.model.OIFitsCollection;
import fr.jmmc.oitools.model.OIFitsFile;
import fr.jmmc.oitools.model.Target;
import fr.jmmc.oitools.util.OIFitsFileComparator;
import fr.jmmc.oitools.util.OITableByFileComparator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Basic selector result (OIData and Granule sets)
 */
public final class SelectorResult {

    /** members */
    /** OIFits collection (data source) */
    private final OIFitsCollection oiFitsCollection;
    /** granule set (insertion ordered) */
    private final Set<Granule> granules = new HashSet<Granule>();
    private final Set<OIData> oiDatas = new HashSet<OIData>();
    /** cached values */
    private List<Target> sortedTargets = null;
    private List<InstrumentMode> sortedInstrumentModes = null;
    private List<NightId> sortedNightIds = null;
    private List<OIData> sortedOIDatas = null;

    public SelectorResult(final OIFitsCollection oiFitsCollection) {
        this.oiFitsCollection = oiFitsCollection;
        reset();
    }

    public OIFitsCollection getOiFitsCollection() {
        return oiFitsCollection;
    }

    public void reset() {
        granules.clear();
        oiDatas.clear();
    }

    public boolean isEmpty() {
        return granules.isEmpty();
    }

    public void addOIData(final Granule g, final OIData oiData) {
        granules.add(g);
        oiDatas.add(oiData);
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

    @Override
    public String toString() {
        return "SelectorResult{" + "granules=" + granules + ", oiDatas=" + oiDatas + '}';
    }
}
