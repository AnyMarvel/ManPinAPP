package com.mp.android.apps.login.fragment;

import com.mp.android.apps.monke.basemvplib.IView;

public interface ILoginFragmentView extends IView {
    void updateViewByverification(boolean result);

    /**
     * 判断登录是否成功
     *
     * @param success 判断登录是否成功
     */
    void notifyLoginStatus(boolean success);
}
