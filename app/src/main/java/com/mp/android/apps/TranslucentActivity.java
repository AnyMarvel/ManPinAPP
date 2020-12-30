package com.mp.android.apps;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

public class TranslucentActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent serviceIntent = new Intent();
        serviceIntent.setAction("com.mp.android.apps.monke.monkeybook.service.DownloadService_action");
        serviceIntent.setPackage(getPackageName());
        startService(serviceIntent);
        finish();
    }
}
