package com.mp.android.apps.read.ui;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.View;

import com.mp.android.apps.R;
import com.mp.android.apps.read.base.adapter.ViewHolderImpl;
import com.mp.android.apps.read.utils.ScreenUtils;


public class PageStyleHolder extends ViewHolderImpl<Drawable> {

    private static final int RING_COLOR = 0xFFEC4A48;    //选中描边(强调色)
    private static final int BORDER_COLOR = 0xFFC2C2C2;   //未选中细边

    private View mReadBg;
    private int mColor;

    @Override
    public void initView() {
        mReadBg = findById(R.id.read_bg_view);
    }

    @Override
    public void onBind(Drawable data, int pos) {
        mColor = (data instanceof ColorDrawable) ? ((ColorDrawable) data).getColor() : Color.GRAY;
        mReadBg.setBackground(buildCircle(false));
    }

    public void setChecked() {
        mReadBg.setBackground(buildCircle(true));
    }

    /**
     * 圆形色点：填充为主题背景色；未选中细灰边，选中强调色粗边。
     */
    private GradientDrawable buildCircle(boolean checked) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(mColor);
        drawable.setStroke(
                ScreenUtils.dpToPx(checked ? 2 : 1),
                checked ? RING_COLOR : BORDER_COLOR);
        return drawable;
    }

    @Override
    protected int getItemLayoutId() {
        return R.layout.item_read_bg;
    }
}
