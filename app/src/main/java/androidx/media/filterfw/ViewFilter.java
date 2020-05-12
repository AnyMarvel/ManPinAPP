package androidx.media.filterfw;

import android.graphics.Rect;
import android.graphics.RectF;
import android.view.View;
import androidx.media.filterfw.InputPort.FrameListener;

public abstract class ViewFilter extends Filter {
    public static final int SCALE_FILL = 3;
    public static final int SCALE_FIT = 2;
    public static final int SCALE_STRETCH = 1;
    protected float[] mClearColor = new float[]{0.0f, 0.0f, 0.0f, 1.0f};
    protected boolean mFlipVertically = true;
    private String mRequestedScaleMode = null;
    protected int mScaleMode = 2;
    private FrameListener mScaleModeListener = new FrameListener() {
        public void onFrameReceived(InputPort port, Frame frame) {
            String scaleMode = (String) frame.asFrameValue().getValue();
            if (!scaleMode.equals(ViewFilter.this.mRequestedScaleMode)) {
                ViewFilter.this.mRequestedScaleMode = scaleMode;
                if (scaleMode.equals("stretch")) {
                    ViewFilter.this.mScaleMode = 1;
                } else if (scaleMode.equals("fit")) {
                    ViewFilter.this.mScaleMode = 2;
                } else if (scaleMode.equals("fill")) {
                    ViewFilter.this.mScaleMode = 3;
                } else {
                    throw new RuntimeException(new StringBuilder(String.valueOf(scaleMode).length() + 22).append("Unknown scale-mode '").append(scaleMode).append("'!").toString());
                }
            }
        }
    };

    protected abstract void onBindToView(View view);

    protected ViewFilter(MffContext context, String name) {
        super(context, name);
    }

    public void bindToView(View view) {
        if (isRunning()) {
            throw new IllegalStateException("Attempting to bind filter to view while it is running!");
        }
        onBindToView(view);
    }

    public void setScaleMode(int scaleMode) {
        if (isRunning()) {
            throw new IllegalStateException("Attempting to change scale mode while filter is running!");
        }
        this.mScaleMode = scaleMode;
    }

    public Signature getSignature() {
        return new Signature().addInputPort("scaleMode", 1, FrameType.single(String.class)).addInputPort("flip", 1, FrameType.single(Boolean.TYPE));
    }

    protected RectF getTargetRect(Rect frameRect, Rect bufferRect) {
        RectF result = new RectF();
        if (bufferRect.width() > 0 && bufferRect.height() > 0) {
            float relativeAR = (((float) bufferRect.width()) / ((float) bufferRect.height())) / (((float) frameRect.width()) / ((float) frameRect.height()));
            float y;
            float x;
            switch (this.mScaleMode) {
                case 1:
                    result.set(0.0f, 0.0f, 1.0f, 1.0f);
                    break;
                case 2:
                    if (relativeAR <= 1.0f) {
                        y = 0.5f - (0.5f * relativeAR);
                        result.set(0.0f, y, 0.0f + 1.0f, y + relativeAR);
                        break;
                    }
                    x = 0.5f - (0.5f / relativeAR);
                    result.set(x, 0.0f, (1.0f / relativeAR) + x, 0.0f + 1.0f);
                    break;
                case 3:
                    if (relativeAR <= 1.0f) {
                        x = 0.5f - (0.5f / relativeAR);
                        result.set(x, 0.0f, (1.0f / relativeAR) + x, 0.0f + 1.0f);
                        break;
                    }
                    y = 0.5f - (0.5f * relativeAR);
                    result.set(0.0f, y, 0.0f + 1.0f, y + relativeAR);
                    break;
            }
        }
        return result;
    }

    protected void connectViewInputs(InputPort port) {
        if (port.getName().equals("scaleMode")) {
            port.bindToListener(this.mScaleModeListener);
            port.setAutoPullEnabled(true);
        } else if (port.getName().equals("flip")) {
            port.bindToFieldNamed("mFlipVertically");
            port.setAutoPullEnabled(true);
        }
    }

    protected void setupShader(ImageShader shader, Rect frameRect, Rect outputRect) {
        shader.setTargetRect(getTargetRect(frameRect, outputRect));
        shader.setClearsOutput(true);
        shader.setClearColor(this.mClearColor);
        if (this.mFlipVertically) {
            shader.setSourceRect(0.0f, 1.0f, 1.0f, -1.0f);
        }
    }
}
