
package com.mp.android.apps.monke.monkeybook.presenter;

import com.mp.android.apps.monke.basemvplib.IPresenter;
import com.mp.android.apps.monke.monkeybook.bean.SearchBookBean;

public interface IChoiceBookPresenter extends IPresenter {

    int getPage();

    void initPage();

    void toSearchBooks(String key);

    void addBookToShelf(final SearchBookBean searchBookBean);

    String getTitle();
}