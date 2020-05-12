package androidx.media.filterpacks.image;

import android.graphics.PointF;
import android.util.Log;
import androidx.media.filterfw.Filter;
import androidx.media.filterfw.FrameImage2D;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.ImageShader;
import androidx.media.filterfw.InputPort;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.OutputPort;
import androidx.media.filterfw.Signature;

public class StraightenFilter extends Filter {
    private static final float DEGREE_TO_RADIAN = 0.017453292f;
    private float mAngle = 0.0f;
    private int mHeight = 0;
    private float mMaxAngle = 45.0f;
    private ImageShader mShader;
    private int mWidth = 0;

    public StraightenFilter(MffContext context, String name) {
        super(context, name);
    }

    public Signature getSignature() {
        FrameType imageIn = FrameType.image2D(FrameType.ELEMENT_RGBA8888, 2);
        return new Signature().addInputPort("image", 2, imageIn).addInputPort("angle", 1, FrameType.single(Float.TYPE)).addInputPort("maxAngle", 1, FrameType.single(Float.TYPE)).addOutputPort("image", 2, FrameType.image2D(FrameType.ELEMENT_RGBA8888, 16)).disallowOtherPorts();
    }

    public void onInputPortOpen(InputPort port) {
        if (port.getName().equals("angle")) {
            port.bindToFieldNamed("mAngle");
            port.setAutoPullEnabled(true);
        } else if (port.getName().equals("maxAngle")) {
            port.bindToFieldNamed("mMaxAngle");
            port.setAutoPullEnabled(true);
        }
    }

    protected void onPrepare() {
        Log.i("StraightenFilter", "onPrepare BEGIN");
        this.mShader = ImageShader.createIdentity();
        Log.i("StraightenFilter", "onPrepare END");
    }

    protected synchronized void onProcess() {
        Log.i("StraightenFilter", "onProcess BEGIN");
        OutputPort outPort = getConnectedOutputPort("image");
        FrameImage2D inputImage = getConnectedInputPort("image").pullFrame().asFrameImage2D();
        FrameImage2D outputImage = outPort.fetchAvailableFrame(inputImage.getDimensions()).asFrameImage2D();
        if (!(inputImage.getWidth() == this.mWidth && inputImage.getHeight() == this.mHeight)) {
            this.mWidth = inputImage.getWidth();
            this.mHeight = inputImage.getHeight();
        }
        updateParameters();
        Log.i("StraightenFilter", "onProcess SHADER");
        this.mShader.process(inputImage, outputImage);
        outPort.pushFrame(outputImage);
        Log.i("StraightenFilter", "onProcess END");
    }

    private void updateParameters() {
        float f = 90.0f;
        float cosTheta = (float) Math.cos((double) (this.mAngle * DEGREE_TO_RADIAN));
        float sinTheta = (float) Math.sin((double) (this.mAngle * DEGREE_TO_RADIAN));
        if (this.mMaxAngle <= 0.0f) {
            throw new RuntimeException("Max angle is out of range (0-180).");
        }
        if (this.mMaxAngle <= 90.0f) {
            f = this.mMaxAngle;
        }
        this.mMaxAngle = f;
        PointF p0 = new PointF(((-cosTheta) * ((float) this.mWidth)) + (((float) this.mHeight) * sinTheta), ((-sinTheta) * ((float) this.mWidth)) - (((float) this.mHeight) * cosTheta));
        PointF p1 = new PointF((((float) this.mWidth) * cosTheta) + (((float) this.mHeight) * sinTheta), (((float) this.mWidth) * sinTheta) - (((float) this.mHeight) * cosTheta));
        PointF p2 = new PointF(((-cosTheta) * ((float) this.mWidth)) - (((float) this.mHeight) * sinTheta), ((-sinTheta) * ((float) this.mWidth)) + (((float) this.mHeight) * cosTheta));
        PointF p3 = new PointF((((float) this.mWidth) * cosTheta) - (((float) this.mHeight) * sinTheta), (((float) this.mWidth) * sinTheta) + (((float) this.mHeight) * cosTheta));
        float scale = 0.5f * Math.min(((float) this.mWidth) / Math.max(Math.abs(p0.x), Math.abs(p1.x)), ((float) this.mHeight) / Math.max(Math.abs(p0.y), Math.abs(p1.y)));
        p0.set(((p0.x * scale) / ((float) this.mWidth)) + 0.5f, ((p0.y * scale) / ((float) this.mHeight)) + 0.5f);
        p1.set(((p1.x * scale) / ((float) this.mWidth)) + 0.5f, ((p1.y * scale) / ((float) this.mHeight)) + 0.5f);
        p2.set(((p2.x * scale) / ((float) this.mWidth)) + 0.5f, ((p2.y * scale) / ((float) this.mHeight)) + 0.5f);
        p3.set(((p3.x * scale) / ((float) this.mWidth)) + 0.5f, ((p3.y * scale) / ((float) this.mHeight)) + 0.5f);
        this.mShader.setSourceCoords(new float[]{p0.x, p0.y, p1.x, p1.y, p2.x, p2.y, p3.x, p3.y});
    }
}
