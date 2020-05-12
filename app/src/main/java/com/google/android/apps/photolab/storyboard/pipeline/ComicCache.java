package com.google.android.apps.photolab.storyboard.pipeline;

import com.google.android.apps.photolab.storyboard.activity.ComicActivity;
import com.google.android.apps.photolab.storyboard.activity.ComicGenerator;
import com.google.android.apps.photolab.storyboard.activity.ComicPageData;
import com.google.android.apps.photolab.storyboard.activity.ComicPanel;

import java.util.ArrayList;

/**
 * 预加载缓存内容
 */
public class ComicCache {
    private static final int BITMAPS_TO_CACHE = 4;
    private static final Object syncProcessing = new Object();
    private ArrayList<ComicPageData> cache;
    private int processingCount;

    public ComicCache() {
        init();
    }

    private void init() {
        this.cache = new ArrayList(BITMAPS_TO_CACHE);
    }

    public ComicPageData getCurrentComic(boolean generateIfNull) {
        if (this.cache.size() == 0 && generateIfNull) {
            ComicGenerator cg = ComicActivity.getActivity().getComicGenerator();
            if (cg != null) {
                this.cache.add(cg.createLayout(cg.getCurrentLayoutIndex()));
            }
        }
        return this.cache.size() > 0 ? (ComicPageData) this.cache.get(0) : null;
    }

    public ArrayList<ComicPanel> getCurrentPanels() {
        if (getCurrentComic(true) != null) {
            return getCurrentComic(true).getPanels();
        }
        return new ArrayList();
    }

    public boolean isNextComicReady() {
        return canRemoveComic();
    }

    public void incProcessingCount() {
        synchronized (syncProcessing) {
            this.processingCount++;
        }
    }

    public void decProcessingCount() {
        synchronized (syncProcessing) {
            this.processingCount--;
        }
    }

    public boolean canAddComic() {
        return this.cache.size() + this.processingCount < BITMAPS_TO_CACHE;
    }

    public void addComic(ComicPageData cpd) {
        this.cache.add(cpd);
    }

    public boolean canRemoveComic() {
        return this.cache.size() > 1 && ((ComicPageData) this.cache.get(1)).isFullyGenerated();
    }

    public void removeCurrentComic() {
        if (canRemoveComic()) {
            this.cache.remove(0);
            ComicPageData cpd = (ComicPageData) this.cache.get(0);
            if (cpd != null && cpd.isFullyGenerated() && cpd.panelCount() > 0) {
                ComicIO.getInstance();
                ComicIO.writeTexture(((ComicPageData) this.cache.get(0)).getFilteredBitmap(), ComicIO.LAST_FILTERED_FILENAME);
            }
        }
    }

    public void removeLoadingComics() {
        while (this.cache.size() > 1 && ((ComicPageData) this.cache.get(1)).isFullyGenerated() && ((ComicPageData) this.cache.get(1)).getFilteredBitmap() == null) {
            this.cache.remove(1);
        }
    }

    public void removeLoadingComicsFromEnd() {
        while (this.cache.size() > 1 && ((ComicPageData) this.cache.get(this.cache.size() - 1)).getFilteredBitmap() == null) {
            this.cache.remove(this.cache.size() - 1);
        }
    }

    public void reset() {
        this.processingCount = 0;
        this.cache.clear();
    }
}
