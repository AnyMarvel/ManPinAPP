package androidx.media.filterfw;

import androidx.media.filterfw.FrameQueue.Builder;
import java.lang.reflect.Field;
import  androidx.media.filterfw.Signature.*;

public final class InputPort {
    private boolean mAutoPullEnabled = false;
    private Filter mFilter;
    private PortInfo mInfo;
    private FrameListener mListener = null;
    private String mName;
    private FrameQueue mQueue = null;
    private Builder mQueueBuilder = null;
    private OutputPort mSourceHint = null;
    private boolean mWaitForFrame = true;

    public interface FrameListener {
        void onFrameReceived(InputPort inputPort, Frame frame);
    }

    private class FieldBinding implements FrameListener {
        private Field mField;

        public FieldBinding(Field field) {
            this.mField = field;
        }

        public void onFrameReceived(InputPort port, Frame frame) {
            try {
                if (port.mInfo.type.getNumberOfDimensions() > 0) {
                    this.mField.set(InputPort.this.mFilter, frame.asFrameValues().getValues());
                    return;
                }
                this.mField.set(InputPort.this.mFilter, frame.asFrameValue().getValue());
            } catch (Exception e) {
                String valueOf = String.valueOf(frame);
                String valueOf2 = String.valueOf(this.mField);
                String valueOf3 = String.valueOf(InputPort.this.mFilter);
                throw new RuntimeException(new StringBuilder(((String.valueOf(valueOf).length() + 55) + String.valueOf(valueOf2).length()) + String.valueOf(valueOf3).length()).append("Assigning frame ").append(valueOf).append(" to field ").append(valueOf2).append(" of filter ").append(valueOf3).append(" caused exception!").toString(), e);
            }
        }
    }

    public void attachToOutputPort(OutputPort outputPort) {
        assertInAttachmentStage();
        this.mFilter.openOutputPort(outputPort);
        this.mQueueBuilder.attachQueue(outputPort.getQueue());
    }

    public void setSourceHint(OutputPort outputPort) {
        this.mSourceHint = outputPort;
    }

    public OutputPort getSourceHint() {
        return this.mSourceHint;
    }

    public void bindToListener(FrameListener listener) {
        assertInAttachmentStage();
        this.mListener = listener;
    }

    public void bindToField(Field field) {
        assertInAttachmentStage();
        this.mListener = new FieldBinding(field);
    }

    public void bindToFieldNamed(String fieldName) {
        Field field = findFieldNamed(fieldName, this.mFilter.getClass());
        if (field == null) {
            throw new IllegalArgumentException(new StringBuilder(String.valueOf(fieldName).length() + 39).append("Attempting to bind to unknown field '").append(fieldName).append("'!").toString());
        }
        bindToField(field);
    }

    public void setAutoPullEnabled(boolean enabled) {
        this.mAutoPullEnabled = enabled;
    }

    public boolean isAutoPullEnabled() {
        return this.mAutoPullEnabled;
    }

    public synchronized Frame pullFrame() {
        Frame frame;
        if (this.mQueue == null) {
            throw new IllegalStateException("Cannot pull frame from closed input port!");
        }
        frame = this.mQueue.pullFrame();
        if (frame != null) {
            if (this.mListener != null) {
                this.mListener.onFrameReceived(this, frame);
            }
            this.mFilter.addAutoReleaseFrame(frame);
            if (frame.getTimestamp() != -1) {
                this.mFilter.onPulledFrameWithTimestamp(frame.getTimestamp());
            }
        }
        return frame;
    }

    public synchronized Frame peek() {
        if (this.mQueue == null) {
            throw new IllegalStateException("Cannot pull frame from closed input port!");
        }
        return this.mQueue.peek();
    }

    public boolean isConnected() {
        return this.mQueue != null;
    }

    public synchronized boolean hasFrame() {
        boolean z;
        z = this.mQueue != null && this.mQueue.canPull();
        return z;
    }

    public void setWaitsForFrame(boolean wait) {
        this.mWaitForFrame = wait;
    }

    public boolean waitsForFrame() {
        return this.mWaitForFrame;
    }

    public String getName() {
        return this.mName;
    }

    public FrameType getType() {
        return getQueue().getType();
    }

    public Filter getFilter() {
        return this.mFilter;
    }

    public String toString() {
        String name = this.mFilter.getName();
        String str = this.mName;
        return new StringBuilder((String.valueOf(name).length() + 1) + String.valueOf(str).length()).append(name).append(":").append(str).toString();
    }

    InputPort(Filter filter, String name, PortInfo info) {
        this.mFilter = filter;
        this.mName = name;
        this.mInfo = info;
    }

    boolean conditionsMet() {
        return !this.mWaitForFrame || hasFrame();
    }

    void onOpen(Builder builder) {
        this.mQueueBuilder = builder;
        this.mQueueBuilder.setReadType(this.mInfo.type);
        this.mFilter.onInputPortOpen(this);
    }

    void setQueue(FrameQueue queue) {
        this.mQueue = queue;
        this.mQueueBuilder = null;
    }

    FrameQueue getQueue() {
        return this.mQueue;
    }

    void clear() {
        if (this.mQueue != null) {
            this.mQueue.clear();
        }
    }

    private void assertInAttachmentStage() {
        if (this.mQueueBuilder == null) {
            throw new IllegalStateException("Attempting to attach port while not in attachment stage!");
        }
    }

    private Field findFieldNamed(String fieldName, Class<?> clazz) {
        Field field = null;
        try {
            field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            Class<?> superClass = clazz.getSuperclass();
            if (superClass != null) {
                return findFieldNamed(fieldName, superClass);
            }
            return field;
        }
    }
}
