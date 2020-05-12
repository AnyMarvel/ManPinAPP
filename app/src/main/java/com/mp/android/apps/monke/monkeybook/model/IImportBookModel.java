//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.mp.android.apps.monke.monkeybook.model;

import com.mp.android.apps.monke.monkeybook.bean.LocBookShelfBean;

import java.io.File;

import io.reactivex.Observable;

public interface IImportBookModel {

    Observable<LocBookShelfBean> importBook(File book);
}
