package com.jivesoftware.os.jive.utils.map.store;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Extends the least-recently-accessed capabilities of {@link LinkedHashMap}. Like its parent, this map needs
 * to be synchronized externally.
 * @param <A>
 * @param <B>
 */
public class LruMap<A, B> extends LinkedHashMap<A, B> {

    private final int maxEntries;

    public LruMap(final int maxEntries) {
        super(maxEntries + 1, 0.75f, true);
        this.maxEntries = maxEntries;
    }

    @Override
    protected boolean removeEldestEntry(final Map.Entry<A, B> eldest) {
        return size() > maxEntries;
    }
}
