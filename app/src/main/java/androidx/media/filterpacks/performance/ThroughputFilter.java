package androidx.media.filterpacks.performance;

import android.os.SystemClock;
import android.util.Log;
import androidx.media.filterfw.Filter;
import androidx.media.filterfw.Frame;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.FrameValue;
import androidx.media.filterfw.InputPort;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.OutputPort;
import androidx.media.filterfw.Signature;

public class ThroughputFilter extends Filter {
    private long mLastTime = 0;
    private int mPeriod = 3;
    private int mPeriodFrameCount = 0;
    private int mTotalFrameCount = 0;

    public ThroughputFilter(MffContext context, String name) {
        super(context, name);
    }

    public Signature getSignature() {
        return new Signature().addInputPort("frame", 2, FrameType.any()).addOutputPort("throughput", 2, FrameType.single(Throughput.class)).addOutputPort("frame", 2, FrameType.any()).addInputPort("period", 1, FrameType.single(Integer.TYPE)).disallowOtherPorts();
    }

    public void onInputPortOpen(InputPort port) {
        if (port.getName().equals("period")) {
            port.bindToFieldNamed("mPeriod");
            port.setAutoPullEnabled(true);
            return;
        }
        port.attachToOutputPort(getConnectedOutputPort("frame"));
    }

    protected void onOpen() {
        this.mTotalFrameCount = 0;
        this.mPeriodFrameCount = 0;
        this.mLastTime = 0;
    }

    protected synchronized void onProcess() {
        Frame inputFrame = getConnectedInputPort("frame").pullFrame();
        this.mTotalFrameCount++;
        this.mPeriodFrameCount++;
        if (this.mLastTime == 0) {
            this.mLastTime = SystemClock.elapsedRealtime();
        }
        long curTime = SystemClock.elapsedRealtime();
        if (curTime - this.mLastTime >= ((long) (this.mPeriod * 1000))) {
            Log.i("Thru", "It is time!");
            OutputPort tpPort = getConnectedOutputPort("throughput");
            Throughput throughput = new Throughput(this.mTotalFrameCount, this.mPeriodFrameCount, curTime - this.mLastTime, inputFrame.getElementCount());
            FrameValue throughputFrame = tpPort.fetchAvailableFrame(null).asFrameValue();
            throughputFrame.setValue(throughput);
            tpPort.pushFrame(throughputFrame);
            this.mLastTime = curTime;
            this.mPeriodFrameCount = 0;
        }
        getConnectedOutputPort("frame").pushFrame(inputFrame);
    }
}
