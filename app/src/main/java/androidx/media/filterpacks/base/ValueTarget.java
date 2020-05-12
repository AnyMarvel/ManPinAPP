package androidx.media.filterpacks.base;

import android.os.Handler;
import android.os.Looper;
import androidx.media.filterfw.Filter;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.FrameValue;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.Signature;

public final class ValueTarget extends Filter {
    private Handler mHandler = null;
    private ValueListener mListener = null;

    public interface ValueListener {
        void onReceivedValue(Object obj);
    }

    public ValueTarget(MffContext context, String name) {
        super(context, name);
    }

    public void setListener(ValueListener listener, boolean useLooper) {
        if (isRunning()) {
            throw new IllegalStateException("Attempting to bind filter to callback while it is running!");
        }
        this.mListener = listener;
        if (useLooper) {
            this.mHandler = new Handler(Looper.myLooper() != null ? Looper.myLooper() : Looper.getMainLooper());
        }
    }

    public Signature getSignature() {
        return new Signature().addInputPort("value", 2, FrameType.single()).disallowOtherPorts();
    }

    protected void onProcess() {
        FrameValue valueFrame = getConnectedInputPort("value").pullFrame().asFrameValue();
        if (this.mListener == null) {
            return;
        }
        if (this.mHandler != null) {
            postValueToHandler(valueFrame.getValue());
        } else {
            this.mListener.onReceivedValue(valueFrame.getValue());
        }
    }

    private void postValueToHandler(final Object value) {
        this.mHandler.post(new Runnable() {
            public void run() {
                ValueTarget.this.mListener.onReceivedValue(value);
            }
        });
    }
}
