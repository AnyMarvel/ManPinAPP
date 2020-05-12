package androidx.media.filterpacks.miscellaneous;

import androidx.media.filterfw.Filter;
import androidx.media.filterfw.Frame;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.FrameValue;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.OutputPort;
import androidx.media.filterfw.Signature;

public final class DimensionsFilter extends Filter {
    public DimensionsFilter(MffContext context, String name) {
        super(context, name);
    }

    public Signature getSignature() {
        return new Signature().addInputPort("frame", 2, FrameType.any()).addOutputPort("dimensions", 2, FrameType.array(Integer.TYPE)).disallowOtherPorts();
    }

    protected void onProcess() {
        Frame inFrame = getConnectedInputPort("frame").pullFrame();
        OutputPort outPort = getConnectedOutputPort("dimensions");
        FrameValue outFrame = outPort.fetchAvailableFrame(null).asFrameValue();
        outFrame.setValue(inFrame.getDimensions());
        outPort.pushFrame(outFrame);
    }
}
