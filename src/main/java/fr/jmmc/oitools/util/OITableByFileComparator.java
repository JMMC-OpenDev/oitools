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
package fr.jmmc.oitools.util;

import fr.jmmc.oitools.model.OIFitsFile;
import fr.jmmc.oitools.model.OITable;
import java.util.Comparator;

/**
 * OITable comparator based on their file
 * @author bourgesl
 */
public final class OITableByFileComparator implements Comparator<OITable> {

    /** singleton instance */
    public static final OITableByFileComparator INSTANCE = new OITableByFileComparator();

    private OITableByFileComparator() {
        // private constructor
    }

    @Override
    public int compare(final OITable t1, final OITable t2) {
        return String.CASE_INSENSITIVE_ORDER.compare(getFileName(t1), getFileName(t2));
    }

    public static String getFileName(final OITable t) {
        if (t != null) {
            final OIFitsFile oiFitsFile = t.getOIFitsFile();
            if (oiFitsFile != null) {
                return oiFitsFile.getFileName();
            }
        }
        return "[Undefined]";
    }
}
