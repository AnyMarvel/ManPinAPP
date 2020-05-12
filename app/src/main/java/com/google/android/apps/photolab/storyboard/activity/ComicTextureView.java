package com.google.android.apps.photolab.storyboard.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;

import com.google.android.apps.photolab.storyboard.pipeline.AnimationUtils;
import com.google.android.apps.photolab.storyboard.pipeline.AssetLoader;
import com.google.android.apps.photolab.storyboard.pipeline.ComicCache;

public class ComicTextureView extends TextureView implements SurfaceTextureListener {
    private static final String TAG = "ComicTextureView";
    private ComicPresenter comicPresenter;
    private boolean isThreadRunning = false;
    /**
     * 渲染界面线程,完成解析后使用子线程进行界面渲染
     * 支持下载刷新操作
     */
    private RenderingThread mThread;
    private SurfaceListener surfaceListener;

    public static class RenderingThread extends Thread {
        private int animSpeed = 20;
        private Paint arcPaint;
        private RectF arcRect;
        private ComicPresenter comicPresenter;
        private int curShowingLayout;
        private float lastYOffset = 0.0f;
        private volatile boolean mRunning;
        private final ComicTextureView mSurface;
        private float snapBackLerp = 0.0f;
        private float spinnerCx;
        private int spinnerHeight;
        private int spinnerLerp = 0;
        private float spinnerR;
        private float spinnerRotation;

        RenderingThread(ComicTextureView surface, ComicPresenter comicPresenter) {
            this.mSurface = surface;
            this.comicPresenter = comicPresenter;
            this.spinnerHeight = (int) (((float) surface.getHeight()) * 0.05f);
            this.spinnerCx = (float) ((int) (((double) surface.getWidth()) / 2.0d));
            this.spinnerR = (float) ((int) (((double) this.spinnerHeight) * 0.33d));
            this.arcPaint = new Paint();
            this.arcPaint.setStyle(Style.STROKE);
            this.arcPaint.setColor(Color.LTGRAY);
            this.arcPaint.setStrokeWidth(7.0f);
        }

        @Override
        public void run() {
            String str;
            String str2;
            String valueOf;
            String str3;
            Paint paint = new Paint();
            Rect appRect = ComicActivity.getActivity().getBounds();
            ComicCache cache = ComicActivity.getActivity().getComicCache();
            this.mRunning = true;
            while (this.mRunning && !Thread.interrupted()) {
                if (!(ComicActivity.getActivity().getIsPaused() || cache == null)) {
                    Canvas canvas = this.mSurface.lockCanvas();
                    ComicPageData cpd = cache.getCurrentComic(true);
                    if (cpd.getLayoutIndex() != this.curShowingLayout) {
                        this.curShowingLayout = cpd.getLayoutIndex();
                    }
                    if (cpd != null) {
                        canvas.drawColor(Color.WHITE, Mode.SRC_OVER);
                        if (AssetLoader.isProcessing()) {
                            cpd.centerImages();
                            this.comicPresenter.onDrawGL(canvas, cpd);
                            cpd.setIsFullyGenerated(true);
                        } else if (AssetLoader.getIsPreviewFirstLayout()) {
                            cpd.centerImages();
                            this.comicPresenter.onDrawGL(canvas, cpd);
                        } else {
                            try {
                                Rect src = null;
                                Bitmap bmp = cpd.getFilteredBitmap();
                                if (bmp == null) {
                                    bmp = Bitmap.createBitmap(appRect.width(), appRect.height(), Config.ARGB_8888);
                                    this.comicPresenter.onDrawGL(new Canvas(bmp), cpd);
                                }
                                int yOffset = ComicActivity.getActivity().swipeDownOffset();
                                if (ComicActivity.getActivity().getIsFiltering()) {
                                    this.spinnerLerp = Math.min(this.spinnerLerp + this.animSpeed, this.spinnerHeight);
                                }
                                Rect rect = new Rect(appRect);
                                Rect dest = new Rect(appRect);
                                Paint curPaint = null;
                                int destY = yOffset + this.spinnerLerp;
                                Rect dest2;
                                if (yOffset > 0) {
                                    paint.setAlpha(255 - ((int) (((float) Math.min(yOffset, 400)) / 2.0f)));
                                    rect = new Rect(0, 0, rect.width(), rect.height() - destY);
                                    dest2 = new Rect(0, destY, dest.width(), dest.height());
                                    curPaint = paint;
                                    this.lastYOffset = (float) yOffset;
                                    this.snapBackLerp = 1.0f;
                                    dest = dest2;
                                    src = rect;
                                } else {
                                    if (((double) this.snapBackLerp) > 0.9999d) {
                                        if (this.lastYOffset > 50.0f) {
                                            this.lastYOffset -= (float) this.animSpeed;
                                        }
                                        paint.setAlpha(255 - ((int) (Math.min(this.lastYOffset, 400.0f) / 2.0f)));
                                        curPaint = paint;
                                        this.snapBackLerp = 0.995f;
                                        destY = ((int) this.lastYOffset) + this.spinnerLerp;
                                        rect = new Rect(0, 0, rect.width(), rect.height() - destY);
                                        dest = new Rect(0, destY, dest.width(), dest.height());
                                        src = rect;
                                    } else if (this.snapBackLerp > 0.0f) {
                                        this.snapBackLerp = Math.max(this.snapBackLerp - 0.05f, 0.0f);
                                        destY = ((int) (AnimationUtils.easeInBack(this.snapBackLerp) * this.lastYOffset)) + this.spinnerLerp;
                                        rect = new Rect(0, 0, rect.width(), rect.height() - destY);
                                        dest = new Rect(0, destY, dest.width(), dest.height());
                                        src = rect;
                                    } else {
                                        this.lastYOffset = 0.0f;
                                        this.snapBackLerp = 0.0f;
                                        if (this.spinnerLerp > 0) {
                                            rect = new Rect(0, 0, rect.width(), rect.height() - this.spinnerLerp);
                                            dest2 = new Rect(0, this.spinnerLerp, dest.width(), dest.height());
                                            this.spinnerLerp = Math.max(0, this.spinnerLerp - this.animSpeed);
                                            dest = dest2;
                                            src = rect;
                                        }
                                    }
                                    moveToNextLayoutIfNeeded();
                                }
                                canvas.drawBitmap(bmp, src, dest, curPaint);
                                drawSpinner(canvas, dest.top);
                            } catch (Throwable th) {
                                this.mSurface.unlockCanvasAndPost(canvas);
                            }
                        }
                        this.mSurface.unlockCanvasAndPost(canvas);
                    }
                    if (cache.canAddComic() && !AssetLoader.isProcessing()) {
                        cache.incProcessingCount();
                        ComicActivity.getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ComicActivity.getActivity().getComicGenerator().generateNextLayout();
                                ComicActivity.getActivity().getComicCache().decProcessingCount();
                            }
                        });
                    }
                }
                try {
                    Thread.sleep(15);
                } catch (InterruptedException e) {
                    str = ComicTextureView.TAG;
                    str2 = "Thread Exception: ";
                    valueOf = String.valueOf(e.getMessage());
                    if (valueOf.length() != 0) {
                        valueOf = str2.concat(valueOf);
                    } else {
                        str3 = new String(str2);
                    }
                    Log.i(str, valueOf);
                } catch (Exception e2) {
                    str = ComicTextureView.TAG;
                    str2 = "Thread Exception any: ";
                    valueOf = String.valueOf(e2.getMessage());
                    if (valueOf.length() != 0) {
                        valueOf = str2.concat(valueOf);
                    } else {
                        str3 = new String(str2);
                    }
                    Log.i(str, valueOf);
                }
                this.mSurface.drawComplete();
            }
        }

        private void drawSpinner(Canvas canvas, int height) {
            float cy = Math.min(((float) height) - (this.spinnerR * 1.5f), ((float) height) / 2.0f) + 5.0f;
            if (this.spinnerLerp > 0) {
                this.arcRect = new RectF(this.spinnerCx - this.spinnerR, cy - this.spinnerR, this.spinnerCx + this.spinnerR, this.spinnerR + cy);
                canvas.drawArc(this.arcRect, this.spinnerRotation, 90.0f, false, this.arcPaint);
                canvas.drawArc(this.arcRect, 120.0f + this.spinnerRotation, 90.0f, false, this.arcPaint);
                canvas.drawArc(this.arcRect, 240.0f + this.spinnerRotation, 90.0f, false, this.arcPaint);
                this.spinnerRotation = (this.spinnerRotation + 5.0f) % 360.0f;
            }
        }

        boolean moveToNextLayoutIfNeeded() {
            if (!this.comicPresenter.moveToNextLayoutWhenReady || ComicActivity.getActivity().getIsFiltering()) {
                return false;
            }
            this.comicPresenter.moveToNextLayoutWhenReady = false;
            this.comicPresenter.moveToNextLayout();
            return true;
        }

        void stopRendering() {
            interrupt();
            this.mRunning = false;
        }
    }

    /**
     * 在SurfaceTextureListener中提供外部调用
     */
    interface SurfaceListener {
        void onSurfaceAvailable(TextureView textureView, int i, int i2);

        boolean onSurfaceDestroyed(TextureView textureView);

        void onSurfaceSizeChanged(TextureView textureView, int i, int i2);
    }

    public ComicTextureView(Context context) {
        super(context);
        initialize();
    }

    public ComicTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public ComicTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    public ComicTextureView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize();
    }

    private void initialize() {
        setOpaque(false);//设置控件透明
        setSurfaceTextureListener(this);//设置SurfaceTextureListener
    }

    private ComicCache getComicCache() {
        return this.comicPresenter.getComicCache();
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    public void setSurfaceListener(SurfaceListener surfaceListener) {
        this.surfaceListener = surfaceListener;
    }

    public void setComicPresenter(ComicPresenter comicPresenter) {
        this.comicPresenter = comicPresenter;
    }

    /**
     * SurfaceTextureListener 实现类
     * 在SurfaceTexture准备使用时调用。
     *
     * @param surface
     * @param width
     * @param height
     */
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (this.surfaceListener != null) {
            this.surfaceListener.onSurfaceAvailable(this, width, height);
        }
        this.mThread = new RenderingThread(this, this.comicPresenter);
        resumeRenderThread();
    }

    /**
     * 恢复渲染线程
     * 子线程进行界面刷新
     */
    public void resumeRenderThread() {
        if (this.mThread != null && !this.isThreadRunning) {
            this.isThreadRunning = true;
            this.mThread.start();
            Log.i(TAG, "resumeRenderThread: start");
        }
    }

    public void pauseRenderThread() {
        if (this.mThread != null && this.isThreadRunning) {
            this.isThreadRunning = false;
            this.mThread.stopRendering();
            Log.i(TAG, "resumeRenderThread: stop");
        }
    }

    /**
     * SurfaceTextureListener 实现类
     * 当SurfaceTexture缓冲区大小更改时调用。
     *
     * @param surface
     * @param width
     * @param height
     */
    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        if (this.surfaceListener != null) {
            this.surfaceListener.onSurfaceSizeChanged(this, width, height);
        }
        invalidate();
    }

    /**
     * SurfaceTextureListener 实现类
     * 当指定SurfaceTexture即将被销毁时调用。如果返回true，则调用此方法后，
     * 表面纹理中不会发生渲染。如果返回false，则客户端需要调用release()。
     * 大多数应用程序应该返回true。
     *
     * @param surface
     * @return
     */
    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        pauseRenderThread();
        this.mThread = null;
        if (this.surfaceListener != null) {
            this.surfaceListener.onSurfaceDestroyed(this);
        }
        return false;
    }

    /**
     * SurfaceTextureListener 实现类
     * 当指定SurfaceTexture的更新时调用updateTexImage()。
     *
     * @param surface
     */
    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        return this.comicPresenter.onTouch(this, event);
    }

    public void layoutChanged() {
        ComicActivity.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                invalidate();
            }
        });
    }

    /**
     * 获得当前图片
     *
     * @return
     */
    public Bitmap currentBitmap() {
        ComicPageData cpd = getComicCache().getCurrentComic(true);
        if (cpd != null) {
            return cpd.getFilteredBitmap();
        }
        return null;
    }

    public void drawComplete() {
    }
}
