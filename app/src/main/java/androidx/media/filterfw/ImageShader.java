package androidx.media.filterfw;

import android.graphics.RectF;
import android.opengl.GLES20;
import android.util.Log;
import androidx.media.filterfw.geometry.Quad;
import androidx.media.util.Trace;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.HashMap;

public class ImageShader {
    private static final int FLOAT_SIZE = 4;
    private static final String mDefaultVertexShader = "attribute vec4 a_position;\nattribute vec2 a_texcoord;\nvarying vec2 v_texcoord;\nvoid main() {\n  gl_Position = a_position;\n  v_texcoord = a_texcoord;\n}\n";
    private static final String mExternalIdentityShader = "#extension GL_OES_EGL_image_external : require\nprecision lowp float;\nuniform samplerExternalOES tex_sampler_0;\nvarying vec2 v_texcoord;\nvoid main() {\n  gl_FragColor = texture2D(tex_sampler_0, v_texcoord);\n}\n";
    private static final String mIdentityShader = "precision lowp float;\nuniform sampler2D tex_sampler_0;\nvarying vec2 v_texcoord;\nvoid main() {\n  gl_FragColor = texture2D(tex_sampler_0, v_texcoord);\n}\n";
    private HashMap<String, VertexAttribute> mAttributes = new HashMap();
    private int mBaseTexUnit = 33984;
    private boolean mBlendEnabled = false;
    private int mClearBuffers = 16384;
    private float[] mClearColor = new float[]{0.0f, 0.0f, 0.0f, 0.0f};
    private boolean mClearsOutput = false;
    private int mDFactor = 771;
    private int mDrawMode = 5;
    private int mProgram = 0;
    private int mSFactor = 770;
    private float[] mSourceCoords = new float[]{0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f};
    private float[] mTargetCoords = new float[]{-1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f};
    private HashMap<String, ProgramUniform> mUniforms;
    private int mVertexCount = 4;

    private static final class ProgramUniform {
        private int mLocation;
        private String mName;
        private int mSize;
        private int mType;

        public ProgramUniform(int program, int index) {
            int[] len = new int[1];
            GLES20.glGetProgramiv(program, 35719, len, 0);
            int[] type = new int[1];
            int[] size = new int[1];
            byte[] name = new byte[len[0]];
            int i = program;
            int i2 = index;
            GLES20.glGetActiveUniform(i, i2, len[0], new int[1], 0, size, 0, type, 0, name, 0);
            this.mName = new String(name, 0, ImageShader.strlen(name));
            this.mLocation = GLES20.glGetUniformLocation(program, this.mName);
            this.mType = type[0];
            this.mSize = size[0];
            GLToolbox.checkGlError("Initializing uniform");
        }

        public String getName() {
            return this.mName;
        }

        public int getType() {
            return this.mType;
        }

        public int getLocation() {
            return this.mLocation;
        }

        public int getSize() {
            return this.mSize;
        }
    }

    private static class VertexAttribute {
        private int mComponents;
        private int mIndex;
        private boolean mIsConst;
        private int mLength = -1;
        private String mName;
        private int mOffset;
        private boolean mShouldNormalize;
        private int mStride;
        private int mType;
        private FloatBuffer mValues;
        private int mVbo;

        public VertexAttribute(String name, int index) {
            this.mName = name;
            this.mIndex = index;
        }

        public void set(boolean normalize, int stride, int components, int type, float[] values) {
            this.mIsConst = false;
            this.mShouldNormalize = normalize;
            this.mStride = stride;
            this.mComponents = components;
            this.mType = type;
            this.mVbo = 0;
            if (this.mLength != values.length) {
                initBuffer(values);
                this.mLength = values.length;
            }
            copyValues(values);
        }

        public void set(boolean normalize, int offset, int stride, int components, int type, int vbo) {
            this.mIsConst = false;
            this.mShouldNormalize = normalize;
            this.mOffset = offset;
            this.mStride = stride;
            this.mComponents = components;
            this.mType = type;
            this.mVbo = vbo;
            this.mValues = null;
        }

        public boolean push() {
            if (this.mIsConst) {
                switch (this.mComponents) {
                    case 1:
                        GLES20.glVertexAttrib1fv(this.mIndex, this.mValues);
                        break;
                    case 2:
                        GLES20.glVertexAttrib2fv(this.mIndex, this.mValues);
                        break;
                    case 3:
                        GLES20.glVertexAttrib3fv(this.mIndex, this.mValues);
                        break;
                    case 4:
                        GLES20.glVertexAttrib4fv(this.mIndex, this.mValues);
                        break;
                    default:
                        return false;
                }
                GLES20.glDisableVertexAttribArray(this.mIndex);
            } else {
                if (this.mValues != null) {
                    GLES20.glBindBuffer(34962, 0);
                    GLES20.glVertexAttribPointer(this.mIndex, this.mComponents, this.mType, this.mShouldNormalize, this.mStride, this.mValues);
                } else {
                    GLES20.glBindBuffer(34962, this.mVbo);
                    GLES20.glVertexAttribPointer(this.mIndex, this.mComponents, this.mType, this.mShouldNormalize, this.mStride, this.mOffset);
                }
                GLES20.glEnableVertexAttribArray(this.mIndex);
            }
            GLToolbox.checkGlError("Set vertex-attribute values");
            return true;
        }

        public String toString() {
            return this.mName;
        }

        private void initBuffer(float[] values) {
            this.mValues = ByteBuffer.allocateDirect(values.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        }

        private void copyValues(float[] values) {
            this.mValues.put(values).position(0);
        }
    }

    public ImageShader(String fragmentShader) {
        this.mProgram = createProgram(mDefaultVertexShader, fragmentShader);
        scanUniforms();
    }

    public ImageShader(String vertexShader, String fragmentShader) {
        this.mProgram = createProgram(vertexShader, fragmentShader);
        scanUniforms();
    }

    public static ImageShader createIdentity() {
        return new ImageShader(mIdentityShader);
    }

    public static ImageShader createIdentity(String vertexShader) {
        return new ImageShader(vertexShader, mIdentityShader);
    }

    public static ImageShader createExternalIdentity() {
        return new ImageShader(mExternalIdentityShader);
    }

    public static void renderTextureToTarget(TextureSource texture, RenderTarget target, int width, int height) {
        RenderTarget.currentTarget().getIdentityShader().process(texture, target, width, height);
    }

    public void process(FrameImage2D input, FrameImage2D output) {
        TextureSource texSource = input.lockTextureSource();
        TextureSource[] textureSourceArr = new TextureSource[]{texSource};
        processMulti(textureSourceArr, output.lockRenderTarget(), output.getWidth(), output.getHeight());
        input.unlock();
        output.unlock();
    }

    public void processMulti(FrameImage2D[] inputs, FrameImage2D output) {
        TextureSource[] texSources = new TextureSource[inputs.length];
        for (int i = 0; i < inputs.length; i++) {
            texSources[i] = inputs[i].lockTextureSource();
        }
        processMulti(texSources, output.lockRenderTarget(), output.getWidth(), output.getHeight());
        for (FrameImage2D input : inputs) {
            input.unlock();
        }
        output.unlock();
    }

    public void process(TextureSource texture, RenderTarget target, int width, int height) {
        processMulti(new TextureSource[]{texture}, target, width, height);
    }

    public void processMulti(TextureSource[] sources, RenderTarget target, int width, int height) {
        GLToolbox.checkGlError("Unknown Operation");
        checkExecutable();
        checkTexCount(sources.length);
        focusTarget(target, width, height);
        pushShaderState();
        bindInputTextures(sources);
        render();
    }

    public void processNoInput(FrameImage2D output) {
        processNoInput(output.lockRenderTarget(), output.getWidth(), output.getHeight());
        output.unlock();
    }

    public void processNoInput(RenderTarget target, int width, int height) {
        processMulti(new TextureSource[0], target, width, height);
    }

    public int getUniformLocation(String name) {
        return getProgramUniform(name, true).getLocation();
    }

    public int getAttributeLocation(String name) {
        if (name.equals(positionAttributeName()) || name.equals(texCoordAttributeName())) {
            Log.w("ImageShader", new StringBuilder(String.valueOf(name).length() + 52).append("Attempting to access internal attribute '").append(name).append("' directly!").toString());
        }
        int loc = GLES20.glGetAttribLocation(this.mProgram, name);
        if (loc >= 0) {
            return loc;
        }
        throw new RuntimeException(new StringBuilder(String.valueOf(name).length() + 39).append("Unknown attribute '").append(name).append("' in shader program!").toString());
    }

    public void setUniformValue(String uniformName, int value) {
        useProgram();
        GLES20.glUniform1i(getUniformLocation(uniformName), value);
        GLToolbox.checkGlError(new StringBuilder(String.valueOf(uniformName).length() + 20).append("Set uniform value (").append(uniformName).append(")").toString());
    }

    public void setUniformValue(String uniformName, float value) {
        useProgram();
        GLES20.glUniform1f(getUniformLocation(uniformName), value);
        GLToolbox.checkGlError(new StringBuilder(String.valueOf(uniformName).length() + 20).append("Set uniform value (").append(uniformName).append(")").toString());
    }

    public void setUniformValue(String uniformName, int[] values) {
        ProgramUniform uniform = getProgramUniform(uniformName, true);
        useProgram();
        int len = values.length;
        switch (uniform.getType()) {
            case 5124:
                checkUniformAssignment(uniform, len, 1);
                GLES20.glUniform1iv(uniform.getLocation(), len, values, 0);
                break;
            case 35667:
                checkUniformAssignment(uniform, len, 2);
                GLES20.glUniform2iv(uniform.getLocation(), len / 2, values, 0);
                break;
            case 35668:
                checkUniformAssignment(uniform, len, 3);
                GLES20.glUniform2iv(uniform.getLocation(), len / 3, values, 0);
                break;
            case 35669:
                checkUniformAssignment(uniform, len, 4);
                GLES20.glUniform2iv(uniform.getLocation(), len / 4, values, 0);
                break;
            default:
                throw new RuntimeException(new StringBuilder(String.valueOf(uniformName).length() + 68).append("Cannot assign int-array to incompatible uniform type for uniform '").append(uniformName).append("'!").toString());
        }
        GLToolbox.checkGlError(new StringBuilder(String.valueOf(uniformName).length() + 20).append("Set uniform value (").append(uniformName).append(")").toString());
    }

    public void setUniformValue(String uniformName, float[] values) {
        ProgramUniform uniform = getProgramUniform(uniformName, true);
        useProgram();
        int len = values.length;
        switch (uniform.getType()) {
            case 5126:
                checkUniformAssignment(uniform, len, 1);
                GLES20.glUniform1fv(uniform.getLocation(), len, values, 0);
                break;
            case 35664:
                checkUniformAssignment(uniform, len, 2);
                GLES20.glUniform2fv(uniform.getLocation(), len / 2, values, 0);
                break;
            case 35665:
                checkUniformAssignment(uniform, len, 3);
                GLES20.glUniform3fv(uniform.getLocation(), len / 3, values, 0);
                break;
            case 35666:
                checkUniformAssignment(uniform, len, 4);
                GLES20.glUniform4fv(uniform.getLocation(), len / 4, values, 0);
                break;
            case 35674:
                checkUniformAssignment(uniform, len, 4);
                GLES20.glUniformMatrix2fv(uniform.getLocation(), len / 4, false, values, 0);
                break;
            case 35675:
                checkUniformAssignment(uniform, len, 9);
                GLES20.glUniformMatrix3fv(uniform.getLocation(), len / 9, false, values, 0);
                break;
            case 35676:
                checkUniformAssignment(uniform, len, 16);
                GLES20.glUniformMatrix4fv(uniform.getLocation(), len / 16, false, values, 0);
                break;
            default:
                throw new RuntimeException(new StringBuilder(String.valueOf(uniformName).length() + 70).append("Cannot assign float-array to incompatible uniform type for uniform '").append(uniformName).append("'!").toString());
        }
        GLToolbox.checkGlError(new StringBuilder(String.valueOf(uniformName).length() + 20).append("Set uniform value (").append(uniformName).append(")").toString());
    }

    public void setAttributeValues(String attributeName, float[] data, int components) {
        getProgramAttribute(attributeName, true).set(false, components * 4, components, 5126, data);
    }

    public void setAttributeValues(String attributeName, int vbo, int type, int components, int stride, int offset, boolean normalize) {
        getProgramAttribute(attributeName, true).set(normalize, offset, stride, components, type, vbo);
    }

    public void setSourceRect(float x, float y, float width, float height) {
        setSourceCoords(new float[]{x, y, x + width, y, x, y + height, x + width, y + height});
    }

    public void setSourceRect(RectF rect) {
        setSourceRect(rect.left, rect.top, rect.right - rect.left, rect.bottom - rect.top);
    }

    public void setSourceQuad(Quad quad) {
        setSourceCoords(new float[]{quad.topLeft().x, quad.topLeft().y, quad.topRight().x, quad.topRight().y, quad.bottomLeft().x, quad.bottomLeft().y, quad.bottomRight().x, quad.bottomRight().y});
    }

    public void setSourceCoords(float[] coords) {
        if (coords.length != 8) {
            throw new IllegalArgumentException("Expected 8 coordinates as source coordinates but got " + coords.length + " coordinates!");
        } else {
            this.mSourceCoords = Arrays.copyOf(coords, 8);
        }
    }

    public void setSourceTransform(float[] matrix) {
        if (matrix.length != 16) {
            throw new IllegalArgumentException("Expected 4x4 matrix for source transform!");
        }
        setSourceCoords(new float[]{matrix[12], matrix[13], matrix[0] + matrix[12], matrix[1] + matrix[13], matrix[4] + matrix[12], matrix[5] + matrix[13], (matrix[0] + matrix[4]) + matrix[12], (matrix[1] + matrix[5]) + matrix[13]});
    }

    public void setTargetRect(float x, float y, float width, float height) {
        setTargetCoords(new float[]{x, y, x + width, y, x, y + height, x + width, y + height});
    }

    public void setTargetRect(RectF rect) {
        setTargetCoords(new float[]{rect.left, rect.top, rect.right, rect.top, rect.left, rect.bottom, rect.right, rect.bottom});
    }

    public void setTargetQuad(Quad quad) {
        setTargetCoords(new float[]{quad.topLeft().x, quad.topLeft().y, quad.topRight().x, quad.topRight().y, quad.bottomLeft().x, quad.bottomLeft().y, quad.bottomRight().x, quad.bottomRight().y});
    }

    public void setTargetCoords(float[] coords) {
        if (coords.length != 8) {
            throw new IllegalArgumentException("Expected 8 coordinates as target coordinates but got " + coords.length + " coordinates!");
        }
        this.mTargetCoords = new float[8];
        for (int i = 0; i < 8; i++) {
            this.mTargetCoords[i] = (coords[i] * 2.0f) - 1.0f;
        }
    }

    public void setTargetTransform(float[] matrix) {
        if (matrix.length != 16) {
            throw new IllegalArgumentException("Expected 4x4 matrix for target transform!");
        }
        setTargetCoords(new float[]{matrix[12], matrix[13], matrix[0] + matrix[12], matrix[1] + matrix[13], matrix[4] + matrix[12], matrix[5] + matrix[13], (matrix[0] + matrix[4]) + matrix[12], (matrix[1] + matrix[5]) + matrix[13]});
    }

    public void setClearsOutput(boolean clears) {
        this.mClearsOutput = clears;
    }

    public boolean getClearsOutput() {
        return this.mClearsOutput;
    }

    public void setClearColor(float[] rgba) {
        this.mClearColor = rgba;
    }

    public float[] getClearColor() {
        return this.mClearColor;
    }

    public void setClearBufferMask(int bufferMask) {
        this.mClearBuffers = bufferMask;
    }

    public int getClearBufferMask() {
        return this.mClearBuffers;
    }

    public void setBlendEnabled(boolean enable) {
        this.mBlendEnabled = enable;
    }

    public boolean getBlendEnabled() {
        return this.mBlendEnabled;
    }

    public void setBlendFunc(int sFactor, int dFactor) {
        this.mSFactor = sFactor;
        this.mDFactor = dFactor;
    }

    public void setDrawMode(int drawMode) {
        this.mDrawMode = drawMode;
    }

    public int getDrawMode() {
        return this.mDrawMode;
    }

    public void setVertexCount(int count) {
        this.mVertexCount = count;
    }

    public int getVertexCount() {
        return this.mVertexCount;
    }

    public void setBaseTextureUnit(int baseTexUnit) {
        this.mBaseTexUnit = baseTexUnit;
    }

    public int baseTextureUnit() {
        return this.mBaseTexUnit;
    }

    public String texCoordAttributeName() {
        return "a_texcoord";
    }

    public String positionAttributeName() {
        return "a_position";
    }

    public String inputTextureUniformName(int index) {
        return "tex_sampler_" + index;
    }

    public static int maxTextureUnits() {
        return 35661;
    }

    protected void finalize() throws Throwable {
        GLES20.glDeleteProgram(this.mProgram);
    }

    protected void pushShaderState() {
        useProgram();
        updateSourceCoordAttribute();
        updateTargetCoordAttribute();
        pushAttributes();
        if (this.mClearsOutput) {
            GLES20.glClearColor(this.mClearColor[0], this.mClearColor[1], this.mClearColor[2], this.mClearColor[3]);
            GLES20.glClear(this.mClearBuffers);
        }
        if (this.mBlendEnabled) {
            GLES20.glEnable(3042);
            GLES20.glBlendFunc(this.mSFactor, this.mDFactor);
        } else {
            GLES20.glDisable(3042);
        }
        GLToolbox.checkGlError("Set render variables");
    }

    private void focusTarget(RenderTarget target, int width, int height) {
        target.focus();
        GLES20.glViewport(0, 0, width, height);
        GLToolbox.checkGlError("glViewport");
    }

    private void bindInputTextures(TextureSource[] sources) {
        int i = 0;
        while (i < sources.length) {
            GLES20.glActiveTexture(baseTextureUnit() + i);
            sources[i].bind();
            int texUniform = GLES20.glGetUniformLocation(this.mProgram, inputTextureUniformName(i));
            if (texUniform >= 0) {
                GLES20.glUniform1i(texUniform, i);
                GLToolbox.checkGlError("Binding input texture " + i);
                i++;
            } else {
                int length = sources.length;
                String inputTextureUniformName = inputTextureUniformName(i);
                throw new RuntimeException(new StringBuilder(String.valueOf(inputTextureUniformName).length() + 87).append("Shader does not seem to support ").append(length).append(" number of input textures! Missing uniform ").append(inputTextureUniformName).append("!").toString());
            }
        }
    }

    private void pushAttributes() {
        for (VertexAttribute attr : this.mAttributes.values()) {
            if (!attr.push()) {
                String valueOf = String.valueOf(attr);
                throw new RuntimeException(new StringBuilder(String.valueOf(valueOf).length() + 36).append("Unable to assign attribute value '").append(valueOf).append("'!").toString());
            }
        }
        GLToolbox.checkGlError("Push Attributes");
    }

    private void updateSourceCoordAttribute() {
        VertexAttribute attr = getProgramAttribute(texCoordAttributeName(), false);
        if (!(this.mSourceCoords == null || attr == null)) {
            attr.set(false, 8, 2, 5126, this.mSourceCoords);
        }
        this.mSourceCoords = null;
    }

    private void updateTargetCoordAttribute() {
        VertexAttribute attr = getProgramAttribute(positionAttributeName(), false);
        if (!(this.mTargetCoords == null || attr == null)) {
            attr.set(false, 8, 2, 5126, this.mTargetCoords);
        }
        this.mTargetCoords = null;
    }

    private void render() {
        Trace.beginSection("glDrawArrays");
        GLES20.glDrawArrays(this.mDrawMode, 0, this.mVertexCount);
        GLToolbox.checkGlError("glDrawArrays");
        Trace.endSection();
    }

    private void checkExecutable() {
        if (this.mProgram == 0) {
            throw new RuntimeException("Attempting to execute invalid shader-program!");
        }
    }

    private void useProgram() {
        GLES20.glUseProgram(this.mProgram);
        GLToolbox.checkGlError("glUseProgram");
    }

    private static void checkTexCount(int count) {
        if (count > maxTextureUnits()) {
            throw new RuntimeException("Number of textures passed (" + count + ") exceeds the maximum number of allowed texture units (" + maxTextureUnits() + ")!");
        }
    }

    private static int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        if (shader != 0) {
            GLES20.glShaderSource(shader, source);
            GLES20.glCompileShader(shader);
            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, 35713, compiled, 0);
            if (compiled[0] == 0) {
                String info = GLES20.glGetShaderInfoLog(shader);
                GLES20.glDeleteShader(shader);
                throw new RuntimeException(new StringBuilder(String.valueOf(info).length() + 37).append("Could not compile shader ").append(shaderType).append(":").append(info).toString());
            }
        }
        return shader;
    }

    private static int createProgram(String vertexSource, String fragmentSource) {
        int vertexShader = loadShader(35633, vertexSource);
        if (vertexShader == 0) {
            throw new RuntimeException("Could not create shader-program as vertex shader could not be compiled!");
        }
        int pixelShader = loadShader(35632, fragmentSource);
        if (pixelShader == 0) {
            throw new RuntimeException("Could not create shader-program as fragment shader could not be compiled!");
        }
        int program = GLES20.glCreateProgram();
        if (program != 0) {
            GLES20.glAttachShader(program, vertexShader);
            GLToolbox.checkGlError("glAttachShader");
            GLES20.glAttachShader(program, pixelShader);
            GLToolbox.checkGlError("glAttachShader");
            GLES20.glLinkProgram(program);
            int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(program, 35714, linkStatus, 0);
            if (linkStatus[0] != 1) {
                String info = GLES20.glGetProgramInfoLog(program);
                GLES20.glDeleteProgram(program);
                String str = "Could not link program: ";
                String valueOf = String.valueOf(info);
                throw new RuntimeException(valueOf.length() != 0 ? str.concat(valueOf) : new String(str));
            }
        }
        GLES20.glDeleteShader(vertexShader);
        GLES20.glDeleteShader(pixelShader);
        return program;
    }

    private void scanUniforms() {
        int[] uniformCount = new int[1];
        GLES20.glGetProgramiv(this.mProgram, 35718, uniformCount, 0);
        if (uniformCount[0] > 0) {
            this.mUniforms = new HashMap(uniformCount[0]);
            for (int i = 0; i < uniformCount[0]; i++) {
                ProgramUniform uniform = new ProgramUniform(this.mProgram, i);
                this.mUniforms.put(uniform.getName(), uniform);
            }
        }
    }

    private ProgramUniform getProgramUniform(String name, boolean required) {
        ProgramUniform result = (ProgramUniform) this.mUniforms.get(name);
        if (result != null || !required) {
            return result;
        }
        throw new IllegalArgumentException(new StringBuilder(String.valueOf(name).length() + 19).append("Unknown uniform '").append(name).append("'!").toString());
    }

    private VertexAttribute getProgramAttribute(String name, boolean required) {
        VertexAttribute result = (VertexAttribute) this.mAttributes.get(name);
        if (result != null) {
            return result;
        }
        int handle = GLES20.glGetAttribLocation(this.mProgram, name);
        if (handle >= 0) {
            result = new VertexAttribute(name, handle);
            this.mAttributes.put(name, result);
            return result;
        } else if (!required) {
            return result;
        } else {
            throw new IllegalArgumentException(new StringBuilder(String.valueOf(name).length() + 21).append("Unknown attribute '").append(name).append("'!").toString());
        }
    }

    private void checkUniformAssignment(ProgramUniform uniform, int values, int components) {
        String name;
        if (values % components != 0) {
            name = uniform.getName();
            throw new RuntimeException(new StringBuilder(String.valueOf(name).length() + 111).append("Size mismatch: Attempting to assign values of size ").append(values).append(" to uniform '").append(name).append("' (must be multiple of ").append(components).append(")!").toString());
        } else if (uniform.getSize() != values / components) {
            name = uniform.getName();
            throw new RuntimeException(new StringBuilder(String.valueOf(name).length() + 62).append("Size mismatch: Cannot assign ").append(values).append(" values to uniform '").append(name).append("'!").toString());
        }
    }

    private static int strlen(byte[] strVal) {
        for (int i = 0; i < strVal.length; i++) {
            if (strVal[i] == (byte) 0) {
                return i;
            }
        }
        return strVal.length;
    }
}
