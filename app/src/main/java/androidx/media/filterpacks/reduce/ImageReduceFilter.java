package androidx.media.filterpacks.reduce;

import androidx.media.filterfw.Filter;
import androidx.media.filterfw.Frame;
import androidx.media.filterfw.FrameImage2D;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.ImageShader;
import androidx.media.filterfw.InputPort;
import androidx.media.filterfw.InputPort.FrameListener;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.OutputPort;
import androidx.media.filterfw.Signature;
import java.util.Iterator;
import java.util.Vector;

public class ImageReduceFilter extends Filter {
    public static final int CHANNEL_AVG = 4;
    public static final int CHANNEL_BLUE = 3;
    public static final int CHANNEL_GRAY = 8;
    public static final int CHANNEL_GREEN = 2;
    public static final int CHANNEL_MAX = 7;
    public static final int CHANNEL_MIN = 6;
    public static final int CHANNEL_RED = 1;
    public static final int CHANNEL_SUM = 5;
    public static final int OPERATION_AVG = 3;
    public static final int OPERATION_MAX = 1;
    public static final int OPERATION_MIN = 2;
    public static final int OPERATION_PRODUCT = 5;
    public static final int OPERATION_SUM = 4;
    private int mChannel = 8;
    private FrameListener mChannelListener = new FrameListener() {
        public void onFrameReceived(InputPort port, Frame frame) {
            int channel = ((Integer) frame.asFrameValue().getValue()).intValue();
            if (channel != ImageReduceFilter.this.mChannel) {
                ImageReduceFilter.this.mChannel = channel;
                ImageReduceFilter.this.mShaderDirtyFlag = true;
            }
        }
    };
    private int mCurrentHeight = 0;
    private int mCurrentWidth = 0;
    private int mLevel = -1;
    private int mOperation = 3;
    private FrameListener mOperationListener = new FrameListener() {
        public void onFrameReceived(InputPort port, Frame frame) {
            int operation = ((Integer) frame.asFrameValue().getValue()).intValue();
            if (operation != ImageReduceFilter.this.mOperation) {
                ImageReduceFilter.this.mOperation = operation;
                ImageReduceFilter.this.mShaderDirtyFlag = true;
            }
        }
    };
    private Vector<FrameImage2D> mPyramid = new Vector();
    private ImageShader mShader = null;
    private boolean mShaderDirtyFlag = false;

    public static class PyramidLevel {
        private int mHeight;
        private int mLevel;
        private int mWidth;

        private PyramidLevel(int level, int width, int height) {
            this.mLevel = level;
            this.mWidth = width;
            this.mHeight = height;
        }

        int getLevel() {
            return this.mLevel;
        }

        int getWidth() {
            return this.mWidth;
        }

        int getHeight() {
            return this.mHeight;
        }

        int[] getDimensions() {
            return new int[]{this.mWidth, this.mHeight};
        }
    }

    public ImageReduceFilter(MffContext context, String name) {
        super(context, name);
    }

    public PyramidLevel[] getPyramidDims(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Illegal image dimensions: " + width + "x" + height + "!");
        }
        Vector<PyramidLevel> pyramidLevels = new Vector();
        PyramidLevel curLevel = new PyramidLevel(0, width, height);
        int i = 1;
        while (true) {
            pyramidLevels.add(curLevel);
            if (curLevel.getWidth() == 1 && curLevel.getHeight() == 1) {
                return (PyramidLevel[]) pyramidLevels.toArray(new PyramidLevel[0]);
            }
            PyramidLevel curLevel2 = new PyramidLevel(i, (curLevel.getWidth() + 1) / 2, (curLevel.getHeight() + 1) / 2);
            i++;
            curLevel = curLevel2;
        }
    }

    public Signature getSignature() {
        FrameType imageIn = FrameType.image2D(FrameType.ELEMENT_RGBA8888, 2);
        return new Signature().addInputPort("image", 2, imageIn).addInputPort("operation", 1, FrameType.single(Integer.TYPE)).addInputPort("level", 1, FrameType.single(Integer.TYPE)).addInputPort("channel", 1, FrameType.single(Integer.TYPE)).addOutputPort("image", 2, FrameType.image2D(FrameType.ELEMENT_RGBA8888, 16)).disallowOtherPorts();
    }

    protected void onInputPortOpen(InputPort port) {
        if (port.getName().equals("level")) {
            port.bindToFieldNamed("mLevel");
            port.setAutoPullEnabled(true);
        } else if (port.getName().equals("operation")) {
            port.bindToListener(this.mOperationListener);
            port.setAutoPullEnabled(true);
        } else if (port.getName().equals("channel")) {
            port.bindToListener(this.mChannelListener);
            port.setAutoPullEnabled(true);
        }
    }

    protected void onOpen() {
        this.mShaderDirtyFlag = true;
    }

    protected void onProcess() {
        OutputPort outPort = getConnectedOutputPort("image");
        FrameImage2D inputImage = getConnectedInputPort("image").pullFrame().asFrameImage2D();
        int[] inDims = inputImage.getDimensions();
        if (this.mShaderDirtyFlag) {
            regnerateShader();
            this.mShaderDirtyFlag = false;
        }
        if (!(inDims[0] == this.mCurrentWidth && inDims[1] == this.mCurrentHeight)) {
            this.mCurrentWidth = inDims[0];
            this.mCurrentHeight = inDims[1];
            regenerateImagePyramid();
        }
        if (this.mLevel >= this.mPyramid.size() || this.mLevel < 0) {
            this.mLevel = this.mPyramid.size() - 1;
        }
        FrameImage2D outputImage = outPort.fetchAvailableFrame(((FrameImage2D) this.mPyramid.get(this.mLevel)).getDimensions()).asFrameImage2D();
        for (int i = 0; i < this.mLevel; i++) {
            runReduce(getPyramidLevel(i, inputImage, outputImage), getPyramidLevel(i + 1, inputImage, outputImage));
        }
        outPort.pushFrame(getPyramidLevel(this.mLevel, inputImage, outputImage));
    }

    private FrameImage2D getPyramidLevel(int level, FrameImage2D input, FrameImage2D output) {
        if (level == 0) {
            return input;
        }
        if (level >= this.mLevel) {
            return output;
        }
        return (FrameImage2D) this.mPyramid.get(level);
    }

    private void runReduce(FrameImage2D source, FrameImage2D target) {
        float ash = 1.0f;
        int sw = source.getWidth();
        int sh = source.getHeight();
        int tw = target.getWidth();
        int th = target.getHeight();
        float asw = tw == sw ? 1.0f : (((float) tw) * 2.0f) / ((float) sw);
        if (th != sh) {
            ash = (((float) th) * 2.0f) / ((float) sh);
        }
        this.mShader.setSourceRect(0.0f, 0.0f, asw, ash);
        this.mShader.setUniformValue("pix", new float[]{0.5f / ((float) sw), 0.5f / ((float) sh)});
        this.mShader.process(source, target);
    }

    private void regnerateShader() {
        this.mShader = new ImageShader(genFragmentShader());
    }

    private void regenerateImagePyramid() {
        Iterator it = this.mPyramid.iterator();
        while (it.hasNext()) {
            ((FrameImage2D) it.next()).release();
        }
        this.mPyramid.clear();
        PyramidLevel[] levels = getPyramidDims(this.mCurrentWidth, this.mCurrentHeight);
        FrameType imageType = FrameType.image2D(FrameType.ELEMENT_RGBA8888, 18);
        for (PyramidLevel dimensions : levels) {
            this.mPyramid.add(Frame.create(imageType, dimensions.getDimensions()).asFrameImage2D());
        }
    }

    private String genFragmentShader() {
        String genReduceAlgorithm = genReduceAlgorithm();
        String genColorToValueAlgorithm = genColorToValueAlgorithm();
        return new StringBuilder((String.valueOf(genReduceAlgorithm).length() + 648) + String.valueOf(genColorToValueAlgorithm).length()).append("precision mediump float;\nuniform sampler2D tex_sampler_0;\nuniform vec2 pix;\nvarying vec2 v_texcoord;\n\nfloat reduce(float v0, float v1, float v2, float v3) {\n  return ").append(genReduceAlgorithm).append(";\n}\n\nfloat colorValue(vec4 color) {\n  return ").append(genColorToValueAlgorithm).append(";\n}\nvoid main() {\n  float c0 = colorValue(texture2D(tex_sampler_0, v_texcoord + vec2(-pix.x, -pix.y)));\n  float c1 = colorValue(texture2D(tex_sampler_0, v_texcoord + vec2(+pix.x, -pix.y)));\n  float c2 = colorValue(texture2D(tex_sampler_0, v_texcoord + vec2(-pix.x, +pix.y)));\n  float c3 = colorValue(texture2D(tex_sampler_0, v_texcoord + vec2(+pix.x, +pix.y)));\n  float r = reduce(c0, c1, c2, c3);\n  gl_FragColor = vec4(r, r, r, 1.0);\n}\n").toString();
    }

    private String genReduceAlgorithm() {
        switch (this.mOperation) {
            case 1:
                return "max(max(v0, v1), max(v2, v3))";
            case 2:
                return "min(min(v0, v1), min(v2, v3))";
            case 3:
                return "(v0 + v1 + v2 + v3) / 4.0";
            case 4:
                return "(v0 + v1 + v2 + v3)";
            case 5:
                return "(v0 * v1 * v2 * v3)";
            default:
                throw new IllegalArgumentException("Unknown operation: " + this.mOperation + "!");
        }
    }

    private String genColorToValueAlgorithm() {
        switch (this.mChannel) {
            case 1:
                return "color.r";
            case 2:
                return "color.g";
            case 3:
                return "color.b";
            case 4:
                return "(color.r + color.g + color.b) / 3.0";
            case 5:
                return "(color.r + color.g + color.b)";
            case 6:
                return "min(color.r, min(color.g, color.b))";
            case 7:
                return "max(color.r, max(color.g, color.b))";
            case 8:
                return "dot(color, vec4(0.299, 0.587, 0.114, 0))";
            default:
                throw new IllegalArgumentException("Unknown channel: " + this.mChannel + "!");
        }
    }
}
