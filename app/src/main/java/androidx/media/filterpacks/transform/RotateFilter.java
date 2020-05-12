package androidx.media.filterpacks.transform;

import androidx.media.filterfw.Filter;
import androidx.media.filterfw.FrameImage2D;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.ImageShader;
import androidx.media.filterfw.InputPort;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.OutputPort;
import androidx.media.filterfw.Signature;
import androidx.media.filterfw.geometry.Quad;

public class RotateFilter extends Filter {
    private float mRotateAngle = 0.0f;
    private ImageShader mShader;
    private Quad mSourceRect = Quad.fromRect(0.0f, 0.0f, 1.0f, 1.0f);

    public RotateFilter(MffContext context, String name) {
        super(context, name);
    }

    public Signature getSignature() {
        FrameType imageIn = FrameType.image2D(FrameType.ELEMENT_RGBA8888, 2);
        return new Signature().addInputPort("image", 2, imageIn).addInputPort("rotateAngle", 2, FrameType.single(Float.TYPE)).addInputPort("sourceRect", 1, FrameType.single(Quad.class)).addOutputPort("image", 2, FrameType.image2D(FrameType.ELEMENT_RGBA8888, 16)).disallowOtherPorts();
    }

    public void onInputPortOpen(InputPort port) {
        if (port.getName().equals("rotateAngle")) {
            port.bindToFieldNamed("mRotateAngle");
            port.setAutoPullEnabled(true);
        } else if (port.getName().equals("sourceRect")) {
            port.bindToFieldNamed("mSourceRect");
            port.setAutoPullEnabled(true);
        }
    }

    protected void onPrepare() {
        this.mShader = ImageShader.createIdentity();
    }

    protected void onProcess() {
        OutputPort outPort = getConnectedOutputPort("image");
        FrameImage2D inputImage = getConnectedInputPort("image").pullFrame().asFrameImage2D();
        FrameImage2D outputImage = outPort.fetchAvailableFrame(inputImage.getDimensions()).asFrameImage2D();
        this.mShader.setSourceQuad(this.mSourceRect);
        this.mShader.setTargetQuad(this.mSourceRect.rotated((float) (((double) (this.mRotateAngle / 180.0f)) * Math.PI)));
        this.mShader.process(inputImage, outputImage);
        outPort.pushFrame(outputImage);
    }
}
