package androidx.media.filterfw.imageutils;

import androidx.media.filterfw.Frame;
import androidx.media.filterfw.FrameImage2D;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.ImageShader;
import java.nio.ByteBuffer;

public class FastBoxBlur {
    private static final String mHorizontalBoxFilterSource = "precision mediump float;\nuniform sampler2D tex_sampler_0;\nuniform float pix;\nuniform float pixel_weight;\nvarying vec2 v_texcoord;\nvoid main() {\n  vec4 sum_color = vec4(0.0); \n  for (int i = -$$filter_radius_width$$; i <= $$filter_radius_width$$; i++) {\n    sum_color += texture2D(tex_sampler_0, \n        v_texcoord + vec2(pix * float(i), 0));\n  }\n  gl_FragColor = sum_color * pixel_weight;\n}\n";
    private static final String mVerticalBoxFilterSource = "precision mediump float;\nuniform sampler2D tex_sampler_0;\nuniform float pix;\nuniform float pixel_weight;\nvarying vec2 v_texcoord;\nvoid main() {\n  vec4 sum_color = vec4(0.0);\n  for (int j = -$$filter_radius_height$$; j <= $$filter_radius_height$$; j++) {\n    sum_color += texture2D(tex_sampler_0, \n        v_texcoord + vec2(0, pix * float(j)));\n  }\n  gl_FragColor = sum_color * pixel_weight;\n}\n";
    private final int mFilterHeight;
    private final int mFilterWidth;
    private ImageShader mHorizontalBoxFilterShader;
    private ImageShader mIdentityShader;
    private FrameType mImageType;
    private final boolean mIsOpenGLSupported;
    private ImageShader mVerticalBoxFilterShader;

    private static native boolean blur(int i, int i2, int i3, int i4, ByteBuffer byteBuffer, ByteBuffer byteBuffer2);

    public FastBoxBlur(boolean isOpenGLSupported, int filterWidth, int filterHeight) {
        this.mIsOpenGLSupported = isOpenGLSupported;
        this.mFilterWidth = filterWidth;
        this.mFilterHeight = filterHeight;
        if (this.mIsOpenGLSupported) {
            if (this.mFilterWidth > 1) {
                this.mHorizontalBoxFilterShader = new ImageShader(mHorizontalBoxFilterSource.replaceAll("\\$\\$filter_radius_width\\$\\$", String.valueOf(this.mFilterWidth >> 1)));
            }
            if (this.mFilterHeight > 1) {
                this.mVerticalBoxFilterShader = new ImageShader(mVerticalBoxFilterSource.replaceAll("\\$\\$filter_radius_height\\$\\$", String.valueOf(this.mFilterHeight >> 1)));
            }
            this.mImageType = FrameType.image2D(FrameType.ELEMENT_RGBA8888, 18);
            this.mIdentityShader = ImageShader.createIdentity();
        }
    }

    public void computeBlur(FrameImage2D inputImage, FrameImage2D outputImage) {
        int[] inputDims = inputImage.getDimensions();
        if (this.mIsOpenGLSupported) {
            if (this.mVerticalBoxFilterShader != null) {
                this.mVerticalBoxFilterShader.setUniformValue("pix", 1.0f / ((float) inputDims[1]));
                this.mVerticalBoxFilterShader.setUniformValue("pixel_weight", 1.0f / ((float) this.mFilterHeight));
            }
            if (this.mHorizontalBoxFilterShader != null) {
                this.mHorizontalBoxFilterShader.setUniformValue("pix", 1.0f / ((float) inputDims[0]));
                this.mHorizontalBoxFilterShader.setUniformValue("pixel_weight", 1.0f / ((float) this.mFilterWidth));
            }
            if (this.mFilterHeight == 1 && this.mFilterWidth == 1) {
                this.mIdentityShader.process(inputImage, outputImage);
                return;
            } else if (this.mFilterHeight > 1 && this.mFilterWidth == 1) {
                this.mVerticalBoxFilterShader.process(inputImage, outputImage);
                return;
            } else if (this.mFilterHeight != 1 || this.mFilterWidth <= 1) {
                FrameImage2D verticallyBlurred = Frame.create(this.mImageType, inputDims).asFrameImage2D();
                this.mVerticalBoxFilterShader.process(inputImage, verticallyBlurred);
                this.mHorizontalBoxFilterShader.process(verticallyBlurred, outputImage);
                verticallyBlurred.release();
                return;
            } else {
                this.mHorizontalBoxFilterShader.process(inputImage, outputImage);
                return;
            }
        }
        computeBlur(inputImage.getWidth(), inputImage.getHeight(), this.mFilterWidth, this.mFilterHeight, inputImage.lockBytes(1), outputImage.lockBytes(2));
        inputImage.unlock();
        outputImage.unlock();
    }

    public static void computeBlur(int width, int height, int filterWidth, int filterHeight, ByteBuffer imageBuffer, ByteBuffer outputBuffer) {
        blur(width, height, filterWidth, filterHeight, imageBuffer, outputBuffer);
    }

    static {
        System.loadLibrary("filterframework_jni");
    }
}
