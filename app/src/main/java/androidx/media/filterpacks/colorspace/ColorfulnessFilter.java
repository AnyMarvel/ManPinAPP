package androidx.media.filterpacks.colorspace;

import androidx.media.filterfw.Filter;
import androidx.media.filterfw.FrameBuffer2D;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.FrameValue;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.OutputPort;
import androidx.media.filterfw.Signature;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public final class ColorfulnessFilter extends Filter {
    public ColorfulnessFilter(MffContext context, String name) {
        super(context, name);
    }

    public Signature getSignature() {
        return new Signature().addInputPort("histogram", 2, FrameType.buffer2D(200)).addOutputPort("score", 2, FrameType.single(Float.TYPE)).disallowOtherPorts();
    }

    protected void onProcess() {
        int c;
        FrameBuffer2D histogramFrame = getConnectedInputPort("histogram").pullFrame().asFrameBuffer2D();
        ByteBuffer byteBuffer = histogramFrame.lockBytes(1);
        byteBuffer.order(ByteOrder.nativeOrder());
        FloatBuffer histogramBuffer = byteBuffer.asFloatBuffer();
        histogramBuffer.rewind();
        int hueBins = histogramFrame.getWidth();
        int saturationBins = histogramFrame.getHeight();
        float[] hueHistogram = new float[hueBins];
        float total = 0.0f;
        for (int r = 0; r < saturationBins; r++) {
            float weight = (float) Math.pow(2.0d, (double) r);
            for (c = 0; c < hueBins; c++) {
                float value = histogramBuffer.get() * weight;
                hueHistogram[c] = hueHistogram[c] + value;
                total += value;
            }
        }
        float colorful = 0.0f;
        for (c = 0; c < hueBins; c++) {
            float value = hueHistogram[c] / total;
            if (value > 0.0f) {
                colorful -= ((float) Math.log((double) value)) * value;
            }
        }
        colorful = (float) (((double) colorful) / Math.log(2.0d));
        histogramFrame.unlock();
        OutputPort outPort = getConnectedOutputPort("score");
        FrameValue frameValue = outPort.fetchAvailableFrame(null).asFrameValue();
        frameValue.setValue(Float.valueOf(colorful));
        outPort.pushFrame(frameValue);
    }
}
