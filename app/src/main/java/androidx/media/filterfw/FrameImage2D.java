package androidx.media.filterfw;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

public class FrameImage2D extends FrameBuffer2D {
    public TextureSource lockTextureSource() {
        return (TextureSource) this.mBackingStore.lockData(1, 2);
    }

    public RenderTarget lockRenderTarget() {
        return (RenderTarget) this.mBackingStore.lockData(2, 4);
    }

    public void setBitmap(Bitmap bitmap) {
        bitmap = convertToFrameType(bitmap, this.mBackingStore.getFrameType());
        validateBitmapSize(bitmap, this.mBackingStore.getDimensions());
        this.mBackingStore.lockBacking(2, 16).setData(bitmap);
        this.mBackingStore.unlock();
    }

    public Bitmap toBitmap() {
        Bitmap result = (Bitmap) this.mBackingStore.lockData(1, 16);
        this.mBackingStore.unlock();
        return result;
    }

    public void copyToFrame(FrameImage2D target, RectF sourceRect, RectF targetRect) {
        if (GraphRunner.current().isOpenGLSupported()) {
            gpuImageCopy(this, target, sourceRect, targetRect);
        } else {
            cpuImageCopy(this, target, sourceRect, targetRect);
        }
    }

    static FrameImage2D create(BackingStore backingStore) {
        assertCanCreate(backingStore);
        return new FrameImage2D(backingStore);
    }

    FrameImage2D(BackingStore backingStore) {
        super(backingStore);
    }

    static void assertCanCreate(BackingStore backingStore) {
        FrameBuffer2D.assertCanCreate(backingStore);
    }

    private static Bitmap convertToFrameType(Bitmap bitmap, FrameType type) {
        Config config = bitmap.getConfig();
        Bitmap result = bitmap;
        switch (type.getElementId()) {
            case FrameType.ELEMENT_RGBA8888 /*301*/:
                if (config != Config.ARGB_8888) {
                    result = bitmap.copy(Config.ARGB_8888, false);
                    if (result == null) {
                        throw new RuntimeException("Could not convert bitmap to frame-type RGBA8888!");
                    }
                }
                return result;
            default:
                String valueOf = String.valueOf(type);
                throw new IllegalArgumentException(new StringBuilder(String.valueOf(valueOf).length() + 48).append("Unsupported frame type '").append(valueOf).append("' for bitmap assignment!").toString());
        }
    }

    private void validateBitmapSize(Bitmap bitmap, int[] dimensions) {
        if (bitmap.getWidth() != dimensions[0] || bitmap.getHeight() != dimensions[1]) {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int i = dimensions[0];
            throw new IllegalArgumentException("Cannot assign bitmap of size " + width + "x" + height + " to frame of size " + i + "x" + dimensions[1] + "!");
        }
    }

    private static void gpuImageCopy(FrameImage2D srcImage, FrameImage2D dstImage, RectF srcRect, RectF dstRect) {
        ImageShader idShader = RenderTarget.currentTarget().getIdentityShader();
        idShader.setSourceRect(srcRect);
        idShader.setTargetRect(dstRect);
        idShader.process(srcImage, dstImage);
        idShader.setSourceRect(0.0f, 0.0f, 1.0f, 1.0f);
        idShader.setTargetRect(0.0f, 0.0f, 1.0f, 1.0f);
    }

    private static void cpuImageCopy(FrameImage2D srcImage, FrameImage2D dstImage, RectF srcRect, RectF dstRect) {
        Rect srcIRect = new Rect((int) (srcRect.left * ((float) srcImage.getWidth())), (int) (srcRect.top * ((float) srcImage.getHeight())), (int) (srcRect.right * ((float) srcImage.getWidth())), (int) (srcRect.bottom * ((float) srcImage.getHeight())));
        Rect dstIRect = new Rect((int) (dstRect.left * ((float) dstImage.getWidth())), (int) (dstRect.top * ((float) dstImage.getHeight())), (int) (dstRect.right * ((float) dstImage.getWidth())), (int) (dstRect.bottom * ((float) dstImage.getHeight())));
        Bitmap dstBitmap = dstImage.toBitmap();
        if (dstBitmap == null) {
            dstBitmap = Bitmap.createBitmap(dstImage.getWidth(), dstImage.getHeight(), Config.ARGB_8888);
        }
        Canvas canvas = new Canvas(dstBitmap);
        Paint paint = new Paint();
        paint.setFilterBitmap(true);
        canvas.drawBitmap(srcImage.toBitmap(), srcIRect, dstIRect, paint);
        dstImage.setBitmap(dstBitmap);
    }
}
