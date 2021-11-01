package com.mp.android.apps.main.bookR.view.popupwindow;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * @author ShuiYu
 * Created on 11/1/21
 * Copyright © 2021 Alibaba-inc. All rights reserved.
 */

public interface BCAPI {
    /**
     * 恢复server数据到客户端
     * @param uniqueID
     * @return
     */
    @GET("/appview/backUserBookCollections")
    Observable<String> backUserBookCollections(@Query("uniqueID") String uniqueID);

    /**
     * 备用用户客户端数据到server
     * @param body
     * @return
     */
    @POST("/appview/userBookCorrespondence")
    Observable<String> userBookCorrespondence(@Body RequestBody body);

}
