package com.mp.android.apps.main.bookR.model;

import com.mp.android.apps.monke.monkeybook.base.MBaseModelImpl;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class IBookRFragmentModelImpl extends MBaseModelImpl {
    private final String TAG = "http://10.12.176.59:8080";

    public static IBookRFragmentModelImpl getInstance() {
        return new IBookRFragmentModelImpl();
    }

    /**
     * 获得书城推荐页数据
     *
     * @return
     */
    public Observable<String> getBookRHomeData() {
        return getRetrofitObject(TAG).create(IBookRFragmentAPI.class).getBookRHomeData();
    }

    /**
     * 书城推荐页下拉刷新更多数据内容
     *
     * @return
     */
    public Observable<String> getMoreRecommendList(int page) {
        return getRetrofitObject(TAG).create(IBookRFragmentAPI.class).getMoreRecommendList(page);
    }

    /**
     * 获取书城男生推荐主页数据
     * @return
     */
    public Observable<String> getBookManHomeData() {
        return getRetrofitObject(TAG).create(IBookRFragmentAPI.class).getBookManFragmentdata();
    }

    /**
     * 获取书城女生推荐主页数据
     * @return
     */
    public Observable<String> getBookWomanHomeData() {
        return getRetrofitObject(TAG).create(IBookRFragmentAPI.class).getBookWoManFragmentdata();
    }
    interface IBookRFragmentAPI {
        @GET("/appview/recommendHome")
        Observable<String> getBookRHomeData();

        @GET("/appview/recommendPagData")
        Observable<String> getMoreRecommendList(@Query("page")int page);

        @GET("/appview/recommendManData")
        Observable<String> getBookManFragmentdata();

        @GET("/appview/recommendWomanData")
        Observable<String> getBookWoManFragmentdata();
    }

}
