package com.mp.android.apps.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;


/**
 * UnStatusBarUtils简介
 * 沉浸式工具类
 *
 * @author anymarvel
 * @date 2020-06-29 18:14
 */
public class UnStatusBarUtils {

    /**
     * 默认透明读
     */
    private static final int DEFAULT_ALPHA = 112;
    /**
     * 着色标识
     */
    private static final String TAG_COLOR = "TAG_COLOR";
    /**
     * 透明度标识
     */
    private static final String TAG_ALPHA = "TAG_ALPHA";
    /**
     *
     */
    private static final int TAG_OFFSET = -123;

    /**
     * 获取状态栏高度（px）
     *
     * @param context
     * @return
     */
    private static int getStatusBarHeight(Context context) {
        Resources resources = context.getApplicationContext().getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        return resources.getDimensionPixelSize(resourceId);
    }


    /**
     * 设置状态栏是否可见
     *
     * @param activity
     * @param isVisible
     */
    private static void setStatusBarVisibility(@NonNull Activity activity,
                                               boolean isVisible) {
        setStatusBarVisibility(activity.getWindow(), isVisible);
    }

    /**
     * 设置状态栏是否可见
     *
     * @param window
     * @param isVisible
     */
    private static void setStatusBarVisibility(@NonNull Window window,
                                               boolean isVisible) {
        if (isVisible) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    /**
     * 判断状态栏是否可见
     *
     * @param activity
     * @return
     */
    private static boolean isStatusBarVisible(@NonNull Activity activity) {
        int flags = activity.getWindow().getAttributes().flags;
        return (flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) == 0;
    }


    /**
     * 设置状态栏颜色
     *
     * @param activity
     * @param color    status bar颜色
     */
    private static void setStatusBarColor(@NonNull Activity activity,
                                          @ColorInt int color) {
        setStatusBarColor(activity, color, DEFAULT_ALPHA, false);
    }

    /**
     * 设置状态栏颜色
     *
     * @param activity
     * @param color    状态栏颜色
     * @param alpha    透明度
     * @param isDecor
     */
    private static void setStatusBarColor(@NonNull Activity activity,
                                          @ColorInt int color,
                                          @IntRange(from = 0, to = 255) int alpha,
                                          boolean isDecor) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            //4.4以上才能设置状态栏颜色
            return;
        }
        hideAlphaView(activity);
        transparentStatusBar(activity);
        addStatusBarColor(activity, color, alpha, isDecor);
    }

    /**
     * 隐藏透明view
     *
     * @param activity
     */
    private static void hideAlphaView(Activity activity) {
        if (activity.getWindow().getDecorView() instanceof ViewGroup) {
            ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
            View fakeStatusBarView = decorView.findViewWithTag(TAG_ALPHA);
            if (fakeStatusBarView == null) {
                return;
            }
            fakeStatusBarView.setVisibility(View.GONE);
        }

    }

    /**
     * 使状态栏透明
     *
     * @param activity
     */
    private static void transparentStatusBar(Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return;
        }
        Window window = activity.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //android6.0以上
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            //防止系统栏隐藏时内容区域大小发生变化|Activity全屏显示，但是状态栏不会被覆盖掉，而是正常显示，只是Activity顶端布局会被覆盖住
            int option = View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            window.getDecorView().setSystemUiVisibility(option);
            window.setStatusBarColor(Color.TRANSPARENT);
        } else {
            //使状态栏透明同时会拉伸window到全屏的状态
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    /**
     * 给状态栏添加颜色
     *
     * @param activity
     * @param color
     * @param alpha
     * @param isDecor
     */
    private static void addStatusBarColor(Activity activity,
                                          int color,
                                          int alpha,
                                          boolean isDecor) {

        ViewGroup parent = isDecor ? (ViewGroup) activity.getWindow().getDecorView() : (ViewGroup) activity.findViewById(android.R.id.content);
        View fakeStatusBarView = parent.findViewWithTag(TAG_COLOR);
        if (fakeStatusBarView != null) {
            if (fakeStatusBarView.getVisibility() == View.GONE) {
                fakeStatusBarView.setVisibility(View.VISIBLE);
            }
            fakeStatusBarView.setBackgroundColor(getStatusBarColor(color, alpha));
        } else {
            parent.addView(createColorStatusBarView(parent.getContext(), color, alpha));
        }
    }

    /**
     * 根据color和透明度获取颜色值
     *
     * @param color
     * @param alpha
     * @return
     */
    private static int getStatusBarColor(int color, int alpha) {
        if (alpha == 0) {
            return color;
        }
        float a = 1 - alpha / 255f;
        int red = (color >> 16) & 0xff;
        int green = (color >> 8) & 0xff;
        int blue = color & 0xff;
        red = (int) (red * a + 0.5);
        green = (int) (green * a + 0.5);
        blue = (int) (blue * a + 0.5);
        return Color.argb(255, red, green, blue);
    }

    /**
     * 创建statusbar
     *
     * @param context
     * @param color
     * @param alpha
     * @return
     */
    private static View createColorStatusBarView(Context context,
                                                 int color,
                                                 int alpha) {
        View statusBarView = new View(context);
        statusBarView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, getStatusBarHeight(context)));
        statusBarView.setBackgroundColor(getStatusBarColor(color, alpha));
        statusBarView.setTag(TAG_COLOR);
        return statusBarView;
    }

    /**
     * 设置状态栏透明度
     *
     * @param activity
     */
    private static void setStatusBarAlpha(@NonNull Activity activity) {
        setStatusBarAlpha(activity, DEFAULT_ALPHA, false);
    }

    /**
     * 设置状态栏透明度
     *
     * @param activity
     * @param alpha
     * @param isDecor
     */
    private static void setStatusBarAlpha(@NonNull Activity activity,
                                          @IntRange(from = 0, to = 255) int alpha,
                                          boolean isDecor) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return;
        }
        hideColorView(activity);
        transparentStatusBar(activity);
        addStatusBarAlpha(activity, alpha, isDecor);
    }

    /**
     * 隐藏着色view
     *
     * @param activity
     */
    private static void hideColorView(Activity activity) {
        ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
        View fakeStatusBarView = decorView.findViewWithTag(TAG_COLOR);
        if (fakeStatusBarView == null) {
            return;
        }
        fakeStatusBarView.setVisibility(View.GONE);
    }

    /**
     * 添加透明状态栏
     *
     * @param activity
     * @param alpha
     * @param isDecor
     */
    private static void addStatusBarAlpha(Activity activity,
                                          int alpha,
                                          boolean isDecor) {
        ViewGroup parent = isDecor ?
                (ViewGroup) activity.getWindow().getDecorView() :
                (ViewGroup) activity.findViewById(android.R.id.content);
        View fakeStatusBarView = parent.findViewWithTag(TAG_ALPHA);
        if (fakeStatusBarView != null) {
            if (fakeStatusBarView.getVisibility() == View.GONE) {
                fakeStatusBarView.setVisibility(View.VISIBLE);
            }
            fakeStatusBarView.setBackgroundColor(Color.argb(alpha, 0, 0, 0));
        } else {
            parent.addView(createAlphaStatusBarView(parent.getContext(), alpha));
        }
    }

    /**
     * 创建透明状态栏
     *
     * @param context context
     * @param alpha   透明度
     * @return 透明状态栏view
     */
    private static View createAlphaStatusBarView(Context context, int alpha) {
        View statusBarView = new View(context);
        statusBarView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, getStatusBarHeight(context)));
        statusBarView.setBackgroundColor(Color.argb(alpha, 0, 0, 0));
        statusBarView.setTag(TAG_ALPHA);
        return statusBarView;
    }

}
