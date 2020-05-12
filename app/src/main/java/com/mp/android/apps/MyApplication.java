package com.mp.android.apps;

import android.app.Application;
import android.content.Context;
import android.content.Intent;


import android.os.Build;
import android.support.multidex.MultiDex;

import com.mp.android.apps.monke.monkeybook.ProxyManager;
import com.mp.android.apps.monke.monkeybook.service.DownloadService;
import com.mp.android.apps.utils.Logger;
import com.tencent.bugly.Bugly;

import com.umeng.commonsdk.UMConfigure;
import com.umeng.socialize.PlatformConfig;

public class MyApplication extends Application {

    private static MyApplication instance;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Bugly.init(getApplicationContext(), "097fb8e660", true);
        UMConfigure.init(this, "5b924832b27b0a673c0000d4"
                , "umeng", UMConfigure.DEVICE_TYPE_PHONE, "");
        if (BuildConfig.DEBUG) {
            UMConfigure.setLogEnabled(true);
            Logger.setDebug(true);
        }
        PlatformConfig.setWeixin("wxc76dfbe7fbc52c24", "a6fe1ca5055bd9aeaa3917c09e054036");
        PlatformConfig.setQQZone("1107874454", "vA0OwxNlf3qcp0nK");
        PlatformConfig.setSinaWeibo("1928616164", "36e5f497bc6b20cb73ff7138e0e1cbe5", "http://sns.whalecloud.com");

        instance = this;
        ProxyManager.initProxy();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(this, DownloadService.class));
        } else {
            startService(new Intent(this, DownloadService.class));
        }
    }

    public static MyApplication getInstance() {
        return instance;
    }
}
