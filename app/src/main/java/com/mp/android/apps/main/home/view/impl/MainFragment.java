package com.mp.android.apps.main.home.view.impl;

import android.Manifest;
import android.app.ActivityOptions;
import android.content.Intent;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mp.android.apps.R;
import com.mp.android.apps.book.view.impl.BookRankListActivity;
import com.mp.android.apps.main.MainActivity;
import com.mp.android.apps.main.home.adapter.MainFragmentRecycleAdapter;
import com.mp.android.apps.main.home.adapter.OnHomeAdapterClickListener;
import com.mp.android.apps.main.home.bean.HomeDesignBean;
import com.mp.android.apps.main.home.bean.SourceListContent;
import com.mp.android.apps.main.home.presenter.impl.MainFragmentPresenterImpl;
import com.mp.android.apps.main.home.view.IMainfragmentView;
import com.mp.android.apps.basemvplib.impl.BaseFragment;
import com.mp.android.apps.book.bean.SearchBookBean;
import com.mp.android.apps.book.presenter.impl.BookDetailPresenterImpl;
import com.mp.android.apps.book.utils.BookSourceCheckUtils;
import com.mp.android.apps.book.view.impl.BookDetailActivity;
import com.mp.android.apps.book.view.impl.BookSourceGuideActivity;
import com.mp.android.apps.book.view.impl.SearchActivity;
import com.mylhyl.acp.Acp;
import com.mylhyl.acp.AcpListener;
import com.mylhyl.acp.AcpOptions;

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
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        //设置布局管理器
        recyclerView.setLayoutManager(layoutManager);
        //设置为垂直布局，这也是默认的
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        //设置分隔线
//        recyclerView.addItemDecoration(new SpacesItemDecoration(15));
        //设置增加或删除条目的动画
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }


    @Override
    protected void bindEvent() {
        super.bindEvent();
        mPresenter.initHomeData();
    }

    /**
     * 初始化数据
     */
    @Override
    protected void initData() {

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.main_fragment_layout, container, false);
    }

    /**
     * 主页列表适配器设置
     *
     * @param list           列表主体内容(包含分类和具体详情)
     * @param carouselImages 主页轮播图的数据
     * @param listContents   主页推荐位数据
     * @param useCache       是否使用缓存更新主页
     */
    @Override
    public void notifyRecyclerView(List<HomeDesignBean> list, List<String> carouselImages, List<SourceListContent> listContents, boolean useCache) {
        if (useCache || mainFragmentRecycleAdapter == null) {
            mainFragmentRecycleAdapter = new MainFragmentRecycleAdapter(getContext(), list, this, carouselImages, listContents);
            //设置Adapter
            recyclerView.setAdapter(mainFragmentRecycleAdapter);
            recyclerView.setItemViewCacheSize(10);
        } else {
            mainFragmentRecycleAdapter.setCarouselImages(carouselImages);
            mainFragmentRecycleAdapter.setListContent(list);
            mainFragmentRecycleAdapter.setRecommendList(listContents);
            mainFragmentRecycleAdapter.notifyDataSetChanged();
        }

    }


    @Override
    public void onItemClickListener(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.huojian:
                Intent intent=new Intent(getActivity(), BookRankListActivity.class);
                intent.putExtra("rankRouteUrl",BookRankListActivity.RANKRECOM);
                startActivity(intent);
                break;
            case R.id.jingxuan:
                Intent intent1=new Intent(getActivity(), BookRankListActivity.class);
                intent1.putExtra("rankRouteUrl",BookRankListActivity.RANKVIPCOLLECT);
                startActivity(intent1);
                break;
            case R.id.xiaoshuo:
                ((MainActivity) requireActivity()).showShujiaFragment();
                break;
            case R.id.guangchang:
                Intent intentUrl= new Intent();
                intentUrl.setAction("android.intent.action.VIEW");
                Uri content_url = Uri.parse("http://tool.liumingye.cn/music/?page=searchPage");
                intentUrl.setData(content_url);
                startActivity(intentUrl);
            break;
            case R.id.search_image:
                if (BookSourceCheckUtils.bookSourceSwitch(this.getContext())) {
                    startActivity(new Intent(getActivity(), SearchActivity.class));
                } else {
                    startActivity(new Intent(getActivity(), BookSourceGuideActivity.class));
                }
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

    @Override
    public void onContentChangeClickListener(int mContentPosition, String kinds) {
        mPresenter.getContentPostion(mContentPosition, kinds);
    }

    @Override
    public void notifyContentItemUpdate(int position, List<SourceListContent> sourceListContents) {
        mainFragmentRecycleAdapter.updateContentByPosition(position, sourceListContents);
    }

}
