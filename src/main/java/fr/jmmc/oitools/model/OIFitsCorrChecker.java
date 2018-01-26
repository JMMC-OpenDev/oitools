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

import fr.jmmc.oitools.fits.FitsHDU;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO: Fix row/channel to be 1-based (not 0-based)
 * @author kempsc
 */
public final class OIFitsCorrChecker {

    /** Map indexOrigins */
    private final Map<Integer, Origin> indexOrigins = new HashMap<Integer, Origin>();

    /**
     * Return true if the map contains key at this index
     * @param index index
     * @return true if the map contains key at this index
     */
    public boolean contains(final Integer index) {
        return indexOrigins.containsKey(index);
    }

    /**
     * Puts in the map at the given index, the new object Origin with OIData information
     * @param index index
     * @param extName Hdu extname
     * @param extNb Hdu extnumber
     * @param column column name
     * @param row row
     * @param channel channel
     */
    public void put(final Integer index, String extName, int extNb, String column, int row, int channel) {
        indexOrigins.put(index, new Origin(extName, extNb, column, row, channel));
    }

    /**
     * Origin display call asString in Origin object
     * @param index index
     * @return the map information at this index as a String
     */
    public String getOriginAsString(final Integer index) {
        return indexOrigins.get(index).asString();
    }

    // Classic toString call Origin toString
    @Override
    public String toString() {
        return "CorrChecker{" + "usedIndexes=" + indexOrigins + '}';
    }

    //Origin object for store OIData information for an index
    private final static class Origin {

        String extName;
        int extNb;
        String column;
        int row;
        int channel;

        Origin(String extName, int extNb, String column, int row, int channel) {
            this.extName = extName;
            this.extNb = extNb;
            this.column = column;
            this.row = row;
            this.channel = channel;
        }

        @Override
        public String toString() {
            return "Origin{" + "extName=" + extName + ", extNb=" + extNb + ", column=" + column + ", row=" + row + ", channel=" + channel + '}';
        }

        private String asString() {
            return FitsHDU.getHDUId(extName, extNb) + " in " + column + " column at index " + channel + ", row " + row;
        }

    }
}
