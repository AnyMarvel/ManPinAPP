package com.mp.android.apps.book.common.api;

import io.reactivex.Observable;
import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;
import retrofit2.http.Url;

/**
 * 鸟书网(99wx) 书源接口。搜索为 GET /read/search/?searchkey=xxx；详情/目录/正文均为普通 GET 页面。
 */
public interface I99wxApi {

    @GET("/read/search/")
    @Headers({"Accept:text/html,application/xhtml+xml,application/xml",
            "User-Agent:Mozilla/5.0 (Linux; Android 12; SM-G991B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36",
            "Accept-Charset:UTF-8",
            "Connection:close",
            "Cache-Control:no-cache"})
    Observable<String> searchBook(@Query("searchkey") String searchkey);

    @GET
    @Headers({"Accept:text/html,application/xhtml+xml,application/xml",
            "User-Agent:Mozilla/5.0 (Linux; Android 12; SM-G991B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36",
            "Accept-Charset:UTF-8",
            "Connection:close",
            "Cache-Control:no-cache"})
    Observable<String> getBookInfo(@Url String url);

    @GET
    @Headers({"Accept:text/html,application/xhtml+xml,application/xml",
            "User-Agent:Mozilla/5.0 (Linux; Android 12; SM-G991B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36",
            "Accept-Charset:UTF-8",
            "Connection:close",
            "Cache-Control:no-cache"})
    Single<String> getChapterLists(@Url String url);

    @GET
    @Headers({"Accept:text/html,application/xhtml+xml,application/xml",
            "User-Agent:Mozilla/5.0 (Linux; Android 12; SM-G991B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36",
            "Accept-Charset:UTF-8",
            "Connection:close",
            "Cache-Control:no-cache"})
    Single<String> getChapterInfo(@Url String url);
}
