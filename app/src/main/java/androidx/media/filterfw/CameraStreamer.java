package androidx.media.filterfw;

import android.annotation.TargetApi;
import android.graphics.SurfaceTexture;
import android.graphics.SurfaceTexture.OnFrameAvailableListener;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.opengl.GLES20;
import android.os.Build.VERSION;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceView;

import androidx.media.filterfw.decoder.MediaDecoder;
import androidx.media.filterfw.decoder.VideoFrameInfo;
import androidx.media.filterfw.decoder.VideoStreamProvider;
import androidx.media.filterfw.geometry.ScaleUtils;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import javax.microedition.khronos.egl.EGLContext;

public class CameraStreamer implements VideoStreamProvider {
    public static final int FACING_BACK = 2;
    public static final int FACING_DONTCARE = 0;
    public static final int FACING_FRONT = 1;
    public static final long MAX_CAMERA_WAIT_TIME = 5;
    static ReentrantLock mCameraLock = new ReentrantLock();
    private CameraRunnable mCameraRunner = null;

    private abstract class CamFrameHandler {
        protected int mCameraHeight;
        protected int mCameraWidth;
        protected Vector<VideoFrameConsumer> mClients;
        protected HashMap<EGLContext, Set<VideoFrameConsumer>> mContextClients;
        protected int mOutHeight;
        protected int mOutWidth;
        protected CameraRunnable mRunner;
        protected HashMap<EGLContext, ImageShader> mTargetShaders;
        protected HashMap<EGLContext, TextureSource> mTargetTextures;

        public abstract void grabFrame(FrameImage2D frameImage2D, int i);

        public abstract void release();

        public abstract void setupServerFrame();

        public abstract void updateServerFrame();

        private CamFrameHandler() {
            this.mTargetShaders = new HashMap();
            this.mTargetTextures = new HashMap();
            this.mContextClients = new HashMap();
            this.mClients = new Vector();
        }

        public void initWithRunner(CameraRunnable camRunner) {
            this.mRunner = camRunner;
        }

        public void setCameraSize(int width, int height) {
            this.mCameraWidth = width;
            this.mCameraHeight = height;
        }

        public void registerClient(VideoFrameConsumer client) {
            EGLContext context = RenderTarget.currentContext();
            clientsForContext(context).add(client);
            this.mClients.add(client);
            onRegisterClient(client, context);
        }

        public void unregisterClient(VideoFrameConsumer client) {
            EGLContext context = RenderTarget.currentContext();
            Set<VideoFrameConsumer> clientTargets = clientsForContext(context);
            clientTargets.remove(client);
            if (clientTargets.isEmpty()) {
                onCleanupContext(context);
            }
            this.mClients.remove(client);
        }

        public void onUpdateCameraOrientation(int orientation) {
            if (orientation % MediaDecoder.ROTATE_180 != 0) {
                this.mOutWidth = this.mCameraHeight;
                this.mOutHeight = this.mCameraWidth;
                return;
            }
            this.mOutWidth = this.mCameraWidth;
            this.mOutHeight = this.mCameraHeight;
        }

        protected Set<VideoFrameConsumer> clientsForContext(EGLContext context) {
            Set<VideoFrameConsumer> clients = (Set) this.mContextClients.get(context);
            if (clients != null) {
                return clients;
            }
            clients = new HashSet();
            this.mContextClients.put(context, clients);
            return clients;
        }

        protected void onRegisterClient(VideoFrameConsumer client, EGLContext context) {
        }

        protected void onCleanupContext(EGLContext context) {
            TextureSource texture = (TextureSource) this.mTargetTextures.get(context);
            ImageShader shader = (ImageShader) this.mTargetShaders.get(context);
            if (texture != null) {
                texture.release();
                this.mTargetTextures.remove(context);
            }
            if (shader != null) {
                this.mTargetShaders.remove(context);
            }
        }

        protected TextureSource textureForContext(EGLContext context) {
            TextureSource texture = (TextureSource) this.mTargetTextures.get(context);
            if (texture != null) {
                return texture;
            }
            texture = createClientTexture();
            this.mTargetTextures.put(context, texture);
            return texture;
        }

        protected ImageShader shaderForContext(EGLContext context) {
            ImageShader shader = (ImageShader) this.mTargetShaders.get(context);
            if (shader != null) {
                return shader;
            }
            shader = createClientShader();
            this.mTargetShaders.put(context, shader);
            return shader;
        }

        protected ImageShader createClientShader() {
            return null;
        }

        protected TextureSource createClientTexture() {
            return null;
        }

        public boolean isFrontMirrored() {
            return true;
        }
    }

    public interface CameraListener {
        void onCameraClosed(CameraStreamer cameraStreamer);

        void onCameraOpened(CameraStreamer cameraStreamer);
    }

    private final class CameraRunnable implements Runnable {
        private static final int MAX_EVENTS = 32;
        private int[] mActualDims = null;
        private int mActualFacing = 0;
        private int mActualFramesPerSec = 0;
        private CamFrameHandler mCamFrameHandler = null;
        int mCamId = 0;
        private Set<CameraListener> mCamListeners = new HashSet();
        private int mCamOrientation = 0;
        private int mCamRotation = 0;
        private Camera mCamera = null;
        private Condition mCameraReady = this.mCameraReadyLock.newCondition();
        private ReentrantLock mCameraReadyLock = new ReentrantLock(true);
        private MffContext mContext;
        private Display mDisplay = null;
        private LinkedBlockingQueue<Event> mEventQueue = new LinkedBlockingQueue(32);
        private ExternalCameraLock mExternalCameraLock = new ExternalCameraLock();
        private String mFlashMode = "off";
        private boolean mFlipFront = true;
        private int mOrientation = -1;
        private MediaRecorder mRecorder = null;
        private RenderTarget mRenderTarget;
        private int mRequestedFacing = 0;
        private int mRequestedFramesPerSec = 30;
        private int mRequestedPictureHeight = 480;
        private int mRequestedPictureWidth = 640;
        private int mRequestedPreviewHeight = 480;
        private int mRequestedPreviewWidth = 640;
        private State mState = new State();

        private class ExternalCameraLock {
            public static final int IDLE = 0;
            public static final int IN_USE = 1;
            private final Condition mInUseLockCondition;
            private final ReentrantLock mLock;
            private Object mLockContext;
            private int mLockState;

            private ExternalCameraLock() {
                this.mLockState = 0;
                this.mLock = new ReentrantLock(true);
                this.mInUseLockCondition = this.mLock.newCondition();
            }

            public boolean lock(Object context) {
                if (context == null) {
                    throw new RuntimeException("Null context when locking");
                }
                this.mLock.lock();
                if (this.mLockState == 1) {
                    try {
                        this.mInUseLockCondition.await();
                    } catch (InterruptedException e) {
                        return false;
                    }
                }
                this.mLockState = 1;
                this.mLockContext = context;
                this.mLock.unlock();
                return true;
            }

            public void unlock(Object context) {
                this.mLock.lock();
                if (this.mLockState != 1) {
                    throw new RuntimeException("Not in IN_USE state");
                } else if (context != this.mLockContext) {
                    throw new RuntimeException("Lock is not owned by this context");
                } else {
                    this.mLockState = 0;
                    this.mLockContext = null;
                    this.mInUseLockCondition.signal();
                    this.mLock.unlock();
                }
            }
        }

        public CameraRunnable(MffContext context) {
            this.mContext = context;
            createCamFrameHandler();
            this.mCamFrameHandler.initWithRunner(this);
            launchThread();
        }

        public MffContext getContext() {
            return this.mContext;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void loop() {
            /*
            r3 = this;
        L_0x0000:
            r1 = r3.nextEvent();	 Catch:{ Exception -> 0x0010 }
            if (r1 == 0) goto L_0x0000;
        L_0x0006:
            r2 = r1.code;	 Catch:{ Exception -> 0x0010 }
            switch(r2) {
                case 1: goto L_0x000c;
                case 2: goto L_0x0019;
                case 3: goto L_0x0015;
                case 4: goto L_0x001d;
                case 5: goto L_0x0021;
                case 6: goto L_0x0025;
                case 7: goto L_0x0029;
                default: goto L_0x000b;
            };	 Catch:{ Exception -> 0x0010 }
        L_0x000b:
            goto L_0x0000;
        L_0x000c:
            r3.onStart();	 Catch:{ Exception -> 0x0010 }
            goto L_0x0000;
        L_0x0010:
            r0 = move-exception;
            com.google.devtools.build.android.desugar.runtime.ThrowableExtension.printStackTrace(r0);
            goto L_0x0000;
        L_0x0015:
            r3.onStop();	 Catch:{ Exception -> 0x0010 }
            goto L_0x0000;
        L_0x0019:
            r3.onFrame();	 Catch:{ Exception -> 0x0010 }
            goto L_0x0000;
        L_0x001d:
            r3.onHalt();	 Catch:{ Exception -> 0x0010 }
            goto L_0x0000;
        L_0x0021:
            r3.onRestart();	 Catch:{ Exception -> 0x0010 }
            goto L_0x0000;
        L_0x0025:
            r3.onUpdate();	 Catch:{ Exception -> 0x0010 }
            goto L_0x0000;
        L_0x0029:
            r3.onTearDown();	 Catch:{ Exception -> 0x0010 }
            goto L_0x0000;
            */
            throw new UnsupportedOperationException("Method not decompiled: androidx.media.filterfw.CameraStreamer.CameraRunnable.loop():void");
        }

        public void run() {
            loop();
        }

        public void signalNewFrame() {
            pushEvent(2, false);
        }

        public void pushEvent(int eventId, boolean required) {
            if (required) {
                try {
                    this.mEventQueue.put(new Event(eventId));
                    return;
                } catch (InterruptedException e) {
                    Log.e("CameraStreamer", "Dropping event " + eventId + "!");
                    return;
                }
            }
            this.mEventQueue.offer(new Event(eventId));
        }

        public void launchThread() {
            new Thread(this).start();
        }

        @Deprecated
        public Camera getCamera() {
            Camera camera;
            synchronized (this.mState) {
                camera = this.mCamera;
            }
            return camera;
        }

        public Camera lockCamera(Object context) {
            this.mExternalCameraLock.lock(context);
            while (this.mCamera == null) {
                this.mExternalCameraLock.unlock(context);
                this.mCameraReadyLock.lock();
                try {
                    this.mCameraReady.await();
                    this.mCameraReadyLock.unlock();
                    this.mExternalCameraLock.lock(context);
                } catch (InterruptedException e) {
                    throw new RuntimeException("Condition interrupted", e);
                }
            }
            return this.mCamera;
        }

        public void unlockCamera(Object context) {
            this.mExternalCameraLock.unlock(context);
        }

        public int getCurrentCameraId() {
            int i;
            synchronized (this.mState) {
                i = this.mCamId;
            }
            return i;
        }

        public boolean isRunning() {
            return this.mState.current() != 2;
        }

        public void addListener(CameraListener listener) {
            synchronized (this.mCamListeners) {
                this.mCamListeners.add(listener);
            }
        }

        public void removeListener(CameraListener listener) {
            synchronized (this.mCamListeners) {
                this.mCamListeners.remove(listener);
            }
        }

        public synchronized void bindToDisplay(Display display) {
            this.mDisplay = display;
        }

        public synchronized void setDesiredPreviewSize(int width, int height) {
            if (!(width == this.mRequestedPreviewWidth && height == this.mRequestedPreviewHeight)) {
                this.mRequestedPreviewWidth = width;
                this.mRequestedPreviewHeight = height;
                onParamsUpdated();
            }
        }

        public synchronized void setDesiredPictureSize(int width, int height) {
            if (!(width == this.mRequestedPictureWidth && height == this.mRequestedPictureHeight)) {
                this.mRequestedPictureWidth = width;
                this.mRequestedPictureHeight = height;
                onParamsUpdated();
            }
        }

        public synchronized void setDesiredFrameRate(int fps) {
            if (fps != this.mRequestedFramesPerSec) {
                this.mRequestedFramesPerSec = fps;
                onParamsUpdated();
            }
        }

        public synchronized void setFacing(int facing) {
            if (facing != this.mRequestedFacing) {
                switch (facing) {
                    case 0:
                    case 1:
                    case 2:
                        this.mRequestedFacing = facing;
                        onParamsUpdated();
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown facing value '" + facing + "' passed to setFacing!");
                }
            }
        }

        public synchronized void setFlipFrontCamera(boolean flipFront) {
            if (this.mFlipFront != flipFront) {
                this.mFlipFront = flipFront;
            }
        }

        public synchronized void setFlashMode(String flashMode) {
            if (!flashMode.equals(this.mFlashMode)) {
                this.mFlashMode = flashMode;
                onParamsUpdated();
            }
        }

        public synchronized int getCameraFacing() {
            return this.mActualFacing;
        }

        public synchronized int getCameraRotation() {
            return this.mCamRotation;
        }

        public synchronized boolean supportsHardwareFaceDetection() {
            return true;
        }

        public synchronized int getCameraWidth() {
            int i = 0;
            synchronized (this) {
                if (this.mActualDims != null) {
                    i = this.mActualDims[0];
                }
            }
            return i;
        }

        public synchronized int getCameraHeight() {
            return this.mActualDims != null ? this.mActualDims[1] : 0;
        }

        public synchronized int getCameraFrameRate() {
            return this.mActualFramesPerSec;
        }

        public synchronized String getFlashMode() {
            return this.mCamera.getParameters().getFlashMode();
        }

        public synchronized boolean canStart() {
            boolean z;
            try {
                getCameraId();
                z = true;
            } catch (RuntimeException e) {
                z = false;
            }
            return z;
        }

        public boolean grabFrame(FrameImage2D targetFrame, int maxDim) {
            synchronized (this.mState) {
                if (this.mState.current() != 1) {
                    return false;
                } else if (this.mCamera == null) {
                    return false;
                } else {
                    this.mCamFrameHandler.grabFrame(targetFrame, maxDim);
                    return true;
                }
            }
        }

        public CamFrameHandler getCamFrameHandler() {
            return this.mCamFrameHandler;
        }

        private void onParamsUpdated() {
            pushEvent(6, true);
        }

        private Event nextEvent() {
            try {
                return (Event) this.mEventQueue.take();
            } catch (InterruptedException e) {
                Log.w("GraphRunner", "Event queue processing was interrupted.");
                return null;
            }
        }

        private void onStart() {
            if (this.mState.current() == 2) {
                this.mState.set(1);
                getRenderTarget().focus();
                openCamera();
                Iterator it = this.mCamFrameHandler.mClients.iterator();
                while (it.hasNext()) {
                    ((VideoFrameConsumer) it.next()).onVideoStreamStarted();
                }
            }
        }

        private void onStop() {
            if (this.mState.current() == 1) {
                closeCamera();
                RenderTarget.focusNone();
            }
            this.mState.set(2);
//            for (VideoFrameConsumer client : new ArrayList(this.mCamFrameHandler.mClients)) {
//                client.onVideoStreamStopped();
//            }
        }

        private void onHalt() {
            if (this.mState.current() == 1) {
                closeCamera();
                RenderTarget.focusNone();
                this.mState.set(3);
            }
        }

        private void onRestart() {
            if (this.mState.current() == 3) {
                this.mState.set(1);
                getRenderTarget().focus();
                openCamera();
            }
        }

        private void onUpdate() {
            if (this.mState.current() == 1) {
                pushEvent(3, true);
                pushEvent(1, true);
            }
        }

        private void onFrame() {
            if (this.mState.current() == 1) {
                updateRotation();
                this.mCamFrameHandler.updateServerFrame();
            }
        }

        private void onTearDown() {
            if (this.mState.current() == 2) {
                for (CameraListener listener : this.mCamListeners) {
                    removeListener(listener);
                }
                this.mCamListeners.clear();
                return;
            }
            Log.e("CameraStreamer", "Could not tear-down CameraStreamer as camera still seems to be running!");
        }

        private void createCamFrameHandler() {
            getContext().assertOpenGLSupported();
            if (VERSION.SDK_INT >= 16) {
                this.mCamFrameHandler = new CamFrameHandlerJB();
            } else if (VERSION.SDK_INT >= 15) {
                this.mCamFrameHandler = new CamFrameHandlerICS();
            } else {
                this.mCamFrameHandler = new CamFrameHandlerGB();
            }
        }

        private void updateRotation() {
            if (this.mDisplay != null) {
                updateDisplayRotation(this.mDisplay.getRotation());
            }
        }

        private synchronized void updateDisplayRotation(int rotation) {
            switch (rotation) {
                case 0:
                    onUpdateOrientation(0);
                    break;
                case 1:
                    onUpdateOrientation(90);
                    break;
                case 2:
                    onUpdateOrientation(MediaDecoder.ROTATE_180);
                    break;
                case 3:
                    onUpdateOrientation(MediaDecoder.ROTATE_90_LEFT);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported display rotation constant! Use one of the Surface.ROTATION_ constants!");
            }
        }

        private RenderTarget getRenderTarget() {
            if (this.mRenderTarget == null) {
                this.mRenderTarget = RenderTarget.newTarget(1, 1);
            }
            return this.mRenderTarget;
        }

        private void updateCamera() {
            synchronized (this.mState) {
                this.mCamId = getCameraId();
                updateCameraOrientation(this.mCamId);
                this.mCamera = Camera.open(this.mCamId);
                initCameraParameters();
            }
        }

        private void updateCameraOrientation(int camId) {
            int i = 1;
            CameraInfo cameraInfo = new CameraInfo();
            Camera.getCameraInfo(camId, cameraInfo);
            this.mCamOrientation = cameraInfo.orientation;
            this.mOrientation = -1;
            if (cameraInfo.facing != 1) {
                i = 2;
            }
            this.mActualFacing = i;
        }

        private int getCameraId() {
            int camCount = Camera.getNumberOfCameras();
            if (camCount == 0) {
                throw new RuntimeException("Device does not have any cameras!");
            } else if (this.mRequestedFacing == 0) {
                return 0;
            } else {
                boolean useFrontCam;
                if (this.mRequestedFacing == 1) {
                    useFrontCam = true;
                } else {
                    useFrontCam = false;
                }
                CameraInfo cameraInfo = new CameraInfo();
                for (int i = 0; i < camCount; i++) {
                    boolean z;
                    Camera.getCameraInfo(i, cameraInfo);
                    if (cameraInfo.facing == 1) {
                        z = true;
                    } else {
                        z = false;
                    }
                    if (z == useFrontCam) {
                        return i;
                    }
                }
                throw new RuntimeException("Could not find a camera facing (" + this.mRequestedFacing + ")!");
            }
        }

        private void initCameraParameters() {
            Parameters params = this.mCamera.getParameters();
            this.mActualDims = findClosestPreviewSize(this.mRequestedPreviewWidth, this.mRequestedPreviewHeight, params);
            this.mCamFrameHandler.setCameraSize(this.mActualDims[0], this.mActualDims[1]);
            params.setPreviewSize(this.mActualDims[0], this.mActualDims[1]);
            int[] dims = findClosestPictureSize(this.mRequestedPictureWidth, this.mRequestedPictureHeight, params);
            params.setPictureSize(dims[0], dims[1]);
            int[] closestRange = findClosestFpsRange(this.mRequestedFramesPerSec, params);
            params.setPreviewFpsRange(closestRange[0], closestRange[1]);
            if (params.getFlashMode() != null) {
                params.setFlashMode(this.mFlashMode);
            }
            this.mCamera.setParameters(params);
        }

        private int[] findClosestPreviewSize(int width, int height, Parameters parameters) {
            return findClosestSizeFromList(width, height, parameters.getSupportedPreviewSizes());
        }

        private int[] findClosestPictureSize(int width, int height, Parameters parameters) {
            return findClosestSizeFromList(width, height, parameters.getSupportedPictureSizes());
        }

        private int[] findClosestSizeFromList(int width, int height, List<Size> sizes) {
            int closestWidth = -1;
            int closestHeight = -1;
            int smallestWidth = ((Size) sizes.get(0)).width;
            int smallestHeight = ((Size) sizes.get(0)).height;
            for (Size size : sizes) {
                if (size.width <= width && size.height <= height && size.width >= closestWidth && size.height >= closestHeight) {
                    closestWidth = size.width;
                    closestHeight = size.height;
                }
                if (size.width < smallestWidth && size.height < smallestHeight) {
                    smallestWidth = size.width;
                    smallestHeight = size.height;
                }
            }
            if (closestWidth == -1) {
                closestWidth = smallestWidth;
                closestHeight = smallestHeight;
            }
            return new int[]{closestWidth, closestHeight};
        }

        private int[] findClosestFpsRange(int fps, Parameters params) {
            List<int[]> supportedFpsRanges = params.getSupportedPreviewFpsRange();
            int[] closestRange = (int[]) supportedFpsRanges.get(0);
            int fpsk = fps * 1000;
            int minDiff = 1000000;
            for (int[] range : supportedFpsRanges) {
                int low = range[0];
                int high = range[1];
                if (low <= fpsk && high >= fpsk) {
                    int diff = (fpsk - low) + (high - fpsk);
                    if (diff < minDiff) {
                        closestRange = range;
                        minDiff = diff;
                    }
                }
            }
            this.mActualFramesPerSec = closestRange[1] / 1000;
            return closestRange;
        }

        private void onUpdateOrientation(int orientation) {
            int rotation;
            if (this.mActualFacing == 1) {
                rotation = (this.mCamOrientation + orientation) % 360;
            } else {
                rotation = ((this.mCamOrientation - orientation) + 360) % 360;
            }
            if (rotation != this.mCamRotation) {
                synchronized (this) {
                    this.mCamRotation = rotation;
                }
            }
            int fixedOrientation = rotation;
            if (this.mActualFacing == 1 && this.mCamFrameHandler.isFrontMirrored()) {
                fixedOrientation = (360 - rotation) % 360;
            }
            if (this.mOrientation != fixedOrientation) {
                this.mOrientation = fixedOrientation;
                this.mCamFrameHandler.onUpdateCameraOrientation(this.mOrientation);
            }
        }

        private void openCamera() {
            try {
                if (CameraStreamer.mCameraLock.tryLock(5, TimeUnit.SECONDS)) {
                    Object lockContext = new Object();
                    this.mExternalCameraLock.lock(lockContext);
                    synchronized (this) {
                        updateCamera();
                        updateRotation();
                        this.mCamFrameHandler.setupServerFrame();
                    }
                    this.mCamera.startPreview();
                    synchronized (this.mCamListeners) {
                        for (CameraListener listener : this.mCamListeners) {
                            listener.onCameraOpened(CameraStreamer.this);
                        }
                    }
                    this.mExternalCameraLock.unlock(lockContext);
                    this.mCameraReadyLock.lock();
                    this.mCameraReady.signal();
                    this.mCameraReadyLock.unlock();
                    return;
                }
                throw new RuntimeException("Timed out while waiting to acquire camera!");
            } catch (InterruptedException e) {
                throw new RuntimeException("Interrupted while waiting to acquire camera!");
            }
        }

        public void createRecorder(String outputPath, CamcorderProfile profile) {
            lockCamera(this);
            this.mCamera.unlock();
            if (this.mRecorder != null) {
                this.mRecorder.release();
            }
            this.mRecorder = new MediaRecorder();
            this.mRecorder.setCamera(this.mCamera);
            this.mRecorder.setAudioSource(5);
            this.mRecorder.setVideoSource(1);
            this.mRecorder.setProfile(profile);
            this.mRecorder.setOutputFile(outputPath);
            try {
                this.mRecorder.prepare();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public void startRecording() {
            if (this.mRecorder == null) {
                throw new RuntimeException("No recorder created");
            }
            this.mRecorder.start();
        }

        public void stopRecording() {
            if (this.mRecorder == null) {
                throw new RuntimeException("No recorder created");
            }
            this.mRecorder.stop();
        }

        public void releaseRecorder() {
            if (this.mRecorder == null) {
                throw new RuntimeException("No recorder created");
            }
            this.mRecorder.release();
            this.mRecorder = null;
            this.mCamera.lock();
            unlockCamera(this);
        }

        private void closeCamera() {
            Object lockContext = new Object();
            this.mExternalCameraLock.lock(lockContext);
            if (this.mCamera != null) {
                this.mCamera.stopPreview();
                this.mCamera.release();
                this.mCamera = null;
            }
            CameraStreamer.mCameraLock.unlock();
            this.mCamFrameHandler.release();
            this.mExternalCameraLock.unlock(lockContext);
            synchronized (this.mCamListeners) {
                for (CameraListener listener : this.mCamListeners) {
                    listener.onCameraClosed(CameraStreamer.this);
                }
            }
        }
    }

    private static class Event {
        public static final int FRAME = 2;
        public static final int HALT = 4;
        public static final int RESTART = 5;
        public static final int START = 1;
        public static final int STOP = 3;
        public static final int TEARDOWN = 7;
        public static final int UPDATE = 6;
        public int code;

        public Event(int code) {
            this.code = code;
        }
    }

    private static class State {
        public static final int STATE_HALTED = 3;
        public static final int STATE_RUNNING = 1;
        public static final int STATE_STOPPED = 2;
        private AtomicInteger mCurrent;

        private State() {
            this.mCurrent = new AtomicInteger(2);
        }

        public int current() {
            return this.mCurrent.get();
        }

        public void set(int newState) {
            this.mCurrent.set(newState);
        }
    }

    @TargetApi(9)
    private final class CamFrameHandlerGB extends CamFrameHandler {
        final Object mBufferLock;
        private byte[] mFrameBufferBack;
        private byte[] mFrameBufferFront;
        private String mNV21ToRGBAFragment;
        private String mNV21ToRGBAVertex;
        private PreviewCallback mPreviewCallback;
        private SurfaceView mSurfaceView;
        private float[] mTargetCoords;
        private boolean mWriteToBack;

        private CamFrameHandlerGB() {
            super();
            this.mWriteToBack = true;
            this.mTargetCoords = new float[]{0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f};
            this.mBufferLock = new Object();
            this.mNV21ToRGBAFragment = "precision mediump float;\n\nuniform sampler2D tex_sampler_0;\nvarying vec2 v_y_texcoord;\nvarying vec2 v_vu_texcoord;\nvarying vec2 v_pixcoord;\n\nvec3 select(vec4 yyyy, vec4 vuvu, int s) {\n  if (s == 0) {\n    return vec3(yyyy.r, vuvu.g, vuvu.r);\n  } else if (s == 1) {\n    return vec3(yyyy.g, vuvu.g, vuvu.r);\n } else if (s == 2) {\n    return vec3(yyyy.b, vuvu.a, vuvu.b);\n  } else  {\n    return vec3(yyyy.a, vuvu.a, vuvu.b);\n  }\n}\n\nvec3 yuv2rgb(vec3 yuv) {\n  mat4 conversion = mat4(1.0,  0.0,    1.402, -0.701,\n                         1.0, -0.344, -0.714,  0.529,\n                         1.0,  1.772,  0.0,   -0.886,\n                         0, 0, 0, 0);  return (vec4(yuv, 1.0) * conversion).rgb;\n}\n\nvoid main() {\n  vec4 yyyy = texture2D(tex_sampler_0, v_y_texcoord);\n  vec4 vuvu = texture2D(tex_sampler_0, v_vu_texcoord);\n  int s = int(mod(floor(v_pixcoord.x), 4.0));\n  vec3 yuv = select(yyyy, vuvu, s);\n  vec3 rgb = yuv2rgb(yuv);\n  gl_FragColor = vec4(rgb, 1.0);\n}";
            this.mNV21ToRGBAVertex = "attribute vec4 a_position;\nattribute vec2 a_y_texcoord;\nattribute vec2 a_vu_texcoord;\nattribute vec2 a_pixcoord;\nvarying vec2 v_y_texcoord;\nvarying vec2 v_vu_texcoord;\nvarying vec2 v_pixcoord;\nvoid main() {\n  gl_Position = a_position;\n  v_y_texcoord = a_y_texcoord;\n  v_vu_texcoord = a_vu_texcoord;\n  v_pixcoord = a_pixcoord;\n}\n";
            this.mPreviewCallback = new PreviewCallback() {
                public void onPreviewFrame(byte[] data, Camera camera) {
                    CamFrameHandlerGB.this.swapBuffers();
                    camera.addCallbackBuffer(CamFrameHandlerGB.this.writeBuffer());
                    CamFrameHandlerGB.this.mRunner.signalNewFrame();
                }
            };
        }

        private byte[] readBuffer() {
            byte[] bArr;
            synchronized (this.mBufferLock) {
                bArr = this.mWriteToBack ? this.mFrameBufferFront : this.mFrameBufferBack;
            }
            return bArr;
        }

        private byte[] writeBuffer() {
            byte[] bArr;
            synchronized (this.mBufferLock) {
                bArr = this.mWriteToBack ? this.mFrameBufferBack : this.mFrameBufferFront;
            }
            return bArr;
        }

        private synchronized void swapBuffers() {
            synchronized (this.mBufferLock) {
                this.mWriteToBack = !this.mWriteToBack;
            }
        }

        public void setupServerFrame() {
            checkCameraDimensions();
            Camera camera = this.mRunner.mCamera;
            int bufferSize = this.mCameraWidth * (this.mCameraHeight + (this.mCameraHeight / 2));
            this.mFrameBufferFront = new byte[bufferSize];
            this.mFrameBufferBack = new byte[bufferSize];
            camera.addCallbackBuffer(writeBuffer());
            camera.setPreviewCallbackWithBuffer(this.mPreviewCallback);
            SurfaceView previewDisplay = getPreviewDisplay();
            if (previewDisplay != null) {
                try {
                    camera.setPreviewDisplay(previewDisplay.getHolder());
                } catch (IOException e) {
                    throw new RuntimeException("Could not start camera with given preview display!");
                }
            }
        }

        private void checkCameraDimensions() {
            if (this.mCameraWidth % 4 != 0) {
                throw new RuntimeException("Camera width must be a multiple of 4!");
            } else if (this.mCameraHeight % 2 != 0) {
                throw new RuntimeException("Camera height must be a multiple of 2!");
            }
        }

        public void updateServerFrame() {
            informClients();
        }

        public void grabFrame(FrameImage2D targetFrame, int maxDim) {
            EGLContext clientContext = RenderTarget.currentContext();
            TextureSource clientTex = textureForContext(clientContext);
            int texWidth = this.mCameraWidth / 4;
            int texHeight = this.mCameraHeight + (this.mCameraHeight / 2);
            synchronized (this.mBufferLock) {
                clientTex.allocateWithPixels(ByteBuffer.wrap(readBuffer()), texWidth, texHeight);
            }
            clientTex.setParameter(10240, 9728);
            clientTex.setParameter(10241, 9728);
            ImageShader transferShader = shaderForContext(clientContext);
            transferShader.setTargetCoords(this.mTargetCoords);
            updateShaderPixelSize(transferShader);
            targetFrame.resize(new int[]{this.mOutWidth, this.mOutHeight});
            transferShader.process(clientTex, targetFrame.lockRenderTarget(), this.mOutWidth, this.mOutHeight);
            targetFrame.unlock();
        }

        public void onUpdateCameraOrientation(int orientation) {
            super.onUpdateCameraOrientation(orientation);
            if (this.mRunner.mActualFacing == 1 && this.mRunner.mFlipFront) {
                switch (orientation) {
                    case 0:
                        this.mTargetCoords = new float[]{1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f};
                        return;
                    case 90:
                        this.mTargetCoords = new float[]{0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f};
                        return;
                    case MediaDecoder.ROTATE_180 /*180*/:
                        this.mTargetCoords = new float[]{0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f};
                        return;
                    case MediaDecoder.ROTATE_90_LEFT /*270*/:
                        this.mTargetCoords = new float[]{1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f};
                        return;
                    default:
                        return;
                }
            }
            switch (orientation) {
                case 0:
                    this.mTargetCoords = new float[]{0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f};
                    return;
                case 90:
                    this.mTargetCoords = new float[]{1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f};
                    return;
                case MediaDecoder.ROTATE_180 /*180*/:
                    this.mTargetCoords = new float[]{1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f};
                    return;
                case MediaDecoder.ROTATE_90_LEFT /*270*/:
                    this.mTargetCoords = new float[]{0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f};
                    return;
                default:
                    return;
            }
        }

        public void release() {
            this.mFrameBufferBack = null;
            this.mFrameBufferFront = null;
        }

        public boolean isFrontMirrored() {
            return false;
        }

        protected ImageShader createClientShader() {
            ImageShader shader = new ImageShader(this.mNV21ToRGBAVertex, this.mNV21ToRGBAFragment);
            float[] uvCoords = new float[]{0.0f, 0.6666667f, 1.0f, 0.6666667f, 0.0f, 1.0f, 1.0f, 1.0f};
            shader.setAttributeValues("a_y_texcoord", new float[]{0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.6666667f, 1.0f, 0.6666667f}, 2);
            shader.setAttributeValues("a_vu_texcoord", uvCoords, 2);
            return shader;
        }

        protected TextureSource createClientTexture() {
            TextureSource texture = TextureSource.newTexture();
            texture.setParameter(10240, 9728);
            texture.setParameter(10241, 9728);
            return texture;
        }

        private void updateShaderPixelSize(ImageShader shader) {
            shader.setAttributeValues("a_pixcoord", new float[]{0.0f, 0.0f, (float) this.mCameraWidth, 0.0f, 0.0f, (float) this.mCameraHeight, (float) this.mCameraWidth, (float) this.mCameraHeight}, 2);
        }

        private SurfaceView getPreviewDisplay() {
            if (this.mSurfaceView == null) {
                this.mSurfaceView = this.mRunner.getContext().getDummySurfaceView();
            }
            return this.mSurfaceView;
        }

        private void informClients() {
            synchronized (this.mClients) {
                Iterator it = this.mClients.iterator();
                while (it.hasNext()) {
                    ((VideoFrameConsumer) it.next()).onVideoFrameAvailable(CameraStreamer.this, 0);
                }
            }
        }
    }

    @TargetApi(15)
    private class CamFrameHandlerICS extends CamFrameHandler {
        protected static final String mCopyShaderSource = "#extension GL_OES_EGL_image_external : require\nprecision mediump float;\nuniform samplerExternalOES tex_sampler_0;\nvarying vec2 v_texcoord;\nvoid main() {\n  gl_FragColor = texture2D(tex_sampler_0, v_texcoord);\n}\n";
        private float[] mCameraTransform;
        protected HashMap<SurfaceTexture, RenderTarget> mClientRenderTargets;
        protected ImageShader mCopyShader;
        protected OnFrameAvailableListener mOnCameraFrameListener;
        protected SurfaceTexture mPreviewSurfaceTexture;
        protected TextureSource mPreviewTexture;
        protected HashMap<EGLContext, SurfaceTexture> mTargetSurfaceTextures;

        private CamFrameHandlerICS() {
            super();
            this.mCameraTransform = new float[16];
            this.mPreviewTexture = null;
            this.mPreviewSurfaceTexture = null;
            this.mTargetSurfaceTextures = new HashMap();
            this.mClientRenderTargets = new HashMap();
            this.mCopyShader = null;
            this.mOnCameraFrameListener = new OnFrameAvailableListener() {
                public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                    CamFrameHandlerICS.this.mRunner.signalNewFrame();
                }
            };
        }

        public synchronized void setupServerFrame() {
            setupPreviewTexture(this.mRunner.mCamera);
        }

        public synchronized void updateServerFrame() {
            this.mPreviewSurfaceTexture.updateTexImage();
            distributeFrames();
        }

        public void onUpdateCameraOrientation(int orientation) {
            super.onUpdateCameraOrientation(orientation);
            this.mRunner.mCamera.setDisplayOrientation(orientation);
            updateSurfaceTextureSizes();
        }

        public synchronized void onRegisterClient(VideoFrameConsumer client, EGLContext context) {
            final Set<VideoFrameConsumer> clientTargets = clientsForContext(context);
            TextureSource clientTex = textureForContext(context);
            ImageShader copyShader = shaderForContext(context);
            surfaceTextureForContext(context).setOnFrameAvailableListener(new OnFrameAvailableListener() {
                public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                    long timestampNs = CamFrameHandlerICS.this.mPreviewSurfaceTexture.getTimestamp();
                    for (VideoFrameConsumer clientTarget : clientTargets) {
                        clientTarget.onVideoFrameAvailable(CameraStreamer.this, timestampNs);
                    }
                }
            });
        }

        public synchronized void grabFrame(FrameImage2D targetFrame, int maxDim) {
            EGLContext clientContext = RenderTarget.currentContext();
            TextureSource clientTex = textureForContext(clientContext);
            ImageShader copyShader = shaderForContext(clientContext);
            SurfaceTexture surfTex = surfaceTextureForContext(clientContext);
            if (clientTex == null || copyShader == null || surfTex == null) {
                String valueOf = String.valueOf(Thread.currentThread());
                throw new RuntimeException(new StringBuilder(String.valueOf(valueOf).length() + 54).append("Attempting to grab camera frame from unknown thread: ").append(valueOf).append("!").toString());
            }
            surfTex.updateTexImage();
            int[] dimensions = ScaleUtils.scaleDown(this.mOutWidth, this.mOutHeight, maxDim);
            targetFrame.resize(dimensions);
            copyShader.process(clientTex, targetFrame.lockRenderTarget(), dimensions[0], dimensions[1]);
            targetFrame.setTimestamp(this.mPreviewSurfaceTexture.getTimestamp());
            targetFrame.unlock();
        }

        public synchronized void release() {
            if (this.mPreviewTexture != null) {
                this.mPreviewTexture.release();
                this.mPreviewTexture = null;
            }
            if (this.mPreviewSurfaceTexture != null) {
                this.mPreviewSurfaceTexture.release();
                this.mPreviewSurfaceTexture = null;
            }
        }

        protected ImageShader createClientShader() {
            return new ImageShader(mCopyShaderSource);
        }

        protected TextureSource createClientTexture() {
            return TextureSource.newExternalTexture();
        }

        protected void distributeFrames() {
            updateTransform(getCopyShader());
            updateShaderTargetRect(getCopyShader());
            for (SurfaceTexture clientTexture : this.mTargetSurfaceTextures.values()) {
                RenderTarget clientTarget = renderTargetFor(clientTexture);
                clientTarget.focus();
                getCopyShader().process(this.mPreviewTexture, clientTarget, this.mOutWidth, this.mOutHeight);
                GLToolbox.checkGlError("distribute frames");
                clientTarget.swapBuffers();
            }
        }

        protected RenderTarget renderTargetFor(SurfaceTexture surfaceTex) {
            RenderTarget target = (RenderTarget) this.mClientRenderTargets.get(surfaceTex);
            if (target != null) {
                return target;
            }
            target = RenderTarget.currentTarget().forSurfaceTexture(surfaceTex);
            this.mClientRenderTargets.put(surfaceTex, target);
            return target;
        }

        protected void setupPreviewTexture(Camera camera) {
            if (this.mPreviewTexture == null) {
                this.mPreviewTexture = TextureSource.newExternalTexture();
            }
            if (this.mPreviewSurfaceTexture == null) {
                this.mPreviewSurfaceTexture = new SurfaceTexture(this.mPreviewTexture.getTextureId());
                try {
                    camera.setPreviewTexture(this.mPreviewSurfaceTexture);
                    this.mPreviewSurfaceTexture.setOnFrameAvailableListener(this.mOnCameraFrameListener);
                } catch (IOException e) {
                    String message = e.getMessage();
                    throw new RuntimeException(new StringBuilder(String.valueOf(message).length() + 40).append("Could not bind camera surface texture: ").append(message).append("!").toString());
                }
            }
        }

        protected ImageShader getCopyShader() {
            if (this.mCopyShader == null) {
                this.mCopyShader = new ImageShader(mCopyShaderSource);
            }
            return this.mCopyShader;
        }

        protected SurfaceTexture surfaceTextureForContext(EGLContext context) {
            SurfaceTexture surfTex = (SurfaceTexture) this.mTargetSurfaceTextures.get(context);
            if (surfTex != null) {
                return surfTex;
            }
            TextureSource texture = textureForContext(context);
            if (texture == null) {
                return surfTex;
            }
            surfTex = new SurfaceTexture(texture.getTextureId());
            surfTex.setDefaultBufferSize(this.mOutWidth, this.mOutHeight);
            this.mTargetSurfaceTextures.put(context, surfTex);
            return surfTex;
        }

        protected void updateShaderTargetRect(ImageShader shader) {
            if (this.mRunner.mActualFacing == 1 && this.mRunner.mFlipFront) {
                shader.setTargetRect(1.0f, 0.0f, -1.0f, 1.0f);
            } else {
                shader.setTargetRect(0.0f, 0.0f, 1.0f, 1.0f);
            }
        }

        protected synchronized void updateSurfaceTextureSizes() {
            for (SurfaceTexture clientTexture : this.mTargetSurfaceTextures.values()) {
                clientTexture.setDefaultBufferSize(this.mOutWidth, this.mOutHeight);
            }
        }

        protected void updateTransform(ImageShader shader) {
            this.mPreviewSurfaceTexture.getTransformMatrix(this.mCameraTransform);
            shader.setSourceTransform(this.mCameraTransform);
        }

        protected void onCleanupContext(EGLContext context) {
            super.onCleanupContext(context);
            SurfaceTexture surfaceTex = (SurfaceTexture) this.mTargetSurfaceTextures.get(context);
            if (surfaceTex != null) {
                surfaceTex.release();
                this.mTargetSurfaceTextures.remove(context);
            }
        }
    }

    @TargetApi(16)
    private class CamFrameHandlerJB extends CamFrameHandlerICS {
        private CamFrameHandlerJB() {
            super();
        }

        public void setupServerFrame() {
            setupPreviewTexture(this.mRunner.mCamera);
        }

        public synchronized void updateServerFrame() {
            updateSurfaceTexture();
            informClients();
        }

        public synchronized void grabFrame(FrameImage2D targetFrame, int maxDim) {
            TextureSource targetTex = TextureSource.newExternalTexture();
            ImageShader copyShader = shaderForContext(RenderTarget.currentContext());
            if (targetTex == null || copyShader == null) {
                String valueOf = String.valueOf(Thread.currentThread());
                throw new RuntimeException(new StringBuilder(String.valueOf(valueOf).length() + 54).append("Attempting to grab camera frame from unknown thread: ").append(valueOf).append("!").toString());
            }
            this.mPreviewSurfaceTexture.attachToGLContext(targetTex.getTextureId());
            updateTransform(copyShader);
            updateShaderTargetRect(copyShader);
            int[] dimensions = ScaleUtils.scaleDown(this.mOutWidth, this.mOutHeight, maxDim);
            targetFrame.resize(dimensions);
            copyShader.process(targetTex, targetFrame.lockRenderTarget(), dimensions[0], dimensions[1]);
            targetFrame.setTimestamp(this.mPreviewSurfaceTexture.getTimestamp());
            targetFrame.unlock();
            this.mPreviewSurfaceTexture.detachFromGLContext();
            targetTex.release();
        }

        protected void updateShaderTargetRect(ImageShader shader) {
            if (this.mRunner.mActualFacing == 1 && this.mRunner.mFlipFront) {
                shader.setTargetRect(1.0f, 1.0f, -1.0f, -1.0f);
            } else {
                shader.setTargetRect(0.0f, 1.0f, 1.0f, -1.0f);
            }
        }

        public void onRegisterClient(VideoFrameConsumer client, EGLContext context) {
        }

        protected void setupPreviewTexture(Camera camera) {
            super.setupPreviewTexture(camera);
            this.mPreviewSurfaceTexture.detachFromGLContext();
        }

        protected void updateSurfaceTexture() {
            this.mPreviewSurfaceTexture.attachToGLContext(this.mPreviewTexture.getTextureId());
            this.mPreviewSurfaceTexture.updateTexImage();
            this.mPreviewSurfaceTexture.detachFromGLContext();
        }

        protected void informClients() {
            synchronized (this.mClients) {
                long timestampNs = this.mPreviewSurfaceTexture.getTimestamp();
                Iterator it = this.mClients.iterator();
                while (it.hasNext()) {
                    ((VideoFrameConsumer) it.next()).onVideoFrameAvailable(CameraStreamer.this, timestampNs);
                }
            }
        }
    }

    public void updateDisplayRotation(int rotation) {
        this.mCameraRunner.updateDisplayRotation(rotation);
    }

    public void bindToDisplay(Display display) {
        this.mCameraRunner.bindToDisplay(display);
    }

    public void setDesiredPreviewSize(int width, int height) {
        this.mCameraRunner.setDesiredPreviewSize(width, height);
    }

    public void setDesiredPictureSize(int width, int height) {
        this.mCameraRunner.setDesiredPictureSize(width, height);
    }

    public void setDesiredFrameRate(int fps) {
        this.mCameraRunner.setDesiredFrameRate(fps);
    }

    public void setFacing(int facing) {
        this.mCameraRunner.setFacing(facing);
    }

    public void setFlipFrontCamera(boolean flipFront) {
        this.mCameraRunner.setFlipFrontCamera(flipFront);
    }

    public void setFlashMode(String flashMode) {
        this.mCameraRunner.setFlashMode(flashMode);
    }

    public String getFlashMode() {
        return this.mCameraRunner.getFlashMode();
    }

    public int getCameraFacing() {
        return this.mCameraRunner.getCameraFacing();
    }

    public int getCameraRotation() {
        return this.mCameraRunner.getCameraRotation();
    }

    public boolean supportsHardwareFaceDetection() {
        return this.mCameraRunner.supportsHardwareFaceDetection();
    }

    public static int getDefaultFacing() {
        if (Camera.getNumberOfCameras() == 0) {
            return 0;
        }
        CameraInfo cameraInfo = new CameraInfo();
        Camera.getCameraInfo(0, cameraInfo);
        if (cameraInfo.facing != 1) {
            return 2;
        }
        return 1;
    }

    public int getCameraWidth() {
        return this.mCameraRunner.getCameraWidth();
    }

    public int getCameraHeight() {
        return this.mCameraRunner.getCameraHeight();
    }

    public int getCameraFrameRate() {
        return this.mCameraRunner.getCameraFrameRate();
    }

    public boolean canStart() {
        return this.mCameraRunner.canStart();
    }

    public boolean isRunning() {
        return this.mCameraRunner.isRunning();
    }

    public void start() {
        this.mCameraRunner.pushEvent(1, true);
    }

    public void stop() {
        this.mCameraRunner.pushEvent(3, true);
    }

    public long getDurationNs() {
        return Long.MAX_VALUE;
    }

    public void stopAndWait() {
        this.mCameraRunner.pushEvent(3, true);
        try {
            if (!mCameraLock.tryLock(5, TimeUnit.SECONDS)) {
                Log.w("CameraStreamer", "Time-out waiting for camera to close!");
            }
        } catch (InterruptedException e) {
            Log.w("CameraStreamer", "Interrupted while waiting for camera to close!");
        }
        mCameraLock.unlock();
    }

    public void addListener(CameraListener listener) {
        this.mCameraRunner.addListener(listener);
    }

    public void removeListener(CameraListener listener) {
        this.mCameraRunner.removeListener(listener);
    }

    public boolean getLatestFrame(FrameImage2D targetFrame, int maxDim) {
        return this.mCameraRunner.grabFrame(targetFrame, maxDim);
    }

    public void skipVideoFrame() {
    }

    public boolean grabVideoFrame(FrameImage2D outputVideoFrame, FrameValue infoFrame, int maxDim) {
        if (!this.mCameraRunner.grabFrame(outputVideoFrame, maxDim)) {
            return false;
        }
        if (infoFrame != null) {
            infoFrame.setValue(new VideoFrameInfo(false));
        }
        return true;
    }

    public void addVideoFrameConsumer(VideoFrameConsumer consumer) {
        this.mCameraRunner.getCamFrameHandler().registerClient(consumer);
    }

    public void removeVideoFrameConsumer(VideoFrameConsumer consumer) {
        this.mCameraRunner.getCamFrameHandler().unregisterClient(consumer);
    }

    @Deprecated
    public Camera getCamera() {
        return this.mCameraRunner.getCamera();
    }

    public Camera lockCamera(Object context) {
        return this.mCameraRunner.lockCamera(context);
    }

    public void unlockCamera(Object context) {
        this.mCameraRunner.unlockCamera(context);
    }

    public void createRecorder(String path, CamcorderProfile profile) {
        this.mCameraRunner.createRecorder(path, profile);
    }

    public void releaseRecorder() {
        this.mCameraRunner.releaseRecorder();
    }

    public void startRecording() {
        this.mCameraRunner.startRecording();
    }

    public void stopRecording() {
        this.mCameraRunner.stopRecording();
    }

    public int getCameraId() {
        return this.mCameraRunner.getCurrentCameraId();
    }

    public static int getNumberOfCameras() {
        return Camera.getNumberOfCameras();
    }

    CameraStreamer(MffContext context) {
        this.mCameraRunner = new CameraRunnable(context);
    }

    void halt() {
        this.mCameraRunner.pushEvent(4, true);
    }

    void restart() {
        this.mCameraRunner.pushEvent(5, true);
    }

    static boolean requireDummySurfaceView() {
        return VERSION.SDK_INT < 15;
    }

    void tearDown() {
        this.mCameraRunner.pushEvent(7, true);
    }
}
