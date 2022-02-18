
package com.mp.android.apps.book.service;


import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;

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
import java.util.List;
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
//        try {
//            List<CollBookBean> collBookBeanList = BookRepository.getInstance().getCollBooks();
//            if (collBookBeanList != null && collBookBeanList.size() > 0) {
//                for (CollBookBean collBookBean : collBookBeanList) {
//                    WebBookModelControl.getInstance().getBookChapters(collBookBean).toObservable()
//                            .flatMap(new Function<List<BookChapterBean>, Observable<?>>() {
//                                @Override
//                                public Observable<?> apply(@NonNull List<BookChapterBean> bookChapterBeans) throws Exception {
//                                    return Observable.create(new ObservableOnSubscribe<Boolean>() {
//
//                                        @Override
//                                        public void subscribe(@NonNull ObservableEmitter<Boolean> emitter) throws Exception {
//                                            collBookBean.__setDaoSession(BookRepository.getInstance().getSession());
//
//                                            List<BookChapterBean> taskChapters = BookRepository.getInstance().getSession()
//                                                    .getBookChapterBeanDao()
//                                                    .queryBuilder()
//                                                    .where(BookChapterBeanDao.Properties.BookId.eq(collBookBean.get_id()))
//                                                    .list();
//                                            if (bookChapterBeans.size() > taskChapters.size()) {
//                                                BookRepository.getInstance().saveBookChaptersWithAsync(bookChapterBeans);
//                                                emitter.onNext(true);
//                                            } else {
//                                                emitter.onNext(false);
//                                            }
//                                            emitter.onComplete();
//                                        }
//                                    });
//                                }
//                            })
//                            .subscribeOn(Schedulers.io())
//                            .observeOn(AndroidSchedulers.mainThread())
//                            .subscribe();
//                }
//
//            }
//        } catch (Exception e) {
//
//        }

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MyDownloadBinder();
    }


    public void startTask(DownloadTaskBean taskEvent) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Logger.d("======= 异步任务开始执行");
                Object object=new Object();
                synchronized (object) {
                    List<BookChapterBean> taskChapters = BookRepository.getInstance().getSession()
                            .getBookChapterBeanDao()
                            .queryBuilder()
                            .where(BookChapterBeanDao.Properties.BookId.eq(taskEvent.getBookId()))
                            .list();

                    DownloadTaskBean downloadTaskBean = BookRepository.getInstance().getSession().getDownloadTaskBeanDao()
                            .queryBuilder().where(DownloadTaskBeanDao.Properties.TaskName.eq(taskEvent.getTaskName())).unique();


                    if (downloadTaskBean == null) {
                        downloadTaskBean = taskEvent;
                        BookRepository.getInstance().saveDownloadTask(downloadTaskBean);
                    } else {
                        //如果数据库中列表内容未更新，且标志位未完成状态，则舍弃当前下载任务
                        if (downloadTaskBean.getStatus() == DownloadTaskBean.STATUS_FINISH) {
                            return;
                        }
                    }

                    //下载任务初始化
                    downloadTaskBean.setBookChapters(taskChapters);
                    downloadTaskBean.setLastChapter(taskChapters.size());
                    downloadTaskBean.setStatus(DownloadTaskBean.STATUS_LOADING);
                    downloadTaskBean.update();

                    //更新图书状态
                    getContentResolver().notifyChange(MyContentProvider.CONTENT_URI, null);


                    List<BookChapterBean> bookChapterBeans = downloadTaskBean.getBookChapters();

                    AtomicInteger currentDownload = new AtomicInteger();

                    StringBuilder interrupt = new StringBuilder();

                    int totalBookChapterSize = bookChapterBeans.size();
                    //缓存全本，下载全本内容
                    for (int i = 0; i < totalBookChapterSize; ++i) {

                        BookChapterBean bookChapterBean = bookChapterBeans.get(i);
                        Logger.d("======= 开始下载" + bookChapterBean.getTitle());
                        //首先判断该章节是否曾经被加载过 (从文件中判断)
                        if (BookManager
                                .isChapterCached(downloadTaskBean.getBookId(), bookChapterBean.getTitle())) {

                            //设置任务进度
                            downloadTaskBean.setCurrentChapter(currentDownload.get());
                            currentDownload.incrementAndGet();
                            //无需进行下一步，跳出当次循环，执行下一次循环
                            continue;
                        }

                        //判断网络是否出问题
                        if (!NetworkUtils.isAvailable()) {
                            //章节加载失败
                            downloadTaskBean.setStatus(DownloadTaskBean.STATUS_PAUSE);
                            Logger.d("======= 中断解锁 ********");
                            object.notify();
                            //结束循环体，提出for循环
                            break;
                        }
                        AtomicInteger errorNumber = new AtomicInteger();

                        //加载数据
                        loadChapter(currentDownload, downloadTaskBean, bookChapterBean, errorNumber, totalBookChapterSize, object,interrupt);
                    }
                    Logger.d("======= 加锁 ********");
                    try {
                        object.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Logger.d("======= 解锁 ********");

                    //存储DownloadTask的状态
                    if (interrupt.toString().contains("True")){
                        downloadTaskBean.setStatus(DownloadTaskBean.STATUS_PAUSE);//Task的状态
                    }else {
                        downloadTaskBean.setStatus(DownloadTaskBean.STATUS_FINISH);//Task的状态
                    }
                    downloadTaskBean.setCurrentChapter(downloadTaskBean.getBookChapters().size());//当前下载的章节数量
                    downloadTaskBean.setSize(BookManager.getBookSize(downloadTaskBean.getBookId()));//Task的大小

                    //Entity is detached from DAO context
                    downloadTaskBean.__setDaoSession(DaoDbHelper.getInstance().getSession());
                    downloadTaskBean.update();
                    getContentResolver().notifyChange(MyContentProvider.CONTENT_URI, null);
                    //轮询下一个事件，用RxBus用来保证事件是在主线程
                }
                //移除完成的任务
                mDownloadTaskQueue.remove(taskEvent);
                //设置为空闲
            }
        };

        mCachedExecutor.execute(runnable);
    }

    private void loadChapter(AtomicInteger currentDownload,DownloadTaskBean downloadTaskBean,
                             BookChapterBean bean,AtomicInteger errorNumber,int totalSize,Object object, StringBuilder interrupt) {
        //加载的结果参数
        Disposable disposable = WebBookModelControl.getInstance()
                .getChapterInfo(bean.getLink())
                .subscribeOn(Schedulers.io())
                //表示在当前环境下执行
                .subscribe(new Consumer<ChapterInfoBean>() {

                    @Override
                    public void accept(ChapterInfoBean chapterInfo) throws Exception {
                        //Chapter的title可能重复，但是BookChapter的title不会重复
                        //BookChapter的title = 卷名 + 章节名 chapter 的 title 就是章节名

                        BookRepository.getInstance()
                                .saveChapterInfo(downloadTaskBean.getBookId(), bean.getTitle(), chapterInfo.getBody());
                        //设置当前已下载进度
                        int current = currentDownload.incrementAndGet();
                        downloadTaskBean.setCurrentChapter(current);
                        downloadTaskBean.update();
                        Logger.d("======= 下载成功——current:" + String.valueOf(current) + "__totalSize:" + String.valueOf(totalSize));
                        if (current == totalSize) {
                            synchronized (object) {
                                Logger.d("======= 解锁" + String.valueOf(current));
                                object.notify();
                            }
                        }
                        getContentResolver().notifyChange(MyContentProvider.CONTENT_URI, null);

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        //当前进度加载错误（这里需要判断是什么问题，根据相应的问题做出相应的回答）
                        Logger.e("DownloadService retry", throwable);
                        Logger.d("======= 下载失败——current:" + String.valueOf(currentDownload.get())+"__totalSize:"+String.valueOf(totalSize));
                        if (errorNumber.get() < 3) {
                            //重试逻辑，单章错误重试3次
                            errorNumber.incrementAndGet();
                            loadChapter(currentDownload, downloadTaskBean, bean, errorNumber,totalSize,object,interrupt);
                        }else {
                            //错误三次，跳过当前下载，标记中断标识
                            int current = currentDownload.incrementAndGet();
                            downloadTaskBean.setCurrentChapter(current);
                            downloadTaskBean.update();
                            interrupt.append("True");
                            getContentResolver().notifyChange(MyContentProvider.CONTENT_URI, null);
                        }
                    }
                });
        addDisposable(disposable);
    }

    /**
     * binder的接口实现
     */
    class MyDownloadBinder extends IDownloadBookInterface.Stub {
        @Override
        public void addTask(DownloadTaskBean taskEvent) throws RemoteException {
            //判断是否为轮询请求
            if (!TextUtils.isEmpty(taskEvent.getBookId())) {
                if (!mDownloadTaskQueue.contains(taskEvent)) {
                    //加入总列表中，表示创建，修改CollBean的状态。
                    mDownloadTaskQueue.add(taskEvent);
                }
            }
            startTask(taskEvent);
        }
    }

}