package com.mp.android.apps.base.impl;

import androidx.annotation.NonNull;
import com.mp.android.apps.base.IPresenter;
import com.mp.android.apps.base.IView;

public abstract class BasePresenterImpl<T extends IView> implements IPresenter {
    protected T mView;

    @Override
    public void attachView(@NonNull IView iView) {
        mView = (T) iView;
    }
}
