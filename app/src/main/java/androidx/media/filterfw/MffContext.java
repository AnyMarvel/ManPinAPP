package androidx.media.filterfw;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Build.VERSION;
import android.os.Handler;
import android.os.Looper;
import android.renderscript.RenderScript;
import android.util.Log;
import android.view.SurfaceView;
import android.view.ViewGroup.LayoutParams;
import java.util.HashSet;
import java.util.Set;

public class MffContext {
    private Context mApplicationContext = null;
    private CameraStreamer mCameraStreamer = null;
    private boolean mCameraStreamingSupport;
    private SurfaceView mDummySurfaceView = null;
    private boolean mGLSupport;
    private Set<FilterGraph> mGraphs = new HashSet();
    private Handler mHandler = null;
    private boolean mPreserveFramesOnPause = false;
    private RenderScript mRenderScript;
    private Set<GraphRunner> mRunners = new HashSet();
    private State mState = new State();

    public static class Config {
        public SurfaceView dummySurface = null;
        public boolean forceNoGL = false;
        public boolean requireCamera = true;
        public boolean requireOpenGL = true;
    }

    private static class State {
        public static final int STATE_DESTROYED = 3;
        public static final int STATE_PAUSED = 2;
        public static final int STATE_RUNNING = 1;
        public int current;

        private State() {
            this.current = 1;
        }
    }

    public MffContext(Context context) {
        init(context, new Config());
    }

    public MffContext(Context context, Config config) {
        init(context, config);
    }

    public void onPause() {
        synchronized (this.mState) {
            if (this.mState.current == 1) {
                if (this.mCameraStreamer != null) {
                    this.mCameraStreamer.halt();
                }
                stopRunners(true);
                this.mState.current = 2;
            }
        }
    }

    public void onResume() {
        synchronized (this.mState) {
            if (this.mState.current == 2) {
                resumeRunners();
                resumeCamera();
                this.mState.current = 1;
            }
        }
    }

    public void release() {
        synchronized (this.mState) {
            if (this.mState.current != 3) {
                if (this.mCameraStreamer != null) {
                    this.mCameraStreamer.stop();
                    this.mCameraStreamer.tearDown();
                }
                if (VERSION.SDK_INT >= 11) {
                    maybeDestroyRenderScript();
                }
                stopRunners(false);
                waitUntilStopped();
                tearDown();
                this.mState.current = 3;
            }
        }
    }

    public void setPreserveFramesOnPause(boolean preserve) {
        this.mPreserveFramesOnPause = preserve;
    }

    public boolean getPreserveFramesOnPause() {
        return this.mPreserveFramesOnPause;
    }

    public Context getApplicationContext() {
        return this.mApplicationContext;
    }

    public CameraStreamer getCameraStreamer() {
        if (this.mCameraStreamer == null) {
            this.mCameraStreamer = new CameraStreamer(this);
        }
        return this.mCameraStreamer;
    }

    public static void setEGLConfigChooser(int redSize, int greenSize, int blueSize, int alphaSize, int depthSize, int stencilSize) {
        RenderTarget.setEGLConfigChooser(redSize, greenSize, blueSize, alphaSize, depthSize, stencilSize);
    }

    public final boolean isOpenGLSupported() {
        return this.mGLSupport;
    }

    public final boolean isCameraStreamingSupported() {
        return this.mCameraStreamingSupport;
    }

    @TargetApi(11)
    public final RenderScript getRenderScript() {
        if (this.mRenderScript == null) {
            this.mRenderScript = RenderScript.create(this.mApplicationContext);
        }
        return this.mRenderScript;
    }

    final void assertOpenGLSupported() {
        if (!isOpenGLSupported()) {
            throw new RuntimeException("Attempting to use OpenGL ES 2 in a context that does not support it!");
        }
    }

    void addGraph(FilterGraph graph) {
        synchronized (this.mGraphs) {
            this.mGraphs.add(graph);
        }
    }

    void removeGraph(FilterGraph graph) {
        synchronized (this.mGraphs) {
            this.mGraphs.remove(graph);
        }
    }

    void addRunner(GraphRunner runner) {
        synchronized (this.mRunners) {
            this.mRunners.add(runner);
        }
    }

    SurfaceView getDummySurfaceView() {
        return this.mDummySurfaceView;
    }

    void postRunnable(Runnable runnable) {
        this.mHandler.post(runnable);
    }

    private void init(Context context, Config config) {
        determineGLSupport(context, config);
        determineCameraSupport(config);
        createHandler();
        this.mApplicationContext = context.getApplicationContext();
        fetchDummySurfaceView(context, config);
    }

    private void fetchDummySurfaceView(Context context, Config config) {
        if (config.requireCamera && CameraStreamer.requireDummySurfaceView()) {
            SurfaceView surfaceView;
            if (config.dummySurface != null) {
                surfaceView = config.dummySurface;
            } else {
                surfaceView = createDummySurfaceView(context);
            }
            this.mDummySurfaceView = surfaceView;
        }
    }

    private void determineGLSupport(Context context, Config config) {
        if (config.forceNoGL) {
            this.mGLSupport = false;
            return;
        }
        this.mGLSupport = getPlatformSupportsGLES2(context);
        if (config.requireOpenGL && !this.mGLSupport) {
            throw new RuntimeException("Cannot create context that requires GL support on this platform!");
        }
    }

    private void determineCameraSupport(Config config) {
        this.mCameraStreamingSupport = CameraStreamer.getNumberOfCameras() > 0;
        if (config.requireCamera && !this.mCameraStreamingSupport) {
            throw new RuntimeException("Cannot create context that requires a camera on this platform!");
        }
    }

    private static boolean getPlatformSupportsGLES2(Context context) {
        return ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getDeviceConfigurationInfo().reqGlEsVersion >= 131072;
    }

    private void createHandler() {
        this.mHandler = new Handler(Looper.myLooper() != null ? Looper.myLooper() : Looper.getMainLooper());
    }

    private void stopRunners(boolean haltOnly) {
        synchronized (this.mRunners) {
            for (GraphRunner runner : this.mRunners) {
                if (haltOnly) {
                    runner.halt();
                } else {
                    runner.stop();
                }
            }
            if (!this.mPreserveFramesOnPause) {
                for (GraphRunner runner2 : this.mRunners) {
                    runner2.flushFrames();
                }
            }
        }
    }

    private void resumeRunners() {
        synchronized (this.mRunners) {
            for (GraphRunner runner : this.mRunners) {
                runner.restart();
            }
        }
    }

    private void resumeCamera() {
        if (this.mCameraStreamer != null) {
            this.mCameraStreamer.restart();
        }
    }

    private void waitUntilStopped() {
        for (GraphRunner runner : this.mRunners) {
            runner.waitUntilStop();
        }
    }

    private void tearDown() {
        Set<FilterGraph> rootGraphs = new HashSet();
        synchronized (this.mGraphs) {
            for (FilterGraph graph : this.mGraphs) {
                if (!graph.isSubGraph()) {
                    rootGraphs.add(graph);
                }
            }
        }
        for (FilterGraph graph2 : rootGraphs) {
            graph2.tearDown();
        }
        for (GraphRunner runner : this.mRunners) {
            runner.tearDown();
        }
    }

    private SurfaceView createDummySurfaceView(Context context) {
        SurfaceView dummySurfaceView = new SurfaceView(context);
        dummySurfaceView.getHolder().setType(3);
        Activity activity = findActivityForContext(context);
        if (activity != null) {
            activity.addContentView(dummySurfaceView, new LayoutParams(1, 1));
        } else {
            Log.w("MffContext", "Could not find activity for dummy surface! Consider specifying your own SurfaceView!");
        }
        return dummySurfaceView;
    }

    private Activity findActivityForContext(Context context) {
        return context instanceof Activity ? (Activity) context : null;
    }

    @TargetApi(11)
    private void maybeDestroyRenderScript() {
        if (this.mRenderScript != null) {
            this.mRenderScript.destroy();
            this.mRenderScript = null;
        }
    }
}
