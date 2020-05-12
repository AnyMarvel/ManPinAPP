package androidx.media.filterpacks.performance;

public class Throughput {
    private final int mPeriodFrames;
    private final long mPeriodTime;
    private final int mTotalFrames;

    public Throughput(int totalFrames, int periodFrames, long periodTime, int size) {
        this.mTotalFrames = totalFrames;
        this.mPeriodFrames = periodFrames;
        this.mPeriodTime = periodTime;
    }

    public int getTotalFrameCount() {
        return this.mTotalFrames;
    }

    public int getPeriodFrameCount() {
        return this.mPeriodFrames;
    }

    public long getPeriodTime() {
        return this.mPeriodTime;
    }

    public float getFramesPerSecond() {
        return ((float) this.mPeriodFrames) / (((float) this.mPeriodTime) / 1000.0f);
    }

    public String toString() {
        return Math.round(getFramesPerSecond()) + " FPS";
    }
}
