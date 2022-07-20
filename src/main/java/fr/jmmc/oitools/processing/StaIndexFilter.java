/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.processing;

import fr.jmmc.oitools.OIFitsConstants;
import fr.jmmc.oitools.fits.FitsTable;
import fr.jmmc.oitools.model.OIData;
import fr.jmmc.oitools.model.StaNamesDir;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

/**
 *
 * @author bourgesl
 */
public final class StaIndexFilter extends FitsTableFilter<String> {

    // members:
    private final Map<String, StaNamesDir> usedStaNamesMap;
    private final Set<short[]> staIndexMatchings = new HashSet<short[]>(); // identity
    private short[][] staIndexes = null;

    public StaIndexFilter(final Map<String, StaNamesDir> usedStaNamesMap, final List<String> baselines) {
        super(OIFitsConstants.COLUMN_STA_INDEX, baselines);
        this.usedStaNamesMap = usedStaNamesMap;
    }

    @Override
    protected void reset() {
        this.staIndexMatchings.clear();
        this.staIndexes = null;
    }

    @Override
    public FilterState prepare(final FitsTable fitsTable) {
        final OIData oiData = (OIData) fitsTable;

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "oiData distinct StaIndexes: {0}", oiData.getDistinctStaIndex());
        }
        // collect matching baselines (as usual staIndex instances):
        oiData.getMatchingStaIndexes(usedStaNamesMap, getAcceptedValues(), staIndexMatchings);

        if (staIndexMatchings.isEmpty()) {
            logger.log(Level.FINE, "Skip {0}, no matching baseline", fitsTable);
            // skip OIData (no match):
            return FilterState.INVALID;
        }
        logger.log(Level.FINE, "staIndexMatching: {0}", staIndexMatchings);

        if (oiData.getDistinctStaIndex().size() > staIndexMatchings.size()) {
            // resolve column once
            staIndexes = fitsTable.getColumnShorts(columnName);

            if (staIndexes == null) {
                // missing column, ignore filter:
                return FilterState.FULL;
            }
            return FilterState.MASK;
        }
        return FilterState.FULL;
    }

    public boolean accept(final int row, final int col) {
        return staIndexMatchings.contains(staIndexes[row]);
    }

}
