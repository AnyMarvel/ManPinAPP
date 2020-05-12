package androidx.media.filterfw;

import java.nio.ByteBuffer;

public class ColorSpace {
    private static native void nativeArgb8888ToRgba8888(ByteBuffer byteBuffer, ByteBuffer byteBuffer2, int i, int i2, int i3);

    private static native void nativeCropRgbaImage(ByteBuffer byteBuffer, ByteBuffer byteBuffer2, int i, int i2, int i3, int i4, int i5, int i6);

    private static native void nativeRgba8888ToGray8888(ByteBuffer byteBuffer, ByteBuffer byteBuffer2, int i, int i2);

    private static native void nativeRgba8888ToHsva8888(ByteBuffer byteBuffer, ByteBuffer byteBuffer2, int i, int i2);

    private static native void nativeRgba8888ToYcbcra8888(ByteBuffer byteBuffer, ByteBuffer byteBuffer2, int i, int i2);

    private static native void nativeYuv420pToRgba8888(ByteBuffer byteBuffer, ByteBuffer byteBuffer2, int i, int i2, int i3);

    public static void convertYuv420pToRgba8888(ByteBuffer input, ByteBuffer output, int width, int height, int stride) {
        expectInputSize(input, ((stride * 3) * height) / 2);
        expectOutputSize(output, (width * height) * 4);
        nativeYuv420pToRgba8888(input, output, width, height, stride);
    }

    public static void convertArgb8888ToRgba8888(ByteBuffer input, ByteBuffer output, int width, int height, int stride) {
        expectInputSize(input, stride * height);
        expectOutputSize(output, (width * height) * 4);
        nativeArgb8888ToRgba8888(input, output, width, height, stride);
    }

    public static void convertRgba8888ToHsva8888(ByteBuffer input, ByteBuffer output, int width, int height) {
        expectInputSize(input, (width * height) * 4);
        expectOutputSize(output, (width * height) * 4);
        nativeRgba8888ToHsva8888(input, output, width, height);
    }

    public static void convertRgba8888ToYcbcra8888(ByteBuffer input, ByteBuffer output, int width, int height) {
        expectInputSize(input, (width * height) * 4);
        expectOutputSize(output, (width * height) * 4);
        nativeRgba8888ToYcbcra8888(input, output, width, height);
    }

    public static void convertRgba8888ToGray8888(ByteBuffer input, ByteBuffer output, int width, int height) {
        expectInputSize(input, (width * height) * 4);
        expectOutputSize(output, (width * height) * 4);
        nativeRgba8888ToGray8888(input, output, width, height);
    }

    public static void cropRgbaImage(ByteBuffer input, ByteBuffer output, int width, int height, int left, int top, int right, int bottom) {
        expectInputSize(input, (width * height) * 4);
        expectOutputSize(output, (((right + 1) - left) * ((bottom + 1) - top)) * 4);
        nativeCropRgbaImage(input, output, width, height, left, top, right, bottom);
    }

    private static void expectInputSize(ByteBuffer input, int expectedSize) {
        if (input.remaining() < expectedSize) {
            throw new IllegalArgumentException("Input buffer's size does not fit given width and height! Expected: " + expectedSize + ", Got: " + input.remaining() + ".");
        }
    }

    private static void expectOutputSize(ByteBuffer output, int expectedSize) {
        if (output.remaining() < expectedSize) {
            throw new IllegalArgumentException("Output buffer's size does not fit given width and height! Expected: " + expectedSize + ", Got: " + output.remaining() + ".");
        }
    }

    static {
        System.loadLibrary("filterframework_jni");
    }
}
