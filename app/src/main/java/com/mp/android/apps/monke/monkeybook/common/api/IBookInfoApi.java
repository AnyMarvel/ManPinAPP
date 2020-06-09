package com.mp.android.apps.monke.monkeybook.common.api;

import com.mp.android.apps.monke.monkeybook.bean.SearchBookBean;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Url;

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
            , @Query("kind") String kind);
}
