package androidx.media.filterpacks.colorspace;

import androidx.media.filterfw.ColorSpace;
import androidx.media.filterfw.Filter;
import androidx.media.filterfw.FrameImage2D;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.OutputPort;
import androidx.media.filterfw.Signature;

public class RgbToYcbcrFilter extends Filter {
    public RgbToYcbcrFilter(MffContext context, String name) {
        super(context, name);
    }

    public Signature getSignature() {
        FrameType imageIn = FrameType.image2D(FrameType.ELEMENT_RGBA8888, 2);
        return new Signature().addInputPort("image", 2, imageIn).addOutputPort("image", 2, FrameType.image2D(FrameType.ELEMENT_RGBA8888, 16)).disallowOtherPorts();
    }

    protected void onProcess() {
        OutputPort outPort = getConnectedOutputPort("image");
        FrameImage2D inputImage = getConnectedInputPort("image").pullFrame().asFrameImage2D();
        int[] dim = inputImage.getDimensions();
        FrameImage2D outputImage = outPort.fetchAvailableFrame(dim).asFrameImage2D();
        ColorSpace.convertRgba8888ToYcbcra8888(inputImage.lockBytes(1), outputImage.lockBytes(1), dim[0], dim[1]);
        inputImage.unlock();
        outputImage.unlock();
        outPort.pushFrame(outputImage);
    }
}
