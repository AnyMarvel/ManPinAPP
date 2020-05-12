package androidx.media.filterpacks.base;

import androidx.media.filterfw.Filter;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.InputPort;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.Signature;

public final class CountLimitFilter extends Filter {
    private int mCount = 0;
    private int mMaxCount = 1;

    public CountLimitFilter(MffContext context, String name) {
        super(context, name);
    }

    public synchronized void setMaxCount(int maxCount) {
        this.mMaxCount = maxCount;
    }

    public Signature getSignature() {
        return new Signature().addInputPort("frame", 2, FrameType.any()).addInputPort("maxCount", 1, FrameType.single(Integer.TYPE)).addOutputPort("frame", 2, FrameType.any()).disallowOtherPorts();
    }

    public void onInputPortOpen(InputPort port) {
        if (port.getName().equals("maxCount")) {
            port.bindToFieldNamed("mMaxCount");
            port.setAutoPullEnabled(true);
            return;
        }
        port.attachToOutputPort(getConnectedOutputPort("frame"));
    }

    protected void onOpen() {
        this.mCount = 0;
    }

    protected void onClose() {
        this.mCount = 0;
    }

    protected synchronized void onProcess() {
        if (this.mCount < this.mMaxCount) {
            getConnectedOutputPort("frame").pushFrame(getConnectedInputPort("frame").pullFrame());
        }
        this.mCount++;
        if (this.mCount == this.mMaxCount) {
            requestClose();
        }
    }
}
