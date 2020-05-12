package com.google.android.libraries.vision.humansensing.ssd;

import android.graphics.Bitmap;
import com.google.common.base.Preconditions;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protos.humansensing.FacesProtos.Faces;
import com.google.protos.mobilessd.MobileSSDClientOptionsProto.MobileSSDClientOptions;
import java.io.Closeable;

public final class FaceDetector implements Closeable {
    private boolean isClosed = false;
    private final long nativeContext;

    private static native void nativeClose(long j);

    private static native long nativeCreateFromOptions(byte[] bArr);

    private static native byte[] nativeDetectFaces(long j, Bitmap bitmap);

    static {
        System.loadLibrary("facedetector_native");
    }

    private FaceDetector(long nativeContext) {
        this.nativeContext = nativeContext;
    }

    public static FaceDetector createFromOptions(MobileSSDClientOptions options) {
        if (options == null) {
            throw new IllegalArgumentException("MobileSSDClientOptions cannot be null.");
        }
        long nativeContext = nativeCreateFromOptions(options.toByteArray());
        if (nativeContext != 0) {
            return new FaceDetector(nativeContext);
        }
        throw new RuntimeException("Could not initialize FaceDetector.");
    }

    public Faces detectFaces(Bitmap bitmap) throws IllegalArgumentException, InvalidProtocolBufferException {
        Preconditions.checkState(!this.isClosed, "FaceDetector has been closed");
        if (bitmap != null) {
            return Faces.parseFrom(nativeDetectFaces(this.nativeContext, bitmap));
        }
        throw new IllegalArgumentException("Bitmap cannot be null.");
    }

    public void close() {
        if (!this.isClosed) {
            nativeClose(this.nativeContext);
            this.isClosed = true;
        }
    }

    protected void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }
}
