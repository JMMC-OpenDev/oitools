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
 * In OIFITS V2, the physical units for few columns are 'user-defined' given in the column description
 * WARNING: CustomUnits INSTANCES MUST NEVER BE SHARED among keyword / columns !
 *
 * @author kempsc
 */
public final class CustomUnits extends Units {

    /** flag indicating if the units is required */
    private final boolean required;

    /**
     * Public constructor
     * WARNING: CustomUnits INSTANCES MUST NEVER BE SHARED among keyword / columns !
     */
    public CustomUnits() {
        this(true);
    }

    /**
     * Public constructor
     * WARNING: CustomUnits INSTANCES MUST NEVER BE SHARED among keyword / columns !
     * @param required flag indicating if the units is required
     */
    public CustomUnits(final boolean required) {
        super("UNIT_CUSTOM", "");
        this.required = required;
    }

    /**
     * Define the value of unit 
     * @param unit other CustomUnits instance
     */
    public void set(final CustomUnits unit) {
        set(unit.getStandardRepresentation());
    }

    /**
     * Define the value of unit
     * @param unit unit in the file
     * @return this
     */
    public CustomUnits setRepresentation(final String unit) {
        set(unit);
        return this;
    }

    /**
     * True if the unit is required
     * @return true if the unit is required
     */
    public boolean isRequired() {
        return required;
    }

}
