//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.mp.android.apps.monke.monkeybook.presenter;

import com.mp.android.apps.monke.basemvplib.IPresenter;

public interface IMainPresenter extends IPresenter{
    void queryBookShelf(Boolean needRefresh);
}
