package androidx.media.filterpacks.video;

import android.util.Log;

import androidx.media.filterfw.CameraStreamer;
import androidx.media.filterfw.Filter;
import androidx.media.filterfw.Frame;
import androidx.media.filterfw.FrameImage2D;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.InputPort;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.OutputPort;
import androidx.media.filterfw.Signature;
import androidx.media.filterfw.VideoFrameConsumer;
import androidx.media.filterfw.VideoFrameProvider;

public class CameraSource extends Filter implements VideoFrameConsumer {
    private long mCurrentTimestamp;
    private boolean mNewFrameAvailable = false;
    private FrameType mOutputType = FrameType.image2D(FrameType.ELEMENT_RGBA8888, 16);
    private boolean mUseWallClock = false;

    public CameraSource(MffContext context, String name) {
        super(context, name);
    }

    public Signature getSignature() {
        return new Signature().addInputPort("useWallClock", 1, FrameType.single(Boolean.TYPE)).addOutputPort("video", 2, this.mOutputType).disallowOtherPorts();
    }

    public void onInputPortOpen(InputPort port) {
        if (port.getName().equals("useWallClock")) {
            port.bindToFieldNamed("mUseWallClock");
            port.setAutoPullEnabled(true);
        }
    }

    protected void onOpen() {
        getContext().getCameraStreamer().addVideoFrameConsumer(this);
    }

    protected void onProcess() {
        if (nextFrame()) {
            CameraStreamer streamer = getContext().getCameraStreamer();
            OutputPort outPort = getConnectedOutputPort("video");
            FrameImage2D outputFrame = Frame.create(this.mOutputType, new int[]{1, 1}).asFrameImage2D();
            if (streamer.getLatestFrame(outputFrame, Integer.MAX_VALUE)) {
                if (this.mUseWallClock) {
                    outputFrame.setTimestamp(System.currentTimeMillis() * 1000000);
                }
                outPort.pushFrame(outputFrame);
            }
            synchronized (this) {
                this.mNewFrameAvailable = false;
                this.mCurrentTimestamp = outputFrame.getTimestamp();
            }
            outputFrame.release();
        }
    }

    protected void onClose() {
        getContext().getCameraStreamer().removeVideoFrameConsumer(this);
        this.mNewFrameAvailable = false;
    }

    private synchronized boolean nextFrame() {
        if (!this.mNewFrameAvailable) {
            enterSleepState();
        }
        return this.mNewFrameAvailable;
    }

    public void onVideoFrameAvailable(VideoFrameProvider provider, long timestampNs) {
        boolean shouldWakeup;
        synchronized (this) {
            this.mNewFrameAvailable = timestampNs != this.mCurrentTimestamp;
            if (!this.mNewFrameAvailable) {
                Log.i("CameraSource", "Frame is already grabbed: " + timestampNs);
            }
            shouldWakeup = this.mNewFrameAvailable;
        }
        if (shouldWakeup) {
            wakeUp();
        }
    }

    public void onVideoStreamStarted() {
    }

    public void onVideoStreamStopped() {
    }

    public void onVideoStreamError(Exception e) {
        throw new RuntimeException("Camera encountered an error. Aborting.", e);
    }
}
