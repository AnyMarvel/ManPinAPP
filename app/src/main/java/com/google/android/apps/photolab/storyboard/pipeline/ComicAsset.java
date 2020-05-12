package com.google.android.apps.photolab.storyboard.pipeline;

import android.graphics.Bitmap;
import com.google.protos.humansensing.FacesProtos.Faces;
import java.util.ArrayList;
import java.util.Iterator;

public class ComicAsset {
    private ObjectDetectionSet detectedObjects;
    private Bitmap originalBitmap;
    private Faces rawFaces;

    public ComicAsset(Bitmap originalBitmap) {
        this.originalBitmap = originalBitmap;
        this.detectedObjects = new ObjectDetectionSet(originalBitmap.getWidth(), originalBitmap.getHeight());
    }

    public Bitmap getBitmap() {
        return this.originalBitmap;
    }

    public void addDetections(ObjectDetectionSet detections) {
        Iterator it = detections.getDetections().iterator();
        while (it.hasNext()) {
            this.detectedObjects.addDetection((ObjectDetection) it.next());
        }
    }

    public ObjectDetectionSet getDetectedObjects() {
        return this.detectedObjects;
    }

    public ArrayList<ObjectDetection> getDetectedFaces() {
        return this.detectedObjects.getFaces();
    }

    public Faces getRawFaces() {
        return this.rawFaces;
    }

    public void setRawFaces(Faces faces) {
        this.rawFaces = faces;
    }
}
