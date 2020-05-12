package androidx.media.filterfw.decoder;

import androidx.media.filterfw.VideoFrameProvider;

public interface VideoStreamProvider extends VideoFrameProvider {
    long getDurationNs();

    void start();

    void stop();
}
