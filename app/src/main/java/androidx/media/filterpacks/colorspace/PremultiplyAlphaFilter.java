package androidx.media.filterpacks.colorspace;

import androidx.media.filterfw.Filter;
import androidx.media.filterfw.FrameImage2D;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.ImageShader;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.OutputPort;
import androidx.media.filterfw.Signature;

public class PremultiplyAlphaFilter extends Filter {
    private static final String mPremultShaderCode = "precision mediump float;\nuniform sampler2D tex_sampler_0;\nvarying vec2 v_texcoord;\nvoid main() {\n  vec4 c = texture2D(tex_sampler_0, v_texcoord);\n  gl_FragColor = vec4(c.r * c.a, c.g * c.a, c.b * c.a, c.a);\n}\n";
    private ImageShader mShader;

    public PremultiplyAlphaFilter(MffContext context, String name) {
        super(context, name);
    }

    public Signature getSignature() {
        FrameType imageIn = FrameType.image2D(FrameType.ELEMENT_RGBA8888, 2);
        return new Signature().addInputPort("image", 2, imageIn).addOutputPort("image", 2, FrameType.image2D(FrameType.ELEMENT_RGBA8888, 16)).disallowOtherPorts();
    }

    protected void onPrepare() {
        this.mShader = new ImageShader(mPremultShaderCode);
    }

    protected void onProcess() {
        OutputPort outPort = getConnectedOutputPort("image");
        FrameImage2D inputImage = getConnectedInputPort("image").pullFrame().asFrameImage2D();
        FrameImage2D outputImage = outPort.fetchAvailableFrame(inputImage.getDimensions()).asFrameImage2D();
        this.mShader.process(inputImage, outputImage);
        outPort.pushFrame(outputImage);
    }
}
