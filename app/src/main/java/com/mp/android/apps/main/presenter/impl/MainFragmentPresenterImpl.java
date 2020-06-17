package com.mp.android.apps.main.presenter.impl;

import android.text.TextUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mp.android.apps.main.bean.HomeDesignBean;
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
                    if (!TextUtils.isEmpty(homebookJson)) {
                        List<HomeDesignBean> list = JSON.parseArray(homebookJson, HomeDesignBean.class);
                        if (list != null && list.size() > 0) {
                            mView.notifyRecyclerView(list, carouselImages);
                        }

                    }
                }
            }

            @Override
            public void onError(Throwable e) {

            }
        });
    }
}
