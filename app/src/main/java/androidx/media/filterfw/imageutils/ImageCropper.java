package androidx.media.filterfw.imageutils;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import androidx.media.filterfw.FrameImage2D;
import androidx.media.filterfw.ImageShader;
import androidx.media.filterfw.geometry.Quad;

public class ImageCropper {
    private FrameImage2D mPow2Frame = null;
    private ImageShader mShader;
    private final boolean mUseOpenGL;

    public ImageCropper(boolean useOpenGL) {
        this.mUseOpenGL = useOpenGL;
        if (this.mUseOpenGL) {
            this.mShader = ImageShader.createIdentity();
        }
    }

    public void release() {
        if (this.mPow2Frame != null) {
            this.mPow2Frame.release();
            this.mPow2Frame = null;
            this.mShader = null;
        }
    }

    public static int[] computeCropDimensions(int[] inputDimensions, Quad cropRect) {
        return new int[]{(int) Math.ceil((double) (cropRect.xEdge().length() * ((float) inputDimensions[0]))), (int) Math.ceil((double) (cropRect.yEdge().length() * ((float) inputDimensions[1])))};
    }

    public void cropImage(FrameImage2D inputImage, Quad cropQuad, FrameImage2D outputImage, boolean useMipmaps) {
        int[] inDims = inputImage.getDimensions();
        int[] croppedDims = computeCropDimensions(inDims, cropQuad);
        int outputWidth = outputImage.getWidth();
        int outputHeight = outputImage.getHeight();
        if (this.mUseOpenGL) {
            FrameImage2D sourceFrame;
            Quad sourceQuad;
            boolean scaleDown = outputWidth < croppedDims[0] || outputHeight < croppedDims[1];
            if (scaleDown && useMipmaps) {
                this.mPow2Frame = MipMapUtils.makeMipMappedFrame(this.mPow2Frame, croppedDims);
                int[] extDims = this.mPow2Frame.getDimensions();
                Quad targetQuad = Quad.fromRect(0.0f, 0.0f, ((float) croppedDims[0]) / ((float) extDims[0]), ((float) croppedDims[1]) / ((float) extDims[1]));
                this.mShader.setSourceQuad(cropQuad);
                this.mShader.setTargetQuad(targetQuad);
                this.mShader.process(inputImage, this.mPow2Frame);
                MipMapUtils.generateMipMaps(this.mPow2Frame);
                sourceFrame = this.mPow2Frame;
                sourceQuad = targetQuad;
            } else {
                sourceFrame = inputImage;
                sourceQuad = cropQuad;
            }
            this.mShader.setSourceQuad(sourceQuad);
            this.mShader.setTargetRect(0.0f, 0.0f, 1.0f, 1.0f);
            this.mShader.process(sourceFrame, outputImage);
            return;
        }
        Matrix transform = Quad.getTransform(cropQuad.scale2((float) inDims[0], (float) inDims[1]), Quad.fromRect(0.0f, 0.0f, (float) inDims[0], (float) inDims[1]));
        transform.postScale(((float) outputWidth) / ((float) inDims[0]), ((float) outputHeight) / ((float) inDims[1]));
        Bitmap cropped = Bitmap.createBitmap(outputWidth, outputHeight, Config.ARGB_8888);
        Canvas canvas = new Canvas(cropped);
        Paint paint = new Paint();
        paint.setFilterBitmap(useMipmaps);
        canvas.drawBitmap(inputImage.toBitmap(), transform, paint);
        outputImage.setBitmap(cropped);
    }
}
