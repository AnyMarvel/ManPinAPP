//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.mp.android.apps.monke.monkeybook.presenter.impl;

import android.os.Handler;

import com.mp.android.apps.monke.basemvplib.impl.BasePresenterImpl;

import com.mp.android.apps.monke.monkeybook.base.observer.SimpleObserver;
import com.mp.android.apps.monke.monkeybook.bean.LibraryBean;
import com.mp.android.apps.monke.monkeybook.cache.ACache;
import com.mp.android.apps.monke.monkeybook.model.impl.GxwztvBookModelImpl;
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
    private ACache mCache;
    private Boolean isFirst = true;

    private final LinkedHashMap<String,String> kinds = new LinkedHashMap<>();

    public LibraryPresenterImpl() {
        kinds.put("东方玄幻","http://www.wzzw.la/xuanhuanxiaoshuo/");
        kinds.put("西方奇幻","http://www.wzzw.la/qihuanxiaoshuo/");
        kinds.put("热血修真","http://www.wzzw.la/xiuzhenxiaoshuo/");
        kinds.put("武侠仙侠","http://www.wzzw.la/wuxiaxiaoshuo/");
        kinds.put("都市爽文","http://www.wzzw.la/dushixiaoshuo/");
        kinds.put("言情暧昧","http://www.wzzw.la/yanqingxiaoshuo/");
        kinds.put("灵异悬疑","http://www.wzzw.la/lingyixiaoshuo/");
        kinds.put("运动竞技","http://www.wzzw.la/jingjixiaoshuo/");
        kinds.put("历史架空","http://www.wzzw.la/lishixiaoshuo/");
        kinds.put("审美","http://www.wzzw.la/danmeixiaoshuo/");
        kinds.put("科幻迷航","http://www.wzzw.la/kehuanxiaoshuo/");
        kinds.put("游戏人生","http://www.wzzw.la/youxixiaoshuo/");
        kinds.put("军事斗争","http://www.wzzw.la/junshixiaoshuo/");
        kinds.put("商战人生","http://www.wzzw.la/shangzhanxiaoshuo/");
        kinds.put("校园爱情","http://www.wzzw.la/xiaoyuanxiaoshuo/");
        kinds.put("官场仕途","http://www.wzzw.la/guanchangxiaoshuo/");
        kinds.put("娱乐明星","http://www.wzzw.la/zhichangxiaoshuo/");
        kinds.put("其他","http://www.wzzw.la/qitaxiaoshuo/");

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
            }).flatMap(new Function<String, ObservableSource<LibraryBean>>() {
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
        }else{
            getLibraryNewData();
        }
    }

    private void getLibraryNewData() {
        GxwztvBookModelImpl.getInstance().getLibraryData(mCache).subscribeOn(Schedulers.io())
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
                        },1000);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        mView.finishRefresh();
                    }
                });
    }

    @Override
    public LinkedHashMap<String, String> getKinds() {
        return kinds;
    }
}