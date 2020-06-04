package com.mp.android.apps.main.view.bar;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;


public class ShStatusView extends View {

    private int mBarSize;

    public ShStatusView(Context context) {
        this(context, null);

    }

    public ShStatusView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShStatusView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Resources resources = getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if(resourceId>0){
            mBarSize = resources.getDimensionPixelSize(resourceId);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), mBarSize);
        } else {
            setMeasuredDimension(0, 0);
        }
    }

    /**
     * Get status bar height.
     */
    public int getBarSize() {
        return mBarSize;
    }
}
