package androidx.media.filterpacks.transform;

import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.InputPort;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.Signature;

public class ScaleToAreaFilter extends ResizeFilter {
    private int mHeightMultiple = 4;
    private int mTargetArea = 76800;
    private int mWidthMultiple = 4;

    public ScaleToAreaFilter(MffContext context, String name) {
        super(context, name);
    }

    public Signature getSignature() {
        FrameType imageIn = FrameType.image2D(FrameType.ELEMENT_RGBA8888, 2);
        return new Signature().addInputPort("image", 2, imageIn).addInputPort("targetArea", 1, FrameType.single(Integer.TYPE)).addInputPort("widthMultiple", 1, FrameType.single(Integer.TYPE)).addInputPort("heightMultiple", 1, FrameType.single(Integer.TYPE)).addInputPort("useMipmaps", 1, FrameType.single(Boolean.TYPE)).addOutputPort("image", 2, FrameType.image2D(FrameType.ELEMENT_RGBA8888, 16)).disallowOtherPorts();
    }

    public void onInputPortOpen(InputPort port) {
        if (port.getName().equals("targetArea")) {
            port.bindToFieldNamed("mTargetArea");
            port.setAutoPullEnabled(true);
        } else if (port.getName().equals("useMipmaps")) {
            port.bindToFieldNamed("mUseMipmaps");
            port.setAutoPullEnabled(true);
        } else if (port.getName().equals("widthMultiple")) {
            port.bindToFieldNamed("mWidthMultiple");
            port.setAutoPullEnabled(true);
        } else if (port.getName().equals("heightMultiple")) {
            port.bindToFieldNamed("mHeightMultiple");
            port.setAutoPullEnabled(true);
        }
    }

    private float calcVideoScale(int inWidth, int inHeight) {
        return (float) Math.sqrt((double) (((float) this.mTargetArea) / ((float) (inWidth * inHeight))));
    }

    protected int getOutputWidth(int inWidth, int inHeight) {
        int newWidth = Math.round(((float) inWidth) * calcVideoScale(inWidth, inHeight));
        return ((this.mWidthMultiple - (newWidth % this.mWidthMultiple)) % this.mWidthMultiple) + newWidth;
    }

    protected int getOutputHeight(int inWidth, int inHeight) {
        int newHeight = Math.round(((float) inHeight) * calcVideoScale(inWidth, inHeight));
        return ((this.mHeightMultiple - (newHeight % this.mHeightMultiple)) % this.mHeightMultiple) + newHeight;
    }
}
