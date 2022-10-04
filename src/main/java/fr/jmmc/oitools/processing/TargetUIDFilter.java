/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.processing;

import fr.jmmc.oitools.OIFitsConstants;
import fr.jmmc.oitools.fits.FitsTable;
import fr.jmmc.oitools.model.OIData;
import fr.jmmc.oitools.model.TargetIdMatcher;
import fr.jmmc.oitools.model.TargetManager;
import java.util.List;
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

    public TargetUIDFilter(final TargetManager tm, final List<String> targetUIDs) {
        super(OIFitsConstants.COLUMN_TARGET_ID, targetUIDs, true); // always inclusive
        this.tm = tm;
    }

    @Override
    protected void reset() {
        tm = null;
        targetIdMatcher = null;
        targetIds = null;
    }

    @Override
    public FilterState prepare(final FitsTable fitsTable) {
        final OIData oiData = (OIData) fitsTable;

        if (oiData.hasSingleTarget()) {
            // implicitely matching selected target
            return FilterState.FULL;
        }

        // targetIDs can not be null as the OIData table is supposed to have the target:
        targetIdMatcher = oiData.getTargetIdMatcherByUIDs(this.tm, getAcceptedValues());

        if (targetIdMatcher == null) {
            logger.log(Level.FINE, "Skip {0}, no matching targetUID", fitsTable);
            // skip OIData (no match) if include or keep OIData (full) if exclude:
            return (include) ? FilterState.INVALID : FilterState.FULL;
        }

        if (targetIdMatcher.matchAll(oiData.getDistinctTargetId())) {
            // keep OIData (full) if include or skip OIData (no match) if exclude:
            return (include) ? FilterState.FULL : FilterState.INVALID;
        }
        // resolve column once
        targetIds = fitsTable.getColumnShort(columnName);

        if (targetIds == null) {
            // missing column, ignore filter:
            return FilterState.FULL;
        }
        return FilterState.MASK;
    }

    @Override
    public boolean accept(final int row, final int col) {
        return targetIdMatcher.match(targetIds[row]);
    }

}
