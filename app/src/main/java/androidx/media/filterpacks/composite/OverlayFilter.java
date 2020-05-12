package androidx.media.filterpacks.composite;

import androidx.media.filterfw.Filter;
import androidx.media.filterfw.FrameImage2D;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.ImageShader;
import androidx.media.filterfw.InputPort;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.OutputPort;
import androidx.media.filterfw.Signature;
import androidx.media.filterfw.geometry.Quad;
import com.google.protos.humansensing.FaceProtos.Face.Landmark.Type;

public final class OverlayFilter extends Filter {
    public static final int OVERLAY_ADD = 2;
    public static final int OVERLAY_BURN = 8;
    public static final int OVERLAY_DARKEN = 11;
    public static final int OVERLAY_DIFFERENCE = 5;
    public static final int OVERLAY_DIVIDE = 3;
    public static final int OVERLAY_DODGE = 7;
    public static final int OVERLAY_HARDLIGHT = 9;
    public static final int OVERLAY_LIGHTEN = 12;
    public static final int OVERLAY_MULTIPLY = 1;
    public static final int OVERLAY_NORMAL = 0;
    public static final int OVERLAY_OVERLAY = 13;
    public static final int OVERLAY_SCREEN = 6;
    public static final int OVERLAY_SOFTLIGHT = 10;
    public static final int OVERLAY_SQUARED_DIFFERENCE = 14;
    public static final int OVERLAY_SUBTRACT = 4;
    private static final Quad[] mDefaultQuads = new Quad[]{Quad.fromRect(0.0f, 0.0f, 1.0f, 1.0f)};
    private boolean mHasMask = false;
    private ImageShader mIdShader;
    private int mInputFrameCount = 1;
    private int mOldOverlayMode = -1;
    private float mOpacity = 1.0f;
    private int mOverlayMode = 0;
    private ImageShader mOverlayShader;
    private Quad[] mSourceQuads = null;
    private Quad[] mTargetQuads = null;

    public OverlayFilter(MffContext context, String name) {
        super(context, name);
    }

    public Signature getSignature() {
        FrameType imageIn = FrameType.image2D(FrameType.ELEMENT_RGBA8888, 2);
        return new Signature().addInputPort("source", 2, imageIn).addInputPort("overlay", 2, imageIn).addInputPort("mask", 1, imageIn).addInputPort("opacity", 1, FrameType.single(Float.TYPE)).addInputPort("mode", 1, FrameType.single(Integer.TYPE)).addInputPort("sourceQuads", 1, FrameType.array(Quad.class)).addInputPort("targetQuads", 1, FrameType.array(Quad.class)).addOutputPort("composite", 2, FrameType.image2D(FrameType.ELEMENT_RGBA8888, 16)).disallowOtherPorts();
    }

    public void onInputPortAttached(InputPort port) {
        if (port.getName().equals("mask")) {
            this.mHasMask = true;
        }
    }

    public void onInputPortOpen(InputPort port) {
        if (port.getName().equals("opacity")) {
            port.bindToFieldNamed("mOpacity");
            port.setAutoPullEnabled(true);
        } else if (port.getName().equals("sourceQuads")) {
            port.bindToFieldNamed("mSourceQuads");
            port.setAutoPullEnabled(true);
        } else if (port.getName().equals("targetQuads")) {
            port.bindToFieldNamed("mTargetQuads");
            port.setAutoPullEnabled(true);
        } else if (port.getName().equals("mode")) {
            port.bindToFieldNamed("mOverlayMode");
            port.setAutoPullEnabled(true);
        }
    }

    protected void onPrepare() {
        this.mIdShader = ImageShader.createIdentity();
    }

    protected void onProcess() {
        OutputPort outPort = getConnectedOutputPort("composite");
        boolean needsSource = needSourceForMode(this.mOverlayMode);
        if (this.mOverlayMode != this.mOldOverlayMode) {
            createShader(needsSource);
            updateInputCount(needsSource);
            this.mOldOverlayMode = this.mOverlayMode;
        }
        FrameImage2D inputSource = getConnectedInputPort("source").pullFrame().asFrameImage2D();
        FrameImage2D inputOverlay = getConnectedInputPort("overlay").pullFrame().asFrameImage2D();
        FrameImage2D inputMask = null;
        if (this.mHasMask) {
            inputMask = getConnectedInputPort("mask").pullFrame().asFrameImage2D();
        }
        FrameImage2D outputImage = outPort.fetchAvailableFrame(inputSource.getDimensions()).asFrameImage2D();
        this.mIdShader.process(inputSource, outputImage);
        this.mOverlayShader.setUniformValue("opacity", this.mOpacity);
        if (this.mSourceQuads == null || this.mTargetQuads == null || this.mSourceQuads.length == this.mTargetQuads.length) {
            Quad[] sourceQuads = mDefaultQuads;
            Quad[] targetQuads = mDefaultQuads;
            boolean quadsSpecified = false;
            if (this.mSourceQuads != null) {
                sourceQuads = this.mSourceQuads;
                quadsSpecified = true;
            }
            if (this.mTargetQuads != null) {
                targetQuads = this.mTargetQuads;
                quadsSpecified = true;
            }
            int passes = quadsSpecified ? passCountFor(this.mSourceQuads, this.mTargetQuads) : 1;
            int i = 0;
            while (i < passes) {
                int j;
                Quad sourceQuad = sourceQuads[i < sourceQuads.length ? i : 0];
                Quad targetQuad = targetQuads[i < targetQuads.length ? i : 0];
                this.mOverlayShader.setSourceQuad(sourceQuad);
                this.mOverlayShader.setTargetQuad(targetQuad);
                if (needsSource) {
                    this.mOverlayShader.setAttributeValues("a_texcoord_src", targetQuad.asCoords(), 2);
                }
                FrameImage2D[] inputs = new FrameImage2D[this.mInputFrameCount];
                int j2 = 0 + 1;
                inputs[0] = inputOverlay;
                if (needsSource) {
                    j = j2 + 1;
                    inputs[j2] = inputSource;
                } else {
                    j = j2;
                }
                if (this.mHasMask) {
                    j2 = j + 1;
                    inputs[j] = inputMask;
                    j = j2;
                }
                this.mOverlayShader.processMulti(inputs, outputImage);
                i++;
            }
            outputImage.setTimestamp(inputSource.getTimestamp());
            outPort.pushFrame(outputImage);
            return;
        }
        throw new RuntimeException("Mismatch between input source quad count (" + this.mSourceQuads.length + ") and target quad count (" + this.mTargetQuads.length + ")!");
    }

    private int passCountFor(Quad[] sourceQuads, Quad[] targetQuads) {
        if (sourceQuads == null) {
            return targetQuads.length;
        }
        if (targetQuads == null) {
            return sourceQuads.length;
        }
        if (sourceQuads.length == targetQuads.length) {
            return sourceQuads.length;
        }
        int length = sourceQuads.length;
        throw new RuntimeException("Mismatch between input source quad count (" + length + ") and target quad count (" + targetQuads.length + ")!");
    }

    private void createShader(boolean needsSource) {
        this.mOverlayShader = new ImageShader(genVertexShader(needsSource, this.mHasMask), genFragmentShader(needsSource, this.mHasMask));
        if (this.mHasMask) {
            this.mOverlayShader.setAttributeValues("a_texcoord_full", new float[]{0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f}, 2);
        }
        this.mOverlayShader.setBlendEnabled(true);
        this.mOverlayShader.setBlendFunc(770, 771);
    }

    private String getOutputColor() {
        switch (this.mOverlayMode) {
            case 1:
                return "srcColor.rgb * ovlColor.rgb";
            case 2:
                return "srcColor.rgb + ovlColor.rgb";
            case 3:
                return "srcColor.rgb / ovlColor.rgb";
            case 4:
                return "srcColor.rgb - ovlColor.rgb";
            case 5:
                return "abs(srcColor.rgb - ovlColor.rgb)";
            case 6:
                return "1.0 - ((1.0 - ovlColor.rgb) * (1.0 - srcColor.rgb))";
            case 7:
                return "srcColor.rgb / (1.0 - ovlColor.rgb)";
            case 8:
                return "1.0 - ((1.0 - srcColor.rgb) / ovlColor.rgb)";
            case 9:
                return "vec3(ovlColor.r > 0.5 ? 1.0 - ((1.0 - 2.0 * (ovlColor.r - 0.5)) * (1.0 - srcColor.r)) : (2.0 * ovlColor.r * srcColor.r),     ovlColor.g > 0.5 ? 1.0 - ((1.0 - 2.0 * (ovlColor.g - 0.5)) * (1.0 - srcColor.g)) : (2.0 * ovlColor.g * srcColor.g),     ovlColor.b > 0.5 ? 1.0 - ((1.0 - 2.0 * (ovlColor.b - 0.5)) * (1.0 - srcColor.b)) : (2.0 * ovlColor.b * srcColor.b))";
            case 10:
                return "srcColor.rgb * ((1.0 - srcColor.rgb) * ovlColor.rgb + (1.0 - ((1.0 - ovlColor.rgb) * (1.0 - srcColor.rgb))))";
            case 11:
                return "min(srcColor.rgb, ovlColor.rgb)";
            case 12:
                return "max(srcColor.rgb, ovlColor.rgb)";
            case 13:
                return "srcColor.rgb * (srcColor.rgb + (2.0 * ovlColor.rgb) * (1.0 - srcColor.rgb))";
            case 14:
                return "(srcColor.rgb - ovlColor.rgb) * (srcColor.rgb - ovlColor.rgb)";
            default:
                return "ovlColor.rgb";
        }
    }

    private void updateInputCount(boolean needsSource) {
        this.mInputFrameCount = 1;
        if (needsSource) {
            this.mInputFrameCount++;
        }
        if (this.mHasMask) {
            this.mInputFrameCount++;
        }
    }

    private String genVertexShader(boolean needsSource, boolean hasMask) {
        String str;
        String str2 = hasMask ? "attribute vec2 a_texcoord_full;\n" : "";
        String str3 = needsSource ? "attribute vec2 a_texcoord_src;\n" : "";
        String str4 = hasMask ? "varying vec2 v_texcoord_mask;\n" : "";
        String str5 = needsSource ? "varying vec2 v_texcoord_src;\n" : "";
        String str6 = hasMask ? "v_texcoord_mask = a_texcoord_full;\n" : "";
        if (needsSource) {
            str = "v_texcoord_src = a_texcoord_src;\n";
        } else {
            str = "";
        }
        return new StringBuilder((((((String.valueOf(str2).length() + 150) + String.valueOf(str3).length()) + String.valueOf(str4).length()) + String.valueOf(str5).length()) + String.valueOf(str6).length()) + String.valueOf(str).length()).append("attribute vec4 a_position;\nattribute vec2 a_texcoord;\n").append(str2).append(str3).append("varying vec2 v_texcoord;\n").append(str4).append(str5).append("void main() {\n  gl_Position = a_position;\n  v_texcoord = a_texcoord;\n").append(str6).append(str).append("}\n").toString();
    }

    private String genFragmentShader(boolean needsSource, boolean hasMask) {
        String str;
        String stringBuilder;
        String srcSampler = "tex_sampler_1";
        String maskSampler = needsSource ? "tex_sampler_2" : "tex_sampler_1";
        String str2 = needsSource ? "uniform sampler2D tex_sampler_1;\n" : "";
        String stringBuilder2 = hasMask ? new StringBuilder(String.valueOf(maskSampler).length() + 20).append("uniform sampler2D ").append(maskSampler).append(";\n").toString() : "";
        String str3 = hasMask ? "varying vec2 v_texcoord_mask;\n" : "";
        String str4 = needsSource ? "varying vec2 v_texcoord_src;\n" : "";
        if (needsSource) {
            str = "  vec4 srcColor = texture2D(tex_sampler_1, v_texcoord_src);\n";
        } else {
            str = "";
        }
        if (hasMask) {
            stringBuilder = new StringBuilder(String.valueOf(maskSampler).length() + 45).append("ovlColor.a = texture2D(").append(maskSampler).append(", v_texcoord_mask).a;\n").toString();
        } else {
            stringBuilder = "";
        }
        String outputColor = getOutputColor();
        return new StringBuilder(((((((String.valueOf(str2).length() + Type.RIGHT_EYE_RIGHT_CORNER_VALUE) + String.valueOf(stringBuilder2).length()) + String.valueOf(str3).length()) + String.valueOf(str4).length()) + String.valueOf(str).length()) + String.valueOf(stringBuilder).length()) + String.valueOf(outputColor).length()).append("precision mediump float;\nuniform sampler2D tex_sampler_0;\n").append(str2).append(stringBuilder2).append("uniform float opacity;\nvarying vec2 v_texcoord;\n").append(str3).append(str4).append("void main() {\n  vec4 ovlColor = texture2D(tex_sampler_0, v_texcoord);\n").append(str).append(stringBuilder).append("  gl_FragColor = vec4(").append(outputColor).append(", ovlColor.a * opacity);\n}\n").toString();
    }

    private static boolean needSourceForMode(int mode) {
        return mode != 0;
    }
}
