package com.google.android.apps.photolab.storyboard.activity;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Size;

import com.google.android.apps.photolab.storyboard.activity.IComicMoveable.ComicMoveableKind;
import com.google.android.apps.photolab.storyboard.pipeline.MediaManager;
import com.google.android.apps.photolab.storyboard.pipeline.ObjectDetectionSet;
//import com.google.protos.humansensing.Faces;

public class ComicBitmapInstance implements IComicMoveable {
    private static final String TAG = "ComicBitmapInstance";
    public Rect[] faceRects;
    private float imageCenterX = 0.5f;
    private float imageCenterY = 0.5f;
    private int imageIndex;
    private float imageScale = 1.0f;
    public SpeechBubble speechBubble;
    public Point speechOrigin;

    public ComicBitmapInstance(int imageIndex) {
        setImageIndex(imageIndex);
    }

    @Override
    public ComicMoveableKind getMoveableKind() {
        return ComicMoveableKind.BITMAP;
    }

    public int getImageIndex() {
        return this.imageIndex;
    }

    public Rect getImageRect() {
        return new Rect(0, 0, getImageWidth(), getImageHeight());
    }

    public void setImageIndex(int index) {
        this.imageIndex = Math.max(0, Math.min(index, MediaManager.instance().getCount() - 1));
    }

    public Bitmap getImage() {
        return MediaManager.instance().getImageByIndex(this.imageIndex);
    }

    public ObjectDetectionSet getDetectionSet() {
        return MediaManager.instance().getDetectionsByIndex(this.imageIndex);
    }

//    public Faces getFaceDetections() {
//        return MediaManager.instance().getFacesByIndex(this.imageIndex);
//    }

    public RectF getRectF() {
        return new RectF(0.0f, 0.0f, (float) getImageWidth(), (float) getImageHeight());
    }

    private Size getScaledSize() {
        return new Size((int) (((float) getImageWidth()) * getImageScale()), (int) (((float) getImageHeight()) * getImageScale()));
    }

    public int getImageWidth() {
        Bitmap bmp = MediaManager.instance().getImageByIndex(this.imageIndex);
        if (bmp != null) {
            return bmp.getWidth();
        }
        return 1;
    }

    public int getImageHeight() {
        Bitmap bmp = MediaManager.instance().getImageByIndex(this.imageIndex);
        if (bmp != null) {
            return bmp.getHeight();
        }
        return 1;
    }

    public float getImageCenterX() {
        return this.imageCenterX;
    }

    public void setImageCenterX(float imageCenterX) {
        this.imageCenterX = imageCenterX;
    }

    public float getImageCenterY() {
        return this.imageCenterY;
    }

    public void setImageCenterY(float imageCenterY) {
        this.imageCenterY = imageCenterY;
    }

    public float getImageScale() {
        return this.imageScale;
    }

    public void setImageScale(float imageScale) {
        this.imageScale = imageScale;
    }
}
