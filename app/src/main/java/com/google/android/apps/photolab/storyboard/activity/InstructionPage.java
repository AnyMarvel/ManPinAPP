package com.google.android.apps.photolab.storyboard.activity;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PorterDuff.Mode;
import android.util.AttributeSet;
import android.view.View;

public class InstructionPage extends View {
    public InstructionPage(Context context) {
        super(context);
        initialize();
    }

    public InstructionPage(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public InstructionPage(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    public InstructionPage(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize();
    }

    private void initialize() {
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(-1, Mode.CLEAR);
    }
}
