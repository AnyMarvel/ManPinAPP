package androidx.media.filterfw.decoder;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;
import android.util.LongSparseArray;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;

@TargetApi(16)
abstract class TrackDecoder {
    private static final long DEFAULT_LOOPING_OFFSET = 100000000;
    private static final String LOG_TAG = "TrackDecoder";
    private static final int NO_INPUT_BUFFER = -1;
    private static final long TIMEOUT_US = 50;
    private ByteBuffer[] mCodecInputBuffers;
    private ByteBuffer[] mCodecOutputBuffers;
    private boolean mIsMediaCodecStarted = false;
    private final Listener mListener;
    private long mLoopingOffset = DEFAULT_LOOPING_OFFSET;
    private MediaCodec mMediaCodec;
    private final MediaFormat mMediaFormat;
    private MediaFormat mOutputFormat;
    private boolean mShouldEnqueueEndOfStream;
    private final Queue<Long> mSynchronizationSampleTimestampsUs;
    private final int mTrackIndex;
    private LongSparseArray<Long> timestampMap = new LongSparseArray();

    interface Listener {
        void onDecodedOutputAvailable(TrackDecoder trackDecoder);

        void onEndOfStream(TrackDecoder trackDecoder);
    }

    public abstract void advance();

    public abstract long getTimestampNs();

    protected abstract MediaCodec initMediaCodec(MediaFormat mediaFormat);

    protected abstract boolean onDataAvailable(MediaCodec mediaCodec, ByteBuffer[] byteBufferArr, int i, BufferInfo bufferInfo, boolean z);

    protected TrackDecoder(int trackIndex, MediaFormat mediaFormat, Listener listener) {
        this.mTrackIndex = trackIndex;
        if (mediaFormat == null) {
            throw new NullPointerException("mediaFormat cannot be null");
        }
        this.mMediaFormat = mediaFormat;
        if (listener == null) {
            throw new NullPointerException("listener cannot be null");
        }
        this.mListener = listener;
        this.mSynchronizationSampleTimestampsUs = new LinkedList();
    }

    public void init() {
        this.mMediaCodec = initMediaCodec(this.mMediaFormat);
        this.mMediaCodec.start();
        this.mIsMediaCodecStarted = true;
        this.mCodecInputBuffers = this.mMediaCodec.getInputBuffers();
        this.mCodecOutputBuffers = this.mMediaCodec.getOutputBuffers();
        this.mSynchronizationSampleTimestampsUs.clear();
    }

    public void signalEndOfInput() {
        this.mShouldEnqueueEndOfStream = true;
        tryEnqueueEndOfStream();
    }

    public void release() {
        if (this.mMediaCodec != null) {
            if (this.mIsMediaCodecStarted) {
                try {
                    this.mMediaCodec.stop();
                    this.mIsMediaCodecStarted = false;
                } catch (IllegalStateException e) {
                    String str = LOG_TAG;
                    String valueOf = String.valueOf(e);
                    Log.e(str, new StringBuilder(String.valueOf(valueOf).length() + 40).append("Unexpected exception MediaCodec.stop(): ").append(valueOf).toString());
                }
            }
            this.mMediaCodec.release();
        }
    }

    protected MediaCodec getMediaCodec() {
        return this.mMediaCodec;
    }

    protected void notifyListener() {
        this.mListener.onDecodedOutputAvailable(this);
    }

    public void flush() {
        if (this.mMediaCodec != null) {
            this.mMediaCodec.flush();
        }
    }

    public boolean feedInput(MediaExtractor mediaExtractor, boolean shouldAdvance, boolean hasLooped) {
        int inputBufferIndex = this.mMediaCodec.dequeueInputBuffer(TIMEOUT_US);
        long presentationTimeUs;
        if (inputBufferIndex != -1) {
            int sampleSize = mediaExtractor.readSampleData(this.mCodecInputBuffers[inputBufferIndex], 0);
            if (sampleSize < 0) {
                Log.w(LOG_TAG, "Media extractor had sample but no data.");
                this.mMediaCodec.queueInputBuffer(inputBufferIndex, 0, 0, 0, 4);
                presentationTimeUs = 0;
                return false;
            }
            int flags;
            presentationTimeUs = mediaExtractor.getSampleTime();
            if (hasLooped) {
                long modifiedPts = this.mLoopingOffset;
                this.timestampMap.put(modifiedPts, Long.valueOf(presentationTimeUs));
                this.mLoopingOffset++;
                presentationTimeUs = modifiedPts;
            }
            if ((mediaExtractor.getSampleFlags() & 1) != 0) {
                flags = 1;
                this.mSynchronizationSampleTimestampsUs.add(Long.valueOf(presentationTimeUs));
            } else {
                flags = 0;
            }
            this.mMediaCodec.queueInputBuffer(inputBufferIndex, 0, sampleSize, presentationTimeUs, flags);
            if (!shouldAdvance) {
                return true;
            }
            if (mediaExtractor.advance() && mediaExtractor.getSampleTrackIndex() == this.mTrackIndex) {
                return true;
            }
            return false;
        }
        presentationTimeUs = 0;
        return false;
    }

    private void tryEnqueueEndOfStream() {
        int inputBufferIndex = this.mMediaCodec.dequeueInputBuffer(TIMEOUT_US);
        if (inputBufferIndex != -1) {
            this.mMediaCodec.queueInputBuffer(inputBufferIndex, 0, 0, 0, 4);
            this.mShouldEnqueueEndOfStream = false;
        }
    }

    public boolean drainOutputBuffer() {
        boolean isKeyFrame = true;
        BufferInfo outputInfo = new BufferInfo();
        int outputBufferIndex = this.mMediaCodec.dequeueOutputBuffer(outputInfo, TIMEOUT_US);
        if ((outputInfo.flags & 4) != 0) {
            this.mListener.onEndOfStream(this);
            return false;
        }
        if (this.mShouldEnqueueEndOfStream) {
            tryEnqueueEndOfStream();
        }
        if (outputBufferIndex >= 0) {
            if (this.mSynchronizationSampleTimestampsUs.isEmpty()) {
                isKeyFrame = false;
            } else {
                long nextSynchronizationTimestampUs = ((Long) this.mSynchronizationSampleTimestampsUs.peek()).longValue();
                if (outputInfo.presentationTimeUs != nextSynchronizationTimestampUs) {
                    isKeyFrame = false;
                }
                if (outputInfo.presentationTimeUs >= nextSynchronizationTimestampUs) {
                    this.mSynchronizationSampleTimestampsUs.remove();
                }
            }
            Long correctedPts = (Long) this.timestampMap.get(outputInfo.presentationTimeUs);
            if (correctedPts != null) {
                this.timestampMap.remove(outputInfo.presentationTimeUs);
                outputInfo.presentationTimeUs = correctedPts.longValue();
            }
            return onDataAvailable(this.mMediaCodec, this.mCodecOutputBuffers, outputBufferIndex, outputInfo, isKeyFrame);
        } else if (outputBufferIndex == -3) {
            this.mCodecOutputBuffers = this.mMediaCodec.getOutputBuffers();
            return true;
        } else if (outputBufferIndex != -2) {
            return false;
        } else {
            this.mOutputFormat = this.mMediaCodec.getOutputFormat();
            String str = LOG_TAG;
            String valueOf = String.valueOf(this.mOutputFormat);
            Log.d(str, new StringBuilder(String.valueOf(valueOf).length() + 29).append("Output format has changed to ").append(valueOf).toString());
            return true;
        }
    }
}
