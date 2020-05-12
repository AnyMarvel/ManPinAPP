package androidx.media.filterfw;

import android.annotation.TargetApi;
import android.renderscript.Allocation;
import java.nio.ByteBuffer;

public class FrameBuffer1D extends Frame {
    private int mLength = 0;

    public ByteBuffer lockBytes(int mode) {
        assertAccessible(mode);
        return (ByteBuffer) this.mBackingStore.lockData(mode, 1);
    }

    @TargetApi(11)
    public Allocation lockAllocation(int mode) {
        assertAccessible(mode);
        return (Allocation) this.mBackingStore.lockData(mode, 32);
    }

    public int getLength() {
        return this.mLength;
    }

    public int[] getDimensions() {
        return super.getDimensions();
    }

    public void resize(int[] newDimensions) {
        super.resize(newDimensions);
    }

    static FrameBuffer1D create(BackingStore backingStore) {
        assertCanCreate(backingStore);
        return new FrameBuffer1D(backingStore);
    }

    FrameBuffer1D(BackingStore backingStore) {
        super(backingStore);
        updateLength(backingStore.getDimensions());
    }

    static void assertCanCreate(BackingStore backingStore) {
        FrameType type = backingStore.getFrameType();
        if (type.getElementSize() == 0) {
            String valueOf = String.valueOf(type);
            throw new RuntimeException(new StringBuilder(String.valueOf(valueOf).length() + 55).append("Cannot access Frame of type ").append(valueOf).append(" as a FrameBuffer instance!").toString());
        }
        int[] dims = backingStore.getDimensions();
        if (dims == null || dims.length == 0) {
            throw new RuntimeException("Cannot access Frame with no dimensions as a FrameBuffer instance!");
        }
    }

    void updateLength(int[] r5) {
        /* JADX: method processing error */
/*
Error: java.lang.IndexOutOfBoundsException: bitIndex < 0: -1
	at java.util.BitSet.get(BitSet.java:623)
	at jadx.core.dex.visitors.CodeShrinker$ArgsInfo.usedArgAssign(CodeShrinker.java:138)
	at jadx.core.dex.visitors.CodeShrinker$ArgsInfo.access$300(CodeShrinker.java:43)
	at jadx.core.dex.visitors.CodeShrinker.canMoveBetweenBlocks(CodeShrinker.java:282)
	at jadx.core.dex.visitors.CodeShrinker.shrinkBlock(CodeShrinker.java:232)
	at jadx.core.dex.visitors.CodeShrinker.shrinkMethod(CodeShrinker.java:38)
	at jadx.core.dex.visitors.regions.LoopRegionVisitor.checkArrayForEach(LoopRegionVisitor.java:196)
	at jadx.core.dex.visitors.regions.LoopRegionVisitor.checkForIndexedLoop(LoopRegionVisitor.java:119)
	at jadx.core.dex.visitors.regions.LoopRegionVisitor.processLoopRegion(LoopRegionVisitor.java:65)
	at jadx.core.dex.visitors.regions.LoopRegionVisitor.enterRegion(LoopRegionVisitor.java:52)
	at jadx.core.dex.visitors.regions.DepthRegionTraversal.traverseInternal(DepthRegionTraversal.java:56)
	at jadx.core.dex.visitors.regions.DepthRegionTraversal.traverseInternal(DepthRegionTraversal.java:58)
	at jadx.core.dex.visitors.regions.DepthRegionTraversal.traverse(DepthRegionTraversal.java:18)
	at jadx.core.dex.visitors.regions.LoopRegionVisitor.visit(LoopRegionVisitor.java:46)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
*/
        /*
        r4 = this;
        r1 = 1;
        r4.mLength = r1;
        r2 = r5.length;
        r1 = 0;
    L_0x0005:
        if (r1 >= r2) goto L_0x0011;
    L_0x0007:
        r0 = r5[r1];
        r3 = r4.mLength;
        r3 = r3 * r0;
        r4.mLength = r3;
        r1 = r1 + 1;
        goto L_0x0005;
    L_0x0011:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.media.filterfw.FrameBuffer1D.updateLength(int[]):void");
    }
}
