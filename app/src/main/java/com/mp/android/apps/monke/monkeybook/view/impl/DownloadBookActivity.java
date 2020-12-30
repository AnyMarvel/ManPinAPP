package com.mp.android.apps.monke.monkeybook.view.impl;

import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mp.android.apps.R;
import com.mp.android.apps.monke.monkeybook.base.MBaseActivity;
import com.mp.android.apps.monke.monkeybook.presenter.IDownloadBookPresenter;
import com.mp.android.apps.monke.monkeybook.presenter.impl.DownloadBookPresenterImpl;
import com.mp.android.apps.monke.monkeybook.view.IDownloadBookView;
import com.mp.android.apps.monke.monkeybook.view.adapter.DownloadBookAdapter;

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
    DownloadBookAdapter downloadBookAdapter;

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

    }

    @Override
    protected void bindView() {
        super.bindView();
        tvTitle.setText("下载列表");
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        downloadRecycle.setLayoutManager(layoutManager);
        downloadRecycle.setItemAnimator(new DefaultItemAnimator());
        downloadBookAdapter = new DownloadBookAdapter(this, null);
        downloadRecycle.setAdapter(downloadBookAdapter);
    }

    @Override
    protected void bindEvent() {
        super.bindEvent();
        ivBack.setOnClickListener(v -> {
            onBackPressed();
        });

    }
}
