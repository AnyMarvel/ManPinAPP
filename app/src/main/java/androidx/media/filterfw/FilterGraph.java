package androidx.media.filterfw;

import android.view.View;

import androidx.media.filterpacks.base.BranchFilter;
import androidx.media.filterpacks.base.FrameSlotSource;
import androidx.media.filterpacks.base.FrameSlotTarget;
import androidx.media.filterpacks.base.GraphInputSource;
import androidx.media.filterpacks.base.GraphOutputTarget;
import androidx.media.filterpacks.base.ValueTarget;
import androidx.media.filterpacks.base.ValueTarget.ValueListener;
import androidx.media.filterpacks.base.VariableSource;


import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

public final class FilterGraph {
    private static final boolean DEBUG = false;
    private Filter[] mAllFilters;
    private MffContext mContext;
    private HashMap<String, Filter> mFilterMap;
    private FilterGraph mParentGraph;
    GraphRunner mRunner;
    private final HashSet<FilterGraph> mSubGraphs;
    private final Object mSubGraphsTearDownLock;

    public static class Builder {
        private MffContext mContext;
        private HashMap<String, Filter> mFilterMap = new HashMap();

        public Builder(MffContext context) {
            this.mContext = context;
        }

        public void addFilter(Filter filter) {
            String valueOf;
            if (this.mFilterMap.values().contains(filter)) {
                valueOf = String.valueOf(filter);
                throw new IllegalArgumentException(new StringBuilder(String.valueOf(valueOf).length() + 55).append("Attempting to add filter ").append(valueOf).append(" that is in the graph already!").toString());
            } else if (this.mFilterMap.containsKey(filter.getName())) {
                valueOf = filter.getName();
                throw new IllegalArgumentException(new StringBuilder(String.valueOf(valueOf).length() + 43).append("Graph contains filter with name '").append(valueOf).append("' already!").toString());
            } else {
                this.mFilterMap.put(filter.getName(), filter);
            }
        }

        public VariableSource addVariable(String name, Object value) {
            if (getFilter(name) != null) {
                throw new IllegalArgumentException(new StringBuilder(String.valueOf(name).length() + 31).append("Filter named '").append(name).append("' exists already!").toString());
            }
            VariableSource valueSource = new VariableSource(this.mContext, name);
            addFilter(valueSource);
            if (value != null) {
                valueSource.setValue(value);
            }
            return valueSource;
        }

        public FrameSlotSource addFrameSlotSource(String name, String slotName) {
            FrameSlotSource filter = new FrameSlotSource(this.mContext, name, slotName);
            addFilter(filter);
            return filter;
        }

        public FrameSlotTarget addFrameSlotTarget(String name, String slotName) {
            FrameSlotTarget filter = new FrameSlotTarget(this.mContext, name, slotName);
            addFilter(filter);
            return filter;
        }

        public void connect(String sourceFilterName, String sourcePort, String targetFilterName, String targetPort) {
            Filter sourceFilter = getFilter(sourceFilterName);
            Filter targetFilter = getFilter(targetFilterName);
            if (sourceFilter == null) {
                throw new IllegalArgumentException(new StringBuilder(String.valueOf(sourceFilterName).length() + 18).append("Unknown filter '").append(sourceFilterName).append("'!").toString());
            } else if (targetFilter == null) {
                throw new IllegalArgumentException(new StringBuilder(String.valueOf(targetFilterName).length() + 18).append("Unknown filter '").append(targetFilterName).append("'!").toString());
            } else {
                connect(sourceFilter, sourcePort, targetFilter, targetPort);
            }
        }

        public void connect(Filter sourceFilter, String sourcePort, Filter targetFilter, String targetPort) {
            sourceFilter.connect(sourcePort, targetFilter, targetPort);
        }

        public Filter getFilter(String name) {
            return (Filter) this.mFilterMap.get(name);
        }

        public FilterGraph build() {
            checkSignatures();
            return buildWithParent(null);
        }

        public FilterGraph buildSubGraph(FilterGraph parentGraph) {
            if (parentGraph == null) {
                throw new NullPointerException("Parent graph must be non-null!");
            }
            checkSignatures();
            return buildWithParent(parentGraph);
        }

        VariableSource assignValueToFilterInput(Object value, String filterName, String inputName) {
            Filter filter = getFilter(filterName);
            if (filter == null) {
                throw new IllegalArgumentException(new StringBuilder(String.valueOf(filterName).length() + 18).append("Unknown filter '").append(filterName).append("'!").toString());
            }
            String valueSourceName = new StringBuilder((String.valueOf(filterName).length() + 1) + String.valueOf(inputName).length()).append(filterName).append(".").append(inputName).toString();
            if (getFilter(valueSourceName) != null) {
                throw new IllegalArgumentException(new StringBuilder((String.valueOf(filterName).length() + 50) + String.valueOf(inputName).length()).append("VariableSource for '").append(filterName).append("' and input '").append(inputName).append("' exists already!").toString());
            }
            VariableSource valueSource = new VariableSource(this.mContext, valueSourceName);
            addFilter(valueSource);
            try {
                valueSource.connect("value", filter, inputName);
                if (value != null) {
                    valueSource.setValue(value);
                }
                return valueSource;
            } catch (RuntimeException e) {
                throw new RuntimeException(new StringBuilder((String.valueOf(inputName).length() + 58) + String.valueOf(filterName).length()).append("Could not connect VariableSource to input '").append(inputName).append("' of filter '").append(filterName).append("'!").toString(), e);
            }
        }

        VariableSource assignVariableToFilterInput(String varName, String filterName, String inputName) {
            Filter filter = getFilter(filterName);
            if (filter == null) {
                throw new IllegalArgumentException(new StringBuilder(String.valueOf(filterName).length() + 18).append("Unknown filter '").append(filterName).append("'!").toString());
            }
            Filter variable = getFilter(varName);
            if (variable == null || !(variable instanceof VariableSource)) {
                throw new IllegalArgumentException(new StringBuilder(String.valueOf(varName).length() + 20).append("Unknown variable '").append(varName).append("'!").toString());
            }
            try {
                connectAndBranch(variable, "value", filter, inputName);
                return (VariableSource) variable;
            } catch (RuntimeException e) {
                throw new RuntimeException(new StringBuilder((String.valueOf(inputName).length() + 58) + String.valueOf(filterName).length()).append("Could not connect VariableSource to input '").append(inputName).append("' of filter '").append(filterName).append("'!").toString(), e);
            }
        }

        private FilterGraph buildWithParent(FilterGraph parent) {
            FilterGraph graph = new FilterGraph(this.mContext, parent);
            graph.mFilterMap = this.mFilterMap;
            graph.mAllFilters = (Filter[]) this.mFilterMap.values().toArray(new Filter[0]);
            for (Entry<String, Filter> filterEntry : this.mFilterMap.entrySet()) {
                ((Filter) filterEntry.getValue()).insertIntoFilterGraph(graph);
            }
            return graph;
        }

        private void checkSignatures() {
            FilterGraph.checkSignaturesForFilters(this.mFilterMap.values());
        }

        private void connectAndBranch(Filter sourceFilter, String sourcePort, Filter targetFilter, String targetPort) {
            String name = sourceFilter.getName();
            String branchName = new StringBuilder((String.valueOf(name).length() + 9) + String.valueOf(sourcePort).length()).append("__").append(name).append("_").append(sourcePort).append("Branch").toString();
            Filter branch = getFilter(branchName);
            if (branch == null) {
                branch = new BranchFilter(this.mContext, branchName, false);
                addFilter(branch);
                sourceFilter.connect(sourcePort, branch, "input");
            }
            name = targetFilter.getName();
            branch.connect(new StringBuilder((String.valueOf(name).length() + 3) + String.valueOf(targetPort).length()).append("to").append(name).append("_").append(targetPort).toString(), targetFilter, targetPort);
        }
    }

    public void attachToRunner(GraphRunner runner) {
        if (this.mRunner == null) {
            Iterator it = this.mSubGraphs.iterator();
            while (it.hasNext()) {
                ((FilterGraph) it.next()).attachToRunner(runner);
            }
            runner.attachGraph(this);
            this.mRunner = runner;
        } else if (this.mRunner != runner) {
            throw new RuntimeException("Cannot attach FilterGraph to GraphRunner that is already attached to another GraphRunner!");
        }
    }

    public void tearDown() {
        if (this.mParentGraph != null) {
            throw new RuntimeException("Attempting to tear down sub-graph!");
        }
        recursiveTearDown();
    }

    public boolean isSubGraph() {
        return this.mParentGraph != null;
    }

    public MffContext getContext() {
        return this.mContext;
    }

    public Filter getFilter(String name) {
        return (Filter) this.mFilterMap.get(name);
    }

    public <T extends Filter> Collection<T> getFiltersByType(Class<T> clazz) {
        Collection<T> filterList = new ArrayList();
        for (Entry<String, Filter> filterEntry : this.mFilterMap.entrySet()) {
            Filter filter = (Filter) filterEntry.getValue();
            if (filter.getClass() == clazz) {
                filterList.add((T) filter);
            }
        }
        return filterList;
    }

    public VariableSource getVariable(String name) {
        Filter result = (Filter) this.mFilterMap.get(name);
        if (result != null && (result instanceof VariableSource)) {
            return (VariableSource) result;
        }
        throw new IllegalArgumentException(new StringBuilder(String.valueOf(name).length() + 30).append("Unknown variable '").append(name).append("' specified!").toString());
    }

    public GraphOutputTarget getGraphOutput(String name) {
        Filter result = (Filter) this.mFilterMap.get(name);
        if (result != null && (result instanceof GraphOutputTarget)) {
            return (GraphOutputTarget) result;
        }
        throw new IllegalArgumentException(new StringBuilder(String.valueOf(name).length() + 28).append("Unknown target '").append(name).append("' specified!").toString());
    }

    public GraphInputSource getGraphInput(String name) {
        Filter result = (Filter) this.mFilterMap.get(name);
        if (result != null && (result instanceof GraphInputSource)) {
            return (GraphInputSource) result;
        }
        throw new IllegalArgumentException(new StringBuilder(String.valueOf(name).length() + 28).append("Unknown source '").append(name).append("' specified!").toString());
    }

    public void bindFilterToView(String filterName, View view) {
        Filter filter = (Filter) this.mFilterMap.get(filterName);
        if (filter == null || !(filter instanceof ViewFilter)) {
            throw new IllegalArgumentException(new StringBuilder(String.valueOf(filterName).length() + 23).append("Unknown view filter '").append(filterName).append("'!").toString());
        }
        ((ViewFilter) filter).bindToView(view);
    }

    public void bindValueTarget(String filterName, ValueListener listener, boolean onCallerThread) {
        Filter filter = (Filter) this.mFilterMap.get(filterName);
        if (filter == null || !(filter instanceof ValueTarget)) {
            throw new IllegalArgumentException(new StringBuilder(String.valueOf(filterName).length() + 30).append("Unknown ValueTarget filter '").append(filterName).append("'!").toString());
        }
        ((ValueTarget) filter).setListener(listener, onCallerThread);
    }

    public void dumpGraphState(PrintWriter writer) {
        for (Filter filter : this.mAllFilters) {
            String name;
            String valueOf = String.valueOf(filter);
            writer.println(new StringBuilder(String.valueOf(valueOf).length() + 8).append("Filter ").append(valueOf).append(":").toString());
            for (InputPort port : filter.getConnectedInputPorts()) {
                String source;
                String portState = port.hasFrame() ? "X" : " ";
                String comment = (!port.waitsForFrame() || port.hasFrame()) ? "" : " (BLOCKED)";
                OutputPort sourcePort = port.getSourceHint();
                if (sourcePort != null) {
                    name = sourcePort.getFilter().getName();
                    String name2 = sourcePort.getName();
                    source = new StringBuilder((String.valueOf(name).length() + 2) + String.valueOf(name2).length()).append(name).append("[").append(name2).append("]").toString();
                } else {
                    source = "<unknown source>";
                }
                name = port.getName();
                writer.println(new StringBuilder((((String.valueOf(source).length() + 13) + String.valueOf(portState).length()) + String.valueOf(name).length()) + String.valueOf(comment).length()).append("  IN: ").append(source).append(" =[").append(portState).append("]=> ").append(name).append(comment).toString());
            }
            for (OutputPort port2 : filter.getConnectedOutputPorts()) {
                String portState = port2.isAvailable() ? " " : "X";
                InputPort targetPort = port2.getTarget();
                name = targetPort.getFilter().getName();
                String name2 = targetPort.getName();
                String target = new StringBuilder((String.valueOf(name).length() + 2) + String.valueOf(name2).length()).append(name).append("[").append(name2).append("]").toString();
                String comment;
                if (!port2.waitsUntilAvailable() || port2.isAvailable()) {
                    comment = "";
                } else {
                    comment = " (BLOCKED)";
                }
                name = port2.getName();
                writer.println(new StringBuilder((((String.valueOf(name).length() + 14) + String.valueOf(portState).length()) + String.valueOf(target).length()) + String.valueOf(comment).length()).append("  OUT: ").append(name).append(" =[").append(portState).append("]=> ").append(target).append(comment).toString());
            }
        }
    }

    public GraphRunner run() {
        GraphRunner runner = getRunner();
        runner.setIsVerbose(false);
        runner.start(this);
        return runner;
    }

    public GraphRunner getRunner() {
        if (this.mRunner == null) {
            attachToRunner(new GraphRunner(this.mContext));
        }
        return this.mRunner;
    }

    public boolean isRunning() {
        return this.mRunner != null && this.mRunner.isRunning();
    }

    public void checkSignatures() {
        checkSignaturesForFilters(this.mFilterMap.values());
    }

    Filter[] getAllFilters() {
        return this.mAllFilters;
    }

    Set<FilterGraph> getSubGraphs() {
        return this.mSubGraphs;
    }

    static void checkSignaturesForFilters(Collection<Filter> filters) {
        for (Filter filter : filters) {
            Signature signature = filter.getSignature();
            signature.checkInputPortsConform(filter);
            signature.checkOutputPortsConform(filter);
        }
    }

    void wipe() {
        synchronized (this.mSubGraphsTearDownLock) {
            this.mSubGraphs.clear();
        }
        this.mContext.removeGraph(this);
        this.mAllFilters = null;
        this.mFilterMap = null;
        this.mParentGraph = null;
    }

    void flushFrames() {
        for (Filter filter : this.mFilterMap.values()) {
            for (InputPort inputPort : filter.getConnectedInputPorts()) {
                inputPort.clear();
            }
            for (OutputPort outputPort : filter.getConnectedOutputPorts()) {
                outputPort.clear();
            }
        }
    }

    private FilterGraph(MffContext context, FilterGraph parentGraph) {
        this.mFilterMap = new HashMap();
        this.mAllFilters = null;
        this.mSubGraphs = new HashSet();
        this.mSubGraphsTearDownLock = new Object();
        this.mContext = context;
        this.mContext.addGraph(this);
        if (parentGraph != null) {
            this.mParentGraph = parentGraph;
            this.mParentGraph.mSubGraphs.add(this);
        }
    }

    private void recursiveTearDown() {
        synchronized (this.mSubGraphsTearDownLock) {
            Iterator it = this.mSubGraphs.iterator();
            while (it.hasNext()) {
                ((FilterGraph) it.next()).recursiveTearDown();
            }
        }
        if (this.mRunner != null) {
            this.mRunner.tearDownGraph(this);
        }
    }
}
