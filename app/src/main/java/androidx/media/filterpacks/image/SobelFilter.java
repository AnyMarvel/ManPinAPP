package androidx.media.filterpacks.image;

import androidx.media.filterfw.Filter;
import androidx.media.filterfw.FrameImage2D;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.OutputPort;
import androidx.media.filterfw.Signature;
import androidx.media.filterfw.imageutils.SobelOperator;

public class SobelFilter extends Filter {
    private SobelOperator mSobelOperator;

    public SobelFilter(MffContext context, String name) {
        super(context, name);
    }

    public Signature getSignature() {
        FrameType imageIn = FrameType.image2D(FrameType.ELEMENT_RGBA8888, 2);
        FrameType imageOut = FrameType.image2D(FrameType.ELEMENT_RGBA8888, 16);
        return new Signature().addInputPort("image", 2, imageIn).addOutputPort("gradientX", 1, imageOut).addOutputPort("gradientY", 1, imageOut).addOutputPort("direction", 1, imageOut).addOutputPort("magnitude", 1, imageOut).disallowOtherPorts();
    }

    protected void onPrepare() {
        this.mSobelOperator = new SobelOperator(isOpenGLSupported());
    }

    protected void onProcess() {
        FrameImage2D magImage;
        FrameImage2D dirImage;
        FrameImage2D gxFrame;
        FrameImage2D gyFrame = null;
        OutputPort gradientXPort = getConnectedOutputPort("gradientX");
        OutputPort gradientYPort = getConnectedOutputPort("gradientY");
        OutputPort magnitudePort = getConnectedOutputPort("magnitude");
        OutputPort directionPort = getConnectedOutputPort("direction");
        FrameImage2D inputImage = getConnectedInputPort("image").pullFrame().asFrameImage2D();
        int[] inputDims = inputImage.getDimensions();
        if (magnitudePort != null) {
            magImage = magnitudePort.fetchAvailableFrame(inputDims).asFrameImage2D();
        } else {
            magImage = null;
        }
        if (directionPort != null) {
            dirImage = directionPort.fetchAvailableFrame(inputDims).asFrameImage2D();
        } else {
            dirImage = null;
        }
        if (gradientXPort != null) {
            gxFrame = gradientXPort.fetchAvailableFrame(inputDims).asFrameImage2D();
        } else {
            gxFrame = null;
        }
        if (gradientYPort != null) {
            gyFrame = gradientYPort.fetchAvailableFrame(inputDims).asFrameImage2D();
        }
        this.mSobelOperator.calculate(inputImage, gxFrame, gyFrame, magImage, dirImage);
        if (magImage != null) {
            magnitudePort.pushFrame(magImage);
        }
        if (dirImage != null) {
            directionPort.pushFrame(dirImage);
        }
        if (gradientXPort != null) {
            gradientXPort.pushFrame(gxFrame);
        }
        if (gradientYPort != null) {
            gradientYPort.pushFrame(gyFrame);
        }
    }
}
