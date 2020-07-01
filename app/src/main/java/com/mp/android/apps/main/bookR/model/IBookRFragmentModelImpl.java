package com.mp.android.apps.main.bookR.model;

import com.mp.android.apps.monke.monkeybook.base.MBaseModelImpl;

import io.reactivex.Observable;
import retrofit2.http.GET;

public class IBookRFragmentModelImpl extends MBaseModelImpl {
    private final String TAG = "http://aimanpin.com";

    public static IBookRFragmentModelImpl getInstance() {
        return new IBookRFragmentModelImpl();
    }

    public Observable<String> getBookRHomeData() {
        return getRetrofitObject(TAG).create(IBookRFragmentAPI.class).getBookRHomeData();
    }


    interface IBookRFragmentAPI {
        @GET("/mpxs/recommendHome")
        Observable<String> getBookRHomeData();
    }

}
