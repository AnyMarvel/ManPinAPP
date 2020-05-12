package androidx.media.filterfw.decoder;

import android.annotation.TargetApi;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;
import androidx.media.filterfw.FrameImage2D;
import androidx.media.filterfw.FrameValue;

@TargetApi(16)
public abstract class VideoTrackDecoder extends TrackDecoder {
    private static final String LOG_TAG = "VideoTrackDecoder";
    protected volatile boolean mFrameAvailable;
    protected final Object mFrameMonitor = new Object();

    protected abstract void copyFrameDataTo(FrameImage2D frameImage2D, FrameValue frameValue, int i, int i2);

    public /* bridge */ /* synthetic */ boolean drainOutputBuffer() {
        return super.drainOutputBuffer();
    }

    public /* bridge */ /* synthetic */ boolean feedInput(MediaExtractor mediaExtractor, boolean z, boolean z2) {
        return super.feedInput(mediaExtractor, z, z2);
    }

    public /* bridge */ /* synthetic */ void flush() {
        super.flush();
    }

    public /* bridge */ /* synthetic */ void init() {
        super.init();
    }

    public /* bridge */ /* synthetic */ void release() {
        super.release();
    }

    public /* bridge */ /* synthetic */ void signalEndOfInput() {
        super.signalEndOfInput();
    }

    protected VideoTrackDecoder(int trackIndex, MediaFormat format, Listener listener) {
        super(trackIndex, format, listener);
        if (!DecoderUtil.isSupportedVideoFormat(format)) {
            throw new IllegalArgumentException("VideoTrackDecoder can only be used with supported video formats");
        }
    }

    public void grabFrame(FrameImage2D outputVideoFrame, FrameValue infoFrame, int maxDim, int rotation) {
        synchronized (this.mFrameMonitor) {
            if (this.mFrameAvailable) {
                copyFrameDataTo(outputVideoFrame, infoFrame, maxDim, rotation);
                return;
            }
            Log.w(LOG_TAG, "frame is not ready - the caller has to wait for a corresponding onDecodedFrameAvailable() call");
        }
    }

    public void advance() {
        synchronized (this.mFrameMonitor) {
            this.mFrameAvailable = false;
            this.mFrameMonitor.notifyAll();
        }
    }

    public boolean waitForFrameGrabs() {
        boolean z;
        synchronized (this.mFrameMonitor) {
            while (this.mFrameAvailable) {
                try {
                    this.mFrameMonitor.wait();
                } catch (InterruptedException e) {
                    z = false;
                }
            }
            z = true;
        }
        return z;
    }

    protected final void markFrameAvailable() {
        synchronized (this.mFrameMonitor) {
            this.mFrameAvailable = true;
            this.mFrameMonitor.notifyAll();
        }
    }

    protected static boolean needSwapDimension(int rotation) {
        switch (rotation) {
            case 0:
            case MediaDecoder.ROTATE_180 /*180*/:
                return false;
            case 90:
            case MediaDecoder.ROTATE_90_LEFT /*270*/:
                return true;
            default:
                throw new IllegalArgumentException("Unsupported rotation angle.");
        }
    }
}
