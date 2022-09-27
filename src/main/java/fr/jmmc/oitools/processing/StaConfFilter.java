/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.processing;

import fr.jmmc.oitools.OIFitsConstants;
import fr.jmmc.oitools.fits.FitsTable;
import fr.jmmc.oitools.model.OIData;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;

/**
 * Specific filter on StaConfs
 * @author bourgesl
 */
public final class StaConfFilter extends FitsTableFilter<String> {

    // members:
    private final HashSet<short[]> staIndexMatchings = new HashSet<short[]>(); // identity
    private short[][] staConf = null;

    public StaConfFilter(final List<String> staConfs, final boolean include) {
        super(OIFitsConstants.COLUMN_STA_CONF, staConfs, include);
    }

    @Override
    protected void reset() {
        staIndexMatchings.clear();
        staConf = null;
    }

    @Override
    public FilterState prepare(final FitsTable fitsTable) {
        final OIData oiData = (OIData) fitsTable;

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "oiData distinct StaConfs: {0}", oiData.getDistinctStaConf());
        }
        // collect matching baselines (as usual staIndex instances):
        oiData.getMatchingStaConfs(getAcceptedValues(), staIndexMatchings);

        if (staIndexMatchings.isEmpty()) {
            logger.log(Level.FINE, "Skip {0}, no matching conf", fitsTable);
            // skip OIData (no match) if include or keep OIData (full) if exclude:
            return (include) ? FilterState.INVALID : FilterState.FULL;
        }
        logger.log(Level.FINE, "staIndexMatching: {0}", staIndexMatchings);

        if (oiData.getDistinctStaConf().size() == staIndexMatchings.size()) {
            // keep OIData (full) if include or skip OIData (no match) if exclude:
            return (include) ? FilterState.FULL : FilterState.INVALID;
        }
        // resolve column once
        staConf = fitsTable.getColumnDerivedShorts(columnName);

        if (staConf == null) {
            // missing column, ignore filter:
            return FilterState.FULL;
        }
        return FilterState.MASK;
    }

    public boolean accept(final int row, final int col) {
        return staIndexMatchings.contains(staConf[row]) == include;
    }
}
