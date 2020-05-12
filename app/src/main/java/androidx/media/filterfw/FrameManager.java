package androidx.media.filterfw;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import androidx.media.filterfw.BackingStore.*;

public class FrameManager {
    public static final int DEFAULT_MAX_CACHE_SIZE = 12582912;
    public static final int FRAME_CACHE_LFU = 2;
    public static final int FRAME_CACHE_LRU = 1;
    public static final int FRAME_CACHE_NONE = 0;
    public static final int SLOT_FLAGS_NONE = 0;
    public static final int SLOT_FLAG_STICKY = 1;
    private Set<Backing> mBackings = new HashSet();
    private BackingCache mCache;
    private Map<String, FrameSlot> mFrameSlots = new HashMap();
    private GraphRunner mRunner;

    private static abstract class BackingCache {
        protected int mCacheMaxSize;

        public abstract boolean cacheBacking(Backing backing);

        public abstract void clear();

        public abstract Backing fetchBacking(int i, int i2, int[] iArr, int i3);

        public abstract int getSizeLeft();

        private BackingCache() {
            this.mCacheMaxSize = FrameManager.DEFAULT_MAX_CACHE_SIZE;
        }

        public void setSize(int size) {
            this.mCacheMaxSize = size;
        }

        public int getSize() {
            return this.mCacheMaxSize;
        }
    }

    static class FrameSlot {
        private int mFlags;
        private Frame mFrame = null;
        private FrameType mType;

        public FrameSlot(FrameType type, int flags) {
            this.mType = type;
            this.mFlags = flags;
        }

        public FrameType getType() {
            return this.mType;
        }

        public boolean hasFrame() {
            return this.mFrame != null;
        }

        public void releaseFrame() {
            if (this.mFrame != null) {
                this.mFrame.release();
                this.mFrame = null;
            }
        }

        public void assignFrame(Frame frame) {
            Frame oldFrame = this.mFrame;
            this.mFrame = frame.retain();
            if (oldFrame != null) {
                oldFrame.release();
            }
        }

        public Frame getFrame() {
            Frame result = this.mFrame.retain();
            if ((this.mFlags & 1) == 0) {
                releaseFrame();
            }
            return result;
        }

        public void markWritable() {
            if (this.mFrame != null) {
                this.mFrame.setReadOnly(false);
            }
        }
    }

    private static class BackingCacheNone extends BackingCache {
        private BackingCacheNone() {
            super();
        }

        public Backing fetchBacking(int mode, int access, int[] dimensions, int elemSize) {
            return null;
        }

        public boolean cacheBacking(Backing backing) {
            return false;
        }

        public void clear() {
        }

        public int getSize() {
            return 0;
        }

        public int getSizeLeft() {
            return 0;
        }
    }

    private static abstract class PriorityBackingCache extends BackingCache {
        private PriorityQueue<Backing> mQueue = new PriorityQueue(4, new Comparator<Backing>() {
            public int compare(Backing left, Backing right) {
                return left.cachePriority - right.cachePriority;
            }
        });
        private int mSize = 0;

        protected abstract void onCacheBacking(Backing backing);

        protected abstract void onFetchBacking(Backing backing);

        public PriorityBackingCache() {
            super();
        }

        public Backing fetchBacking(int mode, int access, int[] dimensions, int elemSize) {
            Iterator it = this.mQueue.iterator();
            while (it.hasNext()) {
                int backingAccess;
                Backing backing = (Backing) it.next();
                if (mode == 2) {
                    backingAccess = backing.writeAccess();
                } else {
                    backingAccess = backing.readAccess();
                }
                if ((backingAccess & access) == access && FrameManager.dimensionsCompatible(backing.getDimensions(), dimensions) && elemSize == backing.getElementSize()) {
                    this.mQueue.remove(backing);
                    this.mSize -= backing.getSize();
                    onFetchBacking(backing);
                    return backing;
                }
            }
            return null;
        }

        public boolean cacheBacking(Backing backing) {
            if (!reserve(backing.getSize())) {
                return false;
            }
            onCacheBacking(backing);
            this.mQueue.add(backing);
            return true;
        }

        public void clear() {
            this.mQueue.clear();
            this.mSize = 0;
        }

        public int getSizeLeft() {
            return this.mCacheMaxSize - this.mSize;
        }

        private boolean reserve(int size) {
            if (size > this.mCacheMaxSize) {
                return false;
            }
            this.mSize += size;
            while (this.mSize > this.mCacheMaxSize) {
                Backing dropped = (Backing) this.mQueue.poll();
                this.mSize -= dropped.getSize();
                dropped.destroy();
            }
            return true;
        }
    }

    private static class BackingCacheLfu extends PriorityBackingCache {
        private BackingCacheLfu() {
        }

        protected void onCacheBacking(Backing backing) {
            backing.cachePriority = 0;
        }

        protected void onFetchBacking(Backing backing) {
            backing.cachePriority++;
        }
    }

    private static class BackingCacheLru extends PriorityBackingCache {
        private int mTimestamp;

        private BackingCacheLru() {
            this.mTimestamp = 0;
        }

        protected void onCacheBacking(Backing backing) {
            backing.cachePriority = 0;
        }

        protected void onFetchBacking(Backing backing) {
            this.mTimestamp++;
            backing.cachePriority = this.mTimestamp;
        }
    }

    public static FrameManager current() {
        GraphRunner runner = GraphRunner.current();
        return runner != null ? runner.getFrameManager() : null;
    }

    public MffContext getContext() {
        return this.mRunner.getContext();
    }

    public GraphRunner getRunner() {
        return this.mRunner;
    }

    public void setCacheSize(int bytes) {
        this.mCache.setSize(bytes);
    }

    public int getCacheSize() {
        return this.mCache.getSize();
    }

    public Frame importFrame(Frame frame) {
        if (frame.isReadOnly()) {
            return frame.makeCpuCopy(this);
        }
        String valueOf = String.valueOf(frame);
        throw new IllegalArgumentException(new StringBuilder(String.valueOf(valueOf).length() + 61).append("Frame ").append(valueOf).append(" must be read-only to import into another FrameManager!").toString());
    }

    public void addFrameSlot(String name, FrameType type, int flags) {
        assertNotRunning();
        if (((FrameSlot) this.mFrameSlots.get(name)) != null) {
            removeFrameSlot(name);
        }
        this.mFrameSlots.put(name, new FrameSlot(type, flags));
    }

    public void removeFrameSlot(String name) {
        assertNotRunning();
        getSlot(name).releaseFrame();
        this.mFrameSlots.remove(name);
    }

    public void storeFrame(Frame frame, String slotName) {
        assertInGraphRun();
        getSlot(slotName).assignFrame(frame);
    }

    public Frame fetchFrame(String slotName) {
        assertInGraphRun();
        return getSlot(slotName).getFrame();
    }

    public void clearCache() {
        this.mCache.clear();
    }

    FrameManager(GraphRunner runner, int cacheType) {
        this.mRunner = runner;
        switch (cacheType) {
            case 0:
                this.mCache = new BackingCacheNone();
                return;
            case 1:
                this.mCache = new BackingCacheLru();
                return;
            case 2:
                this.mCache = new BackingCacheLfu();
                return;
            default:
                throw new IllegalArgumentException("Unknown cache-type " + cacheType + "!");
        }
    }

    Backing fetchBacking(int mode, int access, int[] dimensions, int elemSize) {
        return this.mCache.fetchBacking(mode, access, dimensions, elemSize);
    }

    void onBackingCreated(Backing backing) {
        if (backing != null) {
            this.mBackings.add(backing);
        }
    }

    void onBackingAvailable(Backing backing) {
        if (!backing.shouldCache() || !this.mCache.cacheBacking(backing)) {
            backing.destroy();
            this.mBackings.remove(backing);
        }
    }

    void destroyBackings() {
        for (Backing backing : this.mBackings) {
            backing.destroy();
        }
        this.mBackings.clear();
        this.mCache.clear();
    }

    FrameSlot getSlot(String name) {
        FrameSlot slot = (FrameSlot) this.mFrameSlots.get(name);
        if (slot != null) {
            return slot;
        }
        throw new IllegalArgumentException(new StringBuilder(String.valueOf(name).length() + 22).append("Unknown frame slot '").append(name).append("'!").toString());
    }

    void onBeginRun() {
        for (FrameSlot slot : this.mFrameSlots.values()) {
            slot.markWritable();
        }
    }

    private static boolean dimensionsCompatible(int[] dimA, int[] dimB) {
        return dimA == null || dimB == null || Arrays.equals(dimA, dimB);
    }

    private void assertNotRunning() {
        if (this.mRunner.isRunning()) {
            throw new IllegalStateException("Attempting to modify FrameManager while graph is running!");
        }
    }

    private void assertInGraphRun() {
        if (!this.mRunner.isRunning() || GraphRunner.current() != this.mRunner) {
            throw new IllegalStateException("Attempting to access FrameManager Frame data outside of graph run-loop!");
        }
    }
}
