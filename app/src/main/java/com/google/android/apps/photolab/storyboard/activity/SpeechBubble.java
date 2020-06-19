package com.google.android.apps.photolab.storyboard.activity;

import android.content.Context;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.Typeface;
import androidx.core.view.ViewCompat;
import android.text.TextPaint;
import android.widget.TextView;

public class SpeechBubble implements IComicMoveable {
    private static TextPaint textPaint;
    private static Typeface typeface;
    float bubbleRoundness;
    ComicGenerator page;
    float speechTickLength;
    float speechTickWidth;
    private String text;
    Rect textArea = new Rect();
    private TextView textView;

    public SpeechBubble(String text) {
        this.text = text;
    }

    public TextView getTextView(Context context) {
        if (!(this.textView != null || this.text == null || this.text.isEmpty())) {
            this.textView = new TextView(ComicActivity.getContext());
            this.textView.setTypeface(typeface);
            this.textView.setWidth(this.textArea.width());
            this.textView.setHeight(this.textArea.height());
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

    public ComicMoveableKind getMoveableKind() {
        return ComicMoveableKind.SPEECH_BUBBLE;
    }

    public static TextPaint getTextPaint() {
        if (textPaint == null) {
            typeface = Typeface.createFromAsset(ComicActivity.getContext().getAssets(), "fonts/CantedComicBold.ttf");
            textPaint = new TextPaint();
            textPaint.setColor(ViewCompat.MEASURED_STATE_MASK);
            textPaint.setTextSize(40.0f);
            textPaint.setAntiAlias(true);
            textPaint.setTextAlign(Align.CENTER);
            textPaint.setTypeface(typeface);
        }
        return textPaint;
    }

    public static Typeface getTypeface() {
        getTextPaint();
        return typeface;
    }
}
