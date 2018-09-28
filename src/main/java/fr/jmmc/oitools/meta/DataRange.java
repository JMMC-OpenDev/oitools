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
 * This range class represents default range for any column data
 * @author bourgesl
 */
public final class DataRange {

    /** data range representing strictly positive numbers [0; +Inf] ie invalid values are replaced by NaN (see OIFits checker) */
    public final static DataRange RANGE_POSITIVE_STRICT = new DataRange("RANGE_POSITIVE_STRICT", 0d, Double.NaN);
    /** data range representing positive numbers [0; +Inf] but non strict */
    public final static DataRange RANGE_POSITIVE = new DataRange("RANGE_POSITIVE", 0d, Double.NaN);
    /** data range representing angle range [-180; 180] with margins = [-200; 200] */
    public final static DataRange RANGE_ANGLE = new DataRange("RANGE_ANGLE", -200.0, 200.0);
    /** data range representing visibilities [0; 1] with margins = [-0.1; 1.1] */
    public final static DataRange RANGE_VIS = new DataRange("RANGE_VIS", -0.1, 1.1);
    /** data range representing sigma [-5; 5] */
    public final static DataRange RANGE_SIGMA = new DataRange("RANGE_SIGMA", -5.0, 5.0);

    /* members */
    /** range name */
    private final String name;
    /** min value (may be NaN to indicate no min value) */
    private final double min;
    /** max value (may be NaN to indicate no max value) */
    private final double max;

    /**
     * Public constructor
     * @param name range name
     * @param min min value
     * @param max max value
     */
    public DataRange(final String name, final double min, final double max) {
        this.name = name;
        this.min = min;
        this.max = max;
    }

    /**
     * Get the range name
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Return the min value
     * @return min value
     */
    public double getMin() {
        return min;
    }

    /**
     * Return the max value
     * @return max value
     */
    public double getMax() {
        return max;
    }

    @Override
    public String toString() {
        return "DataRange{" + "name=" + name + "min=" + min + ", max=" + max + '}';
    }
}
