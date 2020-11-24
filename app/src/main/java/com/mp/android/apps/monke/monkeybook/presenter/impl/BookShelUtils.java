package com.mp.android.apps.monke.monkeybook.presenter.impl;

import android.widget.Toast;

import com.hwangjr.rxbus.RxBus;
import com.mp.android.apps.MyApplication;
import com.mp.android.apps.monke.basemvplib.IView;
import com.mp.android.apps.monke.basemvplib.impl.BaseActivity;
import com.mp.android.apps.monke.monkeybook.base.observer.SimpleObserver;
import com.mp.android.apps.monke.monkeybook.common.RxBusTag;
import com.mp.android.apps.monke.monkeybook.dao.DbHelper;
import com.mp.android.apps.monke.monkeybook.model.impl.WebBookModelImpl;
import com.mp.android.apps.monke.readActivity.bean.BookChapterBean;
import com.mp.android.apps.monke.readActivity.bean.CollBookBean;
import com.mp.android.apps.monke.readActivity.local.BookRepository;
import com.mp.android.apps.monke.readActivity.utils.Constant;
import com.mp.android.apps.monke.readActivity.utils.StringUtils;
import com.trello.rxlifecycle2.android.ActivityEvent;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class BookShelUtils {
    private static BookShelUtils bookShelUtils;

    private BookShelUtils() {
    }

    public static BookShelUtils getInstance() {
        if (bookShelUtils == null) {
            synchronized (BookShelUtils.class) {
                bookShelUtils = new BookShelUtils();
            }
        }
        return bookShelUtils;
    }

    /**
     * 基于CollBookBean添加到书架
     * 适用于搜索到的书籍，主页推荐书籍
     *
     * @param collBookBean
     * @param mView
     */
    public void addToBookShelfUtils(CollBookBean collBookBean, IView mView) {
        WebBookModelImpl.getInstance().getBookChapters(collBookBean)
                .toObservable()
                .flatMap(new Function<List<BookChapterBean>, ObservableSource<Boolean>>() {
                    @Override
                    public ObservableSource<Boolean> apply(List<BookChapterBean> bookChapterBeans) throws Exception {
                        return Observable.create(new ObservableOnSubscribe<Boolean>() {
                            @Override
                            public void subscribe(ObservableEmitter<Boolean> emitter) throws Exception {
                                //在表的一对多联接中，只是简单地创建两个表以及使用@ToMany注解经常会报错.
                                collBookBean.__setDaoSession(DbHelper.getInstance().getmDaoSession());
                                if (collBookBean.getBookChapterList() == null || collBookBean.getBookChapterList().size() == 0) {
                                    collBookBean.setBookChapters(bookChapterBeans);
                                }
                                try {
                                    collBookBean.setLastRead(StringUtils.
                                            dateConvert(System.currentTimeMillis(), Constant.FORMAT_BOOK_DATE));
                                    BookRepository.getInstance().saveCollBookWithAsync(collBookBean);
                                    emitter.onNext(true);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    emitter.onNext(false);
                                }

                                emitter.onComplete();
                            }
                        });
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(((BaseActivity) mView.getContext()).<Boolean>bindUntilEvent(ActivityEvent.DESTROY))
                .subscribe(new SimpleObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean value) {
                        if (value) {
                            RxBus.get().post(RxBusTag.HAD_ADD_BOOK, collBookBean);
                        } else {
                            Toast.makeText(MyApplication.getInstance(), "放入书架失败!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Toast.makeText(MyApplication.getInstance(), "放入书架失败!", Toast.LENGTH_SHORT).show();
                    }
                });

    }
}
