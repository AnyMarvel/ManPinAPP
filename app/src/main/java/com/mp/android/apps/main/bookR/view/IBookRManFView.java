package com.mp.android.apps.main.bookR.view;

import com.mp.android.apps.main.home.bean.HomeDesignBean;
import com.mp.android.apps.main.home.bean.SourceListContent;
import com.mp.android.apps.monke.basemvplib.IView;

import java.util.List;

public interface IBookRManFView extends IView {
    void notifyRecyclerView(List<SourceListContent> recommendList, List<SourceListContent> hotRankingList, List<HomeDesignBean> listContent, boolean useCache);
    void notifyContentItemUpdate(int position, List<SourceListContent> sourceListContents);

}
