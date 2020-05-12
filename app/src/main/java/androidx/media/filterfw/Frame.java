package androidx.media.filterfw;

import java.util.Arrays;

public class Frame {
    public static final int MODE_READ = 1;
    public static final int MODE_WRITE = 2;
    public static final long TIMESTAMP_END_OF_STREAM = -2;
    public static final long TIMESTAMP_NOT_SET = -1;
    BackingStore mBackingStore;
    boolean mReadOnly = false;

    public final FrameType getType() {
        return this.mBackingStore.getFrameType();
    }

    public final int getElementCount() {
        return this.mBackingStore.getElementCount();
    }

    public final void setTimestamp(long timestamp) {
        this.mBackingStore.setTimestamp(timestamp);
    }

    public final long getTimestamp() {
        return this.mBackingStore.getTimestamp();
    }

    public final long getTimestampMillis() {
        return this.mBackingStore.getTimestamp() / 1000000;
    }

    public final boolean isReadOnly() {
        return this.mReadOnly;
    }

    public final FrameValue asFrameValue() {
        return FrameValue.create(this.mBackingStore);
    }

    public final FrameValues asFrameValues() {
        return FrameValues.create(this.mBackingStore);
    }

    public final FrameBuffer1D asFrameBuffer1D() {
        return FrameBuffer1D.create(this.mBackingStore);
    }

    public final FrameBuffer2D asFrameBuffer2D() {
        return FrameBuffer2D.create(this.mBackingStore);
    }

    public final FrameImage2D asFrameImage2D() {
        return FrameImage2D.create(this.mBackingStore);
    }

    public String toString() {
        String frameType = getType().toString();
        String valueOf = String.valueOf(this.mBackingStore);
        return new StringBuilder((String.valueOf(frameType).length() + 9) + String.valueOf(valueOf).length()).append("Frame[").append(frameType).append("]: ").append(valueOf).toString();
    }

    public boolean equals(Object object) {
        return (object instanceof Frame) && ((Frame) object).mBackingStore == this.mBackingStore;
    }

    public static Frame create(FrameType type, int[] dimensions) {
        FrameManager manager = FrameManager.current();
        if (manager != null) {
            return new Frame(type, dimensions, manager);
        }
        throw new IllegalStateException("Attempting to create new Frame outside of FrameManager context!");
    }

    public final Frame release() {
        this.mBackingStore = this.mBackingStore.release();
        return this.mBackingStore != null ? this : null;
    }

    public final Frame retain() {
        this.mBackingStore = this.mBackingStore.retain();
        return this;
    }

    public void unlock() {
        if (!this.mBackingStore.unlock()) {
            throw new RuntimeException("Attempting to unlock frame that is not locked!");
        }
    }

    public int[] getDimensions() {
        int[] dim = this.mBackingStore.getDimensions();
        return dim != null ? Arrays.copyOf(dim, dim.length) : null;
    }

    Frame(FrameType type, int[] dimensions, FrameManager manager) {
        this.mBackingStore = new BackingStore(type, dimensions, manager);
    }

    Frame(BackingStore backingStore) {
        this.mBackingStore = backingStore;
    }

    final void assertAccessible(int mode) {
        if (this.mReadOnly && mode == 2) {
            String valueOf = String.valueOf(this);
            throw new RuntimeException(new StringBuilder(String.valueOf(valueOf).length() + 40).append("Attempting to write to read-only frame ").append(valueOf).append("!").toString());
        }
    }

    final void setReadOnly(boolean readOnly) {
        this.mReadOnly = readOnly;
    }

    void resize(int[] newDims) {
        int newCount = 0;
        int[] oldDims = this.mBackingStore.getDimensions();
        int oldCount = oldDims == null ? 0 : oldDims.length;
        if (newDims != null) {
            newCount = newDims.length;
        }
        if (oldCount != newCount) {
            throw new IllegalArgumentException("Cannot resize " + oldCount + "-dimensional Frame to " + newCount + "-dimensional Frame!");
        } else if (newDims != null && !Arrays.equals(oldDims, newDims)) {
            this.mBackingStore.resize(newDims);
        }
    }

    Frame makeCpuCopy(FrameManager frameManager) {
        Frame frame = new Frame(getType(), getDimensions(), frameManager);
        frame.mBackingStore.importStore(this.mBackingStore);
        return frame;
    }
}
