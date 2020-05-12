package androidx.media.filterpacks.text;

import androidx.media.filterfw.Filter;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.FrameValue;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.OutputPort;
import androidx.media.filterfw.Signature;

public class ToStringFilter extends Filter {
    public ToStringFilter(MffContext context, String name) {
        super(context, name);
    }

    public Signature getSignature() {
        return new Signature().addInputPort("object", 2, FrameType.single()).addOutputPort("string", 2, FrameType.single(String.class)).disallowOtherPorts();
    }

    protected void onProcess() {
        String outStr = getConnectedInputPort("object").pullFrame().asFrameValue().getValue().toString();
        OutputPort outPort = getConnectedOutputPort("string");
        FrameValue stringFrame = outPort.fetchAvailableFrame(null).asFrameValue();
        stringFrame.setValue(outStr);
        outPort.pushFrame(stringFrame);
    }
}
