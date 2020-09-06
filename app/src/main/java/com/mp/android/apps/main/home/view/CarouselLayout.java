package com.mp.android.apps.main.home.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class CarouselLayout extends RelativeLayout {
    private Paint paint;
    Path mDrawPath;
    private PorterDuffXfermode porterDuffXfermodeIn;
    private PorterDuffXfermode porterDuffXfermodeOut;

    public CarouselLayout(Context context) {
        this(context, null);
    }

    public CarouselLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CarouselLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        initView();
    }


    private void initView() {
        paint = new Paint();
        mDrawPath = new Path();
        porterDuffXfermodeIn = new PorterDuffXfermode(PorterDuff.Mode.DST_IN);
        porterDuffXfermodeOut = new PorterDuffXfermode(PorterDuff.Mode.DST_OUT);
        setWillNotDraw(false);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            // 为每一个子控件测量大小
            measureChild(childView, widthMeasureSpec, heightMeasureSpec);
            if (childView instanceof ImageView) {
                ViewGroup.LayoutParams layoutParams = childView.getLayoutParams();
                layoutParams.width = getMeasuredWidth();
                layoutParams.height = getMeasuredHeight();
            }
        }
        setMeasuredDimension(getMeasuredWidth(), getMeasuredHeight());
    }

    @Override
    public void draw(Canvas canvas) {
        int saved = canvas.saveLayer(null, null, Canvas.ALL_SAVE_FLAG);
        super.draw(canvas);
        mDrawPath.moveTo(0, 0);
        mDrawPath.lineTo(getMeasuredWidth(), 0);
        mDrawPath.lineTo(getMeasuredWidth(), getMeasuredHeight() - 100);
        mDrawPath.quadTo(getMeasuredWidth() / 2, getMeasuredHeight() + 100, 0, getMeasuredHeight() - 100);
        mDrawPath.close();

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O_MR1) {
            paint.setXfermode(porterDuffXfermodeIn);
        } else {
            paint.setXfermode(porterDuffXfermodeOut);
            if (!mDrawPath.isInverseFillType()) {
                mDrawPath.toggleInverseFillType();
            }
        }
        canvas.drawPath(mDrawPath, paint);
        canvas.restoreToCount(saved);
    }
}
