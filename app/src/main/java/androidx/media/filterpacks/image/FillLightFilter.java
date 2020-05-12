package androidx.media.filterpacks.image;

import androidx.media.filterfw.Filter;
import androidx.media.filterfw.FrameImage2D;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.ImageShader;
import androidx.media.filterfw.InputPort;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.OutputPort;
import androidx.media.filterfw.Signature;

public class FillLightFilter extends Filter {
    private float mBacklight = 0.0f;
    private final String mFillLightShader = "precision mediump float;\nuniform sampler2D tex_sampler_0;\nuniform float mult;\nuniform float igamma;\nvarying vec2 v_texcoord;\nvoid main()\n{\n  const vec3 color_weights = vec3(0.25, 0.5, 0.25);\n  vec4 color = texture2D(tex_sampler_0, v_texcoord);\n  float lightmask = dot(color.rgb, color_weights);\n  float backmask = (1.0 - lightmask);\n  vec3 ones = vec3(1.0, 1.0, 1.0);\n  vec3 diff = pow(mult * color.rgb, igamma * ones) - color.rgb;\n  diff = min(diff, 1.0);\n  vec3 new_color = min(color.rgb + diff * backmask, 1.0);\n  gl_FragColor = vec4(new_color, color.a);\n}\n";
    private ImageShader mShader;

    public FillLightFilter(MffContext context, String name) {
        super(context, name);
    }

    public Signature getSignature() {
        FrameType imageIn = FrameType.image2D(FrameType.ELEMENT_RGBA8888, 2);
        return new Signature().addInputPort("image", 2, imageIn).addInputPort("strength", 1, FrameType.single(Float.TYPE)).addOutputPort("image", 2, FrameType.image2D(FrameType.ELEMENT_RGBA8888, 16)).disallowOtherPorts();
    }

    public void onInputPortOpen(InputPort port) {
        if (port.getName().equals("strength")) {
            port.bindToFieldNamed("mBacklight");
            port.setAutoPullEnabled(true);
        }
    }

    protected void onPrepare() {
        this.mShader = new ImageShader("precision mediump float;\nuniform sampler2D tex_sampler_0;\nuniform float mult;\nuniform float igamma;\nvarying vec2 v_texcoord;\nvoid main()\n{\n  const vec3 color_weights = vec3(0.25, 0.5, 0.25);\n  vec4 color = texture2D(tex_sampler_0, v_texcoord);\n  float lightmask = dot(color.rgb, color_weights);\n  float backmask = (1.0 - lightmask);\n  vec3 ones = vec3(1.0, 1.0, 1.0);\n  vec3 diff = pow(mult * color.rgb, igamma * ones) - color.rgb;\n  diff = min(diff, 1.0);\n  vec3 new_color = min(color.rgb + diff * backmask, 1.0);\n  gl_FragColor = vec4(new_color, color.a);\n}\n");
    }

    private void updateParameters() {
        float mult = 1.0f / ((0.7f * (1.0f - this.mBacklight)) + 0.3f);
        float igamma = 1.0f / (0.3f + ((1.0f - 0.3f) * mult));
        this.mShader.setUniformValue("mult", mult);
        this.mShader.setUniformValue("igamma", igamma);
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
