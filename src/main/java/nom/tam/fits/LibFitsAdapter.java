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
 * along with this program.  If not, see http://www.gnu.org/licenses/>.
 */
package nom.tam.fits;

/**
 *
 * @author jammetv
 */
public class LibFitsAdapter {

    /**
     * Get the type of a column in the table.
     *
     * // LAURENT : added method
     *
     * @param table owning the column
     * @param index The 0-based column index.
     * @return The type char representing the FITS type or 0 if undefined or an invalid index was requested.
     * @exception FitsException if an invalid index was requested.
     */
    public static final char getColumnType(final BinaryTableHDU table, final int index)
            throws FitsException {

        final String tform = table.getColumnFormat(index);
        if (tform != null) {
            return table.getData().getTFORMType(tform);
        }
        return 0;
    }

    /**
     * Get the type of a varying length column in the table.
     *
     * @param table owning the column
     * @param index The 0-based column index.
     * @return The type char representing the FITS type or 0 if undefined or an invalid index was requested.
     * @exception FitsException if an invalid index was requested.
     */
    public static final char getColumnVarType(final BinaryTableHDU table, final int index)
            throws FitsException {

        final String tform = table.getColumnFormat(index);
        if (tform != null) {
            return table.getData().getTFORMVarType(tform);
        }
        return 0;
    }

    /**
     * Get the explicit or implied length of a column in the table.
     *
     * @param table owning the column
     * @param index The 0-based column index.
     * @return The explicit or implied length or 0 if undefined or an invalid index was requested.
     * @exception FitsException if an invalid index was requested.
     */
    public static final int getColumnLength(final BinaryTableHDU table, final int index)
            throws FitsException {

        final String tform = table.getColumnFormat(index);
        if (tform != null) {
            return table.getData().getTFORMLength(tform);
        }
        return 0;
    }

    /**
     * Get the dimensions of a column in the table or null if TDIM keyword is not present
     *
     * // LAURENT : added method
     *
     * @param table owning the column
     * @param index The 0-based column index.
     * @return the dimensions of a column of null
     */
    public static final int[] getColumnDimensions(final BinaryTableHDU table, final int index) {
        final String tdims = table.getColumnMeta(index, "TDIM");
        if (tdims != null) {
            return BinaryTable.getTDims(tdims);
        }
        return null;
    }

    /**
     * Get the unit of a column in the table.
     *
     * @param table owning the column
     * @param index The 0-based column index.
     * @return The unit of a column or null if undefined or an invalid index was requested.
     */
    public static final String getColumnUnit(final TableHDU table, final int index) {
        return table.getColumnMeta(index, "TUNIT");
    }

    /*
    * Set the name and unit of a column in the table.
    *
    * @param index The 0-based column index.
    * @param name column name
    * @param comment description of the column
    * @param unit column unit
    * @throws FitsException
     */
    public static void setColumnName(
            final TableHDU table, final int index, final String name, final String comment, final String unit)
            throws FitsException {
        table.setColumnName(index, name, comment);
        if (unit != null && unit.length() > 0) {
            // Insert TUNIT keyword after TFORM
            table.setColumnMeta(index, "TUNIT", unit, "ntf::tablehdu:tunit|" + (index + 1), true);
        }
    }

    /**
     * Get the trimmed <CODE>String</CODE> value associated with the given key.
     *
     * @param header owning the value
     * @param keyword the FITS keyword
     * @return either <CODE>null</CODE> or a String with leading/trailing blanks stripped.
     */
    public static final String getTrimmedStringValue(final Header header, final String keyword) {
        final String s = header.getStringValue(keyword);
        if (s != null) {
            return s.trim();
        }
        return null;
    }

    /*
     * ENHANCEMENT : LAURENT :
     *
     * Get the raw <CODE>String</CODE> value
     *
     * @param keyword	the FITS keyword
     * @return either <CODE>null</CODE> or a String
     */
    public static String getValue(final Header header, final String keyword) {
        final HeaderCard fcard = header.findCard(keyword);
        if (fcard == null) {
            return null;
        }

        return fcard.getValue();
    }

}
