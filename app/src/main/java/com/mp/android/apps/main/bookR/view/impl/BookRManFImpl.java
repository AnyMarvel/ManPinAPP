package com.mp.android.apps.main.bookR.view.impl;

import android.app.ActivityOptions;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mp.android.apps.R;
import com.mp.android.apps.main.bookR.adapter.BookManFAdapter;
import com.mp.android.apps.main.bookR.presenter.IBookRManFPresenter;
import com.mp.android.apps.main.bookR.presenter.impl.BookRManFPresenterImpl;
import com.mp.android.apps.main.bookR.view.IBookRManFView;
import com.mp.android.apps.main.home.adapter.OnHomeAdapterClickListener;
import com.mp.android.apps.main.home.bean.HomeDesignBean;
import com.mp.android.apps.main.home.bean.SourceListContent;
import com.mp.android.apps.basemvplib.impl.BaseFragment;
import com.mp.android.apps.book.bean.SearchBookBean;
import com.mp.android.apps.book.presenter.impl.BookDetailPresenterImpl;
import com.mp.android.apps.book.view.impl.BookDetailActivity;

import java.util.List;

public class BookRManFImpl extends BaseFragment<IBookRManFPresenter> implements IBookRManFView, OnHomeAdapterClickListener {
    private RecyclerView recommendRecyclerView;
    private BookManFAdapter recommendRecyclerAdapter;

    @Override
    protected IBookRManFPresenter initInjector() {
        return new BookRManFPresenterImpl();
    }

    @Override
    protected void bindView() {
        super.bindView();
        recommendRecyclerView = view.findViewById(R.id.mp_bookr_man_recyclerview);
    }

    public void initLocalData() {
        mPresenter.initManData();
    }

    @Override
    protected void bindEvent() {
        super.bindEvent();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recommendRecyclerView.setLayoutManager(layoutManager);
        recommendRecyclerView.setItemAnimator(new DefaultItemAnimator());
        initLocalData();
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.mp_book_r_man_layout, container, false);
    }

    public void setClassicRecommendTitle(String title) {
        recommendRecyclerAdapter.setRecommendTitle(title);
    }

    @Override
    public void notifyRecyclerView(List<SourceListContent> recommendList, List<SourceListContent> hotRankingList, List<HomeDesignBean> listContent, boolean useCache) {
        if (useCache || recommendRecyclerAdapter == null) {
            recommendRecyclerAdapter = new BookManFAdapter(getContext(), this, recommendList, hotRankingList, listContent);
            setClassicRecommendTitle("男生推荐");
            recommendRecyclerView.setAdapter(recommendRecyclerAdapter);
        } else {
            recommendRecyclerAdapter.setRecommendList(recommendList);
            recommendRecyclerAdapter.setHotRankingList(hotRankingList);
            recommendRecyclerAdapter.setListContent(listContent);
            recommendRecyclerAdapter.notifyDataSetChanged();
            System.out.println("sssss"+String.valueOf(recommendRecyclerAdapter.getItemCount()));
        }
    }



    @Override
    public void onItemClickListener(View view) {

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
        mPresenter.getBookCardData(mContentPosition,  kinds);
    }
    @Override
    public void notifyContentItemUpdate(int position, List<SourceListContent> sourceListContents) {
        recommendRecyclerAdapter.updateContentByPosition(position, sourceListContents);
    }
}
