package com.google.android.apps.photolab.storyboard.download;

import android.content.Context;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AddCookiesInterceptor implements Interceptor {
    String cookie;

    public AddCookiesInterceptor(String cookie) {
        this.cookie = cookie;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {

        final Request.Builder builder = chain.request().newBuilder();
        builder.addHeader("Cookie", cookie);
        return chain.proceed(builder.build());
    }
}