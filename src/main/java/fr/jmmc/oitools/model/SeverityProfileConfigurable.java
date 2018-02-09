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

import fr.jmmc.jmcs.util.ResourceUtils;
import fr.jmmc.oitools.meta.OIFitsStandard;

/**
 * Implementation for the configurable profile (ascii config file)
 * @author kempsc
 */
public final class SeverityProfileConfigurable implements SeverityProfile {

    // TODO: use a factory or automatically lookup profiles in the CONF_CLASSLOADER_PATH folder
    static final SeverityProfile JMMC = new SeverityProfileConfigurable("JMMC");

    /** classloader path to configuration files */
    public static final String CONF_CLASSLOADER_PATH = "fr/jmmc/oitools/resource/";

    /* members */
    /** profile name */
    private final String name;

    public SeverityProfileConfigurable(final String name) {
        this.name = name;

        loadConfig();
    }

    @Override
    public void defineSeverity(final RuleFailure failure, final OIFitsStandard std) {
        failure.setSeverity(Severity.Error);
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "SeverityProfile" + name;
    }

    private void loadConfig() {
        final String fileName = "profile_" + name + ".conf";

        // use the class loader resource resolver
        final String config = ResourceUtils.readResource(CONF_CLASSLOADER_PATH + fileName);

        System.out.println("config: " + config);
    }

}
