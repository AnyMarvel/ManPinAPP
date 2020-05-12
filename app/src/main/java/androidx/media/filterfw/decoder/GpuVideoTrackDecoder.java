package androidx.media.filterfw.decoder;

import android.annotation.TargetApi;
import android.graphics.SurfaceTexture;
import android.graphics.SurfaceTexture.OnFrameAvailableListener;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaFormat;
import android.opengl.Matrix;
import android.view.Surface;
import androidx.media.filterfw.FrameImage2D;
import androidx.media.filterfw.FrameValue;
import androidx.media.filterfw.ImageShader;
import androidx.media.filterfw.TextureSource;
import androidx.media.filterfw.geometry.ScaleUtils;
import java.io.IOException;
import java.nio.ByteBuffer;

@TargetApi(16)
public class GpuVideoTrackDecoder extends VideoTrackDecoder {
    private boolean mCurrentIsKeyFrame;
    private long mCurrentPresentationTimeUs;
    private ImageShader mImageShader;
    private final int mOutputHeight;
    private final int mOutputWidth;
    private SurfaceTexture mSurfaceTexture;
    private TextureSource mTextureSource;
    private float[] mTransformMatrix = new float[16];

    public GpuVideoTrackDecoder(int trackIndex, MediaFormat format, Listener listener) {
        super(trackIndex, format, listener);
        this.mOutputWidth = format.getInteger("width");
        this.mOutputHeight = format.getInteger("height");
    }

    public long getTimestampNs() {
        return this.mCurrentPresentationTimeUs * 1000;
    }

    protected MediaCodec initMediaCodec(MediaFormat format) {
        this.mTextureSource = TextureSource.newExternalTexture();
        this.mSurfaceTexture = new SurfaceTexture(this.mTextureSource.getTextureId());
        this.mSurfaceTexture.setOnFrameAvailableListener(new OnFrameAvailableListener() {
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                GpuVideoTrackDecoder.this.markFrameAvailable();
            }
        });
        try {
            Surface surface = new Surface(this.mSurfaceTexture);
            MediaCodec mediaCodec = MediaCodec.createDecoderByType(format.getString("mime"));
            mediaCodec.configure(format, surface, null, 0);
            surface.release();
            return mediaCodec;
        } catch (IOException e) {
            return null;
        }
    }

    protected boolean onDataAvailable(MediaCodec codec, ByteBuffer[] buffers, int bufferIndex, BufferInfo info, boolean isKeyFrame) {
        boolean textureAvailable = waitForFrameGrabs();
        this.mCurrentPresentationTimeUs = info.presentationTimeUs;
        this.mCurrentIsKeyFrame = isKeyFrame;
        codec.releaseOutputBuffer(bufferIndex, textureAvailable);
        if (textureAvailable && waitForOnFrameAvailable()) {
            notifyListener();
        }
        return false;
    }

    private boolean waitForOnFrameAvailable() {
        boolean z;
        synchronized (this.mFrameMonitor) {
            while (!this.mFrameAvailable) {
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

    private boolean requiresSwappingDimensions(float[] matrix) {
        if (Math.abs(((matrix[0] + (matrix[4] * 0.5f)) + matrix[12]) - 0.5f) < Math.abs(((matrix[1] + (matrix[5] * 0.5f)) + matrix[13]) - 0.5f)) {
            return true;
        }
        return false;
    }

    private static float[] createRotation(int rotation, boolean flipVertically) {
        if (rotation % 90 != 0) {
            throw new IllegalArgumentException("Rotation must be a multiple of 90!");
        }
        float[] matrix = new float[16];
        Matrix.setIdentityM(matrix, 0);
        Matrix.translateM(matrix, 0, 0.5f, 0.5f, 0.0f);
        Matrix.rotateM(matrix, 0, (float) (360 - rotation), 0.0f, 0.0f, 1.0f);
        Matrix.translateM(matrix, 0, -0.5f, -0.5f, 0.0f);
        if (flipVertically) {
            Matrix.translateM(matrix, 0, 0.0f, 1.0f, 0.0f);
            Matrix.scaleM(matrix, 0, 1.0f, -1.0f, 1.0f);
        }
        return matrix;
    }

    protected void copyFrameDataTo(FrameImage2D outputVideoFrame, FrameValue infoFrame, int maxDim, int rotation) {
        this.mSurfaceTexture.updateTexImage();
        this.mTransformMatrix = createRotation(rotation, true);
        ImageShader imageShader = getImageShader();
        imageShader.setSourceTransform(this.mTransformMatrix);
        int[] dimensions = new int[]{this.mOutputWidth, this.mOutputHeight};
        if (requiresSwappingDimensions(this.mTransformMatrix)) {
            int x = dimensions[0];
            dimensions[0] = dimensions[1];
            dimensions[1] = x;
        }
        dimensions = ScaleUtils.scaleDown(dimensions[0], dimensions[1], maxDim);
        outputVideoFrame.resize(dimensions);
        imageShader.process(this.mTextureSource, outputVideoFrame.lockRenderTarget(), dimensions[0], dimensions[1]);
        outputVideoFrame.setTimestamp(getTimestampNs());
        outputVideoFrame.unlock();
        if (infoFrame != null) {
            infoFrame.setValue(new VideoFrameInfo(this.mCurrentIsKeyFrame));
            infoFrame.setTimestamp(getTimestampNs());
        }
    }

    public void release() {
        super.release();
        synchronized (this.mFrameMonitor) {
            this.mTextureSource.release();
            this.mSurfaceTexture.release();
        }
    }

    private ImageShader getImageShader() {
        if (this.mImageShader == null) {
            this.mImageShader = ImageShader.createExternalIdentity();
            this.mImageShader.setTargetRect(0.0f, 1.0f, 1.0f, -1.0f);
        }
        return this.mImageShader;
    }
}
