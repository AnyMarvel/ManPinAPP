package androidx.media.filterpacks.miscellaneous;

import android.util.Log;
import androidx.media.filterfw.Filter;
import androidx.media.filterfw.Frame;
import androidx.media.filterfw.FrameImage2D;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.FrameValue;
import androidx.media.filterfw.ImageShader;
import androidx.media.filterfw.InputPort;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.OutputPort;
import androidx.media.filterfw.Signature;
import androidx.media.filterfw.geometry.Quad;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

public final class AverageIntensity extends Filter {
    private static int mBinHeight = 2;
    private static int mBinWidth = 2;
    private static int mBins;
    private final String mFragShader = "precision mediump float;\nuniform sampler2D tex_sampler_0;\nvarying vec2 v_texcoord;\nvoid main() {\n    gl_FragColor = texture2D(tex_sampler_0, v_texcoord);\n}\n";
    private int[] mHistogram;
    private Quad mQuad = Quad.fromRect(0.0f, 0.0f, 1.0f, 1.0f);
    private ImageShader mShader;

    private native void averageIntensity(ByteBuffer byteBuffer, IntBuffer intBuffer, int i, int i2, int i3, int i4);

    static {
        System.loadLibrary("filterframework_jni");
    }

    public AverageIntensity(MffContext context, String name) {
        super(context, name);
    }

    public Signature getSignature() {
        return new Signature().addInputPort("image", 2, FrameType.image2D(FrameType.ELEMENT_RGBA8888, 1)).addInputPort("wnum", 1, FrameType.single(Integer.TYPE)).addInputPort("hnum", 1, FrameType.single(Integer.TYPE)).addInputPort("targetQuad", 1, FrameType.single(Quad.class)).addOutputPort("histogram", 2, FrameType.array(Integer.TYPE)).disallowOtherPorts();
    }

    protected void onPrepare() {
        this.mShader = new ImageShader("precision mediump float;\nuniform sampler2D tex_sampler_0;\nvarying vec2 v_texcoord;\nvoid main() {\n    gl_FragColor = texture2D(tex_sampler_0, v_texcoord);\n}\n");
    }

    public void onInputPortOpen(InputPort port) {
        if (port.getName().equals("wnum")) {
            port.bindToFieldNamed("mBinWidth");
            port.setAutoPullEnabled(true);
        }
        if (port.getName().equals("hnum")) {
            port.bindToFieldNamed("mBinHeight");
            port.setAutoPullEnabled(true);
        }
        if (port.getName().equals("targetQuad")) {
            port.bindToFieldNamed("mQuad");
            port.setAutoPullEnabled(true);
        }
    }

    protected void onProcess() {
        FrameImage2D inputImage = getConnectedInputPort("image").pullFrame().asFrameImage2D();
        int[] dim = inputImage.getDimensions();
        int i = dim[0];
        Log.e("AverageIntensity", "width " + i + " height " + dim[1]);
        dim[0] = Math.round(((float) dim[0]) * this.mQuad.xEdge().length());
        dim[1] = Math.round(((float) dim[1]) * this.mQuad.yEdge().length());
        FrameImage2D cropFrame = Frame.create(FrameType.image2D(FrameType.ELEMENT_RGBA8888, 18), dim).asFrameImage2D();
        this.mShader.setSourceQuad(this.mQuad);
        this.mShader.process(inputImage, cropFrame);
        i = dim[0];
        Log.e("AverageIntensity", "width " + i + " height " + dim[1]);
        mBins = mBinWidth * mBinHeight;
        this.mHistogram = new int[mBins];
        ByteBuffer histogramBuffer = ByteBuffer.allocateDirect(mBins * 4);
        histogramBuffer.order(ByteOrder.nativeOrder());
        IntBuffer histogramIntBuffer = histogramBuffer.asIntBuffer();
        ByteBuffer buf = cropFrame.lockBytes(1);
        averageIntensity(buf, histogramIntBuffer, dim[0], dim[1], mBinWidth, mBinHeight);
        buf.rewind();
        cropFrame.unlock();
        histogramIntBuffer.rewind();
        for (int i2 = 0; i2 < mBins; i2++) {
            this.mHistogram[i2] = histogramIntBuffer.get();
        }
        OutputPort outPort = getConnectedOutputPort("histogram");
        FrameValue frame = outPort.fetchAvailableFrame(null).asFrameValues();
        frame.setValue(this.mHistogram);
        outPort.pushFrame(frame);
        cropFrame.release();
    }
}
