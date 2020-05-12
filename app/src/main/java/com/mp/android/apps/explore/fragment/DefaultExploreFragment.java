package com.mp.android.apps.explore.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.mp.android.apps.R;
import com.mp.android.apps.explore.adapter.ExploreSquareAdapter;
import com.mp.android.apps.explore.utils.SpacesItemDecoration;
import com.mp.android.apps.explore.bean.Data;
import com.mp.android.apps.explore.bean.JsonRootBean;
import com.mp.android.apps.explore.network.ExploreApi;
import com.mp.android.apps.login.utils.LoginManager;
import com.mp.android.apps.networkutils.FastJsonConverterFactory;
import com.scwang.smart.refresh.footer.ClassicsFooter;
import com.scwang.smart.refresh.header.ClassicsHeader;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;


import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class DefaultExploreFragment extends Fragment {
    RecyclerView recyclerView;
    ExploreSquareAdapter exploreSquareAdapter;
    RefreshLayout refreshLayout;
    String toExplore;
    FrameLayout nodata;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_explore, container, false);
        assert getArguments() != null;
        toExplore = getArguments().getString("toExplore");
        initView(view);
        return view;
    }

    private void initView(View view) {
        recyclerView = (RecyclerView) view.findViewById(R.id.explore_list);
        nodata = view.findViewById(R.id.nodata);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        //设置布局管理器
        recyclerView.setLayoutManager(layoutManager);
        //设置为垂直布局，这也是默认的
        layoutManager.setOrientation(OrientationHelper.VERTICAL);
        exploreSquareAdapter = new ExploreSquareAdapter(getContext());
        //设置Adapter
        recyclerView.setAdapter(exploreSquareAdapter);
        recyclerView.addItemDecoration(new SpacesItemDecoration(15));
        //设置分隔线
        //设置增加或删除条目的动画
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        refreshLayout = (RefreshLayout) view.findViewById(R.id.explore_refreshLayout);
        refreshLayout.setRefreshHeader(new ClassicsHeader(getContext()));
        refreshLayout.setRefreshFooter(new ClassicsFooter(getContext()));
        onLoadMoreListener();
        exploreSquareAdapter.setPageNumber(1);
        refreshLayout.autoLoadMore();
    }

    private void onLoadMoreListener() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://aimanpin.com/")
                .addConverterFactory(FastJsonConverterFactory.create())
                .build();
        ExploreApi api = retrofit.create(ExploreApi.class);
        //下拉刷新逻辑
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                exploreSquareAdapter.setPageNumber(1);
                Call<JsonRootBean> dataclall;
                if (toExplore.equals("个人中心")) {
                    dataclall = api.getPersonInfos(LoginManager.getInstance().getLoginInfo().getUniqueID(), exploreSquareAdapter.getPageNumber());
                } else {
                    dataclall = api.getSquareInfs(exploreSquareAdapter.getPageNumber());
                }

                dataclall.enqueue(new Callback<JsonRootBean>() {
                    @Override
                    public void onResponse(Call<JsonRootBean> call, Response<JsonRootBean> response) {
                        JsonRootBean jsonsRootBean = response.body();
                        if (jsonsRootBean != null && jsonsRootBean.getData().size() > 0) {
                            nodata.setVisibility(View.GONE);
                            List<Data> exploreDatas = jsonsRootBean.getData();
                            exploreSquareAdapter.setExploreData(exploreDatas);
                            exploreSquareAdapter.notifyDataSetChanged();
                            exploreSquareAdapter.setPageNumber(exploreSquareAdapter.getPageNumber() + 1);
                            refreshlayout.finishRefresh(/*,false*/);//传入false表示加载失败
                        } else {
                            nodata.setVisibility(View.VISIBLE);
                            refreshlayout.finishRefresh();
                        }

                    }

                    @Override
                    public void onFailure(Call<JsonRootBean> call, Throwable t) {
                        refreshlayout.finishRefresh(false/*,false*/);//传入false表示加载失败
                        nodata.setVisibility(View.VISIBLE);

                    }
                });
            }
        });
        //上拉加载逻辑
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshlayout) {

                Call<JsonRootBean> dataclall;
                if (toExplore.equals("个人中心")) {
                    dataclall = api.getPersonInfos(LoginManager.getInstance().getLoginInfo().getUniqueID(), exploreSquareAdapter.getPageNumber());
                } else {
                    dataclall = api.getSquareInfs(exploreSquareAdapter.getPageNumber());
                }

                dataclall.enqueue(new Callback<JsonRootBean>() {
                    @Override
                    public void onResponse(Call<JsonRootBean> call, Response<JsonRootBean> response) {
                        JsonRootBean jsonsRootBean = response.body();
                        if (jsonsRootBean != null && jsonsRootBean.getData().size() > 0) {
                            nodata.setVisibility(View.GONE);
                            List<Data> exploreDatas = jsonsRootBean.getData();
                            exploreSquareAdapter.addExploreData(exploreDatas);
                            exploreSquareAdapter.notifyDataSetChanged();
                            exploreSquareAdapter.setPageNumber(exploreSquareAdapter.getPageNumber() + 1);
                            refreshlayout.finishLoadMore(/*,false*/);//传入false表示加载失败
                        } else {
                            nodata.setVisibility(View.VISIBLE);
                            refreshlayout.finishLoadMore();
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonRootBean> call, Throwable t) {
                        refreshlayout.finishLoadMore(false/*,false*/);//传入false表示加载失败
                        nodata.setVisibility(View.VISIBLE);

                    }
                });


            }
        });
    }

}
