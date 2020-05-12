package com.google.android.apps.photolab.storyboard.activity;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import com.google.android.apps.photolab.storyboard.pipeline.FilterManager;
import com.google.android.apps.photolab.storyboard.pipeline.ObjectDetectionSet;
import com.google.protos.humansensing.FacesProtos.Faces;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

public class ComicPageData {
    private ArrayList<ComicPanel> comicPanels = new ArrayList();
    private Faces detectedFaces;
    private ObjectDetectionSet detectedObjects;
    public PageEdge[] edgeData;
    private Bitmap filteredBitmap;
    private boolean fullyGenerated;
    private int layoutIndex;
    public ArrayList<Integer> mergedPanels;
    private Bitmap workingBitmap;

    public ComicPageData(int layoutIndex) {
        this.layoutIndex = layoutIndex;
    }

    public int getLayoutIndex() {
        return this.layoutIndex;
    }

    public ObjectDetectionSet getDetectedObjects() {
        return this.detectedObjects;
    }

    public Faces getDetectedFaces() {
        return this.detectedFaces;
    }

    public void addDetections(ObjectDetectionSet detectedObjects) {
        this.detectedObjects = detectedObjects;
    }

    public boolean isFullyGenerated() {
        return this.fullyGenerated;
    }

    public void setIsFullyGenerated(boolean value) {
        this.fullyGenerated = value;
    }

    public Bitmap getFilteredBitmap() {
        return this.filteredBitmap;
    }

    public void setFilteredBitmap(Bitmap bmp) {
        this.filteredBitmap = bmp;
        setIsFullyGenerated(bmp != null);
    }

    public Bitmap getWorkingBitmap(int width, int height) {
        this.workingBitmap = Bitmap.createBitmap(Math.max(1, width), Math.max(1, height), Config.ARGB_8888);
        return this.workingBitmap;
    }

    public void filteringComplete() {
        this.filteredBitmap = this.workingBitmap;
        this.workingBitmap = null;
        this.fullyGenerated = true;
    }

    public int panelCount() {
        return this.comicPanels.size();
    }

    public ArrayList<ComicPanel> getPanels() {
        return this.comicPanels;
    }

    public void clear() {
        this.comicPanels.clear();
    }

    public void addPanel(ComicPanel panel) {
        this.comicPanels.add(panel);
    }

    public ComicPanel getPanel(int index) {
        if (index < 0 || index >= this.comicPanels.size()) {
            return null;
        }
        return (ComicPanel) this.comicPanels.get(index);
    }

    public void trimToSize(int index) {
        while (this.comicPanels.size() > index) {
            this.comicPanels.remove(this.comicPanels.size() - 1);
        }
    }

    public void centerImages() {
        Iterator it = this.comicPanels.iterator();
        while (it.hasNext()) {
            ((ComicPanel) it.next()).centerImage();
        }
    }

    public int getFilterIndex() {
        return FilterManager.instance().getFilterIndex();
    }

    public String comicInfo() {
        String result = "L" + this.layoutIndex + "_F" + getFilterIndex();
        Iterator it = this.comicPanels.iterator();
        while (it.hasNext()) {
            ComicPanel cp = (ComicPanel) it.next();
            String valueOf = String.valueOf(result);
            String format = String.format(Locale.US, "%.2f", new Object[]{Float.valueOf(cp.getZoom())});
            format = String.valueOf(new StringBuilder((String.valueOf(valueOf).length() + 1) + String.valueOf(format).length()).append(valueOf).append(" ").append(format).toString());
            valueOf = String.valueOf(cp.detection == null ? "x" : Float.valueOf(cp.detection.getArea()));
            result = new StringBuilder((String.valueOf(format).length() + 1) + String.valueOf(valueOf).length()).append(format).append("_").append(valueOf).toString();
        }
        return result;
    }
}
