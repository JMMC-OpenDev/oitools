/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.model;

import fr.jmmc.oitools.model.range.Range;
import fr.jmmc.oitools.processing.Selector;
import fr.jmmc.oitools.processing.Selector.FilterValues;
import java.util.Collection;

/**
 * Specific Matcher for Granule instances (staNames, wavelength, mjd ranges)
 */
public final class GranuleMatcher implements Matcher<Granule> {

    private final static GranuleMatcher MATCHER_LIKE = new GranuleMatcher(null, null, null);

    public static GranuleMatcher getInstance(final Selector selector) {

        // Baselines criteria:
        final FilterValues<String> filterValuesStaIndex = selector.getFilterValues(Selector.FILTER_STAINDEX);
        // MJD ranges:
        final FilterValues<Range> filterValuesMjd = selector.getFilterValues(Selector.FILTER_MJD);
        // Wavelength ranges:
        final FilterValues<Range> filterValuesEffWave = selector.getFilterValues(Selector.FILTER_EFFWAVE);

        if (isEmpty(filterValuesStaIndex)
                && isEmpty(filterValuesMjd)
                && isEmpty(filterValuesEffWave)) {
            return MATCHER_LIKE;
        }
        return new GranuleMatcher(filterValuesStaIndex, filterValuesMjd, filterValuesEffWave);
    }

    // members:
    /** StaIndex  filter values */
    private final FilterValues<String> filterValuesStaIndex;
    /** MJD filter values */
    private final FilterValues<Range> filterValuesMjd;
    /** Wavelength filter values */
    private final FilterValues<Range> filterValuesEffWave;

    private GranuleMatcher(final FilterValues<String> filterValuesStaIndex,
                           final FilterValues<Range> filterValuesMjd,
                           final FilterValues<Range> filterValuesEffWave) {

        this.filterValuesStaIndex = isEmpty(filterValuesStaIndex) ? null : filterValuesStaIndex;
        this.filterValuesMjd = isEmpty(filterValuesMjd) ? null : filterValuesMjd;
        this.filterValuesEffWave = isEmpty(filterValuesEffWave) ? null : filterValuesEffWave;
    }

    public boolean isEmpty() {
        return (filterValuesStaIndex != null)
                && (filterValuesMjd != null)
                && (filterValuesEffWave != null);
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
        // use matcher:
        if ((filterValuesStaIndex != null) && candidate.hasDistinctStaNames()) {
            if ((filterValuesStaIndex.getIncludeValues() != null)
                    && !match(filterValuesStaIndex.getIncludeValues(), candidate.getDistinctStaNames())) {
                return false;
            }
            if ((filterValuesStaIndex.getExcludeValues() != null)
                    && matchAll(filterValuesStaIndex.getExcludeValues(), candidate.getDistinctStaNames())) {
                return false;
            }
        }
        if ((filterValuesMjd != null) && candidate.hasMjdRange()) {
            if ((filterValuesMjd.getIncludeValues() != null)
                    && !Range.matchRange(filterValuesMjd.getIncludeValues(), candidate.getMjdRange())) {
                return false;
            }
            if ((filterValuesMjd.getExcludeValues() != null)
                    && Range.matchFully(filterValuesMjd.getExcludeValues(), candidate.getMjdRange())) {
                return false;
            }
        }
        if ((filterValuesEffWave != null) && (candidate.getInsMode() != null)) {
            if ((filterValuesEffWave.getIncludeValues() != null)
                    && !Range.matchRange(filterValuesEffWave.getIncludeValues(), candidate.getInsMode().getWavelengthRange())) {
                return false;
            }
            if ((filterValuesEffWave.getExcludeValues() != null)
                    && Range.matchFully(filterValuesEffWave.getExcludeValues(), candidate.getInsMode().getWavelengthRange())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "GranuleMatcher{" + "filterValuesStaIndex=" + filterValuesStaIndex
                + ", filterValuesMjd=" + filterValuesMjd
                + ", filterValuesEffWave=" + filterValuesEffWave + '}';
    }

    private static boolean match(final Collection<String> selected, final Collection<String> candidates) {
        for (String sel : selected) {
            if (candidates.contains(sel)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matchAll(final Collection<String> selected, final Collection<String> candidates) {
        for (String cand : candidates) {
            if (!selected.contains(cand)) {
                // 1 candidate value not present in selected
                return false;
            }
        }
        return true;
    }

    private static boolean isEmpty(final FilterValues fv) {
        return (fv == null) || fv.isEmpty();
    }
}
