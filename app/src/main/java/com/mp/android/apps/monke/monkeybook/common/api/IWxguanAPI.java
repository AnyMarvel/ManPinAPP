package com.mp.android.apps.monke.monkeybook.common.api;

import io.reactivex.Observable;
import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface IWxguanAPI {
    @GET
    @Headers({"Accept:text/html,application/xhtml+xml,application/xml",
            "User-Agent:Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.0.3) Gecko/2008092417 Firefox/3.0.3",
            "Accept-Charset:UTF-8",
            "Connection:close",
            "Cache-Control:no-cache"})
    Observable<String> getBookInfo(@Url String url);

    @GET("/s.php")
    @Headers({"Accept:text/html,application/xhtml+xml,application/xml",
            "User-Agent:Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.0.3) Gecko/2008092417 Firefox/3.0.3",
            "Accept-Charset:UTF-8",
            "Connection:close",
            "Cache-Control:no-cache"})
    Observable<String> searchBook(@Query("siteid") String siteid, @Query("q") String s,@Query("ie") String ie);

    @GET
    @Headers({"Accept:text/html,application/xhtml+xml,application/xml",
            "User-Agent:Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.0.3) Gecko/2008092417 Firefox/3.0.3",
            "Accept-Charset:UTF-8",
            "Connection:close",
            "Cache-Control:no-cache"})
    Observable<String> getBookContent(@Url String url);

    @GET
    @Headers({"Accept:text/html,application/xhtml+xml,application/xml",
            "User-Agent:Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.0.3) Gecko/2008092417 Firefox/3.0.3",
            "Accept-Charset:UTF-8",
            "Connection:close",
            "Cache-Control:no-cache"})
    Observable<String> getChapterList(@Url String url);

    @GET
    @Headers({"Accept:text/html,application/xhtml+xml,application/xml",
            "User-Agent:Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.0.3) Gecko/2008092417 Firefox/3.0.3",
            "Accept-Charset:UTF-8",
            "Connection:close",
            "Cache-Control:no-cache"})
    Single<String> getChapterLists(@Url String url);

    @GET
    @Headers({"Accept:text/html,application/xhtml+xml,application/xml",
            "User-Agent:Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.0.3) Gecko/2008092417 Firefox/3.0.3",
            "Accept-Charset:UTF-8",
            "Connection:close",
            "Cache-Control:no-cache"})
    Single<String> getChapterInfo(@Url String url);
}
