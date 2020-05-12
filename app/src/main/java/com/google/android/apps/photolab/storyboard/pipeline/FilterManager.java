package com.google.android.apps.photolab.storyboard.pipeline;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.android.apps.photolab.storyboard.activity.ComicPageData;

import java.io.IOException;

public class FilterManager {
    private static final String TAG = "FilterManager";
    private static FilterManager _filterManager;
    private int filterIndex = 0;
    private int[] filterList = new int[]{4, 3, 1, 2, 8, 5, 4, 3, 6, 0, 8, 9};
    private int[] filterTime = new int[]{500, 500, 900, 1800, 500, 500, 1800, 500, 500, 500, 500, 500, 500, 2000};

    public static FilterManager instance() {
        if (_filterManager == null) {
            _filterManager = new FilterManager();
        }
        return _filterManager;
    }

    public static void initTexture(String filename, Context context) {
        try {
            RAISRFilters.loadTextureFilterAtlas(ComicIO.writeTexture(BitmapFactory.decodeStream(context.getAssets().open(filename)), filename), 5);
        } catch (IOException e) {
            String str = TAG;
            String str2 = "Image missing: ";
            String valueOf = String.valueOf(filename);
            Log.i(str, valueOf.length() != 0 ? str2.concat(valueOf) : new String(str2));
        }
    }

    public int getFilterIndex() {
        return this.filterIndex;
    }

    public int getExpectedFilterTime() {
        return this.filterTime[this.filterIndex];
    }

    public int setExpectedFilterTime(int index, int ms) {
        this.filterTime[index] = ms;
        return ms;
    }

    public Bitmap filterFullImage(Bitmap bmpIn, ComicPageData cpd) {
        Bitmap result = bmpIn;
        if (!AssetLoader.isProcessing()) {
            RAISRFilters.predefinedRAISRFilters(result, result, this.filterList[this.filterIndex]);
            this.filterIndex = (this.filterIndex + 1) % this.filterList.length;
        }
        return result;
    }

    public Bitmap equalizeImageIfNeeded(Bitmap bmpIn) {
        RAISRFilters.predefinedRAISRFilters(bmpIn, bmpIn, 19);
        return bmpIn;
    }
}
