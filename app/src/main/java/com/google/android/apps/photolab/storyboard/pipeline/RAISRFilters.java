package com.google.android.apps.photolab.storyboard.pipeline;

import android.graphics.Bitmap;

public class RAISRFilters {
    public static native boolean loadTextureFilterAtlas(String str, int i);

    public static native boolean predefinedRAISRFilters(Bitmap bitmap, Bitmap bitmap2, int i);

    static {
        System.loadLibrary("filter_native");
    }
}
