package com.mp.android.apps.main.presenter.impl;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mp.android.apps.main.bean.HomeDesignBean;
import com.mp.android.apps.main.bean.SourceListContent;
import com.mp.android.apps.main.model.IMainFragmentModelImpl;
import com.mp.android.apps.main.presenter.IMainFragmentPresenter;
import com.mp.android.apps.main.view.IMainfragmentView;

import com.mp.android.apps.monke.basemvplib.impl.BasePresenterImpl;
import com.mp.android.apps.monke.monkeybook.base.observer.SimpleObserver;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MainFragmentPresenterImpl extends BasePresenterImpl<IMainfragmentView> implements IMainFragmentPresenter {

    @Override
    public void detachView() {

    }

    /**
     * 初始化首页数据
     */
    @Override
    public void initHomeData() {
        IMainFragmentModelImpl.getInstance().getHomeDatas().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new SimpleObserver<String>() {
            @Override
            public void onNext(String s) {
                JSONObject jsonObject = JSON.parseObject(s);
                JSONObject data = (JSONObject) jsonObject.get("data");
                if (data != null) {
                    List<String> carouselImages = (List<String>) data.get("carouselImages");
                    String homebookJson = JSON.toJSONString(data.get("homeBook"));
                    String recommendJson = JSON.toJSONString(data.get("recommend"));
                    if (!TextUtils.isEmpty(homebookJson) && !TextUtils.isEmpty(recommendJson)) {
                        List<HomeDesignBean> list = JSON.parseArray(homebookJson, HomeDesignBean.class);
                        List<SourceListContent> recommendList = JSON.parseArray(recommendJson, SourceListContent.class);
                        if (list != null && list.size() > 0) {
                            mView.notifyRecyclerView(list, carouselImages, recommendList);
                        }

                    }
                }
            }

            @Override
            public void onError(Throwable e) {
                initHomeData();
            }
        });
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
