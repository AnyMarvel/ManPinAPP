package com.mp.android.apps.monke.readActivity.view.animation.leftPageAnim;

import android.graphics.Canvas;
import android.view.View;

import com.mp.android.apps.monke.readActivity.view.animation.HorizonPageAnim;

/**
 * Created by newbiechen on 17-7-24.
 */

public class LeftNonePageAnim extends LeftHorizonPageAnim {

    public LeftNonePageAnim(int w, int h, View view, OnPageChangeListener listener) {
        super(w, h, view, listener);
    }

    @Override
    public void drawStatic(Canvas canvas) {
        if (isCancel){
            canvas.drawBitmap(mCurBitmap, 0, 0, null);
        }else {
            canvas.drawBitmap(mNextBitmap, 0, 0, null);
        }
    }

    @Override
    public void drawMove(Canvas canvas) {
        if (isCancel){
            canvas.drawBitmap(mCurBitmap, 0, 0, null);
        }else {
            canvas.drawBitmap(mNextBitmap, 0, 0, null);
        }
    }

    @Override
    public void startAnim() {
    }
}
