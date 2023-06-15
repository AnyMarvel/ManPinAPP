package com.mp.android.apps.book.view.impl;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mp.android.apps.R;
import com.mp.android.apps.basemvplib.impl.BaseFragment;
import com.mp.android.apps.book.bean.RankListMenubean;
import com.mp.android.apps.book.presenter.IBookRankListPresenter;
import com.mp.android.apps.book.presenter.impl.BookRankListPresenterImpl;
import com.mp.android.apps.book.view.IBookRankListView;
import com.mp.android.apps.book.view.adapter.BookRankListAdapter;
import com.mp.android.apps.book.view.adapter.BookRankMenuListAdapter;
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


public class BookRankListFragment extends BaseFragment<IBookRankListPresenter> implements IBookRankListView, OnHomeAdapterClickListener,BookRankMenuListAdapter.MenuItemClickListener {
    private RecyclerView recommendRecyclerView;
    private BookRankListAdapter bookRankListAdapter;
    private SmartRefreshLayout bookRrefreshLayout;

    private Button errorButton;
    private RotateLoading bookRankLoading;
    private TextView title;
    private ImageView searchView;

    private LinearLayout mp_book_rank_list_title_layout;
    private DrawerLayout drawer;
    private RecyclerView menu_recyclerView;

    private List<RankListMenubean> sourceList;

    @Override
    protected IBookRankListPresenter initInjector() {
        return new BookRankListPresenterImpl();
    }



    @Override
    protected void firstRequest() {
        super.firstRequest();
        initRequestBookRank(sourceList.get(0));
    }

    private void initRequestBookRank(RankListMenubean rankListMenubean){
        if (rankListMenubean!=null){
            bookRankUrl = rankListMenubean.urlPath;
            title.setText(rankListMenubean.name);
        }
        bookRankLoading.start();
        mPresenter.initBookRankListData(bookRankUrl);
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.manpin_book_rank_list_layout, container, false);
    }


    @Override
    protected void bindView() {
        super.bindView();
        recommendRecyclerView = view.findViewById(R.id.mp_bookr_recommend_recyclerview);
        bookRrefreshLayout = view.findViewById(R.id.bookr_recommend_refreshLayout);
        bookRrefreshLayout.setRefreshFooter(new ClassicsFooter(getContext()));
        bookRrefreshLayout.setRefreshHeader(new ClassicsHeader(getContext()));
        mp_book_rank_list_title_layout = view.findViewById(R.id.mp_book_rank_list_title_layout);
        title = view.findViewById(R.id.mp_book_rank_list_title);
        mp_book_rank_list_title_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.openDrawer(Gravity.LEFT);
            }
        });
        drawer = view.findViewById(R.id.drawerlayout_recommend);
        searchView = view.findViewById(R.id.bookr_fragment_search);
        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent searchIntent = new Intent(getActivity(), SearchActivity.class);
                startActivity(searchIntent);
            }
        });
        bookRankLoading = view.findViewById(R.id.book_rank_loading);
        errorButton = view.findViewById(R.id.mp_error_button);
        errorButton.setVisibility(View.GONE);
        errorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                errorButton.setVisibility(View.GONE);
                mPresenter.initBookRankListData(bookRankUrl);
            }
        });
        menu_recyclerView = view.findViewById(R.id.menu_recyclerView);
        menu_recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        menu_recyclerView.setAdapter(new BookRankMenuListAdapter(sourceList,this));
        menu_recyclerView.setItemAnimator(new DefaultItemAnimator());
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
                if (bookRankListAdapter != null) {
                    mPresenter.getNextPageContent(bookRankUrl, bookRankListAdapter.getPageNumber() + 1);
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

    @Override
    protected void initData() {
        handleSourceList();
    }


    private void handleSourceList() {
        sourceList = new ArrayList<>();
        sourceList.add(new RankListMenubean("/rank/yuepiao/","月票榜"));
        sourceList.add(new RankListMenubean("/rank/hotsales/","畅销榜"));
        sourceList.add(new RankListMenubean("/rank/readindex/","阅读指数榜"));
        sourceList.add(new RankListMenubean("/rank/newfans/","书友榜"));
        sourceList.add(new RankListMenubean("/rank/recom/","推荐榜"));
        sourceList.add(new RankListMenubean("/rank/collect/","收藏榜"));
        sourceList.add(new RankListMenubean("/rank/vipup/","更新榜"));
        sourceList.add(new RankListMenubean("/rank/vipcollect/","VIP收藏榜"));
        sourceList.add(new RankListMenubean("/rank/signnewbook/","签约作者新书榜"));
        sourceList.add(new RankListMenubean("/rank/pubnewbook/","公众作者新书榜"));
        sourceList.add(new RankListMenubean("/rank/newsign/","新人签约新书榜"));
        sourceList.add(new RankListMenubean("/rank/newauthor/","新人作者新书榜"));
    }

    @Override
    public void notifyRecyclerView(List<SourceListContent> contentList, int pageNumber) {
        if (bookRankListAdapter == null) {
            bookRankListAdapter = new BookRankListAdapter(getContext(), contentList, this);
            recommendRecyclerView.setAdapter(bookRankListAdapter);
        }else {
            if (pageNumber==1){
                bookRankListAdapter.resetContentList(contentList);
                if (bookRrefreshLayout.isRefreshing()){
                    bookRrefreshLayout.finishRefresh();
                }
            }else {
                bookRankListAdapter.addContentList(contentList);
                if (bookRrefreshLayout.isLoading()){
                    bookRrefreshLayout.finishLoadMore();
                }
            }
        }
        if (bookRankLoading.isStart()){
            bookRankLoading.stop();
        }
        bookRankListAdapter.setPageNumber(pageNumber);
    }

    @Override
    public void showError(int pageNumber) {
        if (pageNumber == 1) {
            errorButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onItemClickListener(View view) {

    }

    @Override
    public void onLayoutClickListener(View view, SourceListContent sourceListContent) {
        if (sourceListContent != null && !TextUtils.isEmpty(sourceListContent.getName())) {
            Intent searchIntent = new Intent(this.getActivity(), SearchActivity.class);
            searchIntent.putExtra("rankSearchName", sourceListContent.getName());
            startActivity(searchIntent);
        }
    }


    @Override
    public void OnClick(RankListMenubean menubean) {
        //抽屉布局Click事件回调
        drawer.closeDrawer(Gravity.LEFT);
        initRequestBookRank(menubean);
    }
}
