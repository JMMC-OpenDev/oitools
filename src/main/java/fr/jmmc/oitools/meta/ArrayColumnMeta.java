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

/**
 * This specific ColumnMeta indicates this column is always giving an array (2D)
 * @author bourgesl
 */
public class ArrayColumnMeta extends ColumnMeta {

    /**
     * ArrayColumnMeta class constructor with the given cardinality
     *
     * @param name column name
     * @param desc column descriptive comment
     * @param dataType column data type
     * @param repeat column cardinality
     * @param optional true if column is optional
     */
    public ArrayColumnMeta(final String name, final String desc, final Types dataType, final int repeat, final boolean optional) {
        super(name, desc, dataType, repeat, optional);
    }

    /**
     * ArrayColumnMeta class constructor with the given cardinality and unit
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
    public ArrayColumnMeta(final String name, final String desc, final Types dataType, final int repeat, final boolean optional, final boolean is3D,
                           final String[] acceptedValues, final Units unit, final String errName, final DataRange dataRange) {
        super(name, desc, dataType, repeat, optional, is3D, acceptedValues, unit, errName, dataRange);
    }

}
