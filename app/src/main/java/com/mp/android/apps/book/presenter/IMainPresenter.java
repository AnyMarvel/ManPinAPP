
package com.mp.android.apps.book.presenter;

import com.mp.android.apps.base.IPresenter;

public interface IMainPresenter extends IPresenter{
    void queryBookShelf(Boolean needRefresh);
    boolean bookSourceSwitch();
}
