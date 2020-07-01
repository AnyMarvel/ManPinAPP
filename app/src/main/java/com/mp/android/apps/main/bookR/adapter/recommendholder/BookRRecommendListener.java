package com.mp.android.apps.main.bookR.adapter.recommendholder;

import android.view.View;

import com.mp.android.apps.main.home.bean.SourceListContent;

public interface BookRRecommendListener {
    void onItemClick(View view);
    void onLayoutClickListener(View view, SourceListContent sourceListContent);
}
