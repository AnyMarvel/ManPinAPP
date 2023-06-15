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
import com.umeng.commonsdk.UMConfigure;

import java.io.File;
import java.util.Arrays;

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

        UMConfigure.init(this, "6489183fa1a164591b31cc4f"
                , "umeng", UMConfigure.DEVICE_TYPE_PHONE, "");
        if (BuildConfig.DEBUG) {
            UMConfigure.setLogEnabled(true);
            Logger.setDebug(true);
        }
        instance = this;

        startDownloadService();

        GeneralTools.getVersion(this);
    }

    private void startDownloadService() {
        Intent serviceIntent = new Intent(this, DownloadService.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                startService(serviceIntent);
            } catch (Exception e) {
                Logger.d("startDownloadService", Arrays.toString(e.getStackTrace()));
                Intent activityIntent = new Intent(this, TranslucentActivity.class);
                activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(activityIntent);
            }
        } else {
            startService(serviceIntent);
        }
    }



}
