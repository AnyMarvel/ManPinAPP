package com.mp.android.apps.main.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mp.android.apps.R;


public class MyImageTextViewNew extends LinearLayout {
    private ImageView mImageView = null;
    private TextView mTextView = null;
    String text;
    int imageViewID, textColor;

    public MyImageTextViewNew(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.setOrientation(LinearLayout.VERTICAL);//设置垂直排序
        this.setGravity(Gravity.CENTER);//设置居中
        if (mImageView == null) {
            mImageView = new ImageView(context);
        }
        if (mTextView == null) {
            mTextView = new TextView(context);
        }


        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.imageText);
        int count = typedArray.getIndexCount();
        for (int i = 0; i < count; i++) {
            int index = typedArray.getIndex(i);
            switch (index) {
                case R.styleable.imageText_image:
                    imageViewID = typedArray.getResourceId(index, 0);
                    break;
                case R.styleable.imageText_text:
                    text = typedArray.getString(index);
                    break;
                case R.styleable.imageText_textColor:
                    textColor = typedArray.getResourceId(index, 0);
                    break;
                default:
                    break;
            }


        }
        init();
    }

    /**
     * 初始化状态
     */
    private void init() {
        this.setText(text);
        mTextView.setGravity(Gravity.CENTER);//字体居中
        this.setTextColor(textColor);
        this.setImgResource(imageViewID);
        addView(mImageView);//将图片控件加入到布局中
        addView(mTextView);//将文字控件加入到布局中
    }

    public MyImageTextViewNew(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 设置显示的文字
     *
     * @param text
     */
    public void setText(String text) {
        this.mTextView.setText(text);
    }

    /**
     * 设置显示的图片
     *
     * @param resourceID 图片ID
     */
    public void setImgResource(int resourceID) {
        if (resourceID == 0) {
            this.mImageView.setImageResource(0);
        } else {
            this.mImageView.setImageResource(resourceID);
        }
    }

    /**
     * 设置字体颜色(默认为黑色)
     *
     * @param color
     */
    private void setTextColor(int color) {
        if (color == 0) {
            this.mTextView.setTextColor(Color.BLACK);
        } else {
            this.mTextView.setTextColor(getResources().getColor(color));
        }
    }
}
