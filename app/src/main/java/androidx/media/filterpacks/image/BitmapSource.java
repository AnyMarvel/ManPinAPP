package androidx.media.filterpacks.image;

import android.graphics.Bitmap;
import androidx.media.filterfw.Filter;
import androidx.media.filterfw.Frame;
import androidx.media.filterfw.FrameImage2D;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.InputPort;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.OutputPort;
import androidx.media.filterfw.Signature;

public class BitmapSource extends Filter {
    private boolean mAlwaysRead = false;
    private FrameImage2D mImageFrame = null;
    private FrameType mImageType = null;
    private Bitmap mLastBitmap = null;
    private long mTimestamp = -1;

    public BitmapSource(MffContext context, String name) {
        super(context, name);
    }

    public Signature getSignature() {
        this.mImageType = FrameType.image2D(FrameType.ELEMENT_RGBA8888, 8);
        return new Signature().addInputPort("bitmap", 2, FrameType.single(Bitmap.class)).addInputPort("alwaysRead", 1, FrameType.single(Boolean.TYPE)).addInputPort("timestamp", 1, FrameType.single(Long.TYPE)).addOutputPort("image", 2, this.mImageType).disallowOtherPorts();
    }

    public void onInputPortOpen(InputPort port) {
        if (port.getName().equals("alwaysRead")) {
            port.bindToFieldNamed("mAlwaysRead");
            port.setAutoPullEnabled(true);
        } else if (port.getName().equals("timestamp")) {
            port.bindToFieldNamed("mTimestamp");
            port.setAutoPullEnabled(true);
        }
    }

    protected void onProcess() {
        Bitmap bitmap = (Bitmap) getConnectedInputPort("bitmap").pullFrame().asFrameValue().getValue();
        OutputPort outPort = getConnectedOutputPort("image");
        if (this.mLastBitmap != bitmap || this.mAlwaysRead) {
            if (this.mImageFrame != null) {
                this.mImageFrame.release();
            }
            this.mImageFrame = Frame.create(this.mImageType, new int[]{bitmap.getWidth(), bitmap.getHeight()}).asFrameImage2D();
            this.mImageFrame.setBitmap(bitmap);
            this.mLastBitmap = bitmap;
        }
        if (this.mImageFrame == null) {
            throw new RuntimeException("BitmapSource trying to push out an undefined frame! Most likely, graph.getVariable(<BitmapSource filter>).setValue(<Bitmap>) has not been called.");
        }
        if (this.mTimestamp >= 0) {
            this.mImageFrame.setTimestamp(this.mTimestamp);
        }
        outPort.pushFrame(this.mImageFrame);
    }

    protected void onTearDown() {
        if (this.mImageFrame != null) {
            this.mImageFrame.release();
            this.mImageFrame = null;
        }
    }
}
