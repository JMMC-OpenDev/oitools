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
package fr.jmmc.oitools.model.range;

import java.util.ArrayList;

/**
 * This interface defines the range factory pattern
 * 
 * @author Laurent BOURGES.
 */
public interface RangeFactory {

    /**
     * Return a Range instance with given minimum and maximum value
     * @param min minimum value
     * @param max maximum value
     * @return Range instance
     */
    public Range valueOf(final double min, final double max);

    /**
     * Return a List of Range
     * @return List of Range
     */
    public ArrayList<Range> getList();

    /**
     * Reset the factory state
     */
    public void reset();

    /**
     * Dump the factory statistics
     */
    public void dumpStats();
}
