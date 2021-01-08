package com.mp.android.apps.livevblank.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;


import androidx.appcompat.widget.AppCompatRadioButton;

public class CenterRadioButton extends AppCompatRadioButton {
    public CenterRadioButton(Context context) {
        super(context);
    }

    public CenterRadioButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CenterRadioButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Drawable[] drawables = getCompoundDrawables();
        if (drawables != null) {
            Drawable drawableLeft = drawables[0];
            if (drawableLeft != null) {
                float bodyWidth = (((float) drawableLeft.getIntrinsicWidth()) + getPaint().measureText(getText().toString())) + ((float) getCompoundDrawablePadding());
                setPadding(0, 0, (int) (((float) getWidth()) - bodyWidth), 0);
                canvas.translate((((float) getWidth()) - bodyWidth) / 2.0f, 0.0f);
            }
        }
        super.onDraw(canvas);
    }
}
