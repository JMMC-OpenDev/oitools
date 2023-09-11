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
package fr.jmmc.oitools.fits;

import fr.jmmc.oitools.OIFitsConstants;
import fr.jmmc.oitools.meta.ColumnMeta;
import fr.jmmc.oitools.meta.KeywordMeta;
import fr.jmmc.oitools.meta.Types;
import fr.jmmc.oitools.meta.Units;
import fr.jmmc.oitools.meta.WaveColumnMeta;
import fr.jmmc.oitools.model.ModelVisitor;
import fr.jmmc.oitools.model.OIData;
import fr.jmmc.oitools.model.OIFitsChecker;
import fr.jmmc.oitools.model.OITable;
import fr.jmmc.oitools.model.Rule;
import fr.jmmc.oitools.model.range.Range;
import fr.nom.tam.util.ArrayFuncs;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

/**
 * Generic FITS table (column metadata and data)
 */
public abstract class FitsTable extends FitsHDU {

    /* static descriptors */
    /**
     * NAXIS2 keyword descriptor
     */
    private final static KeywordMeta KEYWORD_NAXIS2 = new KeywordMeta(FitsConstants.KEYWORD_NAXIS2,
            "number of table rows", Types.TYPE_INT);
    /**
     * EXTVER keyword descriptor
     */
    private final static KeywordMeta KEYWORD_EXTVER = new KeywordMeta(FitsConstants.KEYWORD_EXT_VER,
            "extension version", Types.TYPE_INT, Units.NO_UNIT, true);
    /* descriptors */
    /**
     * Map storing column definitions ordered according to OIFits specification
     */
    private final Map<String, ColumnMeta> columnsDesc = new LinkedHashMap<String, ColumnMeta>();
    /**
     * Map storing derived column definitions
     */
    private Map<String, ColumnMeta> columnsDerivedDesc = null;
    /**
     * Map storing column aliases to column names
     */
    private final Map<String, String> columnsAliases = new HashMap<String, String>();
    /* data */
    /**
     * Map storing column values
     */
    private final Map<String, Object> columnsValue = new HashMap<String, Object>();
    /* cached computed data */
    /**
     * Map storing computed values derived from this data table or related
     * tables
     */
    private Map<String, Object> columnsDerivedValue = null;
    /**
     * Map storing min/max computed values derived from this column
     */
    private Map<String, Object> columnsRangeValue = null;

    /**
     * Protected FitsTable class constructor
     */
    protected FitsTable() {
        super();

        // NAXIS2    keyword definition
        addKeywordMeta(KEYWORD_NAXIS2);

        // EXTVER    keyword definition
        addKeywordMeta(KEYWORD_EXTVER);
    }

    /**
     * Initialize the table with minimal keywords and empty columns
     *
     * @param nbRows number of rows i.e. the Fits NAXIS2 keyword value
     * @throws IllegalArgumentException if the number of rows is less than 1
     */
    protected final void initializeTable(final int nbRows) throws IllegalArgumentException {
        if (nbRows < 1) {
            throw new IllegalArgumentException("Invalid number of rows : the table must have at least 1 row !");
        }

        this.setNbRows(nbRows);

        for (ColumnMeta column : getColumnDescCollection()) {
            setColumnValue(column.getName(), createColumnArray(column, nbRows));
        }
    }

    /**
     * Copy method for the given table (keyword values, header cards and column values)
     * @param src table to copy
     */
    protected void copyTable(final FitsTable src) throws IllegalArgumentException {
        // Copy keyword values and header cards:
        super.copyHdu(src);

        // Copy column values:
        final int nRows = getNbRows();

        for (ColumnMeta column : getColumnDescCollection()) {
            final String columnName = column.getName();
            Object columnValue = src.getColumnValue(columnName);

            if (columnValue == null && !column.isOptional()) {
                columnValue = createColumnArray(column, nRows);
            }

            // Copy custom units:
            if (column.isCustomUnits()) {
                final ColumnMeta srcColumn = src.getColumnDesc(columnName);
                if (srcColumn != null && srcColumn.isCustomUnits()) {
                    column.getCustomUnits().set(srcColumn.getCustomUnits());
                }
            }

            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "COLUMN {0} = ''{1}''", new Object[]{columnName, columnValue});
            }
            setColumnValue(columnName, columnValue);
        }
        // ignore derived columns (defined by constructor)
        // ignore cached data
    }

    /**
     * Resize the table ie column arrays + fix the Fits NAXIS2 keyword value
     *
     * @param nbKeepRows number of rows to keep i.e. the Fits NAXIS2 keyword value
     * @param maskRows bit set indicating which rows to keep (true means keep row)
     * @throws IllegalArgumentException if the number of rows is less than 1
     */
    public final void resizeTable(final int nbKeepRows, final BitSet maskRows) throws IllegalArgumentException {
        resizeTable(nbKeepRows, maskRows, null);
    }

    /**
     * Resize the table ie column arrays + fix the Fits NAXIS2 keyword value
     *
     * @param nbKeepRows number of rows to keep i.e. the Fits NAXIS2 keyword value
     * @param maskRows bit set indicating which rows to keep (true means keep row)
     * @param maskWavelengths bit set indicating which wavelength to keep (true means keep index)
     * @throws IllegalArgumentException if the number of rows is less than 1
     */
    public final void resizeTable(final int nbKeepRows, final BitSet maskRows, final BitSet maskWavelengths) throws IllegalArgumentException {
        final int nbRows = getNbRows();
        if ((nbKeepRows == nbRows) && (maskWavelengths == null)) {
            return;
        }
        if (nbKeepRows < 1) {
            throw new IllegalArgumentException("Invalid number of rows : the table must have at least 1 row !");
        }
        final int nbKeepWl = (maskWavelengths != null) ? maskWavelengths.cardinality() : -1;

        // Resize column values:
        for (ColumnMeta column : getColumnDescCollection()) {
            final String columnName = column.getName();
            final Object columnValueOriginal = getColumnValue(columnName);

            // ignore optional columns (null):
            if (columnValueOriginal != null) {
                final int[] dims = getColumnArrayDims(column, nbKeepRows, nbKeepWl);
                final Object columnValue = createColumnArray(column, dims);

                // copy data (may filter wavelengths)
                filterColumnArray(columnName, columnValueOriginal, columnValue, (dims.length == 1), maskRows, nbKeepWl, maskWavelengths);

                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, "COLUMN {0} = ''{1}''", new Object[]{columnName, columnValue});
                }
                setColumnValue(columnName, columnValue);
            }
        }

        this.setNbRows(nbKeepRows);

        // reset cached analyzed data:
        this.setChanged();
    }

    /**
     * Create a new column arrays according to their format and the given number
     * of rows (NAXIS2)
     *
     * @param column meta column descriptor
     * @param nbRows number of rows i.e. the Fits NAXIS2 keyword value
     * @return new column arrays
     */
    public Object createColumnArray(final ColumnMeta column, final int nbRows) {
        return createColumnArray(column, getColumnArrayDims(column, nbRows, -1));
    }

    protected static Object createColumnArray(final ColumnMeta column, final int[] dims) {
        // base class :
        final Class<?> clazz = column.getBaseClass();

        Object value = ArrayFuncs.newInstance(clazz, dims); // array value

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "column array = {0}", ArrayFuncs.arrayDescription(value));
        }

        // Ensure arrays are initialized with undefined values:
        fillUndefinedArrays(value, dims, 0);

        return value;
    }

    protected static int[] getColumnArrayDims(final ColumnMeta column, final int nbRows, final int nbWl) {
        final String name = column.getName();
        final Types type = column.getDataType(); // data type

        // fixed array size:
        final int repeat = ((nbWl != -1) && column instanceof WaveColumnMeta) ? nbWl : column.getRepeat();

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "COLUMN [{0}] [{1}{2}]", new Object[]{name, repeat, column.getType()});
        }

        // extract dimensions :
        int ndims = 1; // number of dimensions
        if (column.isArray()) {
            ndims++;
            if (column.is3D()) {
                ndims++;
            }
        }
        if (type == Types.TYPE_COMPLEX) {
            ndims++;
        }

        final int[] dims = new int[ndims];
        ndims = 0;
        dims[ndims++] = nbRows;

        if (column.isArray()) {
            dims[ndims++] = repeat;
            if (column.is3D()) {
                // hypothesis: square matrix M[ n x n ]
                dims[ndims++] = repeat;
            }
        }
        if (type == Types.TYPE_COMPLEX) {
            dims[ndims] = 2; // 2 values (re, im)
        }

        return dims;
    }

    /**
     * Fill the given multi-d array with undefined values (NaN, -32768, false or
     * "").
     *
     * @param output The multi-dimensional array to be filled.
     * @param dimens array of output dimensions.
     * @param index the index in dimens (current level)
     */
    protected static void fillUndefinedArrays(final Object output, final int[] dimens, final int index) {
        final int xindex = index + 1;

        if (xindex == dimens.length) {
            if (output instanceof double[]) {
                /* double data type */
                Arrays.fill((double[]) output, UNDEFINED_DBL);
            } else if (output instanceof float[]) {
                /* real data type or complex data type */
                Arrays.fill((float[]) output, UNDEFINED_FLOAT);
            } else if (output instanceof short[]) {
                /* integer data type */
                Arrays.fill((short[]) output, UNDEFINED_SHORT);
            } else if (output instanceof int[]) {
                /* integer data type */
                Arrays.fill((int[]) output, UNDEFINED_INT);
            } else if (output instanceof boolean[]) {
                /* logical data type */
                Arrays.fill((boolean[]) output, false);
            } else if (output instanceof String[]) {
                /* character/date data type */
                Arrays.fill((String[]) output, UNDEFINED_STRING);
            } else {
                logger.log(Level.INFO, "fillUndefinedArrays: Unsupported array type: {0}",
                        (output != null) ? output.getClass() : null);
            }
        } else {
            final Object[] oo = (Object[]) output;
            for (int i = 0, len = dimens[index]; i < len; i++) {
                fillUndefinedArrays(oo[i], dimens, xindex);
            }
        }
    }

    protected static void filterColumnArray(final String columnName, final Object input, final Object output, final boolean is1D,
                                            final BitSet keepMaskRows, final int nbKeepWl, final BitSet keepMaskWavelengths) {
        // no bound checks:
        if (is1D) {
            filterColumnArray1D(columnName, input, output, keepMaskRows);
        } else {
            final Object[] os = (Object[]) input;
            final Object[] oo = (Object[]) output;

            for (int i = keepMaskRows.nextSetBit(0), j = 0, len = oo.length; i >= 0 && j < len; i = keepMaskRows.nextSetBit(i + 1), j++) {
                // Array copy needed to ensure deep copy (and not mixing sub-arrays):
                filterColumnArray(columnName, os[i], oo[j], nbKeepWl, keepMaskWavelengths);
            }
        }
    }

    private static void filterColumnArray(final String columnName, final Object input, final Object output,
                                          final int nbKeepWl, final BitSet keepMaskWavelengths) {

        final String oname = input.getClass().getName();
        final String cname = output.getClass().getName();

        if (!oname.equals(cname)) {
            return;
        }

        if (oname.charAt(0) != '[') {
            return;
        }

        if (oname.charAt(1) == '[') {
            // N dimensions
            final Object[] os = (Object[]) input;
            final Object[] oo = (Object[]) output;

            if (os.length == oo.length) {
                // dimensions matches:
                for (int i = 0; i < os.length; i += 1) {
                    filterColumnArray(columnName, os[i], oo[i], nbKeepWl, keepMaskWavelengths);
                }
            } else {
                if (keepMaskWavelengths == null) {
                    logger.log(Level.WARNING, "filterColumnArray[{0}] invalid dimensions: {1} != {2}", new Object[]{columnName, os.length, oo.length});
                } else if (nbKeepWl != oo.length) {
                    logger.log(Level.WARNING, "filterColumnArray[{0}] invalid dimensions: {1} != {2}", new Object[]{columnName, nbKeepWl, oo.length});
                } else {
                    // dimensions matches:
                    filterColumnArray(columnName, input, output, false, keepMaskWavelengths, nbKeepWl, keepMaskWavelengths);
                }
            }
        } else {
            // 1 dimension:
            final int oLen = Array.getLength(input);
            final int cLen = Array.getLength(output);

            if (oLen == cLen) {
                // dimensions matches:
                System.arraycopy(input, 0, output, 0, oLen);
            } else {
                if (keepMaskWavelengths == null) {
                    logger.log(Level.WARNING, "filterColumnArray[{0}] invalid dimensions: {1} != {2}", new Object[]{columnName, oLen, cLen});
                } else if (nbKeepWl != cLen) {
                    logger.log(Level.WARNING, "filterColumnArray[{0}] invalid dimensions: {1} != {2}", new Object[]{columnName, nbKeepWl, cLen});
                } else {
                    // dimensions matches:
                    filterColumnArray1D(columnName, input, output, keepMaskWavelengths);
                }
            }
        }
    }

    protected static void filterColumnArray1D(final String columnName, final Object input, final Object output, final BitSet keepMaskRows) {
        // no bound checks:
        if (output instanceof double[]) {
            /* double data type */
            final double[] os = (double[]) input;
            final double[] oo = (double[]) output;

            for (int i = keepMaskRows.nextSetBit(0), j = 0, len = oo.length; i >= 0 && j < len; i = keepMaskRows.nextSetBit(i + 1), j++) {
                oo[j] = os[i];
            }
        } else if (output instanceof float[]) {
            /* real data type */
            final float[] os = (float[]) input;
            final float[] oo = (float[]) output;

            for (int i = keepMaskRows.nextSetBit(0), j = 0, len = oo.length; i >= 0 && j < len; i = keepMaskRows.nextSetBit(i + 1), j++) {
                oo[j] = os[i];
            }
        } else if (output instanceof short[]) {
            /* integer data type */
            final short[] os = (short[]) input;
            final short[] oo = (short[]) output;

            for (int i = keepMaskRows.nextSetBit(0), j = 0, len = oo.length; i >= 0 && j < len; i = keepMaskRows.nextSetBit(i + 1), j++) {
                oo[j] = os[i];
            }
        } else if (output instanceof int[]) {
            /* integer data type */
            final int[] os = (int[]) input;
            final int[] oo = (int[]) output;

            for (int i = keepMaskRows.nextSetBit(0), j = 0, len = oo.length; i >= 0 && j < len; i = keepMaskRows.nextSetBit(i + 1), j++) {
                oo[j] = os[i];
            }
        } else if (output instanceof boolean[]) {
            /* logical data type */
            final boolean[] os = (boolean[]) input;
            final boolean[] oo = (boolean[]) output;

            for (int i = keepMaskRows.nextSetBit(0), j = 0, len = oo.length; i >= 0 && j < len; i = keepMaskRows.nextSetBit(i + 1), j++) {
                oo[j] = os[i];
            }
        } else if (output instanceof String[]) {
            /* character/date data type */
            final String[] os = (String[]) input;
            final String[] oo = (String[]) output;

            for (int i = keepMaskRows.nextSetBit(0), j = 0, len = oo.length; i >= 0 && j < len; i = keepMaskRows.nextSetBit(i + 1), j++) {
                oo[j] = os[i];
            }
        } else {
            logger.log(Level.INFO, "filterColumnArray[{0}]: Unsupported array type: {1}",
                    new Object[]{columnName, (output != null) ? output.getClass() : null});
        }
    }

    /**
     * Indicate to clear any cached value (derived column ...)
     */
    public void setChanged() {
        clearColumnsDerivedValue();
        clearColumnsRangeValue();
    }

    /**
     * Implements the Visitor pattern
     *
     * @param visitor visitor implementation
     */
    @Override
    public final void accept(final ModelVisitor visitor) {
        // TODO fix implementation of other children Tables
        if (this instanceof OITable) {
            visitor.visit((OITable) this);
        }
    }

    /*
     * --- Column descriptors --------------------------------------------------
     */
    /**
     * Return the Map storing column definitions
     *
     * @return Map storing column definitions
     */
    public final Map<String, ColumnMeta> getColumnsDesc() {
        return this.columnsDesc;
    }

    /**
     * Return the Map storing column aliases
     *
     * @return Map storing column alias
     */
    public final Map<String, String> getColumnsAliases() {
        return this.columnsAliases;
    }

    /**
     * Return the column name given its alias
     *
     * @param alias column alias
     * @return column name or null if undefined
     */
    public final String getColumnNameByAlias(final String alias) {
        return getColumnsAliases().get(alias);
    }

    /**
     * Return the column definition given its name
     *
     * @param name column name
     * @return column definition or null if undefined
     */
    public final ColumnMeta getColumnDesc(final String name) {
        return getColumnsDesc().get(name);
    }

    /**
     * Return the ordered collection of column definitions
     *
     * @return ordered collection of column definitions
     */
    public final Collection<ColumnMeta> getColumnDescCollection() {
        return getColumnsDesc().values();
    }

    /**
     * Return the number of columns
     *
     * @return the number of columns
     */
    public final int getNbColumns() {
        return getColumnsDesc().size();
    }

    /**
     * Add the given column descriptor
     *
     * @param meta column descriptor
     */
    protected final void addColumnMeta(final ColumnMeta meta) {
        getColumnsDesc().put(meta.getName(), meta);

        if (meta.getAlias() != null) {
            getColumnsAliases().put(meta.getAlias(), meta.getName());
        }
    }

    /**
     * Remove the given column descriptor
     *
     * @param meta column descriptor
     * @return true if removed; false otherwise
     */
    protected final boolean removeColumnMeta(final ColumnMeta meta) {
        final boolean removed = getColumnsDesc().remove(meta.getName()) != null;

        if (meta.getAlias() != null) {
            getColumnsAliases().remove(meta.getAlias());
        }
        return removed;
    }

    /**
     * Return the ordered collection of all column descriptors
     * (standard then derived)
     *
     * @return ordered collection of column definitions
     */
    public final Collection<ColumnMeta> getAllColumnDescCollection() {
        final ArrayList<ColumnMeta> columnDescList = new ArrayList<ColumnMeta>(
                getNbColumns() + getNbDerivedColumns());
        // Standard columns:
        columnDescList.addAll(getColumnDescCollection());
        // Derived columns:
        columnDescList.addAll(getColumnDerivedDescCollection());
        return columnDescList;
    }

    /**
     * Append column names (standard then derived) representing numerical values
     * (double arrays)
     *
     * @param columnNames set to be filled with numerical column names
     */
    public final void getNumericalColumnsNames(final Set<String> columnNames) {
        // Standard columns:
        for (Map.Entry<String, ColumnMeta> entry : getColumnsDesc().entrySet()) {
            final ColumnMeta meta = entry.getValue();
            switch (meta.getDataType()) {
                case TYPE_DBL:
                    columnNames.add(entry.getKey());
                    break;
                default:
            }
        }
        // Derived columns:
        for (Map.Entry<String, ColumnMeta> entry : getColumnsDerivedDesc().entrySet()) {
            final ColumnMeta meta = entry.getValue();
            switch (meta.getDataType()) {
                case TYPE_DBL:
                    columnNames.add(entry.getKey());
                    break;
                default:
            }
        }
    }

    /**
     * Return the column descriptors (standard then derived) representing
     * numerical values (double arrays)
     *
     * @param columnDescList list to store numerical column descriptors
     * @return given columnDescList reference
     */
    public final ArrayList<ColumnMeta> getNumericalColumnsDescs(final ArrayList<ColumnMeta> columnDescList) {
        // Standard columns:
        for (Map.Entry<String, ColumnMeta> entry : getColumnsDesc().entrySet()) {
            final ColumnMeta meta = entry.getValue();
            switch (meta.getDataType()) {
                case TYPE_DBL:
                    columnDescList.add(meta);
                    break;
                default:
            }
        }
        // Derived columns:
        for (Map.Entry<String, ColumnMeta> entry : getColumnsDerivedDesc().entrySet()) {
            final ColumnMeta meta = entry.getValue();
            switch (meta.getDataType()) {
                case TYPE_DBL:
                    columnDescList.add(meta);
                    break;
                default:
            }
        }
        return columnDescList;
    }

    /**
     * Return the column descriptors (standard then derived) representing
     * CHAR/SHORT/INT values (arrays) except "CORRINDX_..." columns
     *
     * @param columnDescList list to store other column descriptors
     * @return given columnDescList reference
     */
    public final ArrayList<ColumnMeta> getOtherColumnsDescs(final ArrayList<ColumnMeta> columnDescList) {
        // Standard columns:
        for (Map.Entry<String, ColumnMeta> entry : getColumnsDesc().entrySet()) {
            final ColumnMeta meta = entry.getValue();
            if (!meta.getName().startsWith(OIFitsConstants.PREFIX_CORRINDX)) {
                switch (meta.getDataType()) {
                    case TYPE_CHAR:
                    case TYPE_SHORT:
                    case TYPE_INT:
                        columnDescList.add(meta);
                        break;
                    default:
                }
            }
        }
        // Derived columns:
        for (Map.Entry<String, ColumnMeta> entry : getColumnsDerivedDesc().entrySet()) {
            final ColumnMeta meta = entry.getValue();
            if (!meta.getName().startsWith(OIFitsConstants.PREFIX_CORRINDX)) {
                switch (meta.getDataType()) {
                    case TYPE_CHAR:
                    case TYPE_SHORT:
                    case TYPE_INT:
                        columnDescList.add(meta);
                        break;
                    default:
                }
            }
        }
        return columnDescList;
    }

    /*
     * --- Column values -------------------------------------------------------
     */
    /**
     * Return the Map storing column values
     *
     * @return Map storing column values
     */
    public final Map<String, Object> getColumnsValue() {
        return this.columnsValue;
    }

    /**
     * Return true if the table contains the column given its column descriptor
     *
     * @param meta column descriptor
     * @return true if the table contains the column
     */
    public final boolean hasColumn(final ColumnMeta meta) {
        return getColumnValue(meta.getName()) != null;
    }

    /**
     * Remove the optional column given its column descriptor
     * @param meta column descriptor
     * @return true if removed; false otherwise
     */
    public final boolean removeColumn(final ColumnMeta meta) {
        if ((meta != null) && meta.isOptional()) {
            if (removeColumnMeta(meta)) {
                return removeColumnValue(meta.getName());
            }
        }
        return false;
    }

    /**
     * Return the column value given its name The returned value can be null if
     * the column has never been defined
     *
     * @param name column name
     * @return any array value or null if undefined
     */
    public final Object getColumnValue(final String name) {
        return getColumnsValue().get(name);
    }

    /**
     * Return the column value given its name as a String array The returned
     * value can be null if the column has never been defined
     *
     * @param name column name
     * @return String array or null if undefined
     */
    public final String[] getColumnString(final String name) {
        return (String[]) getColumnValue(name);
    }

    /**
     * Return the column value given its name as an integer array (int
     * primitive type) The returned value can be null if the column has never
     * been defined
     *
     * @param name column name
     * @return integer array or null if undefined
     */
    public final int[] getColumnInt(final String name) {
        return (int[]) getColumnValue(name);
    }

    /**
     * Return the column value given its name as a 2D integer array (int
     * primitive type) The returned value can be null if the column has never
     * been defined
     *
     * @param name column name
     * @return integer array or null if undefined
     */
    public final int[][] getColumnInts(final String name) {
        return (int[][]) getColumnValue(name);
    }

    /**
     * Return the column value given its name as an integer array (short
     * primitive type) The returned value can be null if the column has never
     * been defined
     *
     * @param name column name
     * @return integer array or null if undefined
     */
    public final short[] getColumnShort(final String name) {
        return (short[]) getColumnValue(name);
    }

    /**
     * Return the column value given its name as a 2D integer array (short
     * primitive type) The returned value can be null if the column has never
     * been defined
     *
     * @param name column name
     * @return 2D integer array or null if undefined
     */
    public final short[][] getColumnShorts(final String name) {
        return (short[][]) getColumnValue(name);
    }

    /**
     * Return the column value given its name as a float array The returned
     * value can be null if the column has never been defined
     *
     * @param name column name
     * @return float array or null if undefined
     */
    public final float[] getColumnFloat(final String name) {
        return (float[]) getColumnValue(name);
    }

    /**
     * Return the column value given its name as a double array The returned
     * value can be null if the column has never been defined
     *
     * @param name column name
     * @return double array or null if undefined
     */
    public final double[] getColumnDouble(final String name) {
        return (double[]) getColumnValue(name);
    }

    /**
     * Return the column value given its name as a 2D double array The returned
     * value can be null if the column has never been defined
     *
     * @param name column name
     * @return 2D double array or null if undefined
     */
    public final double[][] getColumnDoubles(final String name) {
        return (double[][]) getColumnValue(name);
    }

    /**
     * Return the column value given its name as a 2D complex array The returned
     * value can be null if the column has never been defined
     *
     * @param name column name
     * @return 2D complex array or null if undefined
     */
    public final float[][][] getColumnComplexes(final String name) {
        return (float[][][]) getColumnValue(name);
    }

    /**
     * Return the column value given its name as a 2D boolean array The returned
     * value can be null if the column has never been defined
     *
     * @param name column name
     * @return 2D boolean array or null if undefined
     */
    public final boolean[][] getColumnBooleans(final String name) {
        return (boolean[][]) getColumnValue(name);
    }

    /**
     * Return the column value given its name as a 3D boolean array The returned
     * value can be null if the column has never been defined
     *
     * @param name column name
     * @return 3D boolean array or null if undefined
     */
    public final boolean[][][] getColumnBoolean3D(final String name) {
        return (boolean[][][]) getColumnValue(name);
    }

    /**
     * Define the column value given its name and an array value (String[] or a
     * primitive array)
     *
     * @param name column name
     * @param value any array value
     */
    public final void setColumnValue(final String name, final Object value) {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "COLUMN [{0}] = {1}", new Object[]{name, (value != null) ? ArrayFuncs.arrayDescription(value) : ""});
        }
        getColumnsValue().put(name, value);
    }

    /**
     * Removes the column value given its name
     * 
     * @param name column name
     * @return true if removed; false otherwise
     */
    protected final boolean removeColumnValue(final String name) {
        return getColumnsValue().remove(name) != null;
    }

    /*
     * --- Derived Column descriptors ------------------------------------------
     */
    /**
     * Return the Map storing derived column definitions
     *
     * @return Map storing derived column definitions
     */
    protected final Map<String, ColumnMeta> getColumnsDerivedDesc() {
        // lazy
        if (this.columnsDerivedDesc == null) {
            this.columnsDerivedDesc = new LinkedHashMap<String, ColumnMeta>();
        }
        return this.columnsDerivedDesc;
    }

    /**
     * Return the derived column definition given its name
     *
     * @param name column name
     * @return derived column definition or null if undefined
     */
    public final ColumnMeta getColumnDerivedDesc(final String name) {
        if (this.columnsDerivedDesc != null) {
            return getColumnsDerivedDesc().get(name);
        }
        return null;
    }

    /**
     * Return the collection of derived column definitions
     *
     * @return collection of derived column definitions
     */
    public final Collection<ColumnMeta> getColumnDerivedDescCollection() {
        return getColumnsDerivedDesc().values();
    }

    /**
     * Return the number of derived columns
     *
     * @return the number of derived columns
     */
    public final int getNbDerivedColumns() {
        return getColumnsDerivedDesc().size();
    }

    /**
     * Add the given derived column descriptor
     *
     * @param meta derived meta column descriptor
     */
    public final void addDerivedColumnMeta(final ColumnMeta meta) {
        getColumnsDerivedDesc().put(meta.getName(), meta);

        if (meta.getAlias() != null) {
            getColumnsAliases().put(meta.getAlias(), meta.getName());
        }
    }

    /**
     * Remove the derived column definition given its name
     *
     * @param name column name
     */
    public final void removeDerivedColumnMeta(final String name) {
        getColumnsDerivedDesc().remove(name);
    }

    /*
     * --- Derived Column values -----------------------------------------------
     */
    /**
     * Clear the Map storing column derived value
     */
    protected final void clearColumnsDerivedValue() {
        if (this.columnsDerivedValue != null) {
            this.columnsDerivedValue.clear();
        }
    }

    /**
     * Return the Map storing column derived value
     *
     * @return Map storing column derived value
     */
    public final Map<String, Object> getColumnsDerivedValue() {
        // lazy
        if (this.columnsDerivedValue == null) {
            this.columnsDerivedValue = new HashMap<String, Object>();
        }

        return this.columnsDerivedValue;
    }

    /**
     * Return the column derived value given its name The returned value can be
     * null if the column derived value has never been defined
     *
     * @param name column name
     * @return any value or null if undefined
     */
    public final Object getColumnDerivedValue(final String name) {
        return getColumnsDerivedValue().get(name);
    }

    /**
     * Return the column derived value given its name as a String array The
     * returned value can be null if the column derived value has never been
     * defined
     *
     * @param name column name
     * @return String array or null if undefined
     */
    public final String[] getColumnDerivedString(final String name) {
        return (String[]) getColumnDerivedValue(name);
    }

    /**
     * Return the column derived value given its name as an integer array (int
     * primitive type) The returned value can be null if the column derived
     * value has never been defined
     *
     * @param name column name
     * @return integer array or null if undefined
     */
    public final int[] getColumnDerivedInt(final String name) {
        return (int[]) getColumnDerivedValue(name);
    }

    /**
     * Return the column value given its name as a 2D integer array (int
     * primitive type) The returned value can be null if the column has never
     * been defined
     *
     * @param name column name
     * @return integer array or null if undefined
     */
    public final int[][] getColumnDerivedInts(final String name) {
        return (int[][]) getColumnDerivedValue(name);
    }

    /**
     * Return the column derived value given its name as an integer array (short
     * primitive type) The returned value can be null if the column derived
     * value has never been defined
     *
     * @param name column name
     * @return integer array or null if undefined
     */
    public final short[] getColumnDerivedShort(final String name) {
        return (short[]) getColumnDerivedValue(name);
    }

    /**
     * Return the column derived value given its name as a 2D integer array
     * (short primitive type) The returned value can be null if the column
     * derived value has never been defined
     *
     * @param name column name
     * @return 2D integer array or null if undefined
     */
    public final short[][] getColumnDerivedShorts(final String name) {
        return (short[][]) getColumnDerivedValue(name);
    }

    /**
     * Return the column derived value given its name as a float array The
     * returned value can be null if the column derived value has never been
     * defined
     *
     * @param name column name
     * @return float array or null if undefined
     */
    public final float[] getColumnDerivedFloat(final String name) {
        return (float[]) getColumnDerivedValue(name);
    }

    /**
     * Return the column derived value given its name as a double array The
     * returned value can be null if the column derived value has never been
     * defined
     *
     * @param name column name
     * @return double array or null if undefined
     */
    public final double[] getColumnDerivedDouble(final String name) {
        return (double[]) getColumnDerivedValue(name);
    }

    /**
     * Return the column derived value given its name as a 2D double array The
     * returned value can be null if the column derived value has never been
     * defined
     *
     * @param name column name
     * @return 2D double array or null if undefined
     */
    public final double[][] getColumnDerivedDoubles(final String name) {
        return (double[][]) getColumnDerivedValue(name);
    }

    /**
     * Define the column derived value given its name and any value
     *
     * @param name column name
     * @param value any value
     */
    public final void setColumnDerivedValue(final String name, final Object value) {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "DERIVED COLUMN VALUE [{0}] = {1}", new Object[]{name, (value != null) ? ArrayFuncs.arrayDescription(value) : ""});
        }
        getColumnsDerivedValue().put(name, value);
    }

    /**
     * Remove the column derived value given its name
     *
     * @param name column name
     */
    public final void removeColumnDerivedValue(final String name) {
        getColumnsDerivedValue().remove(name);
    }

    /*
     * --- Column Range values -------------------------------------------------
     */
    /**
     * Clear the Map storing min/max column values
     */
    protected final void clearColumnsRangeValue() {
        if (this.columnsRangeValue != null) {
            this.columnsRangeValue.clear();
        }
    }

    /**
     * Clear the Map storing min/max column values for the given column name
     * @param name column name
     */
    protected final void clearColumnsRangeValue(final String name) {
        if (this.columnsRangeValue != null) {
            // see getColumnRange(name):
            final String key = name + "_RANGE";
            columnsRangeValue.remove(key); // Range
            columnsRangeValue.remove(name); // [min;max] values
        }
    }

    /**
     * Return the Map storing min/max column values
     *
     * @return Map storing min/max of column values
     */
    protected final Map<String, Object> getColumnsRangeValue() {
        // lazy
        if (this.columnsRangeValue == null) {
            this.columnsRangeValue = new HashMap<String, Object>();
        }
        return this.columnsRangeValue;
    }

    /**
     * Return the column range given its name
     *
     * @param name column name
     * @return Range instance or Range.UNDEFINED_RANGE if no data
     */
    public Range getColumnRange(final String name) {
        final String key = name + "_RANGE";

        /* retrieve value in columnsRangeValue map of associated column */
        Range cached = (Range) getColumnsRangeValue().get(key); // Range

        if (cached == null) {
            final Object range = getMinMaxColumnValue(name);

            if (range != null) {
                if (range instanceof double[]) {
                    final double[] dRange = (double[]) range;
                    cached = new Range(dRange[0], dRange[1]);
                } else if (range instanceof float[]) {
                    final float[] fRange = (float[]) range;
                    cached = new Range(fRange[0], fRange[1]);
                } else if (range instanceof int[]) {
                    final int[] iRange = (int[]) range;
                    cached = new Range(iRange[0], iRange[1]);
                } else if (range instanceof short[]) {
                    final short[] sRange = (short[]) range;
                    cached = new Range(sRange[0], sRange[1]);
                }
            }
            if (cached == null) {
                cached = Range.UNDEFINED_RANGE;
            }
            /* store in associated column range value */
            getColumnsRangeValue().put(key, cached);
        }
        return cached;
    }

    /**
     * Return the minimum and maximum column value given its name The returned
     * value can be null if the column has never been defined
     *
     * @param name column name
     * @return [min;max] values or null if undefined
     */
    public final Object getMinMaxColumnValue(final String name) {
        /* retrieve range in columnsRangeValue map of associated column */
        Object range = getColumnsRangeValue().get(name);

        /* compute min and max values if not previously set */
        if (range == null) {
            final ColumnMeta column = getColumnMeta(name);

            if (column != null) {
                switch (column.getDataType()) {
                    case TYPE_CHAR:
                        // Not Applicable
                        break;

                    case TYPE_SHORT:
                        short sMin = Short.MAX_VALUE;
                        short sMax = Short.MIN_VALUE;

                        if (column.isArray()) {
                            final short[][] sValues = getColumnAsShorts(column.getName());
                            if (sValues == null) {
                                break;
                            }
                            for (int i = 0, len = sValues.length; i < len; i++) {
                                final short[] sRowValues = sValues[i];
                                for (int j = 0, jlen = sRowValues.length; j < jlen; j++) {
                                    if (sRowValues[j] < sMin) {
                                        sMin = sRowValues[j];
                                    }
                                    if (sRowValues[j] > sMax) {
                                        sMax = sRowValues[j];
                                    }
                                }
                            }
                        } else {
                            final short[] sValues = getColumnAsShort(column.getName());
                            if (sValues == null) {
                                break;
                            }
                            for (int i = 0, len = sValues.length; i < len; i++) {
                                if (sValues[i] < sMin) {
                                    sMin = sValues[i];
                                }
                                if (sValues[i] > sMax) {
                                    sMax = sValues[i];
                                }
                            }
                        }
                        range = new short[]{sMin, sMax};
                        break;

                    case TYPE_INT:
                        int iMin = Integer.MAX_VALUE;
                        int iMax = Integer.MIN_VALUE;

                        if (column.isArray()) {
                            final int[][] iValues = getColumnAsInts(column.getName());
                            if (iValues == null) {
                                break;
                            }
                            for (int i = 0, len = iValues.length; i < len; i++) {
                                final int[] iRowValues = iValues[i];
                                for (int j = 0, jlen = iRowValues.length; j < jlen; j++) {
                                    if (iRowValues[j] < iMin) {
                                        iMin = iRowValues[j];
                                    }
                                    if (iRowValues[j] > iMax) {
                                        iMax = iRowValues[j];
                                    }
                                }
                            }
                        } else {
                            final int[] iValues = getColumnAsInt(column.getName());
                            if (iValues == null) {
                                break;
                            }
                            for (int i = 0, len = iValues.length; i < len; i++) {
                                if (iValues[i] < iMin) {
                                    iMin = iValues[i];
                                }
                                if (iValues[i] > iMax) {
                                    iMax = iValues[i];
                                }
                            }
                        }
                        range = new int[]{iMin, iMax};
                        break;

                    case TYPE_DBL:
                        double dMin = Double.POSITIVE_INFINITY;
                        double dMax = Double.NEGATIVE_INFINITY;

                        if (column.isArray()) {
                            final double[][] dValues = getColumnAsDoubles(column.getName());
                            if (dValues == null) {
                                break;
                            }
                            // use column flag if present (OIData tables):
                            final boolean[][] flags = (this instanceof OIData) ? ((OIData) this).getFlag() : null;

                            for (int i = 0, len = dValues.length; i < len; i++) {
                                final double[] dRowValues = dValues[i];
                                final boolean[] rowFlags = (flags != null) ? flags[i] : null;

                                for (int j = 0, jlen = dRowValues.length; j < jlen; j++) {
                                    if ((rowFlags == null) || !rowFlags[j]) {
                                        if (dRowValues[j] < dMin) {
                                            dMin = dRowValues[j];
                                        }
                                        if (dRowValues[j] > dMax) {
                                            dMax = dRowValues[j];
                                        }
                                    }
                                }
                            }
                        } else {
                            final double[] dValues = getColumnAsDouble(column.getName());
                            if (dValues == null) {
                                break;
                            }
                            for (int i = 0, len = dValues.length; i < len; i++) {
                                if (dValues[i] < dMin) {
                                    dMin = dValues[i];
                                }
                                if (dValues[i] > dMax) {
                                    dMax = dValues[i];
                                }
                            }
                        }
                        range = new double[]{dMin, dMax};
                        break;
                    case TYPE_REAL:
                        float fMin = Float.POSITIVE_INFINITY;
                        float fMax = Float.NEGATIVE_INFINITY;

                        if (column.isArray()) {
                            // Impossible case in OIFits
                            break;
                        }
                        final float[] fValues = getColumnAsFloat(column.getName());
                        if (fValues == null) {
                            break;
                        }
                        for (int i = 0, len = fValues.length; i < len; i++) {
                            if (fValues[i] < fMin) {
                                fMin = fValues[i];
                            }
                            if (fValues[i] > fMax) {
                                fMax = fValues[i];
                            }
                        }
                        range = new float[]{fMin, fMax};
                        break;

                    case TYPE_COMPLEX:
                        // Not Applicable
                        break;

                    case TYPE_LOGICAL:
                        // Not Applicable
                        break;

                    default:
                    // do nothing
                }
                if (range != null) {
                    /* store in associated column range value */
                    getColumnsRangeValue().put(name, range);
                }
            }
        }
        return range;
    }

    /*
     * --- Fits standard Keywords --------------------------------------------
     */
    /**
     * Get the EXTVER keyword value
     *
     * @return value of EXTVER keyword
     */
    public final int getExtVer() {
        return getKeywordInt(FitsConstants.KEYWORD_EXT_VER);
    }

    /**
     * Define the EXTVER keyword value
     *
     * @param extVer value of EXTVER keyword
     */
    public final void setExtVer(final int extVer) {
        setKeywordInt(FitsConstants.KEYWORD_EXT_VER, extVer);
    }

    /**
     * Return the number of rows i.e. the Fits NAXIS2 keyword value
     *
     * @return the number of rows i.e. the Fits NAXIS2 keyword value
     */
    public final int getNbRows() {
        return getKeywordInt(FitsConstants.KEYWORD_NAXIS2);
    }

    /**
     * Define the number of rows i.e. the Fits NAXIS2 keyword value
     *
     * @param nbRows number of rows i.e. the Fits NAXIS2 keyword value
     */
    protected final void setNbRows(final int nbRows) {
        setKeywordInt(FitsConstants.KEYWORD_NAXIS2, nbRows);
    }

    /*
     * --- Checker -------------------------------------------------------------
     */
    /**
     * Do syntactical analysis of the table
     *
     * @param checker checker component
     */
    @Override
    public void checkSyntax(final OIFitsChecker checker) {
        // First analyse keywords
        super.checkSyntax(checker);

        // Second analyse columns
        checkColumns(checker);
    }

    /**
     * Check syntax of table's columns. It consists in checking all mandatory
     * columns are present, with right name, right format and right associated
     * unit.
     *
     * @param checker checker component
     */
    public void checkColumns(final OIFitsChecker checker) {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "checkColumns : {0}", this.toString());
        }
        final int nRows = getNbRows();
        String columnName;
        Object value;

        /* Get mandatory columns names */
        for (ColumnMeta column : getColumnDescCollection()) {
            columnName = column.getName();
            value = getColumnValue(columnName);

            if ((value == null) || OIFitsChecker.isInspectRules()) {
                if (!column.isOptional()) {
                    /* No column with columnName name */
                    // rule [GENERIC_COL_MANDATORY] check if the required column is present
                    if (checker != null) {
                        checker.ruleFailed(Rule.GENERIC_COL_MANDATORY, this, columnName);
                    }
                }
            }
            if ((value == null) && OIFitsChecker.isInspectRules()) {
                // Create a new column value to always enter in column.check(checker) below:
                value = createColumnArray(column, nRows);
            }
            if (value != null) {
                /* Check the column validity */
                column.check(checker, this, value, nRows);
            }
        }
    }

    /*
     * --- public descriptor access ---------------------------------------------------------
     */
    /**
     * Return the column definition given its name (standard or derived)
     *
     * @param name column name
     * @return column definition or null if undefined
     */
    public final ColumnMeta getColumnMeta(final String name) {
        if (name != null) {
            ColumnMeta meta = getColumnDesc(name);
            if (meta != null) {
                return meta;
            }
            meta = getColumnDerivedDesc(name);
            if (meta != null) {
                return meta;
            }
        }
        return null;
    }

    /*
     * --- public data access ---------------------------------------------------------
     */
    /**
     * Return the column data as String array for the given column name
     * (standard or derived).
     *
     * @param name any column name
     * @return column data as String array or null if undefined or wrong
     * type
     */
    public final String[] getColumnAsString(final String name) {
        if (name != null) {
            ColumnMeta meta = getColumnDesc(name);
            if (meta != null
                    && meta.getDataType() == Types.TYPE_CHAR
                    && !meta.isArray()) {
                return getColumnString(name);
            }
            meta = getColumnDerivedDesc(name);
            if (meta != null
                    && meta.getDataType() == Types.TYPE_CHAR
                    && !meta.isArray()) {
                return getColumnDerivedString(name);
            }
        }
        return null;
    }

    /**
     * Return the column data as int array (1D) for the given column name
     * (standard or derived).
     *
     * @param name any column name
     * @return column data as short array (1D) or null if undefined or wrong
     * type
     */
    public final int[] getColumnAsInt(final String name) {
        if (name != null) {
            ColumnMeta meta = getColumnDesc(name);
            if (meta != null
                    && meta.getDataType() == Types.TYPE_INT
                    && !meta.isArray()) {
                return getColumnInt(name);
            }
            meta = getColumnDerivedDesc(name);
            if (meta != null
                    && meta.getDataType() == Types.TYPE_INT
                    && !meta.isArray()) {
                return getColumnDerivedInt(name);
            }
        }
        return null;
    }

    /**
     * Return the column data as short arrays (2D) for the given column name
     * (standard or derived).
     *
     * @param name any column name
     * @return column data as short arrays (2D) or null if undefined or wrong
     * type
     */
    public final int[][] getColumnAsInts(final String name) {
        if (name != null) {
            ColumnMeta meta = getColumnDesc(name);
            if (meta != null
                    && meta.getDataType() == Types.TYPE_INT
                    && meta.isArray()) {
                return getColumnInts(name);
            }
            meta = getColumnDerivedDesc(name);
            if (meta != null
                    && meta.getDataType() == Types.TYPE_INT
                    && meta.isArray()) {
                return getColumnDerivedInts(name);
            }
        }
        return null;
    }

    /**
     * Return the column data as short array (1D) for the given column name
     * (standard or derived).
     *
     * @param name any column name
     * @return column data as short array (1D) or null if undefined or wrong
     * type
     */
    public final short[] getColumnAsShort(final String name) {
        if (name != null) {
            ColumnMeta meta = getColumnDesc(name);
            if (meta != null
                    && meta.getDataType() == Types.TYPE_SHORT
                    && !meta.isArray()) {
                return getColumnShort(name);
            }
            meta = getColumnDerivedDesc(name);
            if (meta != null
                    && meta.getDataType() == Types.TYPE_SHORT
                    && !meta.isArray()) {
                return getColumnDerivedShort(name);
            }
        }
        return null;
    }

    /**
     * Return the column data as short arrays (2D) for the given column name
     * (standard or derived).
     *
     * @param name any column name
     * @return column data as short arrays (2D) or null if undefined or wrong
     * type
     */
    public final short[][] getColumnAsShorts(final String name) {
        if (name != null) {
            ColumnMeta meta = getColumnDesc(name);
            if (meta != null
                    && meta.getDataType() == Types.TYPE_SHORT
                    && meta.isArray()) {
                return getColumnShorts(name);
            }
            meta = getColumnDerivedDesc(name);
            if (meta != null
                    && meta.getDataType() == Types.TYPE_SHORT
                    && meta.isArray()) {
                return getColumnDerivedShorts(name);
            }
        }
        return null;
    }

    /**
     * Return the column data as float array (1D) for the given column name
     * (standard or derived).
     *
     * @param name any column name
     * @return column data as float array (1D) or null if undefined or wrong
     * type
     */
    public final float[] getColumnAsFloat(final String name) {
        if (name != null) {
            ColumnMeta meta = getColumnDesc(name);
            if (meta != null
                    && meta.getDataType() == Types.TYPE_REAL
                    && !meta.isArray()) {
                return getColumnFloat(name);
            }
            meta = getColumnDerivedDesc(name);
            if (meta != null
                    && meta.getDataType() == Types.TYPE_REAL
                    && !meta.isArray()) {
                return getColumnDerivedFloat(name);
            }
        }
        return null;
    }

    /**
     * Return the column data as double array (1D) for the given column name
     * (standard or derived). No conversion are performed here: only column
     * storing double values are returned !
     *
     * @param name any column name
     * @return column data as double array (1D) or null if undefined or wrong
     * type
     */
    public final double[] getColumnAsDouble(final String name) {
        if (name != null) {
            ColumnMeta meta = getColumnDesc(name);
            if (meta != null
                    && meta.getDataType() == Types.TYPE_DBL
                    && !meta.isArray()) {
                return getColumnDouble(name);
            }
            meta = getColumnDerivedDesc(name);
            if (meta != null
                    && meta.getDataType() == Types.TYPE_DBL
                    && !meta.isArray()) {
                return getDerivedColumnAsDouble(name);
            }
        }
        return null;
    }

    /**
     * Return the derived column data as double array (1D) for the given column
     * name To be overriden in child classes for lazy computed columns
     *
     * @param name any column name
     * @return column data as double array (1D) or null if undefined or wrong
     * type
     */
    protected double[] getDerivedColumnAsDouble(final String name) {
        return null;
    }

    /**
     * Return the column data as double arrays (2D) for the given column name
     * (standard or derived). No conversion are performed here: only column
     * storing double values are returned !
     *
     * @param name any column name
     * @return column data as double arrays (2D) or null if undefined or wrong
     * type
     */
    public final double[][] getColumnAsDoubles(final String name) {
        if (name != null) {
            ColumnMeta meta = getColumnDesc(name);
            if (meta != null
                    && meta.getDataType() == Types.TYPE_DBL
                    && meta.isArray()) {
                return getColumnDoubles(name);
            }
            meta = getColumnDerivedDesc(name);
            if (meta != null
                    && meta.getDataType() == Types.TYPE_DBL
                    && meta.isArray()) {
                return getDerivedColumnAsDoubles(name);
            }
        }
        return null;
    }

    /**
     * Return the derived column data as double arrays (2D) for the given column
     * name To be overriden in child classes for lazy computed columns
     *
     * @param name any column name
     * @return column data as double arrays (2D) or null if undefined or wrong
     * type
     */
    protected double[][] getDerivedColumnAsDoubles(final String name) {
        return null;
    }

}
