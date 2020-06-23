package com.mp.android.apps.main.presenter.impl;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mp.android.apps.MyApplication;
import com.mp.android.apps.main.bean.HomeDesignBean;
import com.mp.android.apps.main.bean.SourceListContent;
import com.mp.android.apps.main.model.IMainFragmentModelImpl;
import com.mp.android.apps.main.presenter.IMainFragmentPresenter;
import com.mp.android.apps.main.view.IMainfragmentView;

import com.mp.android.apps.monke.basemvplib.impl.BasePresenterImpl;
import com.mp.android.apps.monke.monkeybook.base.observer.SimpleObserver;
import com.mp.android.apps.monke.monkeybook.cache.ACache;
import com.mp.android.apps.utils.AssertFileUtils;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MainFragmentPresenterImpl extends BasePresenterImpl<IMainfragmentView> implements IMainFragmentPresenter {
    private ACache mCache;
    /**
     * 首页数据缓存
     */
    public static final String MAINFRAGMENTCACHEDATA = "main_cache";

    @Override
    public void detachView() {
    }

    public MainFragmentPresenterImpl() {
        mCache = ACache.get(MyApplication.getInstance());
    }

    /**
     * 初始化首页数据
     */
    @Override
    public void initHomeData() {
        String mainCacheJson = mCache.getAsString(MAINFRAGMENTCACHEDATA);
        if (!TextUtils.isEmpty(mainCacheJson)) {
            notifyRecyclerViewRefresh(mainCacheJson, true);
        }
        IMainFragmentModelImpl.getInstance().getHomeDatas().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new SimpleObserver<String>() {
            @Override
            public void onNext(String s) {
                notifyRecyclerViewRefresh(s, false);
                mCache.put(MAINFRAGMENTCACHEDATA, s);
            }

            @Override
            public void onError(Throwable e) {
                if (TextUtils.isEmpty(mainCacheJson)){
                    String localData = AssertFileUtils.getJson(mView.getContext(), "localhome.json");
                    notifyRecyclerViewRefresh(localData, false);
                }
            }
        });
    }

    /**
     * 刷新主页数据
     * 1. 本地有缓存基于缓存刷新数据,本地无缓存,基于网络刷新数据
     *
     * @param s
     */
    private void notifyRecyclerViewRefresh(String s, boolean useCache) {
        JSONObject jsonObject = JSON.parseObject(s);
        JSONObject data = (JSONObject) jsonObject.get("data");
        if (data != null) {
            String carouselJson = JSON.toJSONString(data.get("carouselImages"));
            String homebookJson = JSON.toJSONString(data.get("homeBook"));
            String recommendJson = JSON.toJSONString(data.get("recommend"));
            if (!TextUtils.isEmpty(homebookJson) && !TextUtils.isEmpty(recommendJson) && !TextUtils.isEmpty(carouselJson)) {
                List<String> carouselImages = JSON.parseArray(carouselJson, String.class);
                List<HomeDesignBean> list = JSON.parseArray(homebookJson, HomeDesignBean.class);
                List<SourceListContent> recommendList = JSON.parseArray(recommendJson, SourceListContent.class);
                if (list != null && list.size() > 0
                        && carouselImages != null && carouselImages.size() > 0
                        && recommendList != null && recommendList.size() == 3
                ) {
                    mView.notifyRecyclerView(list, carouselImages, recommendList, useCache);
                } else {
                    String localData = AssertFileUtils.getJson(mView.getContext(), "localhome.json");
                    notifyRecyclerViewRefresh(localData, false);
                }
            }
        }
    }


    /**
     * 点击首页Content内容换一换按钮,单个刷新item
     *
     * @param mContentPosition
     * @param kinds
     */
    @Override
    public void getContentPostion(int mContentPosition, String kinds) {
        IMainFragmentModelImpl.getInstance().getContentItemData(kinds).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new SimpleObserver<String>() {
            @Override
            public void onNext(String s) {
                JSONObject jsonObject = JSON.parseObject(s);
                JSONObject data = (JSONObject) jsonObject.get("data");
                if (data != null) {
                    String sourceListContent = JSON.toJSONString(data.get("sourceListContent"));
                    if (!TextUtils.isEmpty(sourceListContent)) {
                        List<SourceListContent> sourceListContents = JSON.parseArray(sourceListContent, SourceListContent.class);
                        mView.notifyContentItemUpdate(mContentPosition, sourceListContents);
                    }
                }
            }

            @Override
            public void onError(Throwable e) {

            }
        });
    }


}
