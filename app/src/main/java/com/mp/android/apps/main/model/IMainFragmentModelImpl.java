package com.mp.android.apps.main.model;

import com.mp.android.apps.monke.monkeybook.base.MBaseModelImpl;

import io.reactivex.Observable;
import retrofit2.http.GET;

public class IMainFragmentModelImpl extends MBaseModelImpl {
    private final String TAG = "http://10.12.176.59:8080";

    public static IMainFragmentModelImpl getInstance() {
        return new IMainFragmentModelImpl();
    }

    public Observable<String> getCycleImages() {
        return getRetrofitObject(TAG).create(IMainFragmentAPI.class).getCycleImages();
    }

    public Observable<String> getHomeDatas() {
        return getRetrofitObject(TAG).create(IMainFragmentAPI.class).getHomeDatasApi();
    }


    interface IMainFragmentAPI {
        @GET("/appview/carouselImages")
        Observable<String> getCycleImages();

        @GET("/manpin_war/appview/homeViewData")
        Observable<String> getHomeDatasApi();

    }

}
