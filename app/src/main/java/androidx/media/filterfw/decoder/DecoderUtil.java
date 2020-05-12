package androidx.media.filterfw.decoder;

import android.annotation.TargetApi;
import android.media.MediaFormat;

@TargetApi(16)
public class DecoderUtil {
    private static final boolean ENABLE_ALL_VIDEO_FORMATS = false;

    public static boolean isAudioFormat(MediaFormat format) {
        return format.getString("mime").startsWith("audio/");
    }

    public static boolean isSupportedVideoFormat(MediaFormat format) {
        return format.getString("mime").equals("video/avc");
    }
}
