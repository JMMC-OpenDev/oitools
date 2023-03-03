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
package fr.jmmc.oitools.meta;

import fr.jmmc.jmcs.util.NumberUtils;
import fr.jmmc.oitools.OIFitsConstants;
import fr.jmmc.oitools.fits.FitsTable;
import fr.jmmc.oitools.model.ModelBase;
import fr.jmmc.oitools.model.OIFitsChecker;
import fr.jmmc.oitools.model.Rule;
import fr.nom.tam.util.ArrayFuncs;
import java.util.logging.Level;

/**
 * This class describes a FITS column
 * @author bourgesl
 */
public class ColumnMeta extends CellMeta {

    /* members */
    /** Column flag is3D (VisRefMap) */
    final boolean is3D;
    /** Cardinality of column :
     * For a Column, there are two cases :
     * - String (A) : maximum number of characters
     * - Other : dimension of the value (1 = single value, more it is an array)
     */
    private final int repeat;
    /** optional column name storing error values (may be null) */
    private final String errName;
    /** optional column name storing data values (may be null) for the error column */
    private String dataName = null;
    /** optional data range (may be null) */
    private final DataRange dataRange;
    /** optional column alias ie alternate name (may be null) */
    private String alias = null;
    /** optional flag to indicate column values depend on baseline / triplet (staIndex) orientation (false by default) */
    private boolean isOrientationDependent = false;

    /**
     * ColumnMeta class constructor with cardinality of 1 and without unit
     *
     * @param name column name
     * @param desc column descriptive comment
     * @param dataType column data type
     */
    public ColumnMeta(final String name, final String desc, final Types dataType) {
        this(name, desc, dataType, 1, false, false, NO_INT_VALUES, NO_STR_VALUES, Units.NO_UNIT, null, null);
    }

    /**
     * ColumnMeta class constructor with the given cardinality
     *
     * @param name column name
     * @param desc column descriptive comment
     * @param dataType column data type
     * @param repeat column cardinality
     */
    public ColumnMeta(final String name, final String desc, final Types dataType, final int repeat) {
        this(name, desc, dataType, repeat, false, false, NO_INT_VALUES, NO_STR_VALUES, Units.NO_UNIT, null, null);
    }

    /**
     * ColumnMeta class constructor with is optional
     *
     * @param name column name
     * @param desc column descriptive comment
     * @param dataType column data type
     * @param repeat column cardinality
     * @param optional true if column is optional
     */
    public ColumnMeta(final String name, final String desc, final Types dataType, final int repeat, final boolean optional) {
        this(name, desc, dataType, repeat, optional, false, NO_INT_VALUES, NO_STR_VALUES, Units.NO_UNIT, null, null);
    }

    /**
     * ColumnMeta class constructor with isoptinal and 3D
     * @param name column name
     * @param desc column descriptive comment
     * @param dataType column data type
     * @param repeat column cardinality
     * @param optional true if column is optional
     * @param is3D true if column is on 3D
     * @param unit column unit
     */
    public ColumnMeta(String name, String desc, Types dataType, int repeat, boolean optional, boolean is3D, final Units unit) {
        this(name, desc, dataType, repeat, optional, is3D, NO_INT_VALUES, NO_STR_VALUES, unit, null, null);
    }

    /**
     * ColumnMeta class constructor with the given cardinality and unit
     *
     * @param name column name
     * @param desc column descriptive comment
     * @param dataType column data type
     * @param unit column unit
     */
    public ColumnMeta(final String name, final String desc, final Types dataType, final Units unit) {
        this(name, desc, dataType, 1, false, false, NO_INT_VALUES, NO_STR_VALUES, unit, null, null);
    }

    /**
     * ColumnMeta class constructor with the given cardinality and unit
     *
     * @param name column name
     * @param desc column descriptive comment
     * @param dataType column data type
     * @param unit column unit
     * @param dataRange optional data range (may be null)
     */
    public ColumnMeta(final String name, final String desc, final Types dataType, final Units unit, final DataRange dataRange) {
        this(name, desc, dataType, 1, false, false, NO_INT_VALUES, NO_STR_VALUES, unit, null, dataRange);
    }

    /**
     * ColumnMeta class constructor with the given cardinality and unit
     *
     * @param name column name
     * @param desc column descriptive comment
     * @param dataType column data type
     * @param repeat column cardinality
     * @param unit column unit
     */
    public ColumnMeta(final String name, final String desc, final Types dataType, final int repeat, final Units unit) {
        this(name, desc, dataType, repeat, false, false, NO_INT_VALUES, NO_STR_VALUES, unit, null, null);
    }

    /**
     * ColumnMeta class constructor with the given cardinality and unit
     *
     * @param name column name
     * @param desc column descriptive comment
     * @param dataType column data type
     * @param repeat column cardinality
     * @param optional true if column is optional
     * @param is3D true if column is on 3D
     * @param acceptedValues possible values for column
     * @param unit column unit
     * @param errName optional column name storing error values (may be null)
     * @param dataRange optional data range (may be null)
     */
    public ColumnMeta(final String name, final String desc, final Types dataType, final int repeat, final boolean optional, final boolean is3D,
                      final String[] acceptedValues, final Units unit, final String errName, final DataRange dataRange) {
        this(name, desc, dataType, repeat, optional, is3D, NO_INT_VALUES, acceptedValues, unit, errName, dataRange);
    }

    /**
     * Private ColumnMeta class constructor with the given cardinality and string possible values
     *
     * @param name column name
     * @param desc column descriptive comment
     * @param dataType column data type
     * @param repeat column cardinality
     * @param stringAcceptedValues string possible values for column/keyword
     */
    public ColumnMeta(final String name, final String desc, final Types dataType,
                      final int repeat, final String[] stringAcceptedValues) {
        this(name, desc, dataType, repeat, false, false, NO_INT_VALUES, stringAcceptedValues, Units.NO_UNIT, null, null);
    }

    /**
     * Private ColumnMeta class constructor with the given cardinality and string possible values
     *
     * @param name column name
     * @param desc column descriptive comment
     * @param dataType column data type
     * @param repeat column cardinality
     * @param optional true if column is optional
     * @param is3D true if column is on 3D
     * @param intAcceptedValues integer possible values for column/keyword
     * @param stringAcceptedValues string possible values for column/keyword
     * @param unit keyword/column unit
     * @param errName optional column name storing error values (may be null)
     * @param dataRange optional data range (may be null)
     */
    private ColumnMeta(final String name, final String desc, final Types dataType,
                       final int repeat, final boolean optional, final boolean is3D, final short[] intAcceptedValues, final String[] stringAcceptedValues,
                       final Units unit, final String errName, final DataRange dataRange) {
        super(MetaType.COLUMN, name, desc, dataType, optional, intAcceptedValues, stringAcceptedValues, unit);

        this.is3D = is3D;
        this.repeat = repeat;
        this.errName = errName;
        this.dataRange = dataRange;
    }

    /**
     * Return true if the value is multiple (array)
     * @return true if the value is multiple
     */
    public final boolean isArray() {
        return getDataType() != Types.TYPE_CHAR && ((getRepeat() > 1) || this instanceof ArrayColumnMeta);
    }

    /**
     * Return true if the value is multiple (3D array)
     * @return true if the value is multiple
     */
    public final boolean is3D() {
        return is3D;
    }

    /**
     * Return the repeat value i.e. cardinality
     * Can be overriden to represent cross - references
     * @return repeat value i.e. cardinality
     */
    public int getRepeat() {
        return this.repeat;
    }


    /* ---  Error relationships --- */
    /**
     * @return optional column name storing error values (may be null)
     */
    public final String getErrorColumnName() {
        return errName;
    }

    /**
     * @return optional column name storing data values (may be null)
     */
    public String getDataColumnName() {
        return dataName;
    }

    /**
     * Define the optional column name storing data values (may be null) for the error column
     * @param dataName optional column name storing data values (may be null) for the error column
     */
    public void setDataColumnName(final String dataName) {
        this.dataName = dataName;
    }

    /* ---  Data range --- */
    /**
     * Return the optional data range (may be null)
     * @return optional data range (may be null)
     */
    public final DataRange getDataRange() {
        return dataRange;
    }

    /* --- optional Alias --- */
    /**
     * Return the optional column alias
     * @return optional column alias
     */
    public final String getAlias() {
        return alias;
    }

    /**
     * Define the optional column alias
     * @param alias optional column alias
     * @return this
     */
    public final ColumnMeta setAlias(final String alias) {
        this.alias = alias;
        return this; // fluent API
    }

    public final boolean isOrientationDependent() {
        return isOrientationDependent;
    }

    public final ColumnMeta setOrientationDependent(final boolean orientationDependent) {
        this.isOrientationDependent = orientationDependent;
        return this;
    }

    /* ---  checker --- */
    /**
     * Check if the given column value is valid.
     *
     * @param checker checker component
     * @param table fits table
     * @param value column data to check
     * @param nbRows number of rows in the column
     */
    public final void check(final OIFitsChecker checker, final FitsTable table,
                            final Object value, final int nbRows) {
        final String colName = getName();
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "check : {0} = {1}", new Object[]{colName, ArrayFuncs.arrayDescription(value)});
        }

        // Check type and cardinality
        final Class<?> baseClass = ArrayFuncs.getBaseClass(value);
        char columnType = Types.getDataType(baseClass).getRepresentation();

        final int[] dims = ArrayFuncs.getDimensions(value);
        final int ndims = dims.length;

        // check rows
        final int columnRows = dims[0];
        if ((columnRows != nbRows) || OIFitsChecker.isInspectRules()) {
            // rule [GENERIC_COL_NBROWS] check if the column length matches the expected number of rows
            if (checker != null) {
                checker.ruleFailed(Rule.GENERIC_COL_NBROWS, table, colName).addKeywordValue(columnRows, nbRows);
            }
        }

        int columnRepeat;
        if (ndims == 1) {
            columnRepeat = 1;

            if (columnType == Types.TYPE_CHAR.getRepresentation()) {
                final String[] strings = (String[]) value;

                // For Strings, repeat corresponds to the number of characters :
                int max = 0;
                String val;
                for (int i = 0, len = strings.length; i < len; i++) {
                    val = strings[i];
                    if (val == null) {
                        // fix null value by empty value:
                        strings[i] = "";
                    } else if (val.length() > max) {
                        max = val.length();
                    }
                }
                columnRepeat = max;
            }

        } else {
            columnRepeat = dims[1];

            if (ndims > 2 && columnRepeat > 0) {
                // special case for Complex type :
                if (ndims == 3) {
                    if ((dims[2] == 2) && (baseClass == float.class)) {
                        columnType = Types.TYPE_COMPLEX.getRepresentation();
                    } else if ((dims[2] == dims[1]) && (baseClass == boolean.class)) {
                        // square matrix of boolean (visRefMap)
                        columnRepeat *= columnRepeat;
                    } else {
                        logger.log(Level.SEVERE, "unsupported array dimensions : {0}", ArrayFuncs.arrayDescription(value));
                    }
                } else {
                    logger.log(Level.SEVERE, "unsupported array dimensions : {0}", ArrayFuncs.arrayDescription(value));
                }
            }
        }

        // Check type and cardinality
        if (checker != null && !checker.isSkipFormat()) {
            checkColumnFormat(checker, table, columnType, columnRepeat);
        }

        // skip check units as the raw object has not this information.
        // Check values:
        checkValues(checker, table, value, columnRows);
    }

    /**
     * Check the column format
     * @param checker checker component
     * @param table fits table
     * @param columnType colum type (raw data)
     * @param columnRepeat colum repeat (raw data)
     * @return true if the corresponding value must be discarded (ignore) as totally incompatible !
     */
    public final boolean checkColumnFormat(final OIFitsChecker checker, final FitsTable table,
                                           final char columnType, final int columnRepeat) {
        final String colName = getName();
        boolean ignore = false;

        // Note : ColumnMeta.getRepeat() is lazily computed for cross-reference columns
        // see WaveColumnMeta.getRepeat()
        final char descType = this.getType();
        int descRepeat = this.getRepeat();

        if ((descRepeat == 0) || OIFitsChecker.isInspectRules()) {
            // May happen if bad reference (wavelength table):
            // rule [GENERIC_COL_DIM] check if the dimension of column values >= 1
            if (checker != null) {
                checker.ruleFailed(Rule.GENERIC_COL_DIM, table, colName);
            }

            if (columnType != descType) {
                // rule [GENERIC_COL_FORMAT] check if the column format matches the expected format (data type & dimensions)
                if (checker != null) {
                    checker.ruleFailed(Rule.GENERIC_COL_FORMAT, table, colName).addKeywordValue(columnType, descType);
                }
            }
        }
        if ((descRepeat != 0) || OIFitsChecker.isInspectRules()) {
            boolean severe = false;

            if (columnType != descType) {
                severe = true;
                if (columnRepeat != descRepeat) {
                    // incompatible array size = ignore totally values:
                    ignore = true;
                }
            } else if (columnType == Types.TYPE_CHAR.getRepresentation()) {
                // For String values, report only errors when the maximum length is exceeded.
                if (columnRepeat > descRepeat) {
                    // should crop string ?
                    severe = true;
                }
            } else {
                if (is3D()) {
                    // visRefMap square matrix:
                    descRepeat *= descRepeat;
                }
                if (columnRepeat != descRepeat) {
                    severe = true;
                    // incompatible array size = ignore totally values:
                    ignore = true;
                }
            }

            if (severe || OIFitsChecker.isInspectRules()) {
                if (checker != null) {
                    checker.ruleFailed(Rule.GENERIC_COL_FORMAT, table, colName).addKeywordValue(columnRepeat + "" + columnType, descRepeat + "" + descType);
                }
            }
        }
        return ignore;
    }

    /**
     * If any are mentioned, check column values are fair.
     *
     * @param checker checker component
     * @param table table FitsTable
     * @param value column data to check (Not null)
     * @param columnRows number of rows in the given column
     */
    private void checkValues(final OIFitsChecker checker, final FitsTable table,
                             final Object value, final int columnRows) {

        final String colName = getName();

        String acceptedValues = null;
        boolean error;

        final short[] intAcceptedValues = getIntAcceptedValues();

        if (intAcceptedValues.length != 0) {
            // OIData: STA_INDEX (2D but nCols=[1,2 or 3]) or TARGET_ID (1D)

            // Skip checks if the column is missing (from file):
            if ((checker != null) && !checker.hasRule(Rule.GENERIC_COL_MANDATORY, table, colName) || OIFitsChecker.isInspectRules()) {
                final boolean isArray = isArray();

                if (!isArray) {
                    // OIData: TARGET_ID (1D)
                    final short[] sValues = (short[]) value;

                    for (int r = 0; r < columnRows; r++) {
                        final short val = sValues[r];

                        if (!ModelBase.isUndefined(val) || OIFitsChecker.isInspectRules()) {
                            error = true;

                            for (int i = 0, len = intAcceptedValues.length; i < len; i++) {
                                if (val == intAcceptedValues[i]) {
                                    error = false;
                                    break;
                                }
                            }

                            if (error || OIFitsChecker.isInspectRules()) {
                                // rule [GENERIC_COL_VAL_ACCEPTED_INT] check if column values match the 'accepted' values (integer)
                                if (checker != null) {
                                    if (!checker.hasFixRule(Rule.OI_TARGET_TARGETID_MIN) || !OIFitsConstants.COLUMN_TARGET_ID.equals(colName)) {
                                        if (acceptedValues == null) {
                                            acceptedValues = getIntAcceptedValuesAsString();
                                        }
                                        checker.ruleFailed(Rule.GENERIC_COL_VAL_ACCEPTED_INT, table, colName).addValueAt(val, acceptedValues, r);
                                    }
                                    if (OIFitsChecker.FIX_BAD_ACCEPTED_FOR_SINGLE_MATCH && (intAcceptedValues.length == 1)) {
                                        // TODO: use FIX RULE to log change ...
                                        sValues[r] = intAcceptedValues[0];
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // OIData: STA_INDEX (2D but nCols=[1,2 or 3])
                    final short[][] sValues = (short[][]) value;

                    for (int r = 0; r < columnRows; r++) {
                        final short[] values = sValues[r];

                        for (int c = 0, rlen = values.length; c < rlen; c++) {
                            final short val = values[c];

                            if (!ModelBase.isUndefined(val) || OIFitsChecker.isInspectRules()) {
                                error = true;

                                for (int i = 0, len = intAcceptedValues.length; i < len; i++) {
                                    if (val == intAcceptedValues[i]) {
                                        error = false;
                                        break;
                                    }
                                }

                                if (error || OIFitsChecker.isInspectRules()) {
                                    // rule [GENERIC_COL_VAL_ACCEPTED_INT] check if column values match the 'accepted' values (integer)
                                    if (checker != null) {
                                        if (acceptedValues == null) {
                                            acceptedValues = getIntAcceptedValuesAsString();
                                        }
                                        checker.ruleFailed(Rule.GENERIC_COL_VAL_ACCEPTED_INT, table, colName).addColValueAt(val, acceptedValues, r, c);

                                        if (OIFitsChecker.FIX_BAD_ACCEPTED_FOR_SINGLE_MATCH && (intAcceptedValues.length == 1)) {
                                            // TODO: use FIX RULE to log change ...
                                            values[c] = intAcceptedValues[0];
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return;
        }

        final String[] stringAcceptedValues = getStringAcceptedValues();

        if (stringAcceptedValues.length != 0) {
            // OITarget : VELTYP, VELDEF
            // OIInspol: INSNAME ...

            // Skip checks if the column is missing (from file):
            if (checker != null && !checker.hasRule(Rule.GENERIC_COL_MANDATORY, table, colName) || OIFitsChecker.isInspectRules()) {
                final String[] sValues = (String[]) value;

                String val;
                for (int r = 0; r < columnRows; r++) {
                    val = sValues[r];
                    error = true;

                    if (val == null) {
                        val = "";
                    } else {
                        for (int i = 0, len = stringAcceptedValues.length; i < len; i++) {
                            if (val.equals(stringAcceptedValues[i])) {
                                error = false;
                                break;
                            }
                        }
                    }

                    if (error || OIFitsChecker.isInspectRules()) {
                        // rule [GENERIC_COL_VAL_ACCEPTED_STR] check if column values match the 'accepted' values (string)
                        if (checker != null) {
                            if (acceptedValues == null) {
                                acceptedValues = getStringAcceptedValuesAsString();
                            }
                            checker.ruleFailed(Rule.GENERIC_COL_VAL_ACCEPTED_STR, table, colName).addValueAt(val, acceptedValues, r);

                            if (OIFitsChecker.FIX_BAD_ACCEPTED_FOR_SINGLE_MATCH && (stringAcceptedValues.length == 1)) {
                                // TODO: use FIX RULE to log change ...
                                sValues[r] = stringAcceptedValues[0];
                            }
                        }
                    }
                }
            }
            return;
        }

        if (getDataRange() == DataRange.RANGE_POSITIVE_STRICT) {
            final boolean isArray = isArray();

            if (getDataType() == Types.TYPE_REAL) {
                if (!isArray) {
                    // OIData: DIAMETER, EFF_WAVE, EFF_BAND (1D)
                    final float[] fValues = (float[]) value;

                    float val;
                    for (int r = 0; r < columnRows; r++) {
                        val = fValues[r];
                        error = !isPositiveValueValid(val);

                        if (error || OIFitsChecker.isInspectRules()) {
                            // rule [GENERIC_COL_VAL_POSITIVE] check if column values are finite and positive
                            if (checker != null) {
                                checker.ruleFailed(Rule.GENERIC_COL_VAL_POSITIVE, table, colName).addValueAt(val, r);
                            }
                        }
                        return;
                    }
                } else {
                    // OIData: UNUSED (2D)
                    final float[][] fValues = (float[][]) value;

                    float[] values;
                    float val;
                    for (int r = 0; r < columnRows; r++) {
                        values = fValues[r];

                        for (int c = 0, rlen = values.length; c < rlen; c++) {
                            val = values[c];
                            error = !isPositiveValueValid(val);

                            if (error || OIFitsChecker.isInspectRules()) {
                                // rule [GENERIC_COL_VAL_POSITIVE] check if column values are finite and positive
                                if (checker != null) {
                                    checker.ruleFailed(Rule.GENERIC_COL_VAL_POSITIVE, table, colName).addColValueAt(val, r, c);
                                }
                            }
                        }
                        return;
                    }
                }
            }

            if (getDataType() == Types.TYPE_DBL) {
                if (!isArray) {
                    // OIData: FOV, INT_TIME, CORR (1D)
                    final double[] fValues = (double[]) value;

                    double val;
                    for (int r = 0; r < columnRows; r++) {
                        val = fValues[r];
                        error = !isPositiveValueValid(val);

                        if (error || OIFitsChecker.isInspectRules()) {
                            // rule [GENERIC_COL_VAL_POSITIVE] check if column values are finite and positive
                            if (checker != null) {
                                checker.ruleFailed(Rule.GENERIC_COL_VAL_POSITIVE, table, colName).addValueAt(val, r);
                            }
                        }
                        return;
                    }
                } else {
                    // OIData: UNUSED (2D)
                    final double[][] fValues = (double[][]) value;

                    double[] values;
                    double val;
                    for (int r = 0; r < columnRows; r++) {
                        values = fValues[r];

                        for (int c = 0, rlen = values.length; c < rlen; c++) {
                            val = values[c];
                            error = !isPositiveValueValid(val);

                            if (error || OIFitsChecker.isInspectRules()) {
                                // rule [GENERIC_COL_VAL_POSITIVE] check if column values are finite and positive
                                if (checker != null) {
                                    checker.ruleFailed(Rule.GENERIC_COL_VAL_POSITIVE, table, colName).addColValueAt(val, r, c);
                                }
                            }
                        }
                        return;
                    }
                }
            }

            logger.log(Level.SEVERE, "Incompatible data type {0} with positive values for column ''{1}'' ...", new Object[]{getDataType(), colName});
        }
    }

    /**
     * Return true if the given value is valid ie. NaN or is positive or greater than 0.0
     * @param val error value
     * @return true if the given error value is valid
     */
    public static boolean isPositiveValueValid(final float val) {
        return Float.isNaN(val) || NumberUtils.isFinitePositive(val);
    }

    /**
     * Return true if the given value is valid ie. NaN or is positive or greater than 0.0
     * @param val error value
     * @return true if the given error value is valid
     */
    public static boolean isPositiveValueValid(final double val) {
        return Double.isNaN(val) || NumberUtils.isFinitePositive(val);
    }
}
