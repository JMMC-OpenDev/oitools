/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.model;

import fr.jmmc.jmcs.util.StringUtils;
import fr.jmmc.oitools.OIFitsConstants;
import fr.jmmc.oitools.model.range.Range;
import fr.jmmc.oitools.util.StationNamesComparator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author bourgesl
 */
public final class OIDataListHelper {

    /** logger */
    protected final static Logger logger = Logger.getLogger(OIDataListHelper.class.getName());

    /* GetOIDataString operators */
    public final static GetOIDataString GET_ARR_NAME = new GetOIDataString() {
        @Override
        public String getString(final OIData oiData) {
            return oiData.getArrName();
        }
    };

    public final static GetOIDataString GET_INS_NAME = new GetOIDataString() {
        @Override
        public String getString(final OIData oiData) {
            return oiData.getInsName();
        }
    };

    public final static GetOIDataString GET_DATE_OBS = new GetOIDataString() {
        @Override
        public String getString(final OIData oiData) {
            return oiData.getDateObs();
        }
    };

    private OIDataListHelper() {
        super();
    }

    /**
     * Return the unique String values from given operator applied on given OIData tables
     * @param oiDataList OIData tables
     * @param set set instance to use
     * @param operator operator to get String values
     * @return unique String values
     */
    public static Set<String> getDistinct(final Collection<OIData> oiDataList, final Set<String> set, final GetOIDataString operator) {
        String value;
        for (OIData oiData : oiDataList) {
            value = operator.getString(oiData);
            if (value != null) {
                logger.log(Level.FINE, "getDistinct: {0}", value);

                int pos = value.indexOf('_');

                if (pos != -1) {
                    value = value.substring(0, pos);
                }
                set.add(value);
            }
        }
        return set;
    }

    /**
     * Return the unique String values from given operator applied on given OIData tables
     * @param oiDataList OIData tables
     * @param set set instance to use
     * @param operator operator to get String values
     * @return unique String values
     */
    public static Set<String> getDistinctNoSuffix(final Collection<OIData> oiDataList, final Set<String> set, final GetOIDataString operator) {
        String value;
        for (OIData oiData : oiDataList) {
            value = operator.getString(oiData);
            if (value != null) {
                logger.log(Level.FINE, "getDistinctNoSuffix: {0}", value);

                int pos = value.lastIndexOf('_');

                if (pos != -1) {
                    final String suffix = value.substring(pos + 1, value.length());
                    try {
                        Integer.parseInt(suffix);
                        // strip suffix:
                        value = value.substring(0, pos);
                    } catch (NumberFormatException nfe) {
                        if (logger.isLoggable(Level.FINE)) {
                            logger.log(Level.FINE, "getDistinctNoSuffix: " + suffix, nfe);
                        }
                        // use complete value
                    }
                }
                set.add(value);
            }
        }
        return set;
    }

    /**
     * Return the unique staNames values (sorted by name) from given OIData tables
     * @param oiDataList OIData tables
     * @param usedStaNamesMap Map of used staNames to StaNamesDir (reference StaNames / orientation)
     * @return given set instance
     */
    public static List<String> getDistinctStaNames(final Collection<OIData> oiDataList,
                                                   final Map<String, StaNamesDir> usedStaNamesMap) {

        final Set<String> set = new HashSet<String>(32);

        for (final OIData oiData : oiDataList) {
            for (final short[] staIndexes : oiData.getDistinctStaIndex()) {
                final String staNames = oiData.getRealStaNames(usedStaNamesMap, staIndexes);
                set.add(staNames);
            }
        }
        // Sort by name (consistent naming & colors):
        final List<String> sortedList = new ArrayList<String>(set);
        Collections.sort(sortedList, StationNamesComparator.INSTANCE);

        return sortedList;
    }

    /**
     * Return the unique staConfs values from given OIData tables
     * @param oiDataList OIData tables
     * @return given set instance
     */
    public static List<String> getDistinctStaConfs(final Collection<OIData> oiDataList) {
        final Set<String> set = new HashSet<String>(32);

        for (OIData oiData : oiDataList) {
            for (short[] staConf : oiData.getDistinctStaConf()) {
                final String staNames = oiData.getStaNames(staConf);
                set.add(staNames);
            }
        }
        // Sort by name (consistent naming & colors):
        final List<String> sortedList = new ArrayList<String>(set);
        Collections.sort(sortedList, StationNamesComparator.INSTANCE);

        logger.log(Level.FINE, "getDistinctStaConfs : {0}", sortedList);
        return sortedList;
    }

    /**
     * Return the global column range from given OIData tables
     * @param oiDataList OIData tables
     * @param name column name to extract values
     * @return Range instance or Range.UNDEFINED_RANGE if no data
     */
    public static Range getColumnRange(final Collection<OIData> oiDataList, final String name) {
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;

        for (OIData oiData : oiDataList) {
            final Range range = oiData.getColumnRange(name);

            if (range.isFinite()) {
                if (range.getMin() < min) {
                    min = range.getMin();
                }
                if (range.getMax() > max) {
                    max = range.getMax();
                }
            }
        }
        final Range range = Range.isFinite(min, max) ? new Range(min, max) : Range.UNDEFINED_RANGE;
        logger.log(Level.FINE, "getColumnRange : {0}", range);
        return range;
    }

    public static Range getWaveLengthRange(final Collection<OIData> oiDataList) {
        return getColumnRange(oiDataList, OIFitsConstants.COLUMN_EFF_WAVE);
    }

    public static void toString(final Set<String> set, final StringBuilder sb, final String internalSeparator, final String separator) {
        toString(set, sb, internalSeparator, separator, Integer.MAX_VALUE);
    }

    public static void toString(final Set<String> set, final StringBuilder sb, final String internalSeparator, final String separator, final int threshold, final String alternateText) {
        // hard coded limit:
        if (set.size() > threshold) {
            sb.append(alternateText);
        } else {
            toString(set, sb, internalSeparator, separator, Integer.MAX_VALUE);
        }
    }

    private static void toString(final Set<String> set, final StringBuilder sb, final String internalSeparator, final String separator, final int maxLength) {
        int n = 0;
        for (String v : set) {
            sb.append(StringUtils.replaceWhiteSpaces(v, internalSeparator)).append(separator);
            n++;
            if (n > maxLength) {
                return;
            }
        }
        if (n != 0) {
            // remove separator at the end:
            sb.setLength(sb.length() - separator.length());

        }
    }

    /**
     * Get String operator applied on any OIData table
     */
    public interface GetOIDataString {

        /**
         * Return a String value (keyword for example) for the given OIData table
         * @param oiData OIData table
         * @return String value
         */
        public String getString(final OIData oiData);
    }

}
