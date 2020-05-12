package com.google.android.apps.photolab.storyboard.pipeline;

import android.graphics.Bitmap;
import com.google.common.base.Preconditions;

import java.io.Closeable;

public final class ObjectDetector implements Closeable {
    private boolean isClosed = false;
    private final long nativeContext;

    private static native void nativeClose(long j);

    private static native long nativeCreate();

    private static native ObjectDetection[] nativeDetectObjects(long j, Bitmap bitmap);

    static {
        System.loadLibrary("objectdetector_native");
    }

    private ObjectDetector(long nativeContext) {
        this.nativeContext = nativeContext;
    }

    public static ObjectDetector create() {
        long nativeContext = nativeCreate();
        if (nativeContext != 0) {
            return new ObjectDetector(nativeContext);
        }
        throw new RuntimeException("Could not initialize ObjectDetector.");
    }

    public ObjectDetection[] detectObjects(Bitmap bitmap) throws IllegalArgumentException {
        Preconditions.checkState(!this.isClosed, "ObjectDetector has been closed");
        if (bitmap != null) {
            return nativeDetectObjects(this.nativeContext, bitmap);
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
