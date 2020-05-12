package com.google.android.apps.photolab.storyboard.activity;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Region.Op;
import android.os.Build;
import android.support.v4.internal.view.SupportMenu;
import android.support.v4.view.InputDeviceCompat;
import android.support.v4.view.ViewCompat;
import android.util.Log;

import com.google.android.apps.photolab.storyboard.pipeline.MediaManager;

import java.util.ArrayList;
import java.util.Iterator;

public class ComicRenderer {
    private static final String TAG = "ComicRenderer";
    private Paint bkgColor;
    private String debugCategories = "";
    private Paint debugColor;
    private Paint desaturate;
    private Paint faceDebugColor;
    private Paint hudColor;
    private Paint panelColor;
    private Paint panelOutline;

    public ComicRenderer(ComicPresenter comicPresenter) {
        init();
    }

    private void init() {
        this.panelColor = new Paint();
//        Color.parseColor("#ff440065");
        this.panelColor.setColor(-12320666);
        this.panelOutline = new Paint();
        this.panelOutline.setColor(ViewCompat.MEASURED_STATE_MASK);
        this.panelOutline.setStrokeWidth(4.0f);
        this.panelOutline.setStyle(Style.STROKE);
        this.bkgColor = new Paint();
        this.bkgColor.setColor(-1);
        this.hudColor = new Paint();
        this.hudColor.setColor(SupportMenu.CATEGORY_MASK);
        this.hudColor.setStrokeWidth(4.0f);
        this.hudColor.setStyle(Style.STROKE);
        this.debugColor = new Paint();
        this.debugColor.setColor(SupportMenu.CATEGORY_MASK);
        this.debugColor.setStrokeWidth(2.0f);
        this.debugColor.setStyle(Style.STROKE);
        this.faceDebugColor = new Paint();
        this.faceDebugColor.setColor(InputDeviceCompat.SOURCE_ANY);
        this.faceDebugColor.setStrokeWidth(4.0f);
        this.faceDebugColor.setStyle(Style.STROKE);
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0.0f);
        ColorMatrixColorFilter colorFilter = new ColorMatrixColorFilter(colorMatrix);
        this.desaturate = new Paint();
        this.desaturate.setColorFilter(colorFilter);
    }

    protected void onDraw(Canvas canvas, ComicPageData comicPageData) {
        ArrayList<ComicPanel> panels = (ArrayList) comicPageData.getPanels().clone();
        canvas.drawRect(0.0f, 0.0f, (float) canvas.getWidth(), (float) canvas.getHeight(), this.bkgColor);
        String cats = "";
        Iterator it = panels.iterator();
        while (it.hasNext()) {
            String valueOf;
            ComicPanel panel = (ComicPanel) it.next();
            drawPanel(panel, canvas);
            if (ComicActivity.SHOW_DEBUG_INFO) {
                if (panel.detection != null) {
                    valueOf = String.valueOf(cats);
                    int imageIndex = panel.getImageIndex();
                    String str = panel.detection.category;
                    cats = new StringBuilder((String.valueOf(valueOf).length() + 13) + String.valueOf(str).length()).append(valueOf).append(" ").append(imageIndex).append(":").append(str).toString();
                } else {
                    valueOf = String.valueOf(cats);
                    cats = new StringBuilder(String.valueOf(valueOf).length() + 15).append(valueOf).append(" ").append(panel.getImageIndex()).append(": -").toString();
                }
            }
        }
        if (ComicActivity.SHOW_DEBUG_INFO && !cats.equals(this.debugCategories)) {
            String valueOf = TAG;
            String str2 = "comic categories: ";
            String valueOf2 = String.valueOf(cats);
            Log.i(valueOf, valueOf2.length() != 0 ? str2.concat(valueOf2) : new String(str2));
            this.debugCategories = cats;
        }
    }

    private void drawPanel(ComicPanel panel, Canvas canvas) {
        RectF panelRect = new RectF(panel.getPanelFrame());
        this.panelColor.setColor(panel.backgroundColor);
        canvas.drawPath(panel.getPath(), this.panelColor);
        drawPanelBitmapIntoRect(panel, panelRect, canvas);
        canvas.drawPath(panel.getInnerPath(), this.panelOutline);
    }

    private void drawPanelBitmapIntoRect(ComicPanel panel, RectF rect, Canvas canvas) {
        Bitmap bmp = MediaManager.instance().getImageByIndex(panel.getImageIndex());
        if (bmp != null) {
            canvas.save();
            try {
                //适配android P canvas.clipPath relace被弃用
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    canvas.clipPath(panel.getPath());
                } else {
                    canvas.clipPath(panel.getPath(), Op.REPLACE);
                }
            } catch (Exception e) {
                canvas.clipPath(panel.getPath());
            }

            Matrix m = new Matrix();
            float scale = panel.fillScale() * panel.getZoom();
            m.preScale(scale, scale, 0.0f, 0.0f);
            m.postTranslate((rect.left + ((float) panel.getBitmapOffsetX())) + ((float) panel.getBitmapMergeOffsetX()), (rect.top + ((float) panel.getBitmapOffsetY())) + ((float) panel.getBitmapMergeOffsetY()));
            canvas.drawBitmap(bmp, m, null);
            if (ComicActivity.SHOW_DEBUG_INFO && panel.detection != null) {
                RectF bmpRect = new RectF(0.0f, 0.0f, (float) bmp.getWidth(), (float) bmp.getHeight());
                m.mapRect(bmpRect);
                canvas.drawRect(panel.detection.getScaledRect(bmpRect), panel.detection.isFace ? this.faceDebugColor : this.debugColor);
            }
            canvas.restore();
        }
    }
}
