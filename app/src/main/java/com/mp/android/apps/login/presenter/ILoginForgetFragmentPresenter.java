package com.mp.android.apps.login.presenter;

import com.mp.android.apps.monke.basemvplib.IPresenter;

public interface ILoginForgetFragmentPresenter extends IPresenter {
    /**
     * 获取验证码
     *
     * @param contract
     */
    void requestVerificationCode(String contract);

    void resetPasswordByVerifyCode(String constract,String newPassworld);
}
