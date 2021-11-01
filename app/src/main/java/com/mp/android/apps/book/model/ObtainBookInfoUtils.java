package com.mp.android.apps.book.model;

import com.mp.android.apps.book.base.MBaseModelImpl;
import com.mp.android.apps.book.base.observer.SimpleObserver;
import com.mp.android.apps.book.common.api.IBookInfoApi;
import com.mp.android.apps.main.home.bean.SourceListContent;
import com.mp.android.apps.readActivity.bean.CollBookBean;

import io.reactivex.android.schedulers.AndroidSchedulers;

import io.reactivex.schedulers.Schedulers;

public class ObtainBookInfoUtils extends MBaseModelImpl {

    public static ObtainBookInfoUtils getInstance() {
        return new ObtainBookInfoUtils();
    }

    //本地CollBookBean类型转换为Server端BookInfo实体对象
    public SourceListContent translateBookInfo(CollBookBean collBookBean) {
        SourceListContent sourceListContent = new SourceListContent();
        sourceListContent.setNoteUrl(collBookBean.get_id());
        sourceListContent.setCoverUrl(collBookBean.getCover());
        sourceListContent.setName(collBookBean.getTitle());
        sourceListContent.setAuthor(collBookBean.getAuthor());
        sourceListContent.setTag(collBookBean.getBookTag());
        sourceListContent.setOrigin(collBookBean.getBookTag());
        return sourceListContent;
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
