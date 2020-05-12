package androidx.media.filterpacks.video;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.SurfaceTexture;
import android.graphics.SurfaceTexture.OnFrameAvailableListener;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.net.Uri;
import android.util.Log;
import android.view.Surface;
import androidx.media.filterfw.Filter;
import androidx.media.filterfw.FrameImage2D;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.ImageShader;
import androidx.media.filterfw.InputPort;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.OutputPort;
import androidx.media.filterfw.Signature;
import androidx.media.filterfw.TextureSource;
import androidx.media.filterfw.decoder.MediaDecoder;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@TargetApi(14)
public class MediaPlayerSource extends Filter {
    private static final FrameType INPUT_ASSET_TYPE = FrameType.single(AssetFileDescriptor.class);
    private static final FrameType INPUT_PATH_TYPE = FrameType.single(Uri.class);
    private static final FrameType OUTPUT_VIDEO_TYPE = FrameType.image2D(FrameType.ELEMENT_RGBA8888, 16);
    private static final String TAG = "MediaPlayerSource";
    private static final float[] TARGET_COORDS_0 = new float[]{0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f};
    private static final float[] TARGET_COORDS_180 = new float[]{1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f};
    private static final float[] TARGET_COORDS_270 = new float[]{1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f};
    private static final float[] TARGET_COORDS_90 = new float[]{0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f};
    private static final String mFrameShader = "#extension GL_OES_EGL_image_external : require\nprecision mediump float;\nuniform samplerExternalOES tex_sampler_0;\nvarying vec2 v_texcoord;\nvoid main() {\n  gl_FragColor = texture2D(tex_sampler_0, v_texcoord);\n}\n";
    private static float[] mSurfaceTransform = new float[16];
    private boolean mCompleted;
    private ImageShader mFrameExtractor;
    private Object mFrameMutex = new Object();
    private int mHeight;
    private final boolean mLogVerbose = Log.isLoggable(TAG, 2);
    private boolean mLooping = false;
    private TextureSource mMediaFrame;
    private MediaPlayer mMediaPlayer;
    private boolean mNewFrameAvailable;
    private boolean mPaused;
    private int mRotation = 0;
    private Uri mSourceUri;
    private SurfaceTexture mSurfaceTexture;
    private float mVolume = 0.0f;
    private int mWidth;
    private OnCompletionListener onCompletionListener = new OnCompletionListener() {
        public void onCompletion(MediaPlayer mp) {
            MediaPlayerSource.this.vLog("MediaPlayer has completed playback");
            synchronized (MediaPlayerSource.this.mFrameMutex) {
                MediaPlayerSource.this.mCompleted = true;
            }
            MediaPlayerSource.this.wakeUp();
        }
    };
    private OnFrameAvailableListener onMediaFrameAvailableListener = new OnFrameAvailableListener() {
        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
            if (MediaPlayerSource.this.mLogVerbose) {
                Log.v(MediaPlayerSource.TAG, "New frame from media player");
            }
            synchronized (MediaPlayerSource.this.mFrameMutex) {
                MediaPlayerSource.this.mNewFrameAvailable = true;
            }
            MediaPlayerSource.this.vLog("New frame: wakeUp");
            MediaPlayerSource.this.wakeUp();
        }
    };
    private OnPreparedListener onPreparedListener = new OnPreparedListener() {
        public void onPrepared(MediaPlayer mp) {
            MediaPlayerSource.this.vLog("MediaPlayer is prepared");
            synchronized (MediaPlayerSource.this) {
                MediaPlayerSource.this.mMediaPlayer.start();
            }
        }
    };
    private OnVideoSizeChangedListener onVideoSizeChangedListener = new OnVideoSizeChangedListener() {
        public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
            MediaPlayerSource.this.vLog("MediaPlayer sent dimensions: " + width + " x " + height);
            synchronized (MediaPlayerSource.this.mFrameMutex) {
                MediaPlayerSource.this.mWidth = width;
                MediaPlayerSource.this.mHeight = height;
            }
        }
    };

    private static float[] getRotationCoords(int rotation) {
        switch (rotation) {
            case 0:
                return TARGET_COORDS_0;
            case 90:
                return TARGET_COORDS_90;
            case MediaDecoder.ROTATE_180 /*180*/:
                return TARGET_COORDS_180;
            case MediaDecoder.ROTATE_90_LEFT /*270*/:
                return TARGET_COORDS_270;
            default:
                throw new RuntimeException("Unsupported rotation angle.");
        }
    }

    private void vLog(String message) {
        if (this.mLogVerbose) {
            Log.v(TAG, message);
        }
    }

    public MediaPlayerSource(MffContext context, String name) {
        super(context, name);
    }

    public Signature getSignature() {
        return new Signature().addInputPort("sourceUri", 1, INPUT_PATH_TYPE).addInputPort("sourceAsset", 1, INPUT_ASSET_TYPE).addInputPort("context", 1, FrameType.single(Context.class)).addInputPort("loop", 1, FrameType.single(Boolean.TYPE)).addInputPort("volume", 1, FrameType.single(Float.TYPE)).addInputPort("rotation", 1, FrameType.single(Integer.TYPE)).addOutputPort("video", 2, OUTPUT_VIDEO_TYPE).disallowOtherPorts();
    }

    public void onInputPortOpen(InputPort port) {
        if (port.getName().equals("sourceUri")) {
            port.bindToFieldNamed("mSourceUri");
            port.setAutoPullEnabled(true);
        } else if (port.getName().equals("loop")) {
            port.bindToFieldNamed("mLooping");
            port.setAutoPullEnabled(true);
        } else if (port.getName().equals("volume")) {
            port.bindToFieldNamed("mVolume");
            port.setAutoPullEnabled(true);
        } else if (port.getName().equals("rotation")) {
            port.bindToFieldNamed("mRotation");
            port.setAutoPullEnabled(true);
        }
    }

    protected void onPrepare() {
        this.mFrameExtractor = new ImageShader(mFrameShader);
    }

    public void onOpen() {
        String valueOf = String.valueOf(this.mSourceUri);
        vLog(new StringBuilder(String.valueOf(valueOf).length() + 15).append("Current URL is ").append(valueOf).toString());
        this.mMediaFrame = TextureSource.newExternalTexture();
        this.mSurfaceTexture = new SurfaceTexture(this.mMediaFrame.getTextureId());
        if (!setupMediaPlayer()) {
            throw new RuntimeException("Error setting up MediaPlayer!");
        }
    }

    private boolean nextFrame() {
        boolean frameAvailable;
        synchronized (this.mFrameMutex) {
            frameAvailable = this.mNewFrameAvailable;
            if (frameAvailable) {
                this.mNewFrameAvailable = false;
            } else {
                enterSleepState();
            }
        }
        return frameAvailable;
    }

    public void onProcess() {
        vLog("Processing new frame");
        if (this.mMediaPlayer == null) {
            throw new NullPointerException("Unexpected null media player!");
        } else if (this.mCompleted) {
            requestClose();
        } else if (nextFrame()) {
            int outputWidth;
            int outputHeight;
            this.mSurfaceTexture.updateTexImage();
            this.mSurfaceTexture.getTransformMatrix(mSurfaceTransform);
            this.mFrameExtractor.setSourceTransform(mSurfaceTransform);
            this.mFrameExtractor.setTargetCoords(getRotationCoords(this.mRotation));
            synchronized (this.mFrameMutex) {
                outputWidth = this.mWidth;
                outputHeight = this.mHeight;
                if (this.mRotation == 90 || this.mRotation == MediaDecoder.ROTATE_90_LEFT) {
                    outputWidth = this.mHeight;
                    outputHeight = this.mWidth;
                }
            }
            int[] dims = new int[]{outputWidth, outputHeight};
            OutputPort outPort = getConnectedOutputPort("video");
            FrameImage2D outFrame = outPort.fetchAvailableFrame(dims).asFrameImage2D();
            this.mFrameExtractor.process(this.mMediaFrame, outFrame.lockRenderTarget(), outputWidth, outputHeight);
            long timestamp = TimeUnit.NANOSECONDS.convert((long) this.mMediaPlayer.getCurrentPosition(), TimeUnit.MILLISECONDS);
            vLog("Timestamp: " + (timestamp / 1000000) + " ms");
            outFrame.setTimestamp(timestamp);
            outFrame.unlock();
            outPort.pushFrame(outFrame);
        }
    }

    public void onClose() {
        if (this.mMediaPlayer.isPlaying()) {
            this.mMediaPlayer.stop();
        }
        this.mPaused = false;
        this.mCompleted = false;
        this.mNewFrameAvailable = false;
        this.mMediaPlayer.release();
        this.mMediaPlayer = null;
        this.mSurfaceTexture.release();
        this.mSurfaceTexture = null;
        vLog("MediaSource closed");
    }

    public void onTearDown() {
        if (this.mMediaFrame != null) {
            this.mMediaFrame.release();
        }
    }

    public synchronized void pauseVideo(boolean pauseState) {
        if (isOpen()) {
            if (pauseState && !this.mPaused) {
                this.mMediaPlayer.pause();
            } else if (!pauseState) {
                if (this.mPaused) {
                    this.mMediaPlayer.start();
                }
            }
        }
        this.mPaused = pauseState;
    }

    private synchronized boolean setupMediaPlayer() {
        this.mPaused = false;
        this.mCompleted = false;
        this.mNewFrameAvailable = false;
        vLog("Setting up playback.");
        if (this.mMediaPlayer != null) {
            vLog("Resetting existing MediaPlayer.");
            this.mMediaPlayer.reset();
        } else {
            vLog("Creating new MediaPlayer.");
            this.mMediaPlayer = new MediaPlayer();
        }
        if (this.mMediaPlayer == null) {
            throw new RuntimeException("Unable to create a MediaPlayer!");
        }
        try {
            String valueOf = String.valueOf(this.mSourceUri);
            vLog(new StringBuilder(String.valueOf(valueOf).length() + 30).append("Setting MediaPlayer source to ").append(valueOf).toString());
            this.mMediaPlayer.setDataSource(getContext().getApplicationContext(), this.mSourceUri);
            this.mMediaPlayer.setLooping(this.mLooping);
            this.mMediaPlayer.setVolume(this.mVolume, this.mVolume);
            Surface surface = new Surface(this.mSurfaceTexture);
            this.mMediaPlayer.setSurface(surface);
            surface.release();
            this.mMediaPlayer.setOnVideoSizeChangedListener(this.onVideoSizeChangedListener);
            this.mMediaPlayer.setOnPreparedListener(this.onPreparedListener);
            this.mMediaPlayer.setOnCompletionListener(this.onCompletionListener);
            this.mSurfaceTexture.setOnFrameAvailableListener(this.onMediaFrameAvailableListener);
            vLog("Preparing MediaPlayer.");
            this.mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            this.mMediaPlayer.release();
            this.mMediaPlayer = null;
            throw new RuntimeException(String.format("Unable to set MediaPlayer to %s!", new Object[]{this.mSourceUri}), e);
        } catch (IllegalArgumentException e2) {
            this.mMediaPlayer.release();
            this.mMediaPlayer = null;
            throw new RuntimeException(String.format("Unable to set MediaPlayer to URL %s!", new Object[]{this.mSourceUri}), e2);
        }
        return true;
    }
}
