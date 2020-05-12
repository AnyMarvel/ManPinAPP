package androidx.media.filterpacks.base;

import androidx.media.filterfw.Filter;
import androidx.media.filterfw.Frame;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.Signature;

public class GraphInputSource extends Filter {
    private Frame mFrame = null;

    public GraphInputSource(MffContext context, String name) {
        super(context, name);
    }

    public Signature getSignature() {
        return new Signature().addOutputPort("frame", 2, FrameType.any()).disallowOtherInputs();
    }

    public void pushFrame(Frame frame) {
        if (this.mFrame != null) {
            this.mFrame.release();
        }
        if (frame == null) {
            throw new RuntimeException("Attempting to assign null-frame!");
        }
        this.mFrame = frame.retain();
    }

    protected void onProcess() {
        if (this.mFrame != null) {
            getConnectedOutputPort("frame").pushFrame(this.mFrame);
            this.mFrame.release();
            this.mFrame = null;
        }
    }

    protected void onTearDown() {
        if (this.mFrame != null) {
            this.mFrame.release();
            this.mFrame = null;
        }
    }

    protected boolean canSchedule() {
        return super.canSchedule() && this.mFrame != null;
    }
}
