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

import fr.jmmc.jmcs.util.NumberUtils;
import fr.jmmc.oitools.image.FileRef;
import fr.jmmc.oitools.model.RuleFailure.RuleFailureField;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Simple RuleFailure comparators
 * @author bourgesl
 */
public final class RuleFailureComparator implements Comparator<RuleFailure> {

    /** singleton instances */
    public static final RuleFailureComparator BY_TABLE = new RuleFailureComparator(
            Arrays.asList(
                    RuleFailureField.FILE_REF,
                    RuleFailureField.EXT_NB,
                    RuleFailureField.RULE,
                    RuleFailureField.MEMBER
            )
    );

    /**
     * Default comparator
     */
    public static final RuleFailureComparator DEFAULT = BY_TABLE;

    /**
     * Comparator by Rule
     */
    public static final RuleFailureComparator BY_RULE = new RuleFailureComparator(
            Arrays.asList(
                    RuleFailureField.RULE,
                    RuleFailureField.FILE_REF,
                    RuleFailureField.EXT_NB,
                    RuleFailureField.MEMBER
            )
    );

    /**
     * Comparator by file
     */
    public static final Comparator<FileRef> CMP_FILE_REF = new Comparator<FileRef>() {
        @Override
        public int compare(final FileRef r1, final FileRef r2) {
            final String p1 = r1.getAbsoluteFilePath();
            final String p2 = r2.getAbsoluteFilePath();

            // Define null less than everything, except null.
            if ((p1 == null) && (p2 == null)) {
                return NumberUtils.compare(r1.getMemoryIndex(), r2.getMemoryIndex());
            }
            if (p1 == null) {
                return -1;
            }
            if (p2 == null) {
                return 1;
            }
            return String.CASE_INSENSITIVE_ORDER.compare(p1, p2);
        }
    };

    /**
     * Comparator by number
     */
    public static final Comparator<Integer> CMP_INT = new Comparator<Integer>() {
        @Override
        public int compare(final Integer i1, final Integer i2) {
            return NumberUtils.compare(i1, i2);
        }
    };

    // members:
    private final List<RuleFailureField> sortDirectives;

    /**
     * Set the value sortDirectives
     * @param sortDirectives List
     */
    public RuleFailureComparator(List<RuleFailureField> sortDirectives) {
        this.sortDirectives = sortDirectives;
    }

    /**
     * Get the list of value: sortDirectives
     * @return sortDirectives
     */
    public List<RuleFailureField> getSortDirectives() {
        return sortDirectives;
    }

    @Override
    public String toString() {
        return "RuleFailureComparator{" + "sortDirectives=" + sortDirectives + '}';
    }

    @Override
    @SuppressWarnings("unchecked")
    public int compare(final RuleFailure f1, final RuleFailure f2) {

        // @see fr.jmmc.sclgui.calibrator.TableSorter
        int cmp;
        Object o1, o2;

        for (int i = 0, len = sortDirectives.size(); i < len; i++) {
            final RuleFailureField field = sortDirectives.get(i);

            o1 = f1.getField(field);
            o2 = f2.getField(field);

            // Define null less than everything, except null.
            if ((o1 == null) && (o2 == null)) {
                cmp = 0;
            } else if (o1 == null) {
                cmp = -1;
            } else if (o2 == null) {
                cmp = 1;
            } else {
                cmp = getComparator(field).compare(o1, o2);
            }
            if (cmp != 0) {
                return cmp;
            }
        }

        return 0;
    }

    /**
     * Get the comparator according to the field
     * @param field RuleFailureField
     * @return the comparator according to the field
     */
    public Comparator getComparator(RuleFailureField field) {
        switch (field) {
            case RULE:
                return Rule.getComparatorByName();
            case FILE_REF:
                return CMP_FILE_REF;
            case EXTNAME:
                return String.CASE_INSENSITIVE_ORDER;
            case EXT_NB:
                return CMP_INT;
            case MEMBER:
                return String.CASE_INSENSITIVE_ORDER;
            case SEVERITY:
                return Severity.getComparatorByName();
            default:
                return null;
        }
    }

}
