package androidx.media.filterpacks.image;

import androidx.media.filterfw.Filter;
import androidx.media.filterfw.FrameImage2D;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.ImageShader;
import androidx.media.filterfw.InputPort;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.OutputPort;
import androidx.media.filterfw.Signature;

public class FisheyeFilter extends Filter {
    private static final String mFisheyeShader = "precision mediump float;\nuniform sampler2D tex_sampler_0;\nuniform vec2 scale;\nuniform float alpha;\nuniform float radius2;\nuniform float factor;\nvarying vec2 v_texcoord;\nvoid main() {\n  const float m_pi_2 = 1.570963;\n  vec2 coord = v_texcoord - vec2(0.5, 0.5);\n  float dist = length(coord * scale);\n  float radian = m_pi_2 - atan(alpha * sqrt(radius2 - dist * dist), dist);\n  float scalar = radian * factor / dist;\n  vec2 new_coord = coord * scalar + vec2(0.5, 0.5);\n  gl_FragColor = texture2D(tex_sampler_0, new_coord);\n}\n";
    private int mHeight = 0;
    private float mScale = 0.5f;
    private ImageShader mShader;
    private int mWidth = 0;

    public FisheyeFilter(MffContext context, String name) {
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
        this.mShader = new ImageShader(mFisheyeShader);
    }

    private void updateFrameSize(int width, int height) {
        this.mWidth = width;
        this.mHeight = height;
    }

    private void updateProgramParams() {
        float[] scale = new float[2];
        if (this.mWidth > this.mHeight) {
            scale[0] = 1.0f;
            scale[1] = ((float) this.mHeight) / ((float) this.mWidth);
        } else {
            scale[0] = ((float) this.mWidth) / ((float) this.mHeight);
            scale[1] = 1.0f;
        }
        float alpha = (this.mScale * 2.0f) + 0.75f;
        float bound2 = 0.25f * ((scale[0] * scale[0]) + (scale[1] * scale[1]));
        float bound = (float) Math.sqrt((double) bound2);
        float radius = 1.15f * bound;
        float radius2 = radius * radius;
        float factor = bound / (1.5707964f - ((float) Math.atan((double) ((alpha / bound) * ((float) Math.sqrt((double) (radius2 - bound2)))))));
        this.mShader.setUniformValue("scale", scale);
        this.mShader.setUniformValue("radius2", radius2);
        this.mShader.setUniformValue("factor", factor);
        this.mShader.setUniformValue("alpha", alpha);
    }

    protected synchronized void onProcess() {
        OutputPort outPort = getConnectedOutputPort("image");
        FrameImage2D inputImage = getConnectedInputPort("image").pullFrame().asFrameImage2D();
        FrameImage2D outputImage = outPort.fetchAvailableFrame(inputImage.getDimensions()).asFrameImage2D();
        if (!(inputImage.getWidth() == this.mWidth && inputImage.getHeight() == this.mHeight)) {
            updateFrameSize(inputImage.getWidth(), inputImage.getHeight());
        }
        updateProgramParams();
        this.mShader.process(inputImage, outputImage);
        outPort.pushFrame(outputImage);
    }
}
