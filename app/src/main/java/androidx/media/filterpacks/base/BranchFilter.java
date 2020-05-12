package androidx.media.filterpacks.base;

import androidx.media.filterfw.Filter;
import androidx.media.filterfw.Frame;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.InputPort;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.OutputPort;
import androidx.media.filterfw.Signature;

public final class BranchFilter extends Filter {
    private boolean mSynchronized = true;

    public BranchFilter(MffContext context, String name) {
        super(context, name);
    }

    public BranchFilter(MffContext context, String name, boolean synced) {
        super(context, name);
        this.mSynchronized = synced;
    }

    public Signature getSignature() {
        return new Signature().addInputPort("input", 2, FrameType.any()).addInputPort("synchronized", 1, FrameType.single(Boolean.TYPE)).disallowOtherInputs();
    }

    public void onInputPortOpen(InputPort port) {
        if (port.getName().equals("input")) {
            for (OutputPort outputPort : getConnectedOutputPorts()) {
                port.attachToOutputPort(outputPort);
            }
        } else if (port.getName().equals("synchronized")) {
            port.bindToFieldNamed("mSynchronized");
            port.setAutoPullEnabled(true);
        }
    }

    protected void onOpen() {
        updateSynchronization();
    }

    protected void onProcess() {
        Frame inputFrame = getConnectedInputPort("input").pullFrame();
        for (OutputPort outputPort : getConnectedOutputPorts()) {
            if (outputPort.isAvailable()) {
                outputPort.pushFrame(inputFrame);
            }
        }
    }

    private void updateSynchronization() {
        int i = 0;
        if (this.mSynchronized) {
            OutputPort[] connectedOutputPorts = getConnectedOutputPorts();
            int length = connectedOutputPorts.length;
            while (i < length) {
                connectedOutputPorts[i].setWaitsUntilAvailable(true);
                i++;
            }
            return;
        }
        for (OutputPort port : getConnectedOutputPorts()) {
            port.setWaitsUntilAvailable(false);
        }
    }
}
