
package com.mp.android.apps.monke.monkeybook.presenter.impl;

import androidx.annotation.NonNull;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.mp.android.apps.monke.basemvplib.IView;
import com.mp.android.apps.monke.basemvplib.impl.BasePresenterImpl;
import com.mp.android.apps.monke.monkeybook.base.observer.SimpleObserver;
import com.mp.android.apps.monke.monkeybook.common.RxBusTag;
import com.mp.android.apps.monke.monkeybook.presenter.IMainPresenter;
import com.mp.android.apps.monke.monkeybook.utils.NetworkUtil;
import com.mp.android.apps.monke.monkeybook.view.IMainView;
import com.mp.android.apps.monke.readActivity.bean.CollBookBean;
import com.mp.android.apps.monke.readActivity.local.BookRepository;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MainPresenterImpl extends BasePresenterImpl<IMainView> implements IMainPresenter {

    public void queryBookShelf(final Boolean needRefresh) {
        if (needRefresh)
            mView.activityRefreshView();
        Observable.create(new ObservableOnSubscribe<List<CollBookBean>>() {
            @Override
            public void subscribe(ObservableEmitter<List<CollBookBean>> e) throws Exception {
                List<CollBookBean> bookShelfes = BookRepository.getInstance().getCollBooks();
                e.onNext(bookShelfes == null ? new ArrayList<CollBookBean>() : bookShelfes);
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<CollBookBean>>() {
                    @Override
                    public void onNext(List<CollBookBean> value) {
                        if (null != value) {
                            mView.refreshBookShelf(value);
                            if (needRefresh) {
                                startRefreshBook(value);
                            } else {
                                mView.refreshFinish();
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        mView.refreshError(NetworkUtil.getErrorTip(NetworkUtil.ERROR_CODE_ANALY));
                    }
                });
    }

    public void startRefreshBook(List<CollBookBean> value) {
        if (value != null && value.size() > 0) {
            mView.setRecyclerMaxProgress(value.size());
            refreshBookShelf(value, 0);
        } else {
            mView.refreshFinish();
        }
    }

    private void refreshBookShelf(final List<CollBookBean> value, final int index) {
        if (index <= value.size() - 1) {
            saveBookToShelf(value, index);
        } else {
            queryBookShelf(false);
        }
    }

    private void saveBookToShelf(final List<CollBookBean> datas, final int index) {
        Observable.create(new ObservableOnSubscribe<CollBookBean>() {
            @Override
            public void subscribe(ObservableEmitter<CollBookBean> e) throws Exception {
                BookRepository.getInstance().saveCollBook(datas.get(index));
                e.onNext(datas.get(index));
                e.onComplete();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<CollBookBean>() {
                    @Override
                    public void onNext(CollBookBean value) {
                        mView.refreshRecyclerViewItemAdd();
                        refreshBookShelf(datas, index + 1);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        mView.refreshError(NetworkUtil.getErrorTip(NetworkUtil.ERROR_CODE_NONET));
                    }
                });
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////

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
                    @Tag(RxBusTag.HAD_ADD_BOOK),
                    @Tag(RxBusTag.HAD_REMOVE_BOOK),
                    @Tag(RxBusTag.UPDATE_BOOK_PROGRESS)
            }
    )
    public void hadddOrRemoveBook(CollBookBean bookShelfBean) {
        queryBookShelf(false);
    }
}
