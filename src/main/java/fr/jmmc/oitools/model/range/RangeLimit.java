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

/**
 * Utility class used by intersection and merge algorithms
 * 
 * @author Laurent BOURGES.
 */
public final class RangeLimit {

    /** position of the limit */
    double position;
    /** int value to indicate the start [+1] or end of the initial range [-1] */
    int flag;

    /**
     * Create a RangeLimit array filled with empty RangeLimit instances
     * @param length array length
     * @return RangeLimit array filled with empty RangeLimit instances
     */
    public static RangeLimit[] createArray(final int length) {
        final RangeLimit[] array = new RangeLimit[length];
        for (int i = 0; i < length; i++) {
            array[i] = new RangeLimit();
        }
        return array;
    }

    /**
     * Empty Constructor
     */
    private RangeLimit() {
    }

    /**
     * Constructor with given position and flag
     * @param position position of the limit
     * @param flag int value to indicate the start [+1] or end of the initial range [-1]
     */
    protected RangeLimit(final double position, final int flag) {
        this.position = position;
        this.flag = flag;
    }

    /**
     * set the given position and flag from the given range limit
     * @param source range limit to copy
     */
    public void set(final RangeLimit source) {
        set(source.position, source.flag);
    }

    /**
     * set the given position and flag
     * @param position position of the limit
     * @param flag int value to indicate the start [+1] or end of the initial range [-1]
     */
    public void set(final double position, final int flag) {
        this.position = position;
        this.flag = flag;
    }
}
