package androidx.media.filterpacks.image;

import androidx.media.filterfw.Filter;
import androidx.media.filterfw.FrameImage2D;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.FrameValue;
import androidx.media.filterfw.InputPort;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.OutputPort;
import androidx.media.filterfw.Signature;
import java.nio.ByteBuffer;

public class PerceptualSharpnessFilter extends Filter {
    private static native float computePerceptualSharpness(int i, int i2, ByteBuffer byteBuffer, ByteBuffer byteBuffer2, ByteBuffer byteBuffer3);

    public PerceptualSharpnessFilter(MffContext context, String name) {
        super(context, name);
    }

    public Signature getSignature() {
        FrameType imageIn = FrameType.image2D(FrameType.ELEMENT_RGBA8888, 2);
        return new Signature().addInputPort("image", 2, imageIn).addInputPort("blurredX", 2, imageIn).addInputPort("blurredY", 2, imageIn).addOutputPort("sharpness", 2, FrameType.single(Float.TYPE)).disallowOtherPorts();
    }

    protected void onProcess() {
        float sharpness;
        InputPort inputPort = getConnectedInputPort("image");
        InputPort blurredXPort = getConnectedInputPort("blurredX");
        InputPort blurredYPort = getConnectedInputPort("blurredY");
        OutputPort sharpnessPort = getConnectedOutputPort("sharpness");
        FrameImage2D inputImage = inputPort.pullFrame().asFrameImage2D();
        FrameImage2D blurredXImage = blurredXPort.pullFrame().asFrameImage2D();
        FrameImage2D blurredYImage = blurredYPort.pullFrame().asFrameImage2D();
        ByteBuffer inputBuffer = inputImage.lockBytes(1);
        ByteBuffer blurredXBuffer = blurredXImage.lockBytes(1);
        ByteBuffer blurredYBuffer = blurredYImage.lockBytes(1);
        if (inputImage.getWidth() == 0 || inputImage.getHeight() == 0) {
            sharpness = 0.0f;
        } else {
            sharpness = computePerceptualSharpness(inputImage.getWidth(), inputImage.getHeight(), inputBuffer, blurredXBuffer, blurredYBuffer);
        }
        inputImage.unlock();
        blurredXImage.unlock();
        blurredYImage.unlock();
        FrameValue sharpnessValue = sharpnessPort.fetchAvailableFrame(null).asFrameValue();
        sharpnessValue.setValue(Float.valueOf(sharpness));
        sharpnessPort.pushFrame(sharpnessValue);
    }

    static {
        System.loadLibrary("filterframework_jni");
    }
}
