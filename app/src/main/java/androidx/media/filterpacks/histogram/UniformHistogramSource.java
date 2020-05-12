package androidx.media.filterpacks.histogram;

import androidx.media.filterfw.Filter;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.FrameValues;
import androidx.media.filterfw.InputPort;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.OutputPort;
import androidx.media.filterfw.Signature;

public final class UniformHistogramSource extends Filter {
    private int mNumBins = 50;

    public UniformHistogramSource(MffContext context, String name) {
        super(context, name);
    }

    public Signature getSignature() {
        return new Signature().addInputPort("binsize", 1, FrameType.array(Integer.TYPE)).addOutputPort("histogram", 2, FrameType.array(Integer.TYPE)).disallowOtherPorts();
    }

    public void onInputPortAttached(InputPort port) {
        if (port.getName().equals("binsize")) {
            port.bindToFieldNamed("mNumBins");
            port.setAutoPullEnabled(true);
        }
    }

    protected void onPrepare() {
    }

    protected void onProcess() {
        int[] histogram = new int[this.mNumBins];
        for (int i = 0; i < this.mNumBins; i++) {
            histogram[i] = 1;
        }
        OutputPort outPort = getConnectedOutputPort("histogram");
        FrameValues frame = outPort.fetchAvailableFrame(null).asFrameValues();
        frame.setValues(histogram);
        outPort.pushFrame(frame);
    }
}
