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

import fr.jmmc.oitools.model.OIAbstractData;

/**
 * This specific ColumnMeta overrides the getRepeat() method to use the OIWaveLength.getNWave() method
 * in a dynamic way
 * @author bourgesl
 */
public final class WaveColumnMeta extends ArrayColumnMeta {

    /* members */
    /** reference to OIAbstractData object to dynamically invoke OIAbstractData.getNWave() */
    private final OIAbstractData oiAbsData;
    /** optional expression to compute derived columns */
    private String expression;

    /**
     * ColumnMeta class constructor
     *
     * @param name keyword/column name
     * @param desc keyword/column descriptive comment
     * @param dataType keyword/column data type
     * @param oiData OIData object to resolve OIWaveLength reference
     */
    public WaveColumnMeta(final String name, final String desc, final Types dataType, final OIAbstractData oiData) {
        this(name, desc, dataType, false, false, NO_STR_VALUES, Units.NO_UNIT, null, null, oiData);
    }

    /**
     * ColumnMeta class constructor
     *
     * @param name keyword/column name
     * @param desc keyword/column descriptive comment
     * @param dataType keyword/column data type
     * @param oiData OIData object to resolve OIWaveLength reference
     * @param expression expression entered by the user
     */
    public WaveColumnMeta(final String name, final String desc, final Types dataType, final OIAbstractData oiData, final String expression) {
        this(name, desc, dataType, false, false, NO_STR_VALUES, Units.NO_UNIT, null, null, oiData);
        this.expression = expression;
    }

    /**
     * ColumnMeta class constructor
     *
     * @param name keyword/column name
     * @param desc keyword/column descriptive comment
     * @param dataType keyword/column data type
     * @param oiData OIData object to resolve OIWaveLength reference
     * @param expression expression entered by the user
     * @param dataRange optional data range (may be null)
     */
    public WaveColumnMeta(final String name, final String desc, final Types dataType, final OIAbstractData oiData, final String expression, final DataRange dataRange) {
        this(name, desc, dataType, false, false, NO_STR_VALUES, Units.NO_UNIT, null, dataRange, oiData);
        this.expression = expression;
    }

    /**
     * ColumnMeta class constructor
     *
     * @param name keyword/column name
     * @param desc keyword/column descriptive comment
     * @param dataType keyword/column data type
     * @param dataRange optional data range (may be null)
     * @param oiData OIData object to resolve OIWaveLength reference
     */
    public WaveColumnMeta(final String name, final String desc, final Types dataType, final DataRange dataRange, final OIAbstractData oiData) {
        this(name, desc, dataType, false, false, NO_STR_VALUES, Units.NO_UNIT, null, dataRange, oiData);
    }

    /**
     * ColumnMeta class constructor
     *
     * @param name keyword/column name
     * @param desc keyword/column descriptive comment
     * @param dataType keyword/column data type
     * @param errName column name storing error values
     * @param dataRange optional data range (may be null)
     * @param oiData OIData object to resolve OIWaveLength reference
     */
    public WaveColumnMeta(final String name, final String desc, final Types dataType, final String errName,
            final DataRange dataRange, final OIAbstractData oiData) {
        this(name, desc, dataType, false, false, NO_STR_VALUES, Units.NO_UNIT, errName, dataRange, oiData);
    }

    /**
     * ColumnMeta class constructor for an optional column
     *
     * @param name keyword/column name
     * @param desc keyword/column descriptive comment
     * @param dataType keyword/column data type
     * @param optional flag to indicate if the column is optional
     * @param is3D true if keyword/column is on 3D
     * @param oiData OIData object to resolve OIWaveLength reference
     */
    public WaveColumnMeta(final String name, final String desc, final Types dataType, final boolean optional, final boolean is3D, final OIAbstractData oiData) {
        this(name, desc, dataType, optional, is3D, NO_STR_VALUES, Units.NO_UNIT, null, null, oiData);
    }

    /**
     * ColumnMeta class constructor for an optional column
     *
     * @param name keyword/column name
     * @param desc keyword/column descriptive comment
     * @param dataType keyword/column data type
     * @param errName column name storing error values
     * @param optional flag to indicate if the column is optional
     * @param oiData OIData object to resolve OIWaveLength reference
     */
    public WaveColumnMeta(final String name, final String desc, final Types dataType, final String errName,
            final boolean optional, final OIAbstractData oiData) {
        this(name, desc, dataType, optional, false, NO_STR_VALUES, Units.NO_UNIT, errName, null, oiData);
    }

    /**
     * ColumnMeta class constructor
     *
     * @param name keyword/column name
     * @param desc keyword/column descriptive comment
     * @param dataType keyword/column data type
     * @param unit keyword/column unit
     * @param oiData OIData object to resolve OIWaveLength reference
     */
    public WaveColumnMeta(final String name, final String desc, final Types dataType, final Units unit, final OIAbstractData oiData) {
        this(name, desc, dataType, false, false, NO_STR_VALUES, unit, null, null, oiData);
    }

    /**
     * ColumnMeta class constructor
     *
     * @param name keyword/column name
     * @param desc keyword/column descriptive comment
     * @param dataType keyword/column data type
     * @param unit keyword/column unit
     * @param dataRange optional data range (may be null)
     * @param oiData OIData object to resolve OIWaveLength reference
     */
    public WaveColumnMeta(final String name, final String desc, final Types dataType, final Units unit,
            final DataRange dataRange, final OIAbstractData oiData) {
        this(name, desc, dataType, false, false, NO_STR_VALUES, unit, null, dataRange, oiData);
    }

    /**
     * ColumnMeta class constructor
     *
     * @param name keyword/column name
     * @param desc keyword/column descriptive comment
     * @param dataType keyword/column data type
     * @param unit keyword/column unit
     * @param errName column name storing error values
     * @param dataRange optional data range (may be null)
     * @param oiData OIData object to resolve OIWaveLength reference
     */
    public WaveColumnMeta(final String name, final String desc, final Types dataType, final Units unit,
            final String errName, final DataRange dataRange, final OIAbstractData oiData) {
        this(name, desc, dataType, false, false, NO_STR_VALUES, unit, errName, dataRange, oiData);
    }

    /**
     * ColumnMeta class constructor
     *
     * @param name keyword/column name
     * @param desc keyword/column descriptive comment
     * @param dataType keyword/column data type
     * @param unit keyword/column unit
     * @param errName column name storing error values
     * @param acceptedValues possible values for keyword/column
     * @param dataRange optional data range (may be null)
     * @param optional flag to indicate if the column is optional
     * @param is3D true if keyword/column is on 3D
     * @param oiData OIData object to resolve OIWaveLength reference
     */
    public WaveColumnMeta(final String name, final String desc, final Types dataType, final boolean optional, final boolean is3D,
            final String[] acceptedValues, final Units unit, final String errName, final DataRange dataRange, final OIAbstractData oiData) {
        super(name, desc, dataType, 0, optional, is3D, acceptedValues, unit, errName, dataRange);

        this.oiAbsData = oiData;
        this.expression = null;
    }

    /**
     * Return the repeat value i.e. cardinality = number of distinct spectral channels
     * It uses the OIAbstractData.getNWave() method to get the number of distinct spectral
     * channels of the associated OI_WAVELENGTH
     * @return repeat value i.e. cardinality = number of distinct spectral channels
     */
    @Override
    public int getRepeat() {
        return this.oiAbsData.getNWave();
    }

    /**
     * Return the optional expression to compute derived columns
     * @return optional expression to compute derived columns
     */
    public String getExpression() {
        return expression;
    }
}
