package androidx.media.filterpacks.image;


import androidx.media.filterfw.Filter;
import androidx.media.filterfw.Frame;
import androidx.media.filterfw.FrameImage2D;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.ImageShader;
import androidx.media.filterfw.InputPort;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.OutputPort;
import androidx.media.filterfw.Signature;

import java.util.Random;

public class GrainFilter extends Filter {
    private ImageShader mGrainShader;
    private final String mGrainSource = "precision mediump float;\nuniform sampler2D tex_sampler_0;\nuniform sampler2D tex_sampler_1;\nuniform float scale;\nuniform float stepX;\nuniform float stepY;\nvarying vec2 v_texcoord;\nvoid main() {\n  float noise = texture2D(tex_sampler_1, v_texcoord + vec2(-stepX, -stepY)).r * 0.224;\n  noise += texture2D(tex_sampler_1, v_texcoord + vec2(-stepX, stepY)).r * 0.224;\n  noise += texture2D(tex_sampler_1, v_texcoord + vec2(stepX, -stepY)).r * 0.224;\n  noise += texture2D(tex_sampler_1, v_texcoord + vec2(stepX, stepY)).r * 0.224;\n  noise += 0.4448;\n  noise *= scale;\n  vec4 color = texture2D(tex_sampler_0, v_texcoord);\n  float energy = 0.33333 * color.r + 0.33333 * color.g + 0.33333 * color.b;\n  float mask = (1.0 - sqrt(energy));\n  float weight = 1.0 - 1.333 * mask * noise;\n  gl_FragColor = vec4(color.rgb * weight, color.a);\n}\n";
    private int mHeight = 0;
    private FrameImage2D mNoiseFrame = null;
    private ImageShader mNoiseShader;
    private final String mNoiseSource = "precision mediump float;\nuniform vec2 seed;\nvarying vec2 v_texcoord;\nfloat rand(vec2 loc) {\n  const float divide = 0.0003630780547;\n  const float factor = 2754.228703;\n  float value = sin(dot(loc, vec2(12.9898, 78.233)));\n  float residual = mod(dot(mod(loc, divide), vec2(0.9898, 0.233)), divide);\n  float part2 = mod(value, divide);\n  float part1 = value - part2;\n  return fract(0.5453 * part1 + factor * (part2 + residual));\n}\nvoid main() {\n  gl_FragColor = vec4(rand(v_texcoord + seed), 0.0, 0.0, 1.0);\n}\n";
    private Random mRandom = new Random();
    private float mScale = 1.0f;
    private int mWidth = 0;

    public GrainFilter(MffContext context, String name) {
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

    public void onTearDown() {
        if (this.mNoiseFrame != null) {
            this.mNoiseFrame.release();
            this.mNoiseFrame = null;
        }
    }

    private void updateParameters() {
        this.mGrainShader.setUniformValue("scale", this.mScale);
        float[] seed = new float[]{this.mRandom.nextFloat(), this.mRandom.nextFloat()};
    }

    private void updateFrameSize(int width, int height) {
        this.mWidth = width;
        this.mHeight = height;
        this.mGrainShader.setUniformValue("stepX", 0.5f / ((float) this.mWidth));
        this.mGrainShader.setUniformValue("stepY", 0.5f / ((float) this.mHeight));
    }

    protected void onPrepare() {
        this.mNoiseShader = new ImageShader("precision mediump float;\nuniform vec2 seed;\nvarying vec2 v_texcoord;\nfloat rand(vec2 loc) {\n  const float divide = 0.0003630780547;\n  const float factor = 2754.228703;\n  float value = sin(dot(loc, vec2(12.9898, 78.233)));\n  float residual = mod(dot(mod(loc, divide), vec2(0.9898, 0.233)), divide);\n  float part2 = mod(value, divide);\n  float part1 = value - part2;\n  return fract(0.5453 * part1 + factor * (part2 + residual));\n}\nvoid main() {\n  gl_FragColor = vec4(rand(v_texcoord + seed), 0.0, 0.0, 1.0);\n}\n");
        this.mGrainShader = new ImageShader("precision mediump float;\nuniform sampler2D tex_sampler_0;\nuniform sampler2D tex_sampler_1;\nuniform float scale;\nuniform float stepX;\nuniform float stepY;\nvarying vec2 v_texcoord;\nvoid main() {\n  float noise = texture2D(tex_sampler_1, v_texcoord + vec2(-stepX, -stepY)).r * 0.224;\n  noise += texture2D(tex_sampler_1, v_texcoord + vec2(-stepX, stepY)).r * 0.224;\n  noise += texture2D(tex_sampler_1, v_texcoord + vec2(stepX, -stepY)).r * 0.224;\n  noise += texture2D(tex_sampler_1, v_texcoord + vec2(stepX, stepY)).r * 0.224;\n  noise += 0.4448;\n  noise *= scale;\n  vec4 color = texture2D(tex_sampler_0, v_texcoord);\n  float energy = 0.33333 * color.r + 0.33333 * color.g + 0.33333 * color.b;\n  float mask = (1.0 - sqrt(energy));\n  float weight = 1.0 - 1.333 * mask * noise;\n  gl_FragColor = vec4(color.rgb * weight, color.a);\n}\n");
    }

    protected synchronized void onProcess() {
        OutputPort outPort = getConnectedOutputPort("image");
        int[] dim = getConnectedInputPort("image").pullFrame().asFrameImage2D().getDimensions();
        FrameImage2D outputImage = outPort.fetchAvailableFrame(dim).asFrameImage2D();
        if (!(dim[0] == this.mWidth && dim[1] == this.mHeight && this.mNoiseFrame != null)) {
            updateFrameSize(dim[0], dim[1]);
            createNoiseFrame(dim[0] / 2, dim[1] / 2);
        }
        updateParameters();
        this.mNoiseShader.processMulti(new FrameImage2D[0], this.mNoiseFrame);
//        this.mGrainShader.processMulti(new FrameImage2D[]{inputImage, this.mNoiseFrame}, outputImage);
        outPort.pushFrame(outputImage);
    }

    private void createNoiseFrame(int width, int height) {
        if (this.mNoiseFrame != null) {
            this.mNoiseFrame.release();
        }
        this.mNoiseFrame = Frame.create(FrameType.image2D(FrameType.ELEMENT_RGBA8888, 18), new int[]{width, height}).asFrameImage2D();
    }
}
