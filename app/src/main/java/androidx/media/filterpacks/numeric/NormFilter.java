package androidx.media.filterpacks.numeric;

import android.util.Log;
import androidx.media.filterfw.Filter;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.FrameValue;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.OutputPort;
import androidx.media.filterfw.Signature;

public final class NormFilter extends Filter {
    private static final String TAG = "NormFilter";
    private static boolean mLogVerbose = Log.isLoggable(TAG, 2);

    public NormFilter(MffContext context, String name) {
        super(context, name);
    }

    public Signature getSignature() {
        FrameType floatT = FrameType.single(Float.TYPE);
        return new Signature().addInputPort("x", 2, floatT).addInputPort("y", 2, floatT).addOutputPort("norm", 2, floatT).disallowOtherPorts();
    }

    protected void onProcess() {
        float norm = (float) Math.hypot((double) ((Float) getConnectedInputPort("x").pullFrame().asFrameValue().getValue()).floatValue(), (double) ((Float) getConnectedInputPort("y").pullFrame().asFrameValue().getValue()).floatValue());
        if (mLogVerbose) {
            Log.v(TAG, "Norm = " + norm);
        }
        OutputPort outPort = getConnectedOutputPort("norm");
        FrameValue outFrame = outPort.fetchAvailableFrame(null).asFrameValue();
        outFrame.setValue(Float.valueOf(norm));
        outPort.pushFrame(outFrame);
    }
}
