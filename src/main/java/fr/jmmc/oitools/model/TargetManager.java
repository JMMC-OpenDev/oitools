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

import java.util.List;

/**
 *
 * @author bourgesl
 */
public final class TargetManager extends AbstractMapper<Target> {

    /** flag to prefer matcher by name */
    private final static boolean PREFER_MATCHER_NAME;

    static {
        PREFER_MATCHER_NAME = System.getProperty("target.matcher.name", "false").equalsIgnoreCase("true");

        if (PREFER_MATCHER_NAME) {
            logger.warning("TargetManager: Using matcher by name !");
        }
    }

    public static TargetManager newInstance() {
        if (PREFER_MATCHER_NAME) {
            return new TargetManager(Target.MATCHER_NAME); // based on target name only
        }
        return new TargetManager(Target.MATCHER_LIKE); // based on distance matching
    }

    private TargetManager(final Matcher<Target> matcher) {
        super(matcher);
    }

    /**
     * Clear the mappings
     */
    @Override
    public void clear() {
        super.clear();
        // insert mapping for Undefined:
        register(Target.UNDEFINED);
    }

    @Override
    protected Target createGlobal(final Target local, final String uid) {
        return new Target(local, uid);
    }

    @Override
    protected String getName(final Target src) {
        return src.getTarget();
    }

    @Override
    public final List<Target> getGlobals() {
        return getGlobals(Target.CMP_TARGET);
    }

}
