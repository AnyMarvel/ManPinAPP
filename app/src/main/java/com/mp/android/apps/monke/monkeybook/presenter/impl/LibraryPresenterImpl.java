
package com.mp.android.apps.monke.monkeybook.presenter.impl;

import android.os.Handler;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.mp.android.apps.monke.basemvplib.impl.BasePresenterImpl;

import com.mp.android.apps.monke.monkeybook.base.observer.SimpleObserver;
import com.mp.android.apps.monke.monkeybook.bean.LibraryBean;
import com.mp.android.apps.monke.monkeybook.bean.WebsiteAvailableBean;
import com.mp.android.apps.monke.monkeybook.cache.ACache;
import com.mp.android.apps.monke.monkeybook.model.impl.GxwztvBookModelImpl;
import com.mp.android.apps.monke.monkeybook.model.impl.LibraryModelImpl;
import com.mp.android.apps.monke.monkeybook.model.impl.LingdiankanshuStationBookModelImpl;
import com.mp.android.apps.monke.monkeybook.model.impl.WebBookModelImpl;
import com.mp.android.apps.monke.monkeybook.presenter.ILibraryPresenter;
import com.mp.android.apps.monke.monkeybook.view.ILibraryView;
import com.mp.android.apps.MyApplication;

import java.util.LinkedHashMap;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;

import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class LibraryPresenterImpl extends BasePresenterImpl<ILibraryView> implements ILibraryPresenter {
    public final static String LIBRARY_CACHE_KEY = "cache_library";
    public final static String LIBRARY_AVAILABLEWEBSITE = "cache_website";
    private ACache mCache;
    private Boolean isFirst = true;

    public LibraryPresenterImpl() {
        mCache = ACache.get(MyApplication.getInstance());
    }

    @Override
    public void detachView() {

    }

    @Override
    public void getLibraryData() {
        if (isFirst) {
            isFirst = false;
            Observable.create(new ObservableOnSubscribe<String>() {
                @Override
                public void subscribe(ObservableEmitter<String> e) throws Exception {
                    String cache = mCache.getAsString(LIBRARY_CACHE_KEY);
                    e.onNext(cache);
                    e.onComplete();
                }
            })
                    .flatMap(new Function<String, ObservableSource<LibraryBean>>() {
                        @Override
                        public ObservableSource<LibraryBean> apply(String s) throws Exception {
                            return GxwztvBookModelImpl.getInstance().analyLibraryData(s);
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SimpleObserver<LibraryBean>() {
                        @Override
                        public void onNext(LibraryBean value) {
                            //执行刷新界面
                            mView.updateUI(value);
                            getLibraryNewData();
                        }

                        @Override
                        public void onError(Throwable e) {
                            getLibraryNewData();
                        }
                    });
        } else {
            getLibraryNewData();
        }
    }

    private void getLibraryNewData() {
        GxwztvBookModelImpl.getInstance().getLibraryData(mCache)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<LibraryBean>() {
                    @Override
                    public void onNext(final LibraryBean value) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mView.updateUI(value);
                                mView.finishRefresh();
                            }
                        }, 1000);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        mView.finishRefresh();
                    }
                });
    }

    @Override
    public void getKinds() {
        LibraryModelImpl.getInstance().getNewWebsiteAvailable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<String>() {
                    @Override
                    public void onNext(String s) {
                        WebsiteAvailableBean websiteAvailableBean = JSON.parseObject(s, WebsiteAvailableBean.class);


                        mView.updateNav(WebBookModelImpl.getInstance().getBookNav(websiteAvailableBean.getData()));
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.updateNav(GxwztvBookModelImpl.getInstance().getBookNavs());
                    }
                });
    }

}