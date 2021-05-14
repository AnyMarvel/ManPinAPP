package com.mp.android.apps.monke.monkeybook.model;

import com.mp.android.apps.monke.monkeybook.bean.SearchBookBean;
import com.mp.android.apps.monke.readActivity.bean.BookChapterBean;
import com.mp.android.apps.monke.readActivity.bean.ChapterInfoBean;
import com.mp.android.apps.monke.readActivity.bean.CollBookBean;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;

public interface IReaderBookModel {

    /**
     * 搜索图书
     * @param content 搜索图书内容
     * @param page
     * @return
     */
    Observable<List<SearchBookBean>> searchBook(String content, int page);

    /**
     * 获取图书详情
     * @param collBookBean
     * @return
     */
    Observable<CollBookBean> getBookInfo(CollBookBean collBookBean);

    /**
     * 获取图书章节
     * @param collBookBean
     * @return
     */
    Single<List<BookChapterBean>> getBookChapters(CollBookBean collBookBean);

    /**
     * 获取当前章节详细内容
     * @param url
     * @return
     */
    Single<ChapterInfoBean> getChapterInfo(String url);


}
