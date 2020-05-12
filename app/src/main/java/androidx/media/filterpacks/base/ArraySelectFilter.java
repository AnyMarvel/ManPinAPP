package androidx.media.filterpacks.base;

import androidx.media.filterfw.Filter;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.FrameValue;
import androidx.media.filterfw.InputPort;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.OutputPort;
import androidx.media.filterfw.Signature;
import java.lang.reflect.Array;

public final class ArraySelectFilter extends Filter {
    private Object mDefaultValue = null;
    private int mIndex = 0;

    public ArraySelectFilter(MffContext context, String name) {
        super(context, name);
    }

    public Signature getSignature() {
        return new Signature().addInputPort("array", 2, FrameType.array()).addInputPort("index", 1, FrameType.single(Integer.TYPE)).addInputPort("defaultValue", 1, FrameType.single()).addOutputPort("element", 2, FrameType.single()).disallowOtherPorts();
    }

    public void onInputPortOpen(InputPort port) {
        if (port.getName().equals("index")) {
            port.bindToFieldNamed("mIndex");
            port.setAutoPullEnabled(true);
        } else if (port.getName().equals("defaultValue")) {
            port.bindToFieldNamed("mDefaultValue");
            port.setAutoPullEnabled(true);
        }
    }

    protected void onProcess() {
        InputPort inputPort = getConnectedInputPort("array");
        OutputPort outputPort = getConnectedOutputPort("element");
        Object array = inputPort.pullFrame().asFrameValues().getValues();
        Object element = Array.getLength(array) > this.mIndex ? Array.get(array, this.mIndex) : this.mDefaultValue;
        FrameValue elemFrame = outputPort.fetchAvailableFrame(null).asFrameValue();
        elemFrame.setValue(element);
        outputPort.pushFrame(elemFrame);
    }
}
