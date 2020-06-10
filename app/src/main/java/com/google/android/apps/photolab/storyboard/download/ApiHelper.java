package com.google.android.apps.photolab.storyboard.download;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;


public class ApiHelper {

    private static final String TAG = "ApiHelper";

    private static ApiHelper mInstance;
    private Retrofit mRetrofit;
    private OkHttpClient mHttpClient;

    private ApiHelper() {
        this(10, 10, 10);
    }

    public ApiHelper(int connTimeout, int readTimeout, int writeTimeout) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(connTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS)
                .writeTimeout(writeTimeout, TimeUnit.SECONDS);

        mHttpClient = builder.build();
    }

    public static ApiHelper getInstance() {
        if (mInstance == null) {
            mInstance = new ApiHelper();
        }

        return mInstance;
    }

    public ApiHelper buildRetrofit(String baseUrl) {
        mRetrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(mHttpClient)
                .build();
        return this;
    }

    public <T> T createService(Class<T> serviceClass) {
        return mRetrofit.create(serviceClass);
    }

}
