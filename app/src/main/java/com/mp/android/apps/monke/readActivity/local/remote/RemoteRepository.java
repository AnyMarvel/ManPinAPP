package com.mp.android.apps.monke.readActivity.local.remote;


import com.mp.android.apps.monke.monkeybook.bean.BookShelfBean;
import com.mp.android.apps.monke.monkeybook.bean.ChapterListBean;
import com.mp.android.apps.monke.monkeybook.dao.ChapterListBeanDao;
import com.mp.android.apps.monke.monkeybook.dao.DbHelper;
import com.mp.android.apps.monke.monkeybook.listener.OnGetChapterListListener;
import com.mp.android.apps.monke.monkeybook.model.impl.ReaderContentWxguanModelImpl;
import com.mp.android.apps.monke.monkeybook.model.impl.WebBookModelImpl;
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
        List<ChapterListBean> chapterListBeans = DbHelper.getInstance().getmDaoSession().getChapterListBeanDao().queryBuilder().where(ChapterListBeanDao.Properties.NoteUrl.eq(bookId)).build().list();
        if (chapterListBeans == null || chapterListBeans.size() == 0) return null;
        List<BookChapterBean> chapters=new ArrayList<>();
        for (ChapterListBean chapterListBean:chapterListBeans){
            BookChapterBean bookChapterBean=new BookChapterBean();
            bookChapterBean.setId(chapterListBean.getNoteUrl());
            bookChapterBean.setTitle(chapterListBean.getDurChapterName());
            bookChapterBean.setLink(chapterListBean.getDurChapterUrl());
            bookChapterBean.setUnreadble(false);
            chapters.add(bookChapterBean);
        }

        return Single.create(new SingleOnSubscribe<List<BookChapterBean>>() {
            @Override
            public void subscribe(SingleEmitter<List<BookChapterBean>> emitter) throws Exception {
                emitter.onSuccess(chapters);
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

        return ReaderContentWxguanModelImpl.getInstance().getChapterInfo(url);


//        return mBookApi.getChapterInfoPackage(url)
//                .map(bean -> bean.getChapter());
    }

}
