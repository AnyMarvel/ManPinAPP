
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
import com.mp.android.apps.monke.monkeybook.common.api.ILingdiankanshuApi;
import com.mp.android.apps.monke.monkeybook.listener.OnGetChapterListListener;
import com.mp.android.apps.monke.monkeybook.model.IDynamicBookModel;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;


import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;


import io.reactivex.android.schedulers.AndroidSchedulers;

import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class LingdiankanshuStationBookModelImpl extends MBaseModelImpl implements IDynamicBookModel {
    public static final String TAG = "https://www.lingdiankanshu.co";
    public static final String SEARCH_URL = "https://sou.xanbhx.com";
    public static final String TAG_SEARCH = "lingdiankanshuco";

    public static LingdiankanshuStationBookModelImpl getInstance() {
        return new LingdiankanshuStationBookModelImpl();
    }

    @Override
    public Observable<List<SearchBookBean>> searchBook(String content, int page) {
        return getRetrofitObject(SEARCH_URL).create(ILingdiankanshuApi.class).searchBook(content, TAG_SEARCH, "920895234054625192").flatMap(new Function<String, ObservableSource<List<SearchBookBean>>>() {
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
                    Elements booksE = doc.getElementsByClass("search-list").get(0).getElementsByTag("li");
                    if (null != booksE && booksE.size() > 1) {
                        List<SearchBookBean> books = new ArrayList<SearchBookBean>();
                        for (int i = 1; i < booksE.size(); i++) {
                            SearchBookBean item = new SearchBookBean();
                            item.setTag(TAG);
                            item.setAuthor(booksE.get(i).getElementsByClass("s4").get(0).text());
                            item.setKind(booksE.get(i).getElementsByClass("s1").get(0).text());
//                            item.setState();
                            item.setLastChapter(booksE.get(i).getElementsByClass("s3").get(0).getElementsByTag("a").get(0).text());
                            item.setOrigin("lingdiankanshu.co");
                            item.setName(booksE.get(i).getElementsByClass("s2").get(0).getElementsByTag("a").get(0).text());
                            item.setNoteUrl(booksE.get(i).getElementsByClass("s2").get(0).getElementsByTag("a").get(0).attr("href"));
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

    //    获取图书内容详情,用于图书悬浮窗内容展示,BookDetailActivity调用
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public Observable<BookShelfBean> getBookInfo(final BookShelfBean bookShelfBean) {
        return getRetrofitObject(TAG).create(ILingdiankanshuApi.class).getBookInfo(bookShelfBean.getNoteUrl().replace(TAG, "")).flatMap(new Function<String, ObservableSource<BookShelfBean>>() {
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
        Element resultE = doc.getElementsByClass("box_con").get(0);

        bookInfoBean.setCoverUrl(TAG + resultE.getElementById("fmimg").getElementsByTag("img").get(0).attr("src"));

        bookInfoBean.setName(resultE.getElementById("info").getElementsByTag("h1").get(0).text());
        String author = resultE.getElementById("info").getElementsByTag("p").get(0).text().toString().trim();
        author = author.replace(" ", "").replace("  ", "").replace("作者：", "");
        bookInfoBean.setAuthor(author);

        List<TextNode> contentEs = resultE.getElementById("intro").textNodes();
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

        bookInfoBean.setIntroduce(content.toString());
        bookInfoBean.setChapterUrl(novelUrl);
        bookInfoBean.setOrigin("lingdiankanshu.co");

        try {
            String lastChapter = resultE.getElementById("info").getElementsByTag("p").get(3).getElementsByTag("a").get(0).text();
//            ObtainBookInfoImpl.getInstance().senMessageManpin(bookInfoBean, "", lastChapter);
        } catch (Exception e) {

        }
        return bookInfoBean;
    }

    //获得图书章节目录
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void getChapterList(final BookShelfBean bookShelfBean, final OnGetChapterListListener getChapterListListener) {
        getRetrofitObject(TAG).create(ILingdiankanshuApi.class).getChapterList(bookShelfBean.getBookInfoBean().getChapterUrl().replace(TAG, "")).flatMap(new Function<String, ObservableSource<WebChapterBean<BookShelfBean>>>() {
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
        Elements chapterlist = doc.getElementById("list").getElementsByTag("dd");
        List<ChapterListBean> chapterBeans = new ArrayList<ChapterListBean>();
        for (int i = 0; i < chapterlist.size(); i++) {
            ChapterListBean temp = new ChapterListBean();
            temp.setDurChapterUrl(novelUrl + chapterlist.get(i).getElementsByTag("a").get(0).attr("href"));   //id
            temp.setDurChapterIndex(i);
            temp.setDurChapterName(chapterlist.get(i).getElementsByTag("a").get(0).text());
            temp.setNoteUrl(novelUrl);
            temp.setTag(TAG);

            chapterBeans.add(temp);
        }
        Boolean next = false;
        return new WebChapterBean<List<ChapterListBean>>(chapterBeans, next);
    }

    //获得图书内容
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public Observable<BookContentBean> getBookContent(final String durChapterUrl, final int durChapterIndex) {
        return getRetrofitObject(TAG).create(ILingdiankanshuApi.class).getBookContent(durChapterUrl.replace(TAG, "")).flatMap(new Function<String, ObservableSource<BookContentBean>>() {
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
                    List<TextNode> contentEs = doc.getElementById("content").textNodes();
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

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Library展示导航索引页数据,获取
    @Override
    public LinkedHashMap<String, String> getBookNavs() {
        LinkedHashMap<String, String> linkedHashMap = new LinkedHashMap<>();
        linkedHashMap.put("东方玄幻", "https://www.lingdiankanshu.co/class/1_1.html");
        linkedHashMap.put("西方奇幻", "https://www.lingdiankanshu.co/class/1_1.html");
        linkedHashMap.put("热血修真", "https://www.lingdiankanshu.co/class/2_1.html");
        linkedHashMap.put("武侠仙侠", "https://www.lingdiankanshu.co/class/2_1.html");
        linkedHashMap.put("都市爽文", "https://www.lingdiankanshu.co/class/3_1.html");
        linkedHashMap.put("言情暧昧", "https://www.lingdiankanshu.co/class/3_1.html");
        linkedHashMap.put("灵异悬疑", "https://www.lingdiankanshu.co/class/5_1.html");
        linkedHashMap.put("运动竞技", "https://www.lingdiankanshu.co/class/6_1.html");
        linkedHashMap.put("历史架空", "https://www.lingdiankanshu.co/class/4_1.html");
        linkedHashMap.put("科幻迷航", "https://www.lingdiankanshu.co/class/5_1.html");
        linkedHashMap.put("游戏人生", "https://www.lingdiankanshu.co/class/6_1.html");
        linkedHashMap.put("军事斗争", "https://www.lingdiankanshu.co/class/4_1.html");
        linkedHashMap.put("校园爱情", "https://www.lingdiankanshu.co/class/3_1.html");
        linkedHashMap.put("其他", "https://www.lingdiankanshu.co/class/1_1.html");
        return linkedHashMap;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //实现导航功能,实现导航页可动态获取
    @Override
    public Observable<List<SearchBookBean>> getKindBook(String url, int page) {
        return getRetrofitObject(LingdiankanshuStationBookModelImpl.TAG).create(ILingdiankanshuApi.class).getKindBooks(url.replace(LingdiankanshuStationBookModelImpl.TAG, "")).flatMap(new Function<String, ObservableSource<List<SearchBookBean>>>() {
            @Override
            public ObservableSource<List<SearchBookBean>> apply(String s) throws Exception {
                if (page > 1) {
                    return analyKindBook("");
                } else {
                    return analyKindBook(s);
                }

            }
        });
    }

    public Observable<List<SearchBookBean>> analyKindBook(final String s) {
        return Observable.create(new ObservableOnSubscribe<List<SearchBookBean>>() {
            @Override
            public void subscribe(ObservableEmitter<List<SearchBookBean>> e) throws Exception {
                try {
                    Document doc = Jsoup.parse(s);
                    Elements booksE = doc.getElementById("main").getElementById("newscontent").getElementsByTag("ul").get(0).getElementsByTag("li");
                    if (null != booksE && booksE.size() > 1) {
                        List<SearchBookBean> books = new ArrayList<SearchBookBean>();
                        for (int i = 1; i < booksE.size(); i++) {
                            SearchBookBean item = new SearchBookBean();
                            item.setTag(TAG);
                            item.setAuthor(booksE.get(i).getElementsByClass("s4").get(0).text());
                            item.setKind(booksE.get(i).getElementsByClass("s1").get(0).text());
//                            item.setState();
                            item.setLastChapter(booksE.get(i).getElementsByClass("s3").get(0).getElementsByTag("a").get(0).text());
                            item.setOrigin("lingdiankanshu.co");
                            item.setName(booksE.get(i).getElementsByClass("s2").get(0).getElementsByTag("a").get(0).text());
                            item.setNoteUrl(booksE.get(i).getElementsByClass("s2").get(0).getElementsByTag("a").get(0).attr("href"));
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


}
