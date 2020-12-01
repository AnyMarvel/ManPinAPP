package com.mp.android.apps.monke.readActivity.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

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
import com.mp.android.apps.monke.readActivity.local.DaoDbHelper;

import java.util.List;

public class DownloadCacheDialog extends Dialog {
    private TextView tv_download;
    private CollBookBean collBookBean;

    public DownloadCacheDialog(@NonNull Context context, CollBookBean collBookBean) {
        super(context);
        this.collBookBean = collBookBean;
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
                RxBus.get().post(RxBusTag.ADD_DOWNLOAD_TASK, translateCollBooBean(collBookBean));
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
