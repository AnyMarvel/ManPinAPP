package androidx.media.filterfw;

import android.opengl.EGL14;
import android.os.ConditionVariable;
import android.os.SystemClock;
import android.util.Log;
import android.util.Pair;

import androidx.media.util.Trace;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.LinkedBlockingQueue;

public final class GraphRunner {
    private static final Event BEGIN_EVENT = new Event(2, null);
    public static final boolean COLLECT_TRACE = false;
    private static final Event FLUSH_EVENT = new Event(10, null);
    private static final Event HALT_EVENT = new Event(7, null);
    private static final Event KILL_EVENT = new Event(12, null);
    private static final Event PAUSE_EVENT = new Event(6, null);
    private static int PRIORITY_SLEEP = -1;
    private static int PRIORITY_STOP = -2;
    private static final Event RELEASE_FRAMES_EVENT = new Event(13, null);
    private static final Event RESTART_EVENT = new Event(9, null);
    private static final Event RESUME_EVENT = new Event(8, null);
    private static final Event STEP_EVENT = new Event(3, null);
    private static final Event STOP_EVENT = new Event(4, null);
    public static final int STRATEGY_FILTER_PRIORITY = 5;
    public static final int STRATEGY_LFU = 3;
    public static final int STRATEGY_LRU = 2;
    public static final int STRATEGY_ONESHOT = 4;
    public static final int STRATEGY_RANDOM = 1;
    private static final String TAG = GraphRunner.class.getSimpleName();
    private static ThreadLocal<GraphRunner> mThreadRunner = new ThreadLocal();
    private final MffContext mContext;
    private FrameManager mFrameManager;
    private final Set<FilterGraph> mGraphs;
    private final RunParameters mParams;
    private GraphRunLoop mRunLoop;
    private Thread mRunThread;
    private FilterGraph mRunningGraph;
    private Scheduler mScheduler;

    public static class Config {
        public boolean allowOpenGL = true;
        public int threadPriority = 5;
    }

    private static class Event {
        public static final int BEGIN = 2;
        public static final int EARLY_PREPARE = 14;
        public static final int FLUSH = 10;
        public static final int HALT = 7;
        public static final int KILL = 12;
        public static final int PAUSE = 6;
        public static final int PREPARE = 1;
        public static final int RELEASE_FRAMES = 13;
        public static final int RESTART = 9;
        public static final int RESUME = 8;
        public static final int STEP = 3;
        public static final int STOP = 4;
        public static final int TEARDOWN = 11;
        public int code;
        public Object object;

        public Event(int code, Object object) {
            this.code = code;
            this.object = object;
        }
    }

    private static class FilterTiming {
        public int count;
        public long realTime;
        public long threadTime;

        private FilterTiming() {
        }
    }

    private final class GraphRunLoop implements Runnable {
        private final boolean mAllowOpenGL;
        private long mBeginTimeReal;
        private long mBeginTimeThread;
        private Exception mCaughtException = null;
        private boolean mClosedSuccessfully = true;
        private LinkedBlockingQueue<Event> mEventQueue = new LinkedBlockingQueue();
        private Map<Filter, FilterTiming> mFilterTimings = new HashMap();
        private Stack<Filter[]> mFilters = new Stack();
        private Set<FilterGraph> mOpenedGraphs = new HashSet();
        private RenderTarget mRenderTarget = null;
        private final ScheduleResult mScheduleResult = new ScheduleResult();
        private State mState = new State();
        public ConditionVariable mStopCondition = new ConditionVariable(true);
        private Stack<SubListener> mSubListeners = new Stack();

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void loop() {
            /*
            r4 = this;
            r2 = 0;
        L_0x0001:
            if (r2 != 0) goto L_0x0071;
        L_0x0003:
            r1 = r4.nextEvent();	 Catch:{ Exception -> 0x0017 }
            if (r1 == 0) goto L_0x0001;
        L_0x0009:
            r3 = r1.code;	 Catch:{ Exception -> 0x0017 }
            switch(r3) {
                case 1: goto L_0x000f;
                case 2: goto L_0x002c;
                case 3: goto L_0x0038;
                case 4: goto L_0x003c;
                case 5: goto L_0x000e;
                case 6: goto L_0x0040;
                case 7: goto L_0x0044;
                case 8: goto L_0x0048;
                case 9: goto L_0x004c;
                case 10: goto L_0x0050;
                case 11: goto L_0x0054;
                case 12: goto L_0x005c;
                case 13: goto L_0x0061;
                case 14: goto L_0x0030;
                default: goto L_0x000e;
            };	 Catch:{ Exception -> 0x0017 }
        L_0x000e:
            goto L_0x0001;
        L_0x000f:
            r3 = r1.object;	 Catch:{ Exception -> 0x0017 }
            r3 = (androidx.media.filterfw.FilterGraph) r3;	 Catch:{ Exception -> 0x0017 }
            r4.onPrepare(r3);	 Catch:{ Exception -> 0x0017 }
            goto L_0x0001;
        L_0x0017:
            r0 = move-exception;
            r3 = r4.mCaughtException;
            if (r3 != 0) goto L_0x0065;
        L_0x001c:
            r4.mCaughtException = r0;
            r3 = 1;
            r4.mClosedSuccessfully = r3;
            com.google.devtools.build.android.desugar.runtime.ThrowableExtension.printStackTrace(r0);
            r3 = androidx.media.filterfw.GraphRunner.STOP_EVENT;
            r4.pushEvent(r3);
            goto L_0x0001;
        L_0x002c:
            r4.onBegin();	 Catch:{ Exception -> 0x0017 }
            goto L_0x0001;
        L_0x0030:
            r3 = r1.object;	 Catch:{ Exception -> 0x0017 }
            r3 = (androidx.media.filterfw.FilterGraph) r3;	 Catch:{ Exception -> 0x0017 }
            r4.onEarlyPrepare(r3);	 Catch:{ Exception -> 0x0017 }
            goto L_0x0001;
        L_0x0038:
            r4.onStep();	 Catch:{ Exception -> 0x0017 }
            goto L_0x0001;
        L_0x003c:
            r4.onStop();	 Catch:{ Exception -> 0x0017 }
            goto L_0x0001;
        L_0x0040:
            r4.onPause();	 Catch:{ Exception -> 0x0017 }
            goto L_0x0001;
        L_0x0044:
            r4.onHalt();	 Catch:{ Exception -> 0x0017 }
            goto L_0x0001;
        L_0x0048:
            r4.onResume();	 Catch:{ Exception -> 0x0017 }
            goto L_0x0001;
        L_0x004c:
            r4.onRestart();	 Catch:{ Exception -> 0x0017 }
            goto L_0x0001;
        L_0x0050:
            r4.onFlush();	 Catch:{ Exception -> 0x0017 }
            goto L_0x0001;
        L_0x0054:
            r3 = r1.object;	 Catch:{ Exception -> 0x0017 }
            r3 = (androidx.media.filterfw.FilterGraph) r3;	 Catch:{ Exception -> 0x0017 }
            r4.onTearDown(r3);	 Catch:{ Exception -> 0x0017 }
            goto L_0x0001;
        L_0x005c:
            r4.onKill();	 Catch:{ Exception -> 0x0017 }
            r2 = 1;
            goto L_0x0001;
        L_0x0061:
            r4.onReleaseFrames();	 Catch:{ Exception -> 0x0017 }
            goto L_0x0001;
        L_0x0065:
            r3 = 0;
            r4.mClosedSuccessfully = r3;
            r3 = r4.mEventQueue;
            r3.clear();
            r4.cleanUp();
            goto L_0x0001;
        L_0x0071:
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: androidx.media.filterfw.GraphRunner.GraphRunLoop.loop():void");
        }

        public GraphRunLoop(boolean allowOpenGL) {
            this.mAllowOpenGL = allowOpenGL;
        }

        public void run() {
            try {
                onInit();
                loop();
                onDestroy();
            } catch (RuntimeException e) {
                this.mCaughtException = e;
                this.mClosedSuccessfully = true;
                Log.w("GraphRunner", "exception running graph", e);
                cleanUp();
                onDestroy();
            }
        }

        public void enterSubGraph(FilterGraph graph, SubListener listener) {
            if (this.mState.check(4)) {
                onOpenGraph(graph);
                this.mSubListeners.push(listener);
            }
        }

        public void pushWakeEvent(Event event) {
            if (this.mEventQueue.isEmpty()) {
                pushEvent(event);
            }
        }

        public void pushEvent(Event event) {
            this.mEventQueue.offer(event);
        }

        public void pushEvent(int eventId, Object object) {
            this.mEventQueue.offer(new Event(eventId, object));
        }

        public boolean checkState(int state) {
            return this.mState.check(state);
        }

        public ConditionVariable getStopCondition() {
            return this.mStopCondition;
        }

        public boolean isOpenGLAllowed() {
            return this.mAllowOpenGL;
        }

        private void dumpTimings(long totalTimeReal, long totalTimeThread) {
            Map<Object, FilterTiming> filterTypes = new HashMap();
            List<Pair<Object, FilterTiming>> filtersSorted = new ArrayList();
            List<Pair<Object, FilterTiming>> filterTypesSorted = new ArrayList();
            FilterTiming netTimings = new FilterTiming();
            for (Entry<Filter, FilterTiming> it : this.mFilterTimings.entrySet()) {
                Filter filter = (Filter) it.getKey();
                FilterTiming timing = (FilterTiming) it.getValue();
                filtersSorted.add(new Pair(filter, timing));
                FilterTiming typeTiming = (FilterTiming) filterTypes.get(filter.getClass());
                if (typeTiming == null) {
                    typeTiming = new FilterTiming();
                    filterTypes.put(filter.getClass(), typeTiming);
                }
                typeTiming.threadTime += timing.threadTime;
                typeTiming.realTime += timing.realTime;
                typeTiming.count += timing.count;
                netTimings.threadTime += timing.threadTime;
                netTimings.realTime += timing.realTime;
                netTimings.count += timing.count;
            }
            for (Entry<Object, FilterTiming> it2 : filterTypes.entrySet()) {
                filterTypesSorted.add(new Pair(it2.getKey(), (FilterTiming) it2.getValue()));
            }
            Comparator<Pair<Object, FilterTiming>> comparator = new Comparator<Pair<Object, FilterTiming>>() {
                public int compare(Pair<Object, FilterTiming> lhs, Pair<Object, FilterTiming> rhs) {
                    return (((FilterTiming) rhs.second).realTime > ((FilterTiming) lhs.second).realTime ? 1 : (((FilterTiming) rhs.second).realTime == ((FilterTiming) lhs.second).realTime ? 0 : -1));
                }
            };
            Collections.sort(filtersSorted, comparator);
            Collections.sort(filterTypesSorted, comparator);
            timingLog("\n*** Timings ***\n");
            timingLog(String.format("Graph time: %dms real, %dms thread (%.4f%%)", new Object[]{Long.valueOf(totalTimeReal), Long.valueOf(totalTimeThread), Float.valueOf((((float) totalTimeThread) * 100.0f) / ((float) totalTimeReal))}));
            timingLog(String.format("Filter totals: %dms real (%.4f%%), %dms thread (%.4f%%)", new Object[]{Long.valueOf(netTimings.realTime), Float.valueOf((((float) netTimings.realTime) * 100.0f) / ((float) totalTimeReal)), Long.valueOf(netTimings.threadTime), Float.valueOf((((float) netTimings.threadTime) * 100.0f) / ((float) totalTimeThread))}));
            timingLog("\n* Individual filters\n");
            for (Pair<Object, FilterTiming> timing2 : filtersSorted) {
                dump(timing2.first.toString(), (FilterTiming) timing2.second, netTimings);
            }
            timingLog("\n* Filter types\n");
            for (Pair<Object, FilterTiming> timing22 : filterTypesSorted) {
                dump(timing22.first.toString(), (FilterTiming) timing22.second, netTimings);
            }
            this.mFilterTimings.clear();
        }

        private void dump(String filterName, FilterTiming timing, FilterTiming netTimings) {
            timingLog(String.format("%dms %.4f%% real, %dms %.4f%% thread (%.4f%%) (x%d) - %s", new Object[]{Long.valueOf(timing.realTime), Float.valueOf((((float) timing.realTime) * 100.0f) / ((float) netTimings.realTime)), Long.valueOf(timing.threadTime), Float.valueOf((((float) timing.threadTime) * 100.0f) / ((float) netTimings.threadTime)), Float.valueOf((((float) timing.threadTime) * 100.0f) / ((float) timing.realTime)), Integer.valueOf(timing.count), filterName}));
        }

        private void timingLog(String message) {
            Log.i(GraphRunner.TAG, message);
        }

        private Event nextEvent() {
            try {
                return (Event) this.mEventQueue.take();
            } catch (InterruptedException e) {
                Log.w("GraphRunner", "Event queue processing was interrupted.");
                return null;
            }
        }

        private void onPause() {
            this.mState.addState(8);
        }

        private void onResume() {
            if (this.mState.removeState(8) && this.mState.current() == 4) {
                pushEvent(GraphRunner.STEP_EVENT);
            }
        }

        private void onHalt() {
            if (this.mState.addState(16) && this.mState.check(4)) {
                closeAllFilters();
            }
        }

        private void onRestart() {
            if (this.mState.removeState(16) && this.mState.current() == 4) {
                pushEvent(GraphRunner.STEP_EVENT);
            }
        }

        private void onDestroy() {
            GraphRunner.this.mFrameManager.destroyBackings();
            if (this.mRenderTarget != null) {
                this.mRenderTarget.release();
                this.mRenderTarget = null;
                EGL14.eglReleaseThread();
            }
        }

        private void onReleaseFrames() {
            if (GraphRunner.this.mGraphs.isEmpty()) {
                GraphRunner.this.mFrameManager.destroyBackings();
            } else {
                throw new IllegalStateException("Attempting to release frames with " + GraphRunner.this.mGraphs.size() + " graphs still attached!");
            }
        }

        private void onInit() {
            GraphRunner.mThreadRunner.set(GraphRunner.this);
            if (GraphRunner.this.getContext().isOpenGLSupported()) {
                this.mRenderTarget = RenderTarget.newTarget(1, 1);
                this.mRenderTarget.focus();
                RenderTarget.setMainTextureTarget(this.mRenderTarget);
            }
        }

        private void onEarlyPrepare(FilterGraph graph) {
            if (this.mState.current() == 1) {
                for (FilterGraph subgraph : graph.getSubGraphs()) {
                    onEarlyPrepare(subgraph);
                }
                for (Filter filter : graph.getAllFilters()) {
                    filter.prepareOnly();
                }
            }
        }

        private void onPrepare(FilterGraph graph) {
            if (this.mState.current() == 1) {
                this.mState.setState(2);
                this.mCaughtException = null;
                onOpenGraph(graph);
            }
        }

        private void onOpenGraph(FilterGraph graph) {
            loadFilters(graph);
            this.mOpenedGraphs.add(graph);
            this.mFilters.push(GraphRunner.this.mScheduler.prepare((Filter[]) this.mFilters.pop()));
            pushEvent(GraphRunner.BEGIN_EVENT);
        }

        private void onBegin() {
            if (this.mState.current() == 2) {
                this.mBeginTimeReal = SystemClock.elapsedRealtime();
                this.mBeginTimeThread = SystemClock.currentThreadTimeMillis();
                this.mState.setState(4);
                pushEvent(GraphRunner.STEP_EVENT);
            }
        }

        private void onStarve() {
            this.mFilters.pop();
            if (this.mFilters.empty()) {
                onStop();
                return;
            }
            SubListener listener = (SubListener) this.mSubListeners.pop();
            if (listener != null) {
                listener.onSubGraphRunEnded(GraphRunner.this);
            }
            this.mFilters.push(GraphRunner.this.mScheduler.prepare((Filter[]) this.mFilters.pop()));
            pushEvent(GraphRunner.STEP_EVENT);
        }

        private void onStop() {
            if (this.mState.check(4) || this.mState.check(2)) {
                if (GraphRunner.this.isVerbose()) {
                    dumpTimings(SystemClock.elapsedRealtime() - this.mBeginTimeReal, SystemClock.currentThreadTimeMillis() - this.mBeginTimeThread);
                }
                if (!this.mState.check(16)) {
                    closeAllFilters();
                }
                cleanUp();
            }
        }

        private void cleanUp() {
            this.mState.setState(1);
            if (GraphRunner.this.flushOnClose()) {
                onFlush();
            }
            GraphRunner.this.mScheduler.cleanUp();
            this.mOpenedGraphs.clear();
            this.mFilters.clear();
            GraphRunner.this.onRunnerStopped(this.mCaughtException, this.mClosedSuccessfully);
            this.mStopCondition.open();
        }

        private void onStep() {
            Trace.beginSection("GraphRunner.onStep()");
            if (this.mState.current() == 4) {
                GraphRunner.this.mScheduler.nextFilter(currentFilters(), this.mScheduleResult);
                if (this.mScheduleResult.priority != ((long) GraphRunner.PRIORITY_SLEEP)) {
                    if (this.mScheduleResult.priority == ((long) GraphRunner.PRIORITY_STOP)) {
                        onStarve();
                    } else {
                        scheduleFilter(this.mScheduleResult.filter);
                        pushEvent(GraphRunner.STEP_EVENT);
                    }
                }
            } else {
                Log.w("GraphRunner", "State is not running! (" + this.mState.current() + ")");
            }
            Trace.endSection();
        }

        private void onFlush() {
            if (this.mState.check(16) || this.mState.check(1)) {
                for (FilterGraph graph : this.mOpenedGraphs) {
                    graph.flushFrames();
                }
            }
        }

        private void onTearDown(FilterGraph graph) {
            if (this.mState.check(4)) {
                throw new IllegalStateException("Attempting to teardown graph while running!");
            }
            if (graph.getAllFilters() != null) {
                for (Filter filter : graph.getAllFilters()) {
                    filter.performTearDown();
                }
                graph.wipe();
            }
            GraphRunner.this.mGraphs.remove(graph);
        }

        private void onKill() {
            synchronized (GraphRunner.this.mGraphs) {
                if (GraphRunner.this.mGraphs.isEmpty()) {
                } else {
                    throw new IllegalStateException("Attempting to tear down runner with " + GraphRunner.this.mGraphs.size() + " graphs still attached!");
                }
            }
        }

        private void loadFilters(FilterGraph graph) {
            this.mFilters.push(graph.getAllFilters());
        }

        private void closeAllFilters() {
            for (FilterGraph graph : this.mOpenedGraphs) {
                closeFilters(graph);
            }
        }

        private void closeFilters(FilterGraph graph) {
            Log.v("GraphRunner", "CLOSING FILTERS");
            Filter[] filters = graph.getAllFilters();
            boolean isVerbose = GraphRunner.this.isVerbose();
            for (int i = 0; i < filters.length; i++) {
                if (isVerbose) {
                    String valueOf = String.valueOf(filters[i]);
                    Log.i("GraphRunner", new StringBuilder(String.valueOf(valueOf).length() + 16).append("Closing Filter ").append(valueOf).append("!").toString());
                }
                filters[i].softReset();
            }
        }

        private Filter[] currentFilters() {
            return (Filter[]) this.mFilters.peek();
        }

        private void scheduleFilter(Filter filter) {
            long realTimeBegin = 0;
            long threadTimeBegin = 0;
            if (GraphRunner.this.isVerbose()) {
                realTimeBegin = SystemClock.elapsedRealtime();
                threadTimeBegin = SystemClock.currentThreadTimeMillis();
            }
            filter.execute();
            if (GraphRunner.this.isVerbose()) {
                long realTimeEnd = SystemClock.elapsedRealtime();
                long threadTimeEnd = SystemClock.currentThreadTimeMillis();
                FilterTiming timing = (FilterTiming) this.mFilterTimings.get(filter);
                if (timing == null) {
                    timing = new FilterTiming();
                    this.mFilterTimings.put(filter, timing);
                }
                timing.realTime += realTimeEnd - realTimeBegin;
                timing.threadTime += threadTimeEnd - threadTimeBegin;
                timing.count++;
            }
        }
    }

    public interface Listener {
        void onGraphRunnerError(Exception exception, boolean z);

        void onGraphRunnerStopped(GraphRunner graphRunner);
    }

    private static class RunParameters {
        public boolean flushOnClose;
        public boolean isVerbose;
        public Listener listener;

        private RunParameters() {
            this.listener = null;
            this.isVerbose = false;
            this.flushOnClose = true;
        }
    }

    private static class ScheduleResult {
        public Filter filter;
        public long priority;

        private ScheduleResult() {
        }
    }

    private interface Scheduler {
        void cleanUp();

        int getStrategy();

        void nextFilter(Filter[] filterArr, ScheduleResult scheduleResult);

        Filter[] prepare(Filter[] filterArr);
    }

    private static class State {
        public static final int HALTED = 16;
        public static final int PAUSED = 8;
        public static final int PREPARING = 2;
        public static final int RUNNING = 4;
        public static final int STOPPED = 1;
        private int mCurrent;

        private State() {
            this.mCurrent = 1;
        }

        public synchronized void setState(int newState) {
            this.mCurrent = newState;
        }

        public synchronized boolean check(int state) {
            return (this.mCurrent & state) == state;
        }

        public synchronized boolean addState(int state) {
            boolean z;
            if ((this.mCurrent & state) != state) {
                this.mCurrent |= state;
                z = true;
            } else {
                z = false;
            }
            return z;
        }

        public synchronized boolean removeState(int state) {
            boolean result;
            result = (this.mCurrent & state) == state;
            this.mCurrent &= state ^ -1;
            return result;
        }

        public synchronized int current() {
            return this.mCurrent;
        }
    }

    public interface SubListener {
        void onSubGraphRunEnded(GraphRunner graphRunner);
    }

    private static class FilterPriorityScheduler implements Scheduler {
        private Set<Filter[]> mAlreadySorted;
        private Comparator<Filter> mFilterComparator;
        private int mNextFilterIndex;

        private FilterPriorityScheduler() {
            this.mNextFilterIndex = 0;
            this.mAlreadySorted = new HashSet();
            this.mFilterComparator = new Comparator<Filter>() {
                public int compare(Filter lhs, Filter rhs) {
                    return rhs.getSchedulePriority() - lhs.getSchedulePriority();
                }
            };
        }

        public Filter[] prepare(Filter[] filters) {
            this.mNextFilterIndex = 0;
            if (this.mAlreadySorted.contains(filters)) {
                return filters;
            }
            Filter[] sorted = sortFilters(filters);
            this.mAlreadySorted.add(sorted);
            return sorted;
        }

        public int getStrategy() {
            return 5;
        }

        public void nextFilter(Filter[] filters, ScheduleResult result) {
            long maxPriority = (long) GraphRunner.PRIORITY_STOP;
            Filter nextFilter = null;
            for (int i = 0; i < filters.length; i++) {
                Filter filter = filters[this.mNextFilterIndex];
                this.mNextFilterIndex = (this.mNextFilterIndex + 1) % filters.length;
                if (filter.isSleeping()) {
                    maxPriority = (long) GraphRunner.PRIORITY_SLEEP;
                } else if (filter.canSchedule()) {
                    nextFilter = filter;
                    maxPriority = 0;
                    break;
                }
            }
            result.filter = nextFilter;
            result.priority = maxPriority;
        }

        public void cleanUp() {
            this.mAlreadySorted.clear();
        }

        private boolean allDependenciesAdded(List<Filter> list, Filter filter) {
            for (InputPort inputPort : filter.getConnectedInputPorts()) {
                OutputPort outputPort = inputPort.getSourceHint();
                if (outputPort != null) {
                    Filter source = outputPort.getFilter();
                    if (!(source == null || list.contains(source))) {
                        return false;
                    }
                }
            }
            return true;
        }

        private Filter[] sortFilters(Filter[] filters) {
            List<Filter> remaining = new ArrayList(Arrays.asList(filters));
            List<Filter> list = new ArrayList(filters.length);
            while (remaining.size() > 0) {
                List<Filter> canExecute = new ArrayList();
                int c = remaining.size();
                for (int i = 0; i < c; i++) {
                    Filter filter = (Filter) remaining.get(i);
                    if (allDependenciesAdded(list, filter)) {
                        canExecute.add(filter);
                        remaining.remove(i);
                        c--;
                    }
                }
                Collections.sort(canExecute, this.mFilterComparator);
                list.addAll(canExecute);
            }
            return (Filter[]) list.toArray(new Filter[list.size()]);
        }
    }

    private static class LruScheduler implements Scheduler {
        private LinkedList<Filter> mFilterQueue;

        private LruScheduler() {
        }

        public Filter[] prepare(Filter[] filters) {
            this.mFilterQueue = new LinkedList(Arrays.asList(filters));
            return filters;
        }

        public int getStrategy() {
            return 2;
        }

        public void nextFilter(Filter[] filters, ScheduleResult result) {
            result.priority = (long) GraphRunner.PRIORITY_STOP;
            ListIterator<Filter> iter = this.mFilterQueue.listIterator();
            while (iter.hasNext()) {
                Filter filter = (Filter) iter.next();
                if (filter.isSleeping()) {
                    result.filter = filter;
                    result.priority = (long) GraphRunner.PRIORITY_SLEEP;
                } else if (filter.canSchedule()) {
                    result.filter = filter;
                    result.priority = 100;
                    iter.remove();
                    this.mFilterQueue.add(filter);
                    return;
                }
            }
        }

        public void cleanUp() {
        }
    }

    private abstract class ScoringScheduler implements Scheduler {
        protected abstract long priorityForFilter(Filter filter);

        private ScoringScheduler() {
        }

        public void nextFilter(Filter[] filters, ScheduleResult result) {
            long maxPriority = (long) GraphRunner.PRIORITY_STOP;
            Filter bestFilter = null;
            for (Filter filter : filters) {
                long priority = priorityForFilter(filter);
                if (priority > maxPriority) {
                    maxPriority = priority;
                    bestFilter = filter;
                }
            }
            result.filter = bestFilter;
            result.priority = maxPriority;
        }
    }

    private class LfuScheduler extends ScoringScheduler {
        private static final int MAX_PRIORITY = Integer.MAX_VALUE;

        private LfuScheduler() {
            super();
        }

        public Filter[] prepare(Filter[] filters) {
            for (Filter filter : filters) {
                filter.resetScheduleCount();
            }
            return filters;
        }

        public int getStrategy() {
            return 3;
        }

        protected long priorityForFilter(Filter filter) {
            if (filter.isSleeping()) {
                return (long) GraphRunner.PRIORITY_SLEEP;
            }
            int scheduleCount;
            if (filter.canSchedule()) {
                scheduleCount = Integer.MAX_VALUE - filter.getScheduleCount();
            } else {
                scheduleCount = GraphRunner.PRIORITY_STOP;
            }
            return (long) scheduleCount;
        }

        public void cleanUp() {
        }
    }

    private class OneShotScheduler extends LfuScheduler {
        private int mCurCount;

        private OneShotScheduler() {
            super();
            this.mCurCount = 1;
        }

        public int getStrategy() {
            return 4;
        }

        protected long priorityForFilter(Filter filter) {
            if (filter.getScheduleCount() < this.mCurCount) {
                return super.priorityForFilter(filter);
            }
            return (long) GraphRunner.PRIORITY_STOP;
        }

        public void cleanUp() {
        }
    }

    public GraphRunner(MffContext context) {
        this(context, new Config());
    }

    public GraphRunner(MffContext context, Config config) {
        this.mRunningGraph = null;
        this.mGraphs = new HashSet();
        this.mRunThread = null;
        this.mFrameManager = null;
        this.mParams = new RunParameters();
        this.mContext = context;
        this.mFrameManager = new FrameManager(this, 1);
        createScheduler(2);
        this.mRunLoop = new GraphRunLoop(config.allowOpenGL);
        this.mRunThread = new Thread(this.mRunLoop);
        this.mRunThread.setPriority(config.threadPriority);
        this.mRunThread.start();
        this.mContext.addRunner(this);
    }

    public void setThreadName(String name) {
        this.mRunThread.setName(name);
    }

    public static GraphRunner current() {
        return (GraphRunner) mThreadRunner.get();
    }

    public synchronized FilterGraph getRunningGraph() {
        return this.mRunningGraph;
    }

    public MffContext getContext() {
        return this.mContext;
    }

    public synchronized void start(FilterGraph graph) {
        if (graph.mRunner != this) {
            throw new IllegalArgumentException("Graph must be attached to runner!");
        }
        this.mRunningGraph = graph;
        this.mRunLoop.getStopCondition().close();
        this.mRunLoop.pushEvent(1, graph);
    }

    public synchronized void earlyPrepare(FilterGraph graph) {
        if (graph.mRunner != this) {
            throw new IllegalArgumentException("Graph must be attached to runner!");
        }
        this.mRunLoop.pushEvent(14, graph);
    }

    public void enterSubGraph(FilterGraph graph, SubListener listener) {
        if (Thread.currentThread() != this.mRunThread) {
            throw new RuntimeException("enterSubGraph must be called from the runner's thread!");
        }
        this.mRunLoop.enterSubGraph(graph, listener);
    }

    public void waitUntilStop() {
        this.mRunLoop.getStopCondition().block();
    }

    public void pause() {
        this.mRunLoop.pushEvent(PAUSE_EVENT);
    }

    public void resume() {
        this.mRunLoop.pushEvent(RESUME_EVENT);
    }

    public void stop() {
        this.mRunLoop.pushEvent(STOP_EVENT);
    }

    public boolean isRunning() {
        return !this.mRunLoop.checkState(1);
    }

    public boolean isPaused() {
        return this.mRunLoop.checkState(8);
    }

    public boolean isStopped() {
        return this.mRunLoop.checkState(1);
    }

    public void setSchedulerStrategy(int strategy) {
        if (isRunning()) {
            throw new RuntimeException("Attempting to change scheduling strategy on running GraphRunner!");
        }
        createScheduler(strategy);
    }

    public int getSchedulerStrategy() {
        return this.mScheduler.getStrategy();
    }

    public void setIsVerbose(boolean isVerbose) {
        synchronized (this.mParams) {
            this.mParams.isVerbose = isVerbose;
        }
    }

    public boolean isVerbose() {
        boolean z;
        synchronized (this.mParams) {
            z = this.mParams.isVerbose;
        }
        return z;
    }

    public boolean isOpenGLSupported() {
        return this.mRunLoop.isOpenGLAllowed() && this.mContext.isOpenGLSupported();
    }

    public void setFlushOnClose(boolean flush) {
        synchronized (this.mParams) {
            this.mParams.flushOnClose = flush;
        }
    }

    public boolean flushOnClose() {
        boolean z;
        synchronized (this.mParams) {
            z = this.mParams.flushOnClose;
        }
        return z;
    }

    public void setListener(Listener listener) {
        synchronized (this.mParams) {
            this.mParams.listener = listener;
        }
    }

    public Listener getListener() {
        Listener listener;
        synchronized (this.mParams) {
            listener = this.mParams.listener;
        }
        return listener;
    }

    public FrameManager getFrameManager() {
        return this.mFrameManager;
    }

    public void tearDown() {
        this.mRunLoop.pushEvent(KILL_EVENT);
        try {
            this.mRunThread.join();
        } catch (InterruptedException e) {
            Log.e("GraphRunner", "Error waiting for runner thread to finish!");
        }
    }

    public void releaseFrames() {
        this.mRunLoop.pushEvent(RELEASE_FRAMES_EVENT);
    }

    void attachGraph(FilterGraph graph) {
        synchronized (this.mGraphs) {
            this.mGraphs.add(graph);
        }
    }

    void signalWakeUp() {
        this.mRunLoop.pushWakeEvent(STEP_EVENT);
    }

    void begin() {
        this.mRunLoop.pushEvent(BEGIN_EVENT);
    }

    void halt() {
        this.mRunLoop.pushEvent(HALT_EVENT);
    }

    void restart() {
        this.mRunLoop.pushEvent(RESTART_EVENT);
    }

    void tearDownGraph(FilterGraph graph) {
        if (graph.getRunner() != this) {
            throw new IllegalArgumentException("Attempting to tear down graph with foreign GraphRunner!");
        }
        this.mRunLoop.pushEvent(11, graph);
    }

    void flushFrames() {
        this.mRunLoop.pushEvent(FLUSH_EVENT);
    }

    private void createScheduler(int strategy) {
        switch (strategy) {
            case 2:
                this.mScheduler = new LruScheduler();
                return;
            case 3:
                this.mScheduler = new LfuScheduler();
                return;
            case 4:
                this.mScheduler = new OneShotScheduler();
                return;
            case 5:
                this.mScheduler = new FilterPriorityScheduler();
                return;
            default:
                throw new IllegalArgumentException("Unknown schedule-strategy constant " + strategy + "!");
        }
    }

    private void onRunnerStopped(final Exception exception, final boolean closed) {
        this.mRunningGraph = null;
        synchronized (this.mParams) {
            if (this.mParams.listener != null) {
                getContext().postRunnable(new Runnable() {
                    public void run() {
                        if (GraphRunner.this.mParams.listener != null) {
                            if (exception == null) {
                                GraphRunner.this.mParams.listener.onGraphRunnerStopped(GraphRunner.this);
                            } else {
                                GraphRunner.this.mParams.listener.onGraphRunnerError(exception, closed);
                            }
                        }
                    }
                });
            } else if (exception != null) {
                Log.e("GraphRunner", "Uncaught exception during graph execution! Stack Trace: ");
                exception.printStackTrace();
            }
        }
    }
}
