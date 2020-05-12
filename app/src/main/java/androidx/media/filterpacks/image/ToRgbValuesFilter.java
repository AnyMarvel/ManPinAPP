package androidx.media.filterpacks.image;

import androidx.media.filterfw.Filter;
import androidx.media.filterfw.FrameBuffer2D;
import androidx.media.filterfw.FrameImage2D;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.OutputPort;
import androidx.media.filterfw.Signature;
import java.nio.ByteBuffer;

public class ToRgbValuesFilter extends Filter {
    private FrameType mImageInType;

    private static native boolean toRgbValues(ByteBuffer byteBuffer, ByteBuffer byteBuffer2);

    public ToRgbValuesFilter(MffContext context, String name) {
        super(context, name);
    }

    public Signature getSignature() {
        this.mImageInType = FrameType.image2D(FrameType.ELEMENT_RGBA8888, 2);
        return new Signature().addInputPort("image", 2, this.mImageInType).addOutputPort("image", 2, FrameType.buffer2D(100)).disallowOtherPorts();
    }

    protected void onProcess() {
        OutputPort outPort = getConnectedOutputPort("image");
        FrameImage2D inputImage = getConnectedInputPort("image").pullFrame().asFrameImage2D();
        int[] dims = inputImage.getDimensions();
        FrameBuffer2D outputFrame = outPort.fetchAvailableFrame(new int[]{dims[0] * 3, dims[1]}).asFrameBuffer2D();
        if (toRgbValues(inputImage.lockBytes(1), outputFrame.lockBytes(2))) {
            inputImage.unlock();
            outputFrame.unlock();
            outPort.pushFrame(outputFrame);
            return;
        }
        throw new RuntimeException("Native implementation encountered an error during processing!");
    }

    static {
        System.loadLibrary("filterframework_jni");
    }
}
