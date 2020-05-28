package com.mp.android.apps.monke.monkeybook.model.impl;

import com.mp.android.apps.monke.monkeybook.base.MBaseModelImpl;

import com.mp.android.apps.monke.monkeybook.common.api.IWebsiteAvailableApi;


import io.reactivex.Observable;

public class LibraryModelImpl extends MBaseModelImpl {
    private static final String MANPIN_TAG = "http://aimanpin.com";

    public static LibraryModelImpl getInstance() {
        return new LibraryModelImpl();
    }


    public Observable<String> getNewWebsiteAvailable() {
        return getRetrofitObject(MANPIN_TAG).create(IWebsiteAvailableApi.class).getWebsite();

    }


}
