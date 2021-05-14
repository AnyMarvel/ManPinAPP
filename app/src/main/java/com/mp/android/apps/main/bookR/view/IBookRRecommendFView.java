package com.mp.android.apps.main.bookR.view;

import com.mp.android.apps.main.home.bean.SourceListContent;
import com.mp.android.apps.basemvplib.IView;

import java.util.List;

public interface IBookRRecommendFView extends IView {
    void notifyRecyclerView(List<SourceListContent> recommendList, List<SourceListContent> hotRankingList, List<SourceListContent> contentList, boolean useCache);

    void notifyMoreRecommendList(List<SourceListContent> recommendList);
}
