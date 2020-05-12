package androidx.media.filterfw.imageutils;

import androidx.media.filterfw.Frame;
import androidx.media.filterfw.FrameBuffer2D;
import androidx.media.filterfw.FrameImage2D;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.ImageShader;
import androidx.media.filterfw.geometry.Quad;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class GrayValuesExtractor {
    private static final String mGrayPackFragment = "precision mediump float;\nconst vec4 coeff_y = vec4(0.299, 0.587, 0.114, 0);\nuniform sampler2D tex_sampler_0;\nuniform float pix_stride;\nvarying vec2 v_texcoord;\nvoid main() {\n  for (int i = 0; i < 4; i++) {\n  vec4 p = texture2D(tex_sampler_0,\n                       v_texcoord + vec2(pix_stride * (float(i) - 1.5), 0.0));\n    gl_FragColor[i] = dot(p, coeff_y);\n  }\n}\n";
    private final boolean mIsOpenGLSupported;
    private ImageShader mShader;

    private static native boolean toGrayValues(ByteBuffer byteBuffer, ByteBuffer byteBuffer2);

    public GrayValuesExtractor(boolean isOpenGLSupported) {
        this.mIsOpenGLSupported = isOpenGLSupported;
        if (this.mIsOpenGLSupported) {
            this.mShader = new ImageShader(mGrayPackFragment);
        }
    }

    public int[] getOutputFrameDimensions(int[] dimensions) {
        if (!this.mIsOpenGLSupported) {
            return dimensions;
        }
        int modular = dimensions[0] % 4;
        return new int[]{dimensions[0] - modular, dimensions[1]};
    }

    public void toGrayValues(FrameImage2D inputImage, FrameBuffer2D outputGrayValuesBuffer) {
        int[] outDim = getOutputFrameDimensions(inputImage.getDimensions());
        if (!Arrays.equals(outputGrayValuesBuffer.getDimensions(), outDim)) {
            throw new IllegalArgumentException("Invalid dimensions of the output frame for gray values.");
        } else if (outputGrayValuesBuffer.getType().getElementId() != 100) {
            throw new IllegalArgumentException("Invalid type of output buffer.");
        } else {
            ByteBuffer grayBuffer = outputGrayValuesBuffer.lockBytes(2);
            if (this.mIsOpenGLSupported) {
                int[] targetDims = new int[]{outDim[0] / 4, outDim[1]};
                FrameImage2D targetFrame = Frame.create(FrameType.image2D(FrameType.ELEMENT_RGBA8888, 2), targetDims).asFrameImage2D();
                this.mShader.setSourceQuad(Quad.fromRect(0.0f, 0.0f, ((float) outDim[0]) / ((float) inputImage.getWidth()), 1.0f));
                this.mShader.setUniformValue("pix_stride", 1.0f / ((float) outDim[0]));
                inputImage.lockTextureSource().setParameter(10240, 9728);
                inputImage.unlock();
                this.mShader.process(inputImage, targetFrame);
                inputImage.lockTextureSource().setDefaultParams();
                inputImage.unlock();
                targetFrame.lockRenderTarget().readPixelData(grayBuffer, targetDims[0], targetDims[1]);
                targetFrame.unlock();
                targetFrame.release();
            } else if (toGrayValues(inputImage.lockBytes(1), grayBuffer)) {
                inputImage.unlock();
            } else {
                throw new RuntimeException("Native implementation encountered an error during processing!");
            }
            outputGrayValuesBuffer.unlock();
        }
    }

    static {
        System.loadLibrary("filterframework_jni");
    }
}
