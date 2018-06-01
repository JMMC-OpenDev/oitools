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
package fr.jmmc.oitools.processing;

import fr.jmmc.oitools.model.OIData;
import java.util.HashMap;
import java.util.Map;

/**
 * Basic selector
 * TODO: enhance
 */
public final class Selector {

    public static final String INSTRUMENT_FILTER = "INSNAME_PATTERN";
    private final Map<String, String> patterns = new HashMap<String, String>();

    public void addPattern(String name, String value) {
        patterns.put(name, value);
    }

    private String getPattern(String name) {
        return patterns.get(name);
    }

    public boolean match(final OIData oiData) {
        final String patternInsname = getPattern(INSTRUMENT_FILTER);
        // TODO: support regexp ... ?
        return (patternInsname == null) || patternInsname.equals(oiData.getInsName());
    }

}
