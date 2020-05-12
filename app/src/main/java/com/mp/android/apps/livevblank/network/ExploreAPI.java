package com.mp.android.apps.livevblank.network;

import com.mp.android.apps.livevblank.bean.UpdateImageBean;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;

public interface ExploreAPI {

    @Multipart
    @POST("app/uploadImage")
    Call<UpdateImageBean> uploadImages(
            @Part MultipartBody.Part file, @PartMap Map<String, RequestBody> map);

}
