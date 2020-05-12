package androidx.media.filterfw.imageutils;

import androidx.media.filterfw.Frame;
import androidx.media.filterfw.FrameImage2D;
import androidx.media.filterfw.FrameType;
import java.util.Arrays;

public class MipMapUtils {
    public static FrameImage2D makeMipMappedFrame(FrameImage2D current, int[] dimensions) {
        int[] pow2Dims = new int[]{powOf2(dimensions[0]), powOf2(dimensions[1])};
        if (current == null) {
            return Frame.create(FrameType.image2D(FrameType.ELEMENT_RGBA8888, 18), pow2Dims).asFrameImage2D();
        }
        if (Arrays.equals(dimensions, current.getDimensions())) {
            return current;
        }
        current.resize(pow2Dims);
        return current;
    }

    public static void generateMipMaps(FrameImage2D frame) {
        frame.lockTextureSource().generateMipmaps();
        frame.unlock();
    }

    private static int powOf2(int x) {
        x--;
        x |= x >> 1;
        x |= x >> 2;
        x |= x >> 4;
        x |= x >> 8;
        return (x | (x >> 16)) + 1;
    }
}
