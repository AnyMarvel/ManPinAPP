package com.mp.android.apps.monke.monkeybook.model.impl;

import com.mp.android.apps.monke.monkeybook.base.MBaseModelImpl;
import com.mp.android.apps.monke.monkeybook.base.observer.SimpleObserver;
import com.mp.android.apps.monke.monkeybook.bean.BookInfoBean;
import com.mp.android.apps.monke.monkeybook.common.api.IBookInfoApi;
import io.reactivex.android.schedulers.AndroidSchedulers;

import io.reactivex.schedulers.Schedulers;

public class ObtainBookInfoImpl extends MBaseModelImpl {

    public static ObtainBookInfoImpl getInstance() {
        return new ObtainBookInfoImpl();
    }

    public void senMessageManpin(BookInfoBean bookInfoBean, String kind, String lastChapter) {
        try {
            if (bookInfoBean.getNoteUrl().contains("http")) {
                getRetrofitObject("http://aimanpin.com").create(IBookInfoApi.class)
                        .obtainBookInfo(bookInfoBean.getNoteUrl(),
                                bookInfoBean.getCoverUrl(),
                                bookInfoBean.getName(),
                                bookInfoBean.getAuthor(),
                                lastChapter,
                                bookInfoBean.getTag(),
                                bookInfoBean.getOrigin(),
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
