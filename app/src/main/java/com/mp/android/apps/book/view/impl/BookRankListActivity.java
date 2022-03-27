package com.mp.android.apps.book.view.impl;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mp.android.apps.R;
import com.mp.android.apps.basemvplib.impl.BaseActivity;
import com.mp.android.apps.book.bean.SearchBookBean;
import com.mp.android.apps.book.presenter.IBookRankListPresenter;
import com.mp.android.apps.book.presenter.impl.BookRankListPresenterImpl;
import com.mp.android.apps.book.view.IBookRankListView;
import com.mp.android.apps.book.view.adapter.BookRankListAdapter;
import com.mp.android.apps.main.bookR.adapter.BookRRecommendFRecyclerAdapter;
import com.mp.android.apps.main.home.adapter.OnHomeAdapterClickListener;
import com.mp.android.apps.main.home.bean.SourceListContent;
import com.scwang.smart.refresh.footer.ClassicsFooter;
import com.scwang.smart.refresh.header.ClassicsHeader;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;
import com.victor.loading.rotate.RotateLoading;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class BookRankListActivity extends BaseActivity<IBookRankListPresenter> implements IBookRankListView , OnHomeAdapterClickListener {
    private RecyclerView recommendRecyclerView;
    private BookRankListAdapter bookRankListAdapter;
    private SmartRefreshLayout bookRrefreshLayout;

    private Button errorButton;
    private RotateLoading bookRankLoading;
    private TextView title;
    private ImageView searchView;
    @Override
    protected IBookRankListPresenter initInjector() {
        return new BookRankListPresenterImpl();
    }

    @Override
    protected void onCreateActivity() {
        setContentView(R.layout.manpin_book_rank_list_layout);
        fitSystemWindows();
    }
    private void fitSystemWindows(){
        //android 6.0以上适配沉浸式
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setStatusBarColor(getResources().getColor(android.R.color.white));
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }


    @Override
    protected void firstRequest() {
        super.firstRequest();
       setRankTitle();
        bookRankLoading.start();
        mPresenter.initBookRankListData(bookRankUrl);
    }
    private void setRankTitle(){
        if (bookRankUrl!=null){
            switch (bookRankUrl){
                case RANKRECOM:
                    title.setText("推荐排行榜");
                    break;
                case RANKVIPCOLLECT:
                    title.setText("收藏排行榜");
                    break;
                case RANKWOMENRECOM:
                    title.setText("女生排行榜");
                    break;
                case RANKWOMENCOLLECT:
                    title.setText("女生收藏榜");
                    break;
                case RANKFANS:
                    title.setText("粉丝推荐榜单");
                    break;
                case RANKREADINDEX:
                    title.setText("月票排行榜");
                    break;
                default:
                    title.setText("排行榜");
            }
        }
    }
    @Override
    protected void bindView() {
        super.bindView();
        recommendRecyclerView = findViewById(R.id.mp_bookr_recommend_recyclerview);
        bookRrefreshLayout = findViewById(R.id.bookr_recommend_refreshLayout);
        bookRrefreshLayout.setRefreshFooter(new ClassicsFooter(Objects.requireNonNull(getContext())));
        bookRrefreshLayout.setRefreshHeader(new ClassicsHeader(Objects.requireNonNull(getContext())));
        title = findViewById(R.id.mp_book_rank_list_title);
        searchView = findViewById(R.id.bookr_fragment_search);
        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent searchIntent = new Intent(BookRankListActivity.this, SearchActivity.class);
                startActivity(searchIntent);
            }
        });
        bookRankLoading=findViewById(R.id.book_rank_loading);
        errorButton=findViewById(R.id.mp_error_button);
        errorButton.setVisibility(View.GONE);
        errorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                errorButton.setVisibility(View.GONE);
                mPresenter.initBookRankListData(bookRankUrl);
            }
        });
    }

    @Override
    protected void bindEvent() {
        super.bindEvent();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recommendRecyclerView.setLayoutManager(layoutManager);
        recommendRecyclerView.setItemAnimator(new DefaultItemAnimator());

        bookRrefreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                if (bookRankListAdapter!=null){
                    mPresenter.getNextPageContent(bookRankUrl,bookRankListAdapter.getPageNumber()+1);
                }
            }
        });
        bookRrefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                mPresenter.initBookRankListData(bookRankUrl);
            }
        });


    }
    public String bookRankUrl;
    /**
     * 推荐榜 /rank/recom
     */
    public static final String RANKRECOM="/rank/recom";
    /**
     * Vip收藏榜 /rank/vipcollect
     */
    public static final String RANKVIPCOLLECT="/rank/vipcollect";

    /**
     * 女士推荐榜
     */
    public static final String RANKWOMENRECOM="/rank/mm/recom";

    /**
     * 女士收藏榜
     */
    public static final String RANKWOMENCOLLECT="/rank/mm/collect";


    /**
     * 男生推荐页粉丝榜
     */
    public static final String RANKFANS="/rank/newfans/";

    /**
     * 阅读榜
     */
    public static final String RANKREADINDEX="/rank/readindex/";

    @Override
    protected void initData() {
        Intent intent=getIntent();
        bookRankUrl=intent.getStringExtra("rankRouteUrl");
        if (TextUtils.isEmpty(bookRankUrl)){
            bookRankUrl=RANKRECOM;
        }
    }

    @Override
    public void notifyRecyclerView(List<SourceListContent> contentList, boolean useCache,int pageNumber) {
        if (useCache || bookRankListAdapter == null) {
            bookRankListAdapter = new BookRankListAdapter(getContext(), contentList,this);
            recommendRecyclerView.setAdapter(bookRankListAdapter);
            bookRankLoading.stop();
        }else if (pageNumber==1){
            bookRankListAdapter.resetContentList(contentList);
            bookRrefreshLayout.finishRefresh();
        }else {
            bookRankListAdapter.addContentList(contentList);
            bookRankListAdapter.setPageNumber(pageNumber);
            bookRrefreshLayout.finishLoadMore();
        }

    }

    @Override
    public void showError(int pageNumber) {
        if (pageNumber==1){
            errorButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onItemClickListener(View view) {

    }

    @Override
    public void onLayoutClickListener(View view, SourceListContent sourceListContent) {
        if (sourceListContent!=null && !TextUtils.isEmpty(sourceListContent.getName())){
            Intent searchIntent = new Intent(BookRankListActivity.this, SearchActivity.class);
            searchIntent.putExtra("rankSearchName",sourceListContent.getName());
            startActivity(searchIntent);
        }
    }

    @Override
    public void onContentChangeClickListener(int mContentPosition, String kinds) {

    }

}
