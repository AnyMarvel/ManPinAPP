package com.mp.android.apps.utils;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

/**
 * 沉浸式状态栏工具。
 * 浅色页面调用 {@link #setLightStatusBar(Activity)}：状态栏透明、内容延伸到其下方、状态栏图标显示为深色。
 * 需配合页面根布局设置 android:fitsSystemWindows="true" 以自动为内容让出状态栏高度。
 */
public final class ImmersiveUtils {

    private ImmersiveUtils() {
    }

    public static void setLightStatusBar(Activity activity) {
        if (activity == null) {
            return;
        }
        Window window = activity.getWindow();
        if (window == null) {
            return;
        }
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        }
        window.getDecorView().setSystemUiVisibility(flags);
        window.setStatusBarColor(Color.TRANSPARENT);
    }
}
