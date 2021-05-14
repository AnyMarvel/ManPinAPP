
package com.mp.android.apps.book.view;

import com.mp.android.apps.basemvplib.IView;

public interface IBookDetailView extends IView {
    /**
     * 更新书籍详情UI
     */
    void updateView();

    /**
     * 数据获取失败
     */
    void getBookShelfError();
}
