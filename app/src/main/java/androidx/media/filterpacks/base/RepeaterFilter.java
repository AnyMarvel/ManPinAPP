package androidx.media.filterpacks.base;

import androidx.media.filterfw.Filter;
import androidx.media.filterfw.Frame;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.InputPort;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.Signature;

public final class RepeaterFilter extends Filter {
    Frame mCachedFrame = null;
    int mRepeat = 0;

    public RepeaterFilter(MffContext context, String name) {
        super(context, name);
    }

    public Signature getSignature() {
        return new Signature().addInputPort("input", 2, FrameType.any()).addInputPort("repeat", 2, FrameType.single(Integer.TYPE)).addOutputPort("output", 2, FrameType.any()).disallowOtherPorts();
    }

    public void onInputPortOpen(InputPort port) {
        if (port.getName().equals("input")) {
            port.attachToOutputPort(getConnectedOutputPort("output"));
        }
    }

    protected void onProcess() {
        InputPort control = getConnectedInputPort("repeat");
        InputPort inputPort = getConnectedInputPort("input");
        if (this.mRepeat <= 1) {
            if (this.mCachedFrame != null) {
                this.mCachedFrame.release();
            }
            this.mCachedFrame = inputPort.pullFrame().retain();
        }
        this.mRepeat = ((Integer) control.pullFrame().asFrameValue().getValue()).intValue();
        inputPort.setWaitsForFrame(this.mRepeat <= 1);
        if (this.mRepeat > 0) {
            getConnectedOutputPort("output").pushFrame(this.mCachedFrame);
        }
    }

    protected void onTearDown() {
        if (this.mCachedFrame != null) {
            this.mCachedFrame.release();
        }
    }
}
