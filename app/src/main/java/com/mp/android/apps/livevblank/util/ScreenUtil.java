package com.mp.android.apps.livevblank.util;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.WindowManager;

public class ScreenUtil {
    static DisplayMetrics dm = new DisplayMetrics();

    private static void setDisplayMetrics(Context context) {
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(dm);
    }

    public static int getWidth(Context Context) {
        setDisplayMetrics(Context);
        return dm.widthPixels;
    }

    public static int getHeight(Context Context) {
        setDisplayMetrics(Context);
        return dm.heightPixels;
    }

    public static int px2dp(Context context, float pxValue) {
        setDisplayMetrics(context);
        return (int) ((pxValue / dm.scaledDensity) + 0.5f);
    }

    public static int dp2px(Context context, float dp) {
        setDisplayMetrics(context);
        return (int) TypedValue.applyDimension(1, dp, context.getResources().getDisplayMetrics());
    }

    public static int getSW(Context context) {
        setDisplayMetrics(context);
        return px2dp(context, (float) dm.widthPixels);
    }

    public static int getStatusBarHeight(Context context) {
        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            return context.getResources().getDimensionPixelSize(Integer.parseInt(c.getField("status_bar_height").get(c.newInstance()).toString()));
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static boolean hasNavigationBar(Context context) {
        boolean hasNavigationBar = false;
        Resources rs = context.getResources();
        int id = rs.getIdentifier("config_showNavigationBar", "bool", "android");
        if (id > 0) {
            hasNavigationBar = rs.getBoolean(id);
        }
        try {
            Class systemPropertiesClass = Class.forName("android.os.SystemProperties");
            String navBarOverride = (String) systemPropertiesClass.getMethod("get", new Class[]{String.class}).invoke(systemPropertiesClass, new Object[]{"qemu.hw.mainkeys"});
            if ("1".equals(navBarOverride)) {
                return false;
            }
            return hasNavigationBar;
        } catch (Exception e) {
            e.printStackTrace();
            return hasNavigationBar;
        }
    }

    public static int getNavigationBarHeight(Context context) {
        Resources rs = context.getResources();
        int id = rs.getIdentifier("navigation_bar_height", "dimen", "android");
        if (id <= 0 || !hasNavigationBar(context)) {
            return 0;
        }
        return rs.getDimensionPixelSize(id);
    }

    public static String getScreenInfo(Context context) {
        setDisplayMetrics(context);
        StringBuilder sb = new StringBuilder();
        sb.append("density:").append(dm.density).append("\n");
        sb.append("densityDpi:").append(dm.densityDpi).append("\n");
        sb.append("heightPixels:").append(dm.heightPixels).append("\n");
        sb.append("widthPixels:").append(dm.widthPixels).append("\n");
        sb.append("scaledDensity:").append(dm.scaledDensity).append("\n");
        sb.append("sw:").append(getSW(context)).append("\n");
        sb.append("hasNavigationBar:").append(hasNavigationBar(context)).append("\n");
        sb.append("NavigationBarHeight").append(getNavigationBarHeight(context)).append("\n");
        return sb.toString();
    }
}
