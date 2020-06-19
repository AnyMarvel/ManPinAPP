package androidx.media.filterpacks.image;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import androidx.core.view.ViewCompat;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import androidx.media.filterfw.FrameImage2D;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.ImageShader;
import androidx.media.filterfw.InputPort;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.RenderTarget;
import androidx.media.filterfw.Signature;
import androidx.media.filterfw.ViewFilter;

public class SurfaceHolderTarget extends ViewFilter {
    private boolean mHasSurface = false;
    private RenderTarget mRenderTarget = null;
    private ImageShader mShader = null;
    private SurfaceHolder mSurfaceHolder = null;
    private Callback mSurfaceHolderListener = new Callback() {
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            SurfaceHolderTarget.this.onSurfaceCreated(holder);
        }

        public void surfaceCreated(SurfaceHolder holder) {
            SurfaceHolderTarget.this.onSurfaceCreated(holder);
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            SurfaceHolderTarget.this.onDestroySurface();
        }
    };

    public SurfaceHolderTarget(MffContext context, String name) {
        super(context, name);
    }

    public void onBindToView(View view) {
        if (view instanceof SurfaceView) {
            SurfaceHolder holder = ((SurfaceView) view).getHolder();
            if (holder == null) {
                String valueOf = String.valueOf(view);
                throw new RuntimeException(new StringBuilder(String.valueOf(valueOf).length() + 46).append("Could not get SurfaceHolder from SurfaceView ").append(valueOf).append("!").toString());
            } else {
                setSurfaceHolder(holder);
                return;
            }
        }
        throw new IllegalArgumentException("View must be a SurfaceView!");
    }

    public synchronized void setSurfaceHolder(SurfaceHolder holder) {
        if (isRunning()) {
            throw new IllegalStateException("Cannot set SurfaceHolder while running!");
        }
        this.mSurfaceHolder = holder;
    }

    public synchronized void onDestroySurface() {
        if (this.mRenderTarget != null) {
            this.mRenderTarget.release();
            this.mRenderTarget = null;
        }
        this.mHasSurface = false;
    }

    public Signature getSignature() {
        return super.getSignature().addInputPort("image", 2, FrameType.image2D(FrameType.ELEMENT_RGBA8888, 2)).disallowOtherPorts();
    }

    protected void onInputPortOpen(InputPort port) {
        super.connectViewInputs(port);
    }

    protected synchronized void onPrepare() {
        if (isOpenGLSupported()) {
            this.mShader = ImageShader.createIdentity();
        }
    }

    protected synchronized void onOpen() {
        this.mSurfaceHolder.addCallback(this.mSurfaceHolderListener);
        Surface surface = this.mSurfaceHolder.getSurface();
        boolean z = surface != null && surface.isValid();
        this.mHasSurface = z;
    }

    protected synchronized void onProcess() {
        FrameImage2D image = getConnectedInputPort("image").pullFrame().asFrameImage2D();
        if (this.mHasSurface) {
            synchronized (this.mSurfaceHolder) {
                if (isOpenGLSupported()) {
                    renderGL(image);
                } else {
                    renderCanvas(image);
                }
            }
        }
    }

    private void renderGL(FrameImage2D image) {
        if (this.mRenderTarget == null) {
            this.mRenderTarget = RenderTarget.currentTarget().forSurfaceHolder(this.mSurfaceHolder);
            this.mRenderTarget.registerAsDisplaySurface();
        }
        Rect frameRect = new Rect(0, 0, image.getWidth(), image.getHeight());
        Rect surfRect = this.mSurfaceHolder.getSurfaceFrame();
        setupShader(this.mShader, frameRect, surfRect);
        this.mShader.process(image.lockTextureSource(), this.mRenderTarget, surfRect.width(), surfRect.height());
        image.unlock();
        this.mRenderTarget.swapBuffers();
    }

    private void renderCanvas(FrameImage2D image) {
        Canvas canvas = this.mSurfaceHolder.lockCanvas();
        Bitmap bitmap = image.toBitmap();
        Rect sourceRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        Rect surfaceRect = this.mSurfaceHolder.getSurfaceFrame();
        RectF targetRect = getTargetRect(sourceRect, surfaceRect);
        canvas.drawColor(ViewCompat.MEASURED_STATE_MASK);
        if (targetRect.width() > 0.0f && targetRect.height() > 0.0f) {
            canvas.scale((float) surfaceRect.width(), (float) surfaceRect.height());
            canvas.drawBitmap(bitmap, sourceRect, targetRect, new Paint());
        }
        this.mSurfaceHolder.unlockCanvasAndPost(canvas);
    }

    protected synchronized void onClose() {
        if (this.mRenderTarget != null) {
            this.mRenderTarget.unregisterAsDisplaySurface();
            this.mRenderTarget.release();
            this.mRenderTarget = null;
        }
        if (this.mSurfaceHolder != null) {
            this.mSurfaceHolder.removeCallback(this.mSurfaceHolderListener);
        }
    }

    private synchronized void onSurfaceCreated(SurfaceHolder holder) {
        if (this.mSurfaceHolder != holder) {
            throw new RuntimeException("Unexpected Holder!");
        }
        this.mHasSurface = true;
    }
}
