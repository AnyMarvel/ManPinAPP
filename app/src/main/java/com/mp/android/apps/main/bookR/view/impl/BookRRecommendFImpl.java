package com.mp.android.apps.main.bookR.view.impl;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mp.android.apps.R;
import com.mp.android.apps.main.bookR.presenter.IBookRRecommendFPresenter;
import com.mp.android.apps.main.bookR.presenter.impl.BookRRecommendFPresenterImpl;
import com.mp.android.apps.main.bookR.view.IBookRRecommendFView;
import com.mp.android.apps.monke.basemvplib.impl.BaseFragment;

public class BookRRecommendFImpl extends BaseFragment<IBookRRecommendFPresenter> implements IBookRRecommendFView {
    @Override
    protected IBookRRecommendFPresenter initInjector() {
        return new BookRRecommendFPresenterImpl();
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.mp_book_r_recommend_layout, container, false);
    }
}
