package androidx.media.filterfw;

import java.lang.reflect.Array;

public class FrameValues extends FrameValue {
    public int getCount() {
        Object value = super.getValue();
        if (value == null || !value.getClass().isArray()) {
            return 1;
        }
        return Array.getLength(super.getValue());
    }

    public Object getValues() {
        Object value = super.getValue();
        if (value == null || value.getClass().isArray()) {
            return super.getValue();
        }
        Object[] array = (Object[]) Array.newInstance(value.getClass(), 1);
        array[0] = value;
        return array;
    }

    public Object getValueAtIndex(int index) {
        Object value = super.getValue();
        if (value != null && value.getClass().isArray()) {
            return Array.get(value, index);
        }
        if (index == 0) {
            return value;
        }
        throw new ArrayIndexOutOfBoundsException(index);
    }

    public FrameValue getFrameValueAtIndex(int index) {
        Object value = getValueAtIndex(index);
        FrameValue result = Frame.create(getType().asSingle(), new int[0]).asFrameValue();
        result.setValue(value);
        return result;
    }

    public void setValues(Object values) {
        super.setValue(values);
    }

    public void setValueAtIndex(Object value, int index) {
        super.assertAccessible(2);
        Object curValue = super.getValue();
        if (curValue != null && curValue.getClass().isArray()) {
            Array.set(curValue, index, value);
        } else if (index != 0) {
            throw new ArrayIndexOutOfBoundsException(index);
        } else {
            super.setValue(value);
        }
    }

    public void setFrameValueAtIndex(FrameValue frame, int index) {
        setValueAtIndex(frame.getValue(), index);
    }

    static FrameValues create(BackingStore backingStore) {
        FrameValue.assertObjectBased(backingStore.getFrameType());
        return new FrameValues(backingStore);
    }

    FrameValues(BackingStore backingStore) {
        super(backingStore);
    }
}
