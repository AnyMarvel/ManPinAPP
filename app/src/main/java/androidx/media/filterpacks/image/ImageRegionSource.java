package androidx.media.filterpacks.image;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory.Options;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.graphics.RectF;
import androidx.media.filterfw.Filter;
import androidx.media.filterfw.Frame;
import androidx.media.filterfw.FrameImage2D;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.InputPort;
import androidx.media.filterfw.InputPort.FrameListener;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.Signature;
import java.io.IOException;

@TargetApi(10)
public class ImageRegionSource extends Filter {
    private FrameImage2D mCurrImageFrame = null;
    private String mCurrImagePath = null;
    private RectF mCurrImageRectF = new RectF();
    private String mImagePath = null;
    private FrameType mImageType = null;
    private int mMaxHeight = 2048;
    private int mMaxWidth = 2048;
    private RectF mRectF = new RectF(0.0f, 0.0f, 1.0f, 1.0f);
    private FrameListener mRectListener = new FrameListener() {
        public void onFrameReceived(InputPort port, Frame frame) {
            ImageRegionSource.this.mRectF.set((RectF) frame.asFrameValue().getValue());
        }
    };
    private BitmapRegionDecoder mRegionDecoder = null;

    public ImageRegionSource(MffContext context, String name) {
        super(context, name);
    }

    public Signature getSignature() {
        this.mImageType = FrameType.image2D(FrameType.ELEMENT_RGBA8888, 8);
        return new Signature().addInputPort("imagePath", 2, FrameType.single(String.class)).addInputPort("rect", 1, FrameType.single(RectF.class)).addInputPort("maxWidth", 1, FrameType.single(Integer.TYPE)).addInputPort("maxHeight", 1, FrameType.single(Integer.TYPE)).addOutputPort("image", 2, this.mImageType).disallowOtherPorts();
    }

    public void onInputPortOpen(InputPort port) {
        if (port.getName().equals("maxWidth")) {
            port.bindToFieldNamed("mMaxWidth");
            port.setAutoPullEnabled(true);
        } else if (port.getName().equals("maxHeight")) {
            port.bindToFieldNamed("mMaxHeight");
            port.setAutoPullEnabled(true);
        } else if (port.getName().equals("imagePath")) {
            port.bindToFieldNamed("mImagePath");
            port.setAutoPullEnabled(true);
        } else if (port.getName().equals("rect")) {
            port.bindToListener(this.mRectListener);
            port.setAutoPullEnabled(true);
        }
    }

    protected void onProcess() {
        boolean isNewDecoder = false;
        if (this.mRegionDecoder == null || !this.mImagePath.equals(this.mCurrImagePath)) {
            if (this.mRegionDecoder != null) {
                this.mRegionDecoder.recycle();
            }
            try {
                this.mRegionDecoder = BitmapRegionDecoder.newInstance(this.mImagePath, false);
                this.mCurrImagePath = this.mImagePath;
                isNewDecoder = true;
            } catch (IOException e) {
                String str = "Failed setting up BitmapRegionDecoder for ";
                String valueOf = String.valueOf(this.mImagePath);
                throw new RuntimeException(valueOf.length() != 0 ? str.concat(valueOf) : new String(str));
            }
        }
        if (isNewDecoder || !this.mRectF.equals(this.mCurrImageRectF)) {
            if (this.mRectF.width() <= 0.0f || this.mRectF.height() <= 0.0f) {
                String valueOf2 = String.valueOf(this.mRectF);
                throw new RuntimeException(new StringBuilder(String.valueOf(valueOf2).length() + 25).append("invalid input rectangle: ").append(valueOf2).toString());
            }
            int imageHeight = this.mRegionDecoder.getHeight();
            int imageWidth = this.mRegionDecoder.getWidth();
            Rect rect = new Rect((int) Math.floor((double) (this.mRectF.left * ((float) imageWidth))), (int) Math.floor((double) (this.mRectF.top * ((float) imageHeight))), (int) Math.floor((double) (this.mRectF.right * ((float) imageWidth))), (int) Math.floor((double) (this.mRectF.bottom * ((float) imageHeight))));
            Options options = new Options();
            options.inSampleSize = getSampleSize(rect.height(), rect.width());
            Bitmap bitmap=mCurrImageFrame.toBitmap();
            int[] dims = new int[]{bitmap.getWidth(), this.mRegionDecoder.decodeRegion(rect, options).getHeight()};
            if (this.mCurrImageFrame != null) {
                this.mCurrImageFrame.release();
            }
            this.mCurrImageFrame = Frame.create(this.mImageType, dims).asFrameImage2D();
            this.mCurrImageFrame.setBitmap(bitmap);
            this.mCurrImageRectF.set(this.mRectF);
        }
        getConnectedOutputPort("image").pushFrame(this.mCurrImageFrame);
    }

    protected void onTearDown() {
        if (this.mRegionDecoder != null) {
            this.mRegionDecoder.recycle();
        }
        if (this.mCurrImageFrame != null) {
            this.mCurrImageFrame.release();
        }
    }

    private int getSampleSize(int sourceHeight, int sourceWidth) {
        double ratio = Math.ceil(Math.max(((double) sourceHeight) / ((double) this.mMaxHeight), ((double) sourceWidth) / ((double) this.mMaxWidth)));
        if (ratio <= 1.0d) {
            return 1;
        }
        return (int) Math.pow(2.0d, Math.ceil(Math.log(ratio) / Math.log(2.0d)));
    }
}
