
package com.mp.android.apps.monke.monkeybook.model.impl;

import com.mp.android.apps.monke.monkeybook.ErrorAnalyContentManager;
import com.mp.android.apps.monke.monkeybook.base.MBaseModelImpl;
import com.mp.android.apps.monke.monkeybook.base.observer.SimpleObserver;
import com.mp.android.apps.monke.monkeybook.bean.BookContentBean;
import com.mp.android.apps.monke.monkeybook.bean.BookInfoBean;
import com.mp.android.apps.monke.monkeybook.bean.BookShelfBean;
import com.mp.android.apps.monke.monkeybook.bean.ChapterListBean;
import com.mp.android.apps.monke.monkeybook.bean.SearchBookBean;
import com.mp.android.apps.monke.monkeybook.bean.WebChapterBean;
import com.mp.android.apps.monke.monkeybook.common.api.IGxwztvApi;
import com.mp.android.apps.monke.monkeybook.listener.OnGetChapterListListener;
import com.mp.android.apps.monke.monkeybook.model.IGxwztvBookModel;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class GxwztvBookModelImpl extends MBaseModelImpl implements IGxwztvBookModel {
    public static final String TAG = "http://www.wzzw.la";

    public static GxwztvBookModelImpl getInstance() {
        return new GxwztvBookModelImpl();
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public Observable<List<SearchBookBean>> searchBook(String content, int page) {
        return getRetrofitObject(TAG).create(IGxwztvApi.class).searchBook(content, page).flatMap(new Function<String, ObservableSource<List<SearchBookBean>>>() {
            @Override
            public ObservableSource<List<SearchBookBean>> apply(String s) throws Exception {
                return analySearchBook(s);
            }
        });
    }

    public Observable<List<SearchBookBean>> analySearchBook(final String s) {
        return Observable.create(new ObservableOnSubscribe<List<SearchBookBean>>() {
            @Override
            public void subscribe(ObservableEmitter<List<SearchBookBean>> e) throws Exception {
                try {
                    Document doc = Jsoup.parse(s);
                    Elements booksE = doc.getElementById("novel-list").getElementsByClass("list-group-item clearfix");
                    if (null != booksE && booksE.size() >= 2) {
                        List<SearchBookBean> books = new ArrayList<SearchBookBean>();
                        for (int i = 1; i < booksE.size(); i++) {
                            SearchBookBean item = new SearchBookBean();
                            item.setTag(TAG);
                            item.setAuthor(booksE.get(i).getElementsByClass("col-xs-2").get(0).text());
                            item.setKind(booksE.get(i).getElementsByClass("col-xs-1").get(0).text());
                            item.setLastChapter(booksE.get(i).getElementsByClass("col-xs-4").get(0).getElementsByTag("a").get(0).text());
                            item.setOrigin("wzzw.la");
                            item.setName(booksE.get(i).getElementsByClass("col-xs-3").get(0).getElementsByTag("a").get(0).text());
                            item.setNoteUrl(TAG + booksE.get(i).getElementsByClass("col-xs-3").get(0).getElementsByTag("a").get(0).attr("href"));
                            item.setCoverUrl("noimage");
                            books.add(item);
                        }
                        e.onNext(books);
                    } else {
                        e.onNext(new ArrayList<SearchBookBean>());
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    e.onNext(new ArrayList<SearchBookBean>());
                }
                e.onComplete();
            }
        });
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public Observable<BookShelfBean> getBookInfo(final BookShelfBean bookShelfBean) {
        return getRetrofitObject(TAG).create(IGxwztvApi.class).getBookInfo(bookShelfBean.getNoteUrl().replace(TAG, "")).flatMap(new Function<String, ObservableSource<BookShelfBean>>() {
            @Override
            public ObservableSource<BookShelfBean> apply(String s) throws Exception {
                return analyBookInfo(s, bookShelfBean);
            }
        });
    }

    private Observable<BookShelfBean> analyBookInfo(final String s, final BookShelfBean bookShelfBean) {
        return Observable.create(new ObservableOnSubscribe<BookShelfBean>() {
            @Override
            public void subscribe(ObservableEmitter<BookShelfBean> e) throws Exception {
                bookShelfBean.setTag(TAG);
                bookShelfBean.setBookInfoBean(analyBookinfo(s, bookShelfBean.getNoteUrl()));
                e.onNext(bookShelfBean);
                e.onComplete();
            }
        });
    }

    private BookInfoBean analyBookinfo(String s, String novelUrl) {
        BookInfoBean bookInfoBean = new BookInfoBean();
        bookInfoBean.setNoteUrl(novelUrl);   //id
        bookInfoBean.setTag(TAG);
        Document doc = Jsoup.parse(s);
        Element resultE = doc.getElementsByClass("panel panel-warning").get(0);
        bookInfoBean.setCoverUrl(resultE.getElementsByClass("panel-body").get(0).getElementsByClass("img-thumbnail").get(0).attr("src"));
        bookInfoBean.setName(resultE.getElementsByClass("active").get(0).text());
        bookInfoBean.setAuthor(resultE.getElementsByClass("col-xs-12 list-group-item no-border").get(0).getElementsByTag("small").get(0).text());
        Element introduceE = resultE.getElementsByClass("panel panel-default mt20").get(0);
        String introduce = "";
        if (introduceE.getElementById("all") != null) {
            introduce = introduceE.getElementById("all").text().replace("[收起]", "");
        } else {
            introduce = Objects.requireNonNull(introduceE.getElementById("shot")).text();
        }
        bookInfoBean.setIntroduce("\u3000\u3000" + introduce);
        bookInfoBean.setChapterUrl(TAG + resultE.getElementsByClass("list-group-item tac").get(0).getElementsByTag("a").get(0).attr("href"));
        bookInfoBean.setOrigin("wzzw.la");

        String kind = resultE.getElementsByClass("col-xs-4 list-group-item no-border").get(0).getElementsByTag("a").get(0).text();
        String lastChapter = resultE.getElementsByClass("col-xs-12 list-group-item no-border").get(1).getElementsByTag("a").get(0).text();
//
        ObtainBookInfoImpl.getInstance().senMessageManpin(bookInfoBean, kind, lastChapter);

        return bookInfoBean;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void getChapterList(final BookShelfBean bookShelfBean, final OnGetChapterListListener getChapterListListener) {
        getRetrofitObject(TAG).create(IGxwztvApi.class).getChapterList(bookShelfBean.getBookInfoBean().getChapterUrl().replace(TAG, "")).flatMap(new Function<String, ObservableSource<WebChapterBean<BookShelfBean>>>() {
            @Override
            public ObservableSource<WebChapterBean<BookShelfBean>> apply(String s) throws Exception {
                return analyChapterList(s, bookShelfBean);
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<WebChapterBean<BookShelfBean>>() {
                    @Override
                    public void onNext(WebChapterBean<BookShelfBean> value) {
                        if (getChapterListListener != null) {
                            getChapterListListener.success(value.getData());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        if (getChapterListListener != null) {
                            getChapterListListener.error();
                        }
                    }
                });
    }

    private Observable<WebChapterBean<BookShelfBean>> analyChapterList(final String s, final BookShelfBean bookShelfBean) {
        return Observable.create(new ObservableOnSubscribe<WebChapterBean<BookShelfBean>>() {
            @Override
            public void subscribe(ObservableEmitter<WebChapterBean<BookShelfBean>> e) throws Exception {
                bookShelfBean.setTag(TAG);
                WebChapterBean<List<ChapterListBean>> temp = analyChapterlist(s, bookShelfBean.getNoteUrl());
                bookShelfBean.getBookInfoBean().setChapterlist(temp.getData());
                e.onNext(new WebChapterBean<BookShelfBean>(bookShelfBean, temp.getNext()));
                e.onComplete();
            }
        });
    }

    private WebChapterBean<List<ChapterListBean>> analyChapterlist(String s, String novelUrl) {
        Document doc = Jsoup.parse(s);
        Elements chapterlist = doc.getElementById("chapters-list").getElementsByTag("a");
        List<ChapterListBean> chapterBeans = new ArrayList<ChapterListBean>();
        for (int i = 0; i < chapterlist.size(); i++) {
            ChapterListBean temp = new ChapterListBean();
            temp.setDurChapterUrl(TAG + chapterlist.get(i).attr("href"));   //id
            temp.setDurChapterIndex(i);
            temp.setDurChapterName(chapterlist.get(i).text());
            temp.setNoteUrl(novelUrl);
            temp.setTag(TAG);

            chapterBeans.add(temp);
        }
        Boolean next = false;
        return new WebChapterBean<List<ChapterListBean>>(chapterBeans, next);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public Observable<BookContentBean> getBookContent(final String durChapterUrl, final int durChapterIndex) {
        return getRetrofitObject(TAG).create(IGxwztvApi.class).getBookContent(durChapterUrl.replace(TAG, "")).flatMap(new Function<String, ObservableSource<BookContentBean>>() {
            @Override
            public ObservableSource<BookContentBean> apply(String s) throws Exception {
                return analyBookContent(s, durChapterUrl, durChapterIndex);
            }
        });
    }

    private Observable<BookContentBean> analyBookContent(final String s, final String durChapterUrl, final int durChapterIndex) {
        return Observable.create(new ObservableOnSubscribe<BookContentBean>() {
            @Override
            public void subscribe(ObservableEmitter<BookContentBean> e) throws Exception {
                BookContentBean bookContentBean = new BookContentBean();
                bookContentBean.setDurChapterIndex(durChapterIndex);
                bookContentBean.setDurChapterUrl(durChapterUrl);
                bookContentBean.setTag(TAG);
                try {
                    Document doc = Jsoup.parse(s);
                    List<TextNode> contentEs = doc.getElementById("txtContent").textNodes();
                    StringBuilder content = new StringBuilder();
                    for (int i = 0; i < contentEs.size(); i++) {
                        String temp = contentEs.get(i).text().trim();
                        temp = temp.replaceAll(" ", "").replaceAll(" ", "");
                        if (temp.length() > 0) {
                            content.append("\u3000\u3000" + temp);
                            if (i < contentEs.size() - 1) {
                                content.append("\r\n");
                            }
                        }
                    }
                    bookContentBean.setDurCapterContent(content.toString());
                    bookContentBean.setRight(true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    ErrorAnalyContentManager.getInstance().writeNewErrorUrl(durChapterUrl);
                    bookContentBean.setDurCapterContent(durChapterUrl.substring(0, durChapterUrl.indexOf('/', 8)) + "站点暂时不支持解析，请反馈给Monke QQ:1105075896,半小时内解决，超级效率的程序员");
                    bookContentBean.setRight(false);
                }
                e.onNext(bookContentBean);
                e.onComplete();
            }
        });
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 获取分类书籍
     */
    @Override
    public Observable<List<SearchBookBean>> getKindBook(String url, int page) {
        url = url + page + ".htm";
        return getRetrofitObject(GxwztvBookModelImpl.TAG).create(IGxwztvApi.class).getKindBooks(url.replace(GxwztvBookModelImpl.TAG, "")).flatMap(new Function<String, ObservableSource<List<SearchBookBean>>>() {
            @Override
            public ObservableSource<List<SearchBookBean>> apply(String s) throws Exception {
                return analySearchBook(s);
            }
        });
    }
}
