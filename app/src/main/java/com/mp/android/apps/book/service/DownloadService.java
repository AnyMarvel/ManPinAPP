
package com.mp.android.apps.book.service;


import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.mp.android.apps.IDownloadBookInterface;
import com.mp.android.apps.book.bean.DownloadTaskBean;
import com.mp.android.apps.book.contentprovider.MyContentProvider;
import com.mp.android.apps.book.dao.BookChapterBeanDao;
import com.mp.android.apps.book.dao.DownloadTaskBeanDao;
import com.mp.android.apps.book.model.WebBookModelControl;
import com.mp.android.apps.book.utils.NetworkUtils;
import com.mp.android.apps.readActivity.base.BaseService;
import com.mp.android.apps.readActivity.bean.BookChapterBean;
import com.mp.android.apps.readActivity.bean.ChapterInfoBean;
import com.mp.android.apps.readActivity.bean.CollBookBean;
import com.mp.android.apps.readActivity.local.BookRepository;
import com.mp.android.apps.readActivity.local.DaoDbHelper;
import com.mp.android.apps.readActivity.utils.BookManager;
import com.mp.android.apps.utils.Logger;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;


import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.internal.observers.ConsumerSingleObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;


public class DownloadService extends BaseService {

    //线程池
    private final ExecutorService mCachedExecutor = Executors.newCachedThreadPool();

    //加载队列 //目前无用
    private final List<DownloadTaskBean> mDownloadTaskQueue = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.d("======onCreate 同步本地数据,同步书架数据");
        try {
            List<CollBookBean> collBookBeanList = BookRepository.getInstance().getCollBooks();
            if (collBookBeanList != null && collBookBeanList.size() > 0) {
                for (CollBookBean collBookBean : collBookBeanList) {
                    Logger.d("====== 同步本地数据:"+collBookBean.getTitle());
                    WebBookModelControl.getInstance().getBookChapters(collBookBean).toObservable()
                            .flatMap(new Function<List<BookChapterBean>, Observable<?>>() {
                                @Override
                                public Observable<?> apply(@NonNull List<BookChapterBean> bookChapterBeans) throws Exception {
                                    return Observable.create(new ObservableOnSubscribe<Boolean>() {

                                        @Override
                                        public void subscribe(@NonNull ObservableEmitter<Boolean> emitter) throws Exception {
                                            collBookBean.__setDaoSession(BookRepository.getInstance().getSession());

                                            List<BookChapterBean> taskChapters = BookRepository.getInstance().getSession()
                                                    .getBookChapterBeanDao()
                                                    .queryBuilder()
                                                    .where(BookChapterBeanDao.Properties.BookId.eq(collBookBean.get_id()))
                                                    .list();

                                            if (taskChapters==null || bookChapterBeans==null){
                                                return;
                                            }
                                            Log.d("taskChapters======size:", "localSize:"+String.valueOf(taskChapters.size())+"netSize:"+String.valueOf(bookChapterBeans.size()));

                                            if (bookChapterBeans.size() > taskChapters.size()) {
                                                //标记图书已更新
                                                if (!collBookBean.isLocal()){
                                                    collBookBean.setUpdate(true);
                                                    collBookBean.update();
                                                }
                                                emitter.onNext(true);
                                            } else {
                                                emitter.onNext(false);
                                            }
                                            emitter.onComplete();
                                        }
                                    });
                                }
                            })
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(value -> { }, throwable ->
                                    android.util.Log.e("DownloadService", "sync book chapters failed", throwable));
                }

            }
        } catch (Exception e) {
            android.util.Log.e("DownloadService", "onCreate sync shelf books failed", e);
        }

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MyDownloadBinder();
    }


    public void startTask(DownloadTaskBean taskEvent) {
        mCachedExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Logger.d("======= 异步任务开始执行");

                List<BookChapterBean> taskChapters = BookRepository.getInstance().getSession()
                        .getBookChapterBeanDao()
                        .queryBuilder()
                        .where(BookChapterBeanDao.Properties.BookId.eq(taskEvent.getBookId()))
                        .list();
                if (taskChapters == null || taskChapters.isEmpty()) {
                    mDownloadTaskQueue.remove(taskEvent);
                    return;
                }

                DownloadTaskBean downloadTaskBean = BookRepository.getInstance().getSession().getDownloadTaskBeanDao()
                        .queryBuilder().where(DownloadTaskBeanDao.Properties.TaskName.eq(taskEvent.getTaskName())).unique();
                if (downloadTaskBean == null) {
                    downloadTaskBean = taskEvent;
                    BookRepository.getInstance().saveDownloadTask(downloadTaskBean);
                }
                //显式绑定 DaoSession，保证后续 update() 不会因实体 detached 抛异常
                //(taskEvent 经 Binder 传来是 detached 的，且 ReadActivity 会并发 detachAll)
                downloadTaskBean.__setDaoSession(BookRepository.getInstance().getSession());

                //下载任务初始化
                downloadTaskBean.setBookChapters(taskChapters);
                downloadTaskBean.setLastChapter(taskChapters.size());
                downloadTaskBean.setStatus(DownloadTaskBean.STATUS_LOADING);
                downloadTaskBean.update();
                //更新图书状态
                getContentResolver().notifyChange(MyContentProvider.CONTENT_URI, null);

                List<BookChapterBean> bookChapterBeans = downloadTaskBean.getBookChapters();
                int totalBookChapterSize = bookChapterBeans.size();
                int currentDownload = 0;
                boolean interrupted = false;

                //逐章顺序下载：避免一次性并发上千请求把书源打挂，逻辑上也无需 wait/notify
                for (int i = 0; i < totalBookChapterSize; ++i) {
                    BookChapterBean bookChapterBean = bookChapterBeans.get(i);

                    //已缓存的章节直接跳过
                    if (BookManager.isChapterCached(downloadTaskBean.getBookId(), bookChapterBean.getTitle())) {
                        downloadTaskBean.setCurrentChapter(++currentDownload);
                        continue;
                    }

                    //网络不可用则暂停整个任务
                    if (!NetworkUtils.isAvailable()) {
                        downloadTaskBean.setStatus(DownloadTaskBean.STATUS_PAUSE);
                        interrupted = true;
                        break;
                    }

                    Logger.d("======= 开始下载" + bookChapterBean.getTitle());
                    boolean ok = downloadChapter(downloadTaskBean.getBookId(), bookChapterBean);
                    if (!ok) {
                        interrupted = true;
                    }
                    downloadTaskBean.setCurrentChapter(++currentDownload);
                    downloadTaskBean.update();
                    getContentResolver().notifyChange(MyContentProvider.CONTENT_URI, null);
                }

                //存储 DownloadTask 的最终状态：任何章节失败/网络中断都视为未完成(暂停)
                if (interrupted) {
                    if (downloadTaskBean.getStatus() != DownloadTaskBean.STATUS_PAUSE) {
                        downloadTaskBean.setStatus(DownloadTaskBean.STATUS_PAUSE);
                    }
                } else {
                    downloadTaskBean.setStatus(DownloadTaskBean.STATUS_FINISH);
                }
                downloadTaskBean.setCurrentChapter(currentDownload);
                downloadTaskBean.setSize(BookManager.getBookSize(downloadTaskBean.getBookId()));
                //Entity is detached from DAO context
                downloadTaskBean.__setDaoSession(DaoDbHelper.getInstance().getSession());
                downloadTaskBean.update();
                getContentResolver().notifyChange(MyContentProvider.CONTENT_URI, null);

                //移除完成的任务
                mDownloadTaskQueue.remove(taskEvent);
            }
        });
    }


    /**
     * 顺序下载单个章节，最多重试 3 次。
     * 运行在后台线程，使用 blockingGet 同步获取，逻辑清晰，无需 wait/notify。
     *
     * @return 是否下载成功
     */
    private boolean downloadChapter(String bookId, BookChapterBean bean) {
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                io.reactivex.Single<ChapterInfoBean> single =
                        WebBookModelControl.getInstance().getChapterInfo(bean.getLink());
                if (single == null) {
                    //没有匹配的书源(源已失效)，无法下载
                    Logger.d("======= 无匹配书源，跳过：" + bean.getTitle());
                    return false;
                }
                ChapterInfoBean chapterInfo = single.blockingGet();
                if (chapterInfo != null) {
                    //Chapter的title = 卷名 + 章节名，BookChapter的title不会重复
                    BookRepository.getInstance()
                            .saveChapterInfo(bookId, bean.getTitle(), chapterInfo.getBody());
                    Logger.d("======= 下载成功：" + bean.getTitle());
                    return true;
                }
            } catch (Exception e) {
                android.util.Log.e("DownloadService",
                        "download chapter failed (" + attempt + "/3): " + bean.getTitle(), e);
            }
        }
        return false;
    }

    /**
     * binder的接口实现
     */
    class MyDownloadBinder extends IDownloadBookInterface.Stub {
        @Override
        public void addTask(DownloadTaskBean taskEvent) throws RemoteException {
            if (taskEvent == null || TextUtils.isEmpty(taskEvent.getBookId())) {
                return;
            }
            //按 bookId 去重：同一本书正在下载时忽略重复请求，
            //避免两个线程并发写同一批 .nb 文件导致文件损坏/竞态
            synchronized (mDownloadTaskQueue) {
                for (DownloadTaskBean queued : mDownloadTaskQueue) {
                    if (taskEvent.getBookId().equals(queued.getBookId())) {
                        Logger.d("======= 该书已在下载队列，忽略重复请求：" + taskEvent.getBookId());
                        return;
                    }
                }
                mDownloadTaskQueue.add(taskEvent);
            }
            startTask(taskEvent);
        }
    }

}