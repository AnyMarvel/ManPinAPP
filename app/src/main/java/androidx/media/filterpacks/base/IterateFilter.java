package androidx.media.filterpacks.base;

import android.util.Log;
import androidx.media.filterfw.Frame;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.InputPort;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.OutputPort;
import androidx.media.filterfw.Signature;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map.Entry;

public class IterateFilter extends MetaFilter {
    int mIndex = 0;
    int mInputArraySize = 0;
    Object mInputsArray = null;
    HashMap<String, Object> mOutputs = new HashMap();

    public IterateFilter(MffContext context, String name) {
        super(context, name);
    }

    public Signature getSignature() {
        return new Signature().addInputPort("array", 2, FrameType.array());
    }

    protected void onProcess() {
        if (this.mState.state == 0) {
            this.mInputsArray = null;
            pullInputs();
            if (this.mInputArraySize > 0) {
                processGraph();
            } else {
                pushOutputs();
            }
        } else if (this.mState.state == 2) {
            assignOutputs();
            if (this.mIndex < this.mInputArraySize) {
                assignInputs();
                processGraph();
                return;
            }
            pushOutputs();
        }
    }

    protected void clearInputs() {
        for (Frame frame : this.mInputFrames.values()) {
            frame.release();
        }
        this.mInputFrames.clear();
    }

    protected void pullInputs() {
        clearInputs();
        for (InputPort inputPort : getConnectedInputPorts()) {
            this.mInputFrames.put(inputPort.getName(), inputPort.pullFrame().retain());
        }
        assignInputs();
    }

    protected void assignInputs() {
        this.mCurrentGraph = this.mGraphProvider.getFilterGraph(this.mInputFrames);
        for (Entry<String, Frame> entry : this.mInputFrames.entrySet()) {
            GraphInputSource source = null;
            Frame frame = null;
            boolean mustRelease = false;
            if (((String) entry.getKey()).equals("array")) {
                if (this.mInputsArray == null) {
                    initializeInputsAndOutputs(((Frame) entry.getValue()).asFrameValues().getValues());
                }
                if (this.mInputArraySize > 0) {
                    source = this.mCurrentGraph.getGraphInput("elem");
                    Object value = Array.get(this.mInputsArray, this.mIndex);
                    frame = Frame.create(FrameType.single(), null);
                    frame.asFrameValue().setValue(value);
                    mustRelease = true;
                }
            } else {
                source = this.mCurrentGraph.getGraphInput((String) entry.getKey());
                frame = (Frame) entry.getValue();
            }
            if (frame != null) {
                if (source == null) {
                    String str = (String) entry.getKey();
                    throw new RuntimeException(new StringBuilder(String.valueOf(str).length() + 67).append("Input port '").append(str).append("' could not be mapped to any input in the filter graph!").toString());
                }
                source.pushFrame(frame);
                if (mustRelease) {
                    frame.release();
                }
            }
        }
    }

    protected void assignOutputs() {
        for (OutputPort outputPort : getConnectedOutputPorts()) {
            String name = outputPort.getName();
            GraphOutputTarget target = this.mCurrentGraph.getGraphOutput(name);
            Frame frame = target.pullFrame();
            if (frame == null) {
                String name2 = target.getName();
                Log.w("IterateFilter", new StringBuilder(String.valueOf(name2).length() + 23).append("Output '").append(name2).append("' has no frame!").toString());
            } else {
                setOutputForPortAtIndex(frame.asFrameValue().getValue(), name, this.mIndex);
                frame.release();
            }
        }
        this.mIndex++;
    }

    protected void pushOutputs() {
        for (OutputPort outputPort : getConnectedOutputPorts()) {
            Object outputArrayForPort = this.mOutputs.get(outputPort.getName());
            if (outputArrayForPort != null) {
                Frame frame = Frame.create(FrameType.array(), new int[]{this.mInputArraySize});
                frame.asFrameValues().setValues(outputArrayForPort);
                outputPort.pushFrame(frame);
                frame.release();
            }
        }
        this.mState.state = 0;
    }

    private void initializeInputsAndOutputs(Object inputValuesArray) {
        this.mInputsArray = inputValuesArray;
        this.mIndex = 0;
        this.mInputArraySize = Array.getLength(this.mInputsArray);
        this.mOutputs.clear();
    }

    private void setOutputForPortAtIndex(Object outputValue, String outputPortName, int index) {
        Object outputArrayForPort = this.mOutputs.get(outputPortName);
        if (outputArrayForPort == null) {
            outputArrayForPort = Array.newInstance(outputValue.getClass(), this.mInputArraySize);
            this.mOutputs.put(outputPortName, outputArrayForPort);
        }
        Array.set(outputArrayForPort, index, outputValue);
    }
}
