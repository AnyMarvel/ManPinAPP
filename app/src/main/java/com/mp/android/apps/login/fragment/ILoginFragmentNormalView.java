package com.mp.android.apps.login.fragment;

import com.mp.android.apps.monke.basemvplib.IView;

public interface ILoginFragmentNormalView extends IView {

    void notifyLoginByUserNameResult(boolean success);


}
