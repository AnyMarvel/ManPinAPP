package androidx.media.filterfw;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

class SimpleCache<K, V> extends LinkedHashMap<K, V> {
    private int mMaxEntries;

    public SimpleCache(int maxEntries) {
        super(maxEntries + 1, 1.0f, true);
        this.mMaxEntries = maxEntries;
    }

    protected boolean removeEldestEntry(Entry<K, V> entry) {
        return super.size() > this.mMaxEntries;
    }
}
