package androidx.media.filterpacks.image;

import android.annotation.TargetApi;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.os.ConditionVariable;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;
import androidx.media.filterfw.FrameImage2D;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.ImageShader;
import androidx.media.filterfw.InputPort;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.RenderTarget;
import androidx.media.filterfw.Signature;
import androidx.media.filterfw.ViewFilter;

@TargetApi(14)
public class SurfaceTextureTarget extends ViewFilter {
    private static final int MAX_WAIT_FOR_VIEW_TIME = 1000;
    private RenderTarget mRenderTarget = null;
    private ImageShader mShader = null;
    private SurfaceTexture mSurfaceTexture = null;
    private Rect mSurfaceTextureRect;
    private TextureView mView = null;
    private ConditionVariable mWaitForST = new ConditionVariable(true);

    private class TextureListener implements SurfaceTextureListener {
        private TextureListener() {
        }

        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            SurfaceTextureTarget.this.setSurfaceTexture(surface, width, height);
            SurfaceTextureTarget.this.mWaitForST.open();
        }

        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            SurfaceTextureTarget.this.updateSurfaceTexture(width, height);
        }

        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            SurfaceTextureTarget.this.killSurfaceTexture();
            return true;
        }

        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    }

    public SurfaceTextureTarget(MffContext context, String name) {
        super(context, name);
    }

    public synchronized void onBindToView(View view) {
        if (view instanceof TextureView) {
            this.mView = (TextureView) view;
        } else {
            throw new IllegalArgumentException("View must be a TextureView!");
        }
    }

    public synchronized void setSurfaceTexture(SurfaceTexture surfaceTexture, int width, int height) {
        if (isRunning()) {
            throw new IllegalStateException("Cannot set SurfaceTexture while running!");
        }
        this.mSurfaceTexture = surfaceTexture;
        updateSurfaceTexture(width, height);
    }

    public synchronized void updateSurfaceTexture(int width, int height) {
        this.mSurfaceTextureRect = new Rect(0, 0, width, height);
    }

    public Signature getSignature() {
        return super.getSignature().addInputPort("image", 2, FrameType.image2D(FrameType.ELEMENT_RGBA8888, 2)).disallowOtherPorts();
    }

    protected void onInputPortOpen(InputPort port) {
        super.connectViewInputs(port);
    }

    protected void onOpen() {
        if (this.mView != null) {
            openView();
        }
        if (this.mSurfaceTexture == null) {
            throw new NullPointerException("SurfaceTextureTarget has no SurfaceTexture!");
        }
        this.mRenderTarget = RenderTarget.currentTarget().forSurfaceTexture(this.mSurfaceTexture);
        this.mShader = ImageShader.createIdentity();
    }

    protected synchronized void onProcess() {
        FrameImage2D image = getConnectedInputPort("image").pullFrame().asFrameImage2D();
        if (this.mSurfaceTexture != null) {
            synchronized (this.mSurfaceTexture) {
                setupShader(this.mShader, new Rect(0, 0, image.getWidth(), image.getHeight()), this.mSurfaceTextureRect);
                this.mShader.process(image.lockTextureSource(), this.mRenderTarget, this.mSurfaceTextureRect.width(), this.mSurfaceTextureRect.height());
                image.unlock();
                this.mRenderTarget.swapBuffers();
            }
        }
    }

    protected void onClose() {
        if (this.mRenderTarget != null) {
            this.mRenderTarget.release();
            this.mRenderTarget = null;
        }
        if (this.mView != null) {
            closeView();
            this.mView = null;
        }
    }

    private void openView() {
        if (this.mView.getSurfaceTextureListener() != null) {
            throw new RuntimeException("TextureView is already hooked up to another listener!");
        }
        this.mWaitForST.close();
        this.mView.setSurfaceTextureListener(new TextureListener());
        if (this.mView.isAvailable()) {
            setSurfaceTexture(this.mView.getSurfaceTexture(), this.mView.getWidth(), this.mView.getHeight());
            this.mWaitForST.open();
        }
        if (!this.mWaitForST.block(1000)) {
            throw new RuntimeException("Timed out waiting for TextureView to become available!");
        }
    }

    private void closeView() {
        this.mView.setSurfaceTextureListener(null);
    }

    private void killSurfaceTexture() {
        this.mSurfaceTexture = null;
        if (this.mView != null) {
            closeView();
            this.mView = null;
        }
    }
}
