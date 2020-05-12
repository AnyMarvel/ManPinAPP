package androidx.media.filterfw;

public abstract class SlotFilter extends Filter {
    protected final String mSlotName;

    protected SlotFilter(MffContext context, String name, String slotName) {
        super(context, name);
        this.mSlotName = slotName;
    }

    protected final FrameType getSlotType() {
        return getFrameManager().getSlot(this.mSlotName).getType();
    }

    protected final boolean slotHasFrame() {
        return getFrameManager().getSlot(this.mSlotName).hasFrame();
    }
}
