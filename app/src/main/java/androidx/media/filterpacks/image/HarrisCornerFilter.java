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

public class HarrisCornerFilter extends Filter {
    private static final float CORNER_STRENGTH_THRESHOLD = 6.0E-4f;
    private static final String mCornerStrengthSource = "precision mediump float;\nuniform sampler2D tex_sampler_0;\nvarying vec2 v_texcoord;\nvoid main() {\n  float Ix2 = texture2D(tex_sampler_0, v_texcoord).r * 2.0 - 1.0;\n  float Iy2 = texture2D(tex_sampler_0, v_texcoord).g * 2.0 - 1.0;\n  float Ixy = texture2D(tex_sampler_0, v_texcoord).b * 2.0 - 1.0;\n  float response = (Ix2 * Iy2 - Ixy * Ixy) / (Ix2 + Iy2 + 0.0000001) ;\n  gl_FragColor = vec4(response, response, response, 1.0);\n}\n";
    private static final String mNonMaxSource = "precision mediump float;\nuniform sampler2D tex_sampler_0;\nuniform float threshold;uniform vec2 pix;\nvarying vec2 v_texcoord;\nvoid main() {\n  vec4 c = texture2D(tex_sampler_0, v_texcoord);\n  float a1 = float(texture2D(tex_sampler_0, \nv_texcoord + vec2(-pix.x, -pix.y)).r < c.r);\n  float a2 = float(texture2D(tex_sampler_0, \nv_texcoord + vec2(0.0,    -pix.y)).r < c.r);\n  float a3 = float(texture2D(tex_sampler_0, \nv_texcoord + vec2(+pix.x, -pix.y)).r < c.r);\n  float a4 = float(texture2D(tex_sampler_0, \nv_texcoord + vec2(-pix.x, 0.0)).r < c.r);\n  float a5 = float(texture2D(tex_sampler_0, \nv_texcoord + vec2(+pix.x, 0.0)).r < c.r);\n  float a6 = float(texture2D(tex_sampler_0, \nv_texcoord + vec2(-pix.x, +pix.y)).r < c.r);\n  float a7 = float(texture2D(tex_sampler_0, \nv_texcoord + vec2(0.0,    +pix.y)).r < c.r);\n  float a8 = float(texture2D(tex_sampler_0, \nv_texcoord + vec2(+pix.x, +pix.y)).r < c.r);\n  float localmax = float(threshold < c.r) * a1 * a2 * a3 * a4 * a5 * a6 * a7 * a8;\n  gl_FragColor = vec4(localmax, localmax, localmax, 1.0);\n}\n";
    private ImageShader mCornerStrengthShader;
    private FrameType mIntermediateImageType;
    private ImageShader mNonMaxShader;

    private static native boolean markCorners(int i, int i2, ByteBuffer byteBuffer, ByteBuffer byteBuffer2);

    public HarrisCornerFilter(MffContext context, String name) {
        super(context, name);
    }

    public Signature getSignature() {
        FrameType strctureTensor = FrameType.image2D(FrameType.ELEMENT_RGBA8888, 2);
        return new Signature().addInputPort("structureTensor", 2, strctureTensor).addOutputPort("cornerMap", 2, FrameType.image2D(FrameType.ELEMENT_RGBA8888, 16)).disallowOtherPorts();
    }

    protected void onPrepare() {
        if (isOpenGLSupported()) {
            this.mCornerStrengthShader = new ImageShader(mCornerStrengthSource);
            this.mNonMaxShader = new ImageShader(mNonMaxSource);
            this.mIntermediateImageType = FrameType.image2D(FrameType.ELEMENT_RGBA8888, 18);
        }
    }

    protected void onProcess() {
        OutputPort cornerMapPort = getConnectedOutputPort("cornerMap");
        FrameImage2D structureTensor = getConnectedInputPort("structureTensor").pullFrame().asFrameImage2D();
        int[] inputDims = structureTensor.getDimensions();
        FrameImage2D cornerMap = cornerMapPort.fetchAvailableFrame(inputDims).asFrameImage2D();
        if (isOpenGLSupported()) {
            FrameImage2D cornerStrengthImage = Frame.create(this.mIntermediateImageType, inputDims).asFrameImage2D();
            this.mCornerStrengthShader.process(structureTensor, cornerStrengthImage);
            this.mNonMaxShader.setUniformValue("pix", new float[]{1.0f / ((float) inputDims[0]), 1.0f / ((float) inputDims[1])});
            this.mNonMaxShader.setUniformValue("threshold", (float) CORNER_STRENGTH_THRESHOLD);
            this.mNonMaxShader.process(cornerStrengthImage, cornerMap);
            cornerStrengthImage.release();
        } else {
            markCorners(structureTensor.getWidth(), structureTensor.getHeight(), structureTensor.lockBytes(1), cornerMap.lockBytes(2));
            structureTensor.unlock();
            cornerMap.unlock();
        }
        cornerMapPort.pushFrame(cornerMap);
    }

    static {
        System.loadLibrary("filterframework_jni");
    }
}
