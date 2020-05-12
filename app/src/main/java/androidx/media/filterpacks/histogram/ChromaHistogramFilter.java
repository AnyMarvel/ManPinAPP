package androidx.media.filterpacks.histogram;

import androidx.media.filterfw.Filter;
import androidx.media.filterfw.FrameBuffer2D;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.InputPort;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.OutputPort;
import androidx.media.filterfw.Signature;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public final class ChromaHistogramFilter extends Filter {
    private int mHueBins = 6;
    private int mSaturationBins = 3;

    private static native void extractChromaHistogram(ByteBuffer byteBuffer, FloatBuffer floatBuffer, int i, int i2);

    public ChromaHistogramFilter(MffContext context, String name) {
        super(context, name);
    }

    public Signature getSignature() {
        FrameType imageIn = FrameType.image2D(FrameType.ELEMENT_RGBA8888, 1);
        return new Signature().addInputPort("image", 2, imageIn).addInputPort("huebins", 1, FrameType.single(Integer.TYPE)).addInputPort("saturationbins", 1, FrameType.single(Integer.TYPE)).addOutputPort("histogram", 2, FrameType.buffer2D(200)).disallowOtherPorts();
    }

    public void onInputPortAttached(InputPort port) {
        if (port.getName().equals("huebins")) {
            port.bindToFieldNamed("mHueBins");
            port.setAutoPullEnabled(true);
        } else if (port.getName().equals("saturationbins")) {
            port.bindToFieldNamed("mSaturationBins");
            port.setAutoPullEnabled(true);
        }
    }

    public int getSchedulePriority() {
        return 25;
    }

    protected void onProcess() {
        FrameBuffer2D imageFrame = getConnectedInputPort("image").pullFrame().asFrameImage2D();
        OutputPort outPort = getConnectedOutputPort("histogram");
        FrameBuffer2D histogramFrame = outPort.fetchAvailableFrame(new int[]{this.mHueBins, this.mSaturationBins}).asFrameBuffer2D();
        ByteBuffer imageBuffer = imageFrame.lockBytes(1);
        ByteBuffer histogramBuffer = histogramFrame.lockBytes(1);
        histogramBuffer.order(ByteOrder.nativeOrder());
        extractChromaHistogram(imageBuffer, histogramBuffer.asFloatBuffer(), this.mHueBins, this.mSaturationBins);
        imageFrame.unlock();
        histogramFrame.unlock();
        outPort.pushFrame(histogramFrame);
    }

    static {
        System.loadLibrary("filterframework_jni");
    }
}
