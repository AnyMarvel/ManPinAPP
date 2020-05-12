package androidx.media.filterfw.decoder;

public class AudioSample {
    public final byte[] bytes;
    public final int channelCount;
    public final int sampleRate;
    public final long timestampUs;

    public AudioSample(int sampleRate, int channelCount, byte[] bytes, long timestampUs) {
        this.sampleRate = sampleRate;
        this.channelCount = channelCount;
        this.bytes = bytes;
        this.timestampUs = timestampUs;
    }
}
