package com.mp.android.apps.login.fragment;

import com.mp.android.apps.basemvplib.IView;

public interface ILoginFragmentNormalView extends IView {

    void notifyLoginByUserNameResult(boolean success);


}
