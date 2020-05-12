package androidx.media.filterpacks.base;

import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.Signature;
import androidx.media.filterfw.SlotFilter;

public final class FrameSlotTarget extends SlotFilter {
    public FrameSlotTarget(MffContext context, String name, String slotName) {
        super(context, name, slotName);
    }

    public Signature getSignature() {
        return new Signature().addInputPort("frame", 2, FrameType.any()).disallowOtherPorts();
    }

    protected void onProcess() {
        getFrameManager().storeFrame(getConnectedInputPort("frame").pullFrame(), this.mSlotName);
    }
}
