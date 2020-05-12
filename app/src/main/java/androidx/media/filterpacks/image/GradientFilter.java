package androidx.media.filterpacks.image;

import androidx.media.filterfw.Filter;
import androidx.media.filterfw.Frame;
import androidx.media.filterfw.FrameImage2D;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.ImageShader;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.OutputPort;
import androidx.media.filterfw.Signature;
import java.nio.ByteBuffer;

public class GradientFilter extends Filter {
    private static final String mDirectionSource = "precision mediump float;\nuniform sampler2D tex_sampler_0;\nuniform sampler2D tex_sampler_1;\nvarying vec2 v_texcoord;\nvoid main() {\n  vec4 gy = 2.0 * texture2D(tex_sampler_1, v_texcoord) - 1.0;\n  vec4 gx = 2.0 * texture2D(tex_sampler_0, v_texcoord) - 1.0;\n  gl_FragColor = vec4((atan(gy.rgb, gx.rgb) + 3.14) / (2.0 * 3.14), 1.0);\n}\n";
    private static final String mGradientXSource = "precision mediump float;\nuniform sampler2D tex_sampler_0;\nuniform vec2 pix;\nvarying vec2 v_texcoord;\nvoid main() {\n  vec4 cr = texture2D(tex_sampler_0, v_texcoord);\n  vec4 right = texture2D(tex_sampler_0, v_texcoord + vec2(pix.x, 0));\n  gl_FragColor = 0.5 + (right - cr) / 2.0;\n}\n";
    private static final String mGradientYSource = "precision mediump float;\nuniform sampler2D tex_sampler_0;\nuniform vec2 pix;\nvarying vec2 v_texcoord;\nvoid main() {\n  vec4 cr = texture2D(tex_sampler_0, v_texcoord);\n  vec4 down = texture2D(tex_sampler_0, v_texcoord + vec2(0, pix.y));\n  gl_FragColor = 0.5 + (down - cr) / 2.0;\n}\n";
    private static final String mMagnitudeSource = "precision mediump float;\nuniform sampler2D tex_sampler_0;\nuniform sampler2D tex_sampler_1;\nvarying vec2 v_texcoord;\nvoid main() {\n  vec4 gx = 2.0 * texture2D(tex_sampler_0, v_texcoord) - 1.0;\n  vec4 gy = 2.0 * texture2D(tex_sampler_1, v_texcoord) - 1.0;\n  gl_FragColor = vec4(sqrt(gx.rgb * gx.rgb + gy.rgb * gy.rgb), 1.0);\n}\n";
    private ImageShader mDirectionShader;
    private ImageShader mGradientXShader;
    private ImageShader mGradientYShader;
    private FrameType mImageType;
    private ImageShader mMagnitudeShader;

    private static native boolean gradientOperator(int i, int i2, ByteBuffer byteBuffer, ByteBuffer byteBuffer2, ByteBuffer byteBuffer3, ByteBuffer byteBuffer4, ByteBuffer byteBuffer5);

    public GradientFilter(MffContext context, String name) {
        super(context, name);
    }

    public Signature getSignature() {
        FrameType imageIn = FrameType.image2D(FrameType.ELEMENT_RGBA8888, 2);
        FrameType imageOut = FrameType.image2D(FrameType.ELEMENT_RGBA8888, 16);
        return new Signature().addInputPort("image", 2, imageIn).addOutputPort("gradientX", 1, imageOut).addOutputPort("gradientY", 1, imageOut).addOutputPort("direction", 1, imageOut).addOutputPort("magnitude", 1, imageOut).disallowOtherPorts();
    }

    protected void onPrepare() {
        if (isOpenGLSupported()) {
            this.mGradientXShader = new ImageShader(mGradientXSource);
            this.mGradientYShader = new ImageShader(mGradientYSource);
            this.mMagnitudeShader = new ImageShader(mMagnitudeSource);
            this.mDirectionShader = new ImageShader(mDirectionSource);
            this.mImageType = FrameType.image2D(FrameType.ELEMENT_RGBA8888, 18);
        }
    }

    protected void onOpen() {
        super.onOpen();
        if (getConnectedOutputPorts().length == 0) {
            throw new IllegalStateException("Gradient Filter has no output port!");
        }
    }

    protected void onProcess() {
        OutputPort gradientXPort = getConnectedOutputPort("gradientX");
        OutputPort gradientYPort = getConnectedOutputPort("gradientY");
        OutputPort magnitudePort = getConnectedOutputPort("magnitude");
        OutputPort directionPort = getConnectedOutputPort("direction");
        FrameImage2D inputImage = getConnectedInputPort("image").pullFrame().asFrameImage2D();
        int[] inputDims = inputImage.getDimensions();
        FrameImage2D magImage = magnitudePort != null ? magnitudePort.fetchAvailableFrame(inputDims).asFrameImage2D() : null;
        FrameImage2D dirImage = directionPort != null ? directionPort.fetchAvailableFrame(inputDims).asFrameImage2D() : null;
        FrameImage2D gxFrame = gradientXPort != null ? gradientXPort.fetchAvailableFrame(inputDims).asFrameImage2D() : null;
        FrameImage2D gyFrame = gradientYPort != null ? gradientYPort.fetchAvailableFrame(inputDims).asFrameImage2D() : null;
        if (isOpenGLSupported()) {
            this.mGradientXShader.setUniformValue("pix", new float[]{1.0f / ((float) inputDims[0]), 1.0f / ((float) inputDims[1])});
            this.mGradientYShader.setUniformValue("pix", new float[]{1.0f / ((float) inputDims[0]), 1.0f / ((float) inputDims[1])});
            if (gradientXPort == null) {
                gxFrame = Frame.create(this.mImageType, inputDims).asFrameImage2D();
            }
            if (gradientYPort == null) {
                gyFrame = Frame.create(this.mImageType, inputDims).asFrameImage2D();
            }
            this.mGradientXShader.process(inputImage, gxFrame);
            this.mGradientYShader.process(inputImage, gyFrame);
            FrameImage2D[] gradientFrames = new FrameImage2D[]{gxFrame, gyFrame};
            if (magnitudePort != null) {
                this.mMagnitudeShader.processMulti(gradientFrames, magImage);
            }
            if (directionPort != null) {
                this.mDirectionShader.processMulti(gradientFrames, dirImage);
            }
            if (gradientXPort == null) {
                gxFrame.release();
            }
            if (gradientYPort == null) {
                gyFrame.release();
            }
        } else {
            gradientOperator(inputImage.getWidth(), inputImage.getHeight(), inputImage.lockBytes(1), magImage != null ? magImage.lockBytes(2) : null, dirImage != null ? dirImage.lockBytes(2) : null, gxFrame != null ? gxFrame.lockBytes(2) : null, gyFrame != null ? gyFrame.lockBytes(2) : null);
            inputImage.unlock();
            if (magImage != null) {
                magImage.unlock();
            }
            if (dirImage != null) {
                dirImage.unlock();
            }
            if (gxFrame != null) {
                gxFrame.unlock();
            }
            if (gyFrame != null) {
                gyFrame.unlock();
            }
        }
        if (magImage != null) {
            magnitudePort.pushFrame(magImage);
        }
        if (dirImage != null) {
            directionPort.pushFrame(dirImage);
        }
        if (gradientXPort != null) {
            gradientXPort.pushFrame(gxFrame);
        }
        if (gradientYPort != null) {
            gradientYPort.pushFrame(gyFrame);
        }
    }

    static {
        System.loadLibrary("filterframework_jni");
    }
}
