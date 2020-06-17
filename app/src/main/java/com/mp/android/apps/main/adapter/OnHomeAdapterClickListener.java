package com.mp.android.apps.main.adapter;

import android.view.View;

import com.mp.android.apps.main.bean.SourceListContent;

public interface OnHomeAdapterClickListener {
    void onItemClickListener(View view);

    void onLayoutClickListener(View view, SourceListContent sourceListContent);

}
