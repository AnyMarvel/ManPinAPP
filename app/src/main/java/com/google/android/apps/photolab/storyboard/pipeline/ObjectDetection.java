package com.google.android.apps.photolab.storyboard.pipeline;

import android.graphics.PointF;
import android.graphics.RectF;

import com.google.protos.humansensing.FaceProtos.Face;
import com.google.protos.humansensing.FaceProtos.Face.BoundingBox;

public class ObjectDetection {
    private static final String TAG = "ObjectDetection";
    private static float[][] adjustHorizontalMap = new float[][]{new float[]{0.5f, 0.0f, 0.0f, 0.0f}, new float[]{0.0f, 0.0f, 0.5f, 0.0f}, new float[]{0.2f, 0.0f, 0.2f, 0.0f}, new float[]{0.2f, 0.0f, 0.4f, 0.0f}, new float[]{0.4f, 0.0f, 0.2f, 0.0f}, new float[]{0.0f, 0.0f, 0.3f, 0.0f}, new float[]{0.3f, 0.0f, 0.0f, 0.0f}, new float[]{0.2f, 0.2f, 0.2f, 0.2f}, new float[]{0.1f, 0.1f, 0.1f, 0.3f}, new float[]{0.2f, 0.2f, 0.2f, 0.6f}};
    private static float[][] adjustMap = new float[][]{new float[]{0.5f, 0.0f, 0.0f, 0.0f}, new float[]{0.0f, 0.5f, 0.0f, 0.0f}, new float[]{0.0f, 0.0f, 0.5f, 0.0f}, new float[]{0.0f, 0.0f, 0.0f, 0.5f}, new float[]{0.2f, 0.0f, 0.2f, 0.0f}, new float[]{0.0f, 0.2f, 0.0f, 0.2f}, new float[]{0.2f, 0.2f, 0.2f, 0.2f}, new float[]{0.1f, 0.1f, 0.3f, 0.3f}, new float[]{0.1f, 0.1f, 0.1f, 0.3f}, new float[]{0.2f, 0.2f, 0.2f, 0.6f}};
    private static float[][] adjustVerticalMap = new float[][]{new float[]{0.0f, 0.5f, 0.0f, 0.0f}, new float[]{0.0f, 0.0f, 0.0f, 0.5f}, new float[]{0.0f, 0.2f, 0.0f, 0.2f}, new float[]{0.0f, 0.2f, 0.0f, 0.4f}, new float[]{0.0f, 0.0f, 0.0f, 0.3f}, new float[]{0.0f, 0.3f, 0.0f, 0.0f}, new float[]{0.0f, 0.1f, 0.0f, 0.1f}, new float[]{0.2f, 0.2f, 0.2f, 0.2f}, new float[]{0.1f, 0.1f, 0.1f, 0.3f}, new float[]{0.2f, 0.2f, 0.2f, 0.6f}};
    public String category;
    public int imageHeight;
    public int imageWidth;
    public boolean isFace = false;
    public float maxX;
    public float maxY;
    public float minX;
    public float minY;
    public float score;

    ObjectDetection() {
    }

    public ObjectDetection(Face face, int imageWidth, int imageHeight) {
        this.score = face.hasConfidence() ? face.getConfidence() : 0.0f;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        BoundingBox bb = face.getBoundingBox();
        this.minX = bb.getX1() / ((float) imageWidth);
        this.minY = bb.getY1() / ((float) imageHeight);
        this.maxX = bb.getX2() / ((float) imageWidth);
        this.maxY = bb.getY2() / ((float) imageHeight);
        this.category = "face";
        this.isFace = true;
        if (getPixelArea() < 90000.0f) {
            expandByPixels(100, 100, 100, 200);
        }
    }

    public void ensureMinimumSize() {
        clamp();
        float pxArea = getPixelArea();
        if (pxArea > 0.0f && pxArea < 90000.0f) {
            expandByPixels(200, 200, 200, 200);
        }
    }

    private void clamp() {
        this.minX = Math.max(0.0f, this.minX);
        this.minY = Math.max(0.0f, this.minY);
        this.maxX = Math.min(1.0f, this.maxX);
        this.maxY = Math.min(1.0f, this.maxY);
    }

    public RectF getRect() {
        return new RectF(this.minX, this.minY, this.maxX, this.maxY);
    }

    public RectF getExpandedRect(float left, float top, float right, float bottom) {
        return new RectF(Math.max(0.0f, this.minX - left), Math.max(0.0f, this.minY - top), Math.min(1.0f, this.maxX + right), Math.min(1.0f, this.maxY + bottom));
    }

    private void expand(float left, float top, float right, float bottom) {
        this.minX = Math.max(0.0f, this.minX - left);
        this.minY = Math.max(0.0f, this.minY - top);
        this.maxX = Math.min(1.0f, this.maxX + right);
        this.maxY = Math.min(1.0f, this.maxY + bottom);
    }

    private void expandByPixels(int leftPx, int topPx, int rightPx, int bottomPx) {
        float top = ((float) topPx) / ((float) this.imageHeight);
        float right = ((float) rightPx) / ((float) this.imageWidth);
        float bottom = ((float) bottomPx) / ((float) this.imageHeight);
        this.minX = Math.max(0.0f, this.minX - (((float) leftPx) / ((float) this.imageWidth)));
        this.minY = Math.max(0.0f, this.minY - top);
        this.maxX = Math.min(1.0f, this.maxX + right);
        this.maxY = Math.min(1.0f, this.maxY + bottom);
    }

    private void expandByPercent(float[] ltrb) {
        if (ltrb.length > 3) {
            float top = ltrb[1] * (this.maxY - this.minY);
            float right = ltrb[2] * (this.maxX - this.minX);
            float bottom = ltrb[3] * (this.maxY - this.minY);
            this.minX = Math.max(0.0f, this.minX - (ltrb[0] * (this.maxX - this.minX)));
            this.minY = Math.max(0.0f, this.minY - top);
            this.maxX = Math.min(1.0f, this.maxX + right);
            this.maxY = Math.min(1.0f, this.maxY + bottom);
        }
    }

    public RectF getScaledRect(RectF targetRect) {
        return new RectF((this.minX * targetRect.width()) + targetRect.left, (this.minY * targetRect.height()) + targetRect.top, (this.maxX * targetRect.width()) + targetRect.left, (this.maxY * targetRect.height()) + targetRect.top);
    }

    public float getArea() {
        return (this.maxX - this.minX) * (this.maxY - this.minY);
    }

    public float getPixelArea() {
        return (((this.maxX - this.minX) * ((float) this.imageWidth)) * (this.maxY - this.minY)) * ((float) this.imageHeight);
    }

    public float getAspect() {
        return (this.maxX - this.minX) / (this.maxY - this.minY);
    }

    public NineBox getLocationInPanel() {
        return NineBox.getNineBoxFromDirections(getHorizontalDirection(), getVerticalDirection());
    }

    private NineBox getHorizontalSection() {
        PointF nc = getNormalizedCenter();
        if (nc.x < 0.333f) {
            return NineBox.LEFT;
        }
        return nc.x < 0.6667f ? NineBox.CENTER : NineBox.RIGHT;
    }

    private NineBox getVerticalSection() {
        PointF nc = getNormalizedCenter();
        if (nc.y < 0.333f) {
            return NineBox.TOP;
        }
        return nc.y < 0.6667f ? NineBox.CENTER : NineBox.BOTTOM;
    }

    private int getHorizontalDirection() {
        PointF nc = getNormalizedCenter();
        if (nc.x < 0.333f) {
            return -1;
        }
        return nc.x < 0.6667f ? 0 : 1;
    }

    private int getVerticalDirection() {
        PointF nc = getNormalizedCenter();
        if (nc.y < 0.333f) {
            return -1;
        }
        return nc.y < 0.6667f ? 0 : 1;
    }

    public PointF getCenter() {
        return new PointF(((this.maxX - this.minX) / 2.0f) + this.minX, ((this.maxY - this.minY) / 2.0f) + this.minY);
    }

    public PointF getNormalizedCenter() {
        return new PointF((((this.maxX - this.minX) / 2.0f) + this.minX) / ((float) this.imageWidth), (((this.maxY - this.minY) / 2.0f) + this.minY) / ((float) this.imageHeight));
    }

    public PointF getScaledCenter(RectF targetRect) {
        RectF r = getScaledRect(targetRect);
        return new PointF(r.centerX(), r.centerY());
    }

    public float getDistanceFromCenter() {
        float offsetX = (((this.maxX - this.minX) / 2.0f) + this.minX) - 0.5f;
        float offsetY = (((this.maxY - this.minY) / 2.0f) + this.minY) - 0.5f;
        return (float) Math.sqrt((double) ((offsetX * offsetX) + (offsetY * offsetY)));
    }

    public float getSquaredDistanceFromCenter() {
        float offsetX = (((this.maxX - this.minX) / 2.0f) + this.minX) - 0.5f;
        float offsetY = (((this.maxY - this.minY) / 2.0f) + this.minY) - 0.5f;
        return (offsetX * offsetX) + (offsetY * offsetY);
    }

    public ObjectDetection getRandomAspectAdjustment(boolean horizontal) {
        ObjectDetection result = duplicate();
        result.expandByPercent((horizontal ? adjustHorizontalMap : adjustVerticalMap)[ComicUtils.rnd.nextInt(adjustMap.length)]);
        return result;
    }

    public ObjectDetection duplicate() {
        ObjectDetection result = new ObjectDetection();
        result.imageWidth = this.imageWidth;
        result.imageHeight = this.imageHeight;
        result.minX = this.minX;
        result.minY = this.minY;
        result.maxX = this.maxX;
        result.maxY = this.maxY;
        result.score = this.score;
        result.category = this.category;
        result.isFace = this.isFace;
        return result;
    }

    private String rectToString() {
        float f = this.minX;
        float f2 = this.minY;
        float f3 = this.maxX;
        return "[" + f + "," + f2 + " " + f3 + "," + this.maxY + "]";
    }

    public String toString() {
        String str = this.category;
        float f = this.score;
        String rectToString = rectToString();
        return new StringBuilder((String.valueOf(str).length() + 34) + String.valueOf(rectToString).length()).append("ObjectDetection:").append(str).append(", ").append(f).append(" ").append(rectToString).toString();
    }
}
