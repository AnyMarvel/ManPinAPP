package com.mp.android.apps.monke.readActivity.ui;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hwangjr.rxbus.RxBus;
import com.mp.android.apps.IDownloadBookInterface;
import com.mp.android.apps.R;
import com.mp.android.apps.monke.monkeybook.bean.DownloadTaskBean;
import com.mp.android.apps.monke.monkeybook.common.RxBusTag;
import com.mp.android.apps.monke.monkeybook.dao.BookChapterBeanDao;
import com.mp.android.apps.monke.readActivity.bean.BookChapterBean;
import com.mp.android.apps.monke.readActivity.bean.CollBookBean;
import com.mp.android.apps.monke.readActivity.local.BookRepository;
import com.mp.android.apps.utils.Logger;

import java.util.List;


public class DownloadCacheDialog extends Dialog {
    private TextView tv_download;
    private IDownloadBookInterface downloadBookInterface;

    private String bookId;
    private Context context;

    public DownloadCacheDialog(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    public DownloadCacheDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected DownloadCacheDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.read_book_cache_download_dialog);
        tv_download = findViewById(R.id.tv_download);
        Intent serviceIntent = new Intent();
        serviceIntent.setAction("com.mp.android.apps.monke.monkeybook.service.DownloadService_action");
        serviceIntent.setPackage(context.getPackageName());
        context.bindService(serviceIntent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                downloadBookInterface = IDownloadBookInterface.Stub.asInterface(service);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        }, Context.BIND_AUTO_CREATE);

        findViewById(R.id.tv_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DownloadCacheDialog.this.dismiss();
            }
        });
        tv_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CollBookBean collBookBean = BookRepository.getInstance().getCollBook(getBookId());
                if (collBookBean != null) {
//                    RxBus.get().post(RxBusTag.ADD_DOWNLOAD_TASK, translateCollBooBean(collBookBean));
                    try {
                        downloadBookInterface.addTask(translateCollBooBean(collBookBean));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(context, "正在离线缓存,可边到书架页面查看缓存进度或取消", Toast.LENGTH_LONG).show();

                } else {
                    Toast.makeText(context, "未加入书架无法离线，请先添加到书架", Toast.LENGTH_LONG).show();
                }
                DownloadCacheDialog.this.dismiss();
            }
        });
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    private DownloadTaskBean translateCollBooBean(CollBookBean collBookBean) {
        DownloadTaskBean downloadTaskBean = new DownloadTaskBean();
        downloadTaskBean.setBookId(collBookBean.get_id());
        downloadTaskBean.setTaskName(collBookBean.getTitle());
        downloadTaskBean.setCoverUrl(collBookBean.getCover());
        List<BookChapterBean> bookChapterBeans = BookRepository.getInstance().getSession()
                .getBookChapterBeanDao()
                .queryBuilder()
                .where(BookChapterBeanDao.Properties.BookId.eq(collBookBean.get_id()))
                .list();
        downloadTaskBean.setBookChapters(bookChapterBeans);
        downloadTaskBean.setCurrentChapter(0);
        downloadTaskBean.setLastChapter(bookChapterBeans.size());
        return downloadTaskBean;
    }
}
