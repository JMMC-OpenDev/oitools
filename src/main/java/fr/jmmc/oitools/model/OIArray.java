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
import fr.jmmc.oitools.meta.DataRange;
import fr.jmmc.oitools.meta.KeywordMeta;
import fr.jmmc.oitools.meta.Types;
import fr.jmmc.oitools.meta.Units;
import fr.jmmc.oitools.util.MathUtils;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Class for OI_ARRAY table.
 */
public final class OIArray extends OITable {

    /* constants */
    /** minimum earth radius constant (m) */
    public final static double MIN_EARTH_RADIUS = 6350000d;

    /* static descriptors */
    /** ARRNAME keyword descriptor */
    private final static KeywordMeta KEYWORD_ARRNAME = new KeywordMeta(OIFitsConstants.KEYWORD_ARRNAME,
            "array name for cross-referencing", Types.TYPE_CHAR);
    /** FRAME   keyword descriptor */
    private final static KeywordMeta KEYWORD_FRAME = new KeywordMeta(OIFitsConstants.KEYWORD_FRAME,
            "coordinate frame", Types.TYPE_CHAR,
            new String[]{OIFitsConstants.KEYWORD_FRAME_GEOCENTRIC, OIFitsConstants.KEYWORD_FRAME_SKY});
    /** ARRAYX  keyword descriptor */
    private final static KeywordMeta KEYWORD_ARRAY_X = new KeywordMeta(OIFitsConstants.KEYWORD_ARRAY_X,
            "[m] array center X-coordinate", Types.TYPE_DBL, Units.UNIT_METER);
    /** ARRAYY  keyword descriptor */
    private final static KeywordMeta KEYWORD_ARRAY_Y = new KeywordMeta(OIFitsConstants.KEYWORD_ARRAY_Y,
            "[m] array center Y-coordinate", Types.TYPE_DBL, Units.UNIT_METER);
    /** ARRAYZ  keyword descriptor */
    private final static KeywordMeta KEYWORD_ARRAY_Z = new KeywordMeta(OIFitsConstants.KEYWORD_ARRAY_Z,
            "[m] array center Z-coordinate", Types.TYPE_DBL, Units.UNIT_METER);
    /** TEL_NAME column descriptor */
    private final static ColumnMeta COLUMN_TEL_NAME = new ColumnMeta(OIFitsConstants.COLUMN_TEL_NAME,
            "telescope name", Types.TYPE_CHAR, 16);
    /** STA_NAME column descriptor */
    private final static ColumnMeta COLUMN_STA_NAME = new ColumnMeta(OIFitsConstants.COLUMN_STA_NAME,
            "station name", Types.TYPE_CHAR, 16);
    /** STA_INDEX column descriptor */
    private final static ColumnMeta COLUMN_STA_INDEX = new ColumnMeta(OIFitsConstants.COLUMN_STA_INDEX,
            "station index", Types.TYPE_SHORT);
    /** DIAMETER column descriptor */
    private final static ColumnMeta COLUMN_DIAMETER = new ColumnMeta(OIFitsConstants.COLUMN_DIAMETER,
            "element diameter", Types.TYPE_REAL, Units.UNIT_METER, DataRange.RANGE_POSITIVE_STRICT);
    /** STAXYZ column descriptor */
    private final static ColumnMeta COLUMN_STA_XYZ = new ColumnMeta(OIFitsConstants.COLUMN_STA_XYZ,
            "station coordinates relative to array center", Types.TYPE_DBL, 3, Units.UNIT_METER);

    /** FOV column descriptor */
    public final static ColumnMeta COLUMN_FOV = new ColumnMeta(OIFitsConstants.COLUMN_FOV,
            "Photometric field of view (arcsee)", Types.TYPE_DBL, 1, false, false, Units.UNIT_ARCSEC);
    /** FOV_TYPE column descriptor */
    public final static ColumnMeta COLUMN_FOVTYPE = new ColumnMeta(OIFitsConstants.COLUMN_FOVTYPE,
            "Model for FOV: 'FWHM' or 'RADIUS'", Types.TYPE_CHAR, 6, false, false, new String[]{OIFitsConstants.COLUMN_FOVTYPE_FWHM,
                OIFitsConstants.COLUMN_FOVTYPE_RADIUS}, Units.NO_UNIT, null, null);

    /* members */
    /** cached analyzed data */
    /** mapping of staIndex values to row index */
    private final Map<Short, Integer> staIndexToRowIndex = new HashMap<Short, Integer>();
    /** cached StaNames corresponding to given OIData StaIndex arrays */
    private final Map<short[], String> staIndexesToStaNames = new IdentityHashMap<short[], String>();

    /**
     * Public OIArray class constructor
     * @param oifitsFile main OifitsFile
     */
    public OIArray(final OIFitsFile oifitsFile) {
        super(oifitsFile);

        // ARRNAME  keyword definition
        addKeywordMeta(KEYWORD_ARRNAME);
        // FRAME  keyword definition
        addKeywordMeta(KEYWORD_FRAME);

        // ARRAYX  keyword definition
        addKeywordMeta(KEYWORD_ARRAY_X);

        // ARRAYY  keyword definition
        addKeywordMeta(KEYWORD_ARRAY_Y);

        // ARRAYZ  keyword definition
        addKeywordMeta(KEYWORD_ARRAY_Z);

        // TEL_NAME  column definition
        addColumnMeta(COLUMN_TEL_NAME);

        // STA_NAME  column definition
        addColumnMeta(COLUMN_STA_NAME);

        // STA_INDEX  column definition
        addColumnMeta(COLUMN_STA_INDEX);

        // DIAMETER  column definition
        addColumnMeta(COLUMN_DIAMETER);

        // STAXYZ  column definition
        addColumnMeta(COLUMN_STA_XYZ);

        if (oifitsFile.isOIFits2()) {
            // FOV  column definition
            addColumnMeta(COLUMN_FOV);

            // FOVTYPE  column definition
            addColumnMeta(COLUMN_FOVTYPE);
        }
    }

    /**
     * Public OIArray class constructor to create a new table
     * @param oifitsFile main OifitsFile
     * @param nbRows number of rows i.e. the Fits NAXIS2 keyword value
     */
    public OIArray(final OIFitsFile oifitsFile, final int nbRows) {
        this(oifitsFile);

        this.initializeTable(nbRows);
    }

    /* --- Keywords --- */
    /**
     * Get the value of ARRNAME keyword
     * @return the value of ARRNAME keyword
     */
    public String getArrName() {
        return getKeyword(OIFitsConstants.KEYWORD_ARRNAME);
    }

    /**
     * Define the ARRNAME keyword value
     * WARNING: should not be used to modify an existing table (cross-references)
     * @param arrName value of ARRNAME keyword
     */
    public void setArrName(final String arrName) {
        setKeyword(OIFitsConstants.KEYWORD_ARRNAME, arrName);
    }

    /**
     * Get the value of FRAME keyword
     * @return the value of FRAME keyword
     */
    public String getFrame() {
        return getKeyword(OIFitsConstants.KEYWORD_FRAME);
    }

    /**
     * Define the FRAME keyword value
     * @param frame value of FRAME keyword
     */
    public void setFrame(final String frame) {
        setKeyword(OIFitsConstants.KEYWORD_FRAME, frame);
    }

    /**
     * Get the value of ARRAYX, ARRAYY, ARRAYZ keywords
     * @return the value of ARRAYX, ARRAYY, ARRAYZ keywords
     */
    public double[] getArrayXYZ() {
        return new double[]{
            getKeywordDouble(OIFitsConstants.KEYWORD_ARRAY_X),
            getKeywordDouble(OIFitsConstants.KEYWORD_ARRAY_Y),
            getKeywordDouble(OIFitsConstants.KEYWORD_ARRAY_Z)};
    }

    /**
     * Define the value of ARRAYX, ARRAYY, ARRAYZ keywords
     * @param x value of ARRAYX
     * @param y value of ARRAYY
     * @param z value of ARRAYZ
     */
    public void setArrayXYZ(final double x, final double y, final double z) {
        setKeywordDouble(OIFitsConstants.KEYWORD_ARRAY_X, x);
        setKeywordDouble(OIFitsConstants.KEYWORD_ARRAY_Y, y);
        setKeywordDouble(OIFitsConstants.KEYWORD_ARRAY_Z, z);
    }

    /* --- Columns --- */
    /**
     * Get TEL_NAME column.
     * 
     * Note: this column must not store any NULL value
     * 
     * @return the array of TEL_NAME
     */
    public String[] getTelName() {
        return getColumnString(OIFitsConstants.COLUMN_TEL_NAME);
    }

    /**
     * Set the TEL_NAME value at the given row index.
     * 
     * Note: no array bounds check
     * 
     * @param row the row index
     * @param value the value of TEL_NAME
     */
    public void setTelName(final int row, final String value) {
        getTelName()[row] = value;
    }

    /**
     * Get STA_NAME column.
     * 
     * Note: this column must not store any NULL value
     * 
     * @return the array of STA_NAME or null if undefined
     */
    public String[] getStaName() {
        return getColumnString(OIFitsConstants.COLUMN_STA_NAME);
    }

    /**
     * Set the STA_NAME value at the given row index.
     * 
     * Note: no array bounds check
     * 
     * @param row the row index
     * @param value the value of SAT_NAME
     */
    public void setStaName(final int row, final String value) {
        getStaName()[row] = value;
    }

    /**
     * Return the STA_INDEX column.
     * @return the STA_INDEX column i.e. an array containing station indexes.
     */
    public short[] getStaIndex() {
        return getColumnShort(OIFitsConstants.COLUMN_STA_INDEX);
    }

    /**
     * Set the STA_INDEX value at the given row index.
     * 
     * Note: no array bounds check
     * 
     * @param row the row index
     * @param value the value of SAT_INDEX
     */
    public void setStaIndex(final int row, final short value) {
        getStaIndex()[row] = value;
    }

    /**
     * Set the STA_INDEX value at the given row index.
     * 
     * Note: no array bounds check
     * 
     * @param row the row index
     * @param iValue the value of SAT_INDEX
     */
    public void setStaIndex(final int row, final int iValue) {
        setStaIndex(row, toShort(iValue));
    }

    /**
     * Get DIAMETER column.
     * @return the array of DIAMETER.
     */
    public float[] getDiameter() {
        return getColumnFloat(OIFitsConstants.COLUMN_DIAMETER);
    }

    /**
     * Set the DIAMETER value at the given row index.
     * 
     * Note: no array bounds check
     * 
     * @param row the row index
     * @param value the value of DIAMETER
     */
    public void setDiameter(final int row, final float value) {
        getDiameter()[row] = value;
    }

    /**
     * Get STAXYZ column.
     * @return the array of STAXYZ.
     */
    public double[][] getStaXYZ() {
        return this.getColumnDoubles(OIFitsConstants.COLUMN_STA_XYZ);
    }

    /**
     * Set the STAXYZ value at the given row index.
     * 
     * Note: no array bounds check
     * 
     * @param row the row index
     * @param x value 1 of SATXYZ
     * @param y value 2 of SATXYZ
     * @param z value 3 of SATXYZ
     */
    public void setStaXYZ(final int row, final double x, final double y, final double z) {
        final double[] staXYZ = getStaXYZ()[row];
        staXYZ[0] = x;
        staXYZ[1] = y;
        staXYZ[2] = z;
    }

    /**
     * Get FOV column.
     * @return the array of FOV.
     */
    public double[] getFov() {
        return getColumnDouble(OIFitsConstants.COLUMN_FOV);
    }

    /**
     * Set the FOV value at the given row index.
     * 
     * @param row the row index
     * @param value the value of FOV
     */
    public void setFov(final int row, final double value) {
        getFov()[row] = value;
    }

    /**
     * Get FOVTYPE column.
     * @return the array of FOVTYPE.
     */
    public String[] getFovType() {
        return getColumnString(OIFitsConstants.COLUMN_FOVTYPE);
    }

    /**
     * Set the FOVTYPE value at the given row index.
     * 
     * @param row the row index
     * @param value the value of FOVTYPE
     */
    public void setFovType(final int row, final String value) {
        getFovType()[row] = value;
    }

    /* --- Other methods --- */
    /**
     * Returns a string representation of this table
     * @return a string representation of this table
     */
    @Override
    public String toString() {
        return super.toString() + " [ ARRNAME=" + getArrName() + " | " + getNbRows() + " telescopes ]";
    }

    /**
     * Do syntactical analysis.
     * @param checker checker component
     */
    @Override
    public void checkSyntax(final OIFitsChecker checker) {
        super.checkSyntax(checker);

        // rule [OI_ARRAY_ARRNAME] check the ARRNAME keyword has a not null or empty value
        if ((getArrName() != null && getArrName().length() == 0) || OIFitsChecker.isInspectRules()) {
            checker.ruleFailed(Rule.OI_ARRAY_ARRNAME, this, OIFitsConstants.KEYWORD_ARRNAME);
        }

        if (OIFitsConstants.KEYWORD_FRAME_GEOCENTRIC.equalsIgnoreCase(getFrame()) || OIFitsChecker.isInspectRules()) {
            final double[] arrayXYZ = getArrayXYZ();

            // ensure coordinates != 0 (not undefined; expected correctly set)
            final double norm = MathUtils.carthesianNorm(arrayXYZ[0], arrayXYZ[1], arrayXYZ[2]);
            // rule [OI_ARRAY_XYZ] check if the ARRAY_XYZ keyword values corresponds to a proper coordinate on earth
            if ((Double.isNaN(norm) || norm <= MIN_EARTH_RADIUS) || OIFitsChecker.isInspectRules()) {
                checker.ruleFailed(Rule.OI_ARRAY_XYZ, this, OIFitsConstants.KEYWORD_ARRAY_X).addKeywordValue(arrayXYZ[0]);
                checker.ruleFailed(Rule.OI_ARRAY_XYZ, this, OIFitsConstants.KEYWORD_ARRAY_Y).addKeywordValue(arrayXYZ[1]);
                checker.ruleFailed(Rule.OI_ARRAY_XYZ, this, OIFitsConstants.KEYWORD_ARRAY_Z).addKeywordValue(arrayXYZ[2]);

                // Fix known interferometer (from ASPRO2 conf 2017.7):
                if (getArrName() != null) {
                    // rule [OI_ARRAY_XYZ_FIX] fix the ARRAY_XYZ keyword values (to VLTI or CHARA according to the ARRNAME keyword) when the ARRAY_XYZ keyword values are incorrect
                    final String fixed;
                    if (getArrName().startsWith("VLTI")) {
                        fixed = "VLTI";
                        setArrayXYZ(1942014.1545180853, -5455311.818167002, -2654530.4375114734);
                    } else if (getArrName().startsWith("CHARA")) {
                        fixed = "CHARA";
                        setArrayXYZ(-2476998.047780274, -4647390.089884061, 3582240.6122966344);
                    } else {
                        // other interferometers ?
                        fixed = null;
                    }
                    if (fixed != null || OIFitsChecker.isInspectRules()) {
                        checker.ruleFailed(Rule.OI_ARRAY_XYZ_FIX, this).addFixedValue(fixed);
                    }
                }
            }
        }

        final short[] staIndexes = getStaIndex();
        final String[] staNames = getStaName();

        /* TODO: avoid redudant messages */
        for (int i = 0; i < staIndexes.length; i++) {
            if (staNames[i] == null || OIFitsChecker.isInspectRules()) {
                // rule [OI_ARRAY_STA_NAME] check if the STA_NAME column values have a not null or empty value
                checker.ruleFailed(Rule.OI_ARRAY_STA_NAME, this, OIFitsConstants.COLUMN_STA_NAME).addValueAt(staNames[i], i);
            }
            if (staNames[i] != null || OIFitsChecker.isInspectRules()) {
                final short refId = staIndexes[i];
                final String refName = staNames[i];
                // rule [OI_ARRAY_STA_INDEX_MIN] check if the STA_INDEX values >= 1
                if (refId < 1 || OIFitsChecker.isInspectRules()) {
                    checker.ruleFailed(Rule.OI_ARRAY_STA_INDEX_MIN, this, OIFitsConstants.COLUMN_STA_INDEX).addValueAt(refId, i);
                }
                for (int j = i + 1; j < staIndexes.length; j++) {
                    // rule [OI_ARRAY_STA_INDEX_UNIQ] check duplicated indexes in the STA_INDEX column of the OI_ARRAY table
                    if (refId == staIndexes[j] || OIFitsChecker.isInspectRules()) {
                        checker.ruleFailed(Rule.OI_ARRAY_STA_INDEX_UNIQ, this, OIFitsConstants.COLUMN_STA_INDEX).addValueAtRows(refId, i, j);
                    }
                    // rule [OI_ARRAY_STA_NAME_UNIQ] check duplicated values in the STA_NAME column of the OI_ARRAY table
                    if (refName.equals(staNames[j]) || OIFitsChecker.isInspectRules()) {
                        checker.ruleFailed(Rule.OI_ARRAY_STA_NAME_UNIQ, this, OIFitsConstants.COLUMN_STA_NAME).addValueAtRows(refName, i, j);
                    }
                }
            }
        }

        getOIFitsFile().checkCrossReference(this, checker);
    }

    /* --- data analysis --- */
    /**
     * Indicate to clear any cached value (derived column ...)
     */
    @Override
    public void setChanged() {
        super.setChanged();
        staIndexToRowIndex.clear();
        staIndexesToStaNames.clear();
    }

    /**
     * Mapping of staIndex values to row index
     * @return staIndexToRowIndex
     */
    Map<Short, Integer> getStaIndexToRowIndex() {
        return staIndexToRowIndex;
    }

    /**
     * Return the row index corresponding to the given staIndex or null if missing
     * @param staIndex staIndex (may be null)
     * @return row index corresponding to the given staIndex or null if missing
     */
    public Integer getRowIndex(final Short staIndex) {
        return getStaIndexToRowIndex().get(staIndex);
    }

    /**
     * Cached StaNames corresponding to given OIData StaIndex arrays
     * @param staIndexes staIndexes table
     * @return label
     */
    public String getStaNames(final short[] staIndexes) {
        if (staIndexes == null) {
            return UNDEFINED_STRING;
        }
        // warning: identity hashcode so use carefully using distinct array instances:
        String label = staIndexesToStaNames.get(staIndexes);

        if (label == null) {
            final StringBuilder sb = new StringBuilder(32);

            final String[] staNames = getStaName();

            Integer i;
            for (short staIndex : staIndexes) {
                i = getRowIndex(Short.valueOf(staIndex));

                if (i == null) {
                    sb.append(staIndex);
                } else {
                    sb.append(staNames[i]);
                }
                sb.append('-');
            }
            sb.setLength(sb.length() - 1);

            label = sb.toString();

            staIndexesToStaNames.put(staIndexes, label);
        }
        return label;
    }
}
/*___oOo___*/
