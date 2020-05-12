package com.google.android.apps.photolab.storyboard.pipeline;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Process;
import android.util.Log;

import androidx.media.filterfw.FrameImage2D;

import com.google.android.apps.photolab.storyboard.activity.ComicActivity;
import com.google.android.apps.photolab.storyboard.pipeline.VideoLoader.Listener;

import java.util.Timer;
import java.util.TimerTask;

public class AssetLoader implements Listener {
    private static final int DURATION_BEFORE_USING_KEYFRAMES = 2000;
    private static final String TAG = "AssetLoader";
    private static boolean canCancelVideoLoad = false;
    public static boolean hasLoadedVideoOnce = false;
    private static boolean isPreviewFirstLayout = false;
    private static boolean isProcessing = false;
    private static boolean isVideoLoading = false;
    public static int maxFramesToLoad = 20;
    private static MediaMetadataRetriever retriever;
    private Bitmap lastFrame;
    private int loadedFrameCount;
    private int savedFrameCount;
    private long videoDurationMS;
    private int videoFrameCount;
    private int videoHeight;
    private boolean videoNeedsResize = false;
    private int videoWidth;

    private class LoadVideoTask extends AsyncTask<Uri, Integer, Uri> {
        Uri firstUri;

        private LoadVideoTask() {
        }

        protected Uri doInBackground(Uri... videoUri) {
            this.firstUri = videoUri.length > 0 ? videoUri[0] : null;
            AssetLoader.this.loadVideoInternal(this.firstUri);
            return this.firstUri;
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(Uri result) {
        }
    }

    public static boolean isProcessing() {
        return isProcessing;
    }

    public static boolean isVideoLoading() {
        return isVideoLoading;
    }

    public static void setIsVideoLoading(boolean value) {
        isVideoLoading = value;
        if (!value && canCancelVideoLoad) {
            if (retriever != null) {
                retriever.release();
                retriever = null;
            }
            if (!hasLoadedVideoOnce) {
                ComicActivity.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ComicActivity.getActivity().closeOverlay();
                    }
                });
            }
        }
    }

    public static boolean getIsPreviewFirstLayout() {
        return isPreviewFirstLayout;
    }

    public void onFrame(FrameImage2D frameImage, long timestamp) {
        Bitmap bmp = frameImage.toBitmap();
        if (this.lastFrame == null || !bmp.sameAs(this.lastFrame)) {
            this.lastFrame = bmp;
            this.loadedFrameCount++;
            if (((float) this.loadedFrameCount) > (((float) this.videoFrameCount) / ((float) maxFramesToLoad)) * ((float) this.savedFrameCount)) {
                this.videoFrameCount = (int) (((float) (this.videoDurationMS * (timestamp / ((long) this.loadedFrameCount)))) / 1000.0f);
                processVideoFrame(bmp, timestamp);
            }
        }
    }

    private void processVideoFrame(Bitmap bmp, long timestamp) {
        ComicActivity.getActivity().stopLoadingDialog();
        if (this.videoNeedsResize) {
            bmp = Bitmap.createScaledBitmap(bmp, this.videoWidth, this.videoHeight, false);
        }
        Bitmap copyBmp = bmp.copy(Config.ARGB_8888, true);
        FilterManager.instance().equalizeImageIfNeeded(copyBmp);
        ComicIO.getInstance().writeImageToPath(copyBmp, "img");
        this.savedFrameCount++;
        Log.i(TAG, "onFrame: " + MediaManager.instance().getCount() + " timestamp:" + timestamp);
        MediaManager.instance().addBitmap(copyBmp);
        ComicActivity.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ComicActivity.getActivity().startOrContinueProcessingDialog();
                ComicActivity.getActivity().shuffleFrames();
            }
        });
    }

    /**
     * 使用AsyncTask分析视频内容
     *
     * @param videoUri
     */
    public void loadVideo(Uri videoUri) {
        isProcessing = true;
        ComicActivity.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new LoadVideoTask().execute(new Uri[]{videoUri});
            }
        });
    }


    /**
     * LoadVideoTask 使用AsyncTask加载视频内容并做解析
     *
     * @param videoUri 视频uri
     */
    private void loadVideoInternal(Uri videoUri) {
        if (videoUri != null) {
            retriever = new MediaMetadataRetriever();
            isVideoLoading = true;
            canCancelVideoLoad = true;
            try {
                retriever.setDataSource(ComicActivity.getContext(), videoUri);
                canCancelVideoLoad = false;
                if (isVideoLoading) {
                    long videoFPS;
                    ComicIO.getInstance().clearImageFolder();
                    MediaManager.instance().clearAssets();
                    ComicActivity.getActivity().getComicCache().reset();
                    Bitmap bmp = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                    this.videoHeight = bmp.getHeight();
                    this.videoWidth = bmp.getWidth();
                    this.videoNeedsResize = false;
                    if (((float) this.videoWidth) > 1920.0f || ((float) this.videoHeight) > 1920.0f) {
                        float ratio;
                        if (this.videoWidth > this.videoHeight) {
                            ratio = 1920.0f / ((float) this.videoWidth);
                        } else {
                            ratio = 1920.0f / ((float) this.videoHeight);
                        }
                        this.videoWidth = (int) (((float) this.videoWidth) * ratio);
                        this.videoHeight = (int) (((float) this.videoHeight) * ratio);
                        this.videoNeedsResize = true;
                    }
                    //获取视频播放长度
                    this.videoDurationMS = Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
                    String fps = retriever.extractMetadata(25);
                    if (fps == null) {
                        videoFPS = 30;
                    } else {
                        videoFPS = (long) Float.parseFloat(fps);
                    }
                    this.videoFrameCount = (int) (((float) (this.videoDurationMS * videoFPS)) / 1000.0f);
                    this.savedFrameCount = 0;
                    isProcessing = true;
                    ComicActivity comicActivity = ComicActivity.getActivity();
                    //判断视频长度是否大于2秒
                    if (this.videoDurationMS > 2000) {
                        loadVideoAsKeyframes(retriever);
                        return;
                    }
                    this.loadedFrameCount = 0;
                    Uri uri = videoUri;
                    new VideoLoader(ComicActivity.getContext(), uri, this.videoWidth, this.videoHeight, this.videoDurationMS, this).start();
                } else if (retriever != null) {
                    retriever.release();
                    retriever = null;
                }
            } catch (Exception e) {
                Log.i(TAG, e.toString());
                isVideoLoading = false;
            }
        }
    }

    private void loadVideoAsKeyframes(final MediaMetadataRetriever retriever) {
        new Thread(new Runnable() {
            public void run() {
                AssetLoader.this.loadedFrameCount = 0;
                int timeStep = (int) (((float) AssetLoader.this.videoDurationMS) / 20.0f);
                for (int curTime = 0; ((long) curTime) < AssetLoader.this.videoDurationMS; curTime += timeStep) {
                    Bitmap bmp = retriever.getFrameAtTime((long) (curTime * 1000), MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                    if (bmp != null && (AssetLoader.this.lastFrame == null || !bmp.sameAs(AssetLoader.this.lastFrame))) {
                        AssetLoader.this.lastFrame = bmp;
                        AssetLoader.this.loadedFrameCount = AssetLoader.this.loadedFrameCount + 1;
                        Log.i(AssetLoader.TAG, "comic frame:" + AssetLoader.this.loadedFrameCount);
                        if (AssetLoader.this.videoNeedsResize) {
                            bmp = Bitmap.createScaledBitmap(bmp, AssetLoader.this.videoWidth, AssetLoader.this.videoHeight, false);
                        }
                        AssetLoader.this.processVideoFrame(bmp, (long) curTime);
                    }
                }
                AssetLoader.this.onVideoCompleted((int) AssetLoader.this.videoDurationMS);
            }
        }).start();
    }

    /**
     * 视频完成 视频帧数为 ComicIO.getInstance().getStoredFrameCount()
     *
     * @param value
     */
    public void onVideoCompleted(int value) {
        Log.i(TAG, "video complete, frames loaded: " + ComicIO.getInstance().getStoredFrameCount());
        if (retriever != null) {
            retriever.release();
            retriever = null;
        }
        hasLoadedVideoOnce = true;
        isPreviewFirstLayout = true;
        isProcessing = false;
        ComicActivity.getActivity().completeInitialLayout();
        isVideoLoading = false;
        this.lastFrame = null;
        ComicActivity.getActivity().getComicGenerator().filterComic(ComicActivity.getActivity().getComicCache().getCurrentComic(true));
        ComicActivity.getActivity().getComicCache().removeLoadingComics();

        new Timer().schedule(new TimerTask() {
            public void run() {
                Process.setThreadPriority(19);
                AssetLoader.isPreviewFirstLayout = false;
                ComicActivity.getActivity().stopProcessingDialog();
            }
        }, 6000);
    }

    public void onVideoError(Exception exception, boolean closedSuccessfully) {
        Log.i(TAG, "video error");
        isVideoLoading = false;
    }
}
