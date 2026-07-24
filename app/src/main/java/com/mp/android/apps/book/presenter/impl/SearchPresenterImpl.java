
package com.mp.android.apps.book.presenter.impl;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.mp.android.apps.base.IView;
import com.mp.android.apps.base.impl.BaseActivity;
import com.mp.android.apps.base.impl.BasePresenterImpl;
import com.mp.android.apps.book.base.observer.SimpleObserver;
import com.mp.android.apps.book.bean.SearchBookBean;
import com.mp.android.apps.book.bean.SearchHistoryBean;
import com.mp.android.apps.book.common.RxBusTag;
import com.mp.android.apps.book.dao.SearchHistoryBeanDao;
import com.mp.android.apps.book.model.WebBookModelControl;
import com.mp.android.apps.book.presenter.ISearchPresenter;
import com.mp.android.apps.book.view.ISearchView;
import com.mp.android.apps.read.bean.CollBookBean;
import com.mp.android.apps.read.local.DaoDbHelper;
import com.trello.rxlifecycle2.android.ActivityEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class SearchPresenterImpl extends BasePresenterImpl<ISearchView> implements ISearchPresenter {
    public static final String TAG_KEY = "tag";

    public static final int BOOK = 2;

    private Boolean hasSearch = false;   //判断是否搜索过

    private int page = 1;
    private List<Map<String,String>> searchEngine;


    private long startThisSearchTime;

    private List<CollBookBean> collBookBeans = new ArrayList<>();   //用来比对搜索的书籍是否已经添加进书架

    private Boolean isInput = false;

    public SearchPresenterImpl() {
        // 书架数据改到 attachView 中异步加载，避免在主线程（onCreate）查询数据库造成卡顿
    }

    /**
     * 异步加载本地书架，用于标记搜索结果是否已加入书架。
     * 放在 IO 线程执行，结果回到主线程更新，避免阻塞 UI。
     */
    private void loadCollBooks() {
        Observable.create(new ObservableOnSubscribe<List<CollBookBean>>() {
            @Override
            public void subscribe(ObservableEmitter<List<CollBookBean>> e) throws Exception {
                List<CollBookBean> temp = DaoDbHelper.getInstance().getSession()
                        .getCollBookBeanDao().queryBuilder().list();
                e.onNext(temp != null ? temp : new ArrayList<CollBookBean>());
                e.onComplete();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<CollBookBean>>() {
                    @Override
                    public void onNext(List<CollBookBean> value) {
                        collBookBeans.clear();
                        collBookBeans.addAll(value);
                    }

                    @Override
                    public void onError(Throwable e) {
                        android.util.Log.e("SearchPresenter", "load coll books failed", e);
                    }
                });
    }


    @Override
    public Boolean getHasSearch() {
        return hasSearch;
    }

    @Override
    public void setHasSearch(Boolean hasSearch) {
        this.hasSearch = hasSearch;
    }

    @Override
    public void insertSearchHistory() {
        final int type = SearchPresenterImpl.BOOK;
        final String content = mView.getEdtContent().getText().toString().trim();
        Observable.create(new ObservableOnSubscribe<SearchHistoryBean>() {
            @Override
            public void subscribe(ObservableEmitter<SearchHistoryBean> e) throws Exception {
                List<SearchHistoryBean> datas = DaoDbHelper.getInstance().getSession().getSearchHistoryBeanDao()
                        .queryBuilder()
                        .where(SearchHistoryBeanDao.Properties.Type.eq(type), SearchHistoryBeanDao.Properties.Content.eq(content))
                        .limit(1)
                        .build().list();
                SearchHistoryBean searchHistoryBean = null;
                if (null != datas && datas.size() > 0) {
                    searchHistoryBean = datas.get(0);
                    searchHistoryBean.setDate(System.currentTimeMillis());
                    DaoDbHelper.getInstance().getSession().getSearchHistoryBeanDao().update(searchHistoryBean);
                } else {
                    searchHistoryBean = new SearchHistoryBean(type, content, System.currentTimeMillis());
                    DaoDbHelper.getInstance().getSession().getSearchHistoryBeanDao().insert(searchHistoryBean);
                }
                e.onNext(searchHistoryBean);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<SearchHistoryBean>() {
                    @Override
                    public void onNext(SearchHistoryBean value) {
                        mView.insertSearchHistorySuccess(value);
                    }

                    @Override
                    public void onError(Throwable e) {
                        android.util.Log.e("SearchPresenter", "request error", e);
                    }
                });
    }

    @Override
    public void cleanSearchHistory() {
        final int type = SearchPresenterImpl.BOOK;
        final String content = mView.getEdtContent().getText().toString().trim();
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                int a = DaoDbHelper.getInstance().getDatabase().delete(SearchHistoryBeanDao.TABLENAME, SearchHistoryBeanDao.Properties.Type.columnName + "=? and " + SearchHistoryBeanDao.Properties.Content.columnName + " like ?", new String[]{String.valueOf(type), "%" + content + "%"});
                e.onNext(a);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<Integer>() {
                    @Override
                    public void onNext(Integer value) {
                        if (value > 0) {
                            mView.querySearchHistorySuccess(null);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        android.util.Log.e("SearchPresenter", "request error", e);
                    }
                });
    }

    @Override
    public void querySearchHistory() {
        final int type = SearchPresenterImpl.BOOK;
        final String content = mView.getEdtContent().getText().toString().trim();
        Observable.create(new ObservableOnSubscribe<List<SearchHistoryBean>>() {
            @Override
            public void subscribe(ObservableEmitter<List<SearchHistoryBean>> e) throws Exception {
                List<SearchHistoryBean> datas = DaoDbHelper.getInstance().getSession().getSearchHistoryBeanDao()
                        .queryBuilder()
                        .where(SearchHistoryBeanDao.Properties.Type.eq(type), SearchHistoryBeanDao.Properties.Content.like("%" + content + "%"))
                        .orderDesc(SearchHistoryBeanDao.Properties.Date)
                        .limit(20)
                        .build().list();
                e.onNext(datas);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<SearchHistoryBean>>() {
                    @Override
                    public void onNext(List<SearchHistoryBean> value) {
                        if (null != value)
                            mView.querySearchHistorySuccess(value);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
    }

    @Override
    public int getPage() {
        return page;
    }

    @Override
    public void initPage() {
        this.page = 1;
    }

    @Override
    public void toSearchBooks(String key, Boolean fromError) {
        if (key != null) {
            this.startThisSearchTime = System.currentTimeMillis();
        }
        searchBook(key, startThisSearchTime, fromError);
    }


    private AtomicInteger callBackNumber;

    private void searchBook(final String content, final long searchTime, Boolean fromError){
            if (searchEngine == null || searchEngine.isEmpty()) {
                mView.refreshFinish(false);
                return;
            }
            // 每次搜索使用独立的计数器，避免快速连续搜索时旧回调串扰计数
            final AtomicInteger callBack = new AtomicInteger();
            callBackNumber = callBack;
            final int engineSize = searchEngine.size();

            for (int i = 0; i < engineSize; i++) {
                WebBookModelControl.getInstance().searchOtherBook(content, page, (String) searchEngine.get(i).get(TAG_KEY))
                        .subscribeOn(Schedulers.io())
                        // 绑定 Activity 生命周期，页面销毁后自动取消，避免回调已销毁视图导致崩溃
                        .compose(((BaseActivity) mView.getContext()).<List<SearchBookBean>>bindUntilEvent(ActivityEvent.DESTROY))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new SimpleObserver<List<SearchBookBean>>() {
                            @Override
                            public void onNext(List<SearchBookBean> value) {
                                // 丢弃过期搜索（用户已发起新的搜索）的回调
                                if (searchTime != startThisSearchTime) {
                                    return;
                                }
                                if (value != null) {
                                    // 判断当前搜索图书是否已经加入本地
                                    for (SearchBookBean temp : value) {
                                        if (temp == null || temp.getNoteUrl() == null) {
                                            continue;
                                        }
                                        for (CollBookBean collBookBean : collBookBeans) {
                                            if (temp.getNoteUrl().equals(collBookBean.get_id())) {
                                                temp.setAdd(true);
                                                break;
                                            }
                                        }
                                    }
                                    mView.loadMoreSearchBook(content, value);
                                }
                                if (callBack.incrementAndGet() == engineSize) {
                                    mView.refreshFinish(false);
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                android.util.Log.e("SearchPresenter", "search error", e);
                                if (searchTime != startThisSearchTime) {
                                    return;
                                }
                                if (callBack.incrementAndGet() == engineSize) {
                                    mView.refreshFinish(false);
                                }
                            }
                        });
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void addBookToShelf(final SearchBookBean searchBookBean) {
        CollBookBean collBookBean = new CollBookBean().getCollBookBeanFromSearch(searchBookBean);
        if (collBookBean != null) {
            if (collBookBean.getBookChapterUrl() != null) {
                BookShelUtils.getInstance().addToBookShelfUtils(collBookBean);
            } else {
                WebBookModelControl.getInstance().getBookInfo(collBookBean).subscribeOn(Schedulers.io())
                        .compose(((BaseActivity) mView.getContext()).<CollBookBean>bindUntilEvent(ActivityEvent.DESTROY))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new SimpleObserver<CollBookBean>() {
                            @Override
                            public void onNext(CollBookBean collBookBean) {
                                BookShelUtils.getInstance().addToBookShelfUtils(collBookBean);
                            }

                            @Override
                            public void onError(Throwable e) {
                                Toast.makeText(mView.getContext(), "加入书架失败", Toast.LENGTH_LONG).show();
                            }
                        });
            }
        }


    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void attachView(@NonNull IView iView) {
        super.attachView(iView);
        //搜索引擎初始化
        searchEngine = new ArrayList<>();
        WebBookModelControl.getInstance().registerSearchEngine(searchEngine, mView.getContext());
        //注册 eventBus
        RxBus.get().register(this);
        //异步加载本地书架
        loadCollBooks();
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
    /**
     * rxbus发送消息进行接受
     */
    public void hadAddBook(CollBookBean collBookBean) {
        if (collBookBean == null || collBookBean.get_id() == null) {
            return;
        }
        collBookBeans.add(collBookBean);
        List<SearchBookBean> datas = mView.getSearchBookAdapter().getSearchBooks();
        if (datas == null) {
            return;
        }
        for (int i = 0; i < datas.size(); i++) {
            SearchBookBean bean = datas.get(i);
            if (bean != null && collBookBean.get_id().equals(bean.getNoteUrl())) {
                bean.setAdd(true);
                mView.updateSearchItem(i);
                break;
            }
        }
    }

    @Subscribe(
            thread = EventThread.MAIN_THREAD,
            tags = {
                    @Tag(RxBusTag.HAD_REMOVE_BOOK)
            }
    )
    public void hadRemoveBook(CollBookBean collBookBean) {
        if (collBookBean == null || collBookBean.get_id() == null) {
            return;
        }
        if (collBookBeans != null) {
            for (int i = 0; i < collBookBeans.size(); i++) {
                CollBookBean coll = collBookBeans.get(i);
                if (coll != null && collBookBean.get_id().equals(coll.get_id())) {
                    collBookBeans.remove(i);
                    break;
                }
            }
        }
        List<SearchBookBean> datas = mView.getSearchBookAdapter().getSearchBooks();
        if (datas == null) {
            return;
        }
        for (int i = 0; i < datas.size(); i++) {
            SearchBookBean bean = datas.get(i);
            if (bean != null && collBookBean.get_id().equals(bean.getNoteUrl())) {
                bean.setAdd(false);
                mView.updateSearchItem(i);
                break;
            }
        }
    }

    @Override
    public Boolean getInput() {
        return isInput;
    }

    @Override
    public void setInput(Boolean input) {
        isInput = input;
    }
}
