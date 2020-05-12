package androidx.media.filterfw;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.os.Looper;
import androidx.media.util.Trace;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class GLToolbox {
    public static int textureNone() {
        return 0;
    }

    public static boolean isTexture(int texId) {
        return GLES20.glIsTexture(texId);
    }

    public static void deleteTexture(int texId) {
        int[] textures = new int[]{texId};
        assertNonUiThread("glDeleteTextures");
        GLES20.glDeleteTextures(1, textures, 0);
        checkGlError("glDeleteTextures");
    }

    public static void deleteFbo(int fboId) {
        int[] fbos = new int[]{fboId};
        assertNonUiThread("glDeleteFramebuffers");
        GLES20.glDeleteFramebuffers(1, fbos, 0);
        checkGlError("glDeleteFramebuffers");
    }

    public static int generateTexture() {
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        checkGlError("glGenTextures");
        return textures[0];
    }

    public static int generateFbo() {
        int[] fbos = new int[1];
        GLES20.glGenFramebuffers(1, fbos, 0);
        checkGlError("glGenFramebuffers");
        return fbos[0];
    }

    public static void readFbo(int fboId, ByteBuffer pixels, int width, int height) {
        Trace.beginSection("readFbo");
        GLES20.glBindFramebuffer(36160, fboId);
        GLES20.glReadPixels(0, 0, width, height, 6408, 5121, pixels);
        checkGlError("glReadPixels");
        Trace.endSection();
    }

    public static void readTarget(RenderTarget target, ByteBuffer pixels, int width, int height) {
        Trace.beginSection("readTarget");
        target.focus();
        GLES20.glReadPixels(0, 0, width, height, 6408, 5121, pixels);
        checkGlError("glReadPixels");
        Trace.endSection();
    }

    public static int attachedTexture(int fboId) {
        int[] params = new int[1];
        GLES20.glGetFramebufferAttachmentParameteriv(36160, 36064, 36049, params, 0);
        checkGlError("glGetFramebufferAttachmentParameteriv");
        return params[0];
    }

    public static void attachTextureToFbo(int texId, int fboId) {
        GLES20.glBindFramebuffer(36160, fboId);
        GLES20.glFramebufferTexture2D(36160, 36064, 3553, texId, 0);
        checkGlError("glFramebufferTexture2D");
    }

    public static void allocateTexturePixels(int texId, int target, int width, int height) {
        setTexturePixels(texId, target, (ByteBuffer) null, width, height);
    }

    public static void setTexturePixels(int texId, int target, Bitmap bitmap) {
        GLES20.glBindTexture(target, texId);
        GLUtils.texImage2D(target, 0, bitmap, 0);
        checkGlError("glTexImage2D");
        setDefaultTexParams();
    }

    public static void setTexturePixels(int texId, int target, ByteBuffer pixels, int width, int height) {
        GLES20.glBindTexture(target, texId);
        GLES20.glTexImage2D(target, 0, 6408, width, height, 0, 6408, 5121, pixels);
        checkGlError("glTexImage2D");
        setDefaultTexParams();
    }

    public static void setDefaultTexParams() {
        GLES20.glTexParameteri(3553, 10240, 9729);
        GLES20.glTexParameteri(3553, 10241, 9729);
        GLES20.glTexParameteri(3553, 10242, 33071);
        GLES20.glTexParameteri(3553, 10243, 33071);
        checkGlError("glTexParameteri");
    }

    public static int vboNone() {
        return 0;
    }

    public static int generateVbo() {
        int[] vbos = new int[1];
        GLES20.glGenBuffers(1, vbos, 0);
        checkGlError("glGenBuffers");
        return vbos[0];
    }

    public static void setVboData(int vboId, ByteBuffer data) {
        GLES20.glBindBuffer(34962, vboId);
        GLES20.glBufferData(34962, data.remaining(), data, 35044);
        checkGlError("glBufferData");
    }

    public static void setVboFloats(int vboId, float[] values) {
        setVboData(vboId, ByteBuffer.allocateDirect(values.length * 4).order(ByteOrder.nativeOrder()));
    }

    public static boolean isVbo(int vboId) {
        return GLES20.glIsBuffer(vboId);
    }

    public static void deleteVbo(int vboId) {
        GLES20.glDeleteBuffers(1, new int[]{vboId}, 0);
        checkGlError("glDeleteBuffers");
    }

    public static void checkGlError(String operation) {
        int error = GLES20.glGetError();
        if (error != 0) {
            String toHexString = Integer.toHexString(error);
            throw new RuntimeException(new StringBuilder((String.valueOf(operation).length() + 30) + String.valueOf(toHexString).length()).append("GL Operation '").append(operation).append("' caused error ").append(toHexString).append("!").toString());
        }
    }

    private static void assertNonUiThread(String operation) {
        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            throw new RuntimeException(new StringBuilder(String.valueOf(operation).length() + 51).append("Attempting to perform GL operation '").append(operation).append("' on UI thread!").toString());
        }
    }
}
