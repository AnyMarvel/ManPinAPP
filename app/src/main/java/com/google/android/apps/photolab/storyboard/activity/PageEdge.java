package com.google.android.apps.photolab.storyboard.activity;

import android.graphics.PointF;

import com.google.android.apps.photolab.storyboard.activity.IComicMoveable.ComicMoveableKind;

import java.util.ArrayList;

public class PageEdge implements IComicMoveable, Comparable<PageEdge> {
    private int endIndex = Integer.MAX_VALUE;
    private float lineA;
    private float lineB;
    private float lineC;
    private float location;
    private float location2;
    private PageEdgeKind pageEdgeKind;
    private int startIndex = 0;

    public enum PageEdgeKind {
        HORIZONTAL,
        VERTICAL,
        HORIZONTAL_ANGLED,
        VERTICAL_ANGLED,
        HORIZONTAL_CURVED,
        VERTICAL_CURVED
    }

    public int getStartIndex() {
        return this.startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public int getEndIndex() {
        return this.endIndex;
    }

    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }

    public float getLocation() {
        return this.location;
    }

    public void setLocation(float location) {
        this.location = location;
    }

    public float getLocation2() {
        return this.location2;
    }

    public void setLocation2(float location2) {
        this.location2 = location2;
    }

    public PageEdgeKind getPageEdgeKind() {
        return this.pageEdgeKind;
    }

    public PageEdge(PageEdgeKind pageEdgeKind, float location) {
        this.pageEdgeKind = pageEdgeKind;
        setLocation(location);
        setLocation2(location);
        setLineEquation();
    }

    public PageEdge(PageEdgeKind pageEdgeKind, float location, int startIndex, int endIndex) {
        this.pageEdgeKind = pageEdgeKind;
        setLocation(location);
        setLocation2(location);
        setStartIndex(startIndex);
        setEndIndex(endIndex);
        setLineEquation();
    }

    public PageEdge(PageEdgeKind pageEdgeKind, float[] locations) {
        this.pageEdgeKind = pageEdgeKind;
        setLocation(locations[0]);
        setLocation2(locations[1]);
        setLineEquation();
    }

    public PageEdge(PageEdgeKind pageEdgeKind, float[] locations, int startIndex, int endIndex) {
        this.pageEdgeKind = pageEdgeKind;
        setLocation(locations[0]);
        setLocation2(locations[1]);
        setStartIndex(startIndex);
        setEndIndex(endIndex);
        setLineEquation();
    }

    public PageEdge(PageEdge other) {
        this.pageEdgeKind = other.getPageEdgeKind();
        setLocation(other.getLocation());
        setLocation2(other.getLocation2());
        setStartIndex(other.getStartIndex());
        setEndIndex(other.getEndIndex());
        this.lineA = other.lineA;
        this.lineB = other.lineB;
        this.lineC = other.lineC;
    }

    private void setLineEquation() {
        if (isHorizontal()) {
            this.lineA = getLocation2() - getLocation();
            this.lineB = -1.0f;
            this.lineC = (this.lineA * 0.0f) + (this.lineB * getLocation());
            return;
        }
        this.lineA = 1.0f;
        this.lineB = getLocation() - getLocation2();
        this.lineC = (this.lineA * getLocation()) + (this.lineB * 0.0f);
    }

    public ComicMoveableKind getMoveableKind() {
        return ComicMoveableKind.EDGE;
    }

    public boolean isHorizontal() {
        return getPageEdgeKind() == PageEdgeKind.HORIZONTAL || getPageEdgeKind() == PageEdgeKind.HORIZONTAL_ANGLED || getPageEdgeKind() == PageEdgeKind.HORIZONTAL_CURVED;
    }

    public boolean isAngled() {
        return getPageEdgeKind() == PageEdgeKind.HORIZONTAL_ANGLED || getPageEdgeKind() == PageEdgeKind.VERTICAL_ANGLED;
    }

    public boolean isOppositeEdge(PageEdge edge) {
        return (isHorizontal() && !edge.isHorizontal()) || (!isHorizontal() && edge.isHorizontal());
    }

    public boolean isInteriorEdge() {
        return (this.location == 0.0f && this.location2 == 0.0f) ? false : true;
    }

    boolean isTouchingPoint(float x, float y, float tolerance, ArrayList<PageEdge> oppositeEdges) {
        if (isHorizontal()) {
            if (Math.abs(y - getLocation()) >= tolerance || x <= ((PageEdge) oppositeEdges.get(getStartIndex())).getLocation()) {
                return false;
            }
            if (getEndIndex() >= oppositeEdges.size() || x < ((PageEdge) oppositeEdges.get(getEndIndex())).getLocation()) {
                return true;
            }
            return false;
        } else if (Math.abs(x - getLocation()) >= tolerance || y <= ((PageEdge) oppositeEdges.get(getStartIndex())).getLocation()) {
            return false;
        } else {
            if (getEndIndex() >= oppositeEdges.size() || y < ((PageEdge) oppositeEdges.get(getEndIndex())).getLocation()) {
                return true;
            }
            return false;
        }
    }

    public boolean containsIndex(int row, int col) {
        if (isHorizontal()) {
            if (col < getStartIndex() || col >= getEndIndex()) {
                return false;
            }
            return true;
        } else if (row < getStartIndex() || row >= getEndIndex()) {
            return false;
        } else {
            return true;
        }
    }

    public PointF intercept(PageEdge edge, PointF size, float halfMargin) {
        float x;
        float y;
        float determinant = (this.lineA * edge.lineB) - (edge.lineA * this.lineB);
        if ((isAngled() || edge.isAngled()) && determinant != 0.0f) {
            x = ((edge.lineB * this.lineC) - (this.lineB * edge.lineC)) / determinant;
            y = ((this.lineA * edge.lineC) - (edge.lineA * this.lineC)) / determinant;
        } else {
            x = isHorizontal() ? edge.getLocation() : getLocation();
            y = isHorizontal() ? getLocation() : edge.getLocation();
        }
        return new PointF((size.x * x) + halfMargin, (size.y * y) + halfMargin);
    }

    @Override
    public int compareTo(PageEdge edge) {
        if (getLocation() != edge.getLocation()) {
            return getLocation() > edge.getLocation() ? 1 : -1;
        } else {
            return getStartIndex() - edge.getStartIndex();
        }
    }
}
