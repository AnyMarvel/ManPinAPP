package com.mp.android.apps;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.mp.android.apps.book.service.DownloadService;

public class TranslucentActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            Intent serviceIntent = new Intent(this, DownloadService.class);
            startService(serviceIntent);
        } catch (Exception e) {
            //Android 8+ 后台启动服务受限；此处非必需(下载时由 bindService 按需拉起)，失败忽略避免崩溃
            android.util.Log.w("TranslucentActivity", "startService skipped: " + e.getMessage());
        }
        finish();
    }
}
