package androidx.media.filterpacks.base;

import android.util.Log;
import androidx.media.filterfw.Frame;
import androidx.media.filterfw.FrameManager;
import androidx.media.filterfw.GraphRunner;
import androidx.media.filterfw.GraphRunner.Listener;
import androidx.media.filterfw.InputPort;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.OutputPort;
import java.util.HashMap;

public class DispatchFilter extends MetaFilter {
    private HashMap<String, Frame> mOutputFrames = null;
    private Listener mRunListener = new Listener() {
        public void onGraphRunnerStopped(GraphRunner runner) {
            synchronized (DispatchFilter.this.mState) {
                if (DispatchFilter.this.mState.state == 1) {
                    DispatchFilter.this.mState.state = 2;
                }
            }
        }

        public void onGraphRunnerError(Exception exception, boolean closedSuccessfully) {
            throw new RuntimeException("Error during dispatched run!", exception);
        }
    };
    private GraphRunner mRunner;

    public DispatchFilter(MffContext context, String name) {
        super(context, name);
    }

    protected void onPrepare() {
        this.mRunner = new GraphRunner(getContext());
        this.mRunner.setListener(this.mRunListener);
    }

    protected void onProcess() {
        synchronized (this.mState) {
            if (this.mState.state == 0) {
                pullInputs();
                processGraph();
            } else {
                ignoreInputs();
            }
            if (this.mState.state == 2) {
                pushOutputs();
            }
            if (this.mState.state == 1) {
                pushSavedOutputs();
            }
        }
    }

    protected void onClose() {
        super.onClose();
        this.mOutputFrames = null;
    }

    protected boolean schedulePolicy() {
        return inSchedulableState() && inputConditionsMet() && outputConditionsMet();
    }

    protected void pullInputs() {
        this.mInputFrames.clear();
        FrameManager fm = this.mRunner.getFrameManager();
        for (InputPort inputPort : getConnectedInputPorts()) {
            this.mInputFrames.put(inputPort.getName(), fm.importFrame(inputPort.pullFrame()));
        }
        assignInputs();
    }

    protected void assignInput(GraphInputSource source, Frame frame) {
        source.pushFrame(frame);
        frame.release();
    }

    protected void pushOutput(Frame frame, OutputPort outputPort) {
        Frame imported = getFrameManager().importFrame(frame);
        outputPort.pushFrame(imported);
        saveOutput(outputPort.getName(), imported);
    }

    protected void processGraph() {
        boolean haveOutputs = true;
        this.mState.state = 1;
        if (this.mOutputFrames == null) {
            haveOutputs = false;
        }
        this.mCurrentGraph.attachToRunner(this.mRunner);
        this.mRunner.start(this.mCurrentGraph);
        if (!haveOutputs) {
            this.mRunner.waitUntilStop();
            this.mState.state = 2;
        }
    }

    private void ignoreInputs() {
        for (InputPort inputPort : getConnectedInputPorts()) {
            inputPort.pullFrame();
        }
    }

    private void saveOutput(String name, Frame frame) {
        if (this.mOutputFrames == null) {
            this.mOutputFrames = new HashMap();
        }
        Frame oldFrame = (Frame) this.mOutputFrames.get(name);
        if (oldFrame != null) {
            oldFrame.release();
        }
        this.mOutputFrames.put(name, frame.retain());
    }

    private void pushSavedOutputs() {
        for (OutputPort outputPort : getConnectedOutputPorts()) {
            Frame frame = (Frame) this.mOutputFrames.get(outputPort.getName());
            if (frame != null) {
                outputPort.pushFrame(frame);
            } else {
                String valueOf = String.valueOf(outputPort);
                Log.w("DF", new StringBuilder(String.valueOf(valueOf).length() + 30).append("No output frame produced for ").append(valueOf).append("!").toString());
            }
        }
    }
}
