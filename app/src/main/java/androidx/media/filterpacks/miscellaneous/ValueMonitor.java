package androidx.media.filterpacks.miscellaneous;

import android.opengl.GLES20;
import androidx.media.filterfw.Filter;
import androidx.media.filterfw.FrameImage2D;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.ImageShader;
import androidx.media.filterfw.InputPort;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.OutputPort;
import androidx.media.filterfw.Signature;

public final class ValueMonitor extends Filter {
    private static final int NUM_SAMPLES = 100;
    private final String mFragmentShader = "precision mediump float;\nvoid main() {\n  gl_FragColor = vec4(1.0, 1.0, 0.0, 0.1);\n}\n";
    private ImageShader mGraphShader;
    private ImageShader mIdShader;
    private float mMaxVal = 1.0f;
    private float mMinVal = 0.0f;
    private float mValue = 0.0f;
    private float[] mValues;
    private final String mVertexShader = "attribute vec4 a_position2;\nvoid main() {\n  gl_Position = a_position2;\n}\n";
    private float mYBottom = 1.0f;
    private float mYTop = 0.0f;

    public ValueMonitor(MffContext context, String name) {
        super(context, name);
    }

    public Signature getSignature() {
        FrameType imageIn = FrameType.image2D(FrameType.ELEMENT_RGBA8888, 2);
        return new Signature().addInputPort("source", 2, imageIn).addInputPort("value", 2, FrameType.single(Float.TYPE)).addInputPort("maxValue", 1, FrameType.single(Float.TYPE)).addInputPort("minValue", 1, FrameType.single(Float.TYPE)).addInputPort("YTop", 1, FrameType.single(Float.TYPE)).addInputPort("YBottom", 1, FrameType.single(Float.TYPE)).addOutputPort("composite", 2, FrameType.image2D(FrameType.ELEMENT_RGBA8888, 16)).disallowOtherPorts();
    }

    public void onInputPortOpen(InputPort port) {
        if (port.getName().equals("value")) {
            port.bindToFieldNamed("mValue");
            port.setAutoPullEnabled(true);
        } else if (port.getName().equals("maxValue")) {
            port.bindToFieldNamed("mMaxVal");
            port.setAutoPullEnabled(true);
        } else if (port.getName().equals("minValue")) {
            port.bindToFieldNamed("mMinVal");
            port.setAutoPullEnabled(true);
        } else if (port.getName().equals("YTop")) {
            port.bindToFieldNamed("mYTop");
            port.setAutoPullEnabled(true);
        } else if (port.getName().equals("YBottom")) {
            port.bindToFieldNamed("mYBottom");
            port.setAutoPullEnabled(true);
        }
    }

    protected void onPrepare() {
        this.mIdShader = ImageShader.createIdentity();
        this.mGraphShader = new ImageShader("attribute vec4 a_position2;\nvoid main() {\n  gl_Position = a_position2;\n}\n", "precision mediump float;\nvoid main() {\n  gl_FragColor = vec4(1.0, 1.0, 0.0, 0.1);\n}\n");
        this.mValues = new float[200];
        for (int i = 0; i < 100; i++) {
            this.mValues[i * 2] = ((2.0f * ((float) i)) / 100.0f) - 1.0f;
            this.mValues[(i * 2) + 1] = this.mYBottom;
        }
    }

    protected void onProcess() {
        for (int i = 0; i < 99; i++) {
            this.mValues[(i * 2) + 1] = this.mValues[(i * 2) + 3];
        }
        this.mValues[199] = mapValue(this.mValue);
        this.mGraphShader.setAttributeValues("a_position2", this.mValues, 2);
        OutputPort outPort = getConnectedOutputPort("composite");
        FrameImage2D inputImage = getConnectedInputPort("source").pullFrame().asFrameImage2D();
        FrameImage2D outputImage = outPort.fetchAvailableFrame(inputImage.getDimensions()).asFrameImage2D();
        this.mIdShader.process(inputImage, outputImage);
        GLES20.glLineWidth(3.0f);
        this.mGraphShader.setDrawMode(3);
        this.mGraphShader.setVertexCount(100);
        this.mGraphShader.processNoInput(outputImage);
        outPort.pushFrame(outputImage);
    }

    protected float mapValue(float v) {
        return ((this.mYTop - this.mYBottom) * (this.mMaxVal > this.mMinVal ? (v - this.mMinVal) / (this.mMaxVal - this.mMinVal) : 0.0f)) + this.mYBottom;
    }
}
