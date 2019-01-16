/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.model;

import java.util.Arrays;
import java.util.Collection;

/**
 * This generic class matches short values especially for targetIds values.
 * @author bourgesl
 */
public final class TargetIdMatcher {

    /* members */
    private final short single;
    private final short[] ids;

    TargetIdMatcher(final Collection<Short> uids) {
        if (uids.size() == 1) {
            single = uids.iterator().next().shortValue();
            ids = null;
        } else {
            final int len = uids.size();
            single = -1;
            ids = new short[len];
            int i = 0;
            for (Short id : uids) {
                ids[i++] = id.shortValue();
            }
        }
    }

    public boolean match(final short id) {
        final short[] a = ids; // local var
        if (a == null) {
            return (single == id);
        } else {
            for (int i = 0; i < a.length; i++) {
                if (a[i] == id) {
                    return true;
                }
            }
            return false;
        }
    }

    public boolean matchAll(final Collection<Short> ids) {
        for (Short id : ids) {
            if (!match(id.shortValue())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "TargetIdMatcher{single=" + single + ", ids=" + Arrays.toString(ids) + '}';
    }

}
