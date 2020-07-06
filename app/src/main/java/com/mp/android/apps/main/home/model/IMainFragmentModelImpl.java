package com.mp.android.apps.main.home.model;

import com.mp.android.apps.monke.monkeybook.base.MBaseModelImpl;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class IMainFragmentModelImpl extends MBaseModelImpl {
    private final String TAG = "http://10.12.176.59:8080";

    public static IMainFragmentModelImpl getInstance() {
        return new IMainFragmentModelImpl();
    }

    /**
     * 获取首页轮播图数据源
     *
     * @return
     */
    public Observable<String> getCycleImages() {
        return getRetrofitObject(TAG).create(IMainFragmentAPI.class).getCycleImages();
    }

    /**
     * 获取Content内容填充数据
     *
     * @return
     */
    public Observable<String> getHomeDatas() {
        return getRetrofitObject(TAG).create(IMainFragmentAPI.class).getHomeDatasApi();
    }

    /**
     * 获取单条item数据
     *
     * @param kinds 单个item数据标识
     * @return
     */
    public Observable<String> getContentItemData(String kinds) {
        return getRetrofitObject(TAG).create(IMainFragmentAPI.class).getContentItemData(kinds);
    }

    /**
     * api数据接口
     */
    interface IMainFragmentAPI {
        @GET("/appview/carouselImages")
        Observable<String> getCycleImages();

        @GET("/appview/homeViewData")
        Observable<String> getHomeDatasApi();

        @GET("/appview/changByBookKind")
        Observable<String> getContentItemData(@Query("kinds") String kinds);
    }

}
