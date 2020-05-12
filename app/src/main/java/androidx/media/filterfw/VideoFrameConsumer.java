package androidx.media.filterfw;

public interface VideoFrameConsumer {
    void onVideoFrameAvailable(VideoFrameProvider videoFrameProvider, long j);

    void onVideoStreamError(Exception exception);

    void onVideoStreamStarted();

    void onVideoStreamStopped();
}
