/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.model;

import java.util.Arrays;
import java.util.Collection;

/**
 * This generic class matches integer values especially for nightIds values.
 * @author bourgesl
 */
public final class NightIdMatcher {

    /* members */
    private final int single;
    private final int[] ids;

    public NightIdMatcher(final NightId nightId) {
        single = nightId.getNightId();
        ids = null;
    }

    public NightIdMatcher(final Collection<NightId> nightIds) {
        if (nightIds.size() == 1) {
            single = nightIds.iterator().next().getNightId();
            ids = null;
        } else {
            final int len = nightIds.size();
            single = -1;
            ids = new int[len];
            int i = 0;
            for (NightId id : nightIds) {
                ids[i++] = id.getNightId();
            }
        }
    }

    public boolean match(final int id) {
        final int[] a = ids; // local var
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

    public boolean matchAll(final Collection<NightId> nightIds) {
        for (NightId id : nightIds) {
            if (!match(id.getNightId())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "NightIdMatcher{single=" + single + ", ids=" + Arrays.toString(ids) + '}';
    }

}
