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
import fr.jmmc.oitools.model.InstrumentMode;
import fr.jmmc.oitools.model.OIData;
import fr.jmmc.oitools.model.Target;
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
    /** granule set (insertion ordered) */
    private final Set<Granule> granules = new HashSet<Granule>();
    private final Set<OIData> oiDatas = new HashSet<OIData>();
    /** cached values */
    private List<Target> sortedTargets = null;
    private List<InstrumentMode> sortedInstrumentModes = null;
    private List<OIData> sortedOIDatas = null;

    public SelectorResult() {
        reset();
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
            final Set<Target> targets = new HashSet<Target>();
            for (Granule g : granules) {
                targets.add(g.getTarget());
            }
            final List<Target> sorted = new ArrayList<Target>(targets);
            Collections.sort(sorted, Target.CMP_TARGET);
            sortedTargets = sorted;
        }
        return sortedTargets;
    }

    public List<InstrumentMode> getDistinctInstrumentModes() {
        if (sortedInstrumentModes == null) {
            final Set<InstrumentMode> insModes = new HashSet<InstrumentMode>();
            for (Granule g : granules) {
                insModes.add(g.getInsMode());
            }
            final List<InstrumentMode> sorted = new ArrayList<InstrumentMode>(insModes);
            Collections.sort(sorted, InstrumentMode.CMP_INS_MODE);
            sortedInstrumentModes = sorted;
        }
        return sortedInstrumentModes;
    }

    public List<OIData> getSortedOIDatas() {
        if (sortedOIDatas == null) {
            final List<OIData> sorted = new ArrayList<OIData>(oiDatas);
            Collections.sort(sorted, OITableByFileComparator.INSTANCE);
            sortedOIDatas = sorted;
        }
        return sortedOIDatas;
    }

    @Override
    public String toString() {
        return "SelectorResult{" + "granules=" + granules + ", oiDatas=" + oiDatas + '}';
    }

}
