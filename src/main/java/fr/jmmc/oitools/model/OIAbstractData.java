/* 
 * Copyright (C) 2018 CNRS - JMMC project ( http://www.jmmc.fr )
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.model;

import fr.jmmc.oitools.OIFitsConstants;
import fr.jmmc.oitools.meta.ColumnMeta;
import fr.jmmc.oitools.meta.KeywordMeta;
import fr.jmmc.oitools.meta.Types;
import fr.jmmc.oitools.meta.Units;
import static fr.jmmc.oitools.model.ModelBase.UNDEFINED_STRING;
import static fr.jmmc.oitools.model.ModelBase.logger;
import fr.jmmc.oitools.model.range.Range;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

/**
 * Class to mutualize methods between OIData Tables and OIInspol
 * @author kempsc
 */
public abstract class OIAbstractData extends OITable {

    /** members */
    /** cached reference on OI_ARRAY table associated to this OIData table */
    private OIArray oiArrayRef = null;
    /* cached analyzed data */
    /** distinct targetId values present in this table (identity hashcode) */
    private final Set<Short> distinctTargetId = new LinkedHashSet<Short>();
    /** distinct nightId values present in this table (identity hashcode) */
    private final Set<NightId> distinctNightId = new LinkedHashSet<NightId>();
    /** distinct StaIndex values present in this table (identity hashcode) */
    private final Set<short[]> distinctStaIndex = new LinkedHashSet<short[]>();
    /** cached StaNames corresponding to given OIData StaIndex arrays (identity hashcode) */
    private final Map<short[], String> staIndexesToString = new IdentityHashMap<short[], String>();
    /** map of distinct StaIndex arrays to sorted StaNames + orientation (identity hashcode) */
    private final Map<short[], StaNamesDir> staIndexesToSortedStaNamesDir = new IdentityHashMap<short[], StaNamesDir>();

    /**
     * Protected OIAbstractData class constructor
     * @param oifitsFile main OifitsFile
     */
    protected OIAbstractData(final OIFitsFile oifitsFile) {
        this(oifitsFile, true);
    }

    /**
     * Protected OIAbstractData class constructor
     * @param oifitsFile main OifitsFile
     * @param useCommonCols flag indicating to add common columns (OI_VIS, OI_VIS2, OI_T3, OI_FLUX)
     */
    protected OIAbstractData(final OIFitsFile oifitsFile, final boolean useCommonCols) {
        super(oifitsFile);

        // since every child class constructor calls the super
        // constructor, next keywords will be common to every subclass :
        // ARRNAME  Optional keyword definition
        addKeywordMeta(new KeywordMeta(OIFitsConstants.KEYWORD_ARRNAME, "name of corresponding array", Types.TYPE_CHAR, Units.NO_UNIT, true) {
            @Override
            public String[] getStringAcceptedValues() {
                return getOIFitsFile().getAcceptedArrNames();
            }
        });

        // TARGET_ID  column definition
        addColumnMeta(new ColumnMeta(OIFitsConstants.COLUMN_TARGET_ID,
                "target number as index into OI_TARGET table", Types.TYPE_SHORT) {
            @Override
            public short[] getIntAcceptedValues() {
                return getOIFitsFile().getAcceptedTargetIds();
            }
        });
    }

    /**
     * Return the number of measurements in this table (= number of rows)
     * @return the number of measurements.
     */
    public final int getNbMeasurements() {
        return getNbRows();
    }

    /**
     * Return the number of data points in this table (= number of rows * number of wavelengths)
     * @return the number of data points.
     */
    public final int getNbDataPoints() {
        return getNbRows() * getNWave();
    }

    /*
     * --- Keywords ------------------------------------------------------------
     */
    /**
     * Return the Optional ARRNAME keyword value.
     * @return the value of ARRNAME keyword if present, NULL otherwise.
     */
    public final String getArrName() {
        return getKeyword(OIFitsConstants.KEYWORD_ARRNAME);
    }

    /**
     * Define the Optional ARRNAME keyword value
     * @param arrName value of ARRNAME keyword
     */
    public final void setArrName(final String arrName) {
        setKeyword(OIFitsConstants.KEYWORD_ARRNAME, arrName);
        // reset cached reference :
        this.oiArrayRef = null;
    }

    /*
     * --- Columns -------------------------------------------------------------
     */
    /**
     * Return the TARGET_ID column.
     * @return the TARGET_ID column.
     */
    public final short[] getTargetId() {
        return this.getColumnShort(OIFitsConstants.COLUMN_TARGET_ID);
    }

    /**
     * Define the TARGET_ID column.
     * @param targetIds the TARGET_ID column.
     */
    public void setTargetId(final short[] targetIds) {
        this.setColumnValue(OIFitsConstants.COLUMN_TARGET_ID, targetIds);
    }

    /**
     * Return the STA_INDEX column.
     * @return the STA_INDEX column.
     */
    public final short[][] getStaIndex() {
        return this.getColumnShorts(OIFitsConstants.COLUMN_STA_INDEX);
    }

    /* --- Utility methods for cross-referencing --- */
    /**
     * Return the number of distinct spectral channels (NWAVE) of the associated OI_WAVELENGTH table(s).
     * @return the number of distinct spectral channels (NWAVE) of the associated OI_WAVELENGTH table(s)
     * or 0 if the OI_WAVELENGTH table(s) are missing !
     * Note: this method is used by WaveColumnMeta.getRepeat() to determine the column dimensions
     */
    public abstract int getNWave();

    /**
     * Return the associated OITarget table.
     * @return the associated OITarget
     */
    public final OITarget getOiTarget() {
        return getOIFitsFile().getOiTarget();
    }

    /**
     * Return the associated optional OIArray table.
     * @return the associated OIArray or null if the keyword ARRNAME is undefined
     */
    public final OIArray getOiArray() {
        /** cached resolved reference */
        if (this.oiArrayRef != null) {
            return this.oiArrayRef;
        }

        final String arrName = getArrName();
        if (arrName != null) {
            final OIFitsFile oiFitsFile = getOIFitsFile();
            final OIArray oiArray = oiFitsFile.getOiArray(arrName);

            if (oiArray != null) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, "Resolved OI_Array reference [{0}] to {1}",
                            new Object[]{oiArray.getExtNb(), super.toString()});
                }
                this.oiArrayRef = oiArray;
            } else if (!oiFitsFile.hasMissingTableName(arrName) && logger.isLoggable(Level.WARNING)) {
                oiFitsFile.addMissingTableName(arrName);
                logger.log(Level.WARNING, "Missing OI_Array table identified by ARRNAME=''{0}''", arrName);
            }
            return oiArray;
        }

        return null;
    }

    /**
     * Mediator method to resolve cross references. Returns the accepted (ie
     * valid) station indexes for the associated OIArray table.
     *
     * @return the array containing the indexes.
     */
    public final short[] getAcceptedStaIndexes() {
        return getOIFitsFile().getAcceptedStaIndexes(getOiArray());
    }

    /* --- Other methods --- */
    /**
     * Check arrname / oiarray and MJD range in addition to OITable.checkKeywords()
     * @param checker checker component
     */
    @Override
    public void checkKeywords(final OIFitsChecker checker) {
        super.checkKeywords(checker);

        if (OIFitsChecker.isInspectRules() || ((getArrName() != null) && (getOiArray() == null))) {
            // rule [ARRNAME_REF] check if an OI_ARRAY table matches the ARRNAME keyword
            if (checker != null) {
                checker.ruleFailed(Rule.ARRNAME_REF, this, OIFitsConstants.KEYWORD_ARRNAME).addKeywordValue(getArrName());
            }
        }
    }

    /**
     * Check if the MJD value is within 'normal' range for datas
     * @param checker checker component
     * @param name column name
     */
    protected void checkMJDColumn(final OIFitsChecker checker, final String name) {
        final Range mjdRange = getColumnRange(name);
        checkMJD(checker, name, mjdRange.getMin());
        checkMJD(checker, name, mjdRange.getMax());
    }

    /* --- data analysis --- */
    /**
     * Indicate to clear any cached value (derived column ...)
     */
    @Override
    public void setChanged() {
        super.setChanged();
        distinctTargetId.clear();
        distinctNightId.clear();
        distinctStaIndex.clear();
        staIndexesToString.clear();
        staIndexesToSortedStaNamesDir.clear();
    }

    /**
     * Get distinct targetId values present in this table
     * @return distinctTargetId
     */
    public final Set<Short> getDistinctTargetId() {
        return distinctTargetId;
    }

    /**
     * Get boolean for distinct targetId values
     * @return true if size of distinctTargetId == 1
     */
    public final boolean hasSingleTarget() {
        return getDistinctTargetId().size() == 1;
    }

    /**
     * Return the targetId Matcher corresponding to the given Target UID (global) or null if missing
     * @param tm TargetManager instance
     * @param targetUID target UID (global)
     * @return targetId Matcher corresponding to the given Target UID (global) or null if missing
     */
    public final TargetIdMatcher getTargetIdMatcher(final TargetManager tm, final String targetUID) {
        final OITarget oiTarget = getOiTarget();
        if (oiTarget != null) {
            return oiTarget.getTargetIdMatcher(tm, targetUID);
        }
        return null;
    }

    /**
     * Return the targetId Matcher corresponding to the given Target (global) or null if missing
     * @param tm TargetManager instance
     * @param globalTarget target (global)
     * @return targetId Matcher corresponding to the given Target (global) or null if missing
     */
    public final TargetIdMatcher getTargetIdMatcher(final TargetManager tm, final Target globalTarget) {
        final OITarget oiTarget = getOiTarget();
        if (oiTarget != null) {
            return oiTarget.getTargetIdMatcher(tm, globalTarget);
        }
        return null;
    }

    /**
     * Get distinct nightId values present in this table
     * @return distinctNightId
     */
    public final Set<NightId> getDistinctNightId() {
        return distinctNightId;
    }

    /**
     * Get boolean for distinct nightId values
     * @return true if size of distinctNightId == 1
     */
    public final boolean hasSingleNight() {
        return getDistinctNightId().size() == 1;
    }

    /**
     * Get distinct StaIndex values present in this table
     * @return distinctStaIndex
     */
    public final Set<short[]> getDistinctStaIndex() {
        return distinctStaIndex;
    }

    /**
     * Get size of distinct StaIndex values 
     * @return distinctStaIndex size
     */
    public final int getDistinctStaIndexCount() {
        return distinctStaIndex.size();
    }

    /**
     * Used by OIFitsExplorer
     * @return distinct StaIndexes as arrays
     */
    public final short[][] getDistinctStaIndexes() {
        final short[][] distinctStaIndexes = new short[distinctStaIndex.size()][];
        int i = 0;
        for (short[] staIndexes : distinctStaIndex) {
            distinctStaIndexes[i++] = staIndexes;
        }
        return distinctStaIndexes;
    }

    /**
     * Return the StaName value corresponding to given OIData StaIndex value
     * @param staIndex staIndex as short
     * @return StaName if found; staIndex as String otherwise
     */
    public String getStaName(final short staIndex) {
        final OIArray oiArray = getOiArray();

        if (oiArray != null) {
            return oiArray.getStaName(staIndex);
        }
        // fallback if ARRNAME is missing:
        return Short.toString(staIndex);
    }

    /**
     * Cached StaNames corresponding to given OIData StaIndex arrays (IDENTITY)
     * @param staIndexes staIndex array
     * @return label
     */
    public final String getStaNames(final short[] staIndexes) {
        if (staIndexes == null) {
            return UNDEFINED_STRING;
        }
        final OIArray oiArray = getOiArray();

        if (oiArray != null) {
            return oiArray.getStaNames(staIndexes);
        }
        // fallback if ARRNAME is missing:
        // warning: identity hashcode so use carefully using distinct array instances:
        String label = staIndexesToString.get(staIndexes);

        if (label == null) {
            final StringBuilder sb = new StringBuilder(32);

            for (short staIndex : staIndexes) {
                sb.append(staIndex).append('-');
            }
            sb.setLength(sb.length() - 1);

            label = sb.toString();

            staIndexesToString.put(staIndexes, label);
        }
        return label;
    }

    /**
     * Get the map of distinct StaIndex arrays to sorted StaNames + orientation (identity hashcode)
     * @return map of distinct StaIndex arrays to sorted StaNames + orientation (identity hashcode)
     */
    public final Map<short[], StaNamesDir> getStaIndexesToSortedStaNamesDir() {
        return staIndexesToSortedStaNamesDir;
    }

    /**
     * Get the StaNamesDir corresponding to given OIData StaIndex arrays
     * @param staIndexes staIndex array
     * @return StaNamesDir or null
     */
    public final StaNamesDir getSortedStaNamesDir(final short[] staIndexes) {
        return staIndexesToSortedStaNamesDir.get(staIndexes);
    }

    /**
     * Return the real (used) staNames values
     * @param usedStaNamesMap Map of used staNames to StaNamesDir (reference StaNames / orientation)
     * @param staIndexes staIndex array
     * @return real (used) staNames values
     */
    public String getRealStaNames(final Map<String, StaNamesDir> usedStaNamesMap,
                                  final short[] staIndexes) {
        if (staIndexes != null) {
            final StaNamesDir sortedStaNamesDir = getSortedStaNamesDir(staIndexes);

            if (sortedStaNamesDir == null) {
                logger.log(Level.WARNING, "getRealStaNames: bad staIndexesToSortedStaNamesDir: missing {0}", Arrays.toString(staIndexes));
            } else {
                // find the previous (real) baseline corresponding to the sorted StaNames (stable):
                final StaNamesDir refStaNamesDir = usedStaNamesMap.get(sortedStaNamesDir.getStaNames());

                if (refStaNamesDir == null) {
                    logger.log(Level.WARNING, "getRealStaNames: bad usedStaNamesMap: missing {0}", sortedStaNamesDir.getStaNames());
                } else {
                    return refStaNamesDir.getStaNames();
                }
            }
        }
        return UNDEFINED_STRING;
    }

    /**
     * Find the StaIndexes instances corresponding to the selected StaNames (using getRealStaNames)
     * @param usedStaNamesMap Map of used staNames to StaNamesDir (reference StaNames / orientation)
     * @param selectedStaNames selected StaNames to match
     * @param staIndexMatching set to store StaIndexes instances
     */
    public void getMatchingStaIndexes(final Map<String, StaNamesDir> usedStaNamesMap,
                                      final List<String> selectedStaNames,
                                      final Set<short[]> staIndexMatching) {
        staIndexMatching.clear();

        for (final short[] staIndexes : getDistinctStaIndex()) {
            final String staNames = getRealStaNames(usedStaNamesMap, staIndexes);

            if (selectedStaNames.contains(staNames)) {
                staIndexMatching.add(staIndexes);
            }
        }
    }

}
