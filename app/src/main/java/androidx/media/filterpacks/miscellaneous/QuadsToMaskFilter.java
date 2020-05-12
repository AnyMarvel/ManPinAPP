package androidx.media.filterpacks.miscellaneous;

import androidx.media.filterfw.Filter;
import androidx.media.filterfw.FrameImage2D;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.ImageShader;
import androidx.media.filterfw.InputPort;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.OutputPort;
import androidx.media.filterfw.Signature;
import androidx.media.filterfw.geometry.Quad;

public final class QuadsToMaskFilter extends Filter {
    private ImageShader mBackgroundShader;
    private final String mBgFragmentShader = "precision mediump float;\nvoid main() {\n  gl_FragColor = vec4(0.0, 0.0, 0.0, 0.0);\n}\n";
    private int[] mImageSize;
    private final String mQuadFragmentShader = "precision mediump float;\nvoid main() {\n  gl_FragColor = vec4(1.0, 1.0, 1.0, 1.0);\n}\n";
    private ImageShader mQuadShader;
    private Quad[] mQuads;

    public QuadsToMaskFilter(MffContext context, String name) {
        super(context, name);
    }

    public Signature getSignature() {
        return new Signature().addInputPort("quads", 2, FrameType.array(Quad.class)).addInputPort("imageSize", 2, FrameType.array(Integer.TYPE)).addOutputPort("mask", 2, FrameType.image2D(FrameType.ELEMENT_RGBA8888, 16)).disallowOtherPorts();
    }

    public void onInputPortOpen(InputPort port) {
        if (port.getName().equals("imageSize")) {
            port.bindToFieldNamed("mImageSize");
            port.setAutoPullEnabled(true);
        } else if (port.getName().equals("quads")) {
            port.bindToFieldNamed("mQuads");
            port.setAutoPullEnabled(true);
        }
    }

    protected void onPrepare() {
        this.mBackgroundShader = new ImageShader("precision mediump float;\nvoid main() {\n  gl_FragColor = vec4(0.0, 0.0, 0.0, 0.0);\n}\n");
        this.mQuadShader = new ImageShader("precision mediump float;\nvoid main() {\n  gl_FragColor = vec4(1.0, 1.0, 1.0, 1.0);\n}\n");
    }

    protected void onProcess() {
        OutputPort outPort = getConnectedOutputPort("mask");
        FrameImage2D outputImage = outPort.fetchAvailableFrame(this.mImageSize).asFrameImage2D();
        this.mBackgroundShader.processNoInput(outputImage);
        for (Quad targetQuad : this.mQuads) {
            this.mQuadShader.setTargetQuad(targetQuad);
            this.mQuadShader.processNoInput(outputImage);
        }
        outPort.pushFrame(outputImage);
    }
}
