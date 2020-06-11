package com.google.android.apps.photolab.storyboard.module;

import com.mp.android.apps.monke.monkeybook.base.MBaseModelImpl;


import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

public class ComicSplashModuleImpl extends MBaseModelImpl {
    public static ComicSplashModuleImpl getInstance() {
        return new ComicSplashModuleImpl();
    }

    public Observable<String> getDownloadUrl() {
        return getRetrofitObject("http://aimanpin.com")
                .create(IComicSplasApi.class).downloadFile("/appview/downloadMPNative");
    }

    public interface IComicSplasApi {
        /**
         * 下载文件
         *
         * @param fileUrl
         * @return
         */
        @Streaming //大文件时要加不然会OOM
        @GET
        Observable<String> downloadFile(@Url String fileUrl);

    }

}
