package androidx.media.filterpacks.image;

import android.text.TextUtils;
import android.util.Log;
import androidx.media.filterfw.Filter;
import androidx.media.filterfw.FrameImage2D;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.ImageShader;
import androidx.media.filterfw.InputPort;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.OutputPort;
import androidx.media.filterfw.Signature;
import java.util.Arrays;
import java.util.Vector;

public final class ConvolutionFilter extends Filter {
    private static final String mConvolutionShader = "precision mediump float;\nuniform sampler2D tex_sampler_0;\nvarying vec2 v_texcoord;\nvoid main() {\n  gl_FragColor = __CONVOLUTION__;\n}\n";
    private float[] mMask = null;
    private int mMaskHeight = 0;
    private int mMaskWidth = 0;
    private int[] mOldDim = null;
    private float[] mOldMask = null;
    private ImageShader mShader;

    public ConvolutionFilter(MffContext context, String name) {
        super(context, name);
    }

    public Signature getSignature() {
        FrameType imageIn = FrameType.image2D(FrameType.ELEMENT_RGBA8888, 2);
        return new Signature().addInputPort("image", 2, imageIn).addInputPort("mask", 1, FrameType.array(Float.TYPE)).addInputPort("maskWidth", 1, FrameType.single(Integer.TYPE)).addInputPort("maskHeight", 1, FrameType.single(Integer.TYPE)).addOutputPort("image", 2, FrameType.image2D(FrameType.ELEMENT_RGBA8888, 16)).disallowOtherPorts();
    }

    public void onInputPortOpen(InputPort port) {
        if (port.getName().equals("mask")) {
            port.bindToFieldNamed("mMask");
            port.setAutoPullEnabled(true);
        } else if (port.getName().equals("maskWidth")) {
            port.bindToFieldNamed("mMaskWidth");
            port.setAutoPullEnabled(true);
        } else if (port.getName().equals("maskHeight")) {
            port.bindToFieldNamed("mMaskHeight");
            port.setAutoPullEnabled(true);
        }
    }

    protected void onProcess() {
        OutputPort outPort = getConnectedOutputPort("image");
        FrameImage2D inputImage = getConnectedInputPort("image").pullFrame().asFrameImage2D();
        int[] dim = inputImage.getDimensions();
        FrameImage2D outputImage = outPort.fetchAvailableFrame(dim).asFrameImage2D();
        if (this.mMask == null) {
            throw new NullPointerException("No mask specified!");
        }
        if (!(Arrays.equals(this.mOldMask, this.mMask) && Arrays.equals(dim, this.mOldDim))) {
            updateMaskSize();
            createShader(dim[0], dim[1]);
            this.mOldMask = this.mMask;
            this.mOldDim = dim;
        }
        this.mShader.process(inputImage, outputImage);
        outPort.pushFrame(outputImage);
    }

    private void updateMaskSize() {
        if (this.mMaskWidth != 0 && this.mMaskHeight != 0) {
            return;
        }
        if (isPerfectSquare(this.mMask.length)) {
            int r = (int) Math.sqrt((double) this.mMask.length);
            if (r % 2 != 1) {
                throw new IllegalArgumentException("Illegal mask size " + this.mMask.length + "! Each dimension must contain odd number of entries!");
            }
            this.mMaskWidth = r;
            this.mMaskHeight = r;
            return;
        }
        throw new IllegalArgumentException("Illegal mask size " + this.mMask.length + "! Must be power of 2 size!");
    }

    private void createShader(int width, int height) {
        String shaderCode = mConvolutionShader.replace("__CONVOLUTION__", generateConvolutionCode(width, height));
        String str = "ConvolutionFilter";
        String str2 = "ShaderCode: ";
        String valueOf = String.valueOf(shaderCode);
        Log.i(str, valueOf.length() != 0 ? str2.concat(valueOf) : new String(str2));
        this.mShader = new ImageShader(shaderCode);
    }

    private String generateConvolutionCode(int width, int height) {
        int i = 0;
        int xs = (this.mMaskWidth - 1) / 2;
        int ys = (this.mMaskHeight - 1) / 2;
        Vector<String> lines = new Vector();
        int y = -ys;
        while (y <= ys) {
            int x = -xs;
            int i2 = i;
            while (x <= xs) {
                float yd = ((float) y) / ((float) height);
                i = i2 + 1;
                lines.add(this.mMask[i2] + " * texture2D(tex_sampler_0, vec2(v_texcoord.x + " + (((float) x) / ((float) width)) + ", v_texcoord.y + " + yd + "))");
                x++;
                i2 = i;
            }
            y++;
            i = i2;
        }
        return TextUtils.join(" + ", lines);
    }

    private boolean isPerfectSquare(int x) {
        double sqx = Math.sqrt((double) x);
        return sqx == ((double) ((int) sqx));
    }
}
