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
import java.util.Comparator;

/**
 * OIFitsFile comparator based on file name
 * @author bourgesl
 */
public final class OIFitsFileComparator implements Comparator<OIFitsFile> {

    /** singleton instance */
    public static final OIFitsFileComparator INSTANCE = new OIFitsFileComparator();

    private OIFitsFileComparator() {
        // private constructor
    }

    @Override
    public int compare(final OIFitsFile o1, final OIFitsFile o2) {
        final String filename1 = (o1 != null) ? o1.getFileName() : "";
        final String filename2 = (o2 != null) ? o2.getFileName() : "";
        return String.CASE_INSENSITIVE_ORDER.compare(filename1, filename2);
    }

}
