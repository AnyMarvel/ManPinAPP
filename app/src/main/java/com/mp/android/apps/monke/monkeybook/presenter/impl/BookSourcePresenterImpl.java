package com.mp.android.apps.monke.monkeybook.presenter.impl;

import com.mp.android.apps.monke.basemvplib.impl.BasePresenterImpl;
import com.mp.android.apps.monke.monkeybook.bean.BookSourceBean;
import com.mp.android.apps.monke.monkeybook.presenter.IBookSourcePresenter;
import com.mp.android.apps.monke.monkeybook.view.IBookSourceView;
import com.mp.android.apps.utils.SharedPreferenceUtil;

import java.util.List;

public class BookSourcePresenterImpl extends BasePresenterImpl<IBookSourceView> implements IBookSourcePresenter {
    @Override
    public void detachView() {

    }


    @Override
    public List<BookSourceBean> handleSource(List<BookSourceBean> bookSource) {
        for (BookSourceBean bookSourceBean : bookSource) {
            boolean sourceSwitch = (boolean) SharedPreferenceUtil.get(mView.getContext(), bookSourceBean.getBookSourceAddress(), false);
            if (sourceSwitch) {
                bookSourceBean.setBookSourceSwitch(true);
            } else {
                bookSourceBean.setBookSourceSwitch(false);
            }
        }
        return bookSource;
    }
}
