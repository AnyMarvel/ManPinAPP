package com.mp.android.apps.main.home.model;

import com.mp.android.apps.book.base.MBaseModelImpl;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class IMainFragmentModelImpl extends MBaseModelImpl {
    private final String TAG = "https://www.qidian.com";

    public static IMainFragmentModelImpl getInstance() {
        return new IMainFragmentModelImpl();
    }

    /**
     * 获取首页轮播图数据源
     *
     * @return
     */
    public Observable<String> getHomeData() {
        return getRetrofitObject(TAG).create(IMainFragmentAPI.class).getHomeData();
    }


    /**
     * api数据接口
     */
    interface IMainFragmentAPI {
        @GET("/")
        Observable<String> getHomeData();
    }

}
