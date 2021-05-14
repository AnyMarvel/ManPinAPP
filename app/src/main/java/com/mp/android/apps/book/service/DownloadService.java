
package com.mp.android.apps.book.service;


import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.mp.android.apps.IDownloadBookInterface;
import com.mp.android.apps.book.bean.DownloadTaskBean;
import com.mp.android.apps.book.contentprovider.MyContentProvider;
import com.mp.android.apps.book.dao.BookChapterBeanDao;
import com.mp.android.apps.book.dao.DownloadTaskBeanDao;
import com.mp.android.apps.book.model.WebBookModelControl;
import com.mp.android.apps.book.utils.NetworkUtils;
import com.mp.android.apps.readActivity.base.BaseService;
import com.mp.android.apps.readActivity.bean.BookChapterBean;
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

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;


import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
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
        List<CollBookBean> collBookBeanList = BookRepository.getInstance().getCollBooks();

        for (CollBookBean collBookBean : collBookBeanList) {
            WebBookModelControl.getInstance().getBookChapters(collBookBean).toObservable().flatMap(new Function<List<BookChapterBean>, Observable<?>>() {
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
                            if (bookChapterBeans.size() > taskChapters.size()) {
                                BookRepository.getInstance().saveBookChaptersWithAsync(bookChapterBeans);
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
                    .subscribe();
        }

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

                List<BookChapterBean> taskChapters = BookRepository.getInstance().getSession()
                        .getBookChapterBeanDao()
                        .queryBuilder()
                        .where(BookChapterBeanDao.Properties.BookId.eq(taskEvent.getBookId()))
                        .list();
                taskEvent.setBookChapters(taskChapters);
                taskEvent.setLastChapter(taskChapters.size());


                DownloadTaskBean downloadTaskBean = BookRepository.getInstance().getSession().getDownloadTaskBeanDao()
                        .queryBuilder().where(DownloadTaskBeanDao.Properties.TaskName.eq(taskEvent.getTaskName())).unique();

                taskEvent.setStatus(DownloadTaskBean.STATUS_LOADING);

                if (downloadTaskBean == null) {
                    downloadTaskBean = taskEvent;
                    BookRepository.getInstance().saveDownloadTask(taskEvent);
                } else {

                    if (taskEvent.getBookChapterList().size() != downloadTaskBean.getBookChapterList().size()) {
                        downloadTaskBean.setBookChapters(taskEvent.getBookChapterList());
                    } else {
                        //如果数据库中列表内容未更新，且标志位未完成状态，则舍弃当前下载任务
                        if (downloadTaskBean.getStatus() == DownloadTaskBean.STATUS_FINISH) {
                            return;
                        }
                    }
                    downloadTaskBean.setStatus(DownloadTaskBean.STATUS_LOADING);
                    downloadTaskBean.update();
                }


                List<BookChapterBean> bookChapterBeans = taskEvent.getBookChapters();
                //调用for循环，下载缓存文件
                for (int i = taskEvent.getCurrentChapter(); i < bookChapterBeans.size(); ++i) {

                    BookChapterBean bookChapterBean = bookChapterBeans.get(i);
                    Logger.d("======= 开始下载" + bookChapterBean.getTitle());
                    //首先判断该章节是否曾经被加载过 (从文件中判断)
                    if (BookManager
                            .isChapterCached(taskEvent.getBookId(), bookChapterBean.getTitle())) {

                        //设置任务进度
                        taskEvent.setCurrentChapter(i);

                        //无需进行下一步，跳出当次循环，执行下一次循环
                        continue;
                    }

                    //判断网络是否出问题
                    if (!NetworkUtils.isAvailable()) {
                        //章节加载失败
                        taskEvent.setStatus(DownloadTaskBean.STATUS_PAUSE);
                        //结束循环体，提出for循环
                        break;
                    }

                    //加载数据
                    int result = loadChapter(taskEvent.getBookId(), bookChapterBean);
                    //去除容错
//                    if (result == 1) {
//                        taskEvent.setStatus(DownloadTaskBean.STATUS_PAUSE);
//                        break;
//                    }
                    taskEvent.setCurrentChapter(i + 1);
                    Logger.d("=================:" + i);
                    downloadTaskBean.setCurrentChapter(i + 1);
                    downloadTaskBean.update();


                    getContentResolver().notifyChange(MyContentProvider.CONTENT_URI, null);
                }

                if (taskEvent.getStatus() == DownloadTaskBean.STATUS_PAUSE ||
                        taskEvent.getCurrentChapter() < taskEvent.getBookChapters().size()) {
                    taskEvent.setStatus(DownloadTaskBean.STATUS_PAUSE);//Task的状态
                } else {
                    //存储DownloadTask的状态
                    taskEvent.setStatus(DownloadTaskBean.STATUS_FINISH);//Task的状态
                    taskEvent.setCurrentChapter(taskEvent.getBookChapters().size());//当前下载的章节数量
                    taskEvent.setSize(BookManager.getBookSize(taskEvent.getBookId()));//Task的大小
                }


                //green dao 数据库升级问题需要进行解决才能开启数据状态存储
                //存储状态
                downloadTaskBean = taskEvent;

                //Entity is detached from DAO context
                downloadTaskBean.__setDaoSession(DaoDbHelper.getInstance().getSession());
                downloadTaskBean.update();
                getContentResolver().notifyChange(MyContentProvider.CONTENT_URI, null);
                //轮询下一个事件，用RxBus用来保证事件是在主线程

                //移除完成的任务
                mDownloadTaskQueue.remove(taskEvent);
                //设置为空闲

            }
        };

        mCachedExecutor.execute(runnable);
    }

    private int loadChapter(String folderName, BookChapterBean bean) {
        //加载的结果参数
        final int[] result = {0};

        //问题:(这里有个问题，就是body其实比较大，如何获取数据流而不是对象，)是不是直接使用OkHttpClient交互会更好一点
        Disposable disposable = WebBookModelControl.getInstance()
                .getChapterInfo(bean.getLink())
                .subscribeOn(Schedulers.io())
                //表示在当前环境下执行
                .subscribe(
                        chapterInfo -> {
                            //TODO:这里文件的名字用的是BookChapter的title,而不是chapter的title。
                            //原因是Chapter的title可能重复，但是BookChapter的title不会重复
                            //BookChapter的title = 卷名 + 章节名 chapter 的 title 就是章节名。。
                            BookRepository.getInstance()
                                    .saveChapterInfo(folderName, bean.getTitle(), chapterInfo.getBody());
                        },
                        e -> {
                            //当前进度加载错误（这里需要判断是什么问题，根据相应的问题做出相应的回答）
                            Logger.e("DownloadService", e);
                            //设置加载结果
                            result[0] = 1;
                        }
                );
        addDisposable(disposable);
        return result[0];
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