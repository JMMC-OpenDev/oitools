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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

/**
 * Abstract Mapper between global and local object instances
 * @author bourgesl
 * @param <K> generic type 
 */
public abstract class AbstractMapper<K> {

    /** Logger */
    private final static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(AbstractMapper.class.getName());

    /* members */
    /** matcher in use */
    protected final Matcher<K> matcher;
    /** global uids */
    protected final Map<String, K> globalUids = new HashMap<String, K>();
    /** global item keyed by local item */
    protected final Map<K, K> globalPerLocal = new IdentityHashMap<K, K>();
    /** local items keyed by global item */
    protected final Map<K, List<K>> localsPerGlobal = new IdentityHashMap<K, List<K>>();

    protected AbstractMapper(final Matcher<K> matcher) {
        this.matcher = matcher;
    }

    /**
     * Clear the mappings
     * May be overriden
     */
    public void clear() {
        globalUids.clear();
        // clear insMode mappings:
        globalPerLocal.clear();
        localsPerGlobal.clear();
    }

    public final void dump() {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "UIDs: {0}", globalUids);
            logger.log(Level.FINE, "Globals: {0}", getGlobals());
            logger.log(Level.FINE, "Locals:  {0}", globalPerLocal.keySet());
            logger.log(Level.FINE, "Globals <=> Locals mapping: {0}", localsPerGlobal);
        }
    }

    public final void register(final K local) {
        if (local != null) {
            K match = null;
            for (K global : localsPerGlobal.keySet()) {
                if (matcher.match(global, local)) {
                    match = global;
                    break;
                }
            }

            final List<K> locals;
            if (match == null) {
                // Generate UID:
                final String uid = generateUid(getName(local));
                // Create global (clone):
                match = createGlobal(local, uid);

                // insert mapping (uid -> global)
                globalUids.put(uid, match);

                locals = new ArrayList<K>(2);
                localsPerGlobal.put(match, locals);
            } else {
                locals = localsPerGlobal.get(match);
            }

            // anyway
            globalPerLocal.put(local, match);

            // ensure unicity (Undefined for example):
            if (!containsInstance(locals, local)) {
                locals.add(local);
            }
        }
    }

    private String generateUid(final String name) {
        String newName = name;
        int idx = 0;

        while (globalUids.get(newName) != null) {
            idx++;
            newName = name + "_" + idx;
        }
        return newName;
    }

    public final List<K> getGlobals(Comparator<K> comparator) {
        final List<K> globals = new ArrayList<K>(localsPerGlobal.keySet());
        Collections.sort(globals, comparator);
        return globals;
    }

    public final K getGlobalByUID(final String uid) {
        return globalUids.get(uid);
    }

    public final K getGlobal(final K local) {
        return globalPerLocal.get(local);
    }

    public final boolean hasLocal(final K global) {
        return getLocals(global) != null;
    }

    public final List<K> getLocals(final K global) {
        return localsPerGlobal.get(global);
    }

    public final List<String> getSortedUniqueAliases(final K global) {
        List<String> results = null;

        final List<K> locals = getLocals(global);
        if (locals != null) {
            final int len = locals.size();
            final String main = getName(global);

            if (len == 1) {
                final String local = getName(locals.get(0));
                if (!main.equals(local)) {
                    return Arrays.asList(new String[]{local});
                }
            } else if (len > 1) {
                final Set<String> aliases = new HashSet<String>();
                // always put global name:
                aliases.add(main);
                for (int j = 0; j < len; j++) {
                    aliases.add(getName(locals.get(j)));
                }
                // remove global name:
                aliases.remove(main);

                if (aliases.size() > 1) {
                    results = new ArrayList<String>(aliases);
                    Collections.sort(results);
                }
            }
        }
        return results;
    }

    protected abstract K createGlobal(final K local, final String uid);

    protected abstract String getName(final K src);

    protected abstract List<K> getGlobals();

    private static <K> boolean containsInstance(final List<K> list, final K value) {
        final int len = list.size();
        for (int i = 0; i < len; i++) {
            // identity comparison:
            if (value == list.get(i)) {
                return true;
            }
        }
        return false;
    }
}
