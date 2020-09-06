package com.mp.android.apps.login.presenter.impl;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mp.android.apps.login.LoginActivity;
import com.mp.android.apps.login.bean.login.LoginRootBean;
import com.mp.android.apps.login.fragment.ILoginFragmentView;
import com.mp.android.apps.login.model.ILoginFragmentModelImpl;
import com.mp.android.apps.login.presenter.ILoginFragmentPresenter;
import com.mp.android.apps.login.utils.LoginManager;
import com.mp.android.apps.monke.basemvplib.impl.BasePresenterImpl;
import com.mp.android.apps.monke.monkeybook.base.observer.SimpleObserver;


import java.util.Objects;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class LoginFragmentPresenterImpl extends BasePresenterImpl<ILoginFragmentView> implements ILoginFragmentPresenter {

    @Override
    public void detachView() {

    }

    @Override
    public void requestVerificationCode(String contract) {
        ILoginFragmentModelImpl.getInstance().verifyLoginUser(contract)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<String>() {
                    @Override
                    public void onNext(String s) {
                        JSONObject jsonObject = JSON.parseObject(s);
                        if (jsonObject != null) {
                            String opt = jsonObject.getString("otp");
                            if (!TextUtils.isEmpty(opt)) {
                                LoginManager.getInstance().editLoginCodeByCodeNumber(contract, opt);
                                mView.updateViewByverification(true);
                            } else {
                                mView.updateViewByverification(false);
                            }
                        } else {
                            mView.updateViewByverification(false);
                        }

                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.updateViewByverification(false);
                    }
                });
    }

    @Override
    public void loginByContractInfo(String contract) {
        ILoginFragmentModelImpl.getInstance().loginByContractInfo(contract)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<String>() {
                    @Override
                    public void onNext(String s) {
                        LoginRootBean loginRootBean = JSON.parseObject(s, LoginRootBean.class);
                        if (loginRootBean != null && loginRootBean.getCode() == 0) {
                            LoginManager.getInstance().editLoginInfo(loginRootBean.getData());
                            mView.notifyLoginStatus(true);
                        } else {
                            mView.notifyLoginStatus(false);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.notifyLoginStatus(false);
                    }
                });
    }
}
