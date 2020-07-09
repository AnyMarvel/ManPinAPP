package com.mp.android.apps.login.presenter;

import com.mp.android.apps.monke.basemvplib.IPresenter;

public interface ILoginFragmentNormalPresenter extends IPresenter {
    void loginByUserName(String userName, String password);
}
