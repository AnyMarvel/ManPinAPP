package com.mp.android.apps.monke.monkeybook.model;

import com.mp.android.apps.monke.monkeybook.bean.SearchBookBean;
import com.mp.android.apps.monke.readActivity.bean.BookChapterBean;
import com.mp.android.apps.monke.readActivity.bean.ChapterInfoBean;
import com.mp.android.apps.monke.readActivity.bean.CollBookBean;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;

public interface IReaderBookModel {

    Observable<List<SearchBookBean>> searchBook(String content, int page);

    Observable<CollBookBean> getBookInfo(CollBookBean collBookBean);

    Single<List<BookChapterBean>> getBookChapters(CollBookBean collBookBean);

    Single<ChapterInfoBean> getChapterInfo(String url);


}
