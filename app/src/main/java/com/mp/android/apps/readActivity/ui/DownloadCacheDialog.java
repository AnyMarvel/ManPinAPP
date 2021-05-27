package com.mp.android.apps.readActivity.ui;

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

import com.mp.android.apps.IDownloadBookInterface;
import com.mp.android.apps.R;
import com.mp.android.apps.book.bean.DownloadTaskBean;
import com.mp.android.apps.book.service.DownloadService;
import com.mp.android.apps.readActivity.bean.CollBookBean;
import com.mp.android.apps.readActivity.local.BookRepository;


public class DownloadCacheDialog extends Dialog {
    private TextView tv_download;
    private IDownloadBookInterface downloadBookInterface;
    private TextView manpinDownloadTips;

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

    ServiceConnection serviceConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.read_book_cache_download_dialog);
        tv_download = findViewById(R.id.tv_download);
        manpinDownloadTips = findViewById(R.id.manpin_download_tips);
        Intent serviceIntent = new Intent(context, DownloadService.class);
//        serviceIntent.setAction("com.mp.android.apps.monkeybook.service.DownloadService_action");
//        serviceIntent.setPackage(context.getPackageName());
        CollBookBean collBookBean = BookRepository.getInstance().getCollBook(getBookId());

        manpinDownloadTips.setText(String.format("《%s》", collBookBean.getTitle()));
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                downloadBookInterface = IDownloadBookInterface.Stub.asInterface(service);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };

        context.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        findViewById(R.id.tv_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DownloadCacheDialog.this.dismiss();
            }
        });
        tv_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (collBookBean != null) {
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

    public void unbinderService(Context context) {
        if (serviceConnection != null)
            context.unbindService(serviceConnection);

    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    /**
     * CollBookBean转换为DownloadTaskBean
     *
     * @param collBookBean 本地收藏图书
     * @return 返回 DownloadTaskBean
     */
    private DownloadTaskBean translateCollBooBean(CollBookBean collBookBean) {
        DownloadTaskBean downloadTaskBean = new DownloadTaskBean();
        downloadTaskBean.setBookId(collBookBean.get_id());
        downloadTaskBean.setTaskName(collBookBean.getTitle());
        downloadTaskBean.setCoverUrl(collBookBean.getCover());
        downloadTaskBean.setCurrentChapter(0);
        return downloadTaskBean;
    }


}
