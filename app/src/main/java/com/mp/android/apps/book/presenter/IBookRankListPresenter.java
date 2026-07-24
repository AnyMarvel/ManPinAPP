package com.mp.android.apps.book.presenter;

import com.mp.android.apps.base.IPresenter;

public interface IBookRankListPresenter extends IPresenter {
    void initBookRankListData(String routePath);
    void getNextPageContent(String routePath,int pageNumber);
}
