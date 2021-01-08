package androidx.media.filterfw.imageutils;

import android.graphics.RectF;
import android.util.Log;
import androidx.media.filterfw.FrameBuffer2D;
import androidx.media.filterfw.geometry.Quad;
import java.nio.ByteBuffer;

public class RegionStatsCalculator {
    private static final int MEAN_INDEX = 0;
    private static final int STDEV_INDEX = 1;
    private static final String TAG = "RegionStatsCalculator";
    private static final boolean mLogVerbose = Log.isLoggable(TAG, Log.VERBOSE);
    private final float[] mStats = new float[2];

    public static class Statistics {
        public final float mean;
        public final float stdDev;

        public Statistics(float mean, float stdDev) {
            this.mean = mean;
            this.stdDev = stdDev;
        }
    }

    private native void regionscore(ByteBuffer byteBuffer, int i, int i2, int i3, int i4, int i5, float[] fArr, boolean z);

    static {
        System.loadLibrary("filterframework_jni");
    }

    public Statistics calcMeanAndStd(FrameBuffer2D inputBuffer, Quad inputRegion, boolean suppressZero) {
        int width = inputBuffer.getWidth();
        int height = inputBuffer.getHeight();
        RectF rect = inputRegion.getEnclosingRectF();
        int x0 = clamp((int) (rect.left * ((float) width)), 0, width - 1);
        int y0 = clamp((int) (rect.top * ((float) height)), 0, height - 1);
        int x1 = clamp((int) (rect.right * ((float) width)), 0, width - 1);
        int y1 = clamp((int) (rect.bottom * ((float) height)), 0, height - 1);
        ByteBuffer pixelBuffer = inputBuffer.lockBytes(1);
        pixelBuffer.rewind();
        regionscore(pixelBuffer, width, x0, y0, x1, y1, this.mStats, suppressZero);
        inputBuffer.unlock();
        if (mLogVerbose) {
            String str = TAG;
            float f = this.mStats[0];
            Log.v(str, "Native calc stats: Mean = " + f + ", Stdev = " + this.mStats[1]);
        }
        return new Statistics(this.mStats[0], this.mStats[1]);
    }

    private static int clamp(int x, int minX, int maxX) {
        if (x < minX) {
            return minX;
        }
        return x > maxX ? maxX : x;
    }
}
