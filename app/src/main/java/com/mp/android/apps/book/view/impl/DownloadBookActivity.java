package com.mp.android.apps.book.view.impl;

import android.content.Intent;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mp.android.apps.R;
import com.mp.android.apps.book.base.MBaseActivity;
import com.mp.android.apps.book.bean.DownloadTaskBean;
import com.mp.android.apps.book.contentprovider.MyContentProvider;
import com.mp.android.apps.book.presenter.IDownloadBookPresenter;
import com.mp.android.apps.book.presenter.impl.DownloadBookPresenterImpl;
import com.mp.android.apps.book.view.IDownloadBookView;
import com.mp.android.apps.book.view.adapter.DownloadBookAdapter;
import com.mp.android.apps.readActivity.ReadActivity;
import com.mp.android.apps.readActivity.bean.CollBookBean;
import com.mp.android.apps.readActivity.local.BookRepository;

import java.util.List;

import butterknife.BindView;

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

        downloadBookAdapter = new DownloadBookAdapter(this, downloadTaskBeanList, new DownloadBookAdapter.ItemClickListener() {
            @Override
            public void onClick(DownloadTaskBean downloadTaskBean) {

                CollBookBean collBookBean=BookRepository.getInstance().getCollBook(downloadTaskBean.getBookId());
                if (collBookBean!=null){
                    Intent intent = new Intent(DownloadBookActivity.this, ReadActivity.class);
                    intent.putExtra("extra_coll_book", collBookBean);
                    intent.putExtra(ReadActivity.EXTRA_IS_COLLECTED, true);
                    startActivityByAnim(intent, android.R.anim.fade_in, android.R.anim.fade_out);
                }else {
                    Toast.makeText(DownloadBookActivity.this, "当前图书已从书架删除，请重新添加到书架", Toast.LENGTH_SHORT).show();
                    BookRepository.getInstance().deleteBookDownload(downloadTaskBean.getBookId());
                    downloadBookAdapter.removeDownloadBean(downloadTaskBean);
                }
            }
        });
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
