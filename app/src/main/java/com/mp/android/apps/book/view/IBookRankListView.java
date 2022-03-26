package com.mp.android.apps.book.view;

import com.mp.android.apps.basemvplib.IView;
import com.mp.android.apps.main.home.bean.SourceListContent;

import java.util.List;

public interface IBookRankListView extends IView {
    void notifyRecyclerView(List<SourceListContent> contentList, boolean useCache,int PageNumber);
    void showError(int pageNumber);
}
