/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.processing;

import fr.jmmc.oitools.OIFitsConstants;
import fr.jmmc.oitools.fits.FitsTable;
import fr.jmmc.oitools.model.NightId;
import fr.jmmc.oitools.model.NightIdMatcher;
import fr.jmmc.oitools.model.OIData;
import java.util.Arrays;
import java.util.logging.Level;

/**
 *
 * @author bourgesl
 */
public final class NightIdFilter extends FitsTableFilter<NightId> {

    // members:
    private NightIdMatcher nightIdMatcher = null;
    private int[] nightIds = null;

    public NightIdFilter(final Integer nightID) {
        super(OIFitsConstants.COLUMN_NIGHT_ID,
                Arrays.asList(new NightId[]{NightId.getCachedInstance(nightID)}));
    }

    @Override
    protected void reset() {
        this.nightIdMatcher = null;
        this.nightIds = null;
    }

    @Override
    public FilterState prepare(final FitsTable fitsTable) {
        final OIData oiData = (OIData) fitsTable;

        if (oiData.hasSingleNight()) {
            // implicitely matching selected night
            return FilterState.FULL;
        }

        final NightId nightId = getAcceptedValues().get(0);

        if (nightId != null) {
            this.nightIdMatcher = new NightIdMatcher(nightId);
        }

        if (nightIdMatcher == null) {
            logger.log(Level.FINE, "Skip {0}, no matching range", fitsTable);
            // skip OIData (no match):
            return FilterState.INVALID;
        }

        if (!nightIdMatcher.matchAll(oiData.getDistinctNightId())) {
            // resolve column once
            nightIds = oiData.getNightId();

            if (nightIds == null) {
                // missing column, ignore filter:
                return FilterState.FULL;
            }
            return FilterState.MASK;
        }
        return FilterState.FULL;
    }

    public boolean accept(final int row, final int col) {
        return nightIdMatcher.match(nightIds[row]);
    }

}
