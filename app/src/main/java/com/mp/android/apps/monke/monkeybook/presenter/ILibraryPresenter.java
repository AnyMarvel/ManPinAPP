
package com.mp.android.apps.monke.monkeybook.presenter;

import com.mp.android.apps.monke.basemvplib.IPresenter;

import java.util.LinkedHashMap;

public interface ILibraryPresenter extends IPresenter {

    void getKinds();

    void getLibraryData();
}
