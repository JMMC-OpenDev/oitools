/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.model;

import fr.jmmc.oitools.model.range.Range;
import java.util.Set;

/**
 *
 */
public final class GranuleMatcher implements Matcher<Granule> {

    private final static GranuleMatcher MATCHER_LIKE = new GranuleMatcher(null);

    public static GranuleMatcher getInstance(final Set<Range> distinctWavelengthRanges) {
        if (distinctWavelengthRanges == null || distinctWavelengthRanges.isEmpty()) {
            return MATCHER_LIKE;
        }
        return new GranuleMatcher(distinctWavelengthRanges);
    }

    /** distinct Wavelength values */
    private final Set<Range> distinctWavelengthRanges;

    private GranuleMatcher(final Set<Range> distinctWavelengthRanges) {
        this.distinctWavelengthRanges = distinctWavelengthRanges;
    }

    public boolean isEmpty() {
        return !hasDistinctWavelengthRanges();
    }

    @Override
    public boolean match(final Granule pattern, final Granule candidate) {
        if (pattern == candidate) {
            return true;
        }
        if ((pattern.getTarget() != null) && (candidate.getTarget() != null)) {
            if (!pattern.getTarget().equals(candidate.getTarget())) {
                return false;
            }
        }
        if ((pattern.getInsMode() != null) && (candidate.getInsMode() != null)) {
            if (!pattern.getInsMode().equals(candidate.getInsMode())) {
                return false;
            }
        }
        if ((pattern.getNight() != null) && (candidate.getNight() != null)) {
            if (!pattern.getNight().equals(candidate.getNight())) {
                return false;
            }
        }
        if (pattern.hasDistinctStaNames() && candidate.hasDistinctStaNames()) {
            if (!match(pattern.getDistinctStaNames(), candidate.getDistinctStaNames())) {
                return false;
            }
        }
        if (pattern.hasDistinctMjdRanges() && candidate.hasDistinctMjdRanges()) {
            if (!Range.matchRanges(pattern.getDistinctMjdRanges(), candidate.getDistinctMjdRanges())) {
                return false;
            }
        }
        if (hasDistinctWavelengthRanges() && (candidate.getInsMode() != null)) {
            if (!Range.matchRange(getDistinctWavelengthRanges(), candidate.getInsMode().getWavelengthRange())) {
                return false;
            }
        }
        return true;
    }

    public boolean hasDistinctWavelengthRanges() {
        return (distinctWavelengthRanges != null) && !distinctWavelengthRanges.isEmpty();
    }

    public Set<Range> getDistinctWavelengthRanges() {
        return distinctWavelengthRanges;
    }

    @Override
    public String toString() {
        return "GranuleMatcher{"
                + ", distinctWavelengthRanges=" + distinctWavelengthRanges
                + '}';
    }

    public static boolean match(final Set<String> selected, final Set<String> candidates) {
        for (String sel : selected) {
            if (candidates.contains(sel)) {
                return true;
            }
        }
        return false;
    }

}
