package androidx.media.filterpacks.transform;

import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.Signature;

public class ResizeFilter extends CropFilter {
    public ResizeFilter(MffContext context, String name) {
        super(context, name);
    }

    public Signature getSignature() {
        FrameType imageIn = FrameType.image2D(FrameType.ELEMENT_RGBA8888, 2);
        return new Signature().addInputPort("image", 2, imageIn).addInputPort("outputWidth", 1, FrameType.single(Integer.TYPE)).addInputPort("outputHeight", 1, FrameType.single(Integer.TYPE)).addInputPort("useMipmaps", 1, FrameType.single(Boolean.TYPE)).addOutputPort("image", 2, FrameType.image2D(FrameType.ELEMENT_RGBA8888, 16)).disallowOtherPorts();
    }
}
