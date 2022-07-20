/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.processing;

import fr.jmmc.oitools.fits.FitsTable;
import fr.jmmc.oitools.model.range.Range;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * Abstract Filter on any FitsTable based on 1 column and a list of accepted values
 * @param <K> type of accepted values
 */
public abstract class FitsTableFilter<K> {

    /** logger */
    protected final static Logger logger = Logger.getLogger(FitsTableFilter.class.getName());

    public enum FilterState {
        INVALID,
        MASK,
        FULL
    }

    // members:
    protected final String columnName;
    protected final List<K> acceptedValues;

    FitsTableFilter(final String columnName, final List<K> acceptedValues) {
        this.columnName = columnName;
        this.acceptedValues = acceptedValues;
    }

    protected void reset() {
        // no-op by default
    }

    public abstract FilterState prepare(final FitsTable fitsTable);

    public abstract boolean accept(final int row, final int col);

    public String getColumnName() {
        return columnName;
    }

    public List<K> getAcceptedValues() {
        return acceptedValues;
    }

    public boolean is2D() {
        return false; // by default
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{columnName=" + columnName + ", acceptedValues=" + acceptedValues
                + ", is2D=" + is2D() + '}';
    }

    // --- utility methods ---
    public static List<String> asList(final String value) {
        return Arrays.asList(new String[]{value});
    }

    public static List<Range> asList(final Range value) {
        return Arrays.asList(new Range[]{value});
    }

    public static void resetFilters(final List<FitsTableFilter<?>> filters) {
        for (int f = 0, len = filters.size(); f < len; f++) {
            filters.get(f).reset();
        }
    }
}
