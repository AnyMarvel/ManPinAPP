package com.google.android.apps.photolab.storyboard.download;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

public interface ApiInterface {
    /**
     * 下载文件
     *
     * @param fileUrl
     * @return
     */
    @Headers({"Accept:text/html,application/xhtml+xml,application/xml",
            "User-Agent:Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.0.3) Gecko/2008092417 Firefox/3.0.3",
            "Accept-Charset:UTF-8",
            "Connection:close",
            "Cache-Control:no-cache"})
    @Streaming //大文件时要加不然会OOM
    @GET
    Call<ResponseBody> downloadFile(@Url String fileUrl);

}
