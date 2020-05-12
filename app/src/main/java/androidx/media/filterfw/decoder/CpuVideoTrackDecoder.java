package androidx.media.filterfw.decoder;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.util.SparseIntArray;
import androidx.media.filterfw.ColorSpace;
import androidx.media.filterfw.Frame;
import androidx.media.filterfw.FrameImage2D;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.FrameValue;
import androidx.media.filterfw.PixelUtils;
import androidx.media.filterfw.geometry.ScaleUtils;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.TreeMap;

@TargetApi(16)
public class CpuVideoTrackDecoder extends VideoTrackDecoder {
    private boolean mCurrentIsKeyFrame;
    private long mCurrentPresentationTimeUs;
    private ByteBuffer mDecodedBuffer;
    private FrameConverter mFrameConverter;
    private final int mHeight;
    private ByteBuffer mRgbaBuffer;
    private final int mWidth;

    private static class FrameConverter {
        private static final String CROP_BOTTOM = "crop-bottom";
        private static final String CROP_LEFT = "crop-left";
        private static final String CROP_RIGHT = "crop-right";
        private static final String CROP_TOP = "crop-top";
        private static final String STRIDE = "stride";
        private final ByteBuffer mBuffer;
        private final int mColorFormat;
        private final int mCropBottom;
        private final int mCropLeft;
        private final int mCropRight;
        private final int mCropTop;
        private final int mHeight;
        private final int mStride;
        private final int mWidth;

        public FrameConverter(MediaFormat format) {
            int integer;
            int i = 0;
            this.mColorFormat = format.getInteger("color-format");
            this.mWidth = format.getInteger("width");
            this.mHeight = format.getInteger("height");
            this.mCropLeft = format.containsKey(CROP_LEFT) ? format.getInteger(CROP_LEFT) : 0;
            if (format.containsKey(CROP_RIGHT)) {
                integer = format.getInteger(CROP_RIGHT);
            } else {
                integer = this.mWidth - 1;
            }
            this.mCropRight = integer;
            if (format.containsKey(CROP_TOP)) {
                integer = format.getInteger(CROP_TOP);
            } else {
                integer = 0;
            }
            this.mCropTop = integer;
            if (format.containsKey(CROP_BOTTOM)) {
                integer = format.getInteger(CROP_BOTTOM);
            } else {
                integer = this.mHeight - 1;
            }
            this.mCropBottom = integer;
            if (format.containsKey(STRIDE)) {
                i = format.getInteger(STRIDE);
            }
            this.mStride = i;
            this.mBuffer = ByteBuffer.allocateDirect((this.mWidth * this.mHeight) * 4);
        }

        private void convertImage(ByteBuffer input, ByteBuffer output) {
            switch (this.mColorFormat) {
                case 16:
                    ColorSpace.convertArgb8888ToRgba8888(input, this.mBuffer, this.mWidth, this.mHeight, this.mStride != 0 ? this.mStride : this.mWidth * 4);
                    break;
                case 19:
                    ColorSpace.convertYuv420pToRgba8888(input, this.mBuffer, this.mWidth, this.mHeight, this.mStride != 0 ? this.mStride : this.mWidth);
                    break;
                default:
                    throw new RuntimeException("Unsupported color format: " + this.mColorFormat + "!");
            }
            ColorSpace.cropRgbaImage(this.mBuffer, output, this.mWidth, this.mHeight, this.mCropLeft, this.mCropTop, this.mCropRight, this.mCropBottom);
        }
    }

    protected CpuVideoTrackDecoder(int trackIndex, MediaFormat format, Listener listener) {
        super(trackIndex, format, listener);
        this.mWidth = format.getInteger("width");
        this.mHeight = format.getInteger("height");
    }

    public long getTimestampNs() {
        return this.mCurrentPresentationTimeUs * 1000;
    }

    protected MediaCodec initMediaCodec(MediaFormat format) {
        MediaCodec mediaCodec = findDecoderCodec(format, new int[]{16, 19});
        if (mediaCodec == null) {
            String valueOf = String.valueOf(format);
            throw new RuntimeException(new StringBuilder(String.valueOf(valueOf).length() + 47).append("Could not find a suitable decoder for format: ").append(valueOf).append("!").toString());
        }
        mediaCodec.configure(format, null, null, 0);
        return mediaCodec;
    }

    protected boolean onDataAvailable(MediaCodec codec, ByteBuffer[] buffers, int bufferIndex, BufferInfo info, boolean isKeyFrame) {
        this.mCurrentPresentationTimeUs = info.presentationTimeUs;
        this.mCurrentIsKeyFrame = isKeyFrame;
        this.mDecodedBuffer = buffers[bufferIndex];
        convertCurrentFrame();
        markFrameAvailable();
        notifyListener();
        waitForFrameGrabs();
        codec.releaseOutputBuffer(bufferIndex, false);
        return false;
    }

    protected void copyFrameDataTo(FrameImage2D outputVideoFrame, FrameValue infoFrame, int maxDim, int rotation) {
        FrameImage2D fullFrame;
        int outputWidth = this.mWidth;
        int outputHeight = this.mHeight;
        if (VideoTrackDecoder.needSwapDimension(rotation)) {
            outputWidth = this.mHeight;
            outputHeight = this.mWidth;
        }
        int[] fullDims = new int[]{outputWidth, outputHeight};
        int[] scaledDims = ScaleUtils.scaleDown(outputWidth, outputHeight, maxDim);
        boolean needsScale = (scaledDims[0] == outputWidth && scaledDims[1] == outputHeight) ? false : true;
        FrameType imageType = FrameType.image2D(FrameType.ELEMENT_RGBA8888, 8);
        if (needsScale) {
            fullFrame = Frame.create(imageType, fullDims).asFrameImage2D();
        } else {
            fullFrame = outputVideoFrame;
        }
        outputVideoFrame.resize(scaledDims);
        ByteBuffer outBytes = fullFrame.lockBytes(2);
        this.mRgbaBuffer.rewind();
        if (rotation == 0) {
            outBytes.put(this.mRgbaBuffer);
        } else {
            copyRotate(this.mRgbaBuffer, outBytes, rotation);
        }
        fullFrame.unlock();
        if (needsScale) {
            outputVideoFrame.setBitmap(Bitmap.createScaledBitmap(fullFrame.toBitmap(), scaledDims[0], scaledDims[1], false));
            fullFrame.release();
        }
        outputVideoFrame.setTimestamp(getTimestampNs());
        if (infoFrame != null) {
            infoFrame.setValue(new VideoFrameInfo(this.mCurrentIsKeyFrame));
            infoFrame.setTimestamp(getTimestampNs());
        }
    }

    private void convertCurrentFrame() {
        if (this.mRgbaBuffer == null) {
            this.mRgbaBuffer = ByteBuffer.allocateDirect((this.mWidth * this.mHeight) * 4);
        }
        if (this.mFrameConverter == null) {
            this.mFrameConverter = new FrameConverter(getMediaCodec().getOutputFormat());
        }
        this.mRgbaBuffer.rewind();
        this.mFrameConverter.convertImage(this.mDecodedBuffer, this.mRgbaBuffer);
    }

    private void copyRotate(ByteBuffer input, ByteBuffer output, int rotation) {
        int offset;
        int pixStride;
        int rowStride;
        switch (rotation) {
            case 0:
                offset = 0;
                pixStride = 1;
                rowStride = this.mWidth;
                break;
            case 90:
                offset = this.mHeight - 1;
                pixStride = this.mHeight;
                rowStride = -1;
                break;
            case MediaDecoder.ROTATE_180 /*180*/:
                offset = (this.mWidth * this.mHeight) - 1;
                pixStride = -1;
                rowStride = -this.mWidth;
                break;
            case MediaDecoder.ROTATE_90_LEFT /*270*/:
                offset = (this.mWidth - 1) * this.mHeight;
                pixStride = -this.mHeight;
                rowStride = 1;
                break;
            default:
                throw new IllegalArgumentException("Unsupported rotation " + rotation + "!");
        }
        PixelUtils.copyPixels(input, output, this.mWidth, this.mHeight, offset, pixStride, rowStride);
    }

    private static MediaCodec findDecoderCodec(MediaFormat format, int[] requiredColorFormats) {
        TreeMap<Integer, String> candidateCodecs = new TreeMap();
        SparseIntArray colorPriorities = intArrayToPriorityMap(requiredColorFormats);
        for (int i = 0; i < MediaCodecList.getCodecCount(); i++) {
            MediaCodecInfo info = MediaCodecList.getCodecInfoAt(i);
            if (!info.isEncoder()) {
                String requiredType = format.getString("mime");
                if (new HashSet(Arrays.asList(info.getSupportedTypes())).contains(requiredType)) {
                    for (int supportedColorFormat : info.getCapabilitiesForType(requiredType).colorFormats) {
                        if (colorPriorities.indexOfKey(supportedColorFormat) >= 0) {
                            candidateCodecs.put(Integer.valueOf(colorPriorities.get(supportedColorFormat)), info.getName());
                        }
                    }
                }
            }
        }
        if (candidateCodecs.isEmpty()) {
            return null;
        }
        try {
            return MediaCodec.createByCodecName((String) candidateCodecs.firstEntry().getValue());
        } catch (IOException e) {
            return null;
        }
    }

    private static SparseIntArray intArrayToPriorityMap(int[] values) {
        SparseIntArray result = new SparseIntArray();
        for (int priority = 0; priority < values.length; priority++) {
            result.append(values[priority], priority);
        }
        return result;
    }
}
