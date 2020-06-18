package com.mp.android.apps.main.view.impl;

import android.Manifest;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.google.android.apps.photolab.storyboard.activity.ComicSplash;
import com.mp.android.apps.R;
import com.mp.android.apps.explore.adapter.ExploreSquareAdapter;
import com.mp.android.apps.explore.utils.SpacesItemDecoration;
import com.mp.android.apps.livevblank.ChoiceItemActivity;
import com.mp.android.apps.main.MainActivity;
import com.mp.android.apps.main.adapter.MainFragmentRecycleAdapter;
import com.mp.android.apps.main.adapter.OnHomeAdapterClickListener;
import com.mp.android.apps.main.bean.HomeDesignBean;
import com.mp.android.apps.main.bean.SourceListContent;
import com.mp.android.apps.main.cycleimage.BannerInfo;
import com.mp.android.apps.main.cycleimage.CycleViewPager;
import com.mp.android.apps.main.presenter.impl.MainFragmentPresenterImpl;
import com.mp.android.apps.main.view.IMainfragmentView;
import com.mp.android.apps.main.view.MyImageTextView;
import com.mp.android.apps.monke.basemvplib.impl.BaseFragment;
import com.mp.android.apps.monke.monkeybook.bean.SearchBookBean;
import com.mp.android.apps.monke.monkeybook.presenter.impl.BookDetailPresenterImpl;
import com.mp.android.apps.monke.monkeybook.view.impl.BookDetailActivity;
import com.mp.android.apps.monke.monkeybook.view.impl.BookMainActivity;
import com.mp.android.apps.monke.monkeybook.view.impl.SearchActivity;
import com.mylhyl.acp.Acp;
import com.mylhyl.acp.AcpListener;
import com.mylhyl.acp.AcpOptions;

import java.util.ArrayList;
import java.util.List;

public class MainFragment extends BaseFragment<MainFragmentPresenterImpl> implements IMainfragmentView, OnHomeAdapterClickListener {


    private RecyclerView recyclerView;

    private MainFragmentRecycleAdapter mainFragmentRecycleAdapter;

    @Override
    protected MainFragmentPresenterImpl initInjector() {
        return new MainFragmentPresenterImpl();
    }

    /**
     * 初始化View
     */
    @Override
    protected void bindView() {
        super.bindView();
        recyclerView = view.findViewById(R.id.homeRecycleView);

    }


    @Override
    protected void bindEvent() {
        super.bindEvent();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        //设置布局管理器
        recyclerView.setLayoutManager(layoutManager);
        //设置为垂直布局，这也是默认的
        layoutManager.setOrientation(OrientationHelper.VERTICAL);
        //设置分隔线
//        recyclerView.addItemDecoration(new SpacesItemDecoration(15));
        //设置增加或删除条目的动画
        recyclerView.setItemAnimator(new DefaultItemAnimator());


    }

    /**
     * 初始化数据
     */
    @Override
    protected void initData() {
        mPresenter.initHomeData();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.main_fragment_layout, container, false);
    }


    @Override
    public void notifyRecyclerView(List<HomeDesignBean> list, List<String> carouselImages, List<SourceListContent> listContents) {
        mainFragmentRecycleAdapter = new MainFragmentRecycleAdapter(getContext(), list, this, carouselImages,listContents);
        //设置Adapter
        recyclerView.setAdapter(mainFragmentRecycleAdapter);
        recyclerView.setItemViewCacheSize(10);
        mainFragmentRecycleAdapter.notifyDataSetChanged();
    }


    @Override
    public void onItemClickListener(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.huojian:
                Acp.getInstance(getActivity()).request(new AcpOptions.Builder()
                        .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE).build(), new AcpListener() {
                    @Override
                    public void onGranted() {
                        Intent intentComic = new Intent(getActivity(), ComicSplash.class);
                        startActivity(intentComic);
                    }

                    @Override
                    public void onDenied(List<String> permissions) {
                    }
                });

                break;
            case R.id.jingxuan:
                Intent intentPostcard = new Intent(getActivity(), ChoiceItemActivity.class);
                startActivity(intentPostcard);
                break;
            case R.id.xiaoshuo:
                Intent intentBook = new Intent(getActivity(), BookMainActivity.class);
                startActivity(intentBook);
                break;
            case R.id.guangchang:
                ((MainActivity) getActivity()).gotoExplore("广场");
                break;
            case R.id.search_image:
                Intent searchIntent = new Intent(getActivity(), SearchActivity.class);
                startActivity(searchIntent);
                break;
            default:
                break;
        }

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


}
