
package com.mp.android.apps.monke.monkeybook.model;

import com.mp.android.apps.monke.monkeybook.bean.BookShelfBean;
import com.mp.android.apps.monke.monkeybook.bean.SearchBookBean;

import java.util.List;

import io.reactivex.Observable;

public interface IStationBookModel {

    /**
     * 搜索书籍
     */
    Observable<List<SearchBookBean>> searchBook(String content, int page);

    /**
     * 网络请求并解析书籍信息
     */
    Observable<BookShelfBean> getBookInfo(final BookShelfBean bookShelfBean);

}
