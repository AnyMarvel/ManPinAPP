package com.google.android.apps.photolab.storyboard.soloader;

import android.content.Context;
import android.text.TextUtils;


import dalvik.system.PathClassLoader;

public class SoStatus {
    /**
     * 判断当前APPSo是否已经加载
     * 查看 https://blog.csdn.net/Gaugamela/article/details/79143309
     *
     * @param context
     * @param libraryName
     * @return
     */
    public static String findNativeLibraryPath(Context context, String libraryName) {
        if (context == null) {
            return null;
        }

        if (TextUtils.isEmpty(libraryName)) {
            return null;
        }

        PathClassLoader classLoader = (PathClassLoader) context.getClassLoader();
        return classLoader.findLibrary(libraryName);
    }



}
