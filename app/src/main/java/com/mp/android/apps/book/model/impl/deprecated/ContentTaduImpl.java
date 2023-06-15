
package com.mp.android.apps.book.model.impl.deprecated;


import com.mp.android.apps.utils.MD5Utils;
import com.mp.android.apps.book.base.MBaseModelImpl;
import com.mp.android.apps.book.bean.SearchBookBean;
import com.mp.android.apps.book.common.api.ITaduAPI;
import com.mp.android.apps.book.model.IReaderBookModel;
import com.mp.android.apps.readActivity.bean.BookChapterBean;
import com.mp.android.apps.readActivity.bean.ChapterInfoBean;
import com.mp.android.apps.readActivity.bean.CollBookBean;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
 * 塔读小说
 */
@Deprecated
public class ContentTaduImpl extends MBaseModelImpl implements IReaderBookModel {
    public static final String TAG = "https://www.tadu.com";
    public static final String ORIGIN = "tadu.com";

    public static ContentTaduImpl getInstance() {
        return new ContentTaduImpl();
    }

    private ContentTaduImpl() {

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public Observable<List<SearchBookBean>> searchBook(String content, int page) {
        Map<String, String> requestDataMap = new HashMap<>();
        requestDataMap.put("query", content);

        return getRetrofitObject(TAG).create(ITaduAPI.class).searchBook(requestDataMap).flatMap(new Function<String, ObservableSource<List<SearchBookBean>>>() {
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
                    Elements booksE = doc.getElementsByClass("bookList").get(0).getElementsByTag("li");
                    if (null != booksE && booksE.size() > 1) {
                        List<SearchBookBean> books = new ArrayList<SearchBookBean>();
                        for (int i = 1; i < booksE.size(); i++) {
                            SearchBookBean item = new SearchBookBean();
                            item.setTag(TAG);
                            item.setAuthor(booksE.get(i).getElementsByClass("authorNm").get(0).text());
                            item.setKind("塔读文学");
//                            item.setState();
                            item.setOrigin(ORIGIN);

                            String bookName = SplicingBookName(booksE.get(i).getElementsByClass("bookNm"));
                            item.setName(bookName);
                            item.setNoteUrl(TAG + booksE.get(i).getElementsByClass("bookImg").get(0).attr("href"));
                            item.setCoverUrl(booksE.get(i).getElementsByClass("bookImg").get(0).getElementsByTag("img").attr("data-src"));
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

    private String SplicingBookName(List<Element> elements) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Element element : elements) {
            String temp = element.text().trim();
            stringBuilder.append(temp);
        }

        return stringBuilder.toString();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public Observable<CollBookBean> getBookInfo(CollBookBean collBookBean) {
        return getRetrofitObject(TAG).create(ITaduAPI.class).getBookInfo(collBookBean.get_id().replace(TAG, "")).flatMap(new Function<String, ObservableSource<CollBookBean>>() {
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

                Element bookInfo = doc.getElementsByClass("bookIntro").get(0);
                collBookBean.setCover(bookInfo.getElementsByClass("bookImg").get(0).getElementsByTag("img").attr("src"));

                collBookBean.setTitle(bookInfo.getElementsByClass("bkNm").get(0).text());
                String author = bookInfo.getElementsByClass("bookNm").get(0).getElementsByTag("span").text().toString().trim();
                author = author.replace(" ", "").replace("  ", "").replace("\"", "").replace("著", "");
                collBookBean.setAuthor(author);

                String updatedTime = "获取失败";
                String lastchapter = "获取失败";
                try {
                    updatedTime = doc.getElementsByClass("upDate").get(0).getElementsByTag("span").get(0).text().trim().replace(" ", "").replace("  ", "");
                    lastchapter = doc.getElementsByClass("upDate").get(0).getElementsByClass("chapter").get(0).text().trim().replace(" ", "").replace("  ", "");
                } catch (Exception e1) {
                }

                collBookBean.setUpdated(updatedTime);

                collBookBean.setLastChapter(lastchapter);

                String contentEs = doc.getElementsByClass("lfO").get(0).getElementsByClass("intro").get(0).text();

                collBookBean.setShortIntro(contentEs);
                collBookBean.setBookChapterUrl(collBookBean.get_id());
                e.onNext(collBookBean);
                e.onComplete();
            }
        });
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////    @Override
    public Single<List<BookChapterBean>> getBookChapters(CollBookBean collBookBean) {
        String chapterUrl = collBookBean.getBookChapterUrl().replace("book", "book/catalogue");

        return getRetrofitObject(TAG).create(ITaduAPI.class).getChapterLists(chapterUrl)
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
        Document doc = Jsoup.parse(s);
        Elements chapterlist = doc.getElementsByClass("chapter").get(0).getElementsByTag("a");
        List<BookChapterBean> chapterBeans = new ArrayList<BookChapterBean>();
        for (int i = 0; i < chapterlist.size(); i++) {
            BookChapterBean temp = new BookChapterBean();
            String linkUrl = TAG + chapterlist.get(i).attr("href").trim();
            temp.setId(MD5Utils.strToMd5By16(linkUrl));
            temp.setTitle(chapterlist.get(i).text());
            temp.setLink(linkUrl);   //id
            temp.setPosition(i);
            temp.setBookId(collBookBean.get_id());
            temp.setUnreadble(false);
            chapterBeans.add(temp);
        }
        return chapterBeans;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    //==================================获取章节内容(具体的阅读内容)
    @Override
    public Single<ChapterInfoBean> getChapterInfo(String url) {


        return getRetrofitObject(TAG).create(ITaduAPI.class).getChapterInfo(url)
                .map(new Function<String, String>() {
                    @Override
                    public String apply(String s) throws Exception {
                        Document doc = Jsoup.parse(s);
                        String realUrl = doc.getElementById("bookPartResourceUrl").attr("value");
                        return realUrl;
                    }
                }).flatMap(new Function<String, SingleSource<? extends String>>() {
                    @Override
                    public SingleSource<? extends String> apply(String s) throws Exception {
                        return getRetrofitObject(TAG).create(ITaduAPI.class).getChapterInfo(s);
                    }
                }).flatMap(new Function<String, SingleSource<? extends ChapterInfoBean>>() {
                    @Override
                    public SingleSource<? extends ChapterInfoBean> apply(String s1) throws Exception {
                        return Single.create(new SingleOnSubscribe<ChapterInfoBean>() {
                            @Override
                            public void subscribe(SingleEmitter<ChapterInfoBean> emitter) throws Exception {
                                emitter.onSuccess(analysisChapterInfo(s1));
                            }
                        });
                    }
                });
    }

    @Override
    public String getTAG() {
        return TAG;
    }

    private ChapterInfoBean analysisChapterInfo(String s) {
        ChapterInfoBean chapterInfoBean = new ChapterInfoBean();
        try {
            String domXML = s.replace("callback({content:'", "<xml>")
                    .replace("'})", "</xml>");
            Document doc = Jsoup.parse(domXML);
            List<Element> contentEs = doc.getElementsByTag("p");
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
