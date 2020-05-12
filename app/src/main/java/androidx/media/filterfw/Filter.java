package androidx.media.filterfw;

import android.os.SystemClock;
import androidx.media.filterfw.FrameQueue.Builder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class Filter {
    public static final int PRIORITY_HIGH = 75;
    public static final int PRIORITY_LOW = 25;
    public static final int PRIORITY_NORMAL = 50;
    private static final int REQUEST_FLAG_CLOSE = 1;
    private static final int REQUEST_FLAG_NONE = 0;
    private ArrayList<Frame> mAutoReleaseFrames = new ArrayList();
    private InputPort[] mConnectedInputPortArray = null;
    private HashMap<String, InputPort> mConnectedInputPorts = new HashMap();
    private OutputPort[] mConnectedOutputPortArray = null;
    private HashMap<String, OutputPort> mConnectedOutputPorts = new HashMap();
    private MffContext mContext;
    private long mCurrentTimestamp = -1;
    private FilterGraph mFilterGraph;
    private boolean mIsActive = true;
    private AtomicBoolean mIsSleeping = new AtomicBoolean(false);
    private long mLastScheduleTime = 0;
    private int mMinimumAvailableInputs = 1;
    private int mMinimumAvailableOutputs = 1;
    private String mName;
    private int mRequests = 0;
    private int mScheduleCount = 0;
    private State mState = new State();

    private static class State {
        private static final int STATE_CLOSED = 4;
        private static final int STATE_DESTROYED = 5;
        private static final int STATE_OPEN = 3;
        private static final int STATE_PREPARED = 2;
        private static final int STATE_UNPREPARED = 1;
        public int current;

        private State() {
            this.current = 1;
        }

        public synchronized boolean check(int state) {
            return this.current == state;
        }
    }

    protected abstract void onProcess();

    protected Filter(MffContext context, String name) {
        this.mName = name;
        this.mContext = context;
    }

    public static final boolean isAvailable(String filterClassName) {
        return FilterFactory.sharedFactory().isFilterAvailable(filterClassName);
    }

    public String getName() {
        return this.mName;
    }

    public Signature getSignature() {
        return new Signature();
    }

    public MffContext getContext() {
        return this.mContext;
    }

    public boolean isActive() {
        return this.mIsActive;
    }

    public void activate() {
        assertIsPaused();
        if (!this.mIsActive) {
            this.mIsActive = true;
        }
    }

    public void deactivate() {
        assertIsPaused();
        if (this.mIsActive) {
            this.mIsActive = false;
        }
    }

    public final InputPort[] getConnectedInputPorts() {
        return this.mConnectedInputPortArray;
    }

    public final OutputPort[] getConnectedOutputPorts() {
        return this.mConnectedOutputPortArray;
    }

    public final InputPort getConnectedInputPort(String name) {
        return (InputPort) this.mConnectedInputPorts.get(name);
    }

    public final OutputPort getConnectedOutputPort(String name) {
        return (OutputPort) this.mConnectedOutputPorts.get(name);
    }

    protected void onInputPortAttached(InputPort port) {
    }

    protected void onOutputPortAttached(OutputPort port) {
    }

    protected void onInputPortOpen(InputPort port) {
    }

    protected void onOutputPortOpen(OutputPort port) {
    }

    public final boolean isOpen() {
        return this.mState.check(3);
    }

    public String toString() {
        String str = this.mName;
        String simpleName = getClass().getSimpleName();
        return new StringBuilder((String.valueOf(str).length() + 3) + String.valueOf(simpleName).length()).append(str).append(" (").append(simpleName).append(")").toString();
    }

    protected void onPrepare() {
    }

    protected void onOpen() {
    }

    protected void onClose() {
    }

    protected void onTearDown() {
    }

    protected boolean inputConditionsMet() {
        if (this.mConnectedInputPortArray.length > 0) {
            int inputFrames = 0;
            for (int i = 0; i < this.mConnectedInputPortArray.length; i++) {
                if (!this.mConnectedInputPortArray[i].conditionsMet()) {
                    return false;
                }
                if (this.mConnectedInputPortArray[i].hasFrame()) {
                    inputFrames++;
                }
            }
            if (inputFrames < this.mMinimumAvailableInputs) {
                return false;
            }
        }
        return true;
    }

    protected boolean outputConditionsMet() {
        if (this.mConnectedOutputPortArray.length > 0) {
            int availableOutputs = 0;
            for (int i = 0; i < this.mConnectedOutputPortArray.length; i++) {
                if (!this.mConnectedOutputPortArray[i].conditionsMet()) {
                    return false;
                }
                if (this.mConnectedOutputPortArray[i].isAvailable()) {
                    availableOutputs++;
                }
            }
            if (availableOutputs < this.mMinimumAvailableOutputs) {
                return false;
            }
        }
        return true;
    }

    protected boolean inSchedulableState() {
        return this.mIsActive && !this.mState.check(4);
    }

    protected boolean canSchedule() {
        return inSchedulableState() && inputConditionsMet() && outputConditionsMet();
    }

    public int getSchedulePriority() {
        return 50;
    }

    protected final FrameManager getFrameManager() {
        return this.mFilterGraph.mRunner != null ? this.mFilterGraph.mRunner.getFrameManager() : null;
    }

    protected final boolean isRunning() {
        return (this.mFilterGraph == null || this.mFilterGraph.mRunner == null || !this.mFilterGraph.mRunner.isRunning()) ? false : true;
    }

    protected final boolean performPreparation(Runnable runnable) {
        boolean z;
        synchronized (this.mState) {
            if (this.mState.current == 3) {
                z = false;
            } else {
                runnable.run();
                z = true;
            }
        }
        return z;
    }

    protected final void requestClose() {
        this.mRequests |= 1;
    }

    protected final void setMinimumAvailableInputs(int count) {
        this.mMinimumAvailableInputs = count;
    }

    protected final int getMinimumAvailableInputs() {
        return this.mMinimumAvailableInputs;
    }

    protected final void setMinimumAvailableOutputs(int count) {
        this.mMinimumAvailableOutputs = count;
    }

    protected final int getMinimumAvailableOutputs() {
        return this.mMinimumAvailableOutputs;
    }

    protected final void enterSleepState() {
        this.mIsSleeping.set(true);
    }

    protected final void wakeUp() {
        if (this.mIsSleeping.getAndSet(false) && isRunning()) {
            this.mFilterGraph.mRunner.signalWakeUp();
        }
    }

    protected final boolean isOpenGLSupported() {
        return this.mFilterGraph.mRunner.isOpenGLSupported();
    }

    public void connect(String outputName, Filter targetFilter, String inputName) {
        String valueOf;
        if (getConnectedOutputPort(outputName) != null) {
            valueOf = String.valueOf(this);
            throw new RuntimeException(new StringBuilder((String.valueOf(outputName).length() + 67) + String.valueOf(valueOf).length()).append("Attempting to connect already connected output port '").append(outputName).append("' of filter ").append(valueOf).append("'!").toString());
        } else if (targetFilter.getConnectedInputPort(inputName) != null) {
            valueOf = String.valueOf(targetFilter);
            throw new RuntimeException(new StringBuilder((String.valueOf(inputName).length() + 66) + String.valueOf(valueOf).length()).append("Attempting to connect already connected input port '").append(inputName).append("' of filter ").append(valueOf).append("'!").toString());
        } else {
            InputPort inputPort = targetFilter.newInputPort(inputName);
            OutputPort outputPort = newOutputPort(outputName);
            outputPort.setTarget(inputPort);
            inputPort.setSourceHint(outputPort);
            targetFilter.onInputPortAttached(inputPort);
            onOutputPortAttached(outputPort);
            updatePortArrays();
        }
    }

    final Map<String, InputPort> getConnectedInputPortMap() {
        return this.mConnectedInputPorts;
    }

    final Map<String, OutputPort> getConnectedOutputPortMap() {
        return this.mConnectedOutputPorts;
    }

    final void prepareOnly() {
        synchronized (this.mState) {
            if (this.mState.current == 1) {
                onPrepare();
                this.mState.current = 2;
            }
        }
    }

    final void execute() {
        synchronized (this.mState) {
            autoPullInputs();
            this.mLastScheduleTime = SystemClock.elapsedRealtime();
            if (this.mState.current == 1) {
                onPrepare();
                this.mState.current = 2;
            }
            if (this.mState.current == 2) {
                openPorts();
                onOpen();
                this.mState.current = 3;
            }
            if (this.mState.current == 3) {
                onProcess();
                if (this.mRequests != 0) {
                    processRequests();
                }
            }
        }
        autoReleaseFrames();
        this.mScheduleCount++;
    }

    final void performClose() {
        synchronized (this.mState) {
            if (this.mState.current == 3) {
                onClose();
                this.mIsSleeping.set(false);
                this.mState.current = 4;
                this.mCurrentTimestamp = -1;
            }
        }
    }

    final void softReset() {
        synchronized (this.mState) {
            performClose();
            if (this.mState.current == 4) {
                this.mState.current = 2;
            }
        }
    }

    final void performTearDown() {
        synchronized (this.mState) {
            if (this.mState.current == 3) {
                String valueOf = String.valueOf(this);
                throw new RuntimeException(new StringBuilder(String.valueOf(valueOf).length() + 58).append("Attempting to tear-down filter ").append(valueOf).append(" which is in an open state!").toString());
            }
            if (!(this.mState.current == 5 || this.mState.current == 1)) {
                onTearDown();
                this.mState.current = 5;
            }
        }
    }

    final void insertIntoFilterGraph(FilterGraph graph) {
        this.mFilterGraph = graph;
        updatePortArrays();
    }

    final int getScheduleCount() {
        return this.mScheduleCount;
    }

    final void resetScheduleCount() {
        this.mScheduleCount = 0;
    }

    final void openPorts() {
        for (OutputPort outputPort : this.mConnectedOutputPorts.values()) {
            openOutputPort(outputPort);
        }
    }

    final void addAutoReleaseFrame(Frame frame) {
        this.mAutoReleaseFrames.add(frame);
    }

    final long getCurrentTimestamp() {
        return this.mCurrentTimestamp;
    }

    final void onPulledFrameWithTimestamp(long timestamp) {
        if (timestamp > this.mCurrentTimestamp || this.mCurrentTimestamp == -1) {
            this.mCurrentTimestamp = timestamp;
        }
    }

    final void openOutputPort(OutputPort outPort) {
        if (outPort.getQueue() == null) {
            String name;
            try {
                Builder builder = new Builder();
                InputPort inPort = outPort.getTarget();
                outPort.onOpen(builder);
                inPort.onOpen(builder);
                Filter targetFilter = inPort.getFilter();
                String str = this.mName;
                name = outPort.getName();
                String str2 = targetFilter.mName;
                String name2 = inPort.getName();
                FrameQueue queue = builder.build(new StringBuilder((((String.valueOf(str).length() + 8) + String.valueOf(name).length()) + String.valueOf(str2).length()) + String.valueOf(name2).length()).append(str).append("[").append(name).append("] -> ").append(str2).append("[").append(name2).append("]").toString());
                outPort.setQueue(queue);
                inPort.setQueue(queue);
            } catch (RuntimeException e) {
                name = String.valueOf(outPort);
                throw new RuntimeException(new StringBuilder(String.valueOf(name).length() + 28).append("Could not open output port ").append(name).append("!").toString(), e);
            }
        }
    }

    final boolean isSleeping() {
        return this.mIsSleeping.get();
    }

    final long getLastScheduleTime() {
        return this.mLastScheduleTime;
    }

    private final void autoPullInputs() {
        for (int i = 0; i < this.mConnectedInputPortArray.length; i++) {
            InputPort port = this.mConnectedInputPortArray[i];
            if (port.hasFrame() && port.isAutoPullEnabled()) {
                this.mConnectedInputPortArray[i].pullFrame();
            }
        }
    }

    private final void autoReleaseFrames() {
        for (int i = 0; i < this.mAutoReleaseFrames.size(); i++) {
            ((Frame) this.mAutoReleaseFrames.get(i)).release();
        }
        this.mAutoReleaseFrames.clear();
    }

    private final InputPort newInputPort(String name) {
        InputPort result = (InputPort) this.mConnectedInputPorts.get(name);
        if (result != null) {
            return result;
        }
        result = new InputPort(this, name, getSignature().getInputPortInfo(name));
        this.mConnectedInputPorts.put(name, result);
        return result;
    }

    private final OutputPort newOutputPort(String name) {
        OutputPort result = (OutputPort) this.mConnectedOutputPorts.get(name);
        if (result != null) {
            return result;
        }
        result = new OutputPort(this, name, getSignature().getOutputPortInfo(name));
        this.mConnectedOutputPorts.put(name, result);
        return result;
    }

    private final void processRequests() {
        if ((this.mRequests & 1) != 0) {
            performClose();
            this.mRequests = 0;
        }
    }

    private void assertIsPaused() {
        GraphRunner runner = GraphRunner.current();
        if (runner != null && !runner.isPaused() && !runner.isStopped()) {
            throw new RuntimeException("Attempting to modify filter state while runner is executing. Please pause or stop the runner first!");
        }
    }

    private final void updatePortArrays() {
        this.mConnectedInputPortArray = (InputPort[]) this.mConnectedInputPorts.values().toArray(new InputPort[0]);
        this.mConnectedOutputPortArray = (OutputPort[]) this.mConnectedOutputPorts.values().toArray(new OutputPort[0]);
    }
}
