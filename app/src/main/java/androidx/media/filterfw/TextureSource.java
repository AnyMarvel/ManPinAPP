package androidx.media.filterfw;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import java.nio.ByteBuffer;

public class TextureSource {
    private boolean mIsAllocated = false;
    private boolean mIsOwner;
    private int mTarget;
    private int mTexId;

    public static TextureSource fromTexture(int texId, int target) {
        return new TextureSource(texId, target, false);
    }

    public static TextureSource fromTexture(int texId) {
        return new TextureSource(texId, 3553, false);
    }

    public static TextureSource newTexture() {
        return new TextureSource(GLToolbox.generateTexture(), 3553, true);
    }

    public static TextureSource newExternalTexture() {
        return new TextureSource(GLToolbox.generateTexture(), 36197, true);
    }

    public int getTextureId() {
        return this.mTexId;
    }

    public int getTarget() {
        return this.mTarget;
    }

    public void bind() {
        GLES20.glBindTexture(this.mTarget, this.mTexId);
        GLToolbox.checkGlError("glBindTexture");
    }

    public void allocate(int width, int height) {
        GLToolbox.allocateTexturePixels(this.mTexId, this.mTarget, width, height);
        this.mIsAllocated = true;
    }

    public void allocateWithPixels(ByteBuffer pixels, int width, int height) {
        GLToolbox.setTexturePixels(this.mTexId, this.mTarget, pixels, width, height);
        this.mIsAllocated = true;
    }

    public void allocateWithBitmapPixels(Bitmap bitmap) {
        GLToolbox.setTexturePixels(this.mTexId, this.mTarget, bitmap);
        this.mIsAllocated = true;
    }

    public void generateMipmaps() {
        GLES20.glBindTexture(this.mTarget, this.mTexId);
        GLES20.glTexParameteri(this.mTarget, 10241, 9987);
        GLES20.glGenerateMipmap(this.mTarget);
        GLES20.glBindTexture(this.mTarget, 0);
    }

    public void setParameter(int parameter, int value) {
        GLES20.glBindTexture(this.mTarget, this.mTexId);
        GLES20.glTexParameteri(this.mTarget, parameter, value);
        GLES20.glBindTexture(this.mTarget, 0);
    }

    public void setDefaultParams() {
        GLES20.glBindTexture(this.mTarget, this.mTexId);
        GLToolbox.setDefaultTexParams();
        GLES20.glBindTexture(this.mTarget, 0);
    }

    public void release() {
        if (GLToolbox.isTexture(this.mTexId) && this.mIsOwner) {
            GLToolbox.deleteTexture(this.mTexId);
        }
        this.mTexId = GLToolbox.textureNone();
    }

    public String toString() {
        int i = this.mTexId;
        return "TextureSource(id=" + i + ", target=" + this.mTarget + ")";
    }

    boolean isAllocated() {
        return this.mIsAllocated;
    }

    private TextureSource(int texId, int target, boolean isOwner) {
        this.mTexId = texId;
        this.mTarget = target;
        this.mIsOwner = isOwner;
    }
}
