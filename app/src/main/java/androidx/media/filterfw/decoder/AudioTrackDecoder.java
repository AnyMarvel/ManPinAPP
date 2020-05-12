package androidx.media.filterfw.decoder;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import androidx.media.filterfw.FrameValue;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

@TargetApi(16)
public class AudioTrackDecoder extends TrackDecoder {
    private static final int MAX_BUFFER_SIZE = 10;
    private int mAudioChannelCount;
    private int mAudioSampleRate;
    private final BlockingDeque<AudioSample> mAudioSamples;
    private final Object mFrameMonitor = new Object();
    private long mPresentationTimeUs;

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

    public AudioTrackDecoder(int trackIndex, MediaFormat format, Listener listener) {
        super(trackIndex, format, listener);
        if (DecoderUtil.isAudioFormat(format)) {
            this.mAudioSamples = new LinkedBlockingDeque();
            this.mAudioSampleRate = format.getInteger("sample-rate");
            this.mAudioChannelCount = format.getInteger("channel-count");
            return;
        }
        throw new IllegalArgumentException("AudioTrackDecoder can only be used with audio formats");
    }

    protected MediaCodec initMediaCodec(MediaFormat format) {
        try {
            MediaCodec mediaCodec = MediaCodec.createDecoderByType(format.getString("mime"));
            mediaCodec.configure(format, null, null, 0);
            return mediaCodec;
        } catch (IOException e) {
            return null;
        }
    }

    protected boolean onDataAvailable(MediaCodec codec, ByteBuffer[] buffers, int bufferIndex, BufferInfo info, boolean isKeyFrame) {
        waitForBufferAvailable();
        synchronized (this.mFrameMonitor) {
            ByteBuffer buffer = buffers[bufferIndex];
            byte[] data = new byte[info.size];
            buffer.position(info.offset);
            buffer.get(data, 0, info.size);
            this.mAudioSamples.offerLast(new AudioSample(this.mAudioSampleRate, this.mAudioChannelCount, data, info.presentationTimeUs));
            buffer.clear();
            codec.releaseOutputBuffer(bufferIndex, false);
            this.mPresentationTimeUs = info.presentationTimeUs;
        }
        notifyListener();
        return true;
    }

    public boolean waitForFrameGrabs() {
        boolean z;
        synchronized (this.mFrameMonitor) {
            while (this.mAudioSamples.size() != 0) {
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

    public void advance() {
        synchronized (this.mFrameMonitor) {
            this.mAudioSamples.pop();
            this.mFrameMonitor.notifyAll();
        }
    }

    public long getTimestampNs() {
        return this.mPresentationTimeUs * 1000;
    }

    public void grabSample(FrameValue audioFrame) {
        synchronized (this.mFrameMonitor) {
            audioFrame.setValue((AudioSample) this.mAudioSamples.getFirst());
            audioFrame.setTimestamp(getTimestampNs());
        }
    }

    public void clearBuffer() {
        synchronized (this.mFrameMonitor) {
            this.mAudioSamples.clear();
        }
    }

    private boolean waitForBufferAvailable() {
        boolean z;
        synchronized (this.mFrameMonitor) {
            while (this.mAudioSamples.size() >= 10) {
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
}
