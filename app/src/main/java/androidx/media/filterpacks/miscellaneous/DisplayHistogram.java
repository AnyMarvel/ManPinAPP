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

public final class DisplayHistogram extends Filter {
    private final String mFragmentShader = "precision mediump float;\nvoid main() {\n  gl_FragColor = vec4(1.0, 0.0, 0.0, 0.5);\n}\n";
    private ImageShader mGraphShader;
    private int[] mHistogram;
    private ImageShader mIdShader;
    private final String mVertexShader = "attribute vec4 a_position2;\nvoid main() {\n  gl_Position = a_position2;\n}\n";
    private float[] mVertices;
    private float mYScale = 1.0f;

    public DisplayHistogram(MffContext context, String name) {
        super(context, name);
    }

    public Signature getSignature() {
        FrameType imageIn = FrameType.image2D(FrameType.ELEMENT_RGBA8888, 2);
        return new Signature().addInputPort("image", 2, imageIn).addInputPort("histogram", 2, FrameType.array(Integer.TYPE)).addInputPort("YScale", 1, FrameType.single(Float.TYPE)).addOutputPort("composite", 2, FrameType.image2D(FrameType.ELEMENT_RGBA8888, 16)).disallowOtherPorts();
    }

    public void onInputPortOpen(InputPort port) {
        if (port.getName().equals("YScale")) {
            port.bindToFieldNamed("mYScale");
            port.setAutoPullEnabled(true);
        } else if (port.getName().equals("histogram")) {
            port.bindToFieldNamed("mHistogram");
            port.setAutoPullEnabled(true);
        }
    }

    protected void onPrepare() {
        this.mIdShader = ImageShader.createIdentity();
        this.mGraphShader = new ImageShader("attribute vec4 a_position2;\nvoid main() {\n  gl_Position = a_position2;\n}\n", "precision mediump float;\nvoid main() {\n  gl_FragColor = vec4(1.0, 0.0, 0.0, 0.5);\n}\n");
    }

    protected void onProcess() {
        OutputPort outPort = getConnectedOutputPort("composite");
        FrameImage2D inputImage = getConnectedInputPort("image").pullFrame().asFrameImage2D();
        int[] imgDim = inputImage.getDimensions();
        FrameImage2D outputImage = outPort.fetchAvailableFrame(imgDim).asFrameImage2D();
        this.mIdShader.process(inputImage, outputImage);
        int numPixels = imgDim[0] * imgDim[1];
        int nBins = this.mHistogram.length;
        float stepX = 2.0f / (((float) nBins) + 2.0f);
        this.mVertices = new float[(nBins * 4)];
        float x = -1.0f + stepX;
        for (int i = 0; i < nBins; i++) {
            this.mVertices[(i * 4) + 0] = x;
            this.mVertices[(i * 4) + 1] = 1.0f;
            this.mVertices[(i * 4) + 2] = x;
            this.mVertices[(i * 4) + 3] = 1.0f - (((2.0f * ((float) this.mHistogram[i])) * this.mYScale) / ((float) numPixels));
            x += stepX;
        }
        this.mGraphShader.setAttributeValues("a_position2", this.mVertices, 2);
        GLES20.glLineWidth((0.6f * stepX) * ((float) imgDim[0]));
        this.mGraphShader.setDrawMode(1);
        this.mGraphShader.setVertexCount(nBins * 2);
        this.mGraphShader.processNoInput(outputImage);
        outPort.pushFrame(outputImage);
    }
}
