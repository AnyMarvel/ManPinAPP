package androidx.media.filterpacks.video;

import androidx.media.filterfw.Filter;
import androidx.media.filterfw.FrameImage2D;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.InputPort;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.OutputPort;
import androidx.media.filterfw.Signature;
import androidx.media.filterfw.VideoFrameConsumer;
import androidx.media.filterfw.VideoFrameProvider;
import java.util.concurrent.atomic.AtomicBoolean;

public class VideoProviderSource extends Filter implements VideoFrameConsumer {
    private static final long NS_IN_S = 1000000000;
    private static final int STATE_END_OF_STREAM = 2;
    private static final int STATE_RUNNING = 0;
    private static final int STATE_STOPPING = 1;
    private boolean mCloseOnStop = true;
    private long mFrameDuration = 0;
    private float mFrameRate = Float.MAX_VALUE;
    private AtomicBoolean mHasNewFrame = new AtomicBoolean(false);
    private int mMaxDim = Integer.MAX_VALUE;
    private long mNextExpectedTimestampNs = 0;
    private final FrameType mOutputType = FrameType.image2D(FrameType.ELEMENT_RGBA8888, 16);
    private boolean mPushEOSFrame = false;
    private volatile int mState = 0;
    private VideoFrameProvider mVideoFrameProvider;

    public VideoProviderSource(MffContext context, String name) {
        super(context, name);
    }

    public void forceClose() {
        this.mState = this.mPushEOSFrame ? 2 : 1;
        wakeUp();
    }

    public Signature getSignature() {
        FrameType videoProviderType = FrameType.single(VideoFrameProvider.class);
        FrameType booleanType = FrameType.single(Boolean.TYPE);
        FrameType intType = FrameType.single(Integer.TYPE);
        return new Signature().addInputPort("provider", 2, videoProviderType).addInputPort("closeOnStop", 1, booleanType).addInputPort("pushEOSFrame", 1, booleanType).addInputPort("maxDimension", 1, intType).addInputPort("frameRate", 1, FrameType.single(Float.TYPE)).addOutputPort("video", 2, this.mOutputType).disallowOtherPorts();
    }

    public void onInputPortOpen(InputPort port) {
        if (port.getName().equals("provider")) {
            port.bindToFieldNamed("mVideoFrameProvider");
            port.setAutoPullEnabled(true);
        } else if (port.getName().equals("closeOnStop")) {
            port.bindToFieldNamed("mCloseOnStop");
            port.setAutoPullEnabled(true);
        } else if (port.getName().equals("pushEOSFrame")) {
            port.bindToFieldNamed("mPushEOSFrame");
            port.setAutoPullEnabled(true);
        } else if (port.getName().equals("maxDimension")) {
            port.bindToFieldNamed("mMaxDim");
            port.setAutoPullEnabled(true);
        } else if (port.getName().equals("frameRate")) {
            port.bindToFieldNamed("mFrameRate");
            port.setAutoPullEnabled(true);
        }
    }

    protected void onOpen() {
        this.mHasNewFrame.set(false);
        this.mVideoFrameProvider.addVideoFrameConsumer(this);
        this.mFrameDuration = this.mFrameRate == Float.MAX_VALUE ? 0 : (long) (1.0E9f / this.mFrameRate);
    }

    protected void onProcess() {
        int[] outDims = new int[]{1, 1};
        OutputPort outPort = getConnectedOutputPort("video");
        FrameImage2D outFrame;
        switch (this.mState) {
            case 0:
                if (nextFrame()) {
                    outFrame = outPort.fetchAvailableFrame(outDims).asFrameImage2D();
                    if (this.mVideoFrameProvider.grabVideoFrame(outFrame, null, this.mMaxDim)) {
                        outPort.pushFrame(outFrame);
                        return;
                    }
                    return;
                }
                return;
            case 1:
                requestClose();
                return;
            case 2:
                outFrame = outPort.fetchAvailableFrame(outDims).asFrameImage2D();
                outFrame.setTimestamp(-2);
                outPort.pushFrame(outFrame);
                this.mState = 1;
                return;
            default:
                throw new IllegalStateException("Illegal state: " + this.mState);
        }
    }

    protected void onClose() {
        this.mVideoFrameProvider.removeVideoFrameConsumer(this);
    }

    public void onVideoFrameAvailable(VideoFrameProvider provider, long timestampNs) {
        if (shouldKeepFrame(timestampNs)) {
            this.mHasNewFrame.set(true);
            wakeUp();
            return;
        }
        provider.skipVideoFrame();
    }

    public void onVideoStreamStarted() {
    }

    public void onVideoStreamStopped() {
        if (this.mCloseOnStop) {
            forceClose();
        }
    }

    public void onVideoStreamError(Exception e) {
        throw new RuntimeException("VideoProvider encountered error!", e);
    }

    private boolean nextFrame() {
        boolean frameAvailable = this.mHasNewFrame.compareAndSet(true, false);
        if (!frameAvailable) {
            enterSleepState();
        }
        return frameAvailable;
    }

    private boolean shouldKeepFrame(long frameTimestamp) {
        if (this.mFrameDuration == 0) {
            return true;
        }
        if (frameTimestamp < this.mNextExpectedTimestampNs) {
            return false;
        }
        while (this.mNextExpectedTimestampNs < frameTimestamp) {
            this.mNextExpectedTimestampNs = nextLongDivisible(frameTimestamp, this.mFrameDuration);
        }
        return true;
    }

    private static long nextLongDivisible(long base, long factor) {
        return (((base - 1) / factor) + 1) * factor;
    }
}
