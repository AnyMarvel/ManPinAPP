package androidx.media.filterfw;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.Build.VERSION;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.Type.Builder;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;

public class BackingStore {
    static final int ACCESS_ALLOCATION = 32;
    static final int ACCESS_BITMAP = 16;
    static final int ACCESS_BYTES = 1;
    static final int ACCESS_NONE = 0;
    static final int ACCESS_OBJECT = 8;
    static final int ACCESS_RENDERTARGET = 4;
    static final int ACCESS_TEXTURE = 2;
    private static final int BACKING_ALLOCATION = 5;
    private static final int BACKING_BITMAP = 4;
    private static final int BACKING_BYTEBUFFER = 1;
    private static final int BACKING_OBJECT = 3;
    private static final int BACKING_TEXTURE = 2;
    private Vector<Backing> mBackings = new Vector();
    private Backing mCurrentBacking = null;
    private int[] mDimensions;
    private final FrameManager mFrameManager;
    private Backing mLockedBacking = null;
    private int mReadLocks = 0;
    private int mRefCount = 1;
    private long mTimestamp = -1;
    private final FrameType mType;
    private boolean mWriteLocked = false;

    public static abstract class Backing {
        int cachePriority = 0;
        protected int[] mDimensions = null;
        private int mElementID;
        private int mElementSize;
        protected boolean mIsDirty = false;

        public abstract void allocate(FrameType frameType);

        public abstract void destroy();

        public abstract int getSize();

        public abstract int getType();

        public abstract Object lock(int i);

        public abstract int readAccess();

        public abstract boolean requiresGpu();

        public abstract boolean shouldCache();

        public abstract void syncTo(Backing backing);

        public abstract int writeAccess();

        Backing() {
        }

        public void unlock() {
        }

        public void setData(Object data) {
            String valueOf = String.valueOf(this);
            throw new RuntimeException(new StringBuilder(String.valueOf(valueOf).length() + 93).append("Internal error: Setting data on frame backing ").append(valueOf).append(", which does not support setting data directly!").toString());
        }

        public void setDimensions(int[] dimensions) {
            this.mDimensions = dimensions;
        }

        public void setElementSize(int elemSize) {
            this.mElementSize = elemSize;
        }

        public void setElementId(int elemId) {
            this.mElementID = elemId;
        }

        public int[] getDimensions() {
            return this.mDimensions;
        }

        public int getElementSize() {
            return this.mElementSize;
        }

        public int getElementId() {
            return this.mElementID;
        }

        public boolean resize(int[] newDimensions) {
            return false;
        }

        public void markDirty() {
            this.mIsDirty = true;
        }

        public boolean isDirty() {
            return this.mIsDirty;
        }

        protected void assertImageCompatible(FrameType type) {
            if (type.getElementId() != FrameType.ELEMENT_RGBA8888) {
                throw new RuntimeException("Cannot allocate texture with non-RGBA data type!");
            } else if (this.mDimensions == null || this.mDimensions.length != 2) {
                throw new RuntimeException("Cannot allocate non 2-dimensional texture!");
            }
        }
    }

    @TargetApi(11)
    static class AllocationBacking extends Backing {
        private Allocation mAllocation = null;
        private final RenderScript mRenderScript;

        public AllocationBacking(RenderScript renderScript) {
            this.mRenderScript = renderScript;
        }

        public void allocate(FrameType frameType) {
            int i;
            int i2 = 1;
            assertCompatible(frameType);
            Element element = null;
            switch (frameType.getElementId()) {
                case 200:
                    element = Element.F32(this.mRenderScript);
                    break;
                case FrameType.ELEMENT_RGBA8888 /*301*/:
                    element = Element.RGBA_8888(this.mRenderScript);
                    break;
            }
            Builder imageTypeBuilder = new Builder(this.mRenderScript, element);
            if (this.mDimensions.length >= 1) {
                i = this.mDimensions[0];
            } else {
                i = 1;
            }
            imageTypeBuilder.setX(i);
            if (this.mDimensions.length == 2) {
                i2 = this.mDimensions[1];
            }
            imageTypeBuilder.setY(i2);
            this.mAllocation = Allocation.createTyped(this.mRenderScript, imageTypeBuilder.create());
        }

        public int readAccess() {
            return 32;
        }

        public int writeAccess() {
            return 32;
        }

        public boolean requiresGpu() {
            return false;
        }

        public void syncTo(Backing backing) {
            int access = backing.readAccess();
            if ((access & 2) != 0) {
                RenderTarget target = (RenderTarget) backing.lock(4);
                ByteBuffer pixels = ByteBuffer.allocateDirect(getSize());
                GLToolbox.readTarget(target, pixels, this.mDimensions[0], this.mDimensions[1]);
                this.mAllocation.copyFrom(pixels.array());
            } else if ((access & 16) != 0) {
                this.mAllocation.copyFrom((Bitmap) backing.lock(16));
            } else if ((access & 1) != 0) {
                ByteBuffer buffer = (ByteBuffer) backing.lock(1);
                if (buffer.order() != ByteOrder.nativeOrder()) {
                    throw new RuntimeException("Trying to sync to the ByteBufferBacking with non-native byte order!");
                }
                byte[] bytes;
                if (buffer.hasArray()) {
                    bytes = buffer.array();
                } else {
                    bytes = new byte[getSize()];
                    buffer.get(bytes);
                    buffer.rewind();
                }
                this.mAllocation.copyFromUnchecked(bytes);
            } else {
                throw new RuntimeException("Cannot sync allocation backing!");
            }
            backing.unlock();
            this.mIsDirty = false;
        }

        public Object lock(int accessType) {
            return this.mAllocation;
        }

        public void unlock() {
        }

        public int getType() {
            return 5;
        }

        public boolean shouldCache() {
            return true;
        }

        public void destroy() {
            if (this.mAllocation != null) {
                this.mAllocation.destroy();
                this.mAllocation = null;
            }
        }

        public int getSize() {
            int elementCount = 1;
            for (int dim : this.mDimensions) {
                elementCount *= dim;
            }
            return getElementSize() * elementCount;
        }

        public static boolean isSupported() {
            return VERSION.SDK_INT >= 11;
        }

        private void assertCompatible(FrameType type) {
            if (type.getElementId() != FrameType.ELEMENT_RGBA8888 && type.getElementId() != 200) {
                throw new RuntimeException("Cannot allocate allocation with a non-RGBA or non-float data type!");
            } else if (this.mDimensions == null || this.mDimensions.length > 2) {
                throw new RuntimeException("Cannot create an allocation with more than 2 dimensions!");
            }
        }
    }

    static class BitmapBacking extends Backing {
        private Bitmap mBitmap = null;

        BitmapBacking() {
        }

        public void allocate(FrameType frameType) {
            assertImageCompatible(frameType);
        }

        public int readAccess() {
            return 16;
        }

        public int writeAccess() {
            return 16;
        }

        public void syncTo(Backing backing) {
            int access = backing.readAccess();
            if ((access & 16) != 0) {
                this.mBitmap = (Bitmap) backing.lock(16);
            } else if ((access & 1) != 0) {
                createBitmap();
                ByteBuffer buffer = (ByteBuffer) backing.lock(1);
                this.mBitmap.copyPixelsFromBuffer(buffer);
                buffer.rewind();
            } else if ((access & 2) != 0) {
                createBitmap();
                this.mBitmap.copyPixelsFromBuffer(((RenderTarget) backing.lock(4)).getPixelData(this.mDimensions[0], this.mDimensions[1]));
            } else if ((access & 32) == 0 || !AllocationBacking.isSupported()) {
                throw new RuntimeException("Cannot sync bytebuffer backing!");
            } else {
                createBitmap();
                syncToAllocationBacking(backing);
            }
            backing.unlock();
            this.mIsDirty = false;
        }

        @TargetApi(11)
        private void syncToAllocationBacking(Backing backing) {
            ((Allocation) backing.lock(32)).copyTo(this.mBitmap);
        }

        public Object lock(int accessType) {
            return this.mBitmap;
        }

        public int getType() {
            return 4;
        }

        public boolean shouldCache() {
            return false;
        }

        public boolean requiresGpu() {
            return false;
        }

        public void destroy() {
            this.mBitmap = null;
        }

        public int getSize() {
            return (this.mDimensions[0] * 4) * this.mDimensions[1];
        }

        public void setData(Object data) {
            this.mBitmap = (Bitmap) data;
        }

        private void createBitmap() {
            this.mBitmap = Bitmap.createBitmap(this.mDimensions[0], this.mDimensions[1], Config.ARGB_8888);
        }
    }

    static class ByteBufferBacking extends Backing {
        ByteBuffer mBuffer = null;

        ByteBufferBacking() {
        }

        public void allocate(FrameType frameType) {
            int size = frameType.getElementSize();
            for (int dim : this.mDimensions) {
                size *= dim;
            }
            this.mBuffer = ByteBuffer.allocateDirect(size);
        }

        public int readAccess() {
            return 1;
        }

        public int writeAccess() {
            return 1;
        }

        public boolean requiresGpu() {
            return false;
        }

        public void syncTo(Backing backing) {
            int access = backing.readAccess();
            if ((access & 2) != 0) {
                GLToolbox.readTarget((RenderTarget) backing.lock(4), this.mBuffer, this.mDimensions[0], this.mDimensions[1]);
            } else if ((access & 16) != 0) {
                ((Bitmap) backing.lock(16)).copyPixelsToBuffer(this.mBuffer);
                this.mBuffer.rewind();
            } else if ((access & 1) != 0) {
                ByteBuffer otherBuffer = (ByteBuffer) backing.lock(1);
                this.mBuffer.put(otherBuffer);
                otherBuffer.rewind();
            } else if ((access & 32) == 0 || !AllocationBacking.isSupported()) {
                throw new RuntimeException("Cannot sync bytebuffer backing!");
            } else {
                syncToAllocationBacking(backing);
            }
            backing.unlock();
            this.mBuffer.rewind();
            this.mIsDirty = false;
        }

        @TargetApi(11)
        private void syncToAllocationBacking(Backing backing) {
            Allocation allocation = (Allocation) backing.lock(32);
            if (getElementId() == FrameType.ELEMENT_RGBA8888) {
                allocation.copyTo(this.mBuffer.array());
            } else if (getElementId() == 200) {
                float[] floats = new float[(getSize() / 4)];
                allocation.copyTo(floats);
                this.mBuffer.asFloatBuffer().put(floats);
            } else {
                throw new RuntimeException("Trying to sync to an allocation with an unsupported element id: " + getElementId());
            }
        }

        public Object lock(int accessType) {
            return this.mBuffer.rewind();
        }

        public void unlock() {
            this.mBuffer.rewind();
        }

        public int getType() {
            return 1;
        }

        public boolean shouldCache() {
            return true;
        }

        public void destroy() {
            this.mBuffer = null;
        }

        public int getSize() {
            return this.mBuffer == null ? 0 : this.mBuffer.remaining();
        }
    }

    static class ObjectBacking extends Backing {
        private Object mObject = null;

        ObjectBacking() {
        }

        public void allocate(FrameType frameType) {
            this.mObject = null;
        }

        public int readAccess() {
            return 8;
        }

        public int writeAccess() {
            return 8;
        }

        public void syncTo(Backing backing) {
            switch (backing.getType()) {
                case 3:
                    this.mObject = backing.lock(8);
                    backing.unlock();
                    break;
                case 4:
                    this.mObject = backing.lock(16);
                    backing.unlock();
                    break;
                default:
                    this.mObject = null;
                    break;
            }
            this.mIsDirty = false;
        }

        public Object lock(int accessType) {
            return this.mObject;
        }

        public int getType() {
            return 3;
        }

        public boolean shouldCache() {
            return false;
        }

        public boolean requiresGpu() {
            return false;
        }

        public void destroy() {
            this.mObject = null;
        }

        public int getSize() {
            return 0;
        }

        public void setData(Object data) {
            this.mObject = data;
        }
    }

    static class TextureBacking extends Backing {
        private RenderTarget mRenderTarget = null;
        private TextureSource mTexture = null;

        TextureBacking() {
        }

        public void allocate(FrameType frameType) {
            assertImageCompatible(frameType);
            this.mTexture = TextureSource.newTexture();
        }

        public int readAccess() {
            return 2;
        }

        public int writeAccess() {
            return 4;
        }

        public void syncTo(Backing backing) {
            int access = backing.readAccess();
            if ((access & 1) != 0) {
                this.mTexture.allocateWithPixels((ByteBuffer) backing.lock(1), this.mDimensions[0], this.mDimensions[1]);
            } else if ((access & 16) != 0) {
                this.mTexture.allocateWithBitmapPixels((Bitmap) backing.lock(16));
            } else if ((access & 2) != 0) {
                ImageShader.renderTextureToTarget((TextureSource) backing.lock(2), getRenderTarget(), this.mDimensions[0], this.mDimensions[1]);
            } else if ((access & 32) == 0 || !AllocationBacking.isSupported()) {
                throw new RuntimeException("Cannot sync bytebuffer backing!");
            } else {
                syncToAllocationBacking(backing);
            }
            backing.unlock();
            this.mIsDirty = false;
        }

        @TargetApi(11)
        private void syncToAllocationBacking(Backing backing) {
            Allocation allocation = (Allocation) backing.lock(32);
            ByteBuffer pixels = ByteBuffer.allocateDirect(getSize());
            allocation.copyTo(pixels.array());
            this.mTexture.allocateWithPixels(pixels, this.mDimensions[0], this.mDimensions[1]);
        }

        public Object lock(int accessType) {
            switch (accessType) {
                case 2:
                    return getTexture();
                case 4:
                    return getRenderTarget();
                default:
                    throw new RuntimeException("Illegal access to texture!");
            }
        }

        public int getType() {
            return 2;
        }

        public boolean shouldCache() {
            return true;
        }

        public boolean requiresGpu() {
            return true;
        }

        public void destroy() {
            if (this.mRenderTarget != null) {
                this.mRenderTarget.release();
            }
            if (this.mTexture.isAllocated()) {
                this.mTexture.release();
            }
        }

        public int getSize() {
            return (this.mDimensions[0] * 4) * this.mDimensions[1];
        }

        private TextureSource getTexture() {
            if (!this.mTexture.isAllocated()) {
                this.mTexture.allocate(this.mDimensions[0], this.mDimensions[1]);
            }
            return this.mTexture;
        }

        private RenderTarget getRenderTarget() {
            if (this.mRenderTarget == null) {
                this.mRenderTarget = RenderTarget.forTexture(getTexture(), this.mDimensions[0], this.mDimensions[1]);
            }
            return this.mRenderTarget;
        }
    }

    public BackingStore(FrameType type, int[] dimensions, FrameManager frameManager) {
        int[] iArr = null;
        this.mType = type;
        if (dimensions != null) {
            iArr = Arrays.copyOf(dimensions, dimensions.length);
        }
        this.mDimensions = iArr;
        this.mFrameManager = frameManager;
    }

    public FrameType getFrameType() {
        return this.mType;
    }

    public Object lockData(int mode, int accessFormat) {
        return lockBacking(mode, accessFormat).lock(accessFormat);
    }

    public Backing lockBacking(int mode, int access) {
        Backing backing = fetchBacking(mode, access);
        if (backing == null) {
            throw new RuntimeException("Could not fetch frame data!");
        }
        lock(backing, mode);
        return backing;
    }

    public boolean unlock() {
        if (this.mWriteLocked) {
            this.mWriteLocked = false;
        } else if (this.mReadLocks <= 0) {
            return false;
        } else {
            this.mReadLocks--;
        }
        this.mLockedBacking.unlock();
        this.mLockedBacking = null;
        return true;
    }

    public BackingStore retain() {
        if (this.mRefCount <= 0) {
            throw new RuntimeException("RETAINING RELEASED");
        }
        this.mRefCount++;
        return this;
    }

    public BackingStore release() {
        if (this.mRefCount <= 0) {
            throw new RuntimeException("DOUBLE-RELEASE");
        }
        this.mRefCount--;
        if (this.mRefCount != 0) {
            return this;
        }
        releaseBackings();
        return null;
    }

    public void resize(int[] newDimensions) {
        Vector<Backing> resized = new Vector();
        Iterator it = this.mBackings.iterator();
        while (it.hasNext()) {
            Backing backing = (Backing) it.next();
            if (backing.resize(newDimensions)) {
                resized.add(backing);
            } else {
                releaseBacking(backing);
            }
        }
        this.mBackings = resized;
        this.mDimensions = newDimensions;
    }

    public int[] getDimensions() {
        return this.mDimensions;
    }

    public int getElementCount() {
        int result = 1;
        if (this.mDimensions != null) {
            for (int dim : this.mDimensions) {
                result *= dim;
            }
        }
        return result;
    }

    public void importStore(BackingStore store) {
        if (store.mBackings.size() > 0) {
            importBacking((Backing) store.mBackings.firstElement());
        }
        this.mTimestamp = store.mTimestamp;
    }

    public long getTimestamp() {
        return this.mTimestamp;
    }

    public void setTimestamp(long timestamp) {
        this.mTimestamp = timestamp;
    }

    private Backing fetchBacking(int mode, int access) {
        Backing backing = getBacking(mode, access);
        if (backing == null) {
            backing = attachNewBacking(mode, access);
        }
        syncBacking(backing);
        return backing;
    }

    private void syncBacking(Backing backing) {
        if (backing != null && backing.isDirty() && this.mCurrentBacking != null) {
            backing.syncTo(this.mCurrentBacking);
        }
    }

    private Backing getBacking(int mode, int access) {
        for (int i = 0; i < this.mBackings.size(); i++) {
            Backing backing = (Backing) this.mBackings.get(i);
            if (((mode == 2 ? backing.writeAccess() : backing.readAccess()) & access) == access) {
                return backing;
            }
        }
        return null;
    }

    private Backing attachNewBacking(int mode, int access) {
        Backing backing = createBacking(mode, access);
        if (this.mBackings.size() > 0) {
            backing.markDirty();
        }
        this.mBackings.add(backing);
        return backing;
    }

    private Backing createBacking(int mode, int access) {
        Backing backing = null;
        int elemSize = this.mType.getElementSize();
        if (shouldFetchCached(access)) {
            backing = this.mFrameManager.fetchBacking(mode, access, this.mDimensions, elemSize);
        }
        if (backing == null) {
            switch (access) {
                case 1:
                    backing = new ByteBufferBacking();
                    break;
                case 2:
                case 4:
                    backing = new TextureBacking();
                    break;
                case 8:
                    backing = new ObjectBacking();
                    break;
                case 16:
                    backing = new BitmapBacking();
                    break;
                case 32:
                    if (AllocationBacking.isSupported()) {
                        backing = new AllocationBacking(this.mFrameManager.getContext().getRenderScript());
                        break;
                    }
                    throw new RuntimeException("Attempted to create an AllocationBacking in context that does not support RenderScript!");
            }
            if (backing == null) {
                throw new RuntimeException("Could not create backing for access type " + access + "!");
            } else if (!backing.requiresGpu() || this.mFrameManager.getRunner().isOpenGLSupported()) {
                backing.setDimensions(this.mDimensions);
                backing.setElementSize(elemSize);
                backing.setElementId(this.mType.getElementId());
                backing.allocate(this.mType);
                this.mFrameManager.onBackingCreated(backing);
            } else {
                throw new RuntimeException("Cannot create backing that requires GPU in a runner that does not support OpenGL!");
            }
        }
        return backing;
    }

    private void importBacking(Backing backing) {
        Backing newBacking = createBacking(1, backing.requiresGpu() ? 1 : backing.readAccess());
        newBacking.syncTo(backing);
        this.mBackings.add(newBacking);
        this.mCurrentBacking = newBacking;
    }

    private void releaseBackings() {
        for (int i = 0; i < this.mBackings.size(); i++) {
            releaseBacking((Backing) this.mBackings.get(i));
        }
        this.mBackings.clear();
        this.mCurrentBacking = null;
    }

    private void releaseBacking(Backing backing) {
        this.mFrameManager.onBackingAvailable(backing);
    }

    private void lock(Backing backingToLock, int mode) {
        String valueOf;
        if (mode == 2) {
            if (this.mReadLocks > 0) {
                valueOf = String.valueOf(this);
                throw new RuntimeException(new StringBuilder(String.valueOf(valueOf).length() + 48).append("Attempting to write-lock the read-locked frame ").append(valueOf).append("!").toString());
            } else if (this.mWriteLocked) {
                valueOf = String.valueOf(this);
                throw new RuntimeException(new StringBuilder(String.valueOf(valueOf).length() + 49).append("Attempting to write-lock the write-locked frame ").append(valueOf).append("!").toString());
            } else {
                for (int i = 0; i < this.mBackings.size(); i++) {
                    Backing backing = (Backing) this.mBackings.get(i);
                    if (backing != backingToLock) {
                        backing.markDirty();
                    }
                }
                this.mWriteLocked = true;
                this.mCurrentBacking = backingToLock;
            }
        } else if (this.mWriteLocked) {
            valueOf = String.valueOf(this);
            throw new RuntimeException(new StringBuilder(String.valueOf(valueOf).length() + 38).append("Attempting to read-lock locked frame ").append(valueOf).append("!").toString());
        } else {
            this.mReadLocks++;
        }
        this.mLockedBacking = backingToLock;
    }

    private static boolean shouldFetchCached(int access) {
        return access != 8;
    }
}
