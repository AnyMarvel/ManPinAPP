package com.mp.android.apps.login.presenter;

import com.mp.android.apps.monke.basemvplib.IPresenter;

public interface ILoginFragmentPresenter extends IPresenter {
    /**
     * 获取验证码
     *
     * @param addressOrPhone
     */
    void requestVerificationCode(String addressOrPhone);

    /**
     * 登录请求,登录获取个人信息
     *
     * @param contract
     */
    void loginByContractInfo(String contract);
}
