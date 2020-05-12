package androidx.media.filterfw.decoder;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;

import androidx.media.filterfw.FrameImage2D;
import androidx.media.filterfw.FrameValue;
import androidx.media.filterfw.VideoFrameConsumer;
import androidx.media.filterfw.geometry.ScaleUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ImageDecoder implements VideoStreamProvider {
    private static final long CONSUMER_REGISTRATION_DELAY_MS = 2000;
    private static final long INTER_FRAME_TIME_GAP_MS = 250;
    private static final long NS_IN_S = 1000000000;
    private final long mConsumerRegistrationDelay;
    private final List<VideoFrameConsumer> mConsumers;
    private final HandlerThread mDecoderThread;
    private final Handler mHandler;
    private final AtomicInteger mImageIndex;
    private final Bitmap[] mImages;
    private boolean mIsRunning;
    private final AtomicInteger mWaitingConsumers;

    public static ImageDecoder createFromUri(Uri uri) {
        Options options = new Options();
        options.inPreferQualityOverSpeed = true;
        if (uri == null) {
            throw new IllegalArgumentException("Image uri is empty!");
        }
        return new ImageDecoder(new Bitmap[]{BitmapFactory.decodeFile(uri.getPath(), options)});
    }

    public ImageDecoder(Bitmap[] bitmaps) {
        this(bitmaps, CONSUMER_REGISTRATION_DELAY_MS);
    }

    public ImageDecoder(Bitmap[] bitmaps, long consumerRegistrationDelayMs) {
        this.mWaitingConsumers = new AtomicInteger(0);
        this.mImageIndex = new AtomicInteger(0);
        this.mConsumers = new ArrayList();
        this.mImages = bitmaps;
        this.mConsumerRegistrationDelay = consumerRegistrationDelayMs;
        this.mDecoderThread = new HandlerThread("ImageDecoder");
        this.mDecoderThread.start();
        this.mHandler = new Handler(this.mDecoderThread.getLooper());
    }

    public void start() {
        this.mHandler.postDelayed(new Runnable() {
            public void run() {
                synchronized (ImageDecoder.this.mConsumers) {
                    ImageDecoder.this.mIsRunning = true;
                    for (VideoFrameConsumer consumer : ImageDecoder.this.mConsumers) {
                        consumer.onVideoStreamStarted();
                    }
                }
                ImageDecoder.this.signalNewFrame();
            }
        }, this.mConsumerRegistrationDelay);
    }

    public void stop() {
        this.mDecoderThread.quitSafely();
    }

    public long getDurationNs() {
        return (long) this.mImages.length;
    }

    public void skipVideoFrame() {
        decrementConsumersAndSignalNextFrame();
    }

    public boolean grabVideoFrame(FrameImage2D outputVideoFrame, FrameValue infoFrame, int maxDim) {
        if (this.mWaitingConsumers.get() <= 0 || this.mImageIndex.get() >= this.mImages.length) {
            return false;
        }
        Bitmap image = this.mImages[this.mImageIndex.get()];
        int[] dimensions = ScaleUtils.scaleDown(image.getWidth(), image.getHeight(), maxDim);
        Bitmap scaled = Bitmap.createScaledBitmap(image, dimensions[0], dimensions[1], false);
        outputVideoFrame.resize(dimensions);
        outputVideoFrame.setBitmap(scaled);
        decrementConsumersAndSignalNextFrame();
        return true;
    }

    public void addVideoFrameConsumer(VideoFrameConsumer consumer) {
        synchronized (this.mConsumers) {
            if (this.mIsRunning) {
                String str = "Attempt to register a consumer while ImageDecoder is running. ";
                String valueOf = String.valueOf(Thread.currentThread().getName());
                throw new IllegalStateException(valueOf.length() != 0 ? str.concat(valueOf) : new String(str));
            }
            this.mConsumers.add(consumer);
        }
        this.mWaitingConsumers.set(this.mConsumers.size());
    }

    public void removeVideoFrameConsumer(VideoFrameConsumer consumer) {
        synchronized (this.mConsumers) {
            if (this.mIsRunning) {
                String str = "Attempt to unregister a consumer while ImageDecoder is running. ";
                String valueOf = String.valueOf(Thread.currentThread().getName());
                throw new IllegalStateException(valueOf.length() != 0 ? str.concat(valueOf) : new String(str));
            }
            this.mConsumers.remove(consumer);
        }
        this.mWaitingConsumers.set(this.mConsumers.size());
    }

    private void decrementConsumersAndSignalNextFrame() {
        synchronized (this.mWaitingConsumers) {
            if (this.mWaitingConsumers.decrementAndGet() == 0) {
                if (this.mImageIndex.incrementAndGet() < this.mImages.length) {
                    this.mWaitingConsumers.set(this.mConsumers.size());
                    signalNewFrame();
                } else {
                    signalStop();
                }
            }
        }
    }

    private void signalNewFrame() {
        this.mHandler.postDelayed(new Runnable() {
            public void run() {
                long timestamp = 10000000000L * ((long) ImageDecoder.this.mImageIndex.get());
                synchronized (ImageDecoder.this.mConsumers) {
                    for (VideoFrameConsumer consumer : ImageDecoder.this.mConsumers) {
                        consumer.onVideoFrameAvailable(ImageDecoder.this, timestamp);
                    }
                }
            }
        }, INTER_FRAME_TIME_GAP_MS);
    }

    private void signalStop() {
        this.mHandler.postDelayed(new Runnable() {
            public void run() {
                synchronized (ImageDecoder.this.mConsumers) {
                    ImageDecoder.this.mIsRunning = false;
                    for (VideoFrameConsumer consumer : ImageDecoder.this.mConsumers) {
                        consumer.onVideoStreamStopped();
                    }
                }
            }
        }, INTER_FRAME_TIME_GAP_MS);
    }
}
