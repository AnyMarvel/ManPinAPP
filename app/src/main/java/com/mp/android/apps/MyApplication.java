package com.mp.android.apps;


import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;
import androidx.multidex.MultiDex;
import com.mp.android.apps.book.service.DownloadService;
import com.mp.android.apps.utils.GeneralTools;
import com.mp.android.apps.utils.Logger;
import com.mp.android.apps.utils.crash.CrashHandler;

import java.io.File;

import io.reactivex.functions.Consumer;
import io.reactivex.plugins.RxJavaPlugins;

public class MyApplication extends Application {

    private static MyApplication instance;
    public static MyApplication getInstance() {
        return instance;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.VERSION_CODE <= 24) {
            File file = this.getDatabasePath("monkebook_db");
            if (file.exists()) {
                this.deleteDatabase("monkebook_db");
                Toast.makeText(this, "为适配新版本搜索引擎,旧版本书架将被清空", Toast.LENGTH_LONG).show();
            }
        }

        //全局崩溃捕获：将崩溃现场写入本地文件，供"设置 → 崩溃日志"查看与分享
        CrashHandler.getInstance().init(this);

        if (BuildConfig.DEBUG) {
            Logger.setDebug(true);
        }
        instance = this;

        startDownloadService();

        GeneralTools.getVersion(this);
        RxJavaPlugins.setErrorHandler(new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                //rxjava异常统一兜底
                android.util.Log.e("MyApplication", "rxjava uncaught error", throwable);
            }
        });
    }

    private void startDownloadService() {
        Intent serviceIntent = new Intent(this, DownloadService.class);

        //离线下载服务非必需在启动时拉起(下载时由 bindService 按需创建)；
        //Android 8+ 后台启动服务/活动均受限，失败一律忽略，避免启动崩溃
        try {
            startService(serviceIntent);
        } catch (Exception e) {
            android.util.Log.w("MyApplication", "startDownloadService skipped: " + e.getMessage());
        }
    }



}
