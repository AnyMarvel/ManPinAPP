
package com.mp.android.apps.book.presenter.impl;

import androidx.annotation.NonNull;

import android.text.TextUtils;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.mp.android.apps.base.IView;
import com.mp.android.apps.base.impl.BasePresenterImpl;
import com.mp.android.apps.book.base.observer.SimpleObserver;
import com.mp.android.apps.book.common.RxBusTag;
import com.mp.android.apps.book.dao.BookChapterBeanDao;
import com.mp.android.apps.book.model.WebBookModelControl;
import com.mp.android.apps.book.presenter.IMainPresenter;
import com.mp.android.apps.book.utils.BookSourceCheckUtils;
import com.mp.android.apps.book.utils.NetworkUtil;
import com.mp.android.apps.book.view.IMainView;
import com.mp.android.apps.read.bean.BookChapterBean;
import com.mp.android.apps.read.bean.CollBookBean;
import com.mp.android.apps.read.local.BookRepository;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class MainPresenterImpl extends BasePresenterImpl<IMainView> implements IMainPresenter {

    /** 更新检查的最大并发，避免同时对过多书源发起请求 */
    private static final int MAX_UPDATE_CONCURRENCY = 3;
    /** 统一管理订阅，detachView 时释放，防止离开页面后回调触碰失效视图 */
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    public void queryBookShelf(final Boolean needRefresh) {
        if (needRefresh) {
            mView.activityRefreshView();
        }
        Observable.create(new ObservableOnSubscribe<List<CollBookBean>>() {
            @Override
            public void subscribe(ObservableEmitter<List<CollBookBean>> e) throws Exception {
                List<CollBookBean> bookShelfes;
                try {
                    bookShelfes = BookRepository.getInstance().getCollBooks();
                } catch (Exception e1) {
                    bookShelfes = null;
                }

                e.onNext(bookShelfes == null ? new ArrayList<CollBookBean>() : bookShelfes);
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<CollBookBean>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(List<CollBookBean> value) {
                        if (mView == null || value == null) return;
                        mView.refreshBookShelf(value);
                        if (needRefresh) {
                            startRefreshBook(value);
                        } else {
                            mView.refreshFinish();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        android.util.Log.e("MainPresenter", "request error", e);
                        if (mView != null) {
                            mView.refreshError(NetworkUtil.getErrorTip(NetworkUtil.ERROR_CODE_ANALY));
                        }
                    }
                });
    }

    @Override
    public boolean bookSourceSwitch() {
        return BookSourceCheckUtils.bookSourceSwitch(mView.getContext());
    }

    public void startRefreshBook(List<CollBookBean> value) {
        if (value == null || value.isEmpty()) {
            if (mView != null) mView.refreshFinish();
            return;
        }
        if (mView != null) mView.setRecyclerMaxProgress(value.size());
        // 有界并发地逐本检查更新；单本失败不影响其余，全部完成后回读书架刷新 UI
        Disposable d = Observable.fromIterable(value)
                .flatMap(new Function<CollBookBean, ObservableSource<CollBookBean>>() {
                    @Override
                    public ObservableSource<CollBookBean> apply(CollBookBean book) {
                        return refreshOneBook(book).subscribeOn(Schedulers.io()).toObservable();
                    }
                }, MAX_UPDATE_CONCURRENCY)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<CollBookBean>() {
                    @Override
                    public void accept(CollBookBean book) {
                        if (mView != null) mView.refreshRecyclerViewItemAdd();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable e) {
                        android.util.Log.e("MainPresenter", "refresh error", e);
                        if (mView != null) {
                            mView.refreshError(NetworkUtil.getErrorTip(NetworkUtil.ERROR_CODE_NONET));
                        }
                    }
                }, new Action() {
                    @Override
                    public void run() {
                        queryBookShelf(false);
                    }
                });
        compositeDisposable.add(d);
    }

    /**
     * 检查单本书是否有更新：联网拉取最新章节目录，若最新章节标题与本地不同则视为有更新，
     * 更新 lastChapter 并置 isUpdate=true（书架显示"更新"提示），同时落库新目录。
     * 本地书、无书源地址、或书源已下线的书直接跳过。全程吞异常，保证刷新链不因单本失败中断。
     */
    private Single<CollBookBean> refreshOneBook(final CollBookBean book) {
        return Single.create(new SingleOnSubscribe<CollBookBean>() {
            @Override
            public void subscribe(SingleEmitter<CollBookBean> emitter) {
                try {
                    if (book != null && !book.isLocal() && !TextUtils.isEmpty(book.getBookChapterUrl())) {
                        Single<List<BookChapterBean>> chaptersSingle =
                                WebBookModelControl.getInstance().getBookChapters(book);
                        if (chaptersSingle != null) {
                            List<BookChapterBean> newChapters = chaptersSingle.blockingGet();
                            if (newChapters != null && !newChapters.isEmpty()) {
                                String newLast = newChapters.get(newChapters.size() - 1).getTitle();
                                if (newLast != null && !newLast.equals(book.getLastChapter())) {
                                    book.setUpdate(true);
                                    book.setLastChapter(newLast);
                                    for (BookChapterBean c : newChapters) {
                                        c.setBookId(book.get_id());
                                    }
                                    BookRepository.getInstance().getSession()
                                            .getBookChapterBeanDao().insertOrReplaceInTx(newChapters);
                                }
                            }
                        }
                        BookRepository.getInstance().saveCollBook(book);
                    }
                } catch (Throwable t) {
                    android.util.Log.e("MainPresenter", "refresh one book failed", t);
                }
                // 单本无论成败都正常发射，保证刷新链继续处理其余书籍
                emitter.onSuccess(book);
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
        //释放所有进行中的订阅，避免刷新途中离开书架页时回调触碰失效视图
        compositeDisposable.clear();
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
