package com.mp.android.apps.main.home.presenter.impl;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mp.android.apps.MyApplication;
import com.mp.android.apps.main.home.bean.HomeDesignBean;
import com.mp.android.apps.main.home.bean.SourceListContent;
import com.mp.android.apps.main.home.model.IMainFragmentModelImpl;
import com.mp.android.apps.main.home.presenter.IMainFragmentPresenter;
import com.mp.android.apps.main.home.view.IMainfragmentView;

import com.mp.android.apps.basemvplib.impl.BasePresenterImpl;
import com.mp.android.apps.book.base.observer.SimpleObserver;
import com.mp.android.apps.book.cache.ACache;
import com.mp.android.apps.utils.AssertFileUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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



    @Override
    public void initSpiderHomeData() {
        String mainCacheJson = mCache.getAsString(MAINFRAGMENTCACHEDATA);
        if (!TextUtils.isEmpty(mainCacheJson)) {
            notifyRecyclerView(mainCacheJson);
        }
        IMainFragmentModelImpl.getInstance().getHomeData()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<String>() {
                    @Override
                    public void onNext(String s) {
                       notifyRecyclerView(s);
                        mCache.put(MAINFRAGMENTCACHEDATA, s);
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (TextUtils.isEmpty(mainCacheJson)){
                            String localData = AssertFileUtils.getJson(mView.getContext(), "localhome.json");
                            notifyRecyclerView(localData);
                        }
                    }
                });

    }

    private void notifyRecyclerView(String s){
        try {
            Document doc = Jsoup.parse(s);
            List<Element> elementList=doc.getElementsByClass("load");
            List<Map<String,String>> carouselList=new ArrayList<>();//首页轮播图图片
            for (int i = 0; i < elementList.size()-1; i++) {
                Map<String,String> map=new HashMap<>();
                map.put("name",elementList.get(i).attr("alt"));
                map.put("url","https:"+elementList.get(i).attr("data-src").trim());
                carouselList.add(map);
            }
            Element wanbenLayout=doc.getElementsByClass("slides").get(0);
            List<Element> recommendList=wanbenLayout.getElementsByClass("slideItem");
            List<Map<String,String>> recommendInfoList=new ArrayList<>();//首页轮播图图片
            for (int i = 0; i < recommendList.size(); i++) {
                Element recommendElement=recommendList.get(i).getElementsByTag("a").get(0).getElementsByTag("img").get(0);
                Map<String,String> map=new HashMap<>();
                map.put("name",recommendElement.attr("alt"));
                map.put("url","https:"+recommendElement.attr("src").trim());
                recommendInfoList.add(map);
            }
            mView.notifyRecyclerHomePage(carouselList,recommendInfoList);
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }




}
