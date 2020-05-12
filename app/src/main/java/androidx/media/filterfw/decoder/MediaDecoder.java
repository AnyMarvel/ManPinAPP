package androidx.media.filterfw.decoder;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build.VERSION;
import android.util.Log;
import androidx.media.filterfw.AudioFrameConsumer;
import androidx.media.filterfw.AudioFrameProvider;
import androidx.media.filterfw.FrameImage2D;
import androidx.media.filterfw.FrameValue;
import androidx.media.filterfw.RenderTarget;
import androidx.media.filterfw.VideoFrameConsumer;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

@TargetApi(16)
public class MediaDecoder implements Runnable, TrackDecoder.Listener, AudioFrameProvider, VideoStreamProvider {
    private static final boolean DEBUG = false;
    private static final long DEFAULT_I_FRAME_SPACING = 30;
    private static final int EVENT_EOF = 2;
    private static final int EVENT_START = 0;
    private static final int EVENT_STOP = 1;
    private static final float FRAME_DURATION_US = 33333.332f;
    private static final String LOG_TAG = "MediaDecoder";
    private static final int MAX_EVENTS = 32;
    public static final int ROTATE_180 = 180;
    public static final int ROTATE_90_LEFT = 270;
    public static final int ROTATE_90_RIGHT = 90;
    public static final int ROTATE_NONE = 0;
    private static final long SEEK_TOLERANCE_US = 10;
    private boolean hasLooped;
    private boolean iFrameDurationDetermined;
    private long iFrameDurationUs;
    private int mAudioConsumerWaitCount;
    private final HashSet<AudioFrameConsumer> mAudioConsumers;
    private AudioTrackDecoder mAudioTrackDecoder;
    private int mAudioTrackIndex;
    private int mAvailableAudioSamples;
    private final Context mContext;
    private final Thread mDecoderThread;
    private int mDefaultRotation;
    private long mEndMicros;
    private final LinkedBlockingQueue<Integer> mEventQueue;
    private final boolean mLooping;
    private MediaExtractor mMediaExtractor;
    private long mOffsetBytes;
    private boolean mOpenGLEnabled;
    private RenderTarget mRenderTarget;
    private boolean mSeenEndOfAudioOutput;
    private boolean mSeenEndOfVideoOutput;
    private boolean mSignaledEndOfInput;
    private long mStartMicros;
    private boolean mStarted;
    private final Uri mUri;
    private boolean mUseAudio;
    private boolean mUseVideo;
    private int mVideoConsumerWaitCount;
    private final HashSet<VideoFrameConsumer> mVideoConsumers;
    private VideoTrackDecoder mVideoTrackDecoder;
    private int mVideoTrackIndex;
    private int timestampIndex;
    private List<Long> timestamps;

    public MediaDecoder(Context context, Uri uri) {
        this(context, uri, 0);
    }

    public MediaDecoder(Context context, Uri uri, long startMicros) {
        this(context, uri, startMicros, -1);
    }

    public MediaDecoder(Context context, Uri uri, long startMicros, long endMicros) {
        this(context, uri, startMicros, endMicros, false);
    }

    public MediaDecoder(Context context, Uri uri, long startMicros, long endMicros, boolean looping) {
        this(context, uri, startMicros, endMicros, looping, 0);
    }

    public MediaDecoder(Context context, Uri uri, long startMicros, long endMicros, boolean looping, long offset) {
        this.mVideoConsumers = new HashSet();
        this.mAudioConsumers = new HashSet();
        this.mAvailableAudioSamples = 0;
        this.mOpenGLEnabled = true;
        this.iFrameDurationUs = 999999;
        this.iFrameDurationDetermined = false;
        this.hasLooped = false;
        if (context == null) {
            throw new NullPointerException("context cannot be null");
        }
        this.mContext = context;
        this.mUseAudio = true;
        this.mUseVideo = true;
        if (uri == null) {
            throw new NullPointerException("uri cannot be null");
        }
        this.mUri = uri;
        this.mOffsetBytes = offset;
        if (startMicros < 0) {
            throw new IllegalArgumentException("startMicros cannot be negative");
        } else if (endMicros == -1 || startMicros < endMicros) {
            this.mStartMicros = startMicros;
            this.mEndMicros = endMicros;
            this.mLooping = looping;
            this.mEventQueue = new LinkedBlockingQueue(32);
            this.mDecoderThread = new Thread(this);
        } else {
            throw new IllegalArgumentException("endMicros should be greater than startMicros or -1.");
        }
    }

    public void setUseAudio(boolean useAudio) {
        this.mUseAudio = useAudio;
    }

    public void setUseVideo(boolean useVideo) {
        this.mUseVideo = useVideo;
    }

    public void setOpenGLEnabled(boolean enabled) {
        if (this.mEventQueue.isEmpty()) {
            this.mOpenGLEnabled = enabled;
            return;
        }
        throw new IllegalStateException("Must call setOpenGLEnabled() before calling start()!");
    }

    public void setPlaybackTimestamps(List<Long> timestamps) {
        this.timestamps = timestamps;
    }

    public void setIFrameSpacing(long iFrameSpacing) {
        this.iFrameDurationUs = (long) (((float) iFrameSpacing) * FRAME_DURATION_US);
    }

    public boolean isOpenGLEnabled() {
        return this.mOpenGLEnabled;
    }

    public void start() {
        this.mEventQueue.offer(Integer.valueOf(0));
        this.mDecoderThread.start();
    }

    public void stop() {
        stop(true);
    }

    private void stop(boolean manual) {
        if (manual) {
            this.mEventQueue.offer(Integer.valueOf(1));
            this.mDecoderThread.interrupt();
            return;
        }
        this.mEventQueue.offer(Integer.valueOf(2));
    }

    public void run() {
        boolean shouldStop;
        do {
            try {
                Integer event = (Integer) this.mEventQueue.poll();
                shouldStop = false;
                if (event != null) {
                    switch (event.intValue()) {
                        case 0:
                            onStart();
                            continue;
                        case 1:
                            break;
                        case 2:
                            if (this.mVideoTrackDecoder != null) {
                                this.mVideoTrackDecoder.waitForFrameGrabs();
                            }
                            if (this.mAudioTrackDecoder != null) {
                                this.mAudioTrackDecoder.waitForFrameGrabs();
                                break;
                            }
                            break;
                        default:
                            continue;
                    }
                    onStop(true);
                    shouldStop = true;
                    continue;
                } else if (this.mStarted) {
                    decode();
                    continue;
                } else {
                    continue;
                }
            } catch (Exception e) {
                synchronized (this.mVideoConsumers) {
                    Iterator it = this.mVideoConsumers.iterator();
                    while (it.hasNext()) {
                        ((VideoFrameConsumer) it.next()).onVideoStreamError(e);
                    }
                    onStop(false);
                    return;
                }
            }
        } while (!shouldStop);
    }

    private void onStart() throws Exception {
        if (this.mOpenGLEnabled) {
            getRenderTarget().focus();
        }
        this.mMediaExtractor = new MediaExtractor();
        if (this.mOffsetBytes > 0) {
            File file = new File(this.mUri.getPath());
            FileInputStream inputStream = new FileInputStream(file);
            this.mMediaExtractor.setDataSource(inputStream.getFD(), this.mOffsetBytes, file.length() - this.mOffsetBytes);
            inputStream.close();
        } else {
            this.mMediaExtractor.setDataSource(this.mContext, this.mUri, null);
        }
        this.mVideoTrackIndex = -1;
        this.mAudioTrackIndex = -1;
        this.timestampIndex = 0;
        for (int i = 0; i < this.mMediaExtractor.getTrackCount(); i++) {
            MediaFormat format = this.mMediaExtractor.getTrackFormat(i);
            if (this.mUseVideo && DecoderUtil.isSupportedVideoFormat(format) && this.mVideoTrackIndex == -1) {
                this.mVideoTrackIndex = i;
            } else if (this.mUseAudio && DecoderUtil.isAudioFormat(format) && this.mAudioTrackIndex == -1) {
                this.mAudioTrackIndex = i;
            }
        }
        if (this.mVideoTrackIndex == -1 && this.mAudioTrackIndex == -1) {
            throw new IllegalArgumentException("Couldn't find a video or audio track in the provided file");
        }
        if (this.mVideoTrackIndex != -1) {
            VideoTrackDecoder gpuVideoTrackDecoder;
            MediaFormat videoFormat = this.mMediaExtractor.getTrackFormat(this.mVideoTrackIndex);
            if (this.mOpenGLEnabled) {
                gpuVideoTrackDecoder = new GpuVideoTrackDecoder(this.mVideoTrackIndex, videoFormat, this);
            } else {
                gpuVideoTrackDecoder = new CpuVideoTrackDecoder(this.mVideoTrackIndex, videoFormat, this);
            }
            this.mVideoTrackDecoder = gpuVideoTrackDecoder;
            this.mVideoTrackDecoder.init();
            this.mMediaExtractor.selectTrack(this.mVideoTrackIndex);
            if (VERSION.SDK_INT >= 17) {
                retrieveDefaultRotation();
            }
        }
        if (this.mAudioTrackIndex != -1) {
            this.mAudioTrackDecoder = new AudioTrackDecoder(this.mAudioTrackIndex, this.mMediaExtractor.getTrackFormat(this.mAudioTrackIndex), this);
            this.mAudioTrackDecoder.init();
            this.mMediaExtractor.selectTrack(this.mAudioTrackIndex);
        }
        if (this.mStartMicros > 0) {
            Log.d(LOG_TAG, "Start seek to: " + this.mStartMicros);
            seekToStart();
        }
        this.mStarted = true;
        synchronized (this.mVideoConsumers) {
            Iterator it = this.mVideoConsumers.iterator();
            while (it.hasNext()) {
                ((VideoFrameConsumer) it.next()).onVideoStreamStarted();
            }
        }
    }

    @TargetApi(17)
    private void retrieveDefaultRotation() {
        MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
        if (this.mOffsetBytes > 0) {
            try {
                File file = new File(this.mUri.getPath());
                FileInputStream inputStream = new FileInputStream(file);
                metadataRetriever.setDataSource(inputStream.getFD(), this.mOffsetBytes, file.length() - this.mOffsetBytes);
                inputStream.close();
            } catch (Exception e) {
                Log.e(LOG_TAG, "caught exception", e);
            }
        } else {
            String scheme = this.mUri.getScheme();
            if ("content".equals(scheme) || "android.resource".equals(scheme)) {
                metadataRetriever.setDataSource(this.mContext, this.mUri);
            } else if ("file".equals(scheme)) {
                metadataRetriever.setDataSource(this.mUri.getPath());
            } else {
                metadataRetriever.setDataSource(this.mUri.toString(), new HashMap());
            }
        }
        String rotationString = metadataRetriever.extractMetadata(24);
        this.mDefaultRotation = rotationString == null ? 0 : Integer.parseInt(rotationString);
    }

    private void onStop(boolean notifyListener) {
        this.mMediaExtractor.release();
        this.mMediaExtractor = null;
        if (this.mVideoTrackDecoder != null) {
            this.mVideoTrackDecoder.release();
            this.mVideoTrackDecoder = null;
        }
        if (this.mAudioTrackDecoder != null) {
            this.mAudioTrackDecoder.release();
            this.mAudioTrackDecoder = null;
        }
        if (this.mOpenGLEnabled) {
            if (this.mRenderTarget != null) {
                getRenderTarget().release();
            }
            RenderTarget.focusNone();
        }
        this.mVideoTrackIndex = -1;
        this.mAudioTrackIndex = -1;
        this.mEventQueue.clear();
        this.mStarted = false;
        if (notifyListener) {
            synchronized (this.mVideoConsumers) {
                Iterator it = this.mVideoConsumers.iterator();
                while (it.hasNext()) {
                    ((VideoFrameConsumer) it.next()).onVideoStreamStopped();
                }
            }
        }
    }

    private void decode() {
        int sampleTrackIndex = this.mMediaExtractor.getSampleTrackIndex();
        long samplePresentationTimeUs = this.mMediaExtractor.getSampleTime();
        boolean feedVideoInputSuccess = false;
        if ((this.mEndMicros <= this.mStartMicros || this.mEndMicros <= 0 || samplePresentationTimeUs <= this.mEndMicros) && sampleTrackIndex >= 0) {
            if (sampleTrackIndex >= 0) {
                long currentTimestamp = 0;
                long previousTimestamp = 0;
                if (this.timestamps != null && this.timestampIndex > 0 && this.timestampIndex < this.timestamps.size()) {
                    currentTimestamp = ((Long) this.timestamps.get(this.timestampIndex)).longValue();
                    previousTimestamp = ((Long) this.timestamps.get(this.timestampIndex - 1)).longValue();
                }
                if (sampleTrackIndex == this.mVideoTrackIndex) {
                    VideoTrackDecoder videoTrackDecoder = this.mVideoTrackDecoder;
                    MediaExtractor mediaExtractor = this.mMediaExtractor;
                    boolean z = this.timestamps == null;
                    boolean z2 = currentTimestamp < previousTimestamp || this.hasLooped;
                    feedVideoInputSuccess = videoTrackDecoder.feedInput(mediaExtractor, z, z2);
                } else if (sampleTrackIndex == this.mAudioTrackIndex) {
                    this.mAudioTrackDecoder.feedInput(this.mMediaExtractor, true, this.hasLooped);
                }
            }
        } else if (this.mLooping) {
            seekToStart();
            samplePresentationTimeUs = this.mMediaExtractor.getSampleTime();
            if (this.timestamps != null) {
                this.timestampIndex = 0;
            }
            if (this.mVideoTrackDecoder != null) {
                this.mVideoTrackDecoder.flush();
            }
            if (this.mAudioTrackDecoder != null) {
                this.mAudioTrackDecoder.flush();
            }
        } else {
            signalEndOfInput();
        }
        if (this.mVideoTrackDecoder != null) {
            this.mVideoTrackDecoder.drainOutputBuffer();
        }
        if (this.mAudioTrackDecoder != null) {
            this.mAudioTrackDecoder.drainOutputBuffer();
        }
        if (!this.mSignaledEndOfInput && feedVideoInputSuccess && feedVideoInputSuccess && samplePresentationTimeUs != -1 && sampleTrackIndex == this.mVideoTrackIndex && this.timestamps != null && !this.timestamps.isEmpty()) {
            seekToNextTimestamp();
        }
    }

    private void seekToStart() {
        this.mMediaExtractor.seekTo(this.mStartMicros, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
    }

    private void seekToNextTimestamp() {
        if (this.timestamps != null) {
            if (!this.iFrameDurationDetermined) {
                this.iFrameDurationUs = determineIFrameDuration(this.iFrameDurationUs);
                this.iFrameDurationDetermined = true;
            }
            long currentTimestamp = ((Long) this.timestamps.get(this.timestampIndex)).longValue();
            long lastIFrame = this.mMediaExtractor.getSampleTime() / this.iFrameDurationUs;
            if (Math.abs(this.mMediaExtractor.getSampleTime() - currentTimestamp) < SEEK_TOLERANCE_US) {
                this.timestampIndex++;
            }
            if (this.timestampIndex < this.timestamps.size()) {
                long seekTimestamp = ((Long) this.timestamps.get(this.timestampIndex)).longValue() + 1;
                if (seekTimestamp / this.iFrameDurationUs != lastIFrame || seekTimestamp < this.mMediaExtractor.getSampleTime()) {
                    this.mMediaExtractor.seekTo(SEEK_TOLERANCE_US + seekTimestamp, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
                    while (seekTimestamp > currentTimestamp && this.mMediaExtractor.getSampleTime() <= currentTimestamp && this.mMediaExtractor.getSampleTime() != -1) {
                        this.mMediaExtractor.advance();
                    }
                    return;
                }
                this.mMediaExtractor.advance();
            } else if (this.mLooping) {
                this.hasLooped = true;
                this.timestampIndex = 0;
                this.mMediaExtractor.seekTo(((Long) this.timestamps.get(this.timestampIndex)).longValue() + SEEK_TOLERANCE_US, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
            } else {
                signalEndOfInput();
            }
        }
    }

    private long determineIFrameDuration(long expectedIFrameDuration) {
        long actualIFrameDuration = expectedIFrameDuration;
        long originalTime = this.mMediaExtractor.getSampleTime();
        long seekErrorThreshold = expectedIFrameDuration / SEEK_TOLERANCE_US;
        this.mMediaExtractor.seekTo(expectedIFrameDuration + seekErrorThreshold, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
        long postSeekTime = this.mMediaExtractor.getSampleTime();
        if (Math.abs(postSeekTime - expectedIFrameDuration) > seekErrorThreshold) {
            if (postSeekTime == 0) {
                this.mMediaExtractor.seekTo(expectedIFrameDuration + seekErrorThreshold, MediaExtractor.SEEK_TO_NEXT_SYNC);
                postSeekTime = this.mMediaExtractor.getSampleTime();
            }
            actualIFrameDuration = postSeekTime;
            Log.d(LOG_TAG, "I-Frame durations do not match! Altering to: " + actualIFrameDuration);
        }
        this.mMediaExtractor.seekTo(originalTime, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
        long currentTime = this.mMediaExtractor.getSampleTime();
        while (currentTime < originalTime) {
            this.mMediaExtractor.advance();
            currentTime = this.mMediaExtractor.getSampleTime();
        }
        return actualIFrameDuration;
    }

    private void signalEndOfInput() {
        if (!this.mSignaledEndOfInput) {
            if (this.mVideoTrackDecoder != null) {
                this.mVideoTrackDecoder.signalEndOfInput();
            }
            if (this.mAudioTrackDecoder != null) {
                this.mAudioTrackDecoder.signalEndOfInput();
            }
            this.mSignaledEndOfInput = true;
        }
    }

    public void skipVideoFrame() {
        decrementConsumersWaiting();
        if (this.mVideoConsumerWaitCount == 0) {
            this.mVideoTrackDecoder.advance();
        }
    }

    public boolean grabVideoFrame(FrameImage2D outputVideoFrame, FrameValue infoFrame, int maxDim) {
        return grabVideoFrame(outputVideoFrame, infoFrame, maxDim, this.mDefaultRotation);
    }

    public void addVideoFrameConsumer(VideoFrameConsumer consumer) {
        synchronized (this.mVideoConsumers) {
            this.mVideoConsumers.add(consumer);
        }
    }

    public void removeVideoFrameConsumer(VideoFrameConsumer consumer) {
        synchronized (this.mVideoConsumers) {
            this.mVideoConsumers.remove(consumer);
        }
    }

    public boolean grabVideoFrame(FrameImage2D outputVideoFrame, FrameValue infoFrame, int maxDim, int videoRotation) {
        if (this.mVideoTrackDecoder == null || outputVideoFrame == null) {
            return false;
        }
        synchronized (this.mVideoConsumers) {
            if (!this.mOpenGLEnabled || this.mVideoConsumerWaitCount <= 1) {
                decrementConsumersWaiting();
                this.mVideoTrackDecoder.grabFrame(outputVideoFrame, infoFrame, maxDim, videoRotation);
                if (this.mVideoConsumerWaitCount == 0) {
                    this.mVideoTrackDecoder.advance();
                }
            } else {
                throw new UnsupportedOperationException("Currently, GPU decoders cannot have more than 1 video consumer.");
            }
        }
        return true;
    }

    public boolean grabAudioSamples(FrameValue outputAudioFrame) {
        if (this.mAudioTrackDecoder == null) {
            return false;
        }
        synchronized (this.mAudioConsumers) {
            if (this.mAudioConsumerWaitCount == 0) {
                throw new IllegalStateException("A consumer is grabbing an audio frame more than once!");
            }
            this.mAudioTrackDecoder.grabSample(outputAudioFrame);
            this.mAudioConsumerWaitCount--;
            if (this.mAudioConsumerWaitCount == 0) {
                this.mAvailableAudioSamples--;
                this.mAudioTrackDecoder.advance();
                if (this.mAvailableAudioSamples > 0) {
                    notifyAudioConsumers();
                }
            }
        }
        return true;
    }

    public void addAudioFrameConsumer(AudioFrameConsumer consumer) {
        synchronized (this.mAudioConsumers) {
            this.mAudioConsumers.add(consumer);
        }
    }

    public void removeAudioFrameConsumer(AudioFrameConsumer consumer) {
        synchronized (this.mAudioConsumers) {
            this.mAudioConsumers.remove(consumer);
        }
    }

    public void clearAudioSamples() {
        if (this.mAudioTrackDecoder != null) {
            this.mAudioTrackDecoder.clearBuffer();
        }
    }

    public long getDurationNs() {
        if (this.mStarted) {
            return this.mMediaExtractor.getTrackFormat(this.mVideoTrackIndex != -1 ? this.mVideoTrackIndex : this.mAudioTrackIndex).getLong("durationUs") * 1000;
        }
        throw new IllegalStateException("MediaDecoder has not been started");
    }

    private RenderTarget getRenderTarget() {
        if (this.mRenderTarget == null) {
            this.mRenderTarget = RenderTarget.newTarget(1, 1);
        }
        return this.mRenderTarget;
    }

    private boolean waitingForConsumer() {
        return this.mAudioConsumerWaitCount != 0;
    }

    private void notifyAudioConsumers() {
        Iterator it = this.mAudioConsumers.iterator();
        while (it.hasNext()) {
            ((AudioFrameConsumer) it.next()).onAudioSamplesAvailable(this);
        }
        this.mAudioConsumerWaitCount = this.mAudioConsumers.size();
    }

    public void onDecodedOutputAvailable(TrackDecoder decoder) {
        if (decoder == this.mVideoTrackDecoder) {
            synchronized (this.mVideoConsumers) {
                this.mVideoConsumerWaitCount = this.mVideoConsumers.size();
                Iterator it = this.mVideoConsumers.iterator();
                while (it.hasNext()) {
                    ((VideoFrameConsumer) it.next()).onVideoFrameAvailable(this, this.mVideoTrackDecoder.getTimestampNs());
                }
            }
        } else if (decoder == this.mAudioTrackDecoder) {
            synchronized (this.mAudioConsumers) {
                this.mAvailableAudioSamples++;
                if (!waitingForConsumer()) {
                    notifyAudioConsumers();
                }
            }
        }
    }

    public void onEndOfStream(TrackDecoder decoder) {
        if (decoder == this.mAudioTrackDecoder) {
            this.mSeenEndOfAudioOutput = true;
        } else if (decoder == this.mVideoTrackDecoder) {
            this.mSeenEndOfVideoOutput = true;
        }
        if (this.mAudioTrackDecoder != null && !this.mSeenEndOfAudioOutput) {
            return;
        }
        if (this.mVideoTrackDecoder == null || this.mSeenEndOfVideoOutput) {
            stop(false);
        }
    }

    private void decrementConsumersWaiting() {
        this.mVideoConsumerWaitCount--;
        if (this.mVideoConsumerWaitCount < 0) {
            throw new IllegalStateException("A consumer is grabbing a video frame more than once!");
        }
    }
}
