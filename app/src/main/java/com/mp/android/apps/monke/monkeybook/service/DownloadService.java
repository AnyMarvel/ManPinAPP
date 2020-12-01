
package com.mp.android.apps.monke.monkeybook.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.mp.android.apps.MyApplication;
import com.mp.android.apps.R;
import com.mp.android.apps.monke.monkeybook.bean.DownloadTaskBean;
import com.mp.android.apps.monke.monkeybook.common.RxBusTag;
import com.mp.android.apps.monke.monkeybook.model.impl.WebBookModelImpl;
import com.mp.android.apps.monke.monkeybook.utils.NetworkUtils;
import com.mp.android.apps.monke.readActivity.base.BaseService;
import com.mp.android.apps.monke.readActivity.bean.BookChapterBean;
import com.mp.android.apps.monke.readActivity.local.BookRepository;
import com.mp.android.apps.monke.readActivity.utils.BookManager;
import com.mp.android.apps.utils.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.disposables.Disposable;


public class DownloadService extends BaseService {
    //加载状态
    private static final int LOAD_ERROR = -1; //加载失败
    private static final int LOAD_NORMAL = 0; //增长加载
    private static final int LOAD_PAUSE = 1; // 暂停加载
    private static final int LOAD_DELETE = 2; //正在加载时候，用户删除收藏书籍的情况。

    //线程池
    private final ExecutorService mSingleExecutor = Executors.newSingleThreadExecutor();
    //加载队列
    private final List<DownloadTaskBean> mDownloadTaskQueue = Collections.synchronizedList(new ArrayList<>());

    //包含所有的DownloadTask
    private List<DownloadTaskBean> mDownloadTaskList;

    private Boolean isInit = false;
    public static final String CHANNEL_ID_STRING = "service_01";
    // serve前台service channel通知
    private Notification notification;
    private boolean isBusy = false;
    private boolean isCancel = false;

    @Override
    public void onCreate() {
        super.onCreate();
        NotificationManager notificationManager = (NotificationManager) MyApplication.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel mChannel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            mChannel = new NotificationChannel(CHANNEL_ID_STRING, getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(mChannel);
            notification = new Notification.Builder(getApplicationContext(), CHANNEL_ID_STRING).build();
            startForeground(1, notification);
        }
        mDownloadTaskList = new ArrayList<>();
//        BookRepository.getInstance().getSession().getDownloadTaskBeanDao().loadAll()
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (RxBus.get() != null) {
            RxBus.get().unregister(this);
        }
        isInit = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForeground(1, notification);
        }

        if (!isInit) {
            isInit = true;
            RxBus.get().register(this);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Subscribe(
            thread = EventThread.MAIN_THREAD,
            tags = {
                    @Tag(RxBusTag.START_DOWNLOAD)
            }
    )
    public void startTask(DownloadTaskBean taskEvent) {
        Runnable runnable = () -> {

            taskEvent.setStatus(DownloadTaskBean.STATUS_LOADING);

            int result = LOAD_NORMAL;
            List<BookChapterBean> bookChapterBeans = taskEvent.getBookChapters();

            //调用for循环，下载数据
            for (int i = taskEvent.getCurrentChapter(); i < bookChapterBeans.size(); ++i) {

                BookChapterBean bookChapterBean = bookChapterBeans.get(i);
                //首先判断该章节是否曾经被加载过 (从文件中判断)
                if (BookManager
                        .isChapterCached(taskEvent.getBookId(), bookChapterBean.getTitle())) {

                    //设置任务进度
                    taskEvent.setCurrentChapter(i);

                    //章节加载完成
//                    postDownloadChange(taskEvent, DownloadTaskBean.STATUS_LOADING, i + "");
                    RxBus.get().post(RxBusTag.FINISH_DOWNLOAD_LISTENER, taskEvent);

                    //无需进行下一步
                    continue;
                }

                //判断网络是否出问题
                if (!NetworkUtils.isAvailable()) {
                    //章节加载失败
                    result = LOAD_ERROR;
                    break;
                }

                if (isCancel) {
                    result = LOAD_PAUSE;
                    isCancel = false;
                    break;
                }

                //加载数据
                result = loadChapter(taskEvent.getBookId(), bookChapterBean);
                //章节加载完成
                if (result == LOAD_NORMAL) {
                    taskEvent.setCurrentChapter(i);
                    RxBus.get().post(RxBusTag.PROGRESS_DOWNLOAD_LISTENER, taskEvent);//章节下载完成,需要更新进度
                    Logger.d("ssssssssssssssss", RxBusTag.PROGRESS_DOWNLOAD_LISTENER);
                }
                //章节加载失败
                else {
                    //遇到错误退出
                    break;
                }
            }


            if (result == LOAD_NORMAL) {
                //存储DownloadTask的状态
                taskEvent.setStatus(DownloadTaskBean.STATUS_FINISH);//Task的状态
                taskEvent.setCurrentChapter(taskEvent.getBookChapters().size());//当前下载的章节数量
                taskEvent.setSize(BookManager.getBookSize(taskEvent.getBookId()));//Task的大小

                //发送完成状态
                RxBus.get().post(RxBusTag.FINISH_DOWNLOAD_LISTENER, taskEvent);//下载完成

            } else if (result == LOAD_ERROR) {
                taskEvent.setStatus(DownloadTaskBean.STATUS_ERROR);//Task的状态
                //任务加载失败
                //资源或网络错误
                RxBus.get().post(RxBusTag.ERROR_DOWNLOAD_LISTENER, taskEvent);
            } else if (result == LOAD_PAUSE) {
                taskEvent.setStatus(DownloadTaskBean.STATUS_PAUSE);//Task的状态
                RxBus.get().post(RxBusTag.PAUSE_DOWNLOAD_LISTENER, taskEvent);//暂停加载监听状态
            } else if (result == LOAD_DELETE) {
                //没想好怎么做
            }

            //存储状态
            BookRepository.getInstance().saveDownloadTask(taskEvent);

            //轮询下一个事件，用RxBus用来保证事件是在主线程

            //移除完成的任务
            mDownloadTaskQueue.remove(taskEvent);
            //设置为空闲
            isBusy = false;
        };
        mSingleExecutor.execute(runnable);
    }

    private int loadChapter(String folderName, BookChapterBean bean) {
        //加载的结果参数
        final int[] result = {LOAD_NORMAL};

        //问题:(这里有个问题，就是body其实比较大，如何获取数据流而不是对象，)是不是直接使用OkHttpClient交互会更好一点
        Disposable disposable = WebBookModelImpl.getInstance()
                .getChapterInfo(bean.getLink())
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
                            result[0] = LOAD_ERROR;
                        }
                );
        addDisposable(disposable);
        return result[0];
    }

    @Subscribe(
            thread = EventThread.MAIN_THREAD,
            tags = {
                    @Tag(RxBusTag.ADD_DOWNLOAD_TASK)
            }
    )
    public void addTask(DownloadTaskBean taskEvent) {
        //判断是否为轮询请求
        if (!TextUtils.isEmpty(taskEvent.getBookId())) {

            if (!mDownloadTaskList.contains(taskEvent)) {
                //加入总列表中，表示创建，修改CollBean的状态。
                mDownloadTaskList.add(taskEvent);
            }
            // 添加到下载队列
            mDownloadTaskQueue.add(taskEvent);
        }

        // 从队列顺序取出第一条下载
        if (mDownloadTaskQueue.size() > 0) {
            startTask(mDownloadTaskQueue.get(0));
        }
    }

    @Subscribe(
            thread = EventThread.MAIN_THREAD,
            tags = {
                    @Tag(RxBusTag.CANCEL_DOWNLOAD)
            }
    )
    public void cancelTask(Object o) {

    }

    @Subscribe(
            thread = EventThread.MAIN_THREAD,
            tags = {
                    @Tag(RxBusTag.PAUSE_DOWNLOAD)
            }
    )
    public void pauseTask(Object o) {

    }
}