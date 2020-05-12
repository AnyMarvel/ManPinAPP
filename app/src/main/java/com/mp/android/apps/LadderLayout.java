package com.mp.android.apps;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;

import android.graphics.RectF;
import android.graphics.Region;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.mp.android.apps.R;
import com.mp.android.apps.livevblank.util.ScreenUtil;

import static com.mp.android.apps.livevblank.util.ScreenUtil.getStatusBarHeight;


/**
 * Created by Aislli on 2017/12/26 0026.
 */

public class LadderLayout extends RelativeLayout {

    private Paint paint;
    private Region mRegion;
    private Path mDrawPath;
    private float topWidthrate;
    private float bottomWidthrate;
    private float verticalHeightRate;
    private boolean leftRect;
    private boolean rightRect;

    private final int strokWidth = 2;

    private float leftHeihhtRate;
    private float rightHeightRate;
    private float horizontalweigthrate;


    private PorterDuffXfermode porterDuffXfermodeIn;
    private PorterDuffXfermode porterDuffXfermodeOut;
    private int mDisPlayWidth;
    private int mDisPlayHeight;

    private int layoutHeight;
    private int layoutWeight;

    public LadderLayout(Context context) {
        this(context, null);
    }

    public LadderLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LadderLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(displayMetrics);
        mDisPlayWidth = displayMetrics.widthPixels - ScreenUtil.dp2px(context, 31);
        mDisPlayHeight = displayMetrics.heightPixels + getStatusBarHeight(context) + ScreenUtil.getNavigationBarHeight(context);


        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);//设置STROKE才能设置成圆角
        paint.setStrokeWidth(strokWidth);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setColor(Color.BLACK);

        porterDuffXfermodeIn = new PorterDuffXfermode(PorterDuff.Mode.DST_IN);
        porterDuffXfermodeOut = new PorterDuffXfermode(PorterDuff.Mode.DST_OUT);
        setWillNotDraw(false);


        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.LadderLayout, defStyleAttr, 0);

        topWidthrate = ta.getFloat(R.styleable.LadderLayout_topWidthrate, 0);
        bottomWidthrate = ta.getFloat(R.styleable.LadderLayout_bottomWidthrate, 0);
        verticalHeightRate = ta.getFloat(R.styleable.LadderLayout_verticalheightrate, 0);


        leftHeihhtRate = ta.getFloat(R.styleable.LadderLayout_leftheightrate, 0);
        rightHeightRate = ta.getFloat(R.styleable.LadderLayout_rightheightrate, 0);
        horizontalweigthrate = ta.getFloat(R.styleable.LadderLayout_horizontalweigthrate, 0);
        leftRect = ta.getBoolean(R.styleable.LadderLayout_leftRect, false);
        rightRect = ta.getBoolean(R.styleable.LadderLayout_rightRect, false);

        ta.recycle();


        mRegion = new Region();
        mDrawPath = new Path();


        if (verticalHeightRate != 0) {
            if (topWidthrate > bottomWidthrate) {
                mDrawPath.moveTo(0, 0);
                mDrawPath.rLineTo(topWidthrate * mDisPlayWidth, 0);
                mDrawPath.lineTo(bottomWidthrate * mDisPlayWidth + (topWidthrate - bottomWidthrate) * mDisPlayWidth / (rightRect ? 1 : 2), verticalHeightRate * mDisPlayHeight);
                mDrawPath.lineTo((leftRect ? 0 : (topWidthrate - bottomWidthrate) * mDisPlayWidth / 2), verticalHeightRate * mDisPlayHeight);
                mDrawPath.close();
            } else {
                mDrawPath.moveTo((leftRect ? 0 : (bottomWidthrate * mDisPlayWidth - topWidthrate * mDisPlayWidth) / 2), 0);
                mDrawPath.lineTo(topWidthrate * mDisPlayWidth + (bottomWidthrate * mDisPlayWidth - topWidthrate * mDisPlayWidth) / (rightRect ? 1 : 2) + 0, 0);
                mDrawPath.lineTo(bottomWidthrate * mDisPlayWidth, verticalHeightRate * mDisPlayHeight);
                mDrawPath.lineTo(0, verticalHeightRate * mDisPlayHeight);
                mDrawPath.close();
            }
            layoutHeight = (int) (verticalHeightRate * mDisPlayHeight);
            layoutWeight = (int) Math.max(topWidthrate * mDisPlayWidth, bottomWidthrate * mDisPlayWidth);
        }

        if (horizontalweigthrate != 0) {
            if (leftHeihhtRate > rightHeightRate) {
                mDrawPath.moveTo(0, 0);
                mDrawPath.lineTo(horizontalweigthrate * mDisPlayWidth, 0);
                mDrawPath.lineTo(horizontalweigthrate * mDisPlayWidth, rightHeightRate * mDisPlayHeight);
                mDrawPath.lineTo(0, leftHeihhtRate * mDisPlayHeight);
                mDrawPath.close();
            } else {
                mDrawPath.moveTo(0, rightHeightRate * mDisPlayHeight - leftHeihhtRate * mDisPlayHeight);
                mDrawPath.lineTo(horizontalweigthrate * mDisPlayWidth, 0);
                mDrawPath.lineTo(horizontalweigthrate * mDisPlayWidth, rightHeightRate * mDisPlayHeight);
                mDrawPath.lineTo(0, rightHeightRate * mDisPlayHeight);
                mDrawPath.close();
            }
            layoutHeight = (int) (leftHeihhtRate > rightHeightRate ? leftHeihhtRate * mDisPlayHeight : rightHeightRate * mDisPlayHeight);
            layoutWeight = (int) (horizontalweigthrate * mDisPlayWidth);
        }

    }


    ImageView imageView;

    public ImageView getImageView() {
        return imageView;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measuredWidth = layoutWeight + strokWidth;
        int measuredHeight = layoutHeight + strokWidth;
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            // 为每一个子控件测量大小
            measureChild(childView, widthMeasureSpec, heightMeasureSpec);
            if (childView instanceof ImageView) {
                imageView = (ImageView) childView;
                ViewGroup.LayoutParams layoutParams = childView.getLayoutParams();
                layoutParams.width = measuredWidth;
                layoutParams.height = measuredHeight;
            }
        }
        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    @Override
    public void draw(Canvas canvas) {

        int saved = canvas.saveLayer(null, null, Canvas.ALL_SAVE_FLAG);
        super.draw(canvas);
        Paint paintStroke = new Paint();
        paintStroke.setStyle(Paint.Style.STROKE);
        paintStroke.setStrokeWidth(3);

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O_MR1) {
            paint.setXfermode(porterDuffXfermodeIn);
            canvas.drawPath(mDrawPath, paint);
            canvas.drawPath(mDrawPath, paintStroke);
        } else {
            paint.setXfermode(porterDuffXfermodeOut);
            if (!mDrawPath.isInverseFillType()) {
                mDrawPath.toggleInverseFillType();
            }
            canvas.drawPath(mDrawPath, paint);
            mDrawPath.toggleInverseFillType();
            canvas.drawPath(mDrawPath, paintStroke);
        }


        canvas.restoreToCount(saved);

    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (!isInRect(event)) {
                return false;
            }
        }
        return super.dispatchTouchEvent(event);
    }

    public boolean isInRect(MotionEvent event) {
        if (mDrawPath.isInverseFillType()) {
            mDrawPath.toggleInverseFillType();
        }
        RectF rectF = new RectF();
        mDrawPath.computeBounds(rectF, true);
        mRegion.setPath(mDrawPath, new Region((int) rectF.left, (int) rectF.top, (int) rectF.right, (int) rectF.bottom));
        return mRegion.contains((int) event.getX(), (int) event.getY());
    }


}
