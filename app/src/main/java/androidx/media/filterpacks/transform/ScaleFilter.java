package androidx.media.filterpacks.transform;

import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.InputPort;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.Signature;

public class ScaleFilter extends ResizeFilter {
    private float mScale = 1.0f;

    public ScaleFilter(MffContext context, String name) {
        super(context, name);
    }

    public Signature getSignature() {
        FrameType imageIn = FrameType.image2D(FrameType.ELEMENT_RGBA8888, 2);
        return new Signature().addInputPort("image", 2, imageIn).addInputPort("scale", 1, FrameType.single(Float.TYPE)).addInputPort("useMipmaps", 1, FrameType.single(Boolean.TYPE)).addOutputPort("image", 2, FrameType.image2D(FrameType.ELEMENT_RGBA8888, 16)).disallowOtherPorts();
    }

    public void onInputPortOpen(InputPort port) {
        if (port.getName().equals("scale")) {
            port.bindToFieldNamed("mScale");
            port.setAutoPullEnabled(true);
        } else if (port.getName().equals("useMipmaps")) {
            port.bindToFieldNamed("mUseMipmaps");
            port.setAutoPullEnabled(true);
        }
    }

    protected int getOutputWidth(int inWidth, int inHeight) {
        return (int) (((float) inWidth) * this.mScale);
    }

    protected int getOutputHeight(int inWidth, int inHeight) {
        return (int) (((float) inHeight) * this.mScale);
    }
}
