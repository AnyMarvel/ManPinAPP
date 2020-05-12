package androidx.media.filterpacks.transform;

import androidx.media.filterfw.Filter;
import androidx.media.filterfw.FrameImage2D;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.ImageShader;
import androidx.media.filterfw.InputPort;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.OutputPort;
import androidx.media.filterfw.Signature;
import androidx.media.filterfw.imageutils.MipMapUtils;

public class GridFilter extends Filter {
    private int mOutputHeight = 0;
    private int mOutputWidth = 0;
    private ImageShader mShader;
    private FrameImage2D mTileFrame = null;
    private boolean mUseMipmaps = true;
    private int mXCount = 1;
    private int mYCount = 1;

    public GridFilter(MffContext context, String name) {
        super(context, name);
    }

    public Signature getSignature() {
        FrameType imageIn = FrameType.image2D(FrameType.ELEMENT_RGBA8888, 2);
        return new Signature().addInputPort("image", 2, imageIn).addInputPort("xCount", 1, FrameType.single(Integer.TYPE)).addInputPort("yCount", 1, FrameType.single(Integer.TYPE)).addInputPort("useMipmaps", 1, FrameType.single(Boolean.TYPE)).addInputPort("outputWidth", 1, FrameType.single(Integer.TYPE)).addInputPort("outputHeight", 1, FrameType.single(Integer.TYPE)).addOutputPort("image", 2, FrameType.image2D(FrameType.ELEMENT_RGBA8888, 16)).disallowOtherPorts();
    }

    public void onInputPortOpen(InputPort port) {
        if (port.getName().equals("xCount")) {
            port.bindToFieldNamed("mXCount");
            port.setAutoPullEnabled(true);
        } else if (port.getName().equals("yCount")) {
            port.bindToFieldNamed("mYCount");
            port.setAutoPullEnabled(true);
        } else if (port.getName().equals("useMipmaps")) {
            port.bindToFieldNamed("mUseMipmaps");
            port.setAutoPullEnabled(true);
        } else if (port.getName().equals("outputWidth")) {
            port.bindToFieldNamed("mOutputWidth");
            port.setAutoPullEnabled(true);
        } else if (port.getName().equals("outputHeight")) {
            port.bindToFieldNamed("mOutputHeight");
            port.setAutoPullEnabled(true);
        }
    }

    protected void onPrepare() {
        this.mShader = ImageShader.createIdentity();
    }

    protected void onProcess() {
        OutputPort outPort = getConnectedOutputPort("image");
        FrameImage2D inputImage = getConnectedInputPort("image").pullFrame().asFrameImage2D();
        int[] inDims = inputImage.getDimensions();
        this.mTileFrame = MipMapUtils.makeMipMappedFrame(this.mTileFrame, inDims);
        setTextureParameter(this.mTileFrame, 10242, 10497);
        setTextureParameter(this.mTileFrame, 10243, 10497);
        this.mShader.setSourceRect(0.0f, 0.0f, 1.0f, 1.0f);
        this.mShader.process(inputImage, this.mTileFrame);
        if (this.mUseMipmaps) {
            generateMipMaps(this.mTileFrame);
        }
        int[] outDims = new int[]{this.mOutputWidth, this.mOutputHeight};
        if (outDims[0] <= 0) {
            outDims[0] = inDims[0] * this.mXCount;
        }
        if (outDims[1] <= 0) {
            outDims[1] = inDims[1] * this.mYCount;
        }
        FrameImage2D outputImage = outPort.fetchAvailableFrame(outDims).asFrameImage2D();
        this.mShader.setSourceRect(0.0f, 0.0f, (float) this.mXCount, (float) this.mYCount);
        this.mShader.process(this.mTileFrame, outputImage);
        outPort.pushFrame(outputImage);
    }

    protected void onClose() {
        if (this.mTileFrame != null) {
            this.mTileFrame.release();
            this.mTileFrame = null;
        }
    }

    private static void setTextureParameter(FrameImage2D frame, int param, int value) {
        frame.lockTextureSource().setParameter(param, value);
        frame.unlock();
    }

    private static void generateMipMaps(FrameImage2D frame) {
        frame.lockTextureSource().generateMipmaps();
        frame.unlock();
    }
}
