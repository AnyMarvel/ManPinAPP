package com.mp.android.apps.explore.network;


import com.mp.android.apps.explore.bean.JsonRootBean;

import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ExploreApi {
    @POST("app/getPersonInfos")
    Call<JsonRootBean> getPersonInfos(@Query("uniqueID") String uniqueID, @Query("page") int page);

    /**
     * 获取广场图片信息数据
     *
     * @param page 页数
     * @return 返回广场信息数据
     */
    @POST("app/getSquareInfs")
    Call<JsonRootBean> getSquareInfs(@Query("page") int page);
}
