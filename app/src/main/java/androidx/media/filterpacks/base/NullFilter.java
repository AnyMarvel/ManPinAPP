package androidx.media.filterpacks.base;

import androidx.media.filterfw.Filter;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.Signature;

public final class NullFilter extends Filter {
    public NullFilter(MffContext context, String name) {
        super(context, name);
    }

    public Signature getSignature() {
        return new Signature().addInputPort("input", 2, FrameType.any()).disallowOtherInputs();
    }

    protected void onProcess() {
        getConnectedInputPort("input").pullFrame();
    }
}
