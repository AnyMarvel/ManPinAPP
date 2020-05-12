package androidx.media.filterfw.decoder;

public class VideoFrameInfo {
    public final boolean isKeyFrame;

    public VideoFrameInfo(boolean isKeyFrame) {
        this.isKeyFrame = isKeyFrame;
    }
}
