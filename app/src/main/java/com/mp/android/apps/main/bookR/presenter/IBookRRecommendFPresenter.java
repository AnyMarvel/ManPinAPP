package com.mp.android.apps.main.bookR.presenter;

import com.mp.android.apps.main.home.bean.SourceListContent;
import com.mp.android.apps.monke.basemvplib.IPresenter;

import java.util.List;

public interface IBookRRecommendFPresenter extends IPresenter {
    void initBookRRcommendData();

    void getNextPageContent(int page);
}
