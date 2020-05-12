package androidx.media.filterpacks.image;

import android.opengl.GLES20;
import androidx.media.filterfw.Filter;
import androidx.media.filterfw.Frame;
import androidx.media.filterfw.FrameImage2D;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.InputPort;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.OutputPort;
import androidx.media.filterfw.Signature;
import java.util.Arrays;

public class FixedColorSource extends Filter {
    private float[] mColor = new float[]{0.0f, 0.0f, 0.0f, 1.0f};
    private int mHeight = 1;
    private FrameImage2D mImageFrame = null;
    private FrameType mImageType = null;
    private float[] mLastColor = null;
    private int mLastHeight = 0;
    private int mLastWidth = 0;
    private int mWidth = 1;

    public FixedColorSource(MffContext context, String name) {
        super(context, name);
    }

    public Signature getSignature() {
        this.mImageType = FrameType.image2D(FrameType.ELEMENT_RGBA8888, 16);
        return new Signature().addInputPort("color", 1, FrameType.array(Float.TYPE)).addInputPort("width", 2, FrameType.single(Integer.TYPE)).addInputPort("height", 2, FrameType.single(Integer.TYPE)).addOutputPort("image", 2, this.mImageType).disallowOtherPorts();
    }

    public void onInputPortOpen(InputPort port) {
        if (port.getName().equals("color")) {
            port.bindToFieldNamed("mColor");
            port.setAutoPullEnabled(true);
        } else if (port.getName().equals("width")) {
            port.bindToFieldNamed("mWidth");
            port.setAutoPullEnabled(true);
        } else if (port.getName().equals("height")) {
            port.bindToFieldNamed("mHeight");
            port.setAutoPullEnabled(true);
        }
    }

    protected void onProcess() {
        OutputPort outPort = getConnectedOutputPort("image");
        if (!(Arrays.equals(this.mLastColor, this.mColor) && this.mWidth == this.mLastWidth && this.mHeight == this.mLastHeight)) {
            int[] dims = new int[]{this.mWidth, this.mHeight};
            if (this.mImageFrame != null) {
                this.mImageFrame.release();
            }
            this.mImageFrame = Frame.create(this.mImageType, dims).asFrameImage2D();
            this.mImageFrame.lockRenderTarget().focus();
            GLES20.glClearColor(this.mColor[0], this.mColor[1], this.mColor[2], this.mColor[3]);
            GLES20.glClear(16384);
            this.mImageFrame.unlock();
            this.mLastColor = this.mColor;
            this.mLastWidth = this.mWidth;
            this.mLastHeight = this.mHeight;
        }
        outPort.pushFrame(this.mImageFrame);
    }

    protected void onTearDown() {
        if (this.mImageFrame != null) {
            this.mImageFrame.release();
        }
    }
}
