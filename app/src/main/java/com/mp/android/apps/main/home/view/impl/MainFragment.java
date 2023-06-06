package com.mp.android.apps.main.home.view.impl;

import android.content.Intent;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.mp.android.apps.R;
import com.mp.android.apps.book.view.impl.BookRankListActivity;
import com.mp.android.apps.main.MainActivity;
import com.mp.android.apps.main.ManpinWXActivity;
import com.mp.android.apps.main.home.adapter.MainFragmentRecycleAdapter;
import com.mp.android.apps.main.home.presenter.impl.MainFragmentPresenterImpl;
import com.mp.android.apps.main.home.view.IMainfragmentView;
import com.mp.android.apps.basemvplib.impl.BaseFragment;
import com.mp.android.apps.book.utils.BookSourceCheckUtils;
import com.mp.android.apps.book.view.impl.BookSourceGuideActivity;
import com.mp.android.apps.book.view.impl.SearchActivity;
import java.util.List;
import java.util.Map;

public class MainFragment extends BaseFragment<MainFragmentPresenterImpl> implements IMainfragmentView, OnMainFragmentClickListener {

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
        mPresenter.initSpiderHomeData();
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


    @Override
    public void notifyRecyclerHomePage(List<Map<String, String>> carouselList, List<Map<String, String>> recommendInfoList) {
        mainFragmentRecycleAdapter = new MainFragmentRecycleAdapter(getContext(), carouselList, recommendInfoList,this);
        //设置Adapter
        recyclerView.setAdapter(mainFragmentRecycleAdapter);
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
                Intent intent2 = new Intent(getActivity(), ManpinWXActivity.class);
                startActivity(intent2);
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
    public void onLayoutClickListener( String name) {
        if ( !TextUtils.isEmpty(name)){
            Intent searchIntent = new Intent(this.getActivity(), SearchActivity.class);
            searchIntent.putExtra("rankSearchName",name);
            startActivity(searchIntent);
        }
    }

}
