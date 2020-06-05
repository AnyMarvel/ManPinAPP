package com.mp.android.apps.main.model;

import com.mp.android.apps.monke.monkeybook.base.MBaseModelImpl;

import io.reactivex.Observable;
import retrofit2.http.GET;

public class IMainFragmentModelImpl extends MBaseModelImpl {
    private final String TAG = "http://aimanpin.com";

    public static IMainFragmentModelImpl getInstance() {
        return new IMainFragmentModelImpl();
    }

    public Observable<String> getCycleImages() {
        return getRetrofitObject(TAG).create(IMainFragmentAPI.class).getCycleImages();
    }

    interface IMainFragmentAPI {
        @GET("/appview/carouselImages")
        Observable<String> getCycleImages();
    }

}
