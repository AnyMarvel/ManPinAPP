package com.mp.android.apps.main.home.adapter;

import android.view.View;

import com.mp.android.apps.main.home.bean.SourceListContent;

public interface OnHomeAdapterClickListener {

    void onItemClickListener(View view);

    void onLayoutClickListener(View view, SourceListContent sourceListContent);

    void onContentChangeClickListener(int mContentPosition,String kinds);
}
