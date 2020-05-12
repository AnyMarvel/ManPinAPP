package androidx.media.filterpacks.image;

import androidx.media.filterfw.Filter;
import androidx.media.filterfw.FrameImage2D;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.ImageShader;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.OutputPort;
import androidx.media.filterfw.Signature;
import java.nio.ByteBuffer;

public class StructureTensor2DFilter extends Filter {
    private static final String mStructuredTensorSource = "precision mediump float;\nuniform sampler2D tex_sampler_0;\nuniform sampler2D tex_sampler_1;\nvarying vec2 v_texcoord;\nvoid main() {\n  float dx = texture2D(tex_sampler_0, v_texcoord).r * 2.0 - 1.0;\n  float dy = texture2D(tex_sampler_1, v_texcoord).r * 2.0 - 1.0;\n  float dx2 = (dx * dx) * 0.5 + 0.5;\n  float dy2 = (dy * dy) * 0.5 + 0.5;\n  float dxy = (dx * dy) * 0.5 + 0.5;\n  gl_FragColor = vec4(dx2, dy2, dxy, 1.0);\n}\n";
    private ImageShader mStructuredTensorShader;

    private static native boolean constructStructureTensor(int i, int i2, ByteBuffer byteBuffer, ByteBuffer byteBuffer2, ByteBuffer byteBuffer3);

    public StructureTensor2DFilter(MffContext context, String name) {
        super(context, name);
    }

    public Signature getSignature() {
        FrameType gradientX = FrameType.image2D(FrameType.ELEMENT_RGBA8888, 2);
        FrameType gradientY = FrameType.image2D(FrameType.ELEMENT_RGBA8888, 2);
        return new Signature().addInputPort("dx", 2, gradientX).addInputPort("dy", 2, gradientY).addOutputPort("image", 2, FrameType.image2D(FrameType.ELEMENT_RGBA8888, 16)).disallowOtherPorts();
    }

    protected void onPrepare() {
        if (isOpenGLSupported()) {
            this.mStructuredTensorShader = new ImageShader(mStructuredTensorSource);
        }
    }

    protected void onProcess() {
        OutputPort structureTensorPort = getConnectedOutputPort("image");
        FrameImage2D inputDx = getConnectedInputPort("dx").pullFrame().asFrameImage2D();
        FrameImage2D inputDy = getConnectedInputPort("dy").pullFrame().asFrameImage2D();
        FrameImage2D structureTensor = structureTensorPort.fetchAvailableFrame(inputDx.getDimensions()).asFrameImage2D();
        if (isOpenGLSupported()) {
            this.mStructuredTensorShader.processMulti(new FrameImage2D[]{inputDx, inputDy}, structureTensor);
        } else {
            constructStructureTensor(inputDx.getWidth(), inputDx.getHeight(), inputDx.lockBytes(1), inputDy.lockBytes(1), structureTensor.lockBytes(2));
            inputDx.unlock();
            inputDy.unlock();
            structureTensor.unlock();
        }
        structureTensorPort.pushFrame(structureTensor);
    }

    static {
        System.loadLibrary("filterframework_jni");
    }
}
