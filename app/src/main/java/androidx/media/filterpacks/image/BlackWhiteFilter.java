package androidx.media.filterpacks.image;

import androidx.media.filterfw.Filter;
import androidx.media.filterfw.FrameImage2D;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.ImageShader;
import androidx.media.filterfw.InputPort;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.OutputPort;
import androidx.media.filterfw.Signature;
import java.util.Random;

public class BlackWhiteFilter extends Filter {
    private float mBlack = 0.0f;
    private final String mBlackWhiteShader = "precision mediump float;\nuniform sampler2D tex_sampler_0;\nuniform vec2 seed;\nuniform float black;\nuniform float scale;\nuniform float stepsize;\nvarying vec2 v_texcoord;\nfloat rand(vec2 loc) {\n  return fract(sin(dot((loc + seed), vec2(12.9898, 78.233))) * 43758.5453);\n}\nvoid main() {\n  vec4 color = texture2D(tex_sampler_0, v_texcoord);\n  float dither = rand(v_texcoord);\n  vec3 xform = clamp((color.rgb - black) * scale, 0.0, 1.0);\n  vec3 temp = clamp((color.rgb + stepsize - black) * scale, 0.0, 1.0);\n  vec3 new_color = clamp(xform + (temp - xform) * (dither - 0.5), 0.0, 1.0);\n  gl_FragColor = vec4(new_color, color.a);\n}\n";
    private Random mRandom = new Random();
    private ImageShader mShader;
    private float mWhite = 1.0f;

    public BlackWhiteFilter(MffContext context, String name) {
        super(context, name);
    }

    public Signature getSignature() {
        FrameType imageIn = FrameType.image2D(FrameType.ELEMENT_RGBA8888, 2);
        return new Signature().addInputPort("image", 2, imageIn).addInputPort("black", 1, FrameType.single(Float.TYPE)).addInputPort("white", 1, FrameType.single(Float.TYPE)).addOutputPort("image", 2, FrameType.image2D(FrameType.ELEMENT_RGBA8888, 16)).disallowOtherPorts();
    }

    public void onInputPortOpen(InputPort port) {
        if (port.getName().equals("black")) {
            port.bindToFieldNamed("mBlack");
            port.setAutoPullEnabled(true);
        } else if (port.getName().equals("white")) {
            port.bindToFieldNamed("mWhite");
            port.setAutoPullEnabled(true);
        }
    }

    private void updateParameters() {
        float scale = this.mBlack != this.mWhite ? 1.0f / (this.mWhite - this.mBlack) : 2000.0f;
        float[] seed = new float[]{this.mRandom.nextFloat(), this.mRandom.nextFloat()};
        this.mShader.setUniformValue("black", this.mBlack);
        this.mShader.setUniformValue("scale", scale);
        this.mShader.setUniformValue("stepsize", 0.003921569f);
        this.mShader.setUniformValue("seed", seed);
    }

    protected void onPrepare() {
        this.mShader = new ImageShader("precision mediump float;\nuniform sampler2D tex_sampler_0;\nuniform vec2 seed;\nuniform float black;\nuniform float scale;\nuniform float stepsize;\nvarying vec2 v_texcoord;\nfloat rand(vec2 loc) {\n  return fract(sin(dot((loc + seed), vec2(12.9898, 78.233))) * 43758.5453);\n}\nvoid main() {\n  vec4 color = texture2D(tex_sampler_0, v_texcoord);\n  float dither = rand(v_texcoord);\n  vec3 xform = clamp((color.rgb - black) * scale, 0.0, 1.0);\n  vec3 temp = clamp((color.rgb + stepsize - black) * scale, 0.0, 1.0);\n  vec3 new_color = clamp(xform + (temp - xform) * (dither - 0.5), 0.0, 1.0);\n  gl_FragColor = vec4(new_color, color.a);\n}\n");
    }

    protected void onProcess() {
        OutputPort outPort = getConnectedOutputPort("image");
        FrameImage2D inputImage = getConnectedInputPort("image").pullFrame().asFrameImage2D();
        FrameImage2D outputImage = outPort.fetchAvailableFrame(inputImage.getDimensions()).asFrameImage2D();
        updateParameters();
        this.mShader.process(inputImage, outputImage);
        outPort.pushFrame(outputImage);
    }
}
