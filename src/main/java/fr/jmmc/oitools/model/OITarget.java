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

import fr.jmmc.jmcs.util.NumberUtils;
import fr.jmmc.oitools.OIFitsConstants;
import fr.jmmc.oitools.meta.ColumnMeta;
import fr.jmmc.oitools.meta.Types;
import fr.jmmc.oitools.meta.Units;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class for OI_TARGET table.
 */
public final class OITarget extends OITable {

    /* constants */
    private final static double COORD_ZERO_EPSILON = Double.MIN_VALUE;

    /* static descriptors */
    /** TARGET_ID column descriptor */
    private final static ColumnMeta COLUMN_TARGET_ID = new ColumnMeta(OIFitsConstants.COLUMN_TARGET_ID,
            "index number", Types.TYPE_SHORT);
    /** TARGET column descriptor */
    private final static ColumnMeta COLUMN_TARGET = new ColumnMeta(OIFitsConstants.COLUMN_TARGET,
            "target name", Types.TYPE_CHAR, 16);
    /** RAEP0 column descriptor */
    private final static ColumnMeta COLUMN_RAEP0 = new ColumnMeta(OIFitsConstants.COLUMN_RAEP0,
            "RA at mean equinox", Types.TYPE_DBL, Units.UNIT_DEGREE);
    /** DECEP0 column descriptor */
    private final static ColumnMeta COLUMN_DECEP0 = new ColumnMeta(OIFitsConstants.COLUMN_DECEP0,
            "DEC at mean equinox", Types.TYPE_DBL, Units.UNIT_DEGREE);
    /** EQUINOX column descriptor */
    private final static ColumnMeta COLUMN_EQUINOX = new ColumnMeta(OIFitsConstants.COLUMN_EQUINOX,
            "equinox", Types.TYPE_REAL, Units.UNIT_YEAR);
    /** RA_ERR column descriptor */
    private final static ColumnMeta COLUMN_RA_ERR = new ColumnMeta(OIFitsConstants.COLUMN_RA_ERR,
            "error in RA at mean equinox", Types.TYPE_DBL, Units.UNIT_DEGREE);
    /** DEC_ERR column descriptor */
    private final static ColumnMeta COLUMN_DEC_ERR = new ColumnMeta(OIFitsConstants.COLUMN_DEC_ERR,
            "error in DEC at mean equinox", Types.TYPE_DBL, Units.UNIT_DEGREE);
    /** SYSVEL column descriptor */
    private final static ColumnMeta COLUMN_SYSVEL = new ColumnMeta(OIFitsConstants.COLUMN_SYSVEL,
            "systemic radial velocity", Types.TYPE_DBL, Units.UNIT_METER_PER_SECOND);
    /** VELTYP column descriptor */
    private final static ColumnMeta COLUMN_VELTYP = new ColumnMeta(OIFitsConstants.COLUMN_VELTYP,
            "reference for radial velocity", Types.TYPE_CHAR, 8,
            new String[]{OIFitsConstants.COLUMN_VELTYP_LSR, OIFitsConstants.COLUMN_VELTYP_HELIOCEN,
                         OIFitsConstants.COLUMN_VELTYP_BARYCENT, OIFitsConstants.COLUMN_VELTYP_GEOCENTR,
                         OIFitsConstants.COLUMN_VELTYP_TOPOCENT, OIFitsConstants.UNKNOWN_VALUE});
    // Note : UNKNOWN is not in OIFits standard
    /** VELDEF column descriptor */
    private final static ColumnMeta COLUMN_VELDEF = new ColumnMeta(OIFitsConstants.COLUMN_VELDEF,
            "definition of radial velocity", Types.TYPE_CHAR, 8,
            new String[]{OIFitsConstants.COLUMN_VELDEF_RADIO, OIFitsConstants.COLUMN_VELDEF_OPTICAL});
    /** PMRA column descriptor */
    private final static ColumnMeta COLUMN_PMRA = new ColumnMeta(OIFitsConstants.COLUMN_PMRA,
            "proper motion in RA", Types.TYPE_DBL, Units.UNIT_DEGREE_PER_YEAR);
    /** PMDEC column descriptor */
    private final static ColumnMeta COLUMN_PMDEC = new ColumnMeta(OIFitsConstants.COLUMN_PMDEC,
            "proper motion in DEC", Types.TYPE_DBL, Units.UNIT_DEGREE_PER_YEAR);
    /** PMRA_ERR column descriptor */
    private final static ColumnMeta COLUMN_PMRA_ERR = new ColumnMeta(OIFitsConstants.COLUMN_PMRA_ERR,
            "error of proper motion in RA", Types.TYPE_DBL, Units.UNIT_DEGREE_PER_YEAR);
    /** PMDEC_ERR column descriptor */
    private final static ColumnMeta COLUMN_PMDEC_ERR = new ColumnMeta(OIFitsConstants.COLUMN_PMDEC_ERR,
            "error of proper motion in DEC", Types.TYPE_DBL, Units.UNIT_DEGREE_PER_YEAR);
    /** PARALLAX column descriptor */
    private final static ColumnMeta COLUMN_PARALLAX = new ColumnMeta(OIFitsConstants.COLUMN_PARALLAX,
            "parallax", Types.TYPE_REAL, Units.UNIT_DEGREE);
    /** PARA_ERR column descriptor */
    private final static ColumnMeta COLUMN_PARA_ERR = new ColumnMeta(OIFitsConstants.COLUMN_PARA_ERR,
            "error in parallax", Types.TYPE_REAL, Units.UNIT_DEGREE);
    /** SPECTYP column descriptor */
    private final static ColumnMeta COLUMN_SPECTYP = new ColumnMeta(OIFitsConstants.COLUMN_SPECTYP,
            "spectral type", Types.TYPE_CHAR, 16);

    /** CATEGORY column descriptor */
    private final static ColumnMeta COLUMN_CATEGORY = new ColumnMeta(OIFitsConstants.COLUMN_CATEGORY,
            "'CAL' or 'SCI'", Types.TYPE_CHAR, 3, true, false, new String[]{OIFitsConstants.COLUMN_CATEGORY_CAL,
                                                                            OIFitsConstants.COLUMN_CATEGORY_SCI}, Units.NO_UNIT, null, null);

    /* members */
 /* cached analyzed data */
    /** mapping of targetId values to row index */
    private final Map<Short, Integer> targetIdToRowIndex = new HashMap<Short, Integer>();
    /** mapping of targetId values to Target */
    private final Map<Short, Target> targetIdToTarget = new HashMap<Short, Target>();
    /** mapping of Target instances (may have duplicates) to targetId values */
    private final Map<Target, Short> targetObjToTargetId = new IdentityHashMap<Target, Short>();

    /**
     * Public OITarget class constructor
     * @param oifitsFile main OifitsFile
     */
    public OITarget(final OIFitsFile oifitsFile) {
        super(oifitsFile);

        // TARGET_ID  column definition
        addColumnMeta(COLUMN_TARGET_ID);

        // TARGET  column definition
        addColumnMeta(COLUMN_TARGET);

        // RAEP0  column definition
        addColumnMeta(COLUMN_RAEP0);

        // DECEP0  column definition
        addColumnMeta(COLUMN_DECEP0);

        // EQUINOX  column definition
        addColumnMeta(COLUMN_EQUINOX);

        // RA_ERR  column definition
        addColumnMeta(COLUMN_RA_ERR);

        // DEC_ERR  column definition
        addColumnMeta(COLUMN_DEC_ERR);

        // SYSVEL  column definition
        addColumnMeta(COLUMN_SYSVEL);

        // VELTYP  column definition
        addColumnMeta(COLUMN_VELTYP);

        // VELDEF  column definition
        addColumnMeta(COLUMN_VELDEF);

        // PMRA  column definition
        addColumnMeta(COLUMN_PMRA);

        // PMDEC  column definition
        addColumnMeta(COLUMN_PMDEC);

        // PMRA_ERR  column definition
        addColumnMeta(COLUMN_PMRA_ERR);

        // PMDEC_ERR  column definition
        addColumnMeta(COLUMN_PMDEC_ERR);

        // PARALLAX  column definition
        addColumnMeta(COLUMN_PARALLAX);

        // PARA_ERR  column definition
        addColumnMeta(COLUMN_PARA_ERR);

        // SPECTYP  column definition
        addColumnMeta(COLUMN_SPECTYP);

        if (oifitsFile.isOIFits2()) {
            // CATEGORY  column definition
            addColumnMeta(COLUMN_CATEGORY);
        }

    }

    /**
     * Public OITarget class constructor to create a new table
     * @param oifitsFile main OifitsFile
     * @param nbRows number of rows i.e. the Fits NAXIS2 keyword value
     */
    public OITarget(final OIFitsFile oifitsFile, final int nbRows) {
        this(oifitsFile);

        this.initializeTable(nbRows);
    }

    /**
     * Public OITarget class constructor to copy the given table (structure only)
     * @param oifitsFile main OifitsFile
     * @param src table to copy
     */
    public OITarget(final OIFitsFile oifitsFile, final OITarget src) {
        this(oifitsFile);

        this.copyTable(src);
    }

    /** 
     * Get number of target identified in this table.
     * @return number of target identified in this table.
     */
    public int getNbTargets() {
        return getNbRows();
    }

    /* --- Columns --- */
    /**
     * Get TARGET_ID column
     * @return the target identifiers
     */
    public short[] getTargetId() {
        return getColumnShort(OIFitsConstants.COLUMN_TARGET_ID);
    }

    /**
     * Get TARGET column.
     * 
     * Note: this column must not store any NULL value
     * 
     * @return the array of TARGET (names) or null if undefined
     */
    public String[] getTarget() {
        return getColumnString(OIFitsConstants.COLUMN_TARGET);
    }

    /**
     * Get RAEP0 column.
     * @return the array of RAEP0.
     */
    public double[] getRaEp0() {
        return getColumnDouble(OIFitsConstants.COLUMN_RAEP0);
    }

    /**
     * Get DECEP0 column.
     * @return the array of DECEP0.
     */
    public double[] getDecEp0() {
        return getColumnDouble(OIFitsConstants.COLUMN_DECEP0);
    }

    /**
     * Get EQUINOX column.
     * @return the array of EQUINOX.
     */
    public float[] getEquinox() {
        return getColumnFloat(OIFitsConstants.COLUMN_EQUINOX);
    }

    /**
     * Get RA_ERR column.
     * @return the array of RA_ERR.
     */
    public double[] getRaErr() {
        return getColumnDouble(OIFitsConstants.COLUMN_RA_ERR);
    }

    /**
     * Get DEC_ERR column.
     * @return the array of DEC_ERR.
     */
    public double[] getDecErr() {
        return getColumnDouble(OIFitsConstants.COLUMN_DEC_ERR);
    }

    /**
     * Get SYSVEL column.
     * @return the array of SYSVEL.
     */
    public double[] getSysVel() {
        return getColumnDouble(OIFitsConstants.COLUMN_SYSVEL);
    }

    /**
     * Get VELTYP column.
     * 
     * Note: this column must not store any NULL value
     * 
     * @return the array of VELTYP
     */
    public String[] getVelTyp() {
        return getColumnString(OIFitsConstants.COLUMN_VELTYP);
    }

    /**
     * Get VELDEF column.
     * 
     * Note: this column must not store any NULL value
     * 
     * @return the array of VELDEF
     */
    public String[] getVelDef() {
        return getColumnString(OIFitsConstants.COLUMN_VELDEF);
    }

    /**
     * Get PMRA column.
     * @return the array of PMRA.
     */
    public double[] getPmRa() {
        return getColumnDouble(OIFitsConstants.COLUMN_PMRA);
    }

    /**
     * Get PMDEC column.
     * @return the array of PMDEC.
     */
    public double[] getPmDec() {
        return getColumnDouble(OIFitsConstants.COLUMN_PMDEC);
    }

    /**
     * Get PMRA_ERR column.
     * @return the array of PMRA_ERR.
     */
    public double[] getPmRaErr() {
        return getColumnDouble(OIFitsConstants.COLUMN_PMRA_ERR);
    }

    /**
     * Get PMDEC_ERR column.
     * @return the array of PMDEC_ERR.
     */
    public double[] getPmDecErr() {
        return getColumnDouble(OIFitsConstants.COLUMN_PMDEC_ERR);
    }

    /**
     * Get PARALLAX column.
     * @return the array of PARALLAX.
     */
    public float[] getParallax() {
        return getColumnFloat(OIFitsConstants.COLUMN_PARALLAX);
    }

    /**
     * Get PARA_ERR column.
     * @return the array of PARA_ERR.
     */
    public float[] getParaErr() {
        return getColumnFloat(OIFitsConstants.COLUMN_PARA_ERR);
    }

    /**
     * Get SPECTYP column.
     * 
     * Note: this column must not store any NULL value
     * 
     * @return the array of SPECTYP
     */
    public String[] getSpecTyp() {
        return getColumnString(OIFitsConstants.COLUMN_SPECTYP);
    }

    /**
     * Get CATEGORY column.
     * 
     * @return the array of CATEGORY or null if undefined
     */
    public String[] getCategory() {
        return getColumnString(OIFitsConstants.COLUMN_CATEGORY);
    }

    /**
     * Get the CATEGORY value at the given index.
     * @param idx index
     * @return the value of CATEGORY or null if undefined
     */
    public String getCategory(int idx) {
        final String[] category = getCategory();
        if (category != null) {
            return category[idx];
        }
        return null;
    }

    /**
     * Set the CATEGORY value at the given index.
     * @param idx index
     * @param value the value of CATEGORY
     */
    public void setCategory(int idx, final String value) {
        final String[] category = getCategory();
        if (category != null) {
            category[idx] = value;
        }
    }

    /* --- Other methods --- */
    /**
     * Do syntactical analysis.
     * @param checker checker component
     */
    @Override
    public void checkSyntax(final OIFitsChecker checker) {
        super.checkSyntax(checker);

        final double[] coorDec = getDecEp0();
        final double[] coorRa = getRaEp0();

        final int len = getNbTargets();
        final short[] targetIds = getTargetId();
        final String[] targetNames = getTarget();

        // TODO: check coordinates (crossmatch ?)
        for (int i = 0; i < len; i++) {

            if ((Double.isNaN(coorRa[i]) || Double.isNaN(coorDec[i])) || OIFitsChecker.isInspectRules()) {
                // rule [OI_TARGET_COORD_EXIST] check if the TARGET RA or DEC value is not undefined
                if (checker != null) {
                    checker.ruleFailed(Rule.OI_TARGET_COORD_EXIST, this, OIFitsConstants.COLUMN_DECEP0).addValueAt(coorDec[i], i);
                }
                if (checker != null) {
                    checker.ruleFailed(Rule.OI_TARGET_COORD_EXIST, this, OIFitsConstants.COLUMN_RAEP0).addValueAt(coorRa[i], i);
                }
            }
            if ((NumberUtils.equals(coorRa[i], 0.0, COORD_ZERO_EPSILON)
                    && NumberUtils.equals(coorDec[i], 0.0, COORD_ZERO_EPSILON)) || OIFitsChecker.isInspectRules()) {
                // note: testing 0.0 here means 0.0 exactly (not a floating-point equality)
                // rule [OI_TARGET_COORD] check if the TARGET RA and DEC values are not 0.0
                if (checker != null) {
                    checker.ruleFailed(Rule.OI_TARGET_COORD, this, OIFitsConstants.COLUMN_RAEP0).addValueAt(coorRa[i], i);
                }
                if (checker != null) {
                    checker.ruleFailed(Rule.OI_TARGET_COORD, this, OIFitsConstants.COLUMN_DECEP0).addValueAt(coorDec[i], i);
                }
            }

            if ((targetNames[i] == null) || OIFitsChecker.isInspectRules()) {
                // rule [OI_TARGET_TARGET] check if the TARGET column values have a not null or empty value
                if (checker != null) {
                    checker.ruleFailed(Rule.OI_TARGET_TARGET, this, OIFitsConstants.COLUMN_TARGET).addValueAt(targetNames[i], i);
                }
            }
            if ((targetNames[i] != null) || OIFitsChecker.isInspectRules()) {
                final short refId = targetIds[i];
                if ((refId < 1) || OIFitsChecker.isInspectRules()) {
                    // rule [OI_TARGET_TARGETID_MIN] check if the TARGET_ID values >= 1
                    if (checker != null) {
                        checker.ruleFailed(Rule.OI_TARGET_TARGETID_MIN, this, OIFitsConstants.COLUMN_TARGET).addValueAt(refId, i);
                    }
                }
                final String refName = targetNames[i];

                for (int j = i + 1; j < len; j++) {
                    if ((refId == targetIds[j]) || OIFitsChecker.isInspectRules()) {
                        // rule [OI_TARGET_TARGETID_UNIQ] check duplicated indexes in the TARGET_ID column of the OI_TARGET table
                        if (checker != null) {
                            checker.ruleFailed(Rule.OI_TARGET_TARGETID_UNIQ, this, OIFitsConstants.COLUMN_TARGET_ID).addValueAtRows(refId, i, j);
                        }
                    }
                    if (refName.equals(targetNames[j]) || OIFitsChecker.isInspectRules()) {
                        // rule [OI_TARGET_TARGET_UNIQ] check duplicated values in the TARGET column of the OI_TARGET table
                        if (checker != null) {
                            checker.ruleFailed(Rule.OI_TARGET_TARGET_UNIQ, this, OIFitsConstants.COLUMN_TARGET).addValueAtRows(refName, i, j);
                        }
                    }
                }
            }

            //TODO CHECK ERR COL ?
        }

        getOIFitsFile().checkCrossReference(this, checker);
    }

    /**
     * Returns a string representation of this table
     * @return a string representation of this table
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(128);

        final short[] targetIds = getTargetId();
        final String[] targets = getTarget();

        final int len = getNbTargets();
        if (len != 0) {
            for (int i = 0; i < len; i++) {
                sb.append("| ").append((targets[i] != null) ? targets[i] : "NULL").append("(").append(targetIds[i]).append(") ");
            }
            sb.deleteCharAt(0);
        }

        return super.toString() + " [ TARGETS[" + sb.toString() + "]]";
    }


    /* --- data analysis --- */
    /**
     * Indicate to clear any cached value (derived column ...)
     */
    @Override
    public void setChanged() {
        super.setChanged();
        targetIdToRowIndex.clear();
        targetIdToTarget.clear();
        targetObjToTargetId.clear();
    }

    Map<Short, Integer> getTargetIdToRowIndex() {
        return targetIdToRowIndex;
    }

    /**
     * Return the row index corresponding to the given targetId or null if missing
     * @param targetId targetId (may be null)
     * @return row index corresponding to the given targetId or null if missing (or null if Analyzer not run)
     */
    public Integer getRowIndex(final Short targetId) {
        return targetIdToRowIndex.get(targetId);
    }

    Map<Short, Target> getTargetIdToTarget() {
        return targetIdToTarget;
    }

    /**
     * Return the Target corresponding to the given targetId or null if missing
     * @param targetId targetId (may be null)
     * @return Target corresponding to the given targetId or null if missing (or null if Analyzer not run)
     */
    public Target getTarget(final Short targetId) {
        return targetIdToTarget.get(targetId);
    }

    Map<Target, Short> getTargetObjToTargetId() {
        return targetObjToTargetId;
    }

    public Set<Target> getTargetSet() {
        return targetObjToTargetId.keySet();
    }

    /**
     * Return the targetId corresponding to the given Target or null if missing
     * @param target Target
     * @return targetId corresponding to the given Target or null if missing (or null if Analyzer not run)
     */
    public Short getTargetId(final Target target) {
        return targetObjToTargetId.get(target);
    }

    Target createTarget(final int idx) {
        if (idx >= 0 && idx < getNbRows()) {
            return new Target(getTarget()[idx], getRaEp0()[idx], getDecEp0()[idx], getEquinox()[idx],
                    getRaErr()[idx], getDecErr()[idx], getSysVel()[idx], getVelTyp()[idx], getVelDef()[idx],
                    getPmRa()[idx], getPmDec()[idx], getPmRaErr()[idx], getPmDecErr()[idx],
                    getParallax()[idx], getParaErr()[idx], getSpecTyp()[idx],
                    getCategory(idx));
        }
        return null;
    }

    public void setTarget(final int idx, final short id, final Target target) {
        if (idx >= 0 && idx < getNbRows()) {
            getTargetId()[idx] = id;
            getTarget()[idx] = target.getTarget();

            getRaEp0()[idx] = target.getRaEp0();
            getDecEp0()[idx] = target.getDecEp0();
            getEquinox()[idx] = target.getEquinox();

            getRaErr()[idx] = target.getRaErr();
            getDecErr()[idx] = target.getDecErr();

            getSysVel()[idx] = target.getSysVel();
            getVelTyp()[idx] = target.getVelTyp();
            getVelDef()[idx] = target.getVelDef();

            getPmRa()[idx] = target.getPmRa();
            getPmDec()[idx] = target.getPmDec();

            getPmRaErr()[idx] = target.getPmRaErr();
            getPmDecErr()[idx] = target.getPmDecErr();

            getParallax()[idx] = target.getParallax();
            getParaErr()[idx] = target.getParaErr();

            getSpecTyp()[idx] = target.getSpecTyp();
            setCategory(idx, target.getCategory());
        }
    }

    /**
     * Return the targetId Matcher corresponding to the given Target UIDs (global) or null if missing
     * @param tm TargetManager instance
     * @param targetUIDs target UIDs (global)
     * @return targetId Matcher corresponding to the given Target UIDs (global) or null if missing
     */
    public final TargetIdMatcher getTargetIdMatcherByUIDs(final TargetManager tm, final List<String> targetUIDs) {
        final List<Target> globalTargets = tm.getGlobalsByUID(targetUIDs);
        if (globalTargets != null) {
            return getTargetIdMatcher(tm, globalTargets);
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
        final Set<Short> matchs = getTargetIds(tm, globalTarget);
        if (matchs != null) {
            return new TargetIdMatcher(matchs);
        }
        return null;
    }

    /**
     * Return the target ids corresponding to the given Targets (global) or null if all missing
     * @param tm TargetManager instance
     * @param globalTarget target (global)
     * @return target ids corresponding to the given Targets (global) or null if all missing
     */
    public final Set<Short> getTargetIds(final TargetManager tm, final Target globalTarget) {
        Set<Short> matchs = null;
        final List<Target> localTargets = tm.getLocals(globalTarget);
        if (localTargets != null) {
            for (Target local : localTargets) {
                final Short id = targetObjToTargetId.get(local);
                if (id != null) {
                    if (matchs == null) {
                        matchs = new HashSet<Short>();
                    }
                    matchs.add(id);
                }
            }
        }
        return matchs;
    }

    /**
     * Return the targetId Matcher corresponding to the given Targets (global) or null if missing
     * @param tm TargetManager instance
     * @param globalTargets targets (global)
     * @return targetId Matcher corresponding to the given Targets (global) or null if missing
     */
    public final TargetIdMatcher getTargetIdMatcher(final TargetManager tm, final List<Target> globalTargets) {
        final Set<Short> matchs = getTargetIds(tm, globalTargets);
        if (matchs != null) {
            return new TargetIdMatcher(matchs);
        }
        return null;
    }

    /**
     * Return the target ids corresponding to the given Targets (global) or null if all missing
     * @param tm TargetManager instance
     * @param globalTargets targets (global)
     * @return target ids corresponding to the given Targets (global) or null if all missing
     */
    public final Set<Short> getTargetIds(final TargetManager tm, final List<Target> globalTargets) {
        Set<Short> matchs = null;
        for (Target globalTarget : globalTargets) {
            final List<Target> localTargets = tm.getLocals(globalTarget);
            if (localTargets != null) {
                for (Target local : localTargets) {
                    final Short id = targetObjToTargetId.get(local);
                    if (id != null) {
                        if (matchs == null) {
                            matchs = new HashSet<Short>();
                        }
                        matchs.add(id);
                    }
                }
            }
        }
        return matchs;
    }
}
