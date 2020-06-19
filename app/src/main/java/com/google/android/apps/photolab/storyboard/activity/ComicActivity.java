package com.google.android.apps.photolab.storyboard.activity;

import android.animation.Animator;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;

import com.google.android.apps.photolab.storyboard.pipeline.AssetLoader;
import com.google.android.apps.photolab.storyboard.pipeline.ComicCache;
import com.google.android.apps.photolab.storyboard.pipeline.ComicIO;
import com.google.android.apps.photolab.storyboard.pipeline.MediaManager;
import com.mp.android.apps.R;
import com.mp.android.apps.StoryboardActivity;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;

import java.util.ArrayDeque;

public class ComicActivity extends StoryboardActivity implements OnItemClickListener, ComicTextureView.SurfaceListener {
    public static final boolean DETECT_OBJECTS = true;
    public static final int FIRST_IMAGE_SHOWING_DELAY = 6000;
    private static final int INSTRUCTION_DELAY = 1000;
    public static final int SHARE_IMAGE_REQUEST = 12322;
    public static boolean SHOW_DEBUG_INFO = false;
    private static final String TAG = "ComicActivity";
    private static ComicActivity activity;
    public static boolean openWithLoadVideoOverlay = false;
    private AssetLoader assetLoader;
    private ComicPresenter comicPresenter;
    private ComicTextureView comicTextureView;
    private boolean hasSeenInstructions;
    private ArrayDeque<Instruction> instructionList;
    private boolean isFiltering = false;
    private boolean isFirstComic = false;
    private boolean isOpeningVideo;
    private boolean isPaused = false;
    private ProgressDialog loadingVideoDialog;
    LoadingView mFilterProgress;

    /**
     * 点击事件
     * 点击加载视频对话框
     */
    private OnClickListener onImportClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            closeOverlay();
            selectVideo();
        }
    };
    /**
     * 点击事件
     * 点击保存对话框,保存当前图片到sd卡中,通知图册更新
     */
    private OnClickListener onSaveClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            closeOverlay();
            ComicIO.getInstance().saveImageToPictureFolder(comicTextureView.currentBitmap());
        }
    };
    /**
     * 点击事件
     * 点击分享
     */
    private OnClickListener onShareClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            closeOverlay();
            UMImage image = new UMImage(ComicActivity.this, comicTextureView.currentBitmap());//本地文件

            new ShareAction(ComicActivity.this).withMedia(image)
                    .setDisplayList(SHARE_MEDIA.WEIXIN, SHARE_MEDIA.WEIXIN_CIRCLE
                            , SHARE_MEDIA.WEIXIN_FAVORITE, SHARE_MEDIA.QQ, SHARE_MEDIA.QZONE
                            , SHARE_MEDIA.SINA
                    ).open();
        }
    };
    AlertDialog overlayDialog;
    ProgressDialog processingDialog;
    private ViewGroup root;
    private long spinnerStartTime = 0;

    //当前状态
    private enum Instruction {
        MENU_TAP,
        REFRESH_SWIPE
    }


    public static ComicActivity getActivity() {
        return activity;
    }

    public static Context getContext() {
        return activity == null ? null : activity.getApplicationContext();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        setContentView(R.layout.activity_comic);
        this.root = (ViewGroup) findViewById(R.id.activity_frame);
        this.comicTextureView = (ComicTextureView) findViewById(R.id.comicView);
        this.comicPresenter = new ComicPresenter(this, this.comicTextureView);
        this.mFilterProgress = (LoadingView) findViewById(R.id.filteringProgress);
        this.instructionList = new ArrayDeque();
        layoutTransitionLoading();
        this.assetLoader = new AssetLoader();
        this.comicTextureView.setSurfaceListener(this);//设置ComicTextureView中SurfaceListener接口对象
//        FilterManager.initTexture("alex_pattern0.png", this);
//        Bitmap bmp = ComicIO.readTexture(ComicIO.LAST_FILTERED_FILENAME);
//        boolean showLastComic = (bmp == null || openWithLoadVideoOverlay) ? false : true;
//        openWithLoadVideoOverlay = false;
//        if (showLastComic) {
//            getActivity().getComicCache().reset();
//            ComicIO.getInstance().loadExistingImages();
//            this.comicPresenter.getComicCache().getCurrentComic(true).setFilteredBitmap(bmp);
//            startInstructionTimerIfNeeded();
//            return;
//        }
        Intent intent = getIntent();
        if (intent != null) {
            Uri uri = (Uri) intent.getParcelableExtra(ComicSplash.VIDEO_PICKER_EXTRA);
            if (uri != null) {
                initiateLoadingVideo(uri);
            }
        }
    }

    /**
     * mFilterProgress设置进入和隐藏动画
     */
    private void layoutTransitionLoading() {
        Animator moveIn = ObjectAnimator.ofFloat(this.mFilterProgress, "y", new float[]{0.0f, 50.0f});
        moveIn.setDuration(30);
        moveIn.setInterpolator(new LinearInterpolator());
        Animator moveOut = ObjectAnimator.ofFloat(this.mFilterProgress, "y", new float[]{50.0f, 0.0f});
        moveOut.setDuration(30);
        moveOut.setInterpolator(new LinearInterpolator());
        ViewGroup comicContainer = (ViewGroup) findViewById(R.id.comicContainer);
        LayoutTransition itemLayoutTransition = new LayoutTransition();
        itemLayoutTransition.setAnimator(LayoutTransition.APPEARING, moveIn);
        itemLayoutTransition.setAnimator(LayoutTransition.DISAPPEARING, moveOut);
        comicContainer.setLayoutTransition(itemLayoutTransition);
    }

    public boolean getIsFiltering() {
        return this.isFiltering;
    }

    public boolean getIsPaused() {
        return this.isPaused;
    }

    public boolean getIsFirstComic() {
        return this.isFirstComic;
    }

    @Nullable
    public ComicPageData getCurrentComic(boolean generateIfNull) {
        ComicCache cache = getComicCache();
        return cache == null ? null : cache.getCurrentComic(generateIfNull);
    }

    public void setIsFirstComic(boolean value) {
        this.isFirstComic = value;
    }

    /**
     * 向下滑动的偏移量计算
     *
     * @return 返回向下滑动的偏移量
     */
    public int swipeDownOffset() {
        float result = 0.0f;
        if (this.comicPresenter != null && this.comicPresenter.isTouchDown() && this.comicPresenter.mCurrentY > this.comicPresenter.mStartY) {
            float max = (float) this.comicTextureView.getHeight();
            float ratio = 1.0f - (Math.min(this.comicPresenter.mCurrentY - this.comicPresenter.mStartY, max) / max);
            result = (max * 0.25f) - (((((ratio * ratio) * ratio) * ratio) * max) * 0.25f);
            if (!getComicCache().isNextComicReady() && this.mFilterProgress.getVisibility() == View.GONE) {
                startSpinner();
            }
        }
        return (int) result;
    }

    public void startSpinner() {
        this.isFiltering = true;
        this.spinnerStartTime = System.currentTimeMillis();
    }

    public void stopSpinner() {
        if (this.isFiltering) {
            this.isFiltering = false;
            this.comicPresenter.moveToNextLayoutWhenReady = true;
            layoutChanged();
        }
        this.spinnerStartTime = 0;
        if (this.mFilterProgress.getVisibility() == View.GONE) {
        }
    }

    /**
     * 打开AlertDialog 弹出可选择内容
     */
    public void openOverlay() {
        closeOverlay();
        this.overlayDialog = new AlertDialog.Builder(this).create();
        View view = getLayoutInflater().inflate(R.layout.menu_overlay, null);
        ((LinearLayout) view.findViewById(R.id.import_row)).setOnClickListener(this.onImportClick);
        ((LinearLayout) view.findViewById(R.id.save_row)).setOnClickListener(this.onSaveClick);
        ((LinearLayout) view.findViewById(R.id.share_row)).setOnClickListener(this.onShareClick);
        this.overlayDialog.setView(view);
        this.overlayDialog.show();
    }

    /**
     * 关闭dialog提示狂
     */
    public void closeOverlay() {
        if (this.overlayDialog != null) {
            ((LinearLayout) this.overlayDialog.findViewById(R.id.import_row)).setOnClickListener(null);
            ((LinearLayout) this.overlayDialog.findViewById(R.id.save_row)).setOnClickListener(null);
            ((LinearLayout) this.overlayDialog.findViewById(R.id.share_row)).setOnClickListener(null);
            this.overlayDialog.dismiss();
            this.overlayDialog = null;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        switch (position) {
            case 0:
                selectVideo();
                return;
            case 1:
                SHOW_DEBUG_INFO = !SHOW_DEBUG_INFO;
                shuffleFrames();
                return;
            default:
                return;
        }
    }

    public ComicPresenter getComicPresenter() {
        return this.comicPresenter;
    }

    public ComicCache getComicCache() {
        if (this.comicPresenter != null) {
            return this.comicPresenter.getComicCache();
        }
        return null;
    }

    public ComicGenerator getComicGenerator() {
        return this.comicPresenter != null ? this.comicPresenter.getComicGenerator() : null;
    }

    public void shuffleFrames() {
        ComicGenerator pm = this.comicPresenter.getComicGenerator();
        if (pm != null) {
            pm.shuffleFrames();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);
        stopSpinner();
        this.isPaused = false;
        if (resultCode == -1) {
            if (requestCode == StoryboardActivity.PICK_VIDEO_REQUEST) {
                initiateLoadingVideo(data.getData());
            }
        } else if (resultCode == 0) {
            Log.d(TAG, "Start Activity For Result cancelled");
            if (AssetLoader.hasLoadedVideoOnce) {
                Log.d(TAG, "AssetLoader has loaded video once");
                if (this.overlayDialog != null) {
                    closeOverlay();
                }
            } else {
                closeOverlay();
            }
            stopLoadingDialog();
            stopSpinner();
        }
    }

    /**
     * 加载分析后的数据
     *
     * @param videoUri 视频地址链接
     */
    private void initiateLoadingVideo(Uri videoUri) {
        this.isOpeningVideo = true;
        showLoadingDialog();
        ComicIO.getInstance().selectedVideoUri = videoUri;
        this.assetLoader.loadVideo(ComicIO.getInstance().selectedVideoUri);
    }

    @Override
    public void onBackPressed() {
        AssetLoader.setIsVideoLoading(false);
        if (this.overlayDialog == null || !this.overlayDialog.isShowing()) {
            startActivity(new Intent(this, ComicSplash.class));
        } else {
            closeOverlay();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!this.isOpeningVideo) {
            stopLoadingDialog();
        }
        this.isOpeningVideo = false;
        if (this.comicTextureView != null) {
            this.comicTextureView.resumeRenderThread();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopSpinner();
        this.isFiltering = false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        getComicCache().removeLoadingComicsFromEnd();
        MediaManager.instance().resumeDetection();
        getComicGenerator().resumeFiltering();
    }

    @Override
    protected void onStop() {
        super.onStop();
        MediaManager.instance().abortAllDetection();
        getComicGenerator().abortAllFiltering();
    }

    private void startInstructionTimerIfNeeded() {
        if (this.instructionList.isEmpty() && !this.hasSeenInstructions) {
            this.hasSeenInstructions = true;
            if (!this.comicPresenter.hasRefreshed) {
                this.instructionList.add(Instruction.REFRESH_SWIPE);
            }
            if (!this.comicPresenter.hasOpenedMenu) {
                this.instructionList.add(Instruction.MENU_TAP);
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (canShowToast()) {
                        ComicActivity.getActivity().showInstruction();
                    }
                }
            }, 7000);

        }
    }


    private void showInstruction() {
        boolean showedOneInstruction = false;
        while (!this.instructionList.isEmpty() && !showedOneInstruction) {
            switch ((Instruction) this.instructionList.remove()) {
                case REFRESH_SWIPE:
                    if (!this.comicPresenter.hasRefreshed) {
                        showedOneInstruction = true;
                        Snackbar.make(this.root, getString(R.string.toast_pull_down), 0).show();
                        if (!this.comicPresenter.hasOpenedMenu) {
                            break;
                        }
                        this.instructionList.clear();
                        break;
                    }
                    break;
                case MENU_TAP:
                    if (!this.comicPresenter.hasOpenedMenu) {
                        showedOneInstruction = true;
                        Snackbar.make(this.root, getString(R.string.toast_tap), 0).show();
                        break;
                    }
                    break;
                default:
                    break;
            }
        }
        if (!this.instructionList.isEmpty()) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (canShowToast()) {
                        ComicActivity.getActivity().showInstruction();
                    }
                }
            }, 7000);

        }
    }


    private boolean canShowToast() {
        return (AssetLoader.isProcessing() || AssetLoader.isVideoLoading() || this.isPaused) ? false : true;
    }

    public void completeInitialLayout() {
        getComicCache().removeLoadingComics();
        stopSpinner();
        this.isFirstComic = true;
    }

    public void layoutChanged() {
        if (this.comicTextureView != null) {
            this.comicTextureView.layoutChanged();
        }
    }

    /**
     * 自定义ComicTextureView 提供 interface SurfaceListener 实现方法
     * SurfaceTextureListener.onSurfaceTextureAvailable中使用
     *
     * @param surface
     * @param width
     * @param height
     */
    @Override
    public void onSurfaceAvailable(TextureView surface, int width, int height) {
        this.comicPresenter.getComicGenerator().onSizeChanged(width, height);
    }

    /**
     * 自定义ComicTextureView 提供 interface SurfaceListener 实现方法
     * SurfaceTextureListener.onSurfaceTextureSizeChanged 中使用
     *
     * @param surface
     * @param width
     * @param height
     */
    @Override
    public void onSurfaceSizeChanged(TextureView surface, int width, int height) {
        if (this.comicPresenter.getComicGenerator() != null) {
            this.comicPresenter.getComicGenerator().onSizeChanged(width, height);
        }
    }

    /**
     * 自定义ComicTextureView 提供 interface SurfaceListener 实现方法
     * SurfaceTextureListener.onSurfaceTextureDestroyed 中使用
     *
     * @param surface
     * @return
     */
    @Override
    public boolean onSurfaceDestroyed(TextureView surface) {
        return true;
    }

    public Size getSize() {
        if (this.root != null) {
            return new Size(this.root.getWidth(), this.root.getHeight());
        }
        return getAppSize();
    }

    /**
     * 获得屏幕举行rect
     *
     * @return
     */
    public Rect getBounds() {
        if (this.root != null) {
            return new Rect(0, 0, this.root.getWidth(), this.root.getHeight());
        }
        Size sz = getAppSize();
        return new Rect(0, 0, sz.getWidth(), sz.getHeight());
    }

    /**
     * 获取屏幕分辨率高度和宽度
     *
     * @return
     */
    private Size getAppSize() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return new Size(displayMetrics.widthPixels, displayMetrics.heightPixels);
    }

    /**
     * 分析视频的过程中
     * 展示dialog 等待执行动画
     * 加载loading圈
     */
    public void showLoadingDialog() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (loadingVideoDialog == null) {
                    loadingVideoDialog = new ProgressDialog(getActivity());
                    loadingVideoDialog.setProgressStyle(0);
                    loadingVideoDialog.setMessage(getString(R.string.loading_video));
                    loadingVideoDialog.setIndeterminate(true);
                    loadingVideoDialog.setCanceledOnTouchOutside(false);
                    loadingVideoDialog.setCancelable(false);
                }
                loadingVideoDialog.show();
            }
        });
    }


    public void stopLoadingDialog() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (loadingVideoDialog != null) {
                    loadingVideoDialog.dismiss();
                }

            }
        });
    }

    /**
     * 视频分析过程中使用,展示dialog内容
     * 展示内容为视频分析中
     */
    public void startOrContinueProcessingDialog() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (processingDialog == null) {
                    processingDialog = new ProgressDialog(ComicActivity.this);
                    processingDialog.setMessage(getString(R.string.analysis_video));
                    processingDialog.setMax(100);
                    processingDialog.setProgress(0);
                    processingDialog.setProgressNumberFormat(null);
                    processingDialog.setProgressPercentFormat(null);
                    processingDialog.setCanceledOnTouchOutside(false);
                    processingDialog.setCancelable(false);
                    processingDialog.show();
                }

            }
        });
    }


    public void stopProcessingDialog() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (processingDialog != null) {
                    processingDialog.dismiss();
                    processingDialog = null;
                }
                startInstructionTimerIfNeeded();
            }
        });
    }

}
