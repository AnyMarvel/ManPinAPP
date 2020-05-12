package androidx.media.filterpacks.histogram;

import androidx.media.filterfw.Filter;
import androidx.media.filterfw.FrameImage2D;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.FrameValue;
import androidx.media.filterfw.InputPort;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.OutputPort;
import androidx.media.filterfw.Signature;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

public final class GrayHistogramFilter extends Filter {
    private boolean mHasMask = false;
    private int mNumBins = 50;

    private static native void extractHistogram(ByteBuffer byteBuffer, ByteBuffer byteBuffer2, IntBuffer intBuffer);

    public GrayHistogramFilter(MffContext context, String name) {
        super(context, name);
    }

    public Signature getSignature() {
        FrameType imageIn = FrameType.image2D(FrameType.ELEMENT_RGBA8888, 1);
        return new Signature().addInputPort("image", 2, imageIn).addInputPort("mask", 1, imageIn).addInputPort("binsize", 1, FrameType.array(Integer.TYPE)).addOutputPort("histogram", 2, FrameType.array(Integer.TYPE)).disallowOtherPorts();
    }

    public void onInputPortAttached(InputPort port) {
        if (port.getName().equals("mask")) {
            this.mHasMask = true;
        } else if (port.getName().equals("binsize")) {
            port.bindToFieldNamed("mNumBins");
            port.setAutoPullEnabled(true);
        }
    }

    protected void onProcess() {
        FrameImage2D inputMask;
        ByteBuffer bufMask;
        FrameImage2D inputImage = getConnectedInputPort("image").pullFrame().asFrameImage2D();
        if (this.mHasMask) {
            inputMask = getConnectedInputPort("mask").pullFrame().asFrameImage2D();
        } else {
            inputMask = null;
        }
        ByteBuffer histogramBuffer = ByteBuffer.allocateDirect(this.mNumBins * 4);
        histogramBuffer.order(ByteOrder.nativeOrder());
        IntBuffer histogramIntBuffer = histogramBuffer.asIntBuffer();
        ByteBuffer bufImg = inputImage.lockBytes(1);
        if (this.mHasMask) {
            bufMask = inputMask.lockBytes(1);
        } else {
            bufMask = null;
        }
        extractHistogram(bufImg, bufMask, histogramIntBuffer);
        bufImg.rewind();
        inputImage.unlock();
        if (this.mHasMask) {
            bufMask.rewind();
            inputMask.unlock();
        }
        histogramIntBuffer.rewind();
        int[] histogram = new int[this.mNumBins];
        for (int i = 0; i < this.mNumBins; i++) {
            histogram[i] = histogramIntBuffer.get();
        }
        OutputPort outPort = getConnectedOutputPort("histogram");
        FrameValue frame = outPort.fetchAvailableFrame(null).asFrameValues();
        frame.setValue(histogram);
        outPort.pushFrame(frame);
    }

    static {
        System.loadLibrary("filterframework_jni");
    }
}
