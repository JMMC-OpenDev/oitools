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
package fr.jmmc.oitools.model;

public final class InstrumentNameMatcher implements Matcher<String> {

    private final String insName;
    private final boolean startsWith;

    public InstrumentNameMatcher(final String insName, final boolean startsWith) {
        this.insName = insName;
        this.startsWith = startsWith;
    }

    public String getInsName() {
        return insName;
    }

    public boolean isStartsWith() {
        return startsWith;
    }

    public boolean match(final String other) {
        return match(this.insName, other);
    }

    @Override
    public boolean match(final String src, final String other) {
        if (src == null || other == null) {
            return false;
        }
        if (startsWith) {
            return other.startsWith(src);
        }
        // Compare only NAME values:
        return src.equalsIgnoreCase(other);
    }
}
