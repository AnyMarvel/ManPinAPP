package com.mp.android.apps.book.model.impl;

import com.mp.android.apps.book.base.MBaseModelImpl;
import com.mp.android.apps.book.base.observer.SimpleObserver;
import com.mp.android.apps.book.common.api.IBookInfoApi;
import com.mp.android.apps.readActivity.bean.CollBookBean;

import io.reactivex.android.schedulers.AndroidSchedulers;

import io.reactivex.schedulers.Schedulers;

public class ObtainBookInfoImpl extends MBaseModelImpl {

    public static ObtainBookInfoImpl getInstance() {
        return new ObtainBookInfoImpl();
    }

    public void senMessageManpin(CollBookBean collBookBean, String kind, String lastChapter) {
        try {
            if (collBookBean.get_id().contains("http")) {
                getRetrofitObject("http://aimanpin.com").create(IBookInfoApi.class)
                        .obtainBookInfo(collBookBean.get_id(),
                                collBookBean.getCover(),
                                collBookBean.getTitle(),
                                collBookBean.getAuthor(),
                                lastChapter,
                                collBookBean.getBookTag(),
                                collBookBean.getBookTag(),
                                kind)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new SimpleObserver<String>() {
                            @Override
                            public void onNext(String s) {

                            }

                            @Override
                            public void onError(Throwable e) {

                            }
                        });
            }

        } catch (Exception e) {

        }

    }


}
