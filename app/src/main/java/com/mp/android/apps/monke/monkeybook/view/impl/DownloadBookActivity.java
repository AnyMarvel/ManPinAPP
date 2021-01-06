package com.mp.android.apps.monke.monkeybook.view.impl;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mp.android.apps.R;
import com.mp.android.apps.monke.monkeybook.base.MBaseActivity;
import com.mp.android.apps.monke.monkeybook.bean.DownloadTaskBean;
import com.mp.android.apps.monke.monkeybook.common.RxBusTag;
import com.mp.android.apps.monke.monkeybook.contentprovider.MyContentProvider;
import com.mp.android.apps.monke.monkeybook.presenter.IDownloadBookPresenter;
import com.mp.android.apps.monke.monkeybook.presenter.impl.DownloadBookPresenterImpl;
import com.mp.android.apps.monke.monkeybook.view.IDownloadBookView;
import com.mp.android.apps.monke.monkeybook.view.adapter.DownloadBookAdapter;
import com.mp.android.apps.monke.readActivity.local.BookRepository;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DownloadBookActivity extends MBaseActivity<IDownloadBookPresenter> implements IDownloadBookView {
    @BindView(R.id.iv_back)
    ImageView ivBack;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.tv_right_btn)
    TextView tvRightBtn;
    @BindView(R.id.download_recycle)
    RecyclerView downloadRecycle;
    private DownloadBookAdapter downloadBookAdapter;
    private Handler handler = new Handler(Looper.getMainLooper());
    private List<DownloadTaskBean> downloadTaskBeanList;

    @Override
    protected IDownloadBookPresenter initInjector() {
        return new DownloadBookPresenterImpl();
    }

    @Override
    protected void onCreateActivity() {
        setContentView(R.layout.mp_book_download_book_activity);
    }

    @Override
    protected void initData() {
        downloadTaskBeanList = BookRepository.getInstance().getSession().getDownloadTaskBeanDao().queryBuilder().list();
        getContentResolver().registerContentObserver(MyContentProvider.CONTENT_URI, false, contentObserver);
    }

    @Override
    protected void bindView() {
        super.bindView();
        tvTitle.setText("下载列表");
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        downloadRecycle.setLayoutManager(layoutManager);
        downloadRecycle.setItemAnimator(new DefaultItemAnimator());
        downloadRecycle.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        downloadBookAdapter = new DownloadBookAdapter(this, downloadTaskBeanList);
        downloadRecycle.setAdapter(downloadBookAdapter);

    }

    @Override
    protected void bindEvent() {
        super.bindEvent();
        ivBack.setOnClickListener(v -> {
            onBackPressed();
        });

    }

    private ContentObserver contentObserver = new ContentObserver(handler) {

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            downloadBookAdapter.downloadNotifyDataSetChanged();
        }

    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getContentResolver().unregisterContentObserver(contentObserver);
    }

}
