package androidx.media.filterpacks.image;

import androidx.media.filterfw.Filter;
import androidx.media.filterfw.FrameImage2D;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.ImageShader;
import androidx.media.filterfw.InputPort;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.OutputPort;
import androidx.media.filterfw.Signature;

public class SaturateFilter extends Filter {
    private static final String mBenSaturateShaderCode = "precision mediump float;\nuniform sampler2D tex_sampler_0;\nuniform float scale;\nuniform float shift;\nuniform vec3 weights;\nvarying vec2 v_texcoord;\nvoid main() {\n  vec4 color = texture2D(tex_sampler_0, v_texcoord);\n  float kv = dot(color.rgb, weights) + shift;\n  vec3 new_color = scale * color.rgb + (1.0 - scale) * kv;\n  gl_FragColor = vec4(new_color, color.a);\n}\n";
    private static final String mHerfSaturateShaderCode = "precision mediump float;\nuniform sampler2D tex_sampler_0;\nuniform vec3 weights;\nuniform vec3 exponents;\nvarying vec2 v_texcoord;\nvoid main() {\n  vec4 color = texture2D(tex_sampler_0, v_texcoord);\n  float de = dot(color.rgb, weights);\n  float inv_de = 1.0 / de;\n  vec3 new_color = de * pow(color.rgb * inv_de, exponents);\n  float max_color = max(max(max(new_color.r, new_color.g), new_color.b), 1.0);\n  gl_FragColor = vec4(new_color / max_color, color.a);\n}\n";
    private ImageShader mBenShader;
    private ImageShader mHerfShader;
    private float mScale = 1.0f;

    public SaturateFilter(MffContext context, String name) {
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
        this.mBenShader = new ImageShader(mBenSaturateShaderCode);
        this.mHerfShader = new ImageShader(mHerfSaturateShaderCode);
        float[] weights = new float[]{0.25f, 0.625f, 0.125f};
        this.mBenShader.setUniformValue("weights", weights);
        this.mBenShader.setUniformValue("shift", 0.003921569f);
        this.mHerfShader.setUniformValue("weights", weights);
    }

    protected void onProcess() {
        OutputPort outPort = getConnectedOutputPort("image");
        FrameImage2D inputImage = getConnectedInputPort("image").pullFrame().asFrameImage2D();
        FrameImage2D outputImage = outPort.fetchAvailableFrame(inputImage.getDimensions()).asFrameImage2D();
        if (this.mScale > 0.0f) {
            this.mHerfShader.setUniformValue("exponents", new float[]{(0.9f * this.mScale) + 1.0f, (2.1f * this.mScale) + 1.0f, (2.7f * this.mScale) + 1.0f});
            this.mHerfShader.process(inputImage, outputImage);
        } else {
            this.mBenShader.setUniformValue("scale", this.mScale + 1.0f);
            this.mBenShader.process(inputImage, outputImage);
        }
        outPort.pushFrame(outputImage);
    }
}
