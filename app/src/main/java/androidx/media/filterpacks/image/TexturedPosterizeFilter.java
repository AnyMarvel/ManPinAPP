package androidx.media.filterpacks.image;

import android.text.TextUtils;
import androidx.media.filterfw.Filter;
import androidx.media.filterfw.FrameImage2D;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.ImageShader;
import androidx.media.filterfw.InputPort;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.OutputPort;
import androidx.media.filterfw.Signature;
import java.util.Map;
import java.util.TreeMap;

public class TexturedPosterizeFilter extends Filter {
    private static final String mPosterizeShaderCode = "precision mediump float;\nuniform sampler2D tex_sampler_0;\n__TEX_SAMPLERS_DECL__\nuniform float binSize;\nvarying vec2 v_texcoord;\nvoid main() {\n  vec4 color = texture2D(tex_sampler_0, v_texcoord);\n  float gray = (color.r + color.g + color.b) / 3.0;\n  int level = int(floor((gray / binSize) + 0.5));\n  vec4 texColor;\n__LEVEL_SELECT__ {\n    texColor = vec4(0.0, 0.0, 0.0, 1.0);\n  }\n  gl_FragColor = texColor;\n}\n";
    private ImageShader mShader;
    private Map<Integer, InputPort> mTexturePorts = new TreeMap();

    public TexturedPosterizeFilter(MffContext context, String name) {
        super(context, name);
    }

    public Signature getSignature() {
        FrameType imageIn = FrameType.image2D(FrameType.ELEMENT_RGBA8888, 2);
        return new Signature().addInputPort("image", 2, imageIn).addOutputPort("image", 2, FrameType.image2D(FrameType.ELEMENT_RGBA8888, 16)).disallowOtherOutputs();
    }

    protected void onInputPortAttached(InputPort port) {
        String portName = port.getName();
        if (portName.startsWith("level")) {
            this.mTexturePorts.put(Integer.valueOf(Integer.parseInt(portName.substring(5))), port);
        } else if (!portName.equals("image")) {
            throw new IllegalArgumentException(new StringBuilder(String.valueOf(portName).length() + 26).append("Unsupported input port '").append(portName).append("'!").toString());
        }
    }

    protected void onPrepare() {
        checkInputTextures();
        createShader();
    }

    protected void onProcess() {
        OutputPort outPort = getConnectedOutputPort("image");
        FrameImage2D[] inputFrames = new FrameImage2D[(this.mTexturePorts.size() + 1)];
        inputFrames[0] = getConnectedInputPort("image").pullFrame().asFrameImage2D();
        int[] dim = inputFrames[0].getDimensions();
        int i = 1;
        for (InputPort port : this.mTexturePorts.values()) {
            int i2 = i + 1;
            inputFrames[i] = port.pullFrame().asFrameImage2D();
            i = i2;
        }
        FrameImage2D outputImage = outPort.fetchAvailableFrame(dim).asFrameImage2D();
        this.mShader.setUniformValue("binSize", 1.0f / ((float) (this.mTexturePorts.size() - 1)));
        this.mShader.processMulti(inputFrames, outputImage);
        outPort.pushFrame(outputImage);
    }

    private void createShader() {
        String samplerDecl = generateSamplerDecl();
        this.mShader = new ImageShader(mPosterizeShaderCode.replace("__TEX_SAMPLERS_DECL__", samplerDecl).replace("__LEVEL_SELECT__", generateLevelSelect()));
    }

    private String generateSamplerDecl() {
        int n = this.mTexturePorts.size();
        String[] decls = new String[n];
        for (int i = 0; i < n; i++) {
            decls[i] = "uniform sampler2D tex_sampler_" + (i + 1) + ";";
        }
        return TextUtils.join("\n", decls);
    }

    private String generateLevelSelect() {
        int n = this.mTexturePorts.size();
        String[] cases = new String[n];
        for (int i = 0; i < n; i++) {
            cases[i] = "  if (level == " + i + ") {\n    texColor = texture2D(tex_sampler_" + (i + 1) + ", v_texcoord);\n  } else ";
        }
        return TextUtils.join("\n", cases);
    }

    private void checkInputTextures() {
        int texCount = this.mTexturePorts.size();
        if (texCount < 2) {
            throw new RuntimeException("Must specify at least two input texture levels!");
        }
        for (int i = 0; i < texCount; i++) {
            if (((InputPort) this.mTexturePorts.get(Integer.valueOf(i))) == null) {
                throw new RuntimeException("Missing input port 'level" + i + "'!");
            }
        }
    }
}
