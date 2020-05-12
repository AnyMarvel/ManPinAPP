package androidx.media.filterpacks.numeric;

import android.os.SystemClock;
import androidx.media.filterfw.Filter;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.FrameValue;
import androidx.media.filterfw.InputPort;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.OutputPort;
import androidx.media.filterfw.Signature;

public final class WaveSource extends Filter {
    public static final int WAVESOURCE_CONST = 0;
    public static final int WAVESOURCE_COS = 2;
    public static final int WAVESOURCE_SAWTOOTH = 3;
    public static final int WAVESOURCE_SIN = 1;
    private float mAmplitude = 1.0f;
    private int mMode = 1;
    private float mSpeed = 0.01f;
    private float mXOffset = 0.0f;
    private float mYOffset = 0.0f;

    public WaveSource(MffContext context, String name) {
        super(context, name);
    }

    public Signature getSignature() {
        return new Signature().addInputPort("speed", 1, FrameType.single(Float.TYPE)).addInputPort("amplitude", 1, FrameType.single(Float.TYPE)).addInputPort("xOffset", 1, FrameType.single(Float.TYPE)).addInputPort("yOffset", 1, FrameType.single(Float.TYPE)).addInputPort("mode", 1, FrameType.single(Integer.TYPE)).addOutputPort("value", 2, FrameType.single()).disallowOtherPorts();
    }

    public void onInputPortOpen(InputPort port) {
        if (port.getName().equals("speed")) {
            port.bindToFieldNamed("mSpeed");
            port.setAutoPullEnabled(true);
        } else if (port.getName().equals("amplitude")) {
            port.bindToFieldNamed("mAmplitude");
            port.setAutoPullEnabled(true);
        } else if (port.getName().equals("xOffset")) {
            port.bindToFieldNamed("mXOffset");
            port.setAutoPullEnabled(true);
        } else if (port.getName().equals("yOffset")) {
            port.bindToFieldNamed("mYOffset");
            port.setAutoPullEnabled(true);
        } else if (port.getName().equals("mode")) {
            port.bindToFieldNamed("mMode");
            port.setAutoPullEnabled(true);
        }
    }

    protected synchronized void onProcess() {
        float result;
        OutputPort outputPort = getConnectedOutputPort("value");
        FrameValue frame = outputPort.fetchAvailableFrame(null).asFrameValue();
        long t = SystemClock.elapsedRealtime();
        switch (this.mMode) {
            case 0:
                result = this.mYOffset;
                break;
            case 1:
                result = (((float) Math.sin((double) (this.mXOffset + (((float) t) * this.mSpeed)))) * this.mAmplitude) + this.mYOffset;
                break;
            case 2:
                result = (((float) Math.cos((double) (this.mXOffset + (((float) t) * this.mSpeed)))) * this.mAmplitude) + this.mYOffset;
                break;
            case 3:
                result = (((this.mXOffset + (((float) t) * this.mSpeed)) % 1.0f) * this.mAmplitude) + this.mYOffset;
                break;
            default:
                result = this.mYOffset;
                break;
        }
        frame.setValue(Float.valueOf(result));
        outputPort.pushFrame(frame);
    }
}
