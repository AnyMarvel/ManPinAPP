package androidx.media.filterpacks.numeric;

import androidx.media.filterfw.Filter;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.FrameValue;
import androidx.media.filterfw.InputPort;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.OutputPort;
import androidx.media.filterfw.Signature;
import androidx.media.filterfw.geometry.Quad;
import androidx.media.filterfw.imageutils.RegionStatsCalculator;
import androidx.media.filterfw.imageutils.RegionStatsCalculator.Statistics;

public class StatsFilter extends Filter {
    private Quad mCropRect = Quad.fromRect(0.0f, 0.0f, 1.0f, 1.0f);
    private RegionStatsCalculator mRegionStatsCalculator;
    private boolean mSuppressZero = false;

    public StatsFilter(MffContext context, String name) {
        super(context, name);
    }

    public Signature getSignature() {
        FrameType inputFrame = FrameType.buffer2D(100);
        FrameType floatT = FrameType.single(Float.TYPE);
        return new Signature().addInputPort("buffer", 2, inputFrame).addInputPort("cropRect", 1, FrameType.single(Quad.class)).addInputPort("suppressZero", 1, FrameType.single(Boolean.TYPE)).addOutputPort("mean", 1, floatT).addOutputPort("stdev", 1, floatT).disallowOtherPorts();
    }

    public void onInputPortOpen(InputPort port) {
        if (port.getName().equals("cropRect")) {
            port.bindToFieldNamed("mCropRect");
            port.setAutoPullEnabled(true);
        }
        if (port.getName().equals("suppressZero")) {
            port.bindToFieldNamed("mSuppressZero");
            port.setAutoPullEnabled(true);
        }
    }

    protected void onPrepare() {
        super.onPrepare();
        this.mRegionStatsCalculator = new RegionStatsCalculator();
    }

    protected void onProcess() {
        Statistics stats = this.mRegionStatsCalculator.calcMeanAndStd(getConnectedInputPort("buffer").pullFrame().asFrameBuffer2D(), this.mCropRect, this.mSuppressZero);
        OutputPort outPort = getConnectedOutputPort("mean");
        if (outPort != null) {
            FrameValue outFrame = outPort.fetchAvailableFrame(null).asFrameValue();
            outFrame.setValue(Float.valueOf(stats.mean));
            outPort.pushFrame(outFrame);
        }
        OutputPort outPortStdev = getConnectedOutputPort("stdev");
        if (outPortStdev != null) {
            FrameValue outFrameStdev = outPortStdev.fetchAvailableFrame(null).asFrameValue();
            outFrameStdev.setValue(Float.valueOf(stats.stdDev));
            outPortStdev.pushFrame(outFrameStdev);
        }
    }
}
