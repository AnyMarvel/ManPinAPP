package com.google.android.apps.photolab.storyboard.pipeline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class ObjectDetectionSet {
    private static final String TAG = "ObjectDetectionSet";
    private ArrayList<ObjectDetection> detections;
    private int imageHeight;
    private int imageWidth;

    public ObjectDetectionSet(int imageWidth, int imageHeight) {
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.detections = new ArrayList();
    }

    public ObjectDetectionSet(ObjectDetection[] detections, int imageWidth, int imageHeight) {
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.detections = new ArrayList(Arrays.asList(detections));
    }

    public int size() {
        return this.detections.size();
    }

    public ObjectDetection get(int index) {
        return (ObjectDetection) this.detections.get(index);
    }

    public void addDetection(ObjectDetection detection) {
        this.detections.add(detection);
    }

    public ArrayList<ObjectDetection> getDetections() {
        return this.detections;
    }

    public void setDetectionsWithArray(ObjectDetection[] detections) {
        ObjectDetection detection;
        int i = 0;
        this.detections.clear();
        for (ObjectDetection detection2 : detections) {
            if (detection2.getArea() > 0.0f) {
                this.detections.add(detection2);
            }
        }
        int length = detections.length;
        while (i < length) {
            detection = detections[i];
            detection.imageWidth = this.imageWidth;
            detection.imageHeight = this.imageHeight;
            detection.ensureMinimumSize();
            i++;
        }
    }

    public ArrayList<ObjectDetection> getFaces() {
        ArrayList<ObjectDetection> result = new ArrayList();
        Iterator it = this.detections.iterator();
        while (it.hasNext()) {
            ObjectDetection detection = (ObjectDetection) it.next();
            if (detection.isFace) {
                result.add(detection);
            }
        }
        return result;
    }

    public static ObjectDetection getLargestDetection(ArrayList<ObjectDetection> dets) {
        ObjectDetection result = null;
        float max = 0.0f;
        Iterator it = dets.iterator();
        while (it.hasNext()) {
            ObjectDetection detection = (ObjectDetection) it.next();
            float area = detection.getArea();
            if (area > max) {
                result = detection;
                max = area;
            }
        }
        return result;
    }

    public static ObjectDetection getMostCentered(ArrayList<ObjectDetection> dets, float maxArea) {
        ObjectDetection result = null;
        ObjectDetection backupResult = null;
        float min = Float.MAX_VALUE;
        Iterator it = dets.iterator();
        while (it.hasNext()) {
            ObjectDetection detection = (ObjectDetection) it.next();
            float dist = detection.getSquaredDistanceFromCenter();
            if (dist < min) {
                backupResult = detection;
                min = dist;
                if (detection.getArea() < maxArea) {
                    result = detection;
                }
            }
        }
        return (result == null || ((double) result.getArea()) < 0.05d) ? backupResult : result;
    }

    public static void rateByCentered(ArrayList<ObjectDetection> detections, float maxArea, float[] scores) {
        for (int i = 0; i < detections.size(); i++) {
            ObjectDetection detection = (ObjectDetection) detections.get(i);
            float distScore = 1.0f - detection.getDistanceFromCenter();
            float areaScore = detection.getArea() / ((float) (detection.imageWidth * detection.imageHeight));
            if (areaScore > maxArea) {
                areaScore = 0.0f;
            }
            scores[i] = scores[i] + ((areaScore + distScore) * 2.0f);
        }
    }

    public static void rateByFaces(ArrayList<ObjectDetection> detections, float faceCount, float[] scores) {
        for (int i = 0; i < detections.size(); i++) {
            if (((ObjectDetection) detections.get(i)).isFace) {
                scores[i] = scores[i] + (3.0f / (1.0f + faceCount));
            }
        }
    }

    public static void rateByAspect(ArrayList<ObjectDetection> detections, float frameAspect, float[] scores) {
        for (int i = 0; i < detections.size(); i++) {
            scores[i] = scores[i] + (Math.abs(((ObjectDetection) detections.get(i)).getAspect() - frameAspect) * 4.0f);
        }
    }

    public static void rateByArea(ArrayList<ObjectDetection> detections, float frameArea, float[] scores) {
        for (int i = 0; i < detections.size(); i++) {
            scores[i] = scores[i] + (Math.abs(((ObjectDetection) detections.get(i)).getArea() / frameArea) * 0.0f);
        }
    }

    public int getImageWidth() {
        return this.imageWidth;
    }

    public void setImageWidth(int imageWidth) {
        this.imageWidth = imageWidth;
    }

    public int getImageHeight() {
        return this.imageHeight;
    }

    public void setImageHeight(int imageHeight) {
        this.imageHeight = imageHeight;
    }
}
