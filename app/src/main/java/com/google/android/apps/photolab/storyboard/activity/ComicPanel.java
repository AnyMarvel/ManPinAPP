package com.google.android.apps.photolab.storyboard.activity;

import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import com.google.android.apps.photolab.storyboard.activity.IComicMoveable.ComicMoveableKind;
import com.google.android.apps.photolab.storyboard.pipeline.AssetLoader;
import com.google.android.apps.photolab.storyboard.pipeline.ObjectDetection;
import com.google.protos.humansensing.FaceProtos.Face;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class ComicPanel implements IComicMoveable {
    private static final float MAX_ZOOM = 1.2f;
    private static final int MIN_PIXELS = 300;
    private static final String TAG = "ComicPanel";
    public int backgroundColor;
    private int bitmapMergeOffsetX = 0;
    private int bitmapMergeOffsetY = 0;
    private int bitmapOffsetX = 0;
    private int bitmapOffsetY = 0;
    private RectF cachedFrame;
    private Path cachedInnerPath;
    private RectF cachedMergedFrame;
    private Path cachedPath;
    public Caption caption;
    public ComicBitmapInstance comicBitmapInstance;
    public ObjectDetection detection;
    public Face faceDetection;
    public float margin = 10.0f;
    public ArrayList<ComicPanel> mergedPanels = new ArrayList();
    public int panelNumber;
    private PointF[] panelPoints = new PointF[]{new PointF(0.0f, 0.0f), new PointF(1.0f, 0.0f), new PointF(1.0f, 1.0f), new PointF(0.0f, 1.0f)};
    private Random rnd;
    public SpeechBubble speechBubble;
    private float zoom = 1.0f;

    public ComicPanel(int backgroundColor, int panelNumber, int imageIndex) {
        this.backgroundColor = backgroundColor;
        this.panelNumber = panelNumber;
        this.comicBitmapInstance = new ComicBitmapInstance(imageIndex);
        this.mergedPanels.add(this);
        this.rnd = new Random(System.currentTimeMillis());
    }

    public float getArea() {
        return this.cachedFrame.width() * this.cachedFrame.height();
    }

    public float getAspect() {
        return this.cachedFrame.width() / this.cachedFrame.height();
    }

    public boolean isHorizontal() {
        return this.cachedFrame.width() > this.cachedFrame.height();
    }

    public void reset() {
        this.panelNumber = 0;
        this.cachedFrame.setEmpty();
        this.cachedMergedFrame.setEmpty();
        this.backgroundColor = (int) (-1.61061274E9f + (this.rnd.nextFloat() * 1.6777215E7f));
        this.mergedPanels.clear();
    }

    public void resetImageTransform() {
        this.bitmapOffsetX = 0;
        this.bitmapOffsetY = 0;
        this.bitmapMergeOffsetX = 0;
        this.bitmapMergeOffsetY = 0;
        this.comicBitmapInstance.setImageScale(1.0f);
        invalidate();
    }

    public Path getPath() {
        return this.cachedPath;
    }

    public Path getInnerPath() {
        return this.cachedInnerPath;
    }

    public float fillScale() {
        if (usingDetection()) {
            return 1.0f;
        }
        return Math.max(this.cachedMergedFrame.width() / ((float) this.comicBitmapInstance.getImageWidth()), this.cachedMergedFrame.height() / ((float) this.comicBitmapInstance.getImageHeight()));
    }

    public float getAspectMatchLevel(RectF rect) {
        return (this.cachedMergedFrame.width() / this.cachedMergedFrame.height()) / (rect.width() / rect.height());
    }

    public float getSizeMatchLevel(RectF rect) {
        return (this.cachedMergedFrame.width() * this.cachedMergedFrame.height()) / (rect.width() * rect.height());
    }

    public boolean contains(float x, float y) {
        return isPointInside(x, y, this.panelPoints);
    }

    public int getImageIndex() {
        int result = this.comicBitmapInstance.getImageIndex();
        if (this.mergedPanels.size() > 1) {
            return ((ComicPanel) this.mergedPanels.get(0)).comicBitmapInstance.getImageIndex();
        }
        return result;
    }

    public void setImageIndex(int imageIndex) {
        this.comicBitmapInstance.setImageIndex(imageIndex);
        if (this.mergedPanels.size() > 1) {
            Iterator it = this.mergedPanels.iterator();
            while (it.hasNext()) {
                ComicPanel p = (ComicPanel) it.next();
                if (p != this) {
                    p.comicBitmapInstance.setImageIndex(imageIndex);
                }
            }
        }
    }

    public RectF getPanelFrame() {
        return this.cachedFrame;
    }

    public void setPanelPoints(PointF tl, PointF tr, PointF br, PointF bl) {
        tl.x += this.margin;
        tl.y += this.margin;
        tr.x -= this.margin;
        tr.y += this.margin;
        br.x -= this.margin;
        br.y -= this.margin;
        bl.x += this.margin;
        bl.y -= this.margin;
        this.panelPoints = new PointF[]{tl, tr, br, bl};
        this.cachedFrame = getBoundingBox();
        this.cachedMergedFrame = getMergedBoundingBox();
        genPath();
    }

    public float getImageCenterX() {
        return this.comicBitmapInstance.getImageCenterX();
    }

    public float getImageCenterY() {
        return this.comicBitmapInstance.getImageCenterY();
    }

    public void setImageCenter(float imageCenterX, float imageCenterY) {
        this.comicBitmapInstance.setImageCenterX(imageCenterX);
        this.comicBitmapInstance.setImageCenterY(imageCenterY);
    }

    public float getImageScale() {
        return this.comicBitmapInstance.getImageScale();
    }

    public void setImageScale(float imageScale) {
        this.comicBitmapInstance.setImageScale(imageScale);
    }

    private void invalidate() {
        this.comicBitmapInstance.setImageScale(Math.max(this.cachedMergedFrame.width() / ((float) this.comicBitmapInstance.getImageWidth()), this.cachedMergedFrame.height() / ((float) this.comicBitmapInstance.getImageHeight())));
        validateValues();
    }

    public ComicMoveableKind getMoveableKind() {
        return ComicMoveableKind.PANEL;
    }

    public int getBitmapOffsetX() {
        return this.bitmapOffsetX;
    }

    public void setBitmapOffsetX(int bitmapOffsetX) {
        this.bitmapOffsetX = bitmapOffsetX;
        Iterator it = this.mergedPanels.iterator();
        while (it.hasNext()) {
            ComicPanel p = (ComicPanel) it.next();
            if (p != this) {
                p.bitmapOffsetX = this.bitmapOffsetX;
            }
        }
    }

    public int getBitmapOffsetY() {
        return this.bitmapOffsetY;
    }

    public void setBitmapOffsetY(int bitmapOffsetY) {
        this.bitmapOffsetY = bitmapOffsetY;
        Iterator it = this.mergedPanels.iterator();
        while (it.hasNext()) {
            ComicPanel p = (ComicPanel) it.next();
            if (p != this) {
                p.bitmapOffsetY = this.bitmapOffsetY;
            }
        }
    }

    public int getBitmapMergeOffsetX() {
        return this.bitmapMergeOffsetX;
    }

    public int getBitmapMergeOffsetY() {
        return this.bitmapMergeOffsetY;
    }

    public float getZoom() {
        return this.zoom;
    }

    public void setZoom(float zoom) {
        this.zoom = zoom;
        Iterator it = this.mergedPanels.iterator();
        while (it.hasNext()) {
            ComicPanel p = (ComicPanel) it.next();
            if (p != this) {
                p.bitmapOffsetX = this.bitmapOffsetX;
                p.bitmapOffsetY = this.bitmapOffsetY;
                p.zoom = this.zoom;
            }
        }
    }

    public boolean useDetection() {
        return this.comicBitmapInstance.getDetectionSet() != null && this.comicBitmapInstance.getDetectionSet().size() > 0 && this.mergedPanels.size() < 2;
    }

    private boolean usingDetection() {
        return this.detection != null && useDetection();
    }

    public void centerImage() {
        this.cachedFrame = getBoundingBox();
        this.cachedMergedFrame = getMergedBoundingBox();
        if (AssetLoader.isProcessing() || !usingDetection()) {
            centerImageOnBounds();
        } else {
            centerImageOnDetection();
        }
    }

    private void centerImageOnBounds() {
        float scale = fillScale();
        setZoom(1.0f);
        int overHeight = (int) (((((float) this.comicBitmapInstance.getImageHeight()) * scale) * this.zoom) - this.cachedMergedFrame.height());
        this.bitmapOffsetX = (int) (((float) (-((int) (((((float) this.comicBitmapInstance.getImageWidth()) * scale) * this.zoom) - this.cachedMergedFrame.width())))) / 2.0f);
        this.bitmapOffsetY = (int) (((float) (-overHeight)) / 2.0f);
        this.bitmapMergeOffsetX = (int) (this.cachedMergedFrame.left - this.cachedFrame.left);
        this.bitmapMergeOffsetY = (int) (this.cachedMergedFrame.top - this.cachedFrame.top);
    }

    private void centerImageOnDetection() {
        RectF bmpRect = this.comicBitmapInstance.getRectF();
        RectF detectRect = this.detection.getScaledRect(bmpRect);
        RectF cf = this.cachedMergedFrame;
        setZoom(Math.max(Math.min(MAX_ZOOM, Math.min(Math.max(detectRect.width() / 300.0f, detectRect.height() / 300.0f), Math.min(cf.width() / detectRect.width(), cf.height() / detectRect.height()))), Math.max(cf.width() / bmpRect.width(), cf.height() / bmpRect.height())));
        PointF detectCenter = this.detection.getScaledCenter(new RectF(0.0f, 0.0f, bmpRect.width() * this.zoom, bmpRect.height() * this.zoom));
        PointF panelCenter = new PointF(cf.centerX(), cf.centerY());
        int offsetY = (int) (panelCenter.y - (detectCenter.y + cf.top));
        this.bitmapOffsetX = (int) (panelCenter.x - (detectCenter.x + cf.left));
        this.bitmapOffsetY = offsetY;
        this.bitmapMergeOffsetX = 0;
        this.bitmapMergeOffsetY = 0;
        validateValues();
    }

    private void validateValues() {
        float scale = fillScale();
        int overHeight = (int) (((((float) this.comicBitmapInstance.getImageHeight()) * scale) * this.zoom) - this.cachedMergedFrame.height());
        this.bitmapOffsetX = Math.min(Math.max(-((int) (((((float) this.comicBitmapInstance.getImageWidth()) * scale) * this.zoom) - this.cachedMergedFrame.width())), this.bitmapOffsetX), 0);
        this.bitmapOffsetY = Math.min(Math.max(-overHeight, this.bitmapOffsetY), 0);
    }

    private RectF getBoundingBox() {
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxX = Float.MIN_VALUE;
        float maxY = Float.MIN_VALUE;
        for (PointF pt : this.panelPoints) {
            minX = Math.min(pt.x, minX);
            minY = Math.min(pt.y, minY);
            maxX = Math.max(pt.x, maxX);
            maxY = Math.max(pt.y, maxY);
        }
        return new RectF(minX, minY, maxX, maxY);
    }

    private RectF getMergedBoundingBox() {
        RectF bounds = getBoundingBox();
        Iterator it = this.mergedPanels.iterator();
        while (it.hasNext()) {
            ComicPanel cp = (ComicPanel) it.next();
            if (!(cp == null || cp == this)) {
                bounds.union(cp.getBoundingBox());
            }
        }
        return bounds;
    }

    private void genPath() {
        if (this.cachedFrame != null) {
            this.cachedPath = new Path();
            this.cachedPath.moveTo(this.panelPoints[0].x, this.panelPoints[0].y);
            this.cachedPath.lineTo(this.panelPoints[1].x, this.panelPoints[1].y);
            this.cachedPath.lineTo(this.panelPoints[2].x, this.panelPoints[2].y);
            this.cachedPath.lineTo(this.panelPoints[3].x, this.panelPoints[3].y);
            this.cachedPath.lineTo(this.panelPoints[0].x, this.panelPoints[0].y);
            genInnerPath(4);
        }
    }

    private void genInnerPath(int inset) {
        if (this.cachedFrame != null) {
            this.cachedInnerPath = new Path();
            this.cachedInnerPath.moveTo(this.panelPoints[0].x + ((float) inset), this.panelPoints[0].y + ((float) inset));
            this.cachedInnerPath.lineTo(this.panelPoints[1].x - ((float) inset), this.panelPoints[1].y + ((float) inset));
            this.cachedInnerPath.lineTo(this.panelPoints[2].x - ((float) inset), this.panelPoints[2].y - ((float) inset));
            this.cachedInnerPath.lineTo(this.panelPoints[3].x + ((float) inset), this.panelPoints[3].y - ((float) inset));
            this.cachedInnerPath.lineTo(this.panelPoints[0].x + ((float) inset), this.panelPoints[0].y + ((float) inset));
        }
    }

    public static boolean isPointInside(float x, float y, PointF[] points) {
        int i;
        boolean result = false;
        float minX = points[0].x;
        float maxX = points[0].x;
        float minY = points[0].y;
        float maxY = points[0].y;
        for (i = 1; i < points.length; i++) {
            minX = Math.min(points[i].x, minX);
            maxX = Math.max(points[i].x, maxX);
            minY = Math.min(points[i].y, minY);
            maxY = Math.max(points[i].y, maxY);
        }
        if (x >= minX && x <= maxX && y >= minY && y <= maxY) {
            i = 0;
            int j = points.length - 1;
            while (i < points.length) {
                int i2;
                if (points[i].y > y) {
                    i2 = 1;
                } else {
                    i2 = 0;
                }
                if (i2 != (points[j].y > y ? 1 : 0) && x < (((points[j].x - points[i].x) * (y - points[i].y)) / (points[j].y - points[i].y)) + points[i].x) {
                    if (result) {
                        result = false;
                    } else {
                        result = true;
                    }
                }
                j = i;
                i++;
            }
        }
        return result;
    }

    public String toString() {
        Rect r = new Rect();
        this.cachedFrame.round(r);
        int i = this.panelNumber;
        String valueOf = String.valueOf(r);
        return new StringBuilder(String.valueOf(valueOf).length() + 43).append("ComicPanel: ").append(i).append(" ").append(valueOf).append(" merged:").append(this.mergedPanels.size()).toString();
    }
}
