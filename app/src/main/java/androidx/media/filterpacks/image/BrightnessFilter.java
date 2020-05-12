package androidx.media.filterpacks.image;

import androidx.media.filterfw.Filter;
import androidx.media.filterfw.FrameImage2D;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.ImageShader;
import androidx.media.filterfw.InputPort;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.OutputPort;
import androidx.media.filterfw.Signature;

public class BrightnessFilter extends Filter {
    private static final String mBrightnessShader = "precision mediump float;\nuniform sampler2D tex_sampler_0;\nuniform float brightness;\nvarying vec2 v_texcoord;\nvoid main() {\n  vec4 color = texture2D(tex_sampler_0, v_texcoord);\n  if (brightness < 0.5) {\n    gl_FragColor = color * (2.0 * brightness);\n  } else {\n    vec4 diff = 1.0 - color;\n    gl_FragColor = color + diff * (2.0 * (brightness - 0.5));\n  }\n}\n";
    private float mBrightness = 1.0f;
    private ImageShader mShader;

    public BrightnessFilter(MffContext context, String name) {
        super(context, name);
    }

    public Signature getSignature() {
        FrameType imageIn = FrameType.image2D(FrameType.ELEMENT_RGBA8888, 2);
        return new Signature().addInputPort("image", 2, imageIn).addInputPort("brightness", 1, FrameType.single(Float.TYPE)).addOutputPort("image", 2, FrameType.image2D(FrameType.ELEMENT_RGBA8888, 16)).disallowOtherPorts();
    }

    public void onInputPortOpen(InputPort port) {
        if (port.getName().equals("brightness")) {
            port.bindToFieldNamed("mBrightness");
            port.setAutoPullEnabled(true);
        }
    }

    protected void onPrepare() {
        this.mShader = new ImageShader(mBrightnessShader);
    }

    protected void onProcess() {
        OutputPort outPort = getConnectedOutputPort("image");
        FrameImage2D inputImage = getConnectedInputPort("image").pullFrame().asFrameImage2D();
        FrameImage2D outputImage = outPort.fetchAvailableFrame(inputImage.getDimensions()).asFrameImage2D();
        this.mShader.setUniformValue("brightness", this.mBrightness);
        this.mShader.process(inputImage, outputImage);
        outPort.pushFrame(outputImage);
    }
}
