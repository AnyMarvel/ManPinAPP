package com.mp.android.apps.book.common.api;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface IBookInfoApi {
    @GET("/appview/obtainBookInfo")
    Observable<String> obtainBookInfo(
            @Query("noteUrl") String noteUrl
            , @Query("coverUrl") String coverUrl
            , @Query("name") String name
            , @Query("author") String author
            , @Query("lastChapter") String lastChapter
            , @Query("tag") String tag
            , @Query("origin") String origin
            , @Query("kind") String kind
            , @Query("desc") String desc
            , @Query("appVersion") String appVersion);
}
