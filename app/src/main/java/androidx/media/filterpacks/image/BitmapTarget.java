package androidx.media.filterpacks.image;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.Signature;
import androidx.media.filterfw.ViewFilter;

public class BitmapTarget extends ViewFilter {
    private Handler mHandler = null;
    private ImageView mImageView = null;
    private BitmapListener mListener = null;

    public interface BitmapListener {
        void onReceivedBitmap(Bitmap bitmap);
    }

    public BitmapTarget(MffContext context, String name) {
        super(context, name);
    }

    public void onBindToView(View view) {
        if (view instanceof ImageView) {
            this.mImageView = (ImageView) view;
            return;
        }
        throw new IllegalArgumentException("View must be an ImageView!");
    }

    public void setListener(BitmapListener listener, boolean onCallerThread) {
        if (isRunning()) {
            throw new IllegalStateException("Attempting to bind filter to callback while it is running!");
        }
        this.mListener = listener;
        if (!onCallerThread) {
            return;
        }
        if (Looper.myLooper() == null) {
            throw new IllegalArgumentException("Attempting to set callback on thread which has no looper!");
        }
        this.mHandler = new Handler();
    }

    public Signature getSignature() {
        return new Signature().addInputPort("image", 2, FrameType.image2D(FrameType.ELEMENT_RGBA8888, 1)).disallowOtherPorts();
    }

    protected void onProcess() {
        final Bitmap bitmap = getConnectedInputPort("image").pullFrame().asFrameImage2D().toBitmap();
        if (this.mImageView != null) {
            this.mImageView.post(new Runnable() {
                public void run() {
                    BitmapTarget.this.mImageView.setImageBitmap(bitmap);
                }
            });
        }
        if (this.mListener == null) {
            return;
        }
        if (this.mHandler != null) {
            postBitmapToUiThread(bitmap);
        } else {
            this.mListener.onReceivedBitmap(bitmap);
        }
    }

    private void postBitmapToUiThread(final Bitmap bitmap) {
        this.mHandler.post(new Runnable() {
            public void run() {
                BitmapTarget.this.mListener.onReceivedBitmap(bitmap);
            }
        });
    }
}
