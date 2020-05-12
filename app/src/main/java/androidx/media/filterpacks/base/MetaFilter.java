package androidx.media.filterpacks.base;

import androidx.media.filterfw.Filter;
import androidx.media.filterfw.FilterGraph;
import androidx.media.filterfw.Frame;
import androidx.media.filterfw.GraphRunner;
import androidx.media.filterfw.GraphRunner.SubListener;
import androidx.media.filterfw.InputPort;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.OutputPort;
import java.util.HashMap;
import java.util.Map.Entry;

public class MetaFilter extends Filter implements SubListener {
    private static final boolean DEBUG = false;
    protected FilterGraph mCurrentGraph;
    protected FilterGraphProvider mGraphProvider;
    protected HashMap<String, Frame> mInputFrames = new HashMap();
    protected State mState = new State();

    public interface FilterGraphProvider {
        FilterGraph getFilterGraph(HashMap<String, Frame> hashMap);
    }

    protected static class State {
        public static final int PROCESSING = 1;
        public static final int PULLINPUT = 0;
        public static final int PUSHOUTPUT = 2;
        public int state = 0;

        protected State() {
        }
    }

    protected static class DefaultGraphProvider implements FilterGraphProvider {
        private FilterGraph mGraph;

        public DefaultGraphProvider(FilterGraph graph) {
            this.mGraph = graph;
        }

        public FilterGraph getFilterGraph(HashMap<String, Frame> hashMap) {
            return this.mGraph;
        }
    }

    public MetaFilter(MffContext context, String name) {
        super(context, name);
    }

    public void setGraph(FilterGraph graph) {
        if (isRunning()) {
            throw new IllegalStateException("Cannot set FilterGraphProvider while MetaFilter is running!");
        }
        this.mGraphProvider = new DefaultGraphProvider(graph);
    }

    public void setGraphProvider(FilterGraphProvider provider) {
        if (isRunning()) {
            throw new IllegalStateException("Cannot set FilterGraphProvider while MetaFilter is running!");
        }
        this.mGraphProvider = provider;
    }

    protected void onOpen() {
        this.mState.state = 0;
    }

    protected void onProcess() {
        if (this.mState.state == 0) {
            pullInputs();
            processGraph();
        } else if (this.mState.state == 2) {
            pushOutputs();
        }
    }

    protected void onClose() {
        this.mState.state = 0;
    }

    protected boolean canSchedule() {
        return schedulePolicy();
    }

    protected boolean schedulePolicy() {
        return inSchedulableState() && ((inputConditionsMet() || this.mState.state == 2) && outputConditionsMet());
    }

    protected void pushOutputs() {
        for (OutputPort outputPort : getConnectedOutputPorts()) {
            Frame frame = this.mCurrentGraph.getGraphOutput(outputPort.getName()).pullFrame();
            if (frame != null) {
                pushOutput(frame, outputPort);
                frame.release();
            }
        }
        this.mState.state = 0;
    }

    protected void pushOutput(Frame frame, OutputPort outputPort) {
        outputPort.pushFrame(frame);
    }

    protected void processGraph() {
        this.mState.state = 1;
        GraphRunner.current().enterSubGraph(this.mCurrentGraph, this);
    }

    protected void pullInputs() {
        this.mInputFrames.clear();
        for (InputPort inputPort : getConnectedInputPorts()) {
            this.mInputFrames.put(inputPort.getName(), inputPort.pullFrame());
        }
        assignInputs();
    }

    protected void assignInputs() {
        this.mCurrentGraph = this.mGraphProvider.getFilterGraph(this.mInputFrames);
        for (Entry<String, Frame> entry : this.mInputFrames.entrySet()) {
            assignInput(this.mCurrentGraph.getGraphInput((String) entry.getKey()), (Frame) entry.getValue());
        }
    }

    protected void assignInput(GraphInputSource source, Frame frame) {
        source.pushFrame(frame);
    }

    public void onSubGraphRunEnded(GraphRunner runner) {
        if (this.mState.state == 1) {
            this.mState.state = 2;
        }
    }
}
