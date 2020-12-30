
package com.mp.android.apps.monke.monkeybook.view.impl;

import android.content.Intent;

import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.mp.android.apps.main.MainActivity;
import com.mp.android.apps.monke.monkeybook.BitIntentDataManager;
import com.mp.android.apps.R;
import com.mp.android.apps.monke.monkeybook.base.MBaseActivity;
import com.mp.android.apps.monke.monkeybook.presenter.IMainPresenter;
import com.mp.android.apps.monke.monkeybook.presenter.impl.BookDetailPresenterImpl;
import com.mp.android.apps.monke.monkeybook.presenter.impl.MainPresenterImpl;
import com.mp.android.apps.monke.monkeybook.view.IMainView;
import com.mp.android.apps.monke.monkeybook.view.adapter.BookShelfAdapter;
import com.mp.android.apps.monke.monkeybook.view.popupwindow.DownloadListPop;
import com.mp.android.apps.monke.monkeybook.view.popupwindow.ProxyPop;
import com.mp.android.apps.monke.monkeybook.widget.refreshview.OnRefreshWithProgressListener;
import com.mp.android.apps.monke.monkeybook.widget.refreshview.RefreshRecyclerView;
import com.mp.android.apps.monke.readActivity.ReadActivity;
import com.mp.android.apps.monke.readActivity.bean.CollBookBean;

import java.util.List;

public class BookMainActivity extends MBaseActivity<IMainPresenter> implements IMainView {
    private ImageView ivBack;
    private ImageButton ibMoney;
    private ImageButton ibSettings;
    private ImageButton ibLibrary;
    private ImageButton ibAdd;
    private ImageButton ibDownload;

    private RefreshRecyclerView rfRvShelf;
    private BookShelfAdapter bookShelfAdapter;

    private FrameLayout flWarn;
    private ImageView ivWarnClose;

    private DownloadListPop downloadListPop;
    private ProxyPop proxyPop;

    @Override
    protected IMainPresenter initInjector() {
        return new MainPresenterImpl();
    }


    @Override
    protected void onCreateActivity() {
        setContentView(R.layout.activity_book_main);
    }

    @Override
    protected void initData() {
        bookShelfAdapter = new BookShelfAdapter();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void bindView() {
        proxyPop = new ProxyPop(BookMainActivity.this);
        downloadListPop = new DownloadListPop(BookMainActivity.this);

        ivBack = findViewById(R.id.iv_back);
        rfRvShelf = (RefreshRecyclerView) findViewById(R.id.rf_rv_shelf);

        ibMoney = (ImageButton) findViewById(R.id.ib_money);
        ibSettings = findViewById(R.id.ib_settings);
        ibLibrary = (ImageButton) findViewById(R.id.ib_library);
        ibAdd = (ImageButton) findViewById(R.id.ib_add);
        ibDownload = (ImageButton) findViewById(R.id.ib_download);

        rfRvShelf.setRefreshRecyclerViewAdapter(bookShelfAdapter, new LinearLayoutManager(this));

        flWarn = (FrameLayout) findViewById(R.id.fl_warn);
        ivWarnClose = (ImageView) findViewById(R.id.iv_warn_close);
    }

    @Override
    protected void bindEvent() {
        bindRvShelfEvent();
        ibSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                proxyPop.showAsDropDown(ibSettings);
            }
        });
        ibDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(BookMainActivity.this, DownloadBookActivity.class));
//                downloadListPop.showAsDropDown(ibDownload);
            }
        });
        ibMoney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击打赏
            }
        });
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ibLibrary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityByAnim(new Intent(BookMainActivity.this, MainActivity.class), 0, 0);
            }
        });
        ibAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击更多
                startActivityByAnim(new Intent(BookMainActivity.this, ImportBookActivity.class), 0, 0);
            }
        });
        bookShelfAdapter.setItemClickListener(new BookShelfAdapter.OnItemClickListener() {
            @Override
            public void toSearch() {
                //点击去选书
                startActivityByAnim(new Intent(BookMainActivity.this, MainActivity.class), 0, 0);
            }

            @Override
            public void onClick(CollBookBean collBookBean, int index) {
                Intent intent = new Intent(BookMainActivity.this, ReadActivity.class);

                intent.putExtra(ReadActivity.EXTRA_COLL_BOOK, collBookBean);
                intent.putExtra(ReadActivity.EXTRA_IS_COLLECTED, true);
                startActivityByAnim(intent, android.R.anim.fade_in, android.R.anim.fade_out);
            }

            @Override
            public void onLongClick(View animView, CollBookBean bookShelfBean, int index) {
                Intent intent = new Intent(BookMainActivity.this, BookDetailActivity.class);
                intent.putExtra("from", BookDetailPresenterImpl.FROM_BOOKSHELF);
                String key = String.valueOf(System.currentTimeMillis());
                intent.putExtra("data_key", key);
                BitIntentDataManager.getInstance().putData(key, bookShelfBean);
                startActivityByAnim(intent, animView, "img_cover", android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

        ivWarnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flWarn.setVisibility(View.GONE);
            }
        });
    }

    private void bindRvShelfEvent() {
        rfRvShelf.setBaseRefreshListener(new OnRefreshWithProgressListener() {
            @Override
            public int getMaxProgress() {
                return bookShelfAdapter.getBooks().size();
            }

            @Override
            public void startRefresh() {
                mPresenter.queryBookShelf(true);
            }
        });
    }

    @Override
    protected void firstRequest() {
        mPresenter.queryBookShelf(false);
    }

    @Override
    public void refreshBookShelf(List<CollBookBean> bookShelfBeanList) {
        bookShelfAdapter.replaceAll(bookShelfBeanList);
    }

    @Override
    public void activityRefreshView() {
        //执行刷新响应
        rfRvShelf.startRefresh();
    }

    @Override
    public void refreshFinish() {
        rfRvShelf.finishRefresh(false, true);
    }

    @Override
    public void refreshError(String error) {
        refreshFinish();
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void refreshRecyclerViewItemAdd() {
        rfRvShelf.getRpb().setDurProgress(rfRvShelf.getRpb().getDurProgress() + 1);
    }

    @Override
    public void setRecyclerMaxProgress(int x) {
        rfRvShelf.getRpb().setMaxProgress(x);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        downloadListPop.onDestroy();
    }


}