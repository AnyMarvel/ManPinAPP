package androidx.media.filterpacks.audio;

import android.media.AudioTrack;
import androidx.media.filterfw.Filter;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.Signature;
import androidx.media.filterfw.decoder.AudioSample;

public class Speaker extends Filter {
    private static final FrameType AUDIO_INPUT_TYPE = FrameType.single(AudioSample.class);
    private AudioTrack mAudioTrack;
    private int mChannelCount;
    private int mSampleRate;

    public Speaker(MffContext context, String name) {
        super(context, name);
    }

    public Signature getSignature() {
        return new Signature().addInputPort("audio", 2, AUDIO_INPUT_TYPE).disallowOtherPorts();
    }

    protected void onProcess() {
        AudioSample sample = (AudioSample) getConnectedInputPort("audio").pullFrame().asFrameValue().getValue();
        if (sample != null) {
            if (!(sample.sampleRate == this.mSampleRate && sample.channelCount == this.mChannelCount)) {
                int channelConfig;
                this.mSampleRate = sample.sampleRate;
                this.mChannelCount = sample.channelCount;
                if (this.mAudioTrack != null) {
                    this.mAudioTrack.release();
                }
                switch (this.mChannelCount) {
                    case 1:
                        channelConfig = 4;
                        break;
                    case 2:
                        channelConfig = 12;
                        break;
                    default:
                        throw new IllegalArgumentException("Only mono and stereo channel configurations are supported");
                }
                this.mAudioTrack = new AudioTrack(3, this.mSampleRate, channelConfig, 2, AudioTrack.getMinBufferSize(this.mSampleRate, channelConfig, 2), 1);
                if (this.mAudioTrack.getState() == 1) {
                    this.mAudioTrack.play();
                }
            }
            this.mAudioTrack.write(sample.bytes, 0, sample.bytes.length);
        }
    }
}
