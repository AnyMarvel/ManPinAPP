package com.mp.android.apps.main.bookR.view.impl;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mp.android.apps.R;
import com.mp.android.apps.main.bookR.presenter.IBookRManFPresenter;
import com.mp.android.apps.main.bookR.presenter.IBookRWomanFPresenter;
import com.mp.android.apps.main.bookR.presenter.impl.BookRWomanFPresenterImpl;
import com.mp.android.apps.main.bookR.view.IBookRManFView;
import com.mp.android.apps.main.bookR.view.IBookRWomanFView;
import com.mp.android.apps.monke.basemvplib.impl.BaseFragment;

public class BookRWomanFImpl extends BookRManFImpl {

    @Override
    public void initLocalData() {
        mPresenter.initWoManData();
    }
}
