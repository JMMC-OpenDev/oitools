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

import fr.jmmc.oitools.model.range.Range;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Basic selector (target UID, instrument mode UID, night Id)
 */
public final class Selector {

    private String targetUID = null;
    private String insModeUID = null;
    private Integer nightID = null;
    // table selection filter expressed as extNb (integer) values + OIFits file (id)
    private final Map<String, List<Integer>> extNbsPerOiFitsPath = new HashMap<String, List<Integer>>();
    // criteria on baselines (1, 2 or 3T)
    private List<String> baselines = null;
    // criteria on MJD ranges
    private List<Range> mjdRanges = null;
    // criteria on wavelength ranges
    private List<Range> wavelengthRanges = null;

    public Selector() {
        reset();
    }

    public void reset() {
        targetUID = null;
        insModeUID = null;
        nightID = null;
        extNbsPerOiFitsPath.clear();
        baselines = null;
        mjdRanges = null;
        wavelengthRanges = null;
    }

    public String getTargetUID() {
        return targetUID;
    }

    public void setTargetUID(String targetUID) {
        if ((targetUID != null) && targetUID.trim().isEmpty()) {
            targetUID = null;
        }
        this.targetUID = targetUID;
    }

    public String getInsModeUID() {
        return insModeUID;
    }

    public void setInsModeUID(String insModeUID) {
        if ((insModeUID != null) && insModeUID.trim().isEmpty()) {
            insModeUID = null;
        }
        this.insModeUID = insModeUID;
    }

    public Integer getNightID() {
        return nightID;
    }

    public void setNightID(final Integer nightID) {
        this.nightID = nightID;
    }

    public boolean hasTable() {
        return !extNbsPerOiFitsPath.isEmpty();
    }

    public List<Integer> getTables(final String oiFitsPath) {
        return extNbsPerOiFitsPath.get(oiFitsPath);
    }

    public void addTable(final String oiFitsPath, final Integer extNb) {
        List<Integer> extNbs = extNbsPerOiFitsPath.get(oiFitsPath);
        if (extNbs == null) {
            extNbs = new ArrayList<Integer>(1);
            extNbsPerOiFitsPath.put(oiFitsPath, extNbs);
        }
        if (extNb != null) {
            extNbs.add(extNb);
        }
    }

    public boolean hasBaselines() {
        return (baselines != null) && !baselines.isEmpty();
    }

    public List<String> getBaselines() {
        return baselines;
    }

    public void setBaselines(final List<String> baselines) {
        this.baselines = baselines;
    }

    public boolean hasMJDRanges() {
        return (mjdRanges != null) && !mjdRanges.isEmpty();
    }

    public List<Range> getMJDRanges() {
        return mjdRanges;
    }

    public void setMJDRanges(final List<Range> mjdRanges) {
        this.mjdRanges = mjdRanges;
    }

    public boolean hasWavelengthRange() {
        return (wavelengthRanges != null) && !wavelengthRanges.isEmpty();
    }

    public List<Range> getWavelengthRanges() {
        return wavelengthRanges;
    }

    public void setWavelengthRanges(final List<Range> wavelengthRanges) {
        this.wavelengthRanges = wavelengthRanges;
    }

    public boolean isEmpty() {
        return (targetUID == null) && (insModeUID == null) && (nightID == null)
                && !hasTable() && !hasMJDRanges() && !hasBaselines();
    }

    @Override
    public String toString() {
        return "Selector["
                + ((targetUID != null) ? " targetUID: " + targetUID : "")
                + ((insModeUID != null) ? " insModeUID: " + insModeUID : "")
                + ((nightID != null) ? " nightID: " + nightID : "")
                + (hasTable() ? " extNbsPerOiFitsPath: " + extNbsPerOiFitsPath : "")
                + (hasBaselines() ? " baselines: " + baselines : "")
                + (hasMJDRanges() ? " mjdRanges: " + mjdRanges : "")
                + (hasWavelengthRange() ? " wavelengthRanges: " + wavelengthRanges : "")
                + ']';
    }

}
