package com.mp.android.apps.login.presenter.impl;

import com.alibaba.fastjson.JSON;
import com.mp.android.apps.login.bean.login.Data;
import com.mp.android.apps.login.bean.login.LoginRootBean;
import com.mp.android.apps.login.fragment.ILoginFragmentNormalView;
import com.mp.android.apps.login.model.ILoginFragmentModelImpl;
import com.mp.android.apps.login.presenter.ILoginFragmentNormalPresenter;
import com.mp.android.apps.login.utils.LoginManager;
import com.mp.android.apps.basemvplib.impl.BasePresenterImpl;
import com.mp.android.apps.book.base.observer.SimpleObserver;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class LoginFragmentNormalPresenterImpl extends BasePresenterImpl<ILoginFragmentNormalView> implements ILoginFragmentNormalPresenter {
    @Override
    public void detachView() {

    }

    @Override
    public void loginByUserName(String userName, String password) {
        ILoginFragmentModelImpl.getInstance().loginByUserName(userName, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<String>() {
                    @Override
                    public void onNext(String s) {
                        LoginRootBean loginRootBean = JSON.parseObject(s, LoginRootBean.class);
                        if (loginRootBean != null && loginRootBean.getData() != null) {
                            Data data = loginRootBean.getData();
                            LoginManager.getInstance().editLoginInfo(data);
                            mView.notifyLoginByUserNameResult(true);
                        }else {
                            mView.notifyLoginByUserNameResult(false);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.notifyLoginByUserNameResult(false);
                    }
                });
    }
}
