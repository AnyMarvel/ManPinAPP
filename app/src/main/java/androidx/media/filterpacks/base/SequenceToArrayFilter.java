package androidx.media.filterpacks.base;

import androidx.media.filterfw.Filter;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.FrameValue;
import androidx.media.filterfw.InputPort;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.OutputPort;
import androidx.media.filterfw.Signature;
import java.lang.reflect.Array;
import java.util.LinkedList;
import java.util.Queue;

public final class SequenceToArrayFilter extends Filter {
    private int mNumRemainingElements;
    private int mStage = 1;
    Queue<Object> mValues = new LinkedList();

    public SequenceToArrayFilter(MffContext context, String name) {
        super(context, name);
    }

    public Signature getSignature() {
        return new Signature().addInputPort("inputSequence", 2, FrameType.single()).addInputPort("remainingElements", 2, FrameType.single(Integer.TYPE)).addOutputPort("outputArray", 2, FrameType.array()).disallowOtherPorts();
    }

    public void onInputPortAttached(InputPort port) {
        if (port.getName().equals("remainingElements")) {
            port.setWaitsForFrame(true);
        } else if (port.getName().equals("inputSequence")) {
            port.setWaitsForFrame(false);
        }
    }

    protected void onProcess() {
        InputPort controlPort = getConnectedInputPort("remainingElements");
        InputPort inputPort = getConnectedInputPort("inputSequence");
        OutputPort outPort = getConnectedOutputPort("outputArray");
        boolean canPush = false;
        Object array = null;
        switch (this.mStage) {
            case 1:
                if (controlPort.hasFrame()) {
                    this.mNumRemainingElements = ((Integer) controlPort.pullFrame().asFrameValue().getValue()).intValue();
                    if (this.mNumRemainingElements <= 0) {
                        array = Array.newInstance(retrieveClasstypeFromReceivingPort(outPort), 0);
                        canPush = true;
                        break;
                    }
                    controlPort.setWaitsForFrame(false);
                    inputPort.setWaitsForFrame(true);
                    this.mStage = 2;
                    break;
                }
                throw new RuntimeException("SequenceToArray expected frame on port remainingElements, but no frame is available!");
            case 2:
                if (inputPort.hasFrame()) {
                    this.mValues.add(inputPort.pullFrame().asFrameValue().getValue());
                    controlPort.setWaitsForFrame(true);
                    inputPort.setWaitsForFrame(false);
                    this.mStage = 1;
                    if (this.mNumRemainingElements == 1) {
                        array = Array.newInstance(retrieveClasstypeFromReceivingPort(outPort), this.mValues.size());
                        for (int i = 0; i < Array.getLength(array); i++) {
                            Array.set(array, i, this.mValues.remove());
                        }
                        canPush = true;
                        break;
                    }
                }
                throw new RuntimeException("SequenceToArray expected frame on port inputSequence, but no frame is available!");
            default:
                throw new RuntimeException("SequenceToArray: invalid state!");
        }
        if (canPush) {
            FrameValue outFrame = outPort.fetchAvailableFrame(null).asFrameValues();
            outFrame.setValue(array);
            outPort.pushFrame(outFrame);
            this.mValues.clear();
        }
    }

    private Class<?> retrieveClasstypeFromReceivingPort(OutputPort port) {
        return port.getTarget().getType().getContentClass();
    }
}
