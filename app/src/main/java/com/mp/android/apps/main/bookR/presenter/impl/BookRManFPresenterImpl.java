package com.mp.android.apps.main.bookR.presenter.impl;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mp.android.apps.MyApplication;
import com.mp.android.apps.main.bookR.model.IBookRFragmentModelImpl;
import com.mp.android.apps.main.bookR.presenter.IBookRManFPresenter;
import com.mp.android.apps.main.bookR.view.IBookRManFView;
import com.mp.android.apps.main.home.bean.HomeDesignBean;
import com.mp.android.apps.main.home.bean.SourceListContent;
import com.mp.android.apps.basemvplib.impl.BasePresenterImpl;
import com.mp.android.apps.book.base.observer.SimpleObserver;
import com.mp.android.apps.book.cache.ACache;
import com.mp.android.apps.utils.AssertFileUtils;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class BookRManFPresenterImpl extends BasePresenterImpl<IBookRManFView> implements IBookRManFPresenter {
    private ACache mCache;
    /**
     * 男生推荐缓存
     */
    private static final String BOOKRMANDATA = "book_fragment_man_cache";
    /**
     * 女生推荐缓存
     */
    private static final String BOOKRWOMANDATA = "book_fragment_woman_cache";

    public BookRManFPresenterImpl() {
        this.mCache = ACache.get(MyApplication.getInstance());
    }

    @Override
    public void detachView() {

    }

    @Override
    public void initManData() {
        String recommendCacheJson = mCache.getAsString(BOOKRMANDATA);
        if (!TextUtils.isEmpty(recommendCacheJson)) {
            notifyRecyclerViewRefresh(recommendCacheJson, true);
        }

        IBookRFragmentModelImpl.getInstance().getBookManHomeData().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new SimpleObserver<String>() {
            @Override
            public void onNext(String s) {
                notifyRecyclerViewRefresh(s, false);
                mCache.put(BOOKRMANDATA, s);
            }

            @Override
            public void onError(Throwable e) {
                if (TextUtils.isEmpty(recommendCacheJson)) {
                    String localData = AssertFileUtils.getJson(mView.getContext(), "bookManfragment.json");
                    notifyRecyclerViewRefresh(localData, false);
                }

            }
        });
    }

    @Override
    public void initWoManData() {
        String recommendCacheJson = mCache.getAsString(BOOKRWOMANDATA);
        if (!TextUtils.isEmpty(recommendCacheJson)) {
            notifyRecyclerViewRefresh(recommendCacheJson, true);
        }

        IBookRFragmentModelImpl.getInstance().getBookWomanHomeData().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new SimpleObserver<String>() {
            @Override
            public void onNext(String s) {
                notifyRecyclerViewRefresh(s, false);
                mCache.put(BOOKRWOMANDATA, s);
            }

            @Override
            public void onError(Throwable e) {
                if (TextUtils.isEmpty(recommendCacheJson)) {
                    String localData = AssertFileUtils.getJson(mView.getContext(), "bookWomanfragment.json");
                    notifyRecyclerViewRefresh(localData, false);
                }

            }
        });
    }

    @Override
    public void getBookCardData(int mContentPosition, String kinds) {
        IBookRFragmentModelImpl.getInstance().getContentItemData(kinds).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new SimpleObserver<String>() {
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
                List<HomeDesignBean> contentList = JSON.parseArray(contentListJson, HomeDesignBean.class);
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
