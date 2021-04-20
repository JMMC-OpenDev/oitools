/*
 * Copyright (C) 2021 CNRS - JMMC project ( http://www.jmmc.fr )
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

public final class StaNamesDir {

    private final String staNames;
    private final boolean orientation;

    public StaNamesDir(final String staNames, final boolean orientation) {
        this.staNames = staNames;
        this.orientation = orientation;
    }

    public String getStaNames() {
        return staNames;
    }

    public boolean isOrientation() {
        return orientation;
    }

    @Override
    public String toString() {
        return "StaNamesDir{staNames=" + staNames + ", orientation=" + orientation + '}';
    }
}
