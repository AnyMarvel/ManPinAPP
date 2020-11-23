
package com.mp.android.apps.monke.monkeybook.model.impl;


import com.google.android.apps.photolab.storyboard.download.MD5Utils;
import com.mp.android.apps.monke.monkeybook.base.MBaseModelImpl;


import com.mp.android.apps.monke.monkeybook.bean.SearchBookBean;
import com.mp.android.apps.monke.monkeybook.common.api.IWxguanAPI;
import com.mp.android.apps.monke.monkeybook.model.IReaderBookModel;
import com.mp.android.apps.monke.readActivity.bean.BookChapterBean;
import com.mp.android.apps.monke.readActivity.bean.ChapterInfoBean;
import com.mp.android.apps.monke.readActivity.bean.CollBookBean;

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

public class ContentWxguanModelImpl extends MBaseModelImpl implements IReaderBookModel {
    public static final String TAG = "https://www.wxguan.com";
    public static final String TAG_SEARCH = "https://so.biqusoso.com";
    public static final String ORIGIN = "wxguan.com";

    public static ContentWxguanModelImpl getInstance() {
        return new ContentWxguanModelImpl();
    }

    private ContentWxguanModelImpl() {

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public Observable<List<SearchBookBean>> searchBook(String content, int page) {
        return getRetrofitObject(TAG_SEARCH).create(IWxguanAPI.class).searchBook("xwxguan.com", content, "utf-8").flatMap(new Function<String, ObservableSource<List<SearchBookBean>>>() {
            @Override
            public ObservableSource<List<SearchBookBean>> apply(String s) throws Exception {
                return analySearchBook(s);
            }
        });
    }

    //todo 修改搜索后跳转详情问题
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
                            item.setKind("文学馆小说");
//                            item.setState();
                            item.setOrigin(ORIGIN);
                            item.setName(booksE.get(i).getElementsByClass("s2").get(0).getElementsByTag("a").get(0).text());
                            String href = booksE.get(i).getElementsByClass("s2").get(0).getElementsByTag("a").get(0).attr("href");
                            item.setNoteUrl(TAG + "/wenzhang/" + Integer.parseInt(href.substring(href.lastIndexOf("/") + 1)) / 2 + "/" + href.substring(href.lastIndexOf("/") + 1));
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


    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public Observable<CollBookBean> getBookInfo(CollBookBean collBookBean) {
        return getRetrofitObject(TAG).create(IWxguanAPI.class).getBookInfo(collBookBean.get_id().replace(TAG, "")).flatMap(new Function<String, ObservableSource<CollBookBean>>() {
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
                Element resultE = doc.getElementsByClass("book").get(0);

                collBookBean.setCover(TAG + resultE.getElementsByTag("img").get(0).attr("src"));

                collBookBean.setTitle(resultE.getElementsByTag("img").get(0).attr("alt"));
                String author = resultE.getElementsByClass("small").get(0).getElementsByTag("span").get(0).text().toString().trim();
                author = author.replace(" ", "").replace("  ", "").replace("作者：", "");
                collBookBean.setAuthor(author);
                String updatedTime = resultE.getElementsByClass("small").get(0).getElementsByTag("span").get(4).text().toString().trim();
                updatedTime = updatedTime.replace(" ", "").replace("  ", "").replace("更新时间：", "");

                collBookBean.setUpdated(updatedTime);

                collBookBean.setLastChapter(resultE.getElementsByClass("small").get(0).getElementsByTag("span").get(5).text().toString().trim());
                List<TextNode> contentEs = resultE.getElementsByClass("intro").get(0).textNodes();
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

                collBookBean.setShortIntro(content.toString());

                try {
                    String kind = resultE.getElementsByClass("small").get(0).getElementsByTag("span").get(1).text().replace("分类：", "");
                    String lastChapter = resultE.getElementsByClass("small").get(0).getElementsByTag("span").get(5).getElementsByTag("a").text();
//                    ObtainBookInfoImpl.getInstance().senMessageManpin(collBookBean, kind, lastChapter);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                e.onNext(collBookBean);
                e.onComplete();
            }
        });
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////    @Override
    public Single<List<BookChapterBean>> getBookChapters(String bookurl) {
        return getRetrofitObject(TAG).create(IWxguanAPI.class).getChapterLists(bookurl)
                .flatMap(new Function<String, Single<List<BookChapterBean>>>() {

                    @Override
                    public Single<List<BookChapterBean>> apply(String s) throws Exception {
                        return Single.create(new SingleOnSubscribe<List<BookChapterBean>>() {
                            @Override
                            public void subscribe(SingleEmitter<List<BookChapterBean>> emitter) throws Exception {
                                emitter.onSuccess(analyChapterlist(s, bookurl));
                            }
                        });
                    }
                });
    }

    private List<BookChapterBean> analyChapterlist(String s, String novelUrl) {
        Document doc = Jsoup.parse(s);
        Elements chapterlist = doc.getElementsByClass("listmain").get(0).getElementsByTag("dd");
        List<BookChapterBean> chapterBeans = new ArrayList<BookChapterBean>();
        for (int i = 11; i < chapterlist.size(); i++) {
            BookChapterBean temp = new BookChapterBean();
            String linkUrl = TAG + chapterlist.get(i).getElementsByTag("a").get(0).attr("href");
            temp.setId(MD5Utils.strToMd5By16(linkUrl));
            temp.setTitle(chapterlist.get(i).getElementsByTag("a").get(0).text());
            temp.setLink(linkUrl);   //id
            temp.setPosition(i - 11);
            temp.setBookId(novelUrl);
            temp.setUnreadble(false);
            chapterBeans.add(temp);
        }
        return chapterBeans;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    //==================================获取章节内容(具体的阅读内容)
    @Override
    public Single<ChapterInfoBean> getChapterInfo(String url) {
        return getRetrofitObject(TAG).create(IWxguanAPI.class).getChapterInfo(url).flatMap(new Function<String, SingleSource<? extends ChapterInfoBean>>() {
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

        }
        return chapterInfoBean;
    }

}
