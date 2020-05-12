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

public class GaussianFilter7x7 extends Filter {
    private static final String mGaussian1x7Source = "precision mediump float;\nuniform sampler2D tex_sampler_0;\nuniform float pix;\nuniform float pix2;\nuniform float pix3;\nvarying vec2 v_texcoord;\nvoid main() {\n  vec4 a1 = 0.0044 * texture2D(tex_sampler_0, v_texcoord + vec2(-pix3, 0.0));\n  vec4 a2 = 0.0540 * texture2D(tex_sampler_0, v_texcoord + vec2(-pix2, 0.0));\n  vec4 a3 = 0.2420 * texture2D(tex_sampler_0, v_texcoord + vec2(-pix, 0.0));\n  vec4 a4 = 0.3992 * texture2D(tex_sampler_0, v_texcoord + vec2(0.0, 0.0));\n  vec4 a5 = 0.2420 * texture2D(tex_sampler_0, v_texcoord + vec2(pix, 0.0));\n  vec4 a6 = 0.0540 * texture2D(tex_sampler_0, v_texcoord + vec2(pix2, 0.0));\n  vec4 a7 = 0.0044 * texture2D(tex_sampler_0, v_texcoord + vec2(pix3, 0.0));\n  gl_FragColor = a1 + a2 + a3 + a4 + a5 + a6 + a7;\n}\n";
    private static final String mGaussian7x1Source = "precision mediump float;\nuniform sampler2D tex_sampler_0;\nuniform float pix;\nuniform float pix2;\nuniform float pix3;\nvarying vec2 v_texcoord;\nvoid main() {\n  vec4 a1 = 0.0044 * texture2D(tex_sampler_0, v_texcoord + vec2(0.0, -pix3));\n  vec4 a2 = 0.0540 * texture2D(tex_sampler_0, v_texcoord + vec2(0.0, -pix2));\n  vec4 a3 = 0.2420 * texture2D(tex_sampler_0, v_texcoord + vec2(0.0, -pix));\n  vec4 a4 = 0.3992 * texture2D(tex_sampler_0, v_texcoord + vec2(0.0, 0.0));\n  vec4 a5 = 0.2420 * texture2D(tex_sampler_0, v_texcoord + vec2(0.0, pix));\n  vec4 a6 = 0.0540 * texture2D(tex_sampler_0, v_texcoord + vec2(0.0, pix2));\n  vec4 a7 = 0.0044 * texture2D(tex_sampler_0, v_texcoord + vec2(0.0, pix3));\n  gl_FragColor = a1 + a2 + a3 + a4 + a5 + a6 + a7;\n}\n";
    private ImageShader mGaussian1x7Shader;
    private ImageShader mGaussian7x1Shader;
    private FrameType mImageType;

    private static native boolean blur(int i, int i2, ByteBuffer byteBuffer, ByteBuffer byteBuffer2);

    public GaussianFilter7x7(MffContext context, String name) {
        super(context, name);
    }

    public Signature getSignature() {
        FrameType imageIn = FrameType.image2D(FrameType.ELEMENT_RGBA8888, 2);
        return new Signature().addInputPort("image", 2, imageIn).addOutputPort("image", 1, FrameType.image2D(FrameType.ELEMENT_RGBA8888, 16)).disallowOtherPorts();
    }

    protected void onPrepare() {
        if (isOpenGLSupported()) {
            this.mGaussian7x1Shader = new ImageShader(mGaussian7x1Source);
            this.mGaussian1x7Shader = new ImageShader(mGaussian1x7Source);
            this.mImageType = FrameType.image2D(FrameType.ELEMENT_RGBA8888, 18);
        }
    }

    protected void onProcess() {
        OutputPort smoothedImagePort = getConnectedOutputPort("image");
        FrameImage2D inputImage = getConnectedInputPort("image").pullFrame().asFrameImage2D();
        int[] inputDims = inputImage.getDimensions();
        FrameImage2D smoothedImage = smoothedImagePort != null ? smoothedImagePort.fetchAvailableFrame(inputDims).asFrameImage2D() : null;
        if (isOpenGLSupported()) {
            this.mGaussian7x1Shader.setUniformValue("pix", 1.0f / ((float) inputDims[1]));
            this.mGaussian7x1Shader.setUniformValue("pix2", 2.0f / ((float) inputDims[1]));
            this.mGaussian7x1Shader.setUniformValue("pix3", 3.0f / ((float) inputDims[1]));
            this.mGaussian1x7Shader.setUniformValue("pix", 1.0f / ((float) inputDims[0]));
            this.mGaussian1x7Shader.setUniformValue("pix2", 2.0f / ((float) inputDims[0]));
            this.mGaussian1x7Shader.setUniformValue("pix3", 3.0f / ((float) inputDims[0]));
            FrameImage2D gaussian7x1SmoothedImage = Frame.create(this.mImageType, inputDims).asFrameImage2D();
            if (smoothedImagePort != null) {
                this.mGaussian7x1Shader.process(inputImage, gaussian7x1SmoothedImage);
                this.mGaussian1x7Shader.process(gaussian7x1SmoothedImage, smoothedImage);
            }
            gaussian7x1SmoothedImage.release();
        } else {
            blur(inputImage.getWidth(), inputImage.getHeight(), inputImage.lockBytes(1), smoothedImage != null ? smoothedImage.lockBytes(2) : null);
            inputImage.unlock();
            if (smoothedImage != null) {
                smoothedImage.unlock();
            }
        }
        if (smoothedImage != null) {
            smoothedImagePort.pushFrame(smoothedImage);
        }
    }

    static {
        System.loadLibrary("filterframework_jni");
    }
}
