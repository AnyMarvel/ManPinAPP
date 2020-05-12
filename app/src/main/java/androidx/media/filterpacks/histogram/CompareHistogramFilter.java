package androidx.media.filterpacks.histogram;

import androidx.media.filterfw.Filter;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.FrameValue;
import androidx.media.filterfw.InputPort;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.OutputPort;
import androidx.media.filterfw.Signature;

public final class CompareHistogramFilter extends Filter {
    public static final int EMD = 0;
    private int[] mHistogram1 = null;
    private int[] mHistogram2 = null;
    private int mMetric = 0;

    public CompareHistogramFilter(MffContext context, String name) {
        super(context, name);
    }

    public Signature getSignature() {
        return new Signature().addInputPort("histogram1", 2, FrameType.array(Integer.TYPE)).addInputPort("histogram2", 2, FrameType.array(Integer.TYPE)).addInputPort("metric", 1, FrameType.single(Integer.TYPE)).addOutputPort("value", 2, FrameType.single(Float.TYPE)).disallowOtherPorts();
    }

    public void onInputPortOpen(InputPort port) {
        if (port.getName().equals("histogram1")) {
            port.bindToFieldNamed("mHistogram1");
            port.setAutoPullEnabled(true);
        } else if (port.getName().equals("histogram2")) {
            port.bindToFieldNamed("mHistogram2");
            port.setAutoPullEnabled(true);
        } else if (port.getName().equals("metric")) {
            port.bindToFieldNamed("mMetric");
            port.setAutoPullEnabled(true);
        }
    }

    protected void onProcess() {
        switch (this.mMetric) {
            case 0:
                if (this.mHistogram1.length != this.mHistogram2.length) {
                    throw new RuntimeException("Can only compare histograms of same length!");
                }
                int i;
                int total1 = 0;
                int total2 = 0;
                for (i = 0; i < this.mHistogram1.length; i++) {
                    total1 += this.mHistogram1[i];
                    total2 += this.mHistogram2[i];
                }
                int runningSum1 = 0;
                int runningSum2 = 0;
                float result = 0.0f;
                for (i = 0; i < this.mHistogram1.length; i++) {
                    runningSum1 += this.mHistogram1[i];
                    runningSum2 += this.mHistogram2[i];
                    result += Math.abs((((float) runningSum1) / ((float) total1)) - (((float) runningSum2) / ((float) total2)));
                }
                result /= this.mHistogram1.length > 1 ? (float) (this.mHistogram1.length - 1) : 1.0f;
                OutputPort outPort = getConnectedOutputPort("value");
                FrameValue frame = outPort.fetchAvailableFrame(null).asFrameValue();
                frame.setValue(Float.valueOf(result));
                outPort.pushFrame(frame);
                return;
            default:
                throw new RuntimeException("Unknown metric to compare histograms!");
        }
    }
}
