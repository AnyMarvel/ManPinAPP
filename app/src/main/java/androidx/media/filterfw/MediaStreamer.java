package androidx.media.filterfw;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.graphics.SurfaceTexture.OnFrameAvailableListener;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.Surface;
import androidx.media.filterfw.decoder.VideoStreamProvider;
import androidx.media.filterfw.geometry.ScaleUtils;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

@TargetApi(18)
public class MediaStreamer implements VideoStreamProvider {
    private final Set<VideoFrameConsumer> mConsumers;
    private Surface mFrameSurface;
    private SurfaceTexture mFrameSurfaceTexture;
    private TextureSource mFrameTexture;
    private int mMediaHeight;
    private final MediaPlayer mMediaPlayer;
    private int mMediaWidth;
    private final Handler mPlayerHandler;
    private RenderTarget mPlayerTarget;
    private final HandlerThread mPlayerThread;

    private class CommandInitThread implements Runnable {
        private CommandInitThread() {
        }

        public void run() {
            MediaStreamer.this.mPlayerTarget = RenderTarget.newTarget(1, 1);
            MediaStreamer.this.mPlayerTarget.focus();
            MediaStreamer.this.mFrameTexture = TextureSource.newExternalTexture();
            MediaStreamer.this.mFrameSurfaceTexture = new SurfaceTexture(MediaStreamer.this.mFrameTexture.getTextureId());
            MediaStreamer.this.mFrameSurface = new Surface(MediaStreamer.this.mFrameSurfaceTexture);
            MediaStreamer.this.mMediaPlayer.setSurface(MediaStreamer.this.mFrameSurface);
            MediaStreamer.this.mFrameSurfaceTexture.detachFromGLContext();
            MediaStreamer.this.mFrameSurfaceTexture.setOnFrameAvailableListener(new OnFrameAvailableListener() {
                public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                    MediaStreamer.this.mPlayerHandler.post(new CommandUpdateServerFrame());
                }
            });
        }
    }

    private class CommandReleaseResources implements Runnable {
        private CommandReleaseResources() {
        }

        public void run() {
            MediaStreamer.this.mMediaPlayer.release();
            MediaStreamer.this.mFrameSurface.release();
            MediaStreamer.this.mFrameSurfaceTexture.release();
            MediaStreamer.this.mFrameTexture.release();
            MediaStreamer.this.mPlayerTarget.release();
        }
    }

    private class CommandUpdateServerFrame implements Runnable {
        private CommandUpdateServerFrame() {
        }

        public void run() {
            synchronized (MediaStreamer.this.mFrameSurfaceTexture) {
                MediaStreamer.this.mFrameSurfaceTexture.attachToGLContext(MediaStreamer.this.mFrameTexture.getTextureId());
                MediaStreamer.this.mFrameSurfaceTexture.updateTexImage();
                MediaStreamer.this.mFrameSurfaceTexture.detachFromGLContext();
            }
            MediaStreamer.this.onProgress();
        }
    }

    public MediaStreamer(Context context, File mediaFile) {
        this(context, Uri.fromFile(mediaFile));
    }

    public MediaStreamer(Context context, Uri mediaUri) {
        this.mConsumers = new HashSet();
        this.mMediaWidth = 1;
        this.mMediaHeight = 1;
        this.mMediaPlayer = MediaPlayer.create(context, mediaUri);
        this.mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
            public void onCompletion(MediaPlayer mediaPlayer) {
                MediaStreamer.this.onStop();
            }
        });
        this.mMediaPlayer.setOnVideoSizeChangedListener(new OnVideoSizeChangedListener() {
            public void onVideoSizeChanged(MediaPlayer mediaPlayer, int width, int height) {
                MediaStreamer.this.mMediaWidth = width;
                MediaStreamer.this.mMediaHeight = height;
            }
        });
        this.mPlayerThread = new HandlerThread("MediaStreamer");
        this.mPlayerThread.start();
        this.mPlayerHandler = new Handler(this.mPlayerThread.getLooper());
        this.mPlayerHandler.post(new CommandInitThread());
    }

    public void start() {
        onStart();
        this.mMediaPlayer.start();
    }

    public void stop() {
        this.mMediaPlayer.stop();
    }

    public long getDurationNs() {
        return ((long) this.mMediaPlayer.getDuration()) * 1000000;
    }

    public void skipVideoFrame() {
    }

    public boolean grabVideoFrame(FrameImage2D outputVideoFrame, FrameValue infoFrame, int maxDim) {
        synchronized (this.mFrameSurfaceTexture) {
            TextureSource targetTex = TextureSource.newExternalTexture();
            ImageShader copyShader = RenderTarget.currentTarget().getExternalIdentityShader();
            this.mFrameSurfaceTexture.attachToGLContext(targetTex.getTextureId());
            int[] dimensions = ScaleUtils.scaleDown(this.mMediaWidth, this.mMediaHeight, maxDim);
            outputVideoFrame.resize(dimensions);
            copyShader.process(targetTex, outputVideoFrame.lockRenderTarget(), dimensions[0], dimensions[1]);
            outputVideoFrame.unlock();
            outputVideoFrame.setTimestamp(this.mFrameSurfaceTexture.getTimestamp());
            this.mFrameSurfaceTexture.detachFromGLContext();
            targetTex.release();
        }
        return true;
    }

    public void addVideoFrameConsumer(VideoFrameConsumer consumer) {
        synchronized (this.mConsumers) {
            this.mConsumers.add(consumer);
        }
    }

    public void removeVideoFrameConsumer(VideoFrameConsumer consumer) {
        synchronized (this.mConsumers) {
            this.mConsumers.remove(consumer);
        }
    }

    public void release() {
        this.mMediaPlayer.stop();
        this.mPlayerHandler.post(new CommandReleaseResources());
        this.mPlayerThread.quitSafely();
    }

    private void onStart() {
        synchronized (this.mConsumers) {
            for (VideoFrameConsumer consumer : this.mConsumers) {
                consumer.onVideoStreamStarted();
            }
        }
    }

    private void onStop() {
        synchronized (this.mConsumers) {
            for (VideoFrameConsumer consumer : this.mConsumers) {
                consumer.onVideoStreamStopped();
            }
        }
    }

    private void onProgress() {
        synchronized (this.mConsumers) {
            long timestampNs = this.mFrameSurfaceTexture.getTimestamp();
            for (VideoFrameConsumer consumer : this.mConsumers) {
                consumer.onVideoFrameAvailable(this, timestampNs);
            }
        }
    }
}
