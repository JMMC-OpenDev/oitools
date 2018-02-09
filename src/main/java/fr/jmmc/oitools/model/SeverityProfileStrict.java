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
 * Implementation for the severe profile
 * @author kempsc
 */
public final class SeverityProfileStrict implements SeverityProfile {

    static final SeverityProfile INSTANCE = new SeverityProfileStrict();

    @Override
    public void defineSeverity(final RuleFailure failure, final OIFitsStandard std) {
        failure.setSeverity(Severity.Error);
    }

    @Override
    public String toString() {
        return "SeverityProfileStrict";
    }

}
