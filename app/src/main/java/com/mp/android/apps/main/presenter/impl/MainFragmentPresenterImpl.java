package com.mp.android.apps.main.presenter.impl;

import android.view.View;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mp.android.apps.R;
import com.mp.android.apps.main.cycleimage.BannerInfo;
import com.mp.android.apps.main.cycleimage.CycleViewPager;
import com.mp.android.apps.main.model.IMainFragmentModelImpl;
import com.mp.android.apps.main.presenter.IMainFragmentPresenter;
import com.mp.android.apps.main.view.IMainfragmentView;

import com.mp.android.apps.monke.basemvplib.impl.BasePresenterImpl;
import com.mp.android.apps.monke.monkeybook.base.observer.SimpleObserver;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MainFragmentPresenterImpl extends BasePresenterImpl<IMainfragmentView> implements IMainFragmentPresenter {

    @Override
    public void detachView() {

    }

    @Override
    public void initmCycleViewPager(CycleViewPager mCycleViewPager) {


    }

    @Override
    public void initHomeData() {
        IMainFragmentModelImpl.getInstance().getHomeDatas().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new SimpleObserver<String>() {
            @Override
            public void onNext(String s) {
                JSONObject jsonObject = JSON.parseObject(s);
                JSONObject data = (JSONObject) jsonObject.get("data");
                if (data != null) {
                    List<String> carouselImages = (List<String>) data.get("carouselImages");
                    if (carouselImages != null) {
                        mView.updatemCycleViewPager(carouselImages);
                    }

                }
            }

            @Override
            public void onError(Throwable e) {
                mView.updatemCycleViewPager(null);
            }
        });
    }
}
