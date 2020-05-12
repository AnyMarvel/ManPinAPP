package com.google.android.apps.photolab.storyboard.pipeline;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.RectF;
import android.util.Log;

import com.google.android.libraries.vision.humansensing.ssd.FaceDetector;
import com.google.protos.humansensing.FaceProtos.Face;
import com.google.protos.humansensing.FaceProtos.Face.BoundingBox;
import com.google.protos.humansensing.FacesProtos.Faces;
import com.google.protos.mobilessd.MobileSSDClientOptionsProto.MobileSSDClientOptions;
import com.google.protos.mobilessd.MobileSSDClientOptionsProto.MobileSSDClientOptions.Builder;
import com.mp.android.apps.utils.Logger;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MediaManager {
    private static final String TAG = "MediaManager";
    private static MediaManager _mediaManager;
    private boolean abortDetection = false;
    private ArrayList<ComicAsset> assets = new ArrayList();
    private int bufferSize = AssetLoader.maxFramesToLoad;
    private ExecutorService executor;
    private FaceDetector faceDetector;
    private ObjectDetector objectDetector;

    public static MediaManager instance() {
        if (_mediaManager == null) {
            _mediaManager = new MediaManager();
        }
        return _mediaManager;
    }

    public MediaManager() {
        init();
    }

    private void init() {
    }

    public ArrayList<ComicAsset> getAssets() {
        return this.assets;
    }

    public int getCount() {
        return this.assets.size();
    }

    public int addBitmap(Bitmap bmp) {
        return addBitmapAndTrim(bmp, this.assets.size());
    }

    /**
     * 添加位图和边框
     *
     * @param bmp
     * @param insertionIndex
     * @return
     */
    private int addBitmapAndTrim(Bitmap bmp, int insertionIndex) {
        if (this.executor == null) {
            this.executor = Executors.newSingleThreadExecutor();
        }
        int result = insertionIndex;
        if (bmp != null) {
            if (bmp.getConfig() != Config.ARGB_8888) {
                bmp = bmp.copy(Config.ARGB_8888, true);
            }
            result = Math.max(0, Math.min(insertionIndex, this.assets.size()));
            final ComicAsset asset = new ComicAsset(bmp);
            this.assets.add(result, asset);
            //若适配分析出的帧数大于20则清理20以上的内容
            if (this.assets.size() > this.bufferSize) {
                this.assets.subList(this.bufferSize, this.assets.size()).clear();
            }
            final Bitmap detectBmp = bmp;
            try {
                this.executor.execute(new Thread(new Runnable() {
                    public void run() {
                        if (!MediaManager.this.abortDetection) {
                            Faces faces = MediaManager.this.detectFaces(detectBmp);
                            ObjectDetectionSet detections = MediaManager.this.detectObjects(detectBmp);
                            if (faces != null) {
                                for (Face face : faces.getFaceList()) {
                                    detections.addDetection(new ObjectDetection(face, detectBmp.getWidth(), detectBmp.getHeight()));
                                }
                            }
                            Log.i(MediaManager.TAG, "detections:" + detections.size());
                            asset.addDetections(detections);
                        }
                    }
                }));
            } catch (Exception localException) {
                String str = TAG;
                String str2 = "Thread executor failed: ";
                String valueOf = String.valueOf(localException.getMessage());
                Log.i(str, valueOf.length() != 0 ? str2.concat(valueOf) : new String(str2));
            }
        }
        return result;
    }

    public Bitmap getImageByIndex(int index) {
        if (index < 0 || index >= this.assets.size()) {
            return null;
        }
        ComicAsset asset = (ComicAsset) this.assets.get(index);
        if (asset != null) {
            return asset.getBitmap();
        }
        Log.i(TAG, "getImageByIndex: Null asset");
        return null;
    }

    public ObjectDetectionSet getDetectionsByIndex(int index) {
        if (index < 0 || index >= this.assets.size()) {
            return null;
        }
        return ((ComicAsset) this.assets.get(index)).getDetectedObjects();
    }

    public Faces getFacesByIndex(int index) {
        if (index < 0 || index >= this.assets.size()) {
            return null;
        }
        return ((ComicAsset) this.assets.get(index)).getRawFaces();
    }

    public void clearAssets() {
        this.assets.clear();
        if (this.executor != null) {
            this.executor.shutdownNow();
        }
        this.executor = null;
    }

    /**
     * 检测人物像素点
     *
     * @param bmp
     * @return
     */
    Faces detectFaces(Bitmap bmp) {
        Faces result = null;
        try {
            result = getFaceDetector().detectFaces(bmp);
        } catch (Exception e) {
            String str = TAG;
            String str2 = "detectFaces: exception:";
            String valueOf = String.valueOf(e.getMessage());
            Logger.w(str, valueOf.length() != 0 ? str2.concat(valueOf) : new String(str2));
        }
        return result;
    }

    ObjectDetectionSet detectObjects(Bitmap bmp) {
        ObjectDetectionSet result = new ObjectDetectionSet(bmp.getWidth(), bmp.getHeight());
        try {
            ObjectDetection[] detections = getObjectDetector().detectObjects(bmp);
            if (detections != null) {
                result.setDetectionsWithArray(detections);
            }
        } catch (Exception e) {
            String str = TAG;
            String str2 = "detectObjects: exception:";
            String valueOf = String.valueOf(e.getMessage());
            Log.i(str, valueOf.length() != 0 ? str2.concat(valueOf) : new String(str2));
        }
        return result;
    }

    private ObjectDetector getObjectDetector() {
        if (this.objectDetector == null) {
            this.objectDetector = ObjectDetector.create();
        }
        return this.objectDetector;
    }

    /**
     * 设置脸部检测参数
     *
     * @return
     */
    private FaceDetector getFaceDetector() {
        if (this.faceDetector == null) {
            Builder optionsBuilder = MobileSSDClientOptions.newBuilder();
            optionsBuilder.setMobileSsdClientName("MobileSSDV1FaceClient");
            optionsBuilder.setMaxDetections(3);
            optionsBuilder.setScoreThreshold(0.0f);
            optionsBuilder.setIouThreshold(0.3f);
            this.faceDetector = FaceDetector.createFromOptions((MobileSSDClientOptions) optionsBuilder.build());
        }
        return this.faceDetector;
    }

    /**
     * 停止检测
     */
    public void abortAllDetection() {
        this.abortDetection = true;
    }

    /**
     * 恢复检测
     */
    public void resumeDetection() {
        this.abortDetection = false;
    }

    public void closeDetection() {
        if (this.objectDetector != null) {
            this.objectDetector.close();
        }
        if (this.faceDetector != null) {
            this.faceDetector.close();
        }
        this.objectDetector = null;
        this.faceDetector = null;
    }

    public static RectF getScaledRectFromFace(Face face, RectF targetRect) {
        BoundingBox bb = face.getBoundingBox();
        return new RectF((bb.getX1() * targetRect.width()) + targetRect.left, (bb.getY1() * targetRect.height()) + targetRect.top, (bb.getX2() * targetRect.width()) + targetRect.left, (bb.getY2() * targetRect.height()) + targetRect.top);
    }

    public static RectF getRectFromFace(Face face) {
        BoundingBox bb = face.getBoundingBox();
        return new RectF(bb.getX1(), bb.getY1(), bb.getX2(), bb.getY2());
    }
}
