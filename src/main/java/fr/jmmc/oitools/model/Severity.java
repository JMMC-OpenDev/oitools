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

import java.util.Comparator;

/** 
 * OIFitsChecker severity
 * @author bourgesl, kempsc
 */
public enum Severity {

    Undefined("UNDEFINED"),
    Disabled("OFF"),
    Information("INFO"),
    Warning("WARNING"),
    Error("SEVERE");

    /* members */
    /** displayed label */
    private final String label;

    Severity(final String label) {
        this.label = label;
    }

    /**
     * Get the label
     * @return label
     */
    public String getLabel() {
        return this.label;
    }

    @Override
    public String toString() {
        return label;
    }

    /**
     * Method to sort on this Enum
     * @return Const.CMP_NAME
     */
    public static Comparator<Severity> getComparatorByName() {
        return Const.CMP_NAME;
    }

    private static final class Const {

        static Comparator<Severity> CMP_NAME = new Comparator<Severity>() {
            @Override
            public int compare(final Severity r1, final Severity r2) {
                return r1.name().compareTo(r2.name());
            }

        };

        private Const() {
            // no-op
        }
    }

}
