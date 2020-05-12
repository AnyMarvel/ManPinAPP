package androidx.media.filterpacks.image;

import android.graphics.Color;
import androidx.media.filterfw.Filter;
import androidx.media.filterfw.FrameImage2D;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.ImageShader;
import androidx.media.filterfw.InputPort;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.OutputPort;
import androidx.media.filterfw.Signature;

public class TintFilter extends Filter {
    private static final String mTintShaderCode = "precision mediump float;\nuniform sampler2D tex_sampler_0;\nuniform vec3 tint;\nuniform vec3 color_ratio;\nvarying vec2 v_texcoord;\nvoid main() {\n  vec4 color = texture2D(tex_sampler_0, v_texcoord);\n  float avg_color = dot(color_ratio, color.rgb);\n  vec3 new_color = min(0.8 * avg_color + 0.2 * tint, 1.0);\n  gl_FragColor = vec4(new_color.rgb, color.a);\n}\n";
    private ImageShader mShader;
    private int mTint = -16776961;

    public TintFilter(MffContext context, String name) {
        super(context, name);
    }

    public Signature getSignature() {
        FrameType imageIn = FrameType.image2D(FrameType.ELEMENT_RGBA8888, 2);
        return new Signature().addInputPort("image", 2, imageIn).addInputPort("tint", 1, FrameType.single(Integer.TYPE)).addOutputPort("image", 2, FrameType.image2D(FrameType.ELEMENT_RGBA8888, 16)).disallowOtherPorts();
    }

    public void onInputPortOpen(InputPort port) {
        if (port.getName().equals("tint")) {
            port.bindToFieldNamed("mTint");
            port.setAutoPullEnabled(true);
        }
    }

    protected void onPrepare() {
        this.mShader = new ImageShader(mTintShaderCode);
        this.mShader.setUniformValue("color_ratio", new float[]{0.21f, 0.71f, 0.07f});
    }

    protected synchronized void onProcess() {
        OutputPort outPort = getConnectedOutputPort("image");
        FrameImage2D inputImage = getConnectedInputPort("image").pullFrame().asFrameImage2D();
        FrameImage2D outputImage = outPort.fetchAvailableFrame(inputImage.getDimensions()).asFrameImage2D();
        this.mShader.setUniformValue("tint", new float[]{((float) Color.red(this.mTint)) / 255.0f, ((float) Color.green(this.mTint)) / 255.0f, ((float) Color.blue(this.mTint)) / 255.0f});
        this.mShader.process(inputImage, outputImage);
        outPort.pushFrame(outputImage);
    }
}
