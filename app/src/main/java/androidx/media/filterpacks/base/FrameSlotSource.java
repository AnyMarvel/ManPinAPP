package androidx.media.filterpacks.base;

import androidx.media.filterfw.Frame;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.Signature;
import androidx.media.filterfw.SlotFilter;

public final class FrameSlotSource extends SlotFilter {
    public FrameSlotSource(MffContext context, String name, String slotName) {
        super(context, name, slotName);
    }

    public Signature getSignature() {
        return new Signature().addOutputPort("frame", 2, FrameType.any()).disallowOtherPorts();
    }

    protected boolean canSchedule() {
        return super.canSchedule() && slotHasFrame();
    }

    protected void onProcess() {
        Frame frame = getFrameManager().fetchFrame(this.mSlotName);
        getConnectedOutputPort("frame").pushFrame(frame);
        frame.release();
    }
}
