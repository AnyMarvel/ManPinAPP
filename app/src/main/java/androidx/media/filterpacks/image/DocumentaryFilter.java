package androidx.media.filterpacks.image;

import androidx.media.filterfw.Filter;
import androidx.media.filterfw.FrameImage2D;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.ImageShader;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.OutputPort;
import androidx.media.filterfw.Signature;
import java.util.Random;

public class DocumentaryFilter extends Filter {
    private final String mDocumentaryShader = "precision mediump float;\nuniform sampler2D tex_sampler_0;\nuniform vec2 seed;\nuniform float stepsize;\nuniform float inv_max_dist;\nuniform vec2 center;\nvarying vec2 v_texcoord;\nfloat rand(vec2 loc) {\n  return fract(sin(dot((loc + seed), vec2(12.9898, 78.233))) * 43758.5453);\n}\nvoid main() {\n  vec4 color = texture2D(tex_sampler_0, v_texcoord);\n  float dither = rand(v_texcoord);\n  vec3 xform = clamp(2.0 * color.rgb, 0.0, 1.0);\n  vec3 temp = clamp(2.0 * (color.rgb + stepsize), 0.0, 1.0);\n  vec3 new_color = clamp(xform + (temp - xform) * (dither - 0.5), 0.0, 1.0);\n  float gray = dot(new_color, vec3(0.299, 0.587, 0.114));\n  new_color = vec3(gray, gray, gray);\n  float dist = distance(gl_FragCoord.xy, center);\n  float lumen = 0.85 / (1.0 + exp((dist * inv_max_dist - 0.83) * 20.0)) + 0.15;\n  gl_FragColor = vec4(new_color * lumen, color.a);\n}\n";
    private int mHeight = 0;
    private Random mRandom = new Random();
    private ImageShader mShader;
    private int mWidth = 0;

    public DocumentaryFilter(MffContext context, String name) {
        super(context, name);
    }

    public Signature getSignature() {
        FrameType imageIn = FrameType.image2D(FrameType.ELEMENT_RGBA8888, 2);
        return new Signature().addInputPort("image", 2, imageIn).addOutputPort("image", 2, FrameType.image2D(FrameType.ELEMENT_RGBA8888, 16)).disallowOtherPorts();
    }

    private void initParameters() {
        float centerX = (float) (((double) this.mWidth) * 0.5d);
        float centerY = (float) (((double) this.mHeight) * 0.5d);
        float max_dist = (float) Math.sqrt((double) ((centerX * centerX) + (centerY * centerY)));
        this.mShader.setUniformValue("center", new float[]{centerX, centerY});
        this.mShader.setUniformValue("inv_max_dist", 1.0f / max_dist);
        this.mShader.setUniformValue("stepsize", 0.003921569f);
    }

    protected void onPrepare() {
        this.mShader = new ImageShader("precision mediump float;\nuniform sampler2D tex_sampler_0;\nuniform vec2 seed;\nuniform float stepsize;\nuniform float inv_max_dist;\nuniform vec2 center;\nvarying vec2 v_texcoord;\nfloat rand(vec2 loc) {\n  return fract(sin(dot((loc + seed), vec2(12.9898, 78.233))) * 43758.5453);\n}\nvoid main() {\n  vec4 color = texture2D(tex_sampler_0, v_texcoord);\n  float dither = rand(v_texcoord);\n  vec3 xform = clamp(2.0 * color.rgb, 0.0, 1.0);\n  vec3 temp = clamp(2.0 * (color.rgb + stepsize), 0.0, 1.0);\n  vec3 new_color = clamp(xform + (temp - xform) * (dither - 0.5), 0.0, 1.0);\n  float gray = dot(new_color, vec3(0.299, 0.587, 0.114));\n  new_color = vec3(gray, gray, gray);\n  float dist = distance(gl_FragCoord.xy, center);\n  float lumen = 0.85 / (1.0 + exp((dist * inv_max_dist - 0.83) * 20.0)) + 0.15;\n  gl_FragColor = vec4(new_color * lumen, color.a);\n}\n");
        initParameters();
    }

    protected synchronized void onProcess() {
        FrameImage2D inputImage = getConnectedInputPort("image").pullFrame().asFrameImage2D();
        int[] dim = inputImage.getDimensions();
        OutputPort outPort = getConnectedOutputPort("image");
        FrameImage2D outputImage = outPort.fetchAvailableFrame(dim).asFrameImage2D();
        if (!(inputImage.getWidth() == this.mWidth && inputImage.getHeight() == this.mHeight)) {
            this.mWidth = inputImage.getWidth();
            this.mHeight = inputImage.getHeight();
            initParameters();
        }
        this.mShader.setUniformValue("seed", new float[]{this.mRandom.nextFloat(), this.mRandom.nextFloat()});
        this.mShader.process(inputImage, outputImage);
        outPort.pushFrame(outputImage);
    }
}
