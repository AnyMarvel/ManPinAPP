package androidx.media.filterfw;

import java.nio.ByteBuffer;

public class PixelUtils {
    private static native void nativeCopyPixels(ByteBuffer byteBuffer, ByteBuffer byteBuffer2, int i, int i2, int i3, int i4, int i5);

    public static void copyPixels(ByteBuffer input, ByteBuffer output, int width, int height, int offset, int pixStride, int rowStride) {
        if (input.remaining() != output.remaining()) {
            throw new IllegalArgumentException("Input and output buffers must have the same size!");
        } else if (input.remaining() % 4 != 0) {
            throw new IllegalArgumentException("Input buffer size must be a multiple of 4!");
        } else if (output.remaining() % 4 != 0) {
            throw new IllegalArgumentException("Output buffer size must be a multiple of 4!");
        } else if ((width * height) * 4 != input.remaining()) {
            throw new IllegalArgumentException("Input buffer size does not match given dimensions!");
        } else if ((width * height) * 4 != output.remaining()) {
            throw new IllegalArgumentException("Output buffer size does not match given dimensions!");
        } else {
            nativeCopyPixels(input, output, width, height, offset, pixStride, rowStride);
        }
    }

    static {
        System.loadLibrary("filterframework_jni");
    }
}
