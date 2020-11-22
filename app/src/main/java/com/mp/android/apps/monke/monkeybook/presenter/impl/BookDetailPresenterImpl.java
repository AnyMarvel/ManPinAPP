
package com.mp.android.apps.monke.monkeybook.presenter.impl;

import android.content.Intent;

import androidx.annotation.NonNull;

import android.widget.Toast;

import com.google.android.apps.photolab.storyboard.download.MD5Utils;
import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.mp.android.apps.monke.basemvplib.IView;
import com.mp.android.apps.monke.basemvplib.impl.BaseActivity;
import com.mp.android.apps.monke.basemvplib.impl.BasePresenterImpl;
import com.mp.android.apps.monke.monkeybook.BitIntentDataManager;
import com.mp.android.apps.monke.monkeybook.base.observer.SimpleObserver;
import com.mp.android.apps.monke.monkeybook.bean.SearchBookBean;
import com.mp.android.apps.monke.monkeybook.common.RxBusTag;
import com.mp.android.apps.monke.monkeybook.dao.CollBookBeanDao;
import com.mp.android.apps.monke.monkeybook.dao.DbHelper;
import com.mp.android.apps.monke.monkeybook.model.impl.WebBookModelImpl;
import com.mp.android.apps.monke.monkeybook.presenter.IBookDetailPresenter;
import com.mp.android.apps.monke.monkeybook.view.IBookDetailView;
import com.mp.android.apps.MyApplication;
import com.mp.android.apps.monke.readActivity.bean.BookChapterBean;
import com.mp.android.apps.monke.readActivity.bean.CollBookBean;
import com.mp.android.apps.monke.readActivity.local.BookRepository;
import com.mp.android.apps.monke.readActivity.local.remote.RemoteRepository;
import com.mp.android.apps.monke.readActivity.utils.Constant;
import com.mp.android.apps.monke.readActivity.utils.RxUtils;
import com.mp.android.apps.monke.readActivity.utils.StringUtils;
import com.trello.rxlifecycle2.android.ActivityEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class BookDetailPresenterImpl extends BasePresenterImpl<IBookDetailView> implements IBookDetailPresenter {
    public final static int FROM_BOOKSHELF = 1;
    public final static int FROM_SEARCH = 2;

    private int openfrom;
    private SearchBookBean searchBook;
    private CollBookBean collBookBean;
    private Boolean inBookShelf = false;

    private List<CollBookBean> localCollBooks = Collections.synchronizedList(new ArrayList<CollBookBean>());   //用来比对搜索的书籍是否已经添加进书架

    public BookDetailPresenterImpl(Intent intent) {
        openfrom = intent.getIntExtra("from", FROM_BOOKSHELF);


        if (openfrom == FROM_BOOKSHELF) {
            String key = intent.getStringExtra("data_key");
            collBookBean = (CollBookBean) BitIntentDataManager.getInstance().getData(key);
            BitIntentDataManager.getInstance().cleanData(key);
            inBookShelf = true;
        } else {
            searchBook = intent.getParcelableExtra("data");
            inBookShelf = searchBook.getAdd();
        }
    }

    public Boolean getInBookShelf() {
        return inBookShelf;
    }

    public void setInBookShelf(Boolean inBookShelf) {
        this.inBookShelf = inBookShelf;
    }

    public int getOpenfrom() {
        return openfrom;
    }

    public SearchBookBean getSearchBook() {
        return searchBook;
    }


    @Override
    public CollBookBean getCollBookBean() {
        return collBookBean;
    }

    @Override
    public void getBookShelfInfo() {
        CollBookBean collBookInfo = new CollBookBean().getCollBookBeanFromSearch(searchBook);
        if (!"noimage".equals(collBookInfo.getCover())) {
            CollBookBean localCollBookBean = BookRepository.getInstance().getSession().getCollBookBeanDao().queryBuilder().where(CollBookBeanDao.Properties._id.eq(collBookInfo.get_id())).build().unique();
            if (localCollBookBean != null) {
                inBookShelf = true;
            }
            collBookBean = collBookInfo;
            mView.updateView();
        } else {
            Observable.create(new ObservableOnSubscribe<CollBookBean>() {
                @Override
                public void subscribe(ObservableEmitter<CollBookBean> emitter) throws Exception {
                    emitter.onNext(collBookInfo);
                }
            }).flatMap(new Function<CollBookBean, ObservableSource<CollBookBean>>() {
                @Override
                public ObservableSource<CollBookBean> apply(CollBookBean collBookBean) throws Exception {
                    return WebBookModelImpl.getInstance().getBookInfo(collBookBean);
                }
            }).subscribeOn(Schedulers.io())
                    .compose(((BaseActivity) mView.getContext()).<CollBookBean>bindUntilEvent(ActivityEvent.DESTROY))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SimpleObserver<CollBookBean>() {
                        @Override
                        public void onNext(CollBookBean value) {
                            CollBookBean localCollBookBean = BookRepository.getInstance().getSession().getCollBookBeanDao().queryBuilder().where(CollBookBeanDao.Properties._id.eq(value.get_id())).build().unique();
                            if (localCollBookBean != null) {
                                inBookShelf = true;
                            }
                            collBookBean = value;
                            mView.updateView();
                        }

                        @Override
                        public void onError(Throwable e) {
                            collBookBean = null;
                            mView.getBookShelfError();
                        }
                    });

        }


    }

    @Override
    public void addToBookShelf() {
        if (collBookBean != null) {
            BookShelUtils.getInstance().addToBookShelfUtils(collBookBean, mView);
        }
    }

    @Override
    public void removeFromBookShelf() {
        if (collBookBean != null) {
            Observable.create(new ObservableOnSubscribe<Boolean>() {
                @Override
                public void subscribe(ObservableEmitter<Boolean> e) throws Exception {
                    BookRepository.getInstance().deleteCollBookSync(collBookBean);
                    e.onNext(true);
                    e.onComplete();
                }
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .compose(((BaseActivity) mView.getContext()).<Boolean>bindUntilEvent(ActivityEvent.DESTROY))
                    .subscribe(new SimpleObserver<Boolean>() {
                        @Override
                        public void onNext(Boolean value) {
                            if (value) {
                                RxBus.get().post(RxBusTag.HAD_REMOVE_BOOK, collBookBean);
                            } else {
                                Toast.makeText(MyApplication.getInstance(), "移出书架失败!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                            Toast.makeText(MyApplication.getInstance(), "移出书架失败!", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Override
    public void attachView(@NonNull IView iView) {
        super.attachView(iView);
        RxBus.get().register(this);
    }

    @Override
    public void detachView() {
        RxBus.get().unregister(this);
    }

    @Subscribe(
            thread = EventThread.MAIN_THREAD,
            tags = {
                    @Tag(RxBusTag.HAD_ADD_BOOK)
            }
    )
    public void hadAddBook(CollBookBean value) {
        if ((null != collBookBean && value.get_id().equals(collBookBean.get_id()))
                || (null != searchBook && value.get_id().equals(searchBook.getNoteUrl()))) {
            inBookShelf = true;
            if (null != searchBook) {
                searchBook.setAdd(inBookShelf);
            }
            mView.updateView();
        }
    }

    @Subscribe(
            thread = EventThread.MAIN_THREAD,
            tags = {
                    @Tag(RxBusTag.HAD_REMOVE_BOOK)
            }
    )
    public void hadRemoveBook(CollBookBean value) {
        if (localCollBooks != null) {
            for (int i = 0; i < localCollBooks.size(); i++) {
                if (localCollBooks.get(i).get_id().equals(value.get_id())) {
                    localCollBooks.remove(i);
                    break;
                }
            }
        }
        if ((null != collBookBean && value.get_id().equals(collBookBean.get_id()))
                || (null != searchBook && value.get_id().equals(searchBook.getNoteUrl()))) {
            inBookShelf = false;
            if (null != searchBook) {
                searchBook.setAdd(false);
            }
            mView.updateView();
        }
    }

    @Subscribe(
            thread = EventThread.MAIN_THREAD,
            tags = {
                    @Tag(RxBusTag.HAD_ADD_BOOK),
            }
    )
    public void hadBook(CollBookBean value) {
        localCollBooks.add(value);
        if ((null != collBookBean && value.get_id().equals(collBookBean.get_id())) || (null != searchBook && value.get_id().equals(searchBook.getNoteUrl()))) {
            inBookShelf = true;
            if (null != searchBook) {
                searchBook.setAdd(true);
            }
            mView.updateView();
        }
    }

}
