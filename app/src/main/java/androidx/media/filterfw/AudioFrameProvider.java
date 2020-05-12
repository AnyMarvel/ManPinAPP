package androidx.media.filterfw;

public interface AudioFrameProvider {
    void addAudioFrameConsumer(AudioFrameConsumer audioFrameConsumer);

    boolean grabAudioSamples(FrameValue frameValue);

    void removeAudioFrameConsumer(AudioFrameConsumer audioFrameConsumer);
}
