package androidx.media.filterpacks.base;

import androidx.media.filterfw.Filter;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.InputPort;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.OutputPort;
import androidx.media.filterfw.Signature;

public final class SwitchFilter extends Filter {
    private String mTarget = null;

    public SwitchFilter(MffContext context, String name) {
        super(context, name);
    }

    public Signature getSignature() {
        return new Signature().addInputPort("input", 2, FrameType.any()).addInputPort("target", 1, FrameType.single(String.class)).disallowOtherInputs();
    }

    public void onOutputPortAttached(OutputPort port) {
        port.setWaitsUntilAvailable(false);
    }

    public void onInputPortOpen(InputPort port) {
        if (port.getName().equals("input")) {
            for (OutputPort outputPort : getConnectedOutputPorts()) {
                port.attachToOutputPort(outputPort);
            }
        } else if (port.getName().equals("target")) {
            port.bindToFieldNamed("mTarget");
            port.setAutoPullEnabled(true);
        }
    }

    protected void onProcess() {
        if (this.mTarget != null) {
            OutputPort outputPort = getConnectedOutputPort(this.mTarget);
            if (outputPort == null) {
                String str = this.mTarget;
                throw new RuntimeException(new StringBuilder(String.valueOf(str).length() + 23).append("Unknown target port '").append(str).append("'!").toString());
            } else if (outputPort.isAvailable()) {
                outputPort.pushFrame(getConnectedInputPort("input").pullFrame());
            }
        }
    }
}
