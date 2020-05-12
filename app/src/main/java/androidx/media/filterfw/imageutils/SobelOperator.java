package androidx.media.filterfw.imageutils;

import androidx.media.filterfw.Frame;
import androidx.media.filterfw.FrameImage2D;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.ImageShader;
import java.nio.ByteBuffer;

public class SobelOperator {
    private static final String mDirectionSource = "precision mediump float;\nuniform sampler2D tex_sampler_0;\nuniform sampler2D tex_sampler_1;\nvarying vec2 v_texcoord;\nvoid main() {\n  vec4 gy = 2.0 * texture2D(tex_sampler_1, v_texcoord) - 1.0;\n  vec4 gx = 2.0 * texture2D(tex_sampler_0, v_texcoord) - 1.0;\n  gl_FragColor = vec4((atan(gy.rgb, gx.rgb) + 3.14) / (2.0 * 3.14), 1.0);\n}\n";
    private static final String mGradientXSource = "precision mediump float;\nuniform sampler2D tex_sampler_0;\nuniform vec2 pix;\nvarying vec2 v_texcoord;\nvoid main() {\n  vec4 a1 = -1.0 * texture2D(tex_sampler_0, v_texcoord + vec2(-pix.x, -pix.y));\n  vec4 a2 = -2.0 * texture2D(tex_sampler_0, v_texcoord + vec2(-pix.x, 0.0));\n  vec4 a3 = -1.0 * texture2D(tex_sampler_0, v_texcoord + vec2(-pix.x, +pix.y));\n  vec4 b1 = +1.0 * texture2D(tex_sampler_0, v_texcoord + vec2(+pix.x, -pix.y));\n  vec4 b2 = +2.0 * texture2D(tex_sampler_0, v_texcoord + vec2(+pix.x, 0.0));\n  vec4 b3 = +1.0 * texture2D(tex_sampler_0, v_texcoord + vec2(+pix.x, +pix.y));\n  gl_FragColor = 0.5 + (a1 + a2 + a3 + b1 + b2 + b3) / 8.0;\n}\n";
    private static final String mGradientYSource = "precision mediump float;\nuniform sampler2D tex_sampler_0;\nuniform vec2 pix;\nvarying vec2 v_texcoord;\nvoid main() {\n  vec4 a1 = -1.0 * texture2D(tex_sampler_0, v_texcoord + vec2(-pix.x, -pix.y));\n  vec4 a2 = -2.0 * texture2D(tex_sampler_0, v_texcoord + vec2(0.0,    -pix.y));\n  vec4 a3 = -1.0 * texture2D(tex_sampler_0, v_texcoord + vec2(+pix.x, -pix.y));\n  vec4 b1 = +1.0 * texture2D(tex_sampler_0, v_texcoord + vec2(-pix.x, +pix.y));\n  vec4 b2 = +2.0 * texture2D(tex_sampler_0, v_texcoord + vec2(0.0,    +pix.y));\n  vec4 b3 = +1.0 * texture2D(tex_sampler_0, v_texcoord + vec2(+pix.x, +pix.y));\n  gl_FragColor = 0.5 + (a1 + a2 + a3 + b1 + b2 + b3) / 8.0;\n}\n";
    private static final String mMagnitudeSource = "precision mediump float;\nuniform sampler2D tex_sampler_0;\nuniform sampler2D tex_sampler_1;\nvarying vec2 v_texcoord;\nvoid main() {\n  vec4 gx = 2.0 * texture2D(tex_sampler_0, v_texcoord) - 1.0;\n  vec4 gy = 2.0 * texture2D(tex_sampler_1, v_texcoord) - 1.0;\n  gl_FragColor = vec4(sqrt(gx.rgb * gx.rgb + gy.rgb * gy.rgb), 1.0);\n}\n";
    private ImageShader mDirectionShader;
    private ImageShader mGradientXShader;
    private ImageShader mGradientYShader;
    private FrameType mImageType;
    private final boolean mIsOpenGLSupported;
    private ImageShader mMagnitudeShader;

    private static native boolean sobelOperator(int i, int i2, ByteBuffer byteBuffer, ByteBuffer byteBuffer2, ByteBuffer byteBuffer3, ByteBuffer byteBuffer4, ByteBuffer byteBuffer5);

    public SobelOperator(boolean isOpenGLSupported) {
        this.mIsOpenGLSupported = isOpenGLSupported;
        if (this.mIsOpenGLSupported) {
            this.mGradientXShader = new ImageShader(mGradientXSource);
            this.mGradientYShader = new ImageShader(mGradientYSource);
            this.mMagnitudeShader = new ImageShader(mMagnitudeSource);
            this.mDirectionShader = new ImageShader(mDirectionSource);
            this.mImageType = FrameType.image2D(FrameType.ELEMENT_RGBA8888, 18);
        }
    }

    public void calculate(FrameImage2D inputImage, FrameImage2D outputGradientXImage, FrameImage2D outputGradientYImage, FrameImage2D outputMagImage, FrameImage2D outputDirImage) {
        int[] inputDims = inputImage.getDimensions();
        if (this.mIsOpenGLSupported) {
            FrameImage2D gxFrame = outputGradientXImage;
            FrameImage2D gyFrame = outputGradientYImage;
            if (outputGradientXImage == null) {
                gxFrame = Frame.create(this.mImageType, inputDims).asFrameImage2D();
            }
            if (outputGradientYImage == null) {
                gyFrame = Frame.create(this.mImageType, inputDims).asFrameImage2D();
            }
            this.mGradientXShader.setUniformValue("pix", new float[]{1.0f / ((float) inputDims[0]), 1.0f / ((float) inputDims[1])});
            this.mGradientYShader.setUniformValue("pix", new float[]{1.0f / ((float) inputDims[0]), 1.0f / ((float) inputDims[1])});
            this.mGradientXShader.process(inputImage, gxFrame);
            this.mGradientYShader.process(inputImage, gyFrame);
            FrameImage2D[] gradientFrames = new FrameImage2D[]{gxFrame, gyFrame};
            if (outputMagImage != null) {
                this.mMagnitudeShader.processMulti(gradientFrames, outputMagImage);
            }
            if (outputDirImage != null) {
                this.mDirectionShader.processMulti(gradientFrames, outputDirImage);
            }
            if (outputGradientXImage == null) {
                gxFrame.release();
            }
            if (outputGradientYImage == null) {
                gyFrame.release();
                return;
            }
            return;
        }
        sobelOperator(inputImage.getWidth(), inputImage.getHeight(), inputImage.lockBytes(1), outputMagImage != null ? outputMagImage.lockBytes(2) : null, outputDirImage != null ? outputDirImage.lockBytes(2) : null, outputGradientXImage != null ? outputGradientXImage.lockBytes(2) : null, outputGradientYImage != null ? outputGradientYImage.lockBytes(2) : null);
        inputImage.unlock();
        if (outputMagImage != null) {
            outputMagImage.unlock();
        }
        if (outputDirImage != null) {
            outputDirImage.unlock();
        }
        if (outputGradientXImage != null) {
            outputGradientXImage.unlock();
        }
        if (outputGradientYImage != null) {
            outputGradientYImage.unlock();
        }
    }

    static {
        System.loadLibrary("filterframework_jni");
    }
}
