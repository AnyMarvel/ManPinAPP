package androidx.media.filterpacks.image;

import androidx.media.filterfw.Filter;
import androidx.media.filterfw.FrameImage2D;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.ImageShader;
import androidx.media.filterfw.InputPort;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.OutputPort;
import androidx.media.filterfw.Signature;

public class PosterizeFilter extends Filter {
    private static final String mPosterizeShaderCode = "precision mediump float;\nuniform sampler2D tex_sampler_0;\nuniform float binSize;\nvarying vec2 v_texcoord;\nvoid main() {\n  vec4 color = texture2D(tex_sampler_0, v_texcoord);\n  vec4 bc = mod(color, binSize);\n  float bs2 = binSize / 2.0;\n  vec3 result;\n  result.r = (bc.r >= bs2) ? color.r + binSize - bc.r : color.r - bc.r;\n  result.g = (bc.g >= bs2) ? color.g + binSize - bc.g : color.g - bc.g;\n  result.b = (bc.b >= bs2) ? color.b + binSize - bc.b : color.b - bc.b;\n  gl_FragColor = vec4(result, color.a);\n}\n";
    private int mLevels = 2;
    private ImageShader mShader;

    public PosterizeFilter(MffContext context, String name) {
        super(context, name);
    }

    public Signature getSignature() {
        FrameType imageIn = FrameType.image2D(FrameType.ELEMENT_RGBA8888, 2);
        return new Signature().addInputPort("image", 2, imageIn).addInputPort("levels", 1, FrameType.single(Integer.TYPE)).addOutputPort("image", 2, FrameType.image2D(FrameType.ELEMENT_RGBA8888, 16)).disallowOtherPorts();
    }

    public void onInputPortOpen(InputPort port) {
        if (port.getName().equals("levels")) {
            port.bindToFieldNamed("mLevels");
            port.setAutoPullEnabled(true);
        }
    }

    protected void onPrepare() {
        this.mShader = new ImageShader(mPosterizeShaderCode);
    }

    protected void onProcess() {
        OutputPort outPort = getConnectedOutputPort("image");
        FrameImage2D inputImage = getConnectedInputPort("image").pullFrame().asFrameImage2D();
        FrameImage2D outputImage = outPort.fetchAvailableFrame(inputImage.getDimensions()).asFrameImage2D();
        if (this.mLevels < 2) {
            throw new IllegalArgumentException("Posterize filter obtained levels less than 2 (" + this.mLevels + ")!");
        }
        this.mShader.setUniformValue("binSize", 1.0f / ((float) (this.mLevels - 1)));
        this.mShader.process(inputImage, outputImage);
        outPort.pushFrame(outputImage);
    }
}
