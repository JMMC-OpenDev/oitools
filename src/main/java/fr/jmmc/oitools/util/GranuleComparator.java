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
package fr.jmmc.oitools.util;

import fr.jmmc.oitools.model.Granule;
import fr.jmmc.oitools.model.Granule.GranuleField;
import fr.jmmc.oitools.model.InstrumentMode;
import fr.jmmc.oitools.model.NightId;
import fr.jmmc.oitools.model.Target;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Granule comparator based on GranuleField values
 * @author bourgesl
 */
public final class GranuleComparator implements Comparator<Granule> {

    /** singleton instance */
    public static final GranuleComparator DEFAULT = new GranuleComparator(
            Arrays.asList(
                    GranuleField.TARGET,
                    GranuleField.INS_MODE,
                    GranuleField.NIGHT
            )
    );

    // members:
    private final List<GranuleField> sortDirectives;

    public GranuleComparator(List<GranuleField> sortDirectives) {
        this.sortDirectives = sortDirectives;
    }

    public List<GranuleField> getSortDirectives() {
        return sortDirectives;
    }

    @Override
    @SuppressWarnings("unchecked")
    public int compare(final Granule g1, final Granule g2) {

        // @see fr.jmmc.sclgui.calibrator.TableSorter
        int comparison;
        Object o1, o2;

        for (int i = 0, len = sortDirectives.size(); i < len; i++) {
            final GranuleField field = sortDirectives.get(i);

            o1 = g1.getField(field);
            o2 = g2.getField(field);

            // Define null less than everything, except null.
            if ((o1 == null) && (o2 == null)) {
                comparison = 0;
            } else if (o1 == null) {
                comparison = -1;
            } else if (o2 == null) {
                comparison = 1;
            } else {
                comparison = getComparator(field).compare(o1, o2);
            }
            if (comparison != 0) {
                return comparison;
            }
        }

        return 0;
    }

    public Comparator getComparator(GranuleField field) {
        switch (field) {
            case TARGET:
                return Target.CMP_TARGET;
            case INS_MODE:
                return InstrumentMode.CMP_INS_MODE;
            case NIGHT:
                return NightId.CMP_NIGHT;
            default:
                return null;
        }
    }

}
