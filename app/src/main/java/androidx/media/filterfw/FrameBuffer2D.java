package androidx.media.filterfw;

public class FrameBuffer2D extends FrameBuffer1D {
    public int getWidth() {
        return this.mBackingStore.getDimensions()[0];
    }

    public int getHeight() {
        return this.mBackingStore.getDimensions()[1];
    }

    static FrameBuffer2D create(BackingStore backingStore) {
        assertCanCreate(backingStore);
        return new FrameBuffer2D(backingStore);
    }

    FrameBuffer2D(BackingStore backingStore) {
        super(backingStore);
    }

    static void assertCanCreate(BackingStore backingStore) {
        FrameBuffer1D.assertCanCreate(backingStore);
        int[] dimensions = backingStore.getDimensions();
        int dimCount = dimensions != null ? dimensions.length : 0;
        if (dimCount != 2) {
            throw new RuntimeException("Cannot access " + dimCount + "-dimensional Frame as a FrameBuffer2D instance!");
        }
    }
}
