package com.mp.android.apps.book.presenter.impl;

import android.content.Context;
import android.widget.Toast;

import com.hwangjr.rxbus.RxBus;
import com.mp.android.apps.MyApplication;
import com.mp.android.apps.base.IView;
import com.mp.android.apps.base.impl.BaseActivity;
import com.mp.android.apps.book.base.observer.SimpleObserver;
import com.mp.android.apps.book.common.RxBusTag;
import com.mp.android.apps.book.model.WebBookModelControl;
import com.mp.android.apps.read.bean.BookChapterBean;
import com.mp.android.apps.read.bean.CollBookBean;
import com.mp.android.apps.read.local.BookRepository;
import com.mp.android.apps.read.local.DaoDbHelper;
import com.mp.android.apps.read.utils.Constant;
import com.mp.android.apps.read.utils.StringUtils;
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
     */
    public void addToBookShelfUtils(CollBookBean collBookBean) {
        //乐观更新：立即通知搜索页/书架"已添加"，不必等待章节目录的网络请求(否则列表刷新很慢)
        RxBus.get().post(RxBusTag.HAD_ADD_BOOK, collBookBean);
        WebBookModelControl.getInstance().getBookChapters(collBookBean)
                .toObservable()
                .flatMap(new Function<List<BookChapterBean>, ObservableSource<Boolean>>() {
                    @Override
                    public ObservableSource<Boolean> apply(List<BookChapterBean> bookChapterBeans) throws Exception {
                        return Observable.create(new ObservableOnSubscribe<Boolean>() {
                            @Override
                            public void subscribe(ObservableEmitter<Boolean> emitter) throws Exception {
                                //在表的一对多联接中，只是简单地创建两个表以及使用@ToMany注解经常会报错.
                                collBookBean.__setDaoSession(DaoDbHelper.getInstance().getSession());
                                if (collBookBean.getBookChapterList() == null || collBookBean.getBookChapterList().size() == 0) {
                                    collBookBean.setBookChapters(bookChapterBeans);
                                }
                                try {
                                    collBookBean.setLastRead(StringUtils.
                                            dateConvert(System.currentTimeMillis(), Constant.FORMAT_BOOK_DATE));
                                    BookRepository.getInstance().saveCollBookWithAsync(collBookBean);
                                    emitter.onNext(true);
                                } catch (Exception e) {
                                    android.util.Log.e("BookShelUtils", "collect book failed", e);
                                    emitter.onNext(false);
                                }

                                emitter.onComplete();
                            }
                        });
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean value) {
                        RxBus.get().post(RxBusTag.HIDE_COLLECTION_RLLODING, new Object());
                        if (!value) {
                            //入库失败，回滚之前的乐观"已添加"状态
                            RxBus.get().post(RxBusTag.HAD_REMOVE_BOOK, collBookBean);
                            Toast.makeText(MyApplication.getInstance(), collBookBean.getTitle()+"放入书架失败!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        android.util.Log.e("BookShelUtils", "collect book failed", e);
                        RxBus.get().post(RxBusTag.HIDE_COLLECTION_RLLODING,new Object());
                        //失败回滚乐观"已添加"状态
                        RxBus.get().post(RxBusTag.HAD_REMOVE_BOOK, collBookBean);
                        Toast.makeText(MyApplication.getInstance(), collBookBean.getTitle()+"放入书架失败!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public Observable<CollBookBean>  getBookInfoUtils(CollBookBean collBookBean){
        return WebBookModelControl.getInstance().getBookInfo(collBookBean);

    }


}
