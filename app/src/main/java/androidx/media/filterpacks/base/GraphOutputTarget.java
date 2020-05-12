package androidx.media.filterpacks.base;

import androidx.media.filterfw.Filter;
import androidx.media.filterfw.Frame;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.Signature;

public class GraphOutputTarget extends Filter {
    private Frame mFrame = null;
    private FrameType mType = FrameType.any();

    public GraphOutputTarget(MffContext context, String name) {
        super(context, name);
    }

    public void setType(FrameType type) {
        this.mType = type;
    }

    public FrameType getType() {
        return this.mType;
    }

    public Signature getSignature() {
        return new Signature().addInputPort("frame", 2, this.mType).disallowOtherInputs();
    }

    public Frame pullFrame() {
        if (this.mFrame == null) {
            return null;
        }
        Frame result = this.mFrame;
        this.mFrame = null;
        return result;
    }

    protected void onProcess() {
        Frame frame = getConnectedInputPort("frame").pullFrame();
        if (this.mFrame != null) {
            this.mFrame.release();
        }
        this.mFrame = frame.retain();
    }

    protected boolean canSchedule() {
        return super.canSchedule() && this.mFrame == null;
    }
}
