package androidx.media.filterfw;

public interface AudioFrameConsumer {
    void onAudioSamplesAvailable(AudioFrameProvider audioFrameProvider);
}
