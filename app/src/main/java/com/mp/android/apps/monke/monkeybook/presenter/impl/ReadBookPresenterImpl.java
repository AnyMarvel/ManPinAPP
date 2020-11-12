
package com.mp.android.apps.monke.monkeybook.presenter.impl;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.android.apps.photolab.storyboard.activity.ComicSplash;
import com.google.android.apps.photolab.storyboard.download.DownloadUtil;
import com.google.android.apps.photolab.storyboard.soloader.SoStatus;
import com.hwangjr.rxbus.RxBus;
import com.mp.android.apps.monke.basemvplib.impl.BaseActivity;
import com.mp.android.apps.monke.basemvplib.impl.BasePresenterImpl;
import com.mp.android.apps.monke.monkeybook.BitIntentDataManager;

import com.mp.android.apps.monke.monkeybook.base.observer.SimpleObserver;
import com.mp.android.apps.monke.monkeybook.bean.BookContentBean;
import com.mp.android.apps.monke.monkeybook.bean.BookShelfBean;
import com.mp.android.apps.monke.monkeybook.bean.ChapterListBean;
import com.mp.android.apps.monke.monkeybook.bean.LocBookShelfBean;
import com.mp.android.apps.monke.monkeybook.bean.ReadBookContentBean;
import com.mp.android.apps.monke.monkeybook.common.RxBusTag;
import com.mp.android.apps.monke.monkeybook.dao.BookContentBeanDao;
import com.mp.android.apps.monke.monkeybook.dao.BookShelfBeanDao;
import com.mp.android.apps.monke.monkeybook.dao.DbHelper;
import com.mp.android.apps.monke.monkeybook.model.impl.ImportBookModelImpl;
import com.mp.android.apps.monke.monkeybook.model.impl.WebBookModelImpl;
import com.mp.android.apps.monke.monkeybook.presenter.IBookReadPresenter;
import com.mp.android.apps.monke.monkeybook.utils.PremissionCheck;
import com.mp.android.apps.monke.monkeybook.view.IBookReadView;
import com.mp.android.apps.monke.monkeybook.widget.contentswitchview.BookContentView;
import com.mp.android.apps.MyApplication;
import com.mylhyl.acp.Acp;
import com.mylhyl.acp.AcpListener;
import com.mylhyl.acp.AcpOptions;
import com.trello.rxlifecycle2.android.ActivityEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class ReadBookPresenterImpl extends BasePresenterImpl<IBookReadView> implements IBookReadPresenter {

    public final static int OPEN_FROM_OTHER = 0;
    public final static int OPEN_FROM_APP = 1;

    private Boolean isAdd = false; //判断是否已经添加进书架
    private int open_from;
    private BookShelfBean bookShelf;

    /**
     * 每页有多少行文字
     * 假设5行一页
     */
    private int pageLineCount = 5;

    public ReadBookPresenterImpl() {

    }

    /**
     * 初始化阅读界面数据
     *
     * @param activity
     */
    @Override
    public void initData(Activity activity) {
        Intent intent = activity.getIntent();
        open_from = intent.getIntExtra("from", OPEN_FROM_OTHER);
        if (open_from == OPEN_FROM_APP) {
            String key = intent.getStringExtra("data_key");
            bookShelf = (BookShelfBean) BitIntentDataManager.getInstance().getData(key);
            if (bookShelf != null && !bookShelf.getTag().equals(BookShelfBean.LOCAL_TAG)) {
                mView.showDownloadMenu();
            }
            BitIntentDataManager.getInstance().cleanData(key);
            checkInShelf();
        } else {
            Acp.getInstance(activity).request(new AcpOptions.Builder()
                    .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE).build(), new AcpListener() {
                @Override
                public void onGranted() {
                    openBookFromOther(activity);
                }

                @Override
                public void onDenied(List<String> permissions) {
                    Toast.makeText(activity, "读写权限被权限被拒绝,请到设置界面允许被拒绝权限", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @Override
    public void openBookFromOther(Activity activity) {
        //APP外部打开
        Uri uri = activity.getIntent().getData();
        mView.showLoadBook();
        getRealFilePath(activity, uri)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new SimpleObserver<String>() {
                    @Override
                    public void onNext(String value) {
                        ImportBookModelImpl.getInstance().importBook(new File(value))
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribeOn(Schedulers.io())
                                .subscribe(new SimpleObserver<LocBookShelfBean>() {
                                    @Override
                                    public void onNext(LocBookShelfBean value) {
                                        if (value.getNew())
                                            RxBus.get().post(RxBusTag.HAD_ADD_BOOK, value);
                                        bookShelf = value.getBookShelfBean();
                                        mView.dimissLoadBook();
                                        checkInShelf();
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        e.printStackTrace();
                                        mView.dimissLoadBook();
                                        mView.loadLocationBookError();
                                        Toast.makeText(MyApplication.getInstance(), "文本打开失败！", Toast.LENGTH_SHORT).show();

                                    }
                                });
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        mView.dimissLoadBook();
                        mView.loadLocationBookError();
                        Toast.makeText(MyApplication.getInstance(), "文本打开失败！", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void detachView() {
    }

    @Override
    public int getOpen_from() {
        return open_from;
    }

    @Override
    public BookShelfBean getBookShelf() {
        return bookShelf;
    }

    @Override
    public void initContent() {
        mView.initContentSuccess(bookShelf.getDurChapter(), bookShelf.getBookInfoBean().getChapterlist().size(), bookShelf.getDurChapterPage());
    }

    @Override
    public void loadContent(final BookContentView bookContentView, final long bookTag, final int chapterIndex, int pageIndex) {
        if (null != bookShelf && bookShelf.getBookInfoBean().getChapterlist().size() > 0) {

            BookContentBean contentBookContentBean = bookShelf.getBookInfoBean().getChapterlist().get(chapterIndex).getBookContentBean();
            if (null != contentBookContentBean && null != contentBookContentBean.getDurCapterContent()) {
                if (contentBookContentBean.getLineSize() == mView.getPaint().getTextSize() && contentBookContentBean.getLineContent().size() > 0) {
                    /**
                     * 数据源数组已经划分为可使用数组
                     *
                     * 基于数据源计算当前章节总页数,
                     * 每页展示的内容等,基于bookContentView.updateData方法更新当前页面
                     *
                     */
                    int tempCount = (int) Math.ceil(contentBookContentBean.getLineContent().size() * 1.0 / pageLineCount) - 1;

                    if (pageIndex == BookContentView.DURPAGEINDEXBEGIN) {
                        pageIndex = 0;
                    } else if (pageIndex == BookContentView.DURPAGEINDEXEND) {
                        pageIndex = tempCount;
                    } else {
                        if (pageIndex >= tempCount) {
                            pageIndex = tempCount;
                        }
                    }

                    int start = pageIndex * pageLineCount;//当前章节起始行
                    int end = pageIndex == tempCount ? contentBookContentBean.getLineContent().size() : start + pageLineCount;//当前章节结束行

                    if (bookContentView != null && bookTag == bookContentView.getqTag()) {
                        bookContentView.updateData(bookTag, bookShelf.getBookInfoBean().getChapterlist().get(chapterIndex).getDurChapterName()
                                , contentBookContentBean.getLineContent().subList(start, end)
                                , chapterIndex
                                , bookShelf.getBookInfoBean().getChapterlist().size()
                                , pageIndex
                                , tempCount + 1);
                    }
                } else {
                    /**
                     * 有源数据(原始数据)  重新分行
                     * 将当前章节(爬虫获取到的当前章节的内容)按照当前屏幕每行的可能容多少字体分割为多行,
                     * 存储到 BookContentBean->lineContent数组中用于进行界面渲染
                     */
                    contentBookContentBean.setLineSize(mView.getPaint().getTextSize());
                    final int finalPageIndex = pageIndex;
                    SeparateParagraphtoLines(contentBookContentBean.getDurCapterContent())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .compose(((BaseActivity) mView.getContext()).<List<String>>bindUntilEvent(ActivityEvent.DESTROY))
                            .subscribe(new SimpleObserver<List<String>>() {
                                @Override
                                public void onNext(List<String> value) {
                                    contentBookContentBean.getLineContent().clear();
                                    contentBookContentBean.getLineContent().addAll(value);
                                    loadContent(bookContentView, bookTag, chapterIndex, finalPageIndex);
                                }

                                @Override
                                public void onError(Throwable e) {
                                    if (bookContentView != null && bookTag == bookContentView.getqTag())
                                        bookContentView.loadError();
                                }
                            });
                }
            } else {
                final int finalPageIndex1 = pageIndex;
                ChapterListBean durChapterListBean = bookShelf.getBookInfoBean().getChapterlist().get(chapterIndex);
                Observable.create(new ObservableOnSubscribe<ReadBookContentBean>() {
                    @Override
                    public void subscribe(ObservableEmitter<ReadBookContentBean> e) throws Exception {
                        List<BookContentBean> tempList = DbHelper.getInstance().getmDaoSession().getBookContentBeanDao()
                                .queryBuilder()
                                .where(BookContentBeanDao.Properties.DurChapterUrl.eq(durChapterListBean.getDurChapterUrl())).build().list();
                        e.onNext(new ReadBookContentBean(tempList == null ? new ArrayList<BookContentBean>() : tempList, finalPageIndex1));
                        e.onComplete();
                    }
                }).observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .compose(((BaseActivity) mView.getContext()).<ReadBookContentBean>bindUntilEvent(ActivityEvent.DESTROY))
                        .subscribe(new SimpleObserver<ReadBookContentBean>() {
                            @Override
                            public void onNext(ReadBookContentBean tempList) {
                                if (tempList.getBookContentList() != null && tempList.getBookContentList().size() > 0
                                        && tempList.getBookContentList().get(0).getDurCapterContent() != null) {
                                    durChapterListBean.setBookContentBean(tempList.getBookContentList().get(0));
                                    loadContent(bookContentView, bookTag, chapterIndex, tempList.getPageIndex());
                                } else {
                                    final int finalPageIndex1 = tempList.getPageIndex();
                                    WebBookModelImpl.
                                            getInstance().
                                            getBookContent(durChapterListBean.getDurChapterUrl(), chapterIndex, bookShelf.getTag()).
                                            map(new Function<BookContentBean, BookContentBean>() {
                                                @Override
                                                public BookContentBean apply(BookContentBean bookContentBean) throws Exception {
                                                    if (bookContentBean.getRight()) {
                                                        DbHelper.getInstance().getmDaoSession().getBookContentBeanDao().insertOrReplace(bookContentBean);
                                                        durChapterListBean.setHasCache(true);
                                                        DbHelper.getInstance().getmDaoSession().getChapterListBeanDao().update(durChapterListBean);
                                                    }
                                                    return bookContentBean;
                                                }
                                            })
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribeOn(Schedulers.io())
                                            .compose(((BaseActivity) mView.getContext()).<BookContentBean>bindUntilEvent(ActivityEvent.DESTROY))
                                            .subscribe(new SimpleObserver<BookContentBean>() {
                                                @Override
                                                public void onNext(BookContentBean value) {
                                                    if (value.getDurChapterUrl() != null && value.getDurChapterUrl().length() > 0) {
                                                        bookShelf.getBookInfoBean().getChapterlist().get(chapterIndex).setBookContentBean(value);
                                                        if (bookTag == bookContentView.getqTag())
                                                            loadContent(bookContentView, bookTag, chapterIndex, finalPageIndex1);
                                                    } else {
                                                        if (bookContentView != null && bookTag == bookContentView.getqTag())
                                                            bookContentView.loadError();
                                                    }
                                                }

                                                @Override
                                                public void onError(Throwable e) {
                                                    e.printStackTrace();
                                                    if (bookContentView != null && bookTag == bookContentView.getqTag())
                                                        bookContentView.loadError();
                                                }
                                            });
                                }
                            }

                            @Override
                            public void onError(Throwable e) {

                            }
                        });
            }
        } else {
            if (bookContentView != null && bookTag == bookContentView.getqTag())
                bookContentView.loadError();
        }
    }

    @Override
    public void updateProgress(int chapterIndex, int pageIndex) {
        bookShelf.setDurChapter(chapterIndex);
        bookShelf.setDurChapterPage(pageIndex);
    }

    @Override
    public void saveProgress() {
        if (bookShelf != null) {
            Observable.create(new ObservableOnSubscribe<BookShelfBean>() {
                @Override
                public void subscribe(ObservableEmitter<BookShelfBean> e) throws Exception {
                    bookShelf.setFinalDate(System.currentTimeMillis());
                    DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().insertOrReplace(bookShelf);
                    e.onNext(bookShelf);
                    e.onComplete();
                }
            }).subscribeOn(Schedulers.io())
                    .subscribe(new SimpleObserver<BookShelfBean>() {
                        @Override
                        public void onNext(BookShelfBean value) {
                            RxBus.get().post(RxBusTag.UPDATE_BOOK_PROGRESS, bookShelf);
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                        }
                    });
        }
    }

    @Override
    public String getChapterTitle(int chapterIndex) {
        if (bookShelf.getBookInfoBean().getChapterlist().size() > chapterIndex) {
            return bookShelf.getBookInfoBean().getChapterlist().get(chapterIndex).getDurChapterName();
        } else {
            return "无章节";
        }

    }

    /**
     * 将单章文本内容以当前设置字体大小转换为StaticLayout,然后逐行存储到新数组中
     * linesdata 数组 每行存储的数据为需要进行展示的数据
     *
     * @param paragraphstr 单章数据源
     * @return 返回被观察者, 将获取到的需要裁剪的数据源进行向下传递
     */
    public Observable<List<String>> SeparateParagraphtoLines(final String paragraphstr) {
        return Observable.create(new ObservableOnSubscribe<List<String>>() {
            @Override
            public void subscribe(ObservableEmitter<List<String>> e) throws Exception {
                TextPaint mPaint = (TextPaint) mView.getPaint();
                mPaint.setSubpixelText(true);
                Layout tempLayout = new StaticLayout(paragraphstr, mPaint, mView.getContentWidth(), Layout.Alignment.ALIGN_NORMAL, 0, 0, false);
                List<String> linesdata = new ArrayList<String>();
                for (int i = 0; i < tempLayout.getLineCount(); i++) {
                    linesdata.add(paragraphstr.substring(tempLayout.getLineStart(i), tempLayout.getLineEnd(i)));
                }
                e.onNext(linesdata);
                e.onComplete();
            }
        });
    }

    @Override
    public void setPageLineCount(int pageLineCount) {
        this.pageLineCount = pageLineCount;
    }

    private void checkInShelf() {
        Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> e) throws Exception {
                List<BookShelfBean> temp = DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().queryBuilder().where(BookShelfBeanDao.Properties.NoteUrl.eq(bookShelf.getNoteUrl())).build().list();
                if (temp == null || temp.size() == 0) {
                    isAdd = false;
                } else
                    isAdd = true;
                e.onNext(isAdd);
                e.onComplete();
            }
        }).subscribeOn(Schedulers.io())
                .compose(((BaseActivity) mView.getContext()).<Boolean>bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean value) {
                        mView.initPop();
                        mView.setHpbReadProgressMax(bookShelf.getBookInfoBean().getChapterlist().size());
                        mView.startLoadingBook();
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                });
    }

    public interface OnAddListner {
        public void addSuccess();
    }

    @Override
    public void addToShelf(final OnAddListner addListner) {
        if (bookShelf != null) {
            Observable.create(new ObservableOnSubscribe<Boolean>() {
                @Override
                public void subscribe(ObservableEmitter<Boolean> e) throws Exception {
                    DbHelper.getInstance().getmDaoSession().getChapterListBeanDao().insertOrReplaceInTx(bookShelf.getBookInfoBean().getChapterlist());
                    DbHelper.getInstance().getmDaoSession().getBookInfoBeanDao().insertOrReplace(bookShelf.getBookInfoBean());
                    //网络数据获取成功  存入BookShelf表数据库
                    DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().insertOrReplace(bookShelf);
                    RxBus.get().post(RxBusTag.HAD_ADD_BOOK, bookShelf);
                    isAdd = true;
                    e.onNext(true);
                    e.onComplete();
                }
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SimpleObserver<Object>() {
                        @Override
                        public void onNext(Object value) {
                            if (addListner != null)
                                addListner.addSuccess();
                        }

                        @Override
                        public void onError(Throwable e) {

                        }
                    });
        }
    }

    public Boolean getAdd() {
        return isAdd;
    }

    public Observable<String> getRealFilePath(final Context context, final Uri uri) {
        return Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                String data = "";
                if (null != uri) {
                    final String scheme = uri.getScheme();
                    if (scheme == null)
                        data = uri.getPath();
                    else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
                        data = uri.getPath();
                    } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
                        Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
                        if (null != cursor) {
                            if (cursor.moveToFirst()) {
                                int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                                if (index > -1) {
                                    data = cursor.getString(index);
                                }
                            }
                            cursor.close();
                        }

                        if ((data == null || data.length() <= 0) && uri.getPath() != null && uri.getPath().contains("/storage/emulated/")) {
                            data = uri.getPath().substring(uri.getPath().indexOf("/storage/emulated/"));
                        }
                    }
                }
                e.onNext(data == null ? "" : data);
                e.onComplete();
            }
        });
    }
}
