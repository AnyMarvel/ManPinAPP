package com.mp.android.apps.monke.readActivity.local.remote;


import com.mp.android.apps.monke.monkeybook.bean.ChapterListBean;
import com.mp.android.apps.monke.monkeybook.dao.ChapterListBeanDao;
import com.mp.android.apps.monke.monkeybook.dao.DbHelper;
import com.mp.android.apps.monke.monkeybook.model.impl.ReaderContentWxguanModelImpl;
import com.mp.android.apps.monke.readActivity.bean.BookChapterBean;
import com.mp.android.apps.monke.readActivity.bean.ChapterInfoBean;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import retrofit2.Retrofit;

/**
 * Created by newbiechen on 17-4-20.
 */

public class RemoteRepository {
    private static final String TAG = "RemoteRepository";

    private static RemoteRepository sInstance;

    private RemoteRepository() {
    }

    public static RemoteRepository getInstance() {
        if (sInstance == null) {
            synchronized (RemoteRepository.class) {
                if (sInstance == null) {
                    sInstance = new RemoteRepository();
                }
            }
        }
        return sInstance;
    }

    //根据 图书url获取章节目录
    public Single<List<BookChapterBean>> getBookChapters(String bookId) {

        return ReaderContentWxguanModelImpl.getInstance().getBookChapters(bookId);

    }

    /**
     * 注意这里用的是同步请求
     * 获取章节对应信息内容
     *
     * @param url
     * @return
     */
    public Single<ChapterInfoBean> getChapterInfo(String url) {

        return ReaderContentWxguanModelImpl.getInstance().getChapterInfo(url);
    }

}
