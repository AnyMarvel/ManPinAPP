package com.mp.android.apps.monke.monkeybook.widget.contentswitchview.contentAnimtion;

import android.content.Context;

import com.mp.android.apps.monke.monkeybook.widget.contentswitchview.BookContentView;

import java.util.List;

public abstract class HorizonPageAnim extends MyPageAnimation {

    public HorizonPageAnim(Context context) {
        super(context);
    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom, onLayoutStatus onLayoutStatus, List<BookContentView> viewContents) {
        if (onLayoutStatus.onlyOne()) {
            viewContents.get(0).layout(0, top, onLayoutStatus.getScreenWidth(), bottom);
        } else if (onLayoutStatus.preAndNext()) {
            viewContents.get(0).layout(-onLayoutStatus.getScreenWidth(), top, 0, bottom);
            viewContents.get(1).layout(0, top, onLayoutStatus.getScreenWidth(), bottom);
            viewContents.get(2).layout(0, top, onLayoutStatus.getScreenWidth(), bottom);
        } else if (onLayoutStatus.onlyPre()) {
            viewContents.get(0).layout(-onLayoutStatus.getScreenWidth(), top, 0, bottom);
            viewContents.get(1).layout(0, top, onLayoutStatus.getScreenWidth(), bottom);
        } else if (onLayoutStatus.onlyNext()) {
            viewContents.get(0).layout(0, top, onLayoutStatus.getScreenWidth(), bottom);
            viewContents.get(1).layout(0, top, onLayoutStatus.getScreenWidth(), bottom);
        }
    }
}
