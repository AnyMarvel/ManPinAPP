package androidx.media.filterfw;

import androidx.media.filterfw.FrameQueue.Builder;
import androidx.media.filterfw.Signature.*;

public final class OutputPort {
    private Filter mFilter;
    private PortInfo mInfo;
    private String mName;
    private FrameQueue mQueue = null;
    private Builder mQueueBuilder = null;
    private InputPort mTarget = null;
    private boolean mWaitsUntilAvailable = true;

    public boolean isConnected() {
        return this.mTarget != null;
    }

    public boolean isAvailable() {
        return this.mQueue == null || this.mQueue.canPush();
    }

    public Frame fetchAvailableFrame(int[] dimensions) {
        Frame frame = getQueue().fetchAvailableFrame(dimensions);
        if (frame != null) {
            this.mFilter.addAutoReleaseFrame(frame);
        }
        return frame;
    }

    public void pushFrame(Frame frame) {
        if (frame.getTimestamp() == -1) {
            frame.setTimestamp(this.mFilter.getCurrentTimestamp());
        }
        getQueue().pushFrame(frame);
    }

    public void setWaitsUntilAvailable(boolean wait) {
        this.mWaitsUntilAvailable = wait;
    }

    public boolean waitsUntilAvailable() {
        return this.mWaitsUntilAvailable;
    }

    public String getName() {
        return this.mName;
    }

    public Filter getFilter() {
        return this.mFilter;
    }

    public String toString() {
        String name = this.mFilter.getName();
        String str = this.mName;
        return new StringBuilder((String.valueOf(name).length() + 1) + String.valueOf(str).length()).append(name).append(":").append(str).toString();
    }

    OutputPort(Filter filter, String name, PortInfo info) {
        this.mFilter = filter;
        this.mName = name;
        this.mInfo = info;
    }

    void setTarget(InputPort target) {
        this.mTarget = target;
    }

    public InputPort getTarget() {
        return this.mTarget;
    }

    FrameQueue getQueue() {
        return this.mQueue;
    }

    void setQueue(FrameQueue queue) {
        this.mQueue = queue;
        this.mQueueBuilder = null;
    }

    void onOpen(Builder builder) {
        this.mQueueBuilder = builder;
        this.mQueueBuilder.setWriteType(this.mInfo.type);
        this.mFilter.onOutputPortOpen(this);
    }

    boolean isOpen() {
        return this.mQueue != null;
    }

    final boolean conditionsMet() {
        return !this.mWaitsUntilAvailable || isAvailable();
    }

    void clear() {
        if (this.mQueue != null) {
            this.mQueue.clear();
        }
    }
}
