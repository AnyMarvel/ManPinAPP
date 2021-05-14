package com.mp.android.apps.main.bookR.presenter;

import com.mp.android.apps.basemvplib.IPresenter;

public interface IBookRRecommendFPresenter extends IPresenter {
    void initBookRRcommendData();

    void getNextPageContent(int page);
}
