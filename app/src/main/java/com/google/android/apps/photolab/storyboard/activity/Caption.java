package com.google.android.apps.photolab.storyboard.activity;

import android.content.Context;
import android.graphics.Paint.Align;
import android.graphics.Typeface;
import androidx.core.view.ViewCompat;
import android.text.TextPaint;
import android.widget.TextView;

public class Caption implements IComicMoveable {
    private String text;
    private TextPaint textPaint;
    private TextView textView;
    private Typeface typeface;

    public Caption(String text) {
        this.text = text;
    }

    public ComicMoveableKind getMoveableKind() {
        return ComicMoveableKind.CAPTION;
    }

    public TextView getTextView(Context context) {
        if (!(this.textView != null || this.text == null || this.text.isEmpty())) {
            this.textView = new TextView(ComicActivity.getContext());
            this.textView.setText(this.text);
        }
        return this.textView;
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
        if (this.text == null || this.text.isEmpty()) {
            this.textView = null;
        }
    }

    public TextPaint getTextPaint() {
        if (this.textPaint == null) {
            this.typeface = Typeface.createFromAsset(ComicActivity.getContext().getAssets(), "fonts/CantedComicRegular.ttf");
            this.textPaint = new TextPaint();
            this.textPaint.setColor(ViewCompat.MEASURED_STATE_MASK);
            this.textPaint.setTextSize(30.0f);
            this.textPaint.setAntiAlias(true);
            this.textPaint.setTextAlign(Align.LEFT);
            this.textPaint.setTypeface(this.typeface);
        }
        return this.textPaint;
    }
}
