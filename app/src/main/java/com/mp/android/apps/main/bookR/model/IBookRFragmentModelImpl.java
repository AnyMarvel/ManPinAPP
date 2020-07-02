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


    public Observable<String> getMoreRecommendList() {
        return getRetrofitObject(TAG).create(IBookRFragmentAPI.class).getMoreRecommendList();
    }

    interface IBookRFragmentAPI {
        @GET("/mpsc/recommendHome")
        Observable<String> getBookRHomeData();

        @GET("/mpsc/getMoreRecommend")
        Observable<String> getMoreRecommendList();
    }

}
