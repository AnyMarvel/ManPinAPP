
package com.mp.android.apps.monke.monkeybook.view;

import com.mp.android.apps.monke.basemvplib.IView;

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
