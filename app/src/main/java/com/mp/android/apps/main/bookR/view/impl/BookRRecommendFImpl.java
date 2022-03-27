package com.mp.android.apps.main.bookR.view.impl;

import android.app.ActivityOptions;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mp.android.apps.R;
import com.mp.android.apps.book.view.impl.BookRankListActivity;
import com.mp.android.apps.main.bookR.adapter.BookRRecommendFRecyclerAdapter;
import com.mp.android.apps.main.bookR.presenter.IBookRRecommendFPresenter;
import com.mp.android.apps.main.bookR.presenter.impl.BookRRecommendFPresenterImpl;
import com.mp.android.apps.main.bookR.view.IBookRRecommendFView;
import com.mp.android.apps.main.home.adapter.OnHomeAdapterClickListener;
import com.mp.android.apps.main.home.bean.SourceListContent;
import com.mp.android.apps.basemvplib.impl.BaseFragment;
import com.mp.android.apps.book.bean.SearchBookBean;
import com.mp.android.apps.book.presenter.impl.BookDetailPresenterImpl;
import com.mp.android.apps.book.view.impl.BookDetailActivity;
import com.scwang.smart.refresh.footer.ClassicsFooter;
import com.scwang.smart.refresh.header.ClassicsHeader;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import java.util.List;
import java.util.Objects;


public class BookRRecommendFImpl extends BaseFragment<IBookRRecommendFPresenter> implements IBookRRecommendFView, OnHomeAdapterClickListener {
    private RecyclerView recommendRecyclerView;
    private BookRRecommendFRecyclerAdapter recommendRecyclerAdapter;
    private SmartRefreshLayout bookRrefreshLayout;

    @Override
    protected void initData() {
        super.initData();
    }

    @Override
    protected void bindView() {
        super.bindView();
        recommendRecyclerView = view.findViewById(R.id.mp_bookr_recommend_recyclerview);
        bookRrefreshLayout = view.findViewById(R.id.bookr_recommend_refreshLayout);
        bookRrefreshLayout.setRefreshFooter(new ClassicsFooter(requireContext()));
        bookRrefreshLayout.setRefreshHeader(new ClassicsHeader(requireContext()));
    }

    @Override
    protected void bindEvent() {
        super.bindEvent();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recommendRecyclerView.setLayoutManager(layoutManager);
        recommendRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mPresenter.initBookRRcommendData();
    }

    @Override
    protected IBookRRecommendFPresenter initInjector() {
        return new BookRRecommendFPresenterImpl();
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.mp_book_r_recommend_layout, container, false);
    }

    @Override
    public void onItemClickListener(View view) {
        int id=view.getId();
        Intent intent=new Intent(getActivity(), BookRankListActivity.class);
        switch (id){
            case R.id.mp_bookr_recommend_category:
                intent.putExtra("rankRouteUrl",BookRankListActivity.RANKRECOM);
                break;
            case R.id.mp_bookr_recommend_ranking:
                intent.putExtra("rankRouteUrl",BookRankListActivity.RANKVIPCOLLECT);
                break;
        }

        startActivity(intent);
    }

    @Override
    public void onLayoutClickListener(View view, SourceListContent sourceListContent) {
        SearchBookBean searchBookBean = new SearchBookBean();
        searchBookBean.setName(sourceListContent.getName());
        searchBookBean.setCoverUrl(sourceListContent.getCoverUrl());
        searchBookBean.setNoteUrl(sourceListContent.getNoteUrl());
        searchBookBean.setAuthor(sourceListContent.getAuthor());
        searchBookBean.setDesc(sourceListContent.getBookdesc());
        searchBookBean.setOrigin(sourceListContent.getOrigin());
        searchBookBean.setKind(sourceListContent.getKind());
        searchBookBean.setTag(sourceListContent.getTag());
        searchBookBean.setAdd(false);
        searchBookBean.setWords(0);

        Intent intent = new Intent(getActivity(), BookDetailActivity.class);
        intent.putExtra("from", BookDetailPresenterImpl.FROM_SEARCH);
        intent.putExtra("data", searchBookBean);
        intent.putExtra("start_with_share_ele", true);
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(getActivity(), view, "img_cover").toBundle());
    }

    @Override
    public void onContentChangeClickListener(int mContentPosition, String kinds) {

    }


    @Override
    public void notifyRecyclerView(List<SourceListContent> recommendList, List<SourceListContent> hotRankingList, List<SourceListContent> contentList, boolean useCache) {

        if (useCache || recommendRecyclerAdapter == null) {
            recommendRecyclerAdapter = new BookRRecommendFRecyclerAdapter(getContext(), this, recommendList, hotRankingList, contentList);
            recommendRecyclerView.setAdapter(recommendRecyclerAdapter);
        } else {
            recommendRecyclerAdapter.setRecommendList(recommendList);
            recommendRecyclerAdapter.setHotRankingList(hotRankingList);
            recommendRecyclerAdapter.setContentList(contentList);
            recommendRecyclerAdapter.notifyDataSetChanged();
        }
        bookRrefreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                mPresenter.getNextPageContent(recommendRecyclerAdapter.getmContentListPage() + 1);
            }
        });
        bookRrefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                mPresenter.initBookRRcommendData();
            }
        });
        bookRrefreshLayout.finishRefresh();
    }

    @Override
    public void notifyMoreRecommendList(List<SourceListContent> recommendList) {
        if (recommendRecyclerAdapter != null && recommendList != null) {
            recommendRecyclerAdapter.addRecommendList(recommendList);
        }

        bookRrefreshLayout.finishLoadMore();
    }


}
