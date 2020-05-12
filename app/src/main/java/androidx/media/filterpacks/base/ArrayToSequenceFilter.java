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

public final class ArrayToSequenceFilter extends Filter {
    Queue<Object> mValues = new LinkedList();

    public ArrayToSequenceFilter(MffContext context, String name) {
        super(context, name);
    }

    public Signature getSignature() {
        return new Signature().addInputPort("inputArray", 2, FrameType.array()).addOutputPort("outputSequence", 2, FrameType.single()).addOutputPort("remainingElements", 1, FrameType.single(Integer.TYPE)).disallowOtherPorts();
    }

    protected void onProcess() {
        InputPort inputPort = getConnectedInputPort("inputArray");
        if (this.mValues.size() == 0) {
            Object newValues = inputPort.pullFrame().asFrameValues().getValues();
            for (int i = 0; i < Array.getLength(newValues); i++) {
                this.mValues.add(Array.get(newValues, i));
            }
        }
        OutputPort outPortNum = getConnectedOutputPort("remainingElements");
        if (outPortNum != null) {
            FrameValue outFrameNum = outPortNum.fetchAvailableFrame(null).asFrameValue();
            outFrameNum.setValue(Integer.valueOf(this.mValues.size()));
            outPortNum.pushFrame(outFrameNum);
        }
        if (this.mValues.size() > 0) {
            OutputPort outPortSeq = getConnectedOutputPort("outputSequence");
            FrameValue outFrameSeq = outPortSeq.fetchAvailableFrame(null).asFrameValue();
            outFrameSeq.setValue(this.mValues.remove());
            outPortSeq.pushFrame(outFrameSeq);
        }
        if (this.mValues.size() == 0) {
            inputPort.setWaitsForFrame(true);
            setMinimumAvailableInputs(1);
            return;
        }
        inputPort.setWaitsForFrame(false);
        setMinimumAvailableInputs(0);
    }
}
