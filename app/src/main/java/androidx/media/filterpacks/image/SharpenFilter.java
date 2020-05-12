package androidx.media.filterpacks.image;

import android.util.Log;
import androidx.media.filterfw.Filter;
import androidx.media.filterfw.FrameImage2D;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.ImageShader;
import androidx.media.filterfw.InputPort;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.OutputPort;
import androidx.media.filterfw.Signature;

public class SharpenFilter extends Filter {
    private static final String mSharpenShaderCode = "precision mediump float;\nuniform sampler2D tex_sampler_0;\nuniform float scale;\nuniform float stepsizeX;\nuniform float stepsizeY;\nvarying vec2 v_texcoord;\nvoid main() {\n  vec3 nbr_color = vec3(0.0, 0.0, 0.0);\n  vec2 coord;\n  vec4 color = texture2D(tex_sampler_0, v_texcoord);\n  coord.x = v_texcoord.x - 0.5 * stepsizeX;\n  coord.y = v_texcoord.y - stepsizeY;\n  nbr_color += texture2D(tex_sampler_0, coord).rgb - color.rgb;\n  coord.x = v_texcoord.x - stepsizeX;\n  coord.y = v_texcoord.y + 0.5 * stepsizeY;\n  nbr_color += texture2D(tex_sampler_0, coord).rgb - color.rgb;\n  coord.x = v_texcoord.x + stepsizeX;\n  coord.y = v_texcoord.y - 0.5 * stepsizeY;\n  nbr_color += texture2D(tex_sampler_0, coord).rgb - color.rgb;\n  coord.x = v_texcoord.x + stepsizeX;\n  coord.y = v_texcoord.y + 0.5 * stepsizeY;\n  nbr_color += texture2D(tex_sampler_0, coord).rgb - color.rgb;\n  gl_FragColor = vec4(color.rgb - 2.0 * scale * nbr_color, color.a);\n}\n";
    private float mScale = 0.0f;
    private ImageShader mShader;

    public SharpenFilter(MffContext context, String name) {
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
        Log.i("SharpenFilter", "onPrepare BEGIN");
        this.mShader = new ImageShader(mSharpenShaderCode);
        Log.i("SharpenFilter", "onPrepare END");
    }

    protected synchronized void onProcess() {
        Log.i("SharpenFilter", "onProcess BEGIN");
        OutputPort outPort = getConnectedOutputPort("image");
        FrameImage2D inputImage = getConnectedInputPort("image").pullFrame().asFrameImage2D();
        FrameImage2D outputImage = outPort.fetchAvailableFrame(inputImage.getDimensions()).asFrameImage2D();
        int width = inputImage.getWidth();
        int height = inputImage.getHeight();
        this.mShader.setUniformValue("stepsizeX", 1.0f / ((float) width));
        this.mShader.setUniformValue("stepsizeY", 1.0f / ((float) height));
        this.mShader.setUniformValue("scale", this.mScale);
        this.mShader.process(inputImage, outputImage);
        outPort.pushFrame(outputImage);
        Log.i("SharpenFilter", "onProcess END");
    }
}
