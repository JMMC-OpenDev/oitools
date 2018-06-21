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

import fr.jmmc.jmcs.util.NumberUtils;
import java.util.Comparator;

/**
 * Custom comparator for baselines and configurations
 * @author bourgesl
 */
public final class StationNamesComparator implements Comparator<String> {

    /** singleton instance */
    public static final StationNamesComparator INSTANCE = new StationNamesComparator();

    private StationNamesComparator() {
        // private constructor
    }

    @Override
    public int compare(final String s1, final String s2) {
        int cmp = NumberUtils.compare(s1.length(), s2.length());
        if (cmp == 0) {
            cmp = s1.compareTo(s2);
        }
        return cmp;
    }
}
