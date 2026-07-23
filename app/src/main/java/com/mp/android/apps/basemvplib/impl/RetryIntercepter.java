package com.mp.android.apps.basemvplib.impl;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class RetryIntercepter implements Interceptor {
    private final int maxRetry;//最大重试次数

    public RetryIntercepter(int maxRetry) {
        this.maxRetry = maxRetry;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = null;
        IOException lastException = null;

        // 重试逻辑：假如设置为3次重试的话，则最大可能请求4次（默认1次+3次重试）
        for (int retryNum = 0; retryNum <= maxRetry; retryNum++) {
            try {
                if (response != null) {
                    response.close(); // 关闭之前的响应，避免资源泄漏
                }
                response = chain.proceed(request);

                // 如果请求成功，直接返回
                if (response.isSuccessful()) {
                    return response;
                }

                // 如果不是最后一次尝试，记录日志
                if (retryNum < maxRetry) {
                    android.util.Log.w("RetryIntercepter",
                        "Request failed with code: " + response.code() + ", retrying... (" + (retryNum + 1) + "/" + maxRetry + ")");
                }
            } catch (IOException e) {
                lastException = e;
                // 如果不是最后一次尝试，记录日志并继续重试
                if (retryNum < maxRetry) {
                    android.util.Log.w("RetryIntercepter",
                        "Request failed with exception, retrying... (" + (retryNum + 1) + "/" + maxRetry + ")", e);
                } else {
                    // 最后一次尝试也失败了，抛出异常
                    throw e;
                }
            }
        }

        // 如果所有重试都失败了，返回最后一次的响应或抛出异常
        if (response != null) {
            return response;
        } else if (lastException != null) {
            throw lastException;
        } else {
            throw new IOException("Request failed after " + maxRetry + " retries");
        }
    }
}
