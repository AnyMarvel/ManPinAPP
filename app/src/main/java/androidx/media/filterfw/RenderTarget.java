package androidx.media.filterfw;

import android.annotation.TargetApi;
import android.graphics.SurfaceTexture;
import android.media.MediaRecorder;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.os.Build.VERSION;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import com.google.android.apps.photolab.storyboard.activity.ComicActivity;
import com.mp.android.apps.StoryboardActivity;

import java.nio.ByteBuffer;
import java.util.HashMap;
import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

public final class RenderTarget {
    private static final int EGL_CONTEXT_CLIENT_VERSION = 12440;
    private static final int EGL_OPENGL_ES2_BIT = 4;
    private static EGLDisplay mConfiguredDisplay;
    private static ThreadLocal<RenderTarget> mCurrentTarget = new ThreadLocal();
    private static HashMap<EGLContext, EGLSurface> mDisplaySurfaces = new HashMap();
    private static EGLConfig mEglConfig = null;
    private static HashMap<EGLContext, ImageShader> mExternalIdShaders = new HashMap();
    private static HashMap<EGLContext, ImageShader> mIdShaders = new HashMap();
    private static ThreadLocal<RenderTarget> mMainTextureTarget = new ThreadLocal();
    private static HashMap<Object, Integer> mRefCounts = new HashMap();
    private static boolean mSupportsMultipleDisplaySurfaces = (VERSION.SDK_INT >= 11);
    private static HashMap<Object, EGLSurface> mSurfaceSources = new HashMap();
    private static int sAlphaSize = 8;
    private static int sBlueSize = 8;
    private static int sDepthSize = 0;
    private static int sGreenSize = 8;
    private static int sRedSize = 8;
    private static int sStencilSize = 0;
    private EGLContext mContext;
    private EGLDisplay mDisplay;
    private EGL10 mEgl = ((EGL10) EGLContext.getEGL());
    private int mFbo;
    private boolean mOwnsContext;
    private boolean mOwnsSurface;
    private EGLSurface mSurface;
    private Object mSurfaceSource = null;

    public static RenderTarget newTarget(int width, int height) {
        EGL10 egl = (EGL10) EGLContext.getEGL();
        EGLDisplay eglDisplay = createDefaultDisplay(egl);
        EGLContext eglContext = createContext(egl, eglDisplay, chooseEglConfig(egl, eglDisplay));
        EGLSurface eglSurface = createSurface(egl, eglDisplay, width, height);
        RenderTarget result = new RenderTarget(eglDisplay, eglContext, eglSurface, 0, true, true);
        result.addReferenceTo(eglSurface);
        return result;
    }

    public static RenderTarget currentTarget() {
        return (RenderTarget) mCurrentTarget.get();
    }

    public static void setMainTextureTarget(RenderTarget renderTarget) {
        mMainTextureTarget.set(renderTarget);
    }

    public static RenderTarget forTexture(TextureSource texture, int width, int height) {
        RenderTarget mainTarget = (RenderTarget) mMainTextureTarget.get();
        if (mainTarget == null) {
            throw new RuntimeException("Can't call forTexture() without main target set.");
        }
        mainTarget.focus();
        int fbo = GLToolbox.generateFbo();
        GLES20.glBindFramebuffer(36160, fbo);
        GLToolbox.checkGlError("glBindFramebuffer");
        GLES20.glFramebufferTexture2D(36160, 36064, texture.getTarget(), texture.getTextureId(), 0);
        GLToolbox.checkGlError("glFramebufferTexture2D");
        return new RenderTarget(mainTarget.mDisplay, mainTarget.mContext, mainTarget.surface(), fbo, false, false);
    }

    public RenderTarget forSurfaceHolder(SurfaceHolder surfaceHolder) {
        EGLSurface eglSurf;
        EGLConfig eglConfig = chooseEglConfig(this.mEgl, this.mDisplay);
        synchronized (mSurfaceSources) {
            eglSurf = (EGLSurface) mSurfaceSources.get(surfaceHolder);
            if (eglSurf == null) {
                eglSurf = this.mEgl.eglCreateWindowSurface(this.mDisplay, eglConfig, surfaceHolder, null);
                mSurfaceSources.put(surfaceHolder, eglSurf);
            }
        }
        checkEglError(this.mEgl, "eglCreateWindowSurface");
        checkSurface(this.mEgl, eglSurf);
        RenderTarget result = new RenderTarget(this.mDisplay, this.mContext, eglSurf, 0, false, true);
        result.addReferenceTo(eglSurf);
        result.setSurfaceSource(surfaceHolder);
        return result;
    }

    @TargetApi(11)
    public RenderTarget forSurfaceTexture(SurfaceTexture surfaceTexture) {
        EGLSurface eglSurf;
        EGLConfig eglConfig = chooseEglConfig(this.mEgl, this.mDisplay);
        synchronized (mSurfaceSources) {
            eglSurf = (EGLSurface) mSurfaceSources.get(surfaceTexture);
            if (eglSurf == null) {
                eglSurf = this.mEgl.eglCreateWindowSurface(this.mDisplay, eglConfig, surfaceTexture, null);
                mSurfaceSources.put(surfaceTexture, eglSurf);
            }
        }
        checkEglError(this.mEgl, "eglCreateWindowSurface");
        checkSurface(this.mEgl, eglSurf);
        RenderTarget result = new RenderTarget(this.mDisplay, this.mContext, eglSurf, 0, false, true);
        result.setSurfaceSource(surfaceTexture);
        result.addReferenceTo(eglSurf);
        return result;
    }

    @TargetApi(11)
    public RenderTarget forSurface(Surface surface) {
        EGLSurface eglSurf;
        EGLConfig eglConfig = chooseEglConfig(this.mEgl, this.mDisplay);
        synchronized (mSurfaceSources) {
            eglSurf = (EGLSurface) mSurfaceSources.get(surface);
            if (eglSurf == null) {
                eglSurf = this.mEgl.eglCreateWindowSurface(this.mDisplay, eglConfig, surface, null);
                mSurfaceSources.put(surface, eglSurf);
            }
        }
        checkEglError(this.mEgl, "eglCreateWindowSurface");
        checkSurface(this.mEgl, eglSurf);
        RenderTarget result = new RenderTarget(this.mDisplay, this.mContext, eglSurf, 0, false, true);
        result.setSurfaceSource(surface);
        result.addReferenceTo(eglSurf);
        return result;
    }

    public static RenderTarget forMediaRecorder(MediaRecorder mediaRecorder) {
        throw new RuntimeException("Not yet implemented MediaRecorder -> RenderTarget!");
    }

    public static void setEGLConfigChooser(int redSize, int greenSize, int blueSize, int alphaSize, int depthSize, int stencilSize) {
        sRedSize = redSize;
        sGreenSize = greenSize;
        sBlueSize = blueSize;
        sAlphaSize = alphaSize;
        sDepthSize = depthSize;
        sStencilSize = stencilSize;
    }

    public void registerAsDisplaySurface() {
        if (!mSupportsMultipleDisplaySurfaces) {
            EGLSurface currentSurface = (EGLSurface) mDisplaySurfaces.get(this.mContext);
            if (currentSurface == null || currentSurface.equals(this.mSurface)) {
                mDisplaySurfaces.put(this.mContext, this.mSurface);
                return;
            }
            throw new RuntimeException("This device supports only a single display surface!");
        }
    }

    public void unregisterAsDisplaySurface() {
        if (!mSupportsMultipleDisplaySurfaces) {
            mDisplaySurfaces.put(this.mContext, null);
        }
    }

    public void focus() {
        if (((RenderTarget) mCurrentTarget.get()) != this) {
            this.mEgl.eglMakeCurrent(this.mDisplay, surface(), surface(), this.mContext);
            mCurrentTarget.set(this);
        }
        if (getCurrentFbo() != this.mFbo) {
            GLES20.glBindFramebuffer(36160, this.mFbo);
            GLToolbox.checkGlError("glBindFramebuffer");
        }
    }

    public static void focusNone() {
        EGL10 egl = (EGL10) EGLContext.getEGL();
        egl.eglMakeCurrent(egl.eglGetCurrentDisplay(), EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
        mCurrentTarget.set(null);
        checkEglError(egl, "eglMakeCurrent");
    }

    public void swapBuffers() {
        this.mEgl.eglSwapBuffers(this.mDisplay, surface());
    }

    public EGLContext getContext() {
        return this.mContext;
    }

    public static EGLContext currentContext() {
        RenderTarget current = currentTarget();
        return current != null ? current.getContext() : EGL10.EGL_NO_CONTEXT;
    }

    public void release() {
        if (this.mOwnsContext) {
            this.mEgl.eglDestroyContext(this.mDisplay, this.mContext);
            this.mContext = EGL10.EGL_NO_CONTEXT;
            this.mEgl.eglTerminate(this.mDisplay);
        }
        if (this.mOwnsSurface) {
            synchronized (mSurfaceSources) {
                if (removeReferenceTo(this.mSurface)) {
                    this.mEgl.eglDestroySurface(this.mDisplay, this.mSurface);
                    this.mSurface = EGL10.EGL_NO_SURFACE;
                    mSurfaceSources.remove(this.mSurfaceSource);
                }
            }
        }
        if (this.mFbo != 0) {
            GLToolbox.deleteFbo(this.mFbo);
        }
    }

    public void readPixelData(ByteBuffer pixels, int width, int height) {
        GLToolbox.readTarget(this, pixels, width, height);
    }

    public ByteBuffer getPixelData(int width, int height) {
        ByteBuffer pixels = ByteBuffer.allocateDirect((width * height) * 4);
        GLToolbox.readTarget(this, pixels, width, height);
        return pixels;
    }

    public ImageShader getIdentityShader() {
        ImageShader idShader = (ImageShader) mIdShaders.get(this.mContext);
        if (idShader != null) {
            return idShader;
        }
        idShader = ImageShader.createIdentity();
        mIdShaders.put(this.mContext, idShader);
        return idShader;
    }

    public ImageShader getExternalIdentityShader() {
        ImageShader idShader = (ImageShader) mExternalIdShaders.get(this.mContext);
        if (idShader != null) {
            return idShader;
        }
        idShader = ImageShader.createExternalIdentity();
        mExternalIdShaders.put(this.mContext, idShader);
        return idShader;
    }

    public String toString() {
        String valueOf = String.valueOf(this.mDisplay);
        String valueOf2 = String.valueOf(this.mContext);
        String valueOf3 = String.valueOf(this.mSurface);
        return new StringBuilder(((String.valueOf(valueOf).length() + 31) + String.valueOf(valueOf2).length()) + String.valueOf(valueOf3).length()).append("RenderTarget(").append(valueOf).append(", ").append(valueOf2).append(", ").append(valueOf3).append(", ").append(this.mFbo).append(")").toString();
    }

    private void setSurfaceSource(Object source) {
        this.mSurfaceSource = source;
    }

    private void addReferenceTo(Object object) {
        Integer refCount = (Integer) mRefCounts.get(object);
        if (refCount != null) {
            mRefCounts.put(object, Integer.valueOf(refCount.intValue() + 1));
        } else {
            mRefCounts.put(object, Integer.valueOf(1));
        }
    }

    private boolean removeReferenceTo(Object object) {
        Integer refCount = (Integer) mRefCounts.get(object);
        if (refCount == null || refCount.intValue() <= 0) {
            String valueOf = String.valueOf(object);
            Log.e("RenderTarget", new StringBuilder(String.valueOf(valueOf).length() + 41).append("Removing reference of already released: ").append(valueOf).append("!").toString());
            return false;
        }
        refCount = Integer.valueOf(refCount.intValue() - 1);
        mRefCounts.put(object, refCount);
        if (refCount.intValue() == 0) {
            return true;
        }
        return false;
    }

    private static EGLConfig chooseEglConfig(EGL10 egl, EGLDisplay display) {
        if (mEglConfig == null || !display.equals(mConfiguredDisplay)) {
            int[] configsCount = new int[1];
            EGLConfig[] configs = new EGLConfig[1];
            if (!egl.eglChooseConfig(display, getDesiredConfig(), configs, 1, configsCount)) {
                String str = "EGL Error: eglChooseConfig failed ";
                String valueOf = String.valueOf(getEGLErrorString(egl));
                throw new IllegalArgumentException(valueOf.length() != 0 ? str.concat(valueOf) : new String(str));
            } else if (configsCount[0] > 0) {
                mEglConfig = configs[0];
                mConfiguredDisplay = display;
            }
        }
        return mEglConfig;
    }

    private static int[] getDesiredConfig() {
        return new int[]{12352, 4, 12324, sRedSize, 12323, sGreenSize, ComicActivity.SHARE_IMAGE_REQUEST, sBlueSize, StoryboardActivity.PICK_VIDEO_REQUEST, sAlphaSize, 12325, sDepthSize, 12326, sStencilSize, 12344};
    }

    private RenderTarget(EGLDisplay display, EGLContext context, EGLSurface surface, int fbo, boolean ownsContext, boolean ownsSurface) {
        this.mDisplay = display;
        this.mContext = context;
        this.mSurface = surface;
        this.mFbo = fbo;
        this.mOwnsContext = ownsContext;
        this.mOwnsSurface = ownsSurface;
    }

    private EGLSurface surface() {
        if (mSupportsMultipleDisplaySurfaces) {
            return this.mSurface;
        }
        EGLSurface displaySurface = (EGLSurface) mDisplaySurfaces.get(this.mContext);
        return displaySurface == null ? this.mSurface : displaySurface;
    }

    private static void initEgl(EGL10 egl, EGLDisplay display) {
        if (!egl.eglInitialize(display, new int[2])) {
            String str = "EGL Error: eglInitialize failed ";
            String valueOf = String.valueOf(getEGLErrorString(egl));
            throw new RuntimeException(valueOf.length() != 0 ? str.concat(valueOf) : new String(str));
        }
    }

    private static EGLDisplay createDefaultDisplay(EGL10 egl) {
        EGLDisplay display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        checkDisplay(egl, display);
        initEgl(egl, display);
        return display;
    }

    private static EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig config) {
        EGLContext ctxt = egl.eglCreateContext(display, config, EGL10.EGL_NO_CONTEXT, new int[]{EGL_CONTEXT_CLIENT_VERSION, 2, 12344});
        checkContext(egl, ctxt);
        return ctxt;
    }

    private static EGLSurface createSurface(EGL10 egl, EGLDisplay display, int width, int height) {
        return egl.eglCreatePbufferSurface(display, chooseEglConfig(egl, display), new int[]{12375, width, 12374, height, 12344});
    }

    private static int getCurrentFbo() {
        int[] result = new int[1];
        GLES20.glGetIntegerv(36006, result, 0);
        return result[0];
    }

    private static void checkDisplay(EGL10 egl, EGLDisplay display) {
        if (display == EGL10.EGL_NO_DISPLAY) {
            String str = "EGL Error: Bad display: ";
            String valueOf = String.valueOf(getEGLErrorString(egl));
            throw new RuntimeException(valueOf.length() != 0 ? str.concat(valueOf) : new String(str));
        }
    }

    private static void checkContext(EGL10 egl, EGLContext context) {
        if (context == EGL10.EGL_NO_CONTEXT) {
            String str = "EGL Error: Bad context: ";
            String valueOf = String.valueOf(getEGLErrorString(egl));
            throw new RuntimeException(valueOf.length() != 0 ? str.concat(valueOf) : new String(str));
        }
    }

    private static void checkSurface(EGL10 egl, EGLSurface surface) {
        if (surface == EGL10.EGL_NO_SURFACE) {
            String str = "EGL Error: Bad surface: ";
            String valueOf = String.valueOf(getEGLErrorString(egl));
            throw new RuntimeException(valueOf.length() != 0 ? str.concat(valueOf) : new String(str));
        }
    }

    private static void checkEglError(EGL10 egl, String command) {
        int error = egl.eglGetError();
        if (error != 12288) {
            String toHexString = Integer.toHexString(error);
            throw new RuntimeException(new StringBuilder((String.valueOf(command).length() + 32) + String.valueOf(toHexString).length()).append("Error executing ").append(command).append("! EGL error = 0x").append(toHexString).toString());
        }
    }

    private static String getEGLErrorString(EGL10 egl) {
        int eglError = egl.eglGetError();
        if (VERSION.SDK_INT >= 14) {
            return getEGLErrorStringICS(eglError);
        }
        String str = "EGL Error 0x";
        String valueOf = String.valueOf(Integer.toHexString(eglError));
        return valueOf.length() != 0 ? str.concat(valueOf) : new String(str);
    }

    @TargetApi(14)
    private static String getEGLErrorStringICS(int eglError) {
        return GLUtils.getEGLErrorString(eglError);
    }
}
