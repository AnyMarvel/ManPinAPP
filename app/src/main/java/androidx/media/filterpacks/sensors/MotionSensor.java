package androidx.media.filterpacks.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import androidx.media.filterfw.Filter;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.FrameValues;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.OutputPort;
import androidx.media.filterfw.Signature;

public final class MotionSensor extends Filter implements SensorEventListener {
    private Sensor mSensor = null;
    private SensorManager mSensorManager = null;
    private float[] mValues = new float[3];

    public MotionSensor(MffContext context, String name) {
        super(context, name);
    }

    public Signature getSignature() {
        return new Signature().addOutputPort("values", 2, FrameType.array(Float.TYPE)).disallowOtherPorts();
    }

    protected void onPrepare() {
        this.mSensorManager = (SensorManager) getContext().getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        this.mSensor = this.mSensorManager.getDefaultSensor(10);
        this.mSensorManager.registerListener(this, this.mSensor, 2);
    }

    protected void onTearDown() {
        this.mSensorManager.unregisterListener(this);
    }

    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public final void onSensorChanged(SensorEvent event) {
        synchronized (this.mValues) {
            this.mValues[0] = event.values[0];
            this.mValues[1] = event.values[1];
            this.mValues[2] = event.values[2];
        }
    }

    protected void onProcess() {
        OutputPort outPort = getConnectedOutputPort("values");
        FrameValues outFrame = outPort.fetchAvailableFrame(null).asFrameValues();
        synchronized (this.mValues) {
            outFrame.setValues(this.mValues);
        }
        outFrame.setTimestamp(System.currentTimeMillis() * 1000000);
        outPort.pushFrame(outFrame);
    }
}
