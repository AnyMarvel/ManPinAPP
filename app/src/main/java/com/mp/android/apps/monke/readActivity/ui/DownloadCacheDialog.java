package com.mp.android.apps.monke.readActivity.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hwangjr.rxbus.RxBus;
import com.mp.android.apps.R;
import com.mp.android.apps.monke.monkeybook.bean.DownloadTaskBean;
import com.mp.android.apps.monke.monkeybook.common.RxBusTag;
import com.mp.android.apps.monke.monkeybook.dao.BookChapterBeanDao;
import com.mp.android.apps.monke.readActivity.bean.BookChapterBean;
import com.mp.android.apps.monke.readActivity.bean.CollBookBean;
import com.mp.android.apps.monke.readActivity.local.BookRepository;

import java.util.List;


public class DownloadCacheDialog extends Dialog {
    private TextView tv_download;
    private String bookId;
    private Context context;

    public DownloadCacheDialog(@NonNull Context context, String bookId) {
        super(context);
        this.bookId = bookId;
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
        tv_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CollBookBean collBookBean = BookRepository.getInstance().getCollBook(bookId);
                if (collBookBean != null) {
                    RxBus.get().post(RxBusTag.ADD_DOWNLOAD_TASK, translateCollBooBean(collBookBean));
                } else {
                    Toast.makeText(context, "未加入书架无法离线，请先添加到书架", Toast.LENGTH_LONG).show();
                }

            }
        });
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
