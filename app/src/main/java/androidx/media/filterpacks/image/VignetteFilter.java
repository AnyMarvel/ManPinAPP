package androidx.media.filterpacks.image;

import androidx.media.filterfw.Filter;
import androidx.media.filterfw.FrameImage2D;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.ImageShader;
import androidx.media.filterfw.InputPort;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.OutputPort;
import androidx.media.filterfw.Signature;

public class VignetteFilter extends Filter {
    private static final String mVignetteShaderCode = "precision mediump float;\nuniform sampler2D tex_sampler_0;\nuniform float range;\nuniform float inv_max_dist;\nuniform float shade;\nuniform vec2 scale;\nvarying vec2 v_texcoord;\nvoid main() {\n  const float slope = 20.0;\n  vec2 coord = v_texcoord - vec2(0.5, 0.5);\n  float dist = length(coord * scale);\n  float lumen = shade / (1.0 + exp((dist * inv_max_dist - range) * slope)) + (1.0 - shade);\n  vec4 color = texture2D(tex_sampler_0, v_texcoord);\n  gl_FragColor = vec4(color.rgb * lumen, color.a);\n}\n";
    private int mHeight = 0;
    private float mScale = 1.0f;
    private final float mShade = 0.85f;
    private ImageShader mShader;
    private int mWidth = 0;

    public VignetteFilter(MffContext context, String name) {
        super(context, name);
    }

    public Signature getSignature() {
        FrameType imageIn = FrameType.image2D(FrameType.ELEMENT_RGBA8888, 2);
        return new Signature().addInputPort("image", 2, imageIn).addInputPort("scale", 1, FrameType.single(Float.TYPE)).addOutputPort("image", 2, FrameType.image2D(FrameType.ELEMENT_RGBA8888, 16)).disallowOtherPorts();
    }

    public void onInputPortOpen(InputPort port) {
        if (port.getName().equals("scale")) {
            port.bindToFieldNamed("mScale");
            port.setAutoPullEnabled(true);
        }
    }

    protected void onPrepare() {
        this.mShader = new ImageShader(mVignetteShaderCode);
    }

    private void initParameters() {
        if (this.mShader != null) {
            float[] scale = new float[2];
            if (this.mWidth > this.mHeight) {
                scale[0] = 1.0f;
                scale[1] = ((float) this.mHeight) / ((float) this.mWidth);
            } else {
                scale[0] = ((float) this.mWidth) / ((float) this.mHeight);
                scale[1] = 1.0f;
            }
            float max_dist = ((float) Math.sqrt((double) ((scale[0] * scale[0]) + (scale[1] * scale[1])))) * 0.5f;
            this.mShader.setUniformValue("scale", scale);
            this.mShader.setUniformValue("inv_max_dist", 1.0f / max_dist);
            this.mShader.setUniformValue("shade", 0.85f);
        }
    }

    protected synchronized void onProcess() {
        OutputPort outPort = getConnectedOutputPort("image");
        FrameImage2D inputImage = getConnectedInputPort("image").pullFrame().asFrameImage2D();
        FrameImage2D outputImage = outPort.fetchAvailableFrame(inputImage.getDimensions()).asFrameImage2D();
        if (!(inputImage.getWidth() == this.mWidth && inputImage.getHeight() == this.mHeight)) {
            this.mWidth = inputImage.getWidth();
            this.mHeight = inputImage.getHeight();
            initParameters();
        }
        this.mShader.setUniformValue("range", 1.3f - (((float) Math.sqrt((double) this.mScale)) * 0.7f));
        this.mShader.process(inputImage, outputImage);
        outPort.pushFrame(outputImage);
    }
}
