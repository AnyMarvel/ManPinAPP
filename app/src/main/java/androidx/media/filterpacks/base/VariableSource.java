package androidx.media.filterpacks.base;

import androidx.media.filterfw.Filter;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.FrameValue;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.OutputPort;
import androidx.media.filterfw.Signature;

public final class VariableSource extends Filter {
    private OutputPort mOutputPort = null;
    private Object mValue = null;

    public VariableSource(MffContext context, String name) {
        super(context, name);
    }

    public synchronized void setValue(Object value) {
        this.mValue = value;
    }

    public synchronized Object getValue() {
        return this.mValue;
    }

    public Signature getSignature() {
        return new Signature().addOutputPort("value", 2, FrameType.single()).disallowOtherPorts();
    }

    protected void onPrepare() {
        this.mOutputPort = getConnectedOutputPort("value");
    }

    protected synchronized void onProcess() {
        FrameValue frame = this.mOutputPort.fetchAvailableFrame(null).asFrameValue();
        frame.setValue(this.mValue);
        this.mOutputPort.pushFrame(frame);
    }
}
