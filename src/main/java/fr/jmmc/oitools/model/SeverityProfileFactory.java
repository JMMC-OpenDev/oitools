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
package fr.jmmc.oitools.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple factory for SeverityProfile instances
 * @author bourgesl
 */
public final class SeverityProfileFactory {

    private static final SeverityProfileFactory INSTANCE = new SeverityProfileFactory();

    public static final String PROFILE_JMMC = "JMMC";

    /**
     * Get the factory singleton
     * @return factory singleton
     */
    public static SeverityProfileFactory getInstance() {
        return INSTANCE;
    }

    /* members */
    /** profiles keyed by name */
    private final Map<String, SeverityProfile> profiles = new HashMap<String, SeverityProfile>(8);

    private SeverityProfileFactory() {
        initialize();
    }

    private void initialize() {
        addProfile(new SeverityProfileConfigurable(PROFILE_JMMC));
        addProfile(new SeverityProfileBasic("Strict", Severity.Error));
        addProfile(new SeverityProfileBasic("Lenient", Severity.Information));
    }

    /**
     * Return the collection of profile names
     * @return collection of profile names
     */
    public Collection<String> getProfileNames() {
        return profiles.keySet();
    }

    /**
     * Return the profile given its name or null if unknown
     * @param name profile name
     * @return profile given its name or null if unknown
     */
    public SeverityProfile getProfile(final String name) {
        return profiles.get(name);
    }

    private void addProfile(SeverityProfile profile) {
        profiles.put(profile.getName(), profile);
    }
}
