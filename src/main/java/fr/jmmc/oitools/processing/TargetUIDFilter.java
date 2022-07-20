/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.processing;

import fr.jmmc.oitools.OIFitsConstants;
import fr.jmmc.oitools.fits.FitsTable;
import fr.jmmc.oitools.model.OIData;
import fr.jmmc.oitools.model.TargetIdMatcher;
import fr.jmmc.oitools.model.TargetManager;
import java.util.logging.Level;

/**
 *
 * @author bourgesl
 */
public final class TargetUIDFilter extends FitsTableFilter<String> {

    // members:
    private TargetManager tm = null;
    private TargetIdMatcher targetIdMatcher = null;
    private short[] targetIds = null;

    public TargetUIDFilter(final TargetManager tm, final String targetUID) {
        super(OIFitsConstants.COLUMN_TARGET_ID, FitsTableFilter.asList(targetUID));
        this.tm = tm;
    }

    @Override
    protected void reset() {
        this.tm = null;
        this.targetIdMatcher = null;
        this.targetIds = null;
    }

    @Override
    public FilterState prepare(final FitsTable fitsTable) {
        final OIData oiData = (OIData) fitsTable;

        if (oiData.hasSingleTarget()) {
            // implicitely matching selected target
            return FilterState.FULL;
        }

        final String targetUID = getAcceptedValues().get(0);

        // targetID can not be null as the OIData table is supposed to have the target:
        this.targetIdMatcher = oiData.getTargetIdMatcher(this.tm, targetUID);

        if (targetIdMatcher == null) {
            logger.log(Level.FINE, "Skip {0}, no matching range", fitsTable);
            // skip OIData (no match):
            return FilterState.INVALID;
        }

        if (!targetIdMatcher.matchAll(oiData.getDistinctTargetId())) {
            // resolve column once
            targetIds = fitsTable.getColumnShort(columnName);

            if (targetIds == null) {
                // missing column, ignore filter:
                return FilterState.FULL;
            }
            return FilterState.MASK;
        }
        return FilterState.FULL;
    }

    public boolean accept(final int row, final int col) {
        return targetIdMatcher.match(targetIds[row]);
    }

}
