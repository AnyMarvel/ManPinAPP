package androidx.media.filterpacks.decoder;

import android.annotation.TargetApi;
import android.net.Uri;

import androidx.media.filterfw.AudioFrameConsumer;
import androidx.media.filterfw.AudioFrameProvider;
import androidx.media.filterfw.Filter;
import androidx.media.filterfw.Frame;
import androidx.media.filterfw.FrameImage2D;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.FrameValue;
import androidx.media.filterfw.InputPort;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.OutputPort;
import androidx.media.filterfw.Signature;
import androidx.media.filterfw.VideoFrameConsumer;
import androidx.media.filterfw.VideoFrameProvider;
import androidx.media.filterfw.decoder.AudioSample;
import androidx.media.filterfw.decoder.MediaDecoder;
import androidx.media.filterfw.decoder.VideoFrameInfo;

import java.util.List;

@TargetApi(16)
public class MediaDecoderSource extends Filter implements AudioFrameConsumer, VideoFrameConsumer {
    private static final FrameType INPUT_END_TYPE = FrameType.single(Long.TYPE);
    private static final FrameType INPUT_LOOP_TYPE = FrameType.single(Boolean.TYPE);
    private static final FrameType INPUT_ROTATION_TYPE = FrameType.single(Integer.TYPE);
    private static final FrameType INPUT_SEEK_TYPE = FrameType.single(Long.TYPE);
    private static final FrameType INPUT_START_TYPE = FrameType.single(Long.TYPE);
    private static final FrameType INPUT_URI_TYPE = FrameType.single(Uri.class);
    private static final FrameType OUTPUT_AUDIO_TYPE = FrameType.single(AudioSample.class);
    private static final FrameType OUTPUT_DURATION_TYPE = FrameType.single(Long.TYPE);
    private static final FrameType OUTPUT_INFO_TYPE = FrameType.single(VideoFrameInfo.class);
    private static final FrameType OUTPUT_VIDEO_TYPE = FrameType.image2D(FrameType.ELEMENT_RGBA8888, 16);
    private static final int STATUS_AUDIO_FRAME = 1;
    private static final int STATUS_NO_FRAME = 0;
    private static final int STATUS_VIDEO_FRAME = 2;
    private long iFrameSpacing;
    private boolean mDurationAvailable;
    private long mEndMicros;
    private boolean mHasVideoRotation;
    private final Object mLock;
    private boolean mLooping;
    private MediaDecoder mMediaDecoder;
    private Exception mMediaDecoderException;
    private int mNewAudioFramesAvailable;
    private boolean mNewVideoFrameAvailable;
    private long mOffsetBytes;
    private long mStartMicros;
    private int mVideoRotation;
    private List<Long> timestamps;

    public MediaDecoderSource(MffContext context, String name) {
        this(context, name, 0);
    }

    public MediaDecoderSource(MffContext context, String name, long offsetBytes) {
        super(context, name);
        this.mVideoRotation = 0;
        this.mStartMicros = 0;
        this.mEndMicros = -1;
        this.mLooping = false;
        this.mOffsetBytes = 0;
        this.mLock = new Object();
        this.mOffsetBytes = offsetBytes;
    }

    public void setPlaybackTimestamps(List<Long> timestamps) {
        this.timestamps = timestamps;
    }

    public void setIFrameSpacing(long iFrameSpacing) {
        this.iFrameSpacing = iFrameSpacing;
    }

    public int getSchedulePriority() {
        return 25;
    }

    public Signature getSignature() {
        return new Signature().addInputPort("uri", 2, INPUT_URI_TYPE).addInputPort("rotation", 1, INPUT_ROTATION_TYPE).addInputPort("start", 1, INPUT_START_TYPE).addInputPort("end", 1, INPUT_END_TYPE).addInputPort("loop", 1, INPUT_LOOP_TYPE).addInputPort("seekDuration", 1, INPUT_SEEK_TYPE).addOutputPort("video", 1, OUTPUT_VIDEO_TYPE).addOutputPort("videoInfo", 1, OUTPUT_INFO_TYPE).addOutputPort("audio", 1, OUTPUT_AUDIO_TYPE).addOutputPort("duration", 1, OUTPUT_DURATION_TYPE).disallowOtherPorts();
    }

    public void onInputPortOpen(InputPort port) {
        if (port.getName().equals("rotation")) {
            port.bindToFieldNamed("mVideoRotation");
            port.setAutoPullEnabled(true);
            this.mHasVideoRotation = true;
        } else if (port.getName().equals("start")) {
            port.bindToFieldNamed("mStartMicros");
            port.setAutoPullEnabled(true);
        } else if (port.getName().equals("end")) {
            port.bindToFieldNamed("mEndMicros");
            port.setAutoPullEnabled(true);
        } else if (port.getName().equals("loop")) {
            port.bindToFieldNamed("mLooping");
            port.setAutoPullEnabled(true);
        } else if (port.getName().equals("seekDuration")) {
            port.bindToFieldNamed("mSeekDuration");
            port.setAutoPullEnabled(true);
        }
    }

    protected void onPrepare() {
        super.onPrepare();
        this.mMediaDecoder = new MediaDecoder(getContext().getApplicationContext(), (Uri) getConnectedInputPort("uri").pullFrame().asFrameValue().getValue(), this.mStartMicros, this.mEndMicros, this.mLooping, this.mOffsetBytes);
        this.mMediaDecoder.setPlaybackTimestamps(this.timestamps);
        this.mMediaDecoder.setIFrameSpacing(this.iFrameSpacing);
        if (getConnectedOutputPort("audio") != null) {
            this.mMediaDecoder.setUseAudio(true);
            this.mMediaDecoder.addAudioFrameConsumer(this);
        } else {
            this.mMediaDecoder.setUseAudio(false);
        }
        if (getConnectedOutputPort("video") != null) {
            this.mMediaDecoder.setUseVideo(true);
            this.mMediaDecoder.addVideoFrameConsumer(this);
        } else {
            this.mMediaDecoder.setUseVideo(false);
        }
        this.mMediaDecoder.setOpenGLEnabled(isOpenGLSupported());
        this.mMediaDecoder.start();
    }

    protected void onTearDown() {
        if (this.mMediaDecoder != null) {
            this.mMediaDecoder.stop();
            this.mMediaDecoder = null;
        }
        super.onTearDown();
    }

    private int nextFrame() {
        int status = 0;
        synchronized (this.mLock) {
            if (this.mNewAudioFramesAvailable > 0) {
                status = 0 | 1;
                this.mNewAudioFramesAvailable--;
            }
            if (this.mNewVideoFrameAvailable && this.mNewAudioFramesAvailable == 0) {
                status |= 2;
                this.mNewVideoFrameAvailable = false;
            }
            if (status == 0) {
                enterSleepState();
            }
        }
        return status;
    }

    public void onAudioSamplesAvailable(AudioFrameProvider provider) {
        synchronized (this.mLock) {
            this.mNewAudioFramesAvailable++;
        }
        wakeUp();
    }

    public void onVideoFrameAvailable(VideoFrameProvider provider, long timestampNs) {
        synchronized (this.mLock) {
            this.mNewVideoFrameAvailable = true;
        }
        wakeUp();
    }

    public void onVideoStreamStarted() {
        synchronized (this.mLock) {
            this.mDurationAvailable = true;
        }
        wakeUp();
    }

    public void onVideoStreamError(Exception e) {
        synchronized (this.mLock) {
            this.mMediaDecoderException = e;
        }
        wakeUp();
    }

    public void onVideoStreamStopped() {
        requestClose();
        wakeUp();
    }

    protected void onProcess() {
        checkForMediaDecoderError();
        boolean durationAvailable;
        synchronized (this.mLock) {
            durationAvailable = this.mDurationAvailable;
            this.mDurationAvailable = false;
        }
        if (durationAvailable) {
            OutputPort durationPort = getConnectedOutputPort("duration");
            if (durationPort != null) {
                FrameValue duration = Frame.create(OUTPUT_DURATION_TYPE, new int[]{1}).asFrameValue();
                duration.setValue(Long.valueOf(this.mMediaDecoder.getDurationNs()));
                durationPort.pushFrame(duration);
                duration.release();
                durationPort.setWaitsUntilAvailable(false);
            }
        }
        int status = nextFrame();
        if ((status & 2) != 0) {
            OutputPort videoPort = getConnectedOutputPort("video");
            OutputPort infoPort = getConnectedOutputPort("videoInfo");
            FrameImage2D videoFrame = null;
            FrameValue infoFrame = null;
            if (videoPort != null) {
                videoFrame = Frame.create(OUTPUT_VIDEO_TYPE, new int[]{1, 1}).asFrameImage2D();
            }
            if (infoPort != null) {
                infoFrame = Frame.create(OUTPUT_INFO_TYPE, null).asFrameValue();
            }
            if (this.mHasVideoRotation) {
                this.mMediaDecoder.grabVideoFrame(videoFrame, infoFrame, Integer.MAX_VALUE, this.mVideoRotation);
            } else {
                this.mMediaDecoder.grabVideoFrame(videoFrame, infoFrame, Integer.MAX_VALUE);
            }
            if (videoFrame != null) {
                videoPort.pushFrame(videoFrame);
                videoFrame.release();
            }
            if (infoFrame != null) {
                infoPort.pushFrame(infoFrame);
                infoFrame.release();
            }
        }
        if ((status & 1) != 0) {
            OutputPort audioPort = getConnectedOutputPort("audio");
            boolean validPort = audioPort != null && audioPort.getTarget().getFilter().isActive();
            if (validPort) {
                FrameValue audioFrame = Frame.create(OUTPUT_AUDIO_TYPE, new int[]{1}).asFrameValue();
                this.mMediaDecoder.grabAudioSamples(audioFrame);
                audioPort.pushFrame(audioFrame);
                audioFrame.release();
                return;
            }
            this.mMediaDecoder.clearAudioSamples();
        }
    }

    private void checkForMediaDecoderError() {
        synchronized (this.mLock) {
            if (this.mMediaDecoderException != null) {
                throw new RuntimeException(this.mMediaDecoderException);
            }
        }
    }
}
