
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
import com.mp.android.apps.read.base.BaseService;
import com.mp.android.apps.read.bean.BookChapterBean;
import com.mp.android.apps.read.bean.ChapterInfoBean;
import com.mp.android.apps.read.bean.CollBookBean;
import com.mp.android.apps.read.local.BookRepository;
import com.mp.android.apps.read.local.DaoDbHelper;
import com.mp.android.apps.read.utils.BookManager;
import com.mp.android.apps.utils.Logger;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
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

    //单本书下载时的并发章节数上限：有界并发既提速又避免瞬间上千请求打挂书源
    private static final int DOWNLOAD_CONCURRENCY = 5;

    //加载队列 //目前无用
    private final List<DownloadTaskBean> mDownloadTaskQueue = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.d("======onCreate 同步本地数据,同步书架数据");
        //放到单个后台线程顺序执行，避免一次性并发所有书架书的联网请求
        mCachedExecutor.execute(this::syncShelfBooksUpdate);
    }

    /**
     * 逐本检查书架书籍是否有更新(网络章节数 > 本地章节数则标记)。
     * 顺序执行、单本独立 try/catch：某本书源失效也不会中断其余书的检查。
     */
    private void syncShelfBooksUpdate() {
        try {
            List<CollBookBean> collBookBeanList = BookRepository.getInstance().getCollBooks();
            if (collBookBeanList == null) {
                return;
            }
            for (CollBookBean collBookBean : collBookBeanList) {
                try {
                    //本地书无需联网检查更新
                    if (collBookBean.isLocal()) {
                        continue;
                    }
                    io.reactivex.Single<List<BookChapterBean>> single =
                            WebBookModelControl.getInstance().getBookChapters(collBookBean);
                    //书源已失效(无匹配 model)时返回 null，跳过而不是让整个循环 NPE 中断
                    if (single == null) {
                        continue;
                    }
                    List<BookChapterBean> netChapters = single.blockingGet();

                    collBookBean.__setDaoSession(BookRepository.getInstance().getSession());
                    List<BookChapterBean> localChapters = BookRepository.getInstance().getSession()
                            .getBookChapterBeanDao()
                            .queryBuilder()
                            .where(BookChapterBeanDao.Properties.BookId.eq(collBookBean.get_id()))
                            .list();

                    if (netChapters != null && localChapters != null
                            && netChapters.size() > localChapters.size()) {
                        //标记图书已更新
                        collBookBean.setUpdate(true);
                        collBookBean.update();
                    }
                } catch (Exception e) {
                    android.util.Log.e("DownloadService",
                            "sync update check failed: " + collBookBean.getTitle(), e);
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

                final DownloadTaskBean taskBeanRef = downloadTaskBean;
                final String bookId = downloadTaskBean.getBookId();
                final AtomicInteger currentDownload = new AtomicInteger(0);
                //networkLost：网络中断 → 停止调度剩余章节(等价原串行版的 break)
                //anyFailed：单章下载失败 → 只标记任务未完成，但仍继续下其余章节(对齐原串行语义)
                final AtomicBoolean networkLost = new AtomicBoolean(false);
                final AtomicBoolean anyFailed = new AtomicBoolean(false);

                //有界并发下载：固定 DOWNLOAD_CONCURRENCY 个线程同时下载，其余章节在池队列排队。
                //同时在飞的请求数被限定在小范围，既比串行快数倍，又不会回到早期
                //"一次性并发上千请求把书源打挂"的并发风暴。HTTP 下载与文件写都在池线程并发进行，
                //只有进度更新(共享 greenDAO session 写)通过 onChapterProcessed 串行化。
                ExecutorService downloadPool = Executors.newFixedThreadPool(DOWNLOAD_CONCURRENCY);
                for (int i = 0; i < totalBookChapterSize; ++i) {
                    final BookChapterBean bookChapterBean = bookChapterBeans.get(i);
                    downloadPool.execute(() -> {
                        //网络已断，不再发起新的下载
                        if (networkLost.get()) {
                            return;
                        }
                        //已缓存的章节直接跳过并计入进度
                        if (BookManager.isChapterCached(bookId, bookChapterBean.getTitle())) {
                            onChapterProcessed(taskBeanRef, currentDownload);
                            return;
                        }
                        //网络不可用则暂停整个任务
                        if (!NetworkUtils.isAvailable()) {
                            networkLost.set(true);
                            return;
                        }
                        Logger.d("======= 开始下载" + bookChapterBean.getTitle());
                        boolean ok = downloadChapter(bookId, bookChapterBean);
                        if (!ok) {
                            anyFailed.set(true);
                        }
                        onChapterProcessed(taskBeanRef, currentDownload);
                    });
                }
                //不再接受新任务，等待已提交的全部章节处理完毕
                downloadPool.shutdown();
                try {
                    downloadPool.awaitTermination(7, TimeUnit.DAYS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    networkLost.set(true);
                    downloadPool.shutdownNow();
                }

                //存储 DownloadTask 的最终状态：任何章节失败/网络中断都视为未完成(暂停)
                if (networkLost.get() || anyFailed.get()) {
                    downloadTaskBean.setStatus(DownloadTaskBean.STATUS_PAUSE);
                } else {
                    downloadTaskBean.setStatus(DownloadTaskBean.STATUS_FINISH);
                }
                downloadTaskBean.setCurrentChapter(currentDownload.get());
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
     * 单章处理(下载成功/已缓存/失败)完成后回调：累加进度并按节流刷新 UI。
     * synchronized：并发下载线程会同时调用，需串行化对共享 downloadTaskBean / greenDAO
     * session 的写入(update)，避免 session 竞态；下载与文件写已在锁外并发完成，此处很轻量。
     */
    private synchronized void onChapterProcessed(DownloadTaskBean taskBean, AtomicInteger counter) {
        int done = counter.incrementAndGet();
        taskBean.setCurrentChapter(done);
        try {
            taskBean.update();
        } catch (Exception e) {
            android.util.Log.e("DownloadService", "update progress failed", e);
        }
        //节流：每 5 章通知一次刷新，避免每章都触发主线程查库+目录全量重绑造成卡顿
        //(下载循环结束后仍会再通知一次，保证最终进度/已下载标记及时刷新)
        if (done % 5 == 0) {
            getContentResolver().notifyChange(MyContentProvider.CONTENT_URI, null);
        }
    }

    /**
     * 下载单个章节，最多重试 3 次。运行在并发下载线程池中，使用 blockingGet 同步获取。
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
            //重试前退避(400ms、800ms)，避免失败后连续快速请求打压书源
            if (attempt < 3) {
                try {
                    Thread.sleep(400L * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return false;
                }
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