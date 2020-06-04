package com.mp.android.apps.main.view.bar;

import android.graphics.drawable.Drawable;
import android.view.View;

public interface Bar {

    Bar statusBarDarkFont();

    Bar statusBarLightFont();

    Bar statusBarBackground(int statusBarColor);

    Bar statusBarBackground(Drawable drawable);

    Bar statusBarBackgroundAlpha(int alpha);

    Bar navigationBarBackground(int navigationBarColor);

    Bar navigationBarBackground(Drawable drawable);

    Bar navigationBarBackgroundAlpha(int alpha);

    Bar invasionStatusBar();

    Bar invasionNavigationBar();

    /**
     * @deprecated use {@link #fitsStatusBarView(int)} instead.
     */
    @Deprecated
    Bar fitsSystemWindowView(int viewId);

    /**
     * @deprecated use {@link #fitsStatusBarView(View)} instead.
     */
    @Deprecated
    Bar fitsSystemWindowView(View view);

    Bar fitsStatusBarView(int viewId);

    Bar fitsStatusBarView(View view);

    Bar fitsNavigationBarView(int viewId);

    Bar fitsNavigationBarView(View view);
}
