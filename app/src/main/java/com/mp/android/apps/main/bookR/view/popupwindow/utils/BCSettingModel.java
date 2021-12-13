package com.mp.android.apps.main.bookR.view.popupwindow.utils;

import com.mp.android.apps.book.base.MBaseModelImpl;
import com.mp.android.apps.main.bookR.view.popupwindow.BCAPI;
import com.mp.android.apps.main.bookR.view.popupwindow.bean.UserBookCorrespondenceBean;

import io.reactivex.Observable;




public class BCSettingModel extends MBaseModelImpl {
    private final String TAG = "http://aimanpin.com";

    public Observable<String> userBookCorrespondence(UserBookCorrespondenceBean userBookCorrespondenceBean) {
        return getRetrofitObject(TAG).create(BCAPI.class).userBookCorrespondence(generateRequestBody(userBookCorrespondenceBean));
    }

    public Observable<String> backUserBookCollections(String  uniqueID) {
        return getRetrofitObject(TAG).create(BCAPI.class).backUserBookCollections(uniqueID);
    }

}
