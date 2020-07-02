package com.mp.android.apps.main.bookR.presenter.impl;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mp.android.apps.MyApplication;
import com.mp.android.apps.main.bookR.model.IBookRFragmentModelImpl;
import com.mp.android.apps.main.bookR.presenter.IBookRRecommendFPresenter;
import com.mp.android.apps.main.bookR.view.IBookRRecommendFView;
import com.mp.android.apps.main.home.bean.HomeDesignBean;
import com.mp.android.apps.main.home.bean.SourceListContent;
import com.mp.android.apps.monke.basemvplib.impl.BasePresenterImpl;
import com.mp.android.apps.monke.monkeybook.base.observer.SimpleObserver;
import com.mp.android.apps.monke.monkeybook.cache.ACache;
import com.mp.android.apps.utils.AssertFileUtils;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class BookRRecommendFPresenterImpl extends BasePresenterImpl<IBookRRecommendFView> implements IBookRRecommendFPresenter {
    private ACache mCache;

    public static final String BOOKRRECOMMENDDATA = "book_fragment_cache";


    public BookRRecommendFPresenterImpl() {
        this.mCache = ACache.get(MyApplication.getInstance());
    }

    @Override
    public void detachView() {

    }

    @Override
    public void initBookRRcommendData() {
        String recommendCacheJson = mCache.getAsString(BOOKRRECOMMENDDATA);
        if (!TextUtils.isEmpty(recommendCacheJson)) {
            notifyRecyclerViewRefresh(recommendCacheJson, true);
        }
        IBookRFragmentModelImpl.getInstance().getBookRHomeData().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new SimpleObserver<String>() {
            @Override
            public void onNext(String s) {
                notifyRecyclerViewRefresh(s, false);
                mCache.put(BOOKRRECOMMENDDATA, s);
            }

            @Override
            public void onError(Throwable e) {
                if (TextUtils.isEmpty(recommendCacheJson)) {
                    String localData = AssertFileUtils.getJson(mView.getContext(), "bookRfragment.json");
                    notifyRecyclerViewRefresh(localData, false);
                }

            }
        });
    }

    @Override
    public void getNextPageContent() {
        IBookRFragmentModelImpl.getInstance().getMoreRecommendList().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new SimpleObserver<String>() {
            @Override
            public void onNext(String s) {
                JSONObject jsonObject = JSON.parseObject(s);
                JSONObject data = (JSONObject) jsonObject.get("data");
                if (data != null) {
                    String moreRecommendJson = JSON.toJSONString(data.get("moreRecommendList"));
                    List<SourceListContent> moreRecommendList = JSON.parseArray(moreRecommendJson, SourceListContent.class);
                    mView.notifyMoreRecommendList(moreRecommendList);
                }else {
                    mView.notifyMoreRecommendList(null);
                }
            }

            @Override
            public void onError(Throwable e) {
                mView.notifyMoreRecommendList(null);

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
            String recommendJson = JSON.toJSONString(data.get("recommend"));
            String hotRankingJson = JSON.toJSONString(data.get("hotRanking"));
            String contentListJson = JSON.toJSONString(data.get("contentList"));
            if (!TextUtils.isEmpty(recommendJson) && !TextUtils.isEmpty(hotRankingJson) && !TextUtils.isEmpty(contentListJson)) {

                List<SourceListContent> recommendList = JSON.parseArray(recommendJson, SourceListContent.class);
                List<SourceListContent> hotRankingList = JSON.parseArray(hotRankingJson, SourceListContent.class);
                List<SourceListContent> contentList = JSON.parseArray(contentListJson, SourceListContent.class);
                if (recommendList != null && recommendList.size() == 3
                        && hotRankingList != null && hotRankingList.size() == 6
                        && contentList != null && contentList.size() > 0
                ) {
                    mView.notifyRecyclerView(recommendList, hotRankingList, contentList, useCache);
                } else {
                    String localData = AssertFileUtils.getJson(mView.getContext(), "bookRfragment.json");
                    notifyRecyclerViewRefresh(localData, false);
                }
            }
        }
    }
}
