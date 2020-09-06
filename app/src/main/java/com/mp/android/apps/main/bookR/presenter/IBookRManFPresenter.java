package com.mp.android.apps.main.bookR.presenter;

import com.mp.android.apps.monke.basemvplib.IPresenter;

public interface IBookRManFPresenter extends IPresenter {
    void initManData();

    void initWoManData();

    void getBookCardData(int mContentPosition, String kinds);
}
