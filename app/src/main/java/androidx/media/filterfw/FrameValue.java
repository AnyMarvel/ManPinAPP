package androidx.media.filterfw;

public class FrameValue extends Frame {
    public Object getValue() {
        Object result = this.mBackingStore.lockData(1, 8);
        this.mBackingStore.unlock();
        return result;
    }

    public void setValue(Object value) {
        this.mBackingStore.lockBacking(2, 8).setData(value);
        this.mBackingStore.unlock();
    }

    static FrameValue create(BackingStore backingStore) {
        assertObjectBased(backingStore.getFrameType());
        return new FrameValue(backingStore);
    }

    FrameValue(BackingStore backingStore) {
        super(backingStore);
    }

    static void assertObjectBased(FrameType type) {
        if (type.getElementId() != 1) {
            throw new RuntimeException("Cannot access non-object based Frame as FrameValue!");
        }
    }
}
