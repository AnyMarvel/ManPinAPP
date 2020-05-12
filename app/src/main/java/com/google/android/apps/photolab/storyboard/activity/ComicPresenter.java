package com.google.android.apps.photolab.storyboard.activity;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.mp.android.apps.R;
import com.google.android.apps.photolab.storyboard.pipeline.AssetLoader;
import com.google.android.apps.photolab.storyboard.pipeline.ComicCache;

/**
 * 逻辑处理层
 */
public class ComicPresenter implements OnTouchListener {
    private static final int DOUBLE_TAP_DELAY = 250;
    private static final int LONG_PRESS_DELAY = 400;
    private static final String TAG = "ComicPresenter";
    private static final int TAP_RELEASE_DELAY = 100;
    private final ComicCache cache = new ComicCache();
    private ComicGenerator comicGenerator;
    private ComicTextureView comicTextureView;
    private final Handler doubleTapHandler = new Handler();
    RectF dragRect;
    IComicMoveable draggingElement;
    public boolean hasOpenedMenu = false;
    public boolean hasRefreshed = false;
    public boolean isPaused = false;
    private boolean isTouchDown = false;//判断是否被点击操作
    private long lastActionUpTime = 0;
    private final Handler longPressHandler = new Handler();
    float mCurrentX;
    private float mCurrentX2;
    float mCurrentY;
    private float mCurrentY2;
    private Runnable mDoubleTap = new Runnable() {
        @Override
        public void run() {
            testForDoubleTap();
        }
    };
    private Runnable mLongPressed = new Runnable() {
        @Override
        public void run() {
            waitingForLongPress = false;
            if (draggingElement == null) {
                draggingElement = comicGenerator.getPanelFromPoint((float) ((int) mStartX), (float) ((int) mStartY));
            }
        }
    };
    float mStartX;
    float mStartY;
    public boolean moveToNextLayoutWhenReady = false;
    private ComicRenderer renderer;
    private long secondTapExpiry = 0;
    private IComicMoveable selectedElement;
    ComicPanel targetPanel;
    boolean waitingForLongPress = false;

    ComicPresenter(Context context, ComicTextureView comicTextureView) {
        this.comicTextureView = comicTextureView;
        this.comicTextureView.setComicPresenter(this);
        init();
    }

    private void init() {
        this.dragRect = new RectF(0.0f, 0.0f, 1.0f, 1.0f);
        this.comicGenerator = new ComicGenerator(this);
        this.renderer = new ComicRenderer(this);
    }

    public ComicCache getComicCache() {
        return this.cache;
    }

    public int getWidth() {
        return this.comicTextureView.getWidth();
    }

    public int getHeight() {
        return this.comicTextureView.getHeight();
    }

    public boolean isTouchDown() {
        return this.isTouchDown;
    }

    public void cancelTouch() {
        this.targetPanel = null;
        this.mStartX = 0.0f;
        this.mStartY = 0.0f;
        this.selectedElement = null;
        this.draggingElement = null;
        this.isTouchDown = false;
    }

    /**
     * 测试是否为重复点击
     *
     * @return
     */
    private boolean testForDoubleTap() {
        if (((this.mCurrentX - this.mStartX) * (this.mCurrentX - this.mStartX))
                + ((this.mCurrentY - this.mStartY) * (this.mCurrentY - this.mStartY)) > 20.0f ||
                (this.secondTapExpiry > 0 && this.secondTapExpiry < System.currentTimeMillis())) {
            this.secondTapExpiry = 0;
            return false;
        } else if (this.secondTapExpiry > System.currentTimeMillis()) {
            return true;
        } else {
            this.secondTapExpiry = System.currentTimeMillis() + 250;
            return false;
        }
    }

    /**
     * 处理TextureView onTouch事件
     *
     * @param v 触摸 target view
     * @param e 触发事件
     * @return 返回是否被处理
     */
    @Override
    public boolean onTouch(View v, MotionEvent e) {
        this.isTouchDown = true;
        this.mCurrentX = e.getX();
        this.mCurrentY = e.getY();
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                this.mStartX = this.mCurrentX;
                this.mStartY = this.mCurrentY;
                this.doubleTapHandler.postDelayed(this.mDoubleTap, 100);
                break;
            case MotionEvent.ACTION_UP:
                this.longPressHandler.removeCallbacks(this.mLongPressed);
                this.doubleTapHandler.removeCallbacks(this.mDoubleTap);
                ComicActivity comicActivity = ComicActivity.getActivity();
                boolean consumed = false;//是否为重复点击
                if (this.mCurrentY - this.mStartY <= 150.0f || this.mStartX <= 10.0f || AssetLoader.isProcessing()) {
                    consumed = testForDoubleTap();
                } else {
                    this.hasRefreshed = true;
                    if (this.cache.isNextComicReady()) {
                        this.moveToNextLayoutWhenReady = true;
                        consumed = true;
                        this.comicTextureView.announceForAccessibility(comicActivity.getString(R.string.generating_comic));
                        comicActivity.closeOverlay();
                    }
                }
                //打开dialog弹窗
                if (!consumed && Math.abs(this.mCurrentX - this.mStartX) < 20.0f && Math.abs(this.mCurrentY - this.mStartY) < 20.0f && System.currentTimeMillis() > this.lastActionUpTime + 100 && !comicActivity.getIsPaused() && !AssetLoader.isVideoLoading()) {
                    this.hasOpenedMenu = true;
                    comicActivity.openOverlay();
                }
                this.lastActionUpTime = System.currentTimeMillis();
                cancelTouch();
                break;
            case MotionEvent.ACTION_MOVE:
                this.isTouchDown = true;
                this.doubleTapHandler.removeCallbacks(this.mDoubleTap);
                if (this.selectedElement != null) {
                    break;
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                this.mCurrentX2 = e.getX(1);
                this.mCurrentY2 = e.getY(1);
                break;
        }
        return true;
    }

    public void moveToNextLayout() {
        if (this.cache.isNextComicReady()) {
            ComicActivity comic = ComicActivity.getActivity();
            comic.stopSpinner();
            this.cache.removeCurrentComic();
            comic.layoutChanged();
        }
    }

    public void onDrawGL(Canvas canvas, ComicPageData comicPageData) {
        if (comicPageData != null && !this.isPaused) {
            this.renderer.onDraw(canvas, comicPageData);
        }
    }

    public ComicGenerator getComicGenerator() {
        return this.comicGenerator;
    }
}
