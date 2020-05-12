package androidx.media.filterpacks.image;

import androidx.media.filterfw.Filter;
import androidx.media.filterfw.FrameImage2D;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.InputPort;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.OutputPort;
import androidx.media.filterfw.Signature;
import androidx.media.filterfw.imageutils.FastBoxBlur;

public class BoxFilter extends Filter {
    private FastBoxBlur mBoxBlurOperator;
    private int mFilterHeight;
    private int mFilterWidth;

    public BoxFilter(MffContext context, String name) {
        super(context, name);
    }

    public Signature getSignature() {
        FrameType imageIn = FrameType.image2D(FrameType.ELEMENT_RGBA8888, 2);
        return new Signature().addInputPort("image", 2, imageIn).addInputPort("filterWidth", 2, FrameType.single(Integer.TYPE)).addInputPort("filterHeight", 2, FrameType.single(Integer.TYPE)).addOutputPort("image", 2, FrameType.image2D(FrameType.ELEMENT_RGBA8888, 16)).disallowOtherPorts();
    }

    public void onInputPortOpen(InputPort port) {
        if (port.getName().equals("filterWidth")) {
            port.bindToFieldNamed("mFilterWidth");
            port.setAutoPullEnabled(true);
        }
        if (port.getName().equals("filterHeight")) {
            port.bindToFieldNamed("mFilterHeight");
            port.setAutoPullEnabled(true);
        }
    }

    protected void onPrepare() {
        this.mFilterWidth |= 1;
        this.mFilterHeight |= 1;
        this.mBoxBlurOperator = new FastBoxBlur(isOpenGLSupported(), this.mFilterWidth, this.mFilterHeight);
    }

    protected void onProcess() {
        OutputPort smoothedImagePort = getConnectedOutputPort("image");
        FrameImage2D inputImage = getConnectedInputPort("image").pullFrame().asFrameImage2D();
        FrameImage2D smoothedImage = smoothedImagePort.fetchAvailableFrame(inputImage.getDimensions()).asFrameImage2D();
        if (this.mFilterHeight > inputImage.getHeight() || this.mFilterWidth > inputImage.getWidth()) {
            throw new UnsupportedOperationException("Can not apply a box filter that is largerthan the original image!");
        }
        this.mBoxBlurOperator.computeBlur(inputImage, smoothedImage);
        if (smoothedImage != null) {
            smoothedImagePort.pushFrame(smoothedImage);
        }
    }
}
