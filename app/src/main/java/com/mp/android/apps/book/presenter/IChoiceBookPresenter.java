
package com.mp.android.apps.book.presenter;

import com.mp.android.apps.basemvplib.IPresenter;
import com.mp.android.apps.book.bean.SearchBookBean;

public interface IChoiceBookPresenter extends IPresenter {

    int getPage();

    void initPage();

    void toSearchBooks(String key);

    void addBookToShelf(final SearchBookBean searchBookBean);

    String getTitle();
}