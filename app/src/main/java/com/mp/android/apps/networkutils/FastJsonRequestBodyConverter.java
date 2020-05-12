package com.mp.android.apps.networkutils;

import com.alibaba.fastjson.JSON;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Converter;

public class FastJsonRequestBodyConverter<T> implements Converter<T, RequestBody> {
    private static final MediaType MEDIA_TYPE = MediaType.parse("application/json; charset=UTF-8");

    @Override
    public RequestBody convert(Object value) throws IOException {

        return RequestBody.create(MEDIA_TYPE, JSON.toJSONBytes(value));
    }
}
