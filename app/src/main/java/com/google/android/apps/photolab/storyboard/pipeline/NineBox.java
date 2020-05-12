package com.google.android.apps.photolab.storyboard.pipeline;

public enum NineBox {
    CENTER(0),
    LEFT(1),
    RIGHT(2),
    TOP(4),
    BOTTOM(8),
    TOP_LEFT(5),
    TOP_RIGHT(6),
    BOTTOM_LEFT(9),
    BOTTOM_RIGHT(10);
    
    private int numVal;

    private NineBox(int numVal) {
        this.numVal = numVal;
    }

    private NineBox(int horz, int vert) {
        this.numVal = horz & vert;
    }

    public int horizontalDirection() {
        return (this.numVal & 3) - 1;
    }

    public int verticalDirection() {
        return ((this.numVal << 2) & 3) - 1;
    }

    public NineBox oppositeLocation() {
        return getNineBoxFromDirections(horizontalDirection(), verticalDirection());
    }

    public static NineBox getNineBoxFromDirections(int horz, int vert) {
        switch (horz + (vert * 4)) {
            case 0:
                return CENTER;
            case 1:
                return LEFT;
            case 2:
                return RIGHT;
            case 4:
                return TOP;
            case 5:
                return TOP_LEFT;
            case 6:
                return TOP_RIGHT;
            case 8:
                return BOTTOM;
            case 9:
                return BOTTOM_LEFT;
            case 10:
                return BOTTOM_RIGHT;
            default:
                return CENTER;
        }
    }
}
