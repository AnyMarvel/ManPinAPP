package com.mp.android.apps.login.fragment.imple;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mp.android.apps.R;
import com.mp.android.apps.login.fragment.ILoginForgetFragmentView;
import com.mp.android.apps.login.presenter.impl.LoginForgetFragmentPresenterImpl;

public class LoginForgetFragment extends LoginBaseFragment<LoginForgetFragmentPresenterImpl> implements ILoginForgetFragmentView {
    @Override
    protected LoginForgetFragmentPresenterImpl initInjector() {
        return new LoginForgetFragmentPresenterImpl();
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.login_lyout_forget_password, container, false);
    }



}
