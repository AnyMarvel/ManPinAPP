package com.mp.android.apps.monke.monkeybook.model.impl;

import com.mp.android.apps.monke.monkeybook.base.MBaseModelImpl;
import com.mp.android.apps.monke.monkeybook.bean.BookContentBean;
import com.mp.android.apps.monke.monkeybook.bean.BookShelfBean;
import com.mp.android.apps.monke.monkeybook.bean.SearchBookBean;
import com.mp.android.apps.monke.monkeybook.listener.OnGetChapterListListener;
import com.mp.android.apps.monke.monkeybook.model.IStationBookModel;

import java.util.List;

import io.reactivex.Observable;

public class ContentleixunsongImpl extends MBaseModelImpl implements IStationBookModel {
    public static final String TAG = "http://www.leixuesong.cn";

    public static ContentleixunsongImpl getInstance() {
        return new ContentleixunsongImpl();
    }

    @Override
    public Observable<List<SearchBookBean>> searchBook(String content, int page) {
        return null;
    }

    @Override
    public Observable<BookShelfBean> getBookInfo(BookShelfBean bookShelfBean) {
        return null;
    }

    @Override
    public void getChapterList(BookShelfBean bookShelfBean, OnGetChapterListListener getChapterListListener) {

    }

    @Override
    public Observable<BookContentBean> getBookContent(String durChapterUrl, int durChapterIndex) {
        return null;
    }
}
