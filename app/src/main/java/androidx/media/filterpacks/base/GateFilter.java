package androidx.media.filterpacks.base;

import androidx.media.filterfw.Filter;
import androidx.media.filterfw.Frame;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.InputPort;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.Signature;

public final class GateFilter extends Filter {
    private int mPassFrames;

    public GateFilter(MffContext context, String name) {
        super(context, name);
    }

    public synchronized void passNextFrame() {
        this.mPassFrames = 1;
    }

    public synchronized void passNextFrames(int count) {
        this.mPassFrames = count;
    }

    public Signature getSignature() {
        return new Signature().addInputPort("frame", 2, FrameType.any()).addOutputPort("frame", 2, FrameType.any()).disallowOtherPorts();
    }

    public void onInputPortOpen(InputPort port) {
        port.attachToOutputPort(getConnectedOutputPort("frame"));
    }

    protected void onOpen() {
        this.mPassFrames = 0;
    }

    protected synchronized void onProcess() {
        Frame frame = getConnectedInputPort("frame").pullFrame();
        if (this.mPassFrames > 0) {
            getConnectedOutputPort("frame").pushFrame(frame);
            this.mPassFrames--;
        }
    }
}
