package androidx.media.filterfw.geometry;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;

public class Quad {
    private final PointF mBottomLeft;
    private final PointF mBottomRight;
    private final PointF mTopLeft;
    private final PointF mTopRight;

    public static Quad unitQuad() {
        return new Quad(0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
    }

    public static Quad fromRect(RectF rect) {
        return new Quad(new PointF(rect.left, rect.top), new PointF(rect.right, rect.top), new PointF(rect.left, rect.bottom), new PointF(rect.right, rect.bottom));
    }

    public static Quad fromRect(float x, float y, float width, float height) {
        return new Quad(new PointF(x, y), new PointF(x + width, y), new PointF(x, y + height), new PointF(x + width, y + height));
    }

    public static Quad fromLineAndHeight(PointF topLeft, PointF topRight, float height) {
        PointF dp = new PointF(topRight.x - topLeft.x, topRight.y - topLeft.y);
        float len = dp.length();
        PointF np = new PointF((dp.y / len) * height, (dp.x / len) * height);
        return new Quad(topLeft, topRight, new PointF(topLeft.x - np.x, topLeft.y + np.y), new PointF(topRight.x - np.x, topRight.y + np.y));
    }

    public static Quad fromRotatedRect(RectF rect, float angle) {
        return fromRect(rect).rotated(angle);
    }

    public static Quad fromTransformedRect(RectF rect, Matrix matrix) {
        return fromRect(rect).transformed(matrix);
    }

    public static Matrix getTransform(Quad source, Quad target) {
        Matrix transform = new Matrix();
        transform.setPolyToPoly(source.asCoords(), 0, target.asCoords(), 0, 3);
        return transform;
    }

    public PointF topLeft() {
        return this.mTopLeft;
    }

    public PointF topRight() {
        return this.mTopRight;
    }

    public PointF bottomLeft() {
        return this.mBottomLeft;
    }

    public PointF bottomRight() {
        return this.mBottomRight;
    }

    public Quad rotated(float angle) {
        PointF center = center();
        float cosa = (float) Math.cos((double) angle);
        float sina = (float) Math.sin((double) angle);
        return new Quad(rotatePoint(topLeft(), center, cosa, sina), rotatePoint(topRight(), center, cosa, sina), rotatePoint(bottomLeft(), center, cosa, sina), rotatePoint(bottomRight(), center, cosa, sina));
    }

    public Quad transformed(Matrix matrix) {
        float[] points = asCoords();
        matrix.mapPoints(points);
        return new Quad(points);
    }

    public PointF center() {
        return new PointF((this.mTopLeft.x + this.mBottomRight.x) / 2.0f, (this.mTopLeft.y + this.mBottomRight.y) / 2.0f);
    }

    public float[] asCoords() {
        return new float[]{this.mTopLeft.x, this.mTopLeft.y, this.mTopRight.x, this.mTopRight.y, this.mBottomLeft.x, this.mBottomLeft.y, this.mBottomRight.x, this.mBottomRight.y};
    }

    public Quad grow(float factor) {
        PointF pc = center();
        return new Quad(((this.mTopLeft.x - pc.x) * factor) + pc.x, ((this.mTopLeft.y - pc.y) * factor) + pc.y, ((this.mTopRight.x - pc.x) * factor) + pc.x, ((this.mTopRight.y - pc.y) * factor) + pc.y, ((this.mBottomLeft.x - pc.x) * factor) + pc.x, ((this.mBottomLeft.y - pc.y) * factor) + pc.y, ((this.mBottomRight.x - pc.x) * factor) + pc.x, ((this.mBottomRight.y - pc.y) * factor) + pc.y);
    }

    public Quad scale(float factor) {
        return new Quad(this.mTopLeft.x * factor, this.mTopLeft.y * factor, this.mTopRight.x * factor, this.mTopRight.y * factor, this.mBottomLeft.x * factor, this.mBottomLeft.y * factor, this.mBottomRight.x * factor, this.mBottomRight.y * factor);
    }

    public Quad scale2(float sx, float sy) {
        return new Quad(this.mTopLeft.x * sx, this.mTopLeft.y * sy, this.mTopRight.x * sx, this.mTopRight.y * sy, this.mBottomLeft.x * sx, this.mBottomLeft.y * sy, this.mBottomRight.x * sx, this.mBottomRight.y * sy);
    }

    public PointF xEdge() {
        return new PointF(this.mTopRight.x - this.mTopLeft.x, this.mTopRight.y - this.mTopLeft.y);
    }

    public PointF yEdge() {
        return new PointF(this.mBottomLeft.x - this.mTopLeft.x, this.mBottomLeft.y - this.mTopLeft.y);
    }

    public RectF getEnclosingRectF() {
        return new RectF(Math.min(Math.min(this.mTopLeft.x, this.mTopRight.x), Math.min(this.mBottomLeft.x, this.mBottomRight.x)), Math.min(Math.min(this.mTopLeft.y, this.mTopRight.y), Math.min(this.mBottomLeft.y, this.mBottomRight.y)), Math.max(Math.max(this.mTopLeft.x, this.mTopRight.x), Math.max(this.mBottomLeft.x, this.mBottomRight.x)), Math.max(Math.max(this.mTopLeft.y, this.mTopRight.y), Math.max(this.mBottomLeft.y, this.mBottomRight.y)));
    }

    public String toString() {
        float f = this.mTopLeft.x;
        float f2 = this.mTopLeft.y;
        float f3 = this.mTopRight.x;
        float f4 = this.mTopRight.y;
        float f5 = this.mBottomLeft.x;
        float f6 = this.mBottomLeft.y;
        float f7 = this.mBottomRight.x;
        return "Quad(" + f + ", " + f2 + ", " + f3 + ", " + f4 + ", " + f5 + ", " + f6 + ", " + f7 + ", " + this.mBottomRight.y + ")";
    }

    private Quad(PointF topLeft, PointF topRight, PointF bottomLeft, PointF bottomRight) {
        this.mTopLeft = topLeft;
        this.mTopRight = topRight;
        this.mBottomLeft = bottomLeft;
        this.mBottomRight = bottomRight;
    }

    private Quad(float x0, float y0, float x1, float y1, float x2, float y2, float x3, float y3) {
        this.mTopLeft = new PointF(x0, y0);
        this.mTopRight = new PointF(x1, y1);
        this.mBottomLeft = new PointF(x2, y2);
        this.mBottomRight = new PointF(x3, y3);
    }

    private Quad(float[] points) {
        this.mTopLeft = new PointF(points[0], points[1]);
        this.mTopRight = new PointF(points[2], points[3]);
        this.mBottomLeft = new PointF(points[4], points[5]);
        this.mBottomRight = new PointF(points[6], points[7]);
    }

    private static PointF rotatePoint(PointF p, PointF c, float cosa, float sina) {
        return new PointF((((p.x - c.x) * cosa) - ((p.y - c.y) * sina)) + c.x, (((p.x - c.x) * sina) + ((p.y - c.y) * cosa)) + c.y);
    }
}
