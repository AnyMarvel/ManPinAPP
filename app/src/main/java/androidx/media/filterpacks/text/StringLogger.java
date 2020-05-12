package androidx.media.filterpacks.text;

import android.util.Log;
import androidx.media.filterfw.Filter;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.Signature;

public class StringLogger extends Filter {
    public StringLogger(MffContext context, String name) {
        super(context, name);
    }

    public Signature getSignature() {
        return new Signature().addInputPort("string", 2, FrameType.single(String.class)).disallowOtherPorts();
    }

    protected void onProcess() {
        Log.i("StringLogger", (String) getConnectedInputPort("string").pullFrame().asFrameValue().getValue());
    }
}
