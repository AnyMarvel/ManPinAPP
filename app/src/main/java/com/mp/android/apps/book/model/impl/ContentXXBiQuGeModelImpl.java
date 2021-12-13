package com.mp.android.apps.book.model.impl;


import android.text.TextUtils;

import com.google.android.apps.photolab.storyboard.download.MD5Utils;
import com.mp.android.apps.book.base.MBaseModelImpl;
import com.mp.android.apps.book.bean.SearchBookBean;
import com.mp.android.apps.book.common.api.IBiQuGeAPI;
import com.mp.android.apps.book.model.IReaderBookModel;
import com.mp.android.apps.book.model.ObtainBookInfoUtils;
import com.mp.android.apps.readActivity.bean.BookChapterBean;
import com.mp.android.apps.readActivity.bean.ChapterInfoBean;
import com.mp.android.apps.readActivity.bean.CollBookBean;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;

/**
 * @author ShuiYu
 * Created on 12/13/21
 * Copyright © 2021 Alibaba-inc. All rights reserved.
 */

public class ContentXXBiQuGeModelImpl extends MBaseModelImpl implements IReaderBookModel {
    public static final String TAG = "https://www.xxbiqudu.com";

    private ContentXXBiQuGeModelImpl() {
    }
    public static ContentXXBiQuGeModelImpl getInstance() {
        return new ContentXXBiQuGeModelImpl();
    }

    @Override
    public Observable<List<SearchBookBean>> searchBook(String content, int page) {
        return getRetrofitObject(TAG).create(IBiQuGeAPI.class).searchBook(content).flatMap(new Function<String, ObservableSource<List<SearchBookBean>>>() {
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
                    Elements booksE = doc.getElementsByClass("grid").get(0).getElementsByTag("tr");
                    if (null != booksE && booksE.size() > 1) {
                        List<SearchBookBean> books = new ArrayList<SearchBookBean>();
                        for (int i = 1; i < booksE.size(); i++) {
                            SearchBookBean item = new SearchBookBean();
                            item.setTag(TAG);
                            item.setAuthor(booksE.get(i).getElementsByClass("odd").get(1).text());
//                            item.setKind(booksE.get(i).getElementsByClass("s2").get(0).text());
                            item.setLastChapter(booksE.get(i).getElementsByClass("even").get(0).getElementsByTag("a").get(0).text());
                            item.setOrigin("xxbiqudu.com");
                            item.setName(booksE.get(i).getElementsByClass("odd").get(0).getElementsByTag("a").get(0).text());
                            item.setNoteUrl(booksE.get(i).getElementsByClass("odd").get(0).getElementsByTag("a").get(0).attr("href"));
                            item.setCoverUrl("noimage");
                            item.setUpdated(booksE.get(i).getElementsByClass("odd").get(2).text());
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

    @Override
    public Observable<CollBookBean> getBookInfo(CollBookBean collBookBean) {
        return getRetrofitObject(TAG).create(IBiQuGeAPI.class).getBookInfo(collBookBean.get_id().replace(TAG, "")).flatMap(new Function<String, ObservableSource<CollBookBean>>() {
            @Override
            public ObservableSource<CollBookBean> apply(String s) throws Exception {
                return analyBookInfo(s, collBookBean);
            }
        });

    }
    private Observable<CollBookBean> analyBookInfo(final String s, final CollBookBean collBookBean) {
        return Observable.create(new ObservableOnSubscribe<CollBookBean>() {
            @Override
            public void subscribe(ObservableEmitter<CollBookBean> e) throws Exception {
                collBookBean.setBookTag(TAG);
                Document doc = Jsoup.parse(s);
                Element resultE = doc.getElementsByClass("box_con").get(0);
                collBookBean.set_id(collBookBean.get_id());
                collBookBean.setCover(resultE.getElementById("fmimg").getElementsByTag("img").get(0).attr("src"));

                collBookBean.setTitle(resultE.getElementById("info").getElementsByTag("h1").get(0).text());
                String author = resultE.getElementById("info").getElementsByTag("p").get(0).text().toString().trim();
                author = author.replace(" ", "").replace("  ", "").replace("作者：", "");
                collBookBean.setAuthor(author);

                List<TextNode> contentEs =new ArrayList<>();
                Elements contentElement=resultE.getElementById("intro").getElementsByTag("p");
                for (int i = 0; i < contentElement.size(); i++) {
                    contentEs.addAll(contentElement.get(i).textNodes());
                }
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
                collBookBean.setUpdated(resultE.getElementById("info").getElementsByTag("p").get(2).text().toString().trim());
                collBookBean.setShortIntro(content.toString());
                collBookBean.setBookChapterUrl(collBookBean.get_id());
                String lastChapter=collBookBean.getLastChapter();
                if (TextUtils.isEmpty(lastChapter)){
                    collBookBean.setLastChapter("暂无");
                }

                try {
                    ObtainBookInfoUtils.getInstance().senMessageManpin(collBookBean, "", lastChapter);
                } catch (Exception e1) {

                }

                e.onNext(collBookBean);
                e.onComplete();
            }
        });
    }

    @Override
    public Single<List<BookChapterBean>> getBookChapters(CollBookBean collBookBean) {
        return getRetrofitObject(TAG).create(IBiQuGeAPI.class).getChapterLists(collBookBean.getBookChapterUrl())
                .flatMap(new Function<String, Single<List<BookChapterBean>>>() {

                    @Override
                    public Single<List<BookChapterBean>> apply(String s) throws Exception {
                        return Single.create(new SingleOnSubscribe<List<BookChapterBean>>() {
                            @Override
                            public void subscribe(SingleEmitter<List<BookChapterBean>> emitter) throws Exception {
                                emitter.onSuccess(analyChapterlist(s, collBookBean));
                            }
                        });
                    }
                });

    }
    private List<BookChapterBean> analyChapterlist(String s, CollBookBean collBookBean) {
        List<BookChapterBean> chapterBeans = new ArrayList<BookChapterBean>();
        Document doc = Jsoup.parse(s);
        Elements chapterlist = doc.getElementById("list").getElementsByTag("dd");

        for (int i = 0; i < chapterlist.size(); i++) {
            BookChapterBean temp = new BookChapterBean();
            String linkUrl = chapterlist.get(i).getElementsByTag("a").get(0).attr("href");
            temp.setId(MD5Utils.strToMd5By16(linkUrl));
            temp.setTitle(chapterlist.get(i).getElementsByTag("a").get(0).text());
            temp.setPosition(i);
            temp.setLink(linkUrl);
            temp.setBookId(collBookBean.get_id());
            temp.setUnreadble(false);
            chapterBeans.add(temp);
        }

        return chapterBeans;


    }

    @Override
    public Single<ChapterInfoBean> getChapterInfo(String url) {
        return getRetrofitObject(TAG).create(IBiQuGeAPI.class).getChapterInfo(url).flatMap(new Function<String, SingleSource<? extends ChapterInfoBean>>() {
            @Override
            public SingleSource<? extends ChapterInfoBean> apply(String s) throws Exception {
                return Single.create(new SingleOnSubscribe<ChapterInfoBean>() {
                    @Override
                    public void subscribe(SingleEmitter<ChapterInfoBean> emitter) throws Exception {
                        emitter.onSuccess(analysisChapterInfo(s, url));
                    }
                });
            }
        });

    }
    private ChapterInfoBean analysisChapterInfo(String s, String url) {
        ChapterInfoBean chapterInfoBean = new ChapterInfoBean();

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
            chapterInfoBean.setBody(content.toString());

        } catch (Exception ex) {
            ex.printStackTrace();
            chapterInfoBean.setBody("站点暂时不支持解析，请反馈给Monke QQ:314599558,半小时内解决，超级效率的程序员");
        }
        return chapterInfoBean;
    }

    @Override
    public String getTAG() {
        return TAG;
    }
}
