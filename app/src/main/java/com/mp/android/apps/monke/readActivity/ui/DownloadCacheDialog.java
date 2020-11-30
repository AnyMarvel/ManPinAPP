package com.mp.android.apps.monke.readActivity.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mp.android.apps.R;

public class DownloadCacheDialog extends Dialog {


    public DownloadCacheDialog(@NonNull Context context) {
        super(context);
    }

    public DownloadCacheDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected DownloadCacheDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.read_book_cache_download_dialog);
    }
}
