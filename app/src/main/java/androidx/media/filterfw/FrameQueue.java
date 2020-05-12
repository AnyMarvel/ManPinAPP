package androidx.media.filterfw;

import java.util.Iterator;
import java.util.Vector;

public class FrameQueue {
    private String mName;
    private QueueImpl mQueueImpl;
    private static FrameType mType;

    public static class Builder {
        private Vector<FrameQueue> mAttachedQueues = new Vector();
        private FrameType mReadType = null;
        private FrameType mWriteType = null;

        public void setWriteType(FrameType type) {
            this.mWriteType = type;
        }

        public void setReadType(FrameType type) {
            this.mReadType = type;
        }

        public void attachQueue(FrameQueue queue) {
            this.mAttachedQueues.add(queue);
        }

        public FrameQueue build(String name) {
            FrameQueue result = new FrameQueue(buildType(), name);
            buildQueueImpl(result);
            return result;
        }

        private void buildQueueImpl(FrameQueue queue) {
            queue.getClass();
            queue.mQueueImpl = new SingleFrameQueueImpl();
        }

        private FrameType buildType() {
            FrameType result = FrameType.merge(this.mWriteType, this.mReadType);
            Iterator it = this.mAttachedQueues.iterator();
            while (it.hasNext()) {
                result = FrameType.merge(result, ((FrameQueue) it.next()).mType);
            }
            return result;
        }
    }

    private interface QueueImpl {
        boolean canPull();

        boolean canPush();

        void clear();

        Frame fetchAvailableFrame(int[] iArr);

        Frame peek();

        Frame pullFrame();

        void pushFrame(Frame frame);
    }

    private static class SingleFrameQueueImpl implements QueueImpl {
        private Frame mFrame;
        public SingleFrameQueueImpl() {
            this.mFrame = null;
        }

        public boolean canPull() {
            return this.mFrame != null;
        }

        public boolean canPush() {
            return this.mFrame == null;
        }

        public Frame pullFrame() {
            Frame result = this.mFrame;
            this.mFrame = null;
            return result;
        }

        public Frame peek() {
            return this.mFrame;
        }

        public Frame fetchAvailableFrame(int[] dimensions) {
            return new Frame(FrameQueue.mType, dimensions, FrameManager.current());
        }

        public void pushFrame(Frame frame) {
            this.mFrame = frame.retain();
            this.mFrame.setReadOnly(true);
        }

        public void clear() {
            if (this.mFrame != null) {
                this.mFrame.release();
                this.mFrame = null;
            }
        }
    }

    public FrameType getType() {
        return this.mType;
    }

    public boolean canPull() {
        return this.mQueueImpl.canPull();
    }

    public boolean canPush() {
        return this.mQueueImpl.canPush();
    }

    public Frame pullFrame() {
        return this.mQueueImpl.pullFrame();
    }

    public Frame fetchAvailableFrame(int[] dimensions) {
        return this.mQueueImpl.fetchAvailableFrame(dimensions);
    }

    public void pushFrame(Frame frame) {
        this.mQueueImpl.pushFrame(frame);
    }

    public Frame peek() {
        return this.mQueueImpl.peek();
    }

    public String toString() {
        return this.mName;
    }

    public void clear() {
        this.mQueueImpl.clear();
    }

    private FrameQueue(FrameType type, String name) {
        this.mType = type;
        this.mName = name;
    }
}
