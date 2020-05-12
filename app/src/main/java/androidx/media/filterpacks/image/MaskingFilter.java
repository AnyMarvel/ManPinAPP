package androidx.media.filterpacks.image;

import androidx.media.filterfw.Filter;
import androidx.media.filterfw.FrameImage2D;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.ImageShader;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.OutputPort;
import androidx.media.filterfw.Signature;
import java.nio.ByteBuffer;

public class MaskingFilter extends Filter {
    private static final String mMaskingSource = "precision mediump float;\nuniform sampler2D tex_sampler_0;\nuniform sampler2D tex_sampler_1;\nvarying vec2 v_texcoord;\nvoid main() {\n  gl_FragColor = texture2D(tex_sampler_0, v_texcoord) *\ntexture2D(tex_sampler_1, v_texcoord);\n}\n";
    private FrameType mImageType;
    private ImageShader mMaskingShader;

    private static native boolean applyMask(int i, int i2, ByteBuffer byteBuffer, ByteBuffer byteBuffer2, ByteBuffer byteBuffer3);

    public MaskingFilter(MffContext context, String name) {
        super(context, name);
    }

    public Signature getSignature() {
        FrameType imageIn = FrameType.image2D(FrameType.ELEMENT_RGBA8888, 2);
        FrameType imageMask = FrameType.image2D(FrameType.ELEMENT_RGBA8888, 2);
        return new Signature().addInputPort("image", 2, imageIn).addInputPort("mask", 2, imageMask).addOutputPort("image", 2, FrameType.image2D(FrameType.ELEMENT_RGBA8888, 16)).disallowOtherPorts();
    }

    protected void onPrepare() {
        if (isOpenGLSupported()) {
            this.mMaskingShader = new ImageShader(mMaskingSource);
            this.mImageType = FrameType.image2D(FrameType.ELEMENT_RGBA8888, 18);
        }
    }

    protected void onProcess() {
        OutputPort maskedImagePort = getConnectedOutputPort("image");
        FrameImage2D inputImage = getConnectedInputPort("image").pullFrame().asFrameImage2D();
        FrameImage2D maskImage = getConnectedInputPort("mask").pullFrame().asFrameImage2D();
        FrameImage2D maskedImage = maskedImagePort.fetchAvailableFrame(inputImage.getDimensions()).asFrameImage2D();
        if (isOpenGLSupported()) {
            maskImage.lockTextureSource().setParameter(10240, 9728);
            maskImage.unlock();
            FrameImage2D[] maskFrames = new FrameImage2D[]{inputImage, maskImage};
            if (maskedImage != null) {
                this.mMaskingShader.processMulti(maskFrames, maskedImage);
            }
            maskImage.lockTextureSource().setDefaultParams();
            maskImage.unlock();
        } else {
            applyMask(inputImage.getWidth(), inputImage.getHeight(), inputImage.lockBytes(1), maskImage.lockBytes(1), maskedImage.lockBytes(2));
            inputImage.unlock();
            maskImage.unlock();
            maskedImage.unlock();
        }
        if (maskedImage != null) {
            maskedImagePort.pushFrame(maskedImage);
        }
    }

    static {
        System.loadLibrary("filterframework_jni");
    }
}
