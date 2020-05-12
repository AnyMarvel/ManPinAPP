//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.mp.android.apps.monke.monkeybook.presenter;

import com.mp.android.apps.monke.basemvplib.IPresenter;

import java.io.File;
import java.util.List;

public interface IImportBookPresenter extends IPresenter {
    void searchLocationBook();

    void importBooks(List<File> books);
}
