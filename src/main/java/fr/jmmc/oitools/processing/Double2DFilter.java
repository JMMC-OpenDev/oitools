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
 * Generic numeric filter on double[][] 2D arrays
 * @author bourgesl
 */
public final class Double2DFilter extends FitsTableFilter<Range> {

    // members:
    private double[][] tableColumn2D = null;
    private final Set<Range> rangeMatchings = new HashSet<Range>();

    public Double2DFilter(final String columnName, final List<Range> acceptedValues) {
        super(columnName, acceptedValues);
    }

    @Override
    protected void reset() {
        this.tableColumn2D = null;
        rangeMatchings.clear();
    }

    @Override
    public boolean is2D() {
        return true;
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
            tableColumn2D = fitsTable.getColumnAsDoubles(columnName);

            if (tableColumn2D == null) {
                // missing column, ignore filter:
                return FilterState.FULL;
            }
            return FilterState.MASK;
        }
        return FilterState.FULL;
    }

    public boolean accept(final int row, final int col) {
        return Range.contains(rangeMatchings, tableColumn2D[row][col]);
    }

}
