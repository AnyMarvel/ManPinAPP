package com.mp.android.apps.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * @author ShuiYu
 * Created on 3/3/22
 * Copyright © 2022 Alibaba-inc. All rights reserved.
 */

public class GeneralTools {
    public static String APP_VERSION;
    /**
     * 获取版本号    需要在Activity 中使用
     *
     * @return 当前应用的版本号
     */

    public static String getVersion(Context context) {
        String version = "";
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            version = info.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        APP_VERSION=version;
        return version;
    }
}
