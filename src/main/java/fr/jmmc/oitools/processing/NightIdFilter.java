/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.processing;

import fr.jmmc.oitools.OIFitsConstants;
import fr.jmmc.oitools.fits.FitsTable;
import fr.jmmc.oitools.model.NightId;
import fr.jmmc.oitools.model.NightIdMatcher;
import fr.jmmc.oitools.model.OIData;
import java.util.List;

/**
 *
 * @author bourgesl
 */
public final class NightIdFilter extends FitsTableFilter<NightId> {

    // members:
    private NightIdMatcher nightIdMatcher = null;
    private int[] nightIds = null;

    public NightIdFilter(final List<Integer> nightIDs, final boolean include) {
        super(OIFitsConstants.COLUMN_NIGHT_ID, NightId.getCachedInstances(nightIDs), include); // always inclusive
    }

    @Override
    protected void reset() {
        nightIdMatcher = null;
        nightIds = null;
    }

    @Override
    public FilterState prepare(final FitsTable fitsTable) {
        final OIData oiData = (OIData) fitsTable;

        if (oiData.hasSingleNight()) {
            // keep OIData (full) if include or skip OIData (no match) if exclude:
            return (include) ? FilterState.FULL : FilterState.INVALID;
        }

        nightIdMatcher = new NightIdMatcher(getAcceptedValues());

        if (nightIdMatcher.matchAll(oiData.getDistinctNightId())) {
            // keep OIData (full) if include or skip OIData (no match) if exclude:
            return (include) ? FilterState.FULL : FilterState.INVALID;
        }
        // resolve column once
        nightIds = oiData.getNightId();

        if (nightIds == null) {
            // missing column, ignore filter:
            return FilterState.FULL;
        }
        return FilterState.MASK;
    }

    @Override
    public boolean accept(final int row, final int col) {
        return nightIdMatcher.match(nightIds[row]);
    }
}
