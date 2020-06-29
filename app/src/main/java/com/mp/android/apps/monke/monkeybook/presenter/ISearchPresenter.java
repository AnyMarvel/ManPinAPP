
package com.mp.android.apps.monke.monkeybook.presenter;

import com.mp.android.apps.monke.basemvplib.IPresenter;
import com.mp.android.apps.monke.monkeybook.bean.SearchBookBean;

public interface ISearchPresenter extends IPresenter {

    Boolean getHasSearch();

    void setHasSearch(Boolean hasSearch);

    void insertSearchHistory();

    void querySearchHistory();

    void cleanSearchHistory();

    int getPage();

    void initPage();

    void toSearchBooks(String key, Boolean fromError);

    void addBookToShelf(final SearchBookBean searchBookBean);

    Boolean getInput();

    void setInput(Boolean input);
}
