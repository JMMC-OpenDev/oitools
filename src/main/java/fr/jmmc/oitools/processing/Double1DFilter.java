/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.processing;

import fr.jmmc.oitools.fits.FitsTable;
import fr.jmmc.oitools.model.range.Range;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

/**
 * Generic numeric filter on double[] 1D arrays
 * @author bourgesl
 */
public final class Double1DFilter extends FitsTableFilter<Range> {

    // members:
    private double[] tableColumn1D = null;
    private final Set<Range> rangeMatchings = new HashSet<Range>();

    public Double1DFilter(final String columnName, final List<Range> acceptedValues) {
        super(columnName, acceptedValues);
    }

    @Override
    void reset() {
        this.tableColumn1D = null;
        rangeMatchings.clear();
    }

    @Override
    public FilterState prepare(final FitsTable fitsTable) {
        final Range tableRange = fitsTable.getColumnRange(columnName);
        logger.log(Level.FINE, "prepare: table range: {0}", tableRange);

        if (!tableRange.isFinite()) {
            // missing column or no data, ignore filter:
            return FilterState.FULL;
        }

        // get only matching ranges:
        Range.getMatchingSelected(getAcceptedValues(), tableRange, rangeMatchings);

        if (rangeMatchings.isEmpty()) {
            logger.log(Level.FINE, "Skip {0}, no matching range", fitsTable);
            // skip OIData (no match):
            return FilterState.INVALID;
        }
        logger.log(Level.FINE, "prepare: matching ranges: {0}", rangeMatchings);

        if (!Range.matchFully(rangeMatchings, tableRange)) {
            // resolve column once
            tableColumn1D = fitsTable.getColumnAsDouble(columnName);

            if (tableColumn1D == null) {
                // missing column, ignore filter:
                return FilterState.FULL;
            }
            return FilterState.MASK;
        }
        return FilterState.FULL;
    }

    public boolean accept(final int row, final int col) {
        return Range.contains(rangeMatchings, tableColumn1D[row]);
    }

}
