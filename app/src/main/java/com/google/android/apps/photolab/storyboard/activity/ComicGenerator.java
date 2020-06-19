package com.google.android.apps.photolab.storyboard.activity;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.RectF;
import androidx.core.view.ViewCompat;
import android.util.Log;
import android.util.Size;

import com.google.android.apps.photolab.storyboard.activity.IComicMoveable.ComicMoveableKind;
import com.google.android.apps.photolab.storyboard.activity.PageEdge.PageEdgeKind;
import com.google.android.apps.photolab.storyboard.pipeline.AssetLoader;
import com.google.android.apps.photolab.storyboard.pipeline.ComicCache;
import com.google.android.apps.photolab.storyboard.pipeline.ComicUtils;
import com.google.android.apps.photolab.storyboard.pipeline.FilterManager;
import com.google.android.apps.photolab.storyboard.pipeline.ObjectDetection;
import com.google.android.apps.photolab.storyboard.pipeline.ObjectDetectionSet;
import com.google.android.apps.photolab.storyboard.pipeline.PanelDebugInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ComicGenerator {
    private static final boolean SHUFFLE_FRAMES = true;
    private static ArrayList<ArrayList<Integer>> allMergedPanels;
    private static ArrayList<Integer> emptyMergeList = new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(0)}));
    private static boolean hasShuffled = false;
    private static PageEdge[][] layouts;
    private static int[] shuffledLayouts;
    private boolean abortFiltering = false;
    private ComicPresenter comicPresenter;
    private ExecutorService executor;
    private int height;
    private int heroPanelIndex = 0;
    private ArrayList<PageEdge> horizontalEdges = new ArrayList();
    public boolean isGeneratingLayout = false;
    private int layoutCounter;
    private int layoutIndex;
    private float margin = 0.01f;
    private ArrayList<Integer> mergedImages = new ArrayList();
    private ArrayList<PageEdge> verticalEdges = new ArrayList();
    private int width;

    public ComicGenerator(ComicPresenter comicPresenter) {
        this.comicPresenter = comicPresenter;
        this.layoutCounter = 0;
        this.layoutIndex = 0;
    }

    public int getCurrentLayoutIndex() {
        return this.layoutIndex;
    }

    public void loadEdges(PageEdge[] edges) {
        for (PageEdge edge : edges) {
            addEdge(edge);
        }
    }

    public ComicPageData createLayout(int newLayoutIndex) {
        this.isGeneratingLayout = true;
        this.horizontalEdges.clear();
        this.verticalEdges.clear();
        ComicPageData result = getNewComicPage(newLayoutIndex);
        this.mergedImages = ComicUtils.rnd.nextInt(100) < 10 ? result.mergedPanels : emptyMergeList();
        loadEdges(result.edgeData);
        generatePanels(result);
        this.isGeneratingLayout = false;
        return result;
    }

    private ComicCache getComicCache() {
        return this.comicPresenter.getComicCache();
    }

    private void addEdge(PageEdge edge) {
        if (edge.isHorizontal()) {
            this.horizontalEdges.add(edge);
        } else {
            this.verticalEdges.add(edge);
        }
    }

    public void onSizeChanged(int w, int h) {
        Size sz = ComicActivity.getActivity().getSize();
        if (sz.getWidth() <= 0 || sz.getHeight() <= 0) {
            Log.i("ContentValues", "onSizeChanged: Zero size");
            return;
        }
        this.width = sz.getWidth();
        this.height = sz.getHeight();
    }

    public void generateNextLayout() {
        this.layoutCounter++;
        if (this.layoutCounter >= layouts.length) {
            this.layoutCounter = 0;
        }
        this.layoutIndex = getNextLayoutIndex(this.layoutCounter);
        generateComic(this.layoutIndex);
    }

    public void generateCurrentLayout() {
        generateComic(this.layoutIndex);
    }

    public void generateComic(int index) {
        ComicPageData cpd = createLayout(index);
        if (AssetLoader.isProcessing()) {
            cpd.setIsFullyGenerated(true);
            ComicActivity.getActivity().stopSpinner();
        } else {
            shuffleComicPage(cpd);
            filterComic(cpd);
        }
        getComicCache().addComic(cpd);
    }

    public void abortAllFiltering() {
        this.abortFiltering = true;
    }

    public void resumeFiltering() {
        this.abortFiltering = false;
    }

    public void filterComic(final ComicPageData cpd) {
        if (this.executor == null) {
            this.executor = Executors.newSingleThreadExecutor();
        }
        Thread thread = new Thread(new Runnable() {
            public void run() {
                if (!ComicGenerator.this.abortFiltering) {
                    ComicGenerator.this.filterComicBegin(cpd);
                    ComicGenerator.this.filterComicComplete(cpd);
                }
            }
        });
        thread.setPriority(9);
        try {
            this.executor.execute(thread);
        } catch (Exception localException) {
            String str = "ContentValues";
            String str2 = "Thread executor failed: ";
            String valueOf = String.valueOf(localException.getMessage());
            Log.i(str, valueOf.length() != 0 ? str2.concat(valueOf) : new String(str2));
        }
    }

    private void filterComicBegin(ComicPageData cpd) {
        Log.i("ContentValues", "filterfull: start " + cpd.getLayoutIndex());
        long startTimeMs = System.currentTimeMillis();
        Bitmap bitmapToFilter = cpd.getWorkingBitmap(this.width, this.height);
        if (ComicActivity.getActivity().getComicPresenter() != null) {
            ComicActivity.getActivity().getComicPresenter().onDrawGL(new Canvas(bitmapToFilter), cpd);
        }
        int curFilter = FilterManager.instance().getFilterIndex();
        FilterManager.instance().filterFullImage(bitmapToFilter, cpd);
        int timeToFilter = (int) (System.currentTimeMillis() - startTimeMs);
        FilterManager.instance().setExpectedFilterTime(curFilter, timeToFilter);
        Log.d("ContentValues", "Comic filterfull time: " + timeToFilter);
    }

    private void filterComicComplete(ComicPageData cpd) {
        if (cpd != null && !this.abortFiltering) {
            cpd.filteringComplete();
            ComicActivity.getActivity().stopSpinner();
            ComicActivity.getActivity().setIsFirstComic(false);
            ComicActivity.getActivity().layoutChanged();
            Log.i("ContentValues", "filterfull: end " + cpd.getLayoutIndex());
        }
    }

    public void shuffleFrames() {
        shuffleComicPage(getComicCache().getCurrentComic(true));
        ComicActivity.getActivity().layoutChanged();
    }

    public void shuffleComicPage(ComicPageData comicPageData) {
        int[] imageIndexes = ComicUtils.getRandomSequence(comicPageData.panelCount());
        Arrays.sort(imageIndexes);
        boolean hasMerged = false;
        for (int i = 0; i < comicPageData.panelCount(); i++) {
            ComicPanel cp = comicPageData.getPanel(i);
            int imageIndex = imageIndexes[i];
            if (cp.mergedPanels.size() <= 1) {
                cp.setImageIndex(imageIndex);
            } else if (!hasMerged) {
                hasMerged = true;
                cp.setImageIndex(imageIndexes[((ComicPanel) cp.mergedPanels.get(ComicUtils.rnd.nextInt(cp.mergedPanels.size()))).panelNumber]);
            }
        }
        if (!AssetLoader.isProcessing()) {
            runCroppingRules(comicPageData);
        }
        Iterator it = comicPageData.getPanels().iterator();
        while (it.hasNext()) {
            ((ComicPanel) it.next()).centerImage();
        }
    }

    public int getPanelCount() {
        return getComicCache().getCurrentComic(true).panelCount();
    }

    public ComicPanel getComicPanel(int index) {
        if (index < 0 || index >= getComicCache().getCurrentComic(true).panelCount()) {
            return null;
        }
        return getComicCache().getCurrentComic(true).getPanel(index);
    }

    public ComicPanel getComicPanelContainerFor(IComicMoveable element) {
        if (element == null) {
            return null;
        }
        if (element.getMoveableKind() == ComicMoveableKind.PANEL) {
            return (ComicPanel) element;
        }
        Iterator it = getComicPages().iterator();
        while (it.hasNext()) {
            ComicPanel panel = (ComicPanel) it.next();
            if (panel.speechBubble == element || panel.caption == element) {
                return panel;
            }
            if (panel.comicBitmapInstance == element) {
                return panel;
            }
        }
        return null;
    }

    public ComicPanel getHeroPanel(ComicPageData cpd) {
        if (cpd.panelCount() < this.heroPanelIndex) {
            return cpd.getPanel(this.heroPanelIndex);
        }
        return null;
    }

    public float getAreaPercent(ComicPanel panel) {
        return panel.getArea() / ((float) (this.width * this.height));
    }

    public void generatePanels(ComicPageData cpd) {
        Random random = new Random(System.currentTimeMillis());
        Collections.sort(this.verticalEdges);
        Collections.sort(this.horizontalEdges);
        float hm = (this.margin / 2.0f) * ((float) this.width);
        int index = 0;
        PointF innerSize = new PointF(((float) this.width) - (2.0f * hm), ((float) this.height) - (2.0f * hm));
        float largestPanelSize = 0.0f;
        int[] imageIndexes = ComicUtils.getRandomSequence(12);
        int horz = 0;
        while (horz < this.horizontalEdges.size() - 1) {
            PageEdge hEdge = (PageEdge) this.horizontalEdges.get(horz);
            int vert = 0;
            while (vert < this.verticalEdges.size() - 1) {
                PageEdge vEdge = (PageEdge) this.verticalEdges.get(vert);
                if (vEdge != null && hEdge != null && hEdge.containsIndex(horz, vert) && vEdge.containsIndex(horz, vert)) {
                    ComicPanel panel = null;
                    PageEdge nextHEdge = nextEdgeDownFrom(horz, vert);
                    PageEdge nextVEdge = nextEdgeAcrossFrom(horz, vert);
                    PointF tl = hEdge.intercept(vEdge, innerSize, hm);
                    PointF tr = hEdge.intercept(nextVEdge, innerSize, hm);
                    PointF br = nextHEdge.intercept(nextVEdge, innerSize, hm);
                    PointF bl = nextHEdge.intercept(vEdge, innerSize, hm);
                    if (index >= cpd.panelCount()) {
                        ComicPanel comicPanel = new ComicPanel(ViewCompat.MEASURED_SIZE_MASK, index, this.mergedImages.indexOf(Integer.valueOf(index)) > 0 ? ((Integer) this.mergedImages.get(0)).intValue() : imageIndexes[index]);
                        comicPanel.margin = 2.0f * hm;
                        comicPanel.setPanelPoints(tl, tr, br, bl);
                        cpd.addPanel(comicPanel);
                    } else {
                        panel = cpd.getPanel(index);
                        panel.reset();
                        panel.setPanelPoints(tl, tr, br, bl);

                        panel.panelNumber = index;
                        float panelSize = panel.getPanelFrame().width() * panel.getPanelFrame().height();
                        if (panelSize > largestPanelSize) {
                            this.heroPanelIndex = index;
                            largestPanelSize = panelSize;
                        }
                    }
                    index++;
                }
                vert++;
            }
            horz++;
        }
        cpd.trimToSize(index);
        Iterator it = this.mergedImages.iterator();
        while (it.hasNext()) {
            ComicPanel p = cpd.getPanel(((Integer) it.next()).intValue());
            if (p != null) {
                for (int i = 0; i < this.mergedImages.size(); i++) {
                    p.mergedPanels.add(cpd.getPanel(((Integer) this.mergedImages.get(i)).intValue()));
                }
            }
        }
        shuffleComicPage(cpd);
    }

    private PageEdge nextEdgeDownFrom(int row, int col) {
        PageEdge result = (PageEdge) this.horizontalEdges.get(this.horizontalEdges.size() - 1);
        for (int i = row + 1; i < this.horizontalEdges.size(); i++) {
            if (((PageEdge) this.horizontalEdges.get(i)).containsIndex(row, col)) {
                return (PageEdge) this.horizontalEdges.get(i);
            }
        }
        return result;
    }

    private PageEdge nextEdgeAcrossFrom(int row, int col) {
        PageEdge result = (PageEdge) this.verticalEdges.get(this.verticalEdges.size() - 1);
        for (int i = col + 1; i < this.verticalEdges.size(); i++) {
            if (((PageEdge) this.verticalEdges.get(i)).containsIndex(row, col)) {
                return (PageEdge) this.verticalEdges.get(i);
            }
        }
        return result;
    }

    private RectF getLineFromEdge(PageEdge edge) {
        if (edge.isHorizontal()) {
            return new RectF(0.0f, (edge.getLocation() * ((float) this.height)) - 1.0f, (float) this.width, (edge.getLocation() * ((float) this.height)) + 1.0f);
        }
        return new RectF((edge.getLocation() * ((float) this.width)) - 1.0f, 0.0f, (edge.getLocation() * ((float) this.width)) + 1.0f, (float) this.height);
    }

    IComicMoveable hitTest(int x, int y) {
        return getPanelFromPoint((float) x, (float) y);
    }

    ComicPanel getPanelFromPoint(float x, float y) {
        Iterator it = getComicCache().getCurrentPanels().iterator();
        while (it.hasNext()) {
            ComicPanel panel = (ComicPanel) it.next();
            if (panel.contains(x, y)) {
                return panel;
            }
        }
        return null;
    }

    void setEdgeLocation(PageEdge edge, float x, float y) {
        if (edge.isHorizontal()) {
            edge.setLocation(y / ((float) this.height));
        } else {
            edge.setLocation(x / ((float) this.width));
        }
    }

    private ArrayList<ComicPanel> getComicPages() {
        return getComicCache().getCurrentPanels();
    }

    public float getMargin() {
        return this.margin;
    }

    public void setMargin(float margin) {
        this.margin = margin;
    }

    public ArrayList<PageEdge> getHorizontalEdges() {
        return this.horizontalEdges;
    }

    public void setHorizontalEdges(ArrayList<PageEdge> horizontalEdges) {
        this.horizontalEdges = horizontalEdges;
    }

    public ArrayList<PageEdge> getVerticalEdges() {
        return this.verticalEdges;
    }

    public void setVerticalEdges(ArrayList<PageEdge> verticalEdges) {
        this.verticalEdges = verticalEdges;
    }

    public float getWidth() {
        return (float) this.width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public float getHeight() {
        return (float) this.height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    private ArrayList<ObjectDetection> filterNonUniqueDetections(ComicPageData comicPageData, ObjectDetectionSet detectionSet) {
        ArrayList<ObjectDetection> result = new ArrayList();
        if (detectionSet != null) {
            Iterator it = detectionSet.getDetections().iterator();
            while (it.hasNext()) {
                ObjectDetection detection = (ObjectDetection) it.next();
                if (detectionDuplicateCount(comicPageData, detection) < 2) {
                    result.add(detection);
                }
            }
        }
        return result;
    }

    private ArrayList<ObjectDetection> addRandomAspects(ArrayList<ObjectDetection> detections, boolean isHorizontal) {
        ArrayList<ObjectDetection> result = new ArrayList();
        Iterator it = detections.iterator();
        while (it.hasNext()) {
            ObjectDetection detection = (ObjectDetection) it.next();
            for (int i = 0; i < 5; i++) {
                result.add(detection.getRandomAspectAdjustment(isHorizontal));
            }
        }
        return result;
    }

    private boolean isDetectionUnique(ObjectDetection detection) {
        boolean result = true;
        Iterator it = getComicCache().getCurrentPanels().iterator();
        while (it.hasNext()) {
            ComicPanel panel = (ComicPanel) it.next();
            if (panel.detection != null && panel.detection.category.equals(detection.category)) {
                result = false;
            }
        }
        return result;
    }

    private int detectionDuplicateCount(ComicPageData comicPageData, ObjectDetection detection) {
        int result = 0;
        Iterator it = comicPageData.getPanels().iterator();
        while (it.hasNext()) {
            ComicPanel panel = (ComicPanel) it.next();
            if (panel.detection != null && panel.detection.category.equals(detection.category)) {
                result++;
            }
        }
        return result;
    }

    private void runCroppingRules(ComicPageData comicPageData) {
        Iterator it;
        int faceCount = 0;
        String debug = "";
        int[] shuffledPanels = ComicUtils.shuffleArrayOfSize(comicPageData.panelCount());
        ArrayList<PanelDebugInfo> debugPanels = new ArrayList();
        for (int panel : shuffledPanels) {
            String str;
            ComicPanel panel2 = comicPageData.getPanel(panel);
            if (panel2.useDetection()) {
                PanelDebugInfo debugPanel = new PanelDebugInfo(panel2);
                debugPanels.add(debugPanel);
                ArrayList<ObjectDetection> dets = addRandomAspects(filterNonUniqueDetections(comicPageData, panel2.comicBitmapInstance.getDetectionSet()), panel2.isHorizontal());
                float[] scores = new float[dets.size()];
                debugPanel.debugReasoning("start", dets, scores);
                ObjectDetectionSet.rateByCentered(dets, 0.8f, scores);
                debugPanel.debugReasoning("Center", dets, scores);
                ObjectDetectionSet.rateByFaces(dets, (float) faceCount, scores);
                debugPanel.debugReasoning("Faces", dets, scores);
                ObjectDetectionSet.rateByAspect(dets, panel2.getAspect(), scores);
                debugPanel.debugReasoning("Aspect", dets, scores);
                ObjectDetectionSet.rateByArea(dets, panel2.getAspect(), scores);
                debugPanel.debugReasoning("Area", dets, scores);
                int maxScoreIndex = getWinningScore(scores, debugPanel, dets);
                if (maxScoreIndex > -1) {
                    panel2.detection = (ObjectDetection) dets.get(maxScoreIndex);
                    debugPanel.winIndex = maxScoreIndex;
                    if (panel2.detection != null && panel2.detection.isFace) {
                        faceCount++;
                    }
                }
                if (ComicActivity.SHOW_DEBUG_INFO) {
                    it = dets.iterator();
                    while (it.hasNext()) {
                        ObjectDetection det = (ObjectDetection) it.next();
                        String valueOf = String.valueOf(debug);
                        str = det.category;
                        debug = new StringBuilder((String.valueOf(valueOf).length() + 1) + String.valueOf(str).length()).append(valueOf).append(".").append(str).toString();
                    }
                    debug = String.valueOf(debug).concat(" | ");
                    int imageIndex = panel2.comicBitmapInstance.getImageIndex();
                    str = Arrays.toString(scores);
                    Log.i("ContentValues", new StringBuilder(String.valueOf(str).length() + 48).append("comic CroppingRules: ").append(imageIndex).append(",").append(maxScoreIndex).append(" :: ").append(str).toString());
                }
            } else {
                panel2.detection = null;
                if (ComicActivity.SHOW_DEBUG_INFO) {
                    debug = String.valueOf(debug).concat(" noDet");
                }
            }
        }
        if (ComicActivity.SHOW_DEBUG_INFO) {
            String valueOf = "ContentValues";
            String str = "comic inst dets: ";
            String valueOf2 = String.valueOf(debug);
            Log.i(valueOf, valueOf2.length() != 0 ? str.concat(valueOf2) : new String(str));
            it = debugPanels.iterator();
            while (it.hasNext()) {
                ((PanelDebugInfo) it.next()).traceReasoning();
            }
        }
    }

    private int getWinningScore(float[] scores, PanelDebugInfo debugInfo, ArrayList<ObjectDetection> dets) {
        ArrayList<Integer> possibleWins = new ArrayList();
        ArrayList<Float> winningValues = new ArrayList();
        debugInfo.possibleWins = possibleWins;
        if (scores.length <= 0) {
            return -1;
        }
        LinkedHashSet<Float> unique = new LinkedHashSet();
        for (float score : scores) {
            unique.add(Float.valueOf(score));
        }
        float[] scoresSorted = new float[unique.size()];
        int index = 0;
        Iterator it = unique.iterator();
        while (it.hasNext()) {
            int index2 = index + 1;
            scoresSorted[index] = ((Float) it.next()).floatValue();
            index = index2;
        }
        Arrays.sort(scoresSorted);
        int cutoffIndex = scoresSorted.length - 3;
        if (cutoffIndex < 0) {
            cutoffIndex = 0;
        }
        float cutoffValue = scoresSorted[cutoffIndex];
        int i = 0;
        while (i < scores.length) {
            if (scores[i] >= cutoffValue && winningValues.indexOf(Float.valueOf(scores[i])) == -1) {
                possibleWins.add(Integer.valueOf(i));
                winningValues.add(Float.valueOf(scores[i]));
            }
            i++;
        }
        String winningCategory = ((ObjectDetection) dets.get(((Integer) possibleWins.get(0)).intValue())).category;
        float maxValue = Float.MIN_VALUE;
        int diversityIndex = -1;
        i = 0;
        while (i < scores.length) {
            if (scores[i] > maxValue && !((ObjectDetection) dets.get(i)).category.equals(winningCategory)) {
                diversityIndex = i;
                maxValue = scores[i];
            }
            i++;
        }
        if (diversityIndex > -1) {
            possibleWins.add(Integer.valueOf(diversityIndex));
        }
        return ((Integer) possibleWins.get(ComicUtils.rnd.nextInt(possibleWins.size()))).intValue();
    }

    private static ComicPageData getNewComicPage(int index) {
        ComicPageData result = new ComicPageData(index);
        PageEdge[] layout = layouts[index];
        result.edgeData = new PageEdge[layout.length];
        for (int i = 0; i < layout.length; i++) {
            result.edgeData[i] = new PageEdge(layout[i]);
        }
        result.mergedPanels = (ArrayList) ((ArrayList) allMergedPanels.get(index)).clone();
        return result;
    }

    static {
        ArrayList[] r1 = new ArrayList[60];
        r1[0] = new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(0), Integer.valueOf(1), Integer.valueOf(2)}));
        r1[1] = new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(0), Integer.valueOf(1)}));
        r1[2] = new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(1), Integer.valueOf(2)}));
        r1[3] = new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(4), Integer.valueOf(5), Integer.valueOf(6)}));
        r1[4] = new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(0), Integer.valueOf(2), Integer.valueOf(3)}));
        r1[5] = new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(2), Integer.valueOf(3)}));
        r1[6] = new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(2), Integer.valueOf(3)}));
        r1[7] = new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(0), Integer.valueOf(1)}));
        r1[8] = new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(0), Integer.valueOf(1)}));
        r1[9] = new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(0), Integer.valueOf(1)}));
        r1[10] = new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(0), Integer.valueOf(1)}));
        r1[11] = new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(1), Integer.valueOf(2)}));
        r1[12] = new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(2), Integer.valueOf(3), Integer.valueOf(4)}));
        r1[13] = new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(1), Integer.valueOf(2)}));
        r1[14] = new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(0), Integer.valueOf(1)}));
        r1[15] = new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(0), Integer.valueOf(2), Integer.valueOf(3)}));
        r1[16] = new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(0), Integer.valueOf(1), Integer.valueOf(2)}));
        r1[17] = new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(0), Integer.valueOf(1), Integer.valueOf(2)}));
        r1[18] = new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(0), Integer.valueOf(1), Integer.valueOf(2)}));
        r1[19] = new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(2), Integer.valueOf(3)}));
        r1[20] = new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(3), Integer.valueOf(4), Integer.valueOf(5)}));
        r1[21] = new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(0), Integer.valueOf(1)}));
        r1[22] = new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(5), Integer.valueOf(6)}));
        r1[23] = new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(0), Integer.valueOf(1), Integer.valueOf(2)}));
        r1[24] = new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(3), Integer.valueOf(4), Integer.valueOf(5)}));
        r1[25] = new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(1), Integer.valueOf(2)}));
        r1[26] = new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(2), Integer.valueOf(3)}));
        r1[27] = new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(3), Integer.valueOf(4), Integer.valueOf(5)}));
        r1[28] = new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(1), Integer.valueOf(2)}));
        r1[29] = new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(0), Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3)}));
        r1[30] = new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(2), Integer.valueOf(3), Integer.valueOf(4)}));
        r1[31] = new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(2), Integer.valueOf(3), Integer.valueOf(4)}));
        r1[32] = new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(0), Integer.valueOf(1)}));
        r1[33] = new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(0), Integer.valueOf(1)}));
        r1[34] = new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(0), Integer.valueOf(1), Integer.valueOf(2)}));
        r1[35] = new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(0), Integer.valueOf(1)}));
        r1[36] = new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(0), Integer.valueOf(1)}));
        r1[37] = new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(1), Integer.valueOf(2)}));
        r1[38] = new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(4), Integer.valueOf(5)}));
        r1[39] = new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(2), Integer.valueOf(3), Integer.valueOf(4)}));
        r1[40] = new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(1), Integer.valueOf(2)}));
        r1[41] = new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(1), Integer.valueOf(2)}));
        r1[42] = new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(0), Integer.valueOf(1)}));
        r1[43] = new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(1), Integer.valueOf(2)}));
        r1[44] = new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(3), Integer.valueOf(4)}));
        r1[45] = new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(0), Integer.valueOf(1), Integer.valueOf(2)}));
        r1[46] = new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(1), Integer.valueOf(2)}));
        r1[47] = new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(3), Integer.valueOf(4)}));
        r1[48] = new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(2), Integer.valueOf(3), Integer.valueOf(4)}));
        r1[49] = new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(0), Integer.valueOf(1)}));
        r1[50] = new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(4), Integer.valueOf(5)}));
        r1[51] = new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(3), Integer.valueOf(4)}));
        r1[52] = new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(1), Integer.valueOf(2)}));
        r1[53] = new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(0), Integer.valueOf(1), Integer.valueOf(2)}));
        r1[54] = new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(0), Integer.valueOf(1)}));
        r1[55] = new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(1), Integer.valueOf(2)}));
        r1[56] = new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(4), Integer.valueOf(5), Integer.valueOf(6), Integer.valueOf(7)}));
        r1[57] = new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(2), Integer.valueOf(3), Integer.valueOf(4)}));
        r1[58] = new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(1), Integer.valueOf(2)}));
        r1[59] = new ArrayList(Arrays.asList(new Integer[]{Integer.valueOf(3), Integer.valueOf(5)}));
        allMergedPanels = new ArrayList(Arrays.asList(r1));
        PageEdge[][] r0 = new PageEdge[60][];
        r0[0] = new PageEdge[]{new PageEdge(PageEdgeKind.HORIZONTAL, 0.0f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.21f, 2, 3), new PageEdge(PageEdgeKind.HORIZONTAL, 0.42f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.66f, 0, 1), new PageEdge(PageEdgeKind.HORIZONTAL, 1.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.42f, 2, 4), new PageEdge(PageEdgeKind.VERTICAL, 0.56f, 0, 2), new PageEdge(PageEdgeKind.VERTICAL, 1.0f)};
        r0[1] = new PageEdge[]{new PageEdge(PageEdgeKind.HORIZONTAL, 0.0f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.42f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.7f, 0, 1), new PageEdge(PageEdgeKind.HORIZONTAL, 1.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.21f, 0, 1), new PageEdge(PageEdgeKind.VERTICAL, 0.42f, 1, 3), new PageEdge(PageEdgeKind.VERTICAL, 0.69f, 0, 1), new PageEdge(PageEdgeKind.VERTICAL, 1.0f)};
        r0[2] = new PageEdge[]{new PageEdge(PageEdgeKind.HORIZONTAL, 0.0f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.33f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.67f), new PageEdge(PageEdgeKind.HORIZONTAL, 1.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.0f), new PageEdge(PageEdgeKind.VERTICAL_ANGLED, new float[]{0.65f, 0.75f}, 1, 2), new PageEdge(PageEdgeKind.VERTICAL, 1.0f)};
        r0[3] = new PageEdge[]{new PageEdge(PageEdgeKind.HORIZONTAL, 0.0f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.13f, 2, 3), new PageEdge(PageEdgeKind.HORIZONTAL, 0.26f, 2, 3), new PageEdge(PageEdgeKind.HORIZONTAL, 0.4f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.7f, 1, 3), new PageEdge(PageEdgeKind.HORIZONTAL, 1.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.42f, 3, 5), new PageEdge(PageEdgeKind.VERTICAL, 0.56f, 0, 3), new PageEdge(PageEdgeKind.VERTICAL, 1.0f)};
        r0[4] = new PageEdge[]{new PageEdge(PageEdgeKind.HORIZONTAL, 0.0f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.14f, 0, 1), new PageEdge(PageEdgeKind.HORIZONTAL, 0.27f, 0, 1), new PageEdge(PageEdgeKind.HORIZONTAL, 0.4f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.7f, 2, 3), new PageEdge(PageEdgeKind.HORIZONTAL, 1.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.42f, 0, 3), new PageEdge(PageEdgeKind.VERTICAL, 0.56f, 3, 5), new PageEdge(PageEdgeKind.VERTICAL, 1.0f)};
        r0[5] = new PageEdge[]{new PageEdge(PageEdgeKind.HORIZONTAL, 0.0f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.25f), new PageEdge(PageEdgeKind.HORIZONTAL, (float) 0.5f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.75f), new PageEdge(PageEdgeKind.HORIZONTAL, 1.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.0f), new PageEdge(PageEdgeKind.VERTICAL, 1.0f)};
        r0[6] = new PageEdge[]{new PageEdge(PageEdgeKind.HORIZONTAL, 0.0f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.4f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.64f), new PageEdge(PageEdgeKind.HORIZONTAL, 1.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.0f), new PageEdge(PageEdgeKind.VERTICAL, (float) 0.5f, 0, 1), new PageEdge(PageEdgeKind.VERTICAL, 1.0f)};
        r0[7] = new PageEdge[]{new PageEdge(PageEdgeKind.HORIZONTAL, 0.0f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.42f), new PageEdge(PageEdgeKind.HORIZONTAL, 1.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.0f), new PageEdge(PageEdgeKind.VERTICAL, 1.0f)};
        r0[8] = new PageEdge[]{new PageEdge(PageEdgeKind.HORIZONTAL, 0.0f), new PageEdge(PageEdgeKind.HORIZONTAL, 1.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.32f), new PageEdge(PageEdgeKind.VERTICAL, 1.0f)};
        r0[9] = new PageEdge[]{new PageEdge(PageEdgeKind.HORIZONTAL, 0.0f), new PageEdge(PageEdgeKind.HORIZONTAL, 1.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.66f), new PageEdge(PageEdgeKind.VERTICAL, 1.0f)};
        r0[10] = new PageEdge[]{new PageEdge(PageEdgeKind.HORIZONTAL, 0.0f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.27f), new PageEdge(PageEdgeKind.HORIZONTAL, 1.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.0f), new PageEdge(PageEdgeKind.VERTICAL, 1.0f)};
        r0[11] = new PageEdge[]{new PageEdge(PageEdgeKind.HORIZONTAL, 0.0f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.33f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.66f), new PageEdge(PageEdgeKind.HORIZONTAL, 1.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.0f), new PageEdge(PageEdgeKind.VERTICAL, 1.0f)};
        r0[12] = new PageEdge[]{new PageEdge(PageEdgeKind.HORIZONTAL, 0.0f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.25f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.75f), new PageEdge(PageEdgeKind.HORIZONTAL, 1.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.0f), new PageEdge(PageEdgeKind.VERTICAL, (float) 0.5f, 0, 1), new PageEdge(PageEdgeKind.VERTICAL, (float) 0.5f, 2, 3), new PageEdge(PageEdgeKind.VERTICAL, 1.0f)};
        r0[13] = new PageEdge[]{new PageEdge(PageEdgeKind.HORIZONTAL, 0.0f), new PageEdge(PageEdgeKind.HORIZONTAL_ANGLED, new float[]{0.25f, 0.31f}), new PageEdge(PageEdgeKind.HORIZONTAL_ANGLED, new float[]{0.73f, 0.72f}), new PageEdge(PageEdgeKind.HORIZONTAL, 1.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.0f), new PageEdge(PageEdgeKind.VERTICAL, 1.0f)};
        r0[14] = new PageEdge[]{new PageEdge(PageEdgeKind.HORIZONTAL, 0.0f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.28f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.72f), new PageEdge(PageEdgeKind.HORIZONTAL, 1.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.42f, 2, 3), new PageEdge(PageEdgeKind.VERTICAL, 0.72f, 0, 1), new PageEdge(PageEdgeKind.VERTICAL, 1.0f)};
        r0[15] = new PageEdge[]{new PageEdge(PageEdgeKind.HORIZONTAL, 0.0f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.2f, 0, 1), new PageEdge(PageEdgeKind.HORIZONTAL, 0.4f, 0, 1), new PageEdge(PageEdgeKind.HORIZONTAL, 0.55f, 0, 1), new PageEdge(PageEdgeKind.HORIZONTAL, 0.82f, 0, 1), new PageEdge(PageEdgeKind.HORIZONTAL, 1.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.42f), new PageEdge(PageEdgeKind.VERTICAL, 1.0f)};
        r0[16] = new PageEdge[]{new PageEdge(PageEdgeKind.HORIZONTAL, 0.0f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.73f), new PageEdge(PageEdgeKind.HORIZONTAL, 1.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.0f), new PageEdge(PageEdgeKind.VERTICAL, (float) 0.5f, 1, 2), new PageEdge(PageEdgeKind.VERTICAL, 1.0f)};
        r0[17] = new PageEdge[]{new PageEdge(PageEdgeKind.HORIZONTAL, 0.0f), new PageEdge(PageEdgeKind.HORIZONTAL, 1.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.33f), new PageEdge(PageEdgeKind.VERTICAL, 0.66f), new PageEdge(PageEdgeKind.VERTICAL, 1.0f)};
        r0[18] = new PageEdge[]{new PageEdge(PageEdgeKind.HORIZONTAL, 0.0f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.66f), new PageEdge(PageEdgeKind.HORIZONTAL, 1.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.33f, 0, 1), new PageEdge(PageEdgeKind.VERTICAL, 0.66f, 0, 1), new PageEdge(PageEdgeKind.VERTICAL, 1.0f)};
        r0[19] = new PageEdge[]{new PageEdge(PageEdgeKind.HORIZONTAL, 0.0f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.33f, 0, 1), new PageEdge(PageEdgeKind.HORIZONTAL, 0.66f, 0, 1), new PageEdge(PageEdgeKind.HORIZONTAL, 1.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.4f), new PageEdge(PageEdgeKind.VERTICAL, 1.0f)};
        r0[20] = new PageEdge[]{new PageEdge(PageEdgeKind.HORIZONTAL, 0.0f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.25f), new PageEdge(PageEdgeKind.HORIZONTAL, (float) 0.5f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.75f), new PageEdge(PageEdgeKind.HORIZONTAL, 1.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.31f, 0, 1), new PageEdge(PageEdgeKind.VERTICAL, 0.31f, 2, 3), new PageEdge(PageEdgeKind.VERTICAL, 0.69f, 2, 3), new PageEdge(PageEdgeKind.VERTICAL, 1.0f)};
        r0[21] = new PageEdge[]{new PageEdge(PageEdgeKind.HORIZONTAL, 0.0f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.25f), new PageEdge(PageEdgeKind.HORIZONTAL, (float) 0.5f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.75f), new PageEdge(PageEdgeKind.HORIZONTAL, 1.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.31f, 0, 1), new PageEdge(PageEdgeKind.VERTICAL, (float) 0.5f, 1, 2), new PageEdge(PageEdgeKind.VERTICAL, 0.69f, 2, 3), new PageEdge(PageEdgeKind.VERTICAL, 1.0f)};
        r0[22] = new PageEdge[]{new PageEdge(PageEdgeKind.HORIZONTAL, 0.0f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.25f), new PageEdge(PageEdgeKind.HORIZONTAL, (float) 0.5f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.75f), new PageEdge(PageEdgeKind.HORIZONTAL, 1.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.31f, 0, 1), new PageEdge(PageEdgeKind.VERTICAL, 0.31f, 2, 3), new PageEdge(PageEdgeKind.VERTICAL, (float) 0.5f, 1, 2), new PageEdge(PageEdgeKind.VERTICAL, 0.69f, 2, 3), new PageEdge(PageEdgeKind.VERTICAL, 1.0f)};
        r0[23] = new PageEdge[]{new PageEdge(PageEdgeKind.HORIZONTAL, 0.0f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.36f, 1, 2), new PageEdge(PageEdgeKind.HORIZONTAL, 0.58f), new PageEdge(PageEdgeKind.HORIZONTAL, 1.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.57f, 0, 2), new PageEdge(PageEdgeKind.VERTICAL, 1.0f)};
        r0[24] = new PageEdge[]{new PageEdge(PageEdgeKind.HORIZONTAL, 0.0f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.25f), new PageEdge(PageEdgeKind.HORIZONTAL, (float) 0.5f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.75f), new PageEdge(PageEdgeKind.HORIZONTAL, 1.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.0f), new PageEdge(PageEdgeKind.VERTICAL, (float) 0.5f, 1, 2), new PageEdge(PageEdgeKind.VERTICAL, 0.69f, 2, 3), new PageEdge(PageEdgeKind.VERTICAL, 1.0f)};
        r0[25] = new PageEdge[]{new PageEdge(PageEdgeKind.HORIZONTAL, 0.0f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.37f, 2, 5), new PageEdge(PageEdgeKind.HORIZONTAL, 0.67f), new PageEdge(PageEdgeKind.HORIZONTAL, 1.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.25f, 2, 3), new PageEdge(PageEdgeKind.VERTICAL, 0.34f, 0, 2), new PageEdge(PageEdgeKind.VERTICAL, (float) 0.5f, 2, 3), new PageEdge(PageEdgeKind.VERTICAL, 0.58f, 0, 1), new PageEdge(PageEdgeKind.VERTICAL, 1.0f)};
        r0[26] = new PageEdge[]{new PageEdge(PageEdgeKind.HORIZONTAL, 0.0f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.6f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.8f, 0, 1), new PageEdge(PageEdgeKind.HORIZONTAL, 1.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.39f, 1, 3), new PageEdge(PageEdgeKind.VERTICAL, 0.65f, 1, 3), new PageEdge(PageEdgeKind.VERTICAL, 1.0f)};
        r0[27] = new PageEdge[]{new PageEdge(PageEdgeKind.HORIZONTAL, 0.0f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.33f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.66f), new PageEdge(PageEdgeKind.HORIZONTAL, 1.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.33f, 0, 1), new PageEdge(PageEdgeKind.VERTICAL, (float) 0.5f, 2, 3), new PageEdge(PageEdgeKind.VERTICAL, 0.66f, 0, 1), new PageEdge(PageEdgeKind.VERTICAL, 1.0f)};
        r0[28] = new PageEdge[]{new PageEdge(PageEdgeKind.HORIZONTAL, 0.0f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.33f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.66f), new PageEdge(PageEdgeKind.HORIZONTAL, 1.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.33f, 0, 1), new PageEdge(PageEdgeKind.VERTICAL, (float) 0.5f, 1, 3), new PageEdge(PageEdgeKind.VERTICAL, 0.66f, 0, 1), new PageEdge(PageEdgeKind.VERTICAL, 1.0f)};
        r0[29] = new PageEdge[]{new PageEdge(PageEdgeKind.HORIZONTAL, 0.0f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.33f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.66f), new PageEdge(PageEdgeKind.HORIZONTAL, 1.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.33f, 2, 3), new PageEdge(PageEdgeKind.VERTICAL, (float) 0.5f, 0, 2), new PageEdge(PageEdgeKind.VERTICAL, 0.66f, 2, 3), new PageEdge(PageEdgeKind.VERTICAL, 1.0f)};
        r0[30] = new PageEdge[]{new PageEdge(PageEdgeKind.HORIZONTAL, 0.0f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.33f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.66f), new PageEdge(PageEdgeKind.HORIZONTAL, 1.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.33f, 1, 2), new PageEdge(PageEdgeKind.VERTICAL, (float) 0.5f, 0, 1), new PageEdge(PageEdgeKind.VERTICAL, (float) 0.5f, 2, 3), new PageEdge(PageEdgeKind.VERTICAL, 0.66f, 1, 2), new PageEdge(PageEdgeKind.VERTICAL, 1.0f)};
        r0[31] = new PageEdge[]{new PageEdge(PageEdgeKind.HORIZONTAL, 0.0f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.33f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.66f), new PageEdge(PageEdgeKind.HORIZONTAL, 1.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.0f), new PageEdge(PageEdgeKind.VERTICAL, (float) 0.5f, 0, 1), new PageEdge(PageEdgeKind.VERTICAL, (float) 0.5f, 2, 3), new PageEdge(PageEdgeKind.VERTICAL, 1.0f)};
        r0[32] = new PageEdge[]{new PageEdge(PageEdgeKind.HORIZONTAL, 0.0f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.4f), new PageEdge(PageEdgeKind.HORIZONTAL, 1.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.33f, 0, 1), new PageEdge(PageEdgeKind.VERTICAL, (float) 0.5f, 1, 2), new PageEdge(PageEdgeKind.VERTICAL, 0.66f, 0, 1), new PageEdge(PageEdgeKind.VERTICAL, 1.0f)};
        r0[33] = new PageEdge[]{new PageEdge(PageEdgeKind.HORIZONTAL, 0.0f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.27f), new PageEdge(PageEdgeKind.HORIZONTAL, 1.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.0f), new PageEdge(PageEdgeKind.VERTICAL, (float) 0.5f, 0, 1), new PageEdge(PageEdgeKind.VERTICAL, 1.0f)};
        r0[34] = new PageEdge[]{new PageEdge(PageEdgeKind.HORIZONTAL, 0.0f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.33f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.66f), new PageEdge(PageEdgeKind.HORIZONTAL, 1.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.0f), new PageEdge(PageEdgeKind.VERTICAL, (float) 0.5f, 1, 3), new PageEdge(PageEdgeKind.VERTICAL, 1.0f)};
        r0[35] = new PageEdge[]{new PageEdge(PageEdgeKind.HORIZONTAL, 0.0f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.33f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.66f), new PageEdge(PageEdgeKind.HORIZONTAL, 1.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.0f), new PageEdge(PageEdgeKind.VERTICAL, (float) 0.5f, 0, 2), new PageEdge(PageEdgeKind.VERTICAL, 1.0f)};
        r0[36] = new PageEdge[]{new PageEdge(PageEdgeKind.HORIZONTAL, 0.0f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.66f), new PageEdge(PageEdgeKind.HORIZONTAL, 1.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.33f, 1, 2), new PageEdge(PageEdgeKind.VERTICAL, (float) 0.5f, 0, 1), new PageEdge(PageEdgeKind.VERTICAL, 0.66f, 1, 2), new PageEdge(PageEdgeKind.VERTICAL, 1.0f)};
        r0[37] = new PageEdge[]{new PageEdge(PageEdgeKind.HORIZONTAL, 0.0f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.33f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.66f), new PageEdge(PageEdgeKind.HORIZONTAL, 1.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.33f, 0, 1), new PageEdge(PageEdgeKind.VERTICAL, 0.33f, 2, 3), new PageEdge(PageEdgeKind.VERTICAL, (float) 0.5f, 1, 2), new PageEdge(PageEdgeKind.VERTICAL, 0.66f, 0, 1), new PageEdge(PageEdgeKind.VERTICAL, 0.66f, 2, 3), new PageEdge(PageEdgeKind.VERTICAL, 1.0f)};
        r0[38] = new PageEdge[]{new PageEdge(PageEdgeKind.HORIZONTAL, 0.0f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.33f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.66f), new PageEdge(PageEdgeKind.HORIZONTAL, 1.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.33f, 0, 1), new PageEdge(PageEdgeKind.VERTICAL, 0.33f, 2, 3), new PageEdge(PageEdgeKind.VERTICAL, 0.66f, 0, 1), new PageEdge(PageEdgeKind.VERTICAL, 0.66f, 2, 3), new PageEdge(PageEdgeKind.VERTICAL, 1.0f)};
        r0[39] = new PageEdge[]{new PageEdge(PageEdgeKind.HORIZONTAL, 0.0f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.25f), new PageEdge(PageEdgeKind.HORIZONTAL, (float) 0.5f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.75f), new PageEdge(PageEdgeKind.HORIZONTAL, 1.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.0f), new PageEdge(PageEdgeKind.VERTICAL, (float) 0.5f, 0, 1), new PageEdge(PageEdgeKind.VERTICAL, (float) 0.5f, 2, 3), new PageEdge(PageEdgeKind.VERTICAL, 1.0f)};
        r0[40] = new PageEdge[]{new PageEdge(PageEdgeKind.HORIZONTAL, 0.0f), new PageEdge(PageEdgeKind.HORIZONTAL_ANGLED, new float[]{0.34f, 0.38f}), new PageEdge(PageEdgeKind.HORIZONTAL, 1.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.25f, 1, 2), new PageEdge(PageEdgeKind.VERTICAL, (float) 0.5f, 1, 2), new PageEdge(PageEdgeKind.VERTICAL, 0.75f, 1, 2), new PageEdge(PageEdgeKind.VERTICAL, 1.0f)};
        r0[41] = new PageEdge[]{new PageEdge(PageEdgeKind.HORIZONTAL, 0.0f), new PageEdge(PageEdgeKind.HORIZONTAL_ANGLED, new float[]{0.4f, 0.45f}), new PageEdge(PageEdgeKind.HORIZONTAL, 1.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.64f, 1, 2), new PageEdge(PageEdgeKind.VERTICAL, 1.0f)};
        r0[42] = new PageEdge[]{new PageEdge(PageEdgeKind.HORIZONTAL, 0.0f), new PageEdge(PageEdgeKind.HORIZONTAL_ANGLED, new float[]{0.27f, 0.42f}), new PageEdge(PageEdgeKind.HORIZONTAL, 1.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.0f), new PageEdge(PageEdgeKind.VERTICAL, 1.0f)};
        r0[43] = new PageEdge[]{new PageEdge(PageEdgeKind.HORIZONTAL, 0.0f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.33f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.66f), new PageEdge(PageEdgeKind.HORIZONTAL, 1.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.66f, 1, 2), new PageEdge(PageEdgeKind.VERTICAL, 1.0f)};
        r0[44] = new PageEdge[]{new PageEdge(PageEdgeKind.HORIZONTAL, 0.0f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.23f, 2, 3), new PageEdge(PageEdgeKind.HORIZONTAL, 0.6f), new PageEdge(PageEdgeKind.HORIZONTAL, 1.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.42f, 2, 3), new PageEdge(PageEdgeKind.VERTICAL, 0.56f, 0, 2), new PageEdge(PageEdgeKind.VERTICAL, 1.0f)};
        r0[45] = new PageEdge[]{new PageEdge(PageEdgeKind.HORIZONTAL, 0.0f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.25f), new PageEdge(PageEdgeKind.HORIZONTAL, (float) 0.5f), new PageEdge(PageEdgeKind.HORIZONTAL, 1.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.32f, 0, 1), new PageEdge(PageEdgeKind.VERTICAL, 1.0f)};
        r0[46] = new PageEdge[]{new PageEdge(PageEdgeKind.HORIZONTAL, 0.0f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.34f, 1, 3), new PageEdge(PageEdgeKind.HORIZONTAL, 1.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.34f), new PageEdge(PageEdgeKind.VERTICAL, 0.66f, 0, 1), new PageEdge(PageEdgeKind.VERTICAL, 1.0f)};
        r0[47] = new PageEdge[]{new PageEdge(PageEdgeKind.HORIZONTAL, 0.0f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.33f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.66f), new PageEdge(PageEdgeKind.HORIZONTAL, 1.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.0f), new PageEdge(PageEdgeKind.VERTICAL_ANGLED, new float[]{0.38f, 0.26f}, 2, 3), new PageEdge(PageEdgeKind.VERTICAL_ANGLED, new float[]{0.72f, 0.66f}, 2, 3), new PageEdge(PageEdgeKind.VERTICAL, 1.0f)};
        r0[48] = new PageEdge[]{new PageEdge(PageEdgeKind.HORIZONTAL, 0.0f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.33f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.66f), new PageEdge(PageEdgeKind.HORIZONTAL, 1.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.33f, 1, 2), new PageEdge(PageEdgeKind.VERTICAL, (float) 0.5f, 0, 1), new PageEdge(PageEdgeKind.VERTICAL, 0.66f, 1, 2), new PageEdge(PageEdgeKind.VERTICAL, 1.0f)};
        r0[49] = new PageEdge[]{new PageEdge(PageEdgeKind.HORIZONTAL, 0.0f), new PageEdge(PageEdgeKind.HORIZONTAL_ANGLED, new float[]{0.47f, 0.5f}), new PageEdge(PageEdgeKind.HORIZONTAL, 1.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.36f, 0, 1), new PageEdge(PageEdgeKind.VERTICAL, 1.0f)};
        r0[50] = new PageEdge[]{new PageEdge(PageEdgeKind.HORIZONTAL, 0.0f), new PageEdge(PageEdgeKind.HORIZONTAL_ANGLED, new float[]{0.48f, 0.44f}), new PageEdge(PageEdgeKind.HORIZONTAL, 1.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.25f, 1, 2), new PageEdge(PageEdgeKind.VERTICAL, (float) 0.5f), new PageEdge(PageEdgeKind.VERTICAL, 0.75f, 1, 2), new PageEdge(PageEdgeKind.VERTICAL, 1.0f)};
        r0[51] = new PageEdge[]{new PageEdge(PageEdgeKind.HORIZONTAL, 0.0f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.42f, 1, 3), new PageEdge(PageEdgeKind.HORIZONTAL, 0.7f, 1, 3), new PageEdge(PageEdgeKind.HORIZONTAL, 1.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.34f), new PageEdge(PageEdgeKind.VERTICAL, 0.67f, 0, 1), new PageEdge(PageEdgeKind.VERTICAL, 1.0f)};
        r0[52] = new PageEdge[]{new PageEdge(PageEdgeKind.HORIZONTAL, 0.0f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.28f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.56f), new PageEdge(PageEdgeKind.HORIZONTAL, 1.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.0f), new PageEdge(PageEdgeKind.VERTICAL, 1.0f)};
        r0[53] = new PageEdge[]{new PageEdge(PageEdgeKind.HORIZONTAL, 0.0f), new PageEdge(PageEdgeKind.HORIZONTAL, (float) 0.5f, 1, 2), new PageEdge(PageEdgeKind.HORIZONTAL, 1.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.56f), new PageEdge(PageEdgeKind.VERTICAL, 1.0f)};
        r0[54] = new PageEdge[]{new PageEdge(PageEdgeKind.HORIZONTAL, 0.0f), new PageEdge(PageEdgeKind.HORIZONTAL, 1.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.0f), new PageEdge(PageEdgeKind.VERTICAL, (float) 0.5f), new PageEdge(PageEdgeKind.VERTICAL, 1.0f)};
        r0[55] = new PageEdge[]{new PageEdge(PageEdgeKind.HORIZONTAL, 0.0f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.17f, 0, 1), new PageEdge(PageEdgeKind.HORIZONTAL, 0.35f, 0, 1), new PageEdge(PageEdgeKind.HORIZONTAL, 0.52f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.78f, 0, 2), new PageEdge(PageEdgeKind.HORIZONTAL, 1.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.38f, 0, 3), new PageEdge(PageEdgeKind.VERTICAL, 0.62f), new PageEdge(PageEdgeKind.VERTICAL, 1.0f)};
        r0[56] = new PageEdge[]{new PageEdge(PageEdgeKind.HORIZONTAL, 0.0f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.25f), new PageEdge(PageEdgeKind.HORIZONTAL, (float) 0.5f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.75f), new PageEdge(PageEdgeKind.HORIZONTAL, 1.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.3f, 0, 1), new PageEdge(PageEdgeKind.VERTICAL, (float) 0.5f, 1, 2), new PageEdge(PageEdgeKind.VERTICAL, (float) 0.5f, 3, 4), new PageEdge(PageEdgeKind.VERTICAL, 0.7f, 2, 3), new PageEdge(PageEdgeKind.VERTICAL, 1.0f)};
        r0[57] = new PageEdge[]{new PageEdge(PageEdgeKind.HORIZONTAL, 0.0f), new PageEdge(PageEdgeKind.HORIZONTAL_ANGLED, new float[]{0.35f, 0.36f}), new PageEdge(PageEdgeKind.HORIZONTAL_ANGLED, 0.7f), new PageEdge(PageEdgeKind.HORIZONTAL, 1.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.0f), new PageEdge(PageEdgeKind.VERTICAL_ANGLED, new float[]{0.22f, 0.32f}, 2, 3), new PageEdge(PageEdgeKind.VERTICAL_ANGLED, new float[]{0.71f, 0.65f}, 2, 3), new PageEdge(PageEdgeKind.VERTICAL, 1.0f)};
        r0[58] = new PageEdge[]{new PageEdge(PageEdgeKind.HORIZONTAL, 0.0f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.25f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.75f), new PageEdge(PageEdgeKind.HORIZONTAL, 1.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.0f), new PageEdge(PageEdgeKind.VERTICAL, 1.0f)};
        r0[59] = new PageEdge[]{new PageEdge(PageEdgeKind.HORIZONTAL, 0.0f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.3f, 2, 3), new PageEdge(PageEdgeKind.HORIZONTAL, 0.6f), new PageEdge(PageEdgeKind.HORIZONTAL, 0.75f, 0, 1), new PageEdge(PageEdgeKind.HORIZONTAL, 0.88f, 0, 1), new PageEdge(PageEdgeKind.HORIZONTAL, 1.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.0f), new PageEdge(PageEdgeKind.VERTICAL, 0.41f, 2, 5), new PageEdge(PageEdgeKind.VERTICAL, 0.57f, 0, 2), new PageEdge(PageEdgeKind.VERTICAL, 1.0f)};
        layouts = r0;
    }

    public static int getNextLayoutIndex(int index) {
        if (!(hasShuffled && shuffledLayouts.length == layouts.length)) {
            hasShuffled = true;
            shuffledLayouts = ComicUtils.shuffleArrayOfSize(layouts.length);
        }
        return shuffledLayouts[index];
    }

    private static ArrayList<Integer> emptyMergeList() {
        return emptyMergeList;
    }
}
