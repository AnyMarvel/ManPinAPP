package androidx.media.filterfw.geometry;

public class ScaleUtils {
    public static int[] scaleDown(int width, int height, int maximumDimension) {
        if (maximumDimension >= width && maximumDimension >= height) {
            return new int[]{width, height};
        } else if (width > height) {
            return new int[]{maximumDimension, (maximumDimension * height) / width};
        } else {
            return new int[]{(maximumDimension * width) / height, maximumDimension};
        }
    }
}
