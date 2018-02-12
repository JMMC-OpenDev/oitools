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

import fr.jmmc.oitools.meta.OIFitsStandard;

/**
 * The aim of a SeverityProfile consists in defining the severity of given failures
 * @author kempsc
 */
public abstract class SeverityProfile {

    /* members */
    /** profile name */
    private final String name;

    SeverityProfile(final String name) {
        this.name = name;
    }

    /**
     * Return the name of this SeverityProfile
     * @return name of this SeverityProfile
     */
    public final String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName();
    }

    /**
     * Set the severity for the given failure and OIFITS standard
     * @param failure failure to set the severity
     * @param std OIFITS standard
     */
    public abstract void defineSeverity(final RuleFailure failure, final OIFitsStandard std);

}
