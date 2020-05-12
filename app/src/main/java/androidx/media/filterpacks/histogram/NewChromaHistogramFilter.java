package androidx.media.filterpacks.histogram;

import androidx.media.filterfw.Filter;
import androidx.media.filterfw.Frame;
import androidx.media.filterfw.FrameBuffer1D;
import androidx.media.filterfw.FrameBuffer2D;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.InputPort;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.OutputPort;
import androidx.media.filterfw.Signature;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public final class NewChromaHistogramFilter extends Filter {
    private int mHueBins = 6;
    private int mSaturationBins = 3;
    private int mSaturationThreshold = 26;
    private int mValueThreshold = 51;

    private static native boolean extractChromaHistogram(ByteBuffer byteBuffer, FloatBuffer floatBuffer, int i, int i2, FloatBuffer floatBuffer2, int i3, int i4, int i5);

    public NewChromaHistogramFilter(MffContext context, String name) {
        super(context, name);
    }

    public Signature getSignature() {
        FrameType imageIn = FrameType.image2D(FrameType.ELEMENT_RGBA8888, 1);
        FrameType hueSatOut = FrameType.buffer2D(200);
        FrameType valueOut = FrameType.buffer1D(200);
        FrameType intType = FrameType.single(Integer.TYPE);
        return new Signature().addInputPort("image", 2, imageIn).addInputPort("huebins", 1, intType).addInputPort("saturationbins", 1, intType).addInputPort("saturationthreshold", 1, FrameType.single(Integer.TYPE)).addInputPort("valuethreshold", 1, intType).addOutputPort("huesat", 2, hueSatOut).addOutputPort("", 2, valueOut).disallowOtherPorts();
    }

    public void onInputPortOpen(InputPort port) {
        if (port.getName().equals("huebins")) {
            port.bindToFieldNamed("mHueBins");
            port.setAutoPullEnabled(true);
        } else if (port.getName().equals("saturationbins")) {
            port.bindToFieldNamed("mSaturationBins");
            port.setAutoPullEnabled(true);
        } else if (port.getName().equals("saturationthreshold")) {
            port.bindToFieldNamed("mSaturationThreshold");
            port.setAutoPullEnabled(true);
        } else if (port.getName().equals("valuethreshold")) {
            port.bindToFieldNamed("mValueThreshold");
            port.setAutoPullEnabled(true);
        }
    }

    protected void onProcess() {
        FrameBuffer2D imageFrame = getConnectedInputPort("image").pullFrame().asFrameImage2D();
        OutputPort hueSatPort = getConnectedOutputPort("huesat");
        OutputPort valuePort = getConnectedOutputPort("value");
        int[] valueDims = new int[]{this.mHueBins};
        FrameBuffer2D hueSatFrame = hueSatPort.fetchAvailableFrame(new int[]{this.mHueBins, this.mSaturationBins}).asFrameBuffer2D();
        FrameBuffer1D valueFrame = valuePort.fetchAvailableFrame(valueDims).asFrameBuffer1D();
        ByteBuffer imageBuffer = imageFrame.lockBytes(1);
        ByteBuffer hueSatBuffer = hueSatFrame.lockBytes(2);
        ByteBuffer valueBuffer = valueFrame.lockBytes(2);
        hueSatBuffer.order(ByteOrder.nativeOrder());
        valueBuffer.order(ByteOrder.nativeOrder());
        if (extractChromaHistogram(imageBuffer, hueSatBuffer.asFloatBuffer(), this.mHueBins, this.mSaturationBins, valueBuffer.asFloatBuffer(), this.mHueBins, this.mSaturationThreshold, this.mValueThreshold)) {
            imageFrame.unlock();
            hueSatFrame.unlock();
            valueFrame.unlock();
            hueSatPort.pushFrame(hueSatFrame);
            valuePort.pushFrame(valueFrame);
            return;
        }
        throw new RuntimeException("Error running native histogram extraction!");
    }

    static {
        System.loadLibrary("filterframework_jni");
    }
}
