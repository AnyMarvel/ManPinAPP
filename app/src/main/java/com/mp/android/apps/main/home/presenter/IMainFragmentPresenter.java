package com.mp.android.apps.main.home.presenter;

import com.mp.android.apps.monke.basemvplib.IPresenter;


public interface IMainFragmentPresenter extends IPresenter {


    void initHomeData();

    void getContentPostion(int mContentPosition,String kinds);
}
