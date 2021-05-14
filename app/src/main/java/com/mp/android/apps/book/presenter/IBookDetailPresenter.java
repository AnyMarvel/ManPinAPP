
package com.mp.android.apps.book.presenter;

import com.mp.android.apps.basemvplib.IPresenter;
import com.mp.android.apps.book.bean.SearchBookBean;
import com.mp.android.apps.readActivity.bean.CollBookBean;

public interface IBookDetailPresenter extends IPresenter {

    int getOpenfrom();

    SearchBookBean getSearchBook();


    CollBookBean getCollBookBean();

    /**
     * 是否是在本地书架中
     * @return
     */
    Boolean getInBookShelf();

    void getBookShelfInfo();

    void addToBookShelf();

    void removeFromBookShelf();
}
