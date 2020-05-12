package androidx.media.filterpacks.transform;

import androidx.media.filterfw.Filter;
import androidx.media.filterfw.FrameImage2D;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.InputPort;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.OutputPort;
import androidx.media.filterfw.Signature;
import androidx.media.filterfw.geometry.Quad;
import androidx.media.filterfw.imageutils.ImageCropper;

public class CropFilter extends Filter {
    private Quad mCropRect = Quad.fromRect(0.0f, 0.0f, 1.0f, 1.0f);
    private ImageCropper mImageCropper = null;
    private int mOutputHeight = 0;
    private int mOutputWidth = 0;
    private boolean mUseMipmaps = false;

    public CropFilter(MffContext context, String name) {
        super(context, name);
    }

    public Signature getSignature() {
        FrameType imageIn = FrameType.image2D(FrameType.ELEMENT_RGBA8888, 2);
        return new Signature().addInputPort("image", 2, imageIn).addInputPort("cropRect", 2, FrameType.single(Quad.class)).addInputPort("outputWidth", 1, FrameType.single(Integer.TYPE)).addInputPort("outputHeight", 1, FrameType.single(Integer.TYPE)).addInputPort("useMipmaps", 1, FrameType.single(Boolean.TYPE)).addOutputPort("image", 2, FrameType.image2D(FrameType.ELEMENT_RGBA8888, 16)).disallowOtherPorts();
    }

    public void onInputPortOpen(InputPort port) {
        if (port.getName().equals("cropRect")) {
            port.bindToFieldNamed("mCropRect");
            port.setAutoPullEnabled(true);
        } else if (port.getName().equals("outputWidth")) {
            port.bindToFieldNamed("mOutputWidth");
            port.setAutoPullEnabled(true);
        } else if (port.getName().equals("outputHeight")) {
            port.bindToFieldNamed("mOutputHeight");
            port.setAutoPullEnabled(true);
        } else if (port.getName().equals("useMipmaps")) {
            port.bindToFieldNamed("mUseMipmaps");
            port.setAutoPullEnabled(true);
        }
    }

    protected void onOpen() {
        this.mImageCropper = new ImageCropper(isOpenGLSupported());
    }

    protected void onProcess() {
        OutputPort outPort = getConnectedOutputPort("image");
        FrameImage2D inputImage = getConnectedInputPort("image").pullFrame().asFrameImage2D();
        int[] croppedDims = ImageCropper.computeCropDimensions(inputImage.getDimensions(), this.mCropRect);
        FrameImage2D outputImage = outPort.fetchAvailableFrame(new int[]{getOutputWidth(croppedDims[0], croppedDims[1]), getOutputHeight(croppedDims[0], croppedDims[1])}).asFrameImage2D();
        this.mImageCropper.cropImage(inputImage, this.mCropRect, outputImage, this.mUseMipmaps);
        outPort.pushFrame(outputImage);
    }

    protected void onClose() {
        if (this.mImageCropper != null) {
            this.mImageCropper.release();
            this.mImageCropper = null;
        }
    }

    protected int getOutputWidth(int inWidth, int inHeight) {
        return this.mOutputWidth <= 0 ? inWidth : this.mOutputWidth;
    }

    protected int getOutputHeight(int inWidth, int inHeight) {
        return this.mOutputHeight <= 0 ? inHeight : this.mOutputHeight;
    }
}
