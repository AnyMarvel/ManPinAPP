package com.google.android.apps.photolab.storyboard.activity;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

public class LoadingView extends View {
    private static final String TAG = "LoadingView";
    private Paint arcPaint;
    private RectF arcRect;
    private Handler mHandler;
    private final Runnable mRunnable = new Runnable() {
        public void run() {
            if (LoadingView.this.getVisibility() == View.VISIBLE) {
                LoadingView.this.invalidate();
                LoadingView.this.mHandler.postDelayed(LoadingView.this.mRunnable, 16);
            }
        }
    };
    private float rotation = 0.0f;

    public LoadingView(Context context) {
        super(context);
        initialize();
    }

    public LoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public LoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    public LoadingView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize();
    }

    private void initialize() {
        this.arcPaint = new Paint();
        this.arcPaint.setStyle(Style.STROKE);
        this.arcPaint.setColor(Color.LTGRAY);
        this.arcPaint.setStrokeWidth(7.0f);
    }

    public void resetAndRun(int milliseconds) {
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == 0) {
            int w = getWidth();
            int h = getHeight();
            int cx = (int) (((double) w) / 2.0d);
            int cy = (int) (((double) h) / 2.0d);
            int r = (int) (((double) h) * 0.3d);
            this.arcRect = new RectF((float) (cx - r), (float) (cy - r), (float) (cx + r), (float) (cy + r));
            this.mHandler = new Handler();
            this.mHandler.postDelayed(this.mRunnable, 16);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawArc(this.arcRect, this.rotation, 90.0f, false, this.arcPaint);
        canvas.drawArc(this.arcRect, 120.0f + this.rotation, 90.0f, false, this.arcPaint);
        canvas.drawArc(this.arcRect, 240.0f + this.rotation, 90.0f, false, this.arcPaint);
        this.rotation = (this.rotation + 5.0f) % 360.0f;
    }
}
