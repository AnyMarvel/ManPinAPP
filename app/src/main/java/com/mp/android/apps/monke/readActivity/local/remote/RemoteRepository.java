package com.mp.android.apps.monke.readActivity.local.remote;


import com.mp.android.apps.monke.readActivity.bean.BookChapterBean;
import com.mp.android.apps.monke.readActivity.bean.ChapterInfoBean;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;
import retrofit2.Retrofit;

/**
 * Created by newbiechen on 17-4-20.
 */

public class RemoteRepository {
    private static final String TAG = "RemoteRepository";

    private static RemoteRepository sInstance;
    private Retrofit mRetrofit;
    private BookApi mBookApi;

    private RemoteRepository() {
        mRetrofit = RemoteHelper.getInstance()
                .getRetrofit();

        mBookApi = mRetrofit.create(BookApi.class);
    }

    public static RemoteRepository getInstance() {
        if (sInstance == null) {
            synchronized (RemoteHelper.class) {
                if (sInstance == null) {
                    sInstance = new RemoteRepository();
                }
            }
        }
        return sInstance;
    }


    public Single<List<BookChapterBean>> getBookChapters(String bookId) {
        return mBookApi.getBookChapterPackage(bookId, "chapter")
                .map(bean -> {
                    if (bean.getMixToc() == null) {
                        return new ArrayList<BookChapterBean>(1);
                    } else {
                        return bean.getMixToc().getChapters();
                    }
                });
    }

    /**
     * 注意这里用的是同步请求
     *
     * @param url
     * @return
     */
    public Single<ChapterInfoBean> getChapterInfo(String url) {
        return mBookApi.getChapterInfoPackage(url)
                .map(bean -> bean.getChapter());
    }

}
