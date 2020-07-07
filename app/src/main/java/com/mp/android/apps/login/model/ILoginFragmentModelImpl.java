package com.mp.android.apps.login.model;

import android.nfc.Tag;

import com.alibaba.fastjson.JSON;
import com.mp.android.apps.monke.basemvplib.impl.BaseModelImpl;
import com.mp.android.apps.monke.monkeybook.base.MBaseModelImpl;


import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public class ILoginFragmentModelImpl  extends MBaseModelImpl {
    private final String TAG = "http://10.12.176.59:8080";

    public static ILoginFragmentModelImpl getInstance() {
        return new ILoginFragmentModelImpl();
    }

    public Observable<String> verifyLoginUser(String contract) {
        Map<String, String> map = new HashMap<String, String>(16);
        map.put("clientId", "59dfbd1db8544992aa9f7e12f5c1a213");
        map.put("clientSecret", "");
        map.put("recipient", contract);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), JSON.toJSONString(map));
        return getRetrofitObject("https://www.onlyid.net").create(ILoginFragmentAPI.class).verifyCodeByID(requestBody);
    }

    public Observable<String> loginByContractInfo(String str) {
        return getRetrofitObject(TAG).create(ILoginFragmentAPI.class).loginByContractInfo(str);
    }


    interface ILoginFragmentAPI {
        @POST("/api/open/send-otp")
        Observable<String> verifyCodeByID(@Body RequestBody requestBody);

        @GET("/app/phoneOrEmailLogin")
        Observable<String> loginByContractInfo(@Query("constr") String constr);

    }

}
