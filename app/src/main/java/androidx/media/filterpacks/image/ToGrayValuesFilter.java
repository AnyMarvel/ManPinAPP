package androidx.media.filterpacks.image;

import androidx.media.filterfw.Filter;
import androidx.media.filterfw.FrameBuffer2D;
import androidx.media.filterfw.FrameImage2D;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.OutputPort;
import androidx.media.filterfw.Signature;
import androidx.media.filterfw.imageutils.GrayValuesExtractor;

public class ToGrayValuesFilter extends Filter {
    private GrayValuesExtractor mGrayValuesExtractor;

    public ToGrayValuesFilter(MffContext context, String name) {
        super(context, name);
    }

    public Signature getSignature() {
        return new Signature().addInputPort("image", 2, FrameType.image2D(FrameType.ELEMENT_RGBA8888, 2)).addOutputPort("image", 2, FrameType.buffer2D(100)).disallowOtherPorts();
    }

    protected void onPrepare() {
        this.mGrayValuesExtractor = new GrayValuesExtractor(isOpenGLSupported());
    }

    protected void onProcess() {
        OutputPort outPort = getConnectedOutputPort("image");
        FrameImage2D inputImage = getConnectedInputPort("image").pullFrame().asFrameImage2D();
        FrameBuffer2D outputGrayValuesFrame = outPort.fetchAvailableFrame(this.mGrayValuesExtractor.getOutputFrameDimensions(inputImage.getDimensions())).asFrameBuffer2D();
        this.mGrayValuesExtractor.toGrayValues(inputImage, outputGrayValuesFrame);
        outPort.pushFrame(outputGrayValuesFrame);
    }
}
