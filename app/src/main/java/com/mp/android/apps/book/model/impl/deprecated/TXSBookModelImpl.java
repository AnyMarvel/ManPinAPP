
package com.mp.android.apps.book.model.impl.deprecated;


import com.mp.android.apps.utils.MD5Utils;
import com.mp.android.apps.book.base.MBaseModelImpl;
import com.mp.android.apps.book.bean.SearchBookBean;
import com.mp.android.apps.book.common.api.TXSAPI;
import com.mp.android.apps.book.model.IReaderBookModel;
import com.mp.android.apps.readActivity.bean.BookChapterBean;
import com.mp.android.apps.readActivity.bean.ChapterInfoBean;
import com.mp.android.apps.readActivity.bean.CollBookBean;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
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

@Deprecated
public class TXSBookModelImpl extends MBaseModelImpl implements IReaderBookModel {
    public static final String TAG = "https://www.9txs.org";
    public static final String SEARCH_TAG = "https://so.9txs.org";

    public static final String ORIGIN = "9txs.com";

    public static TXSBookModelImpl getInstance() {
        return new TXSBookModelImpl();
    }

    private TXSBookModelImpl() {

    }



    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public Observable<List<SearchBookBean>> searchBook(String content, int page) {

        try {
            Map<String, String> requestDataMap = new HashMap<>();
            requestDataMap.put("searchkey", content);
            return getRetrofitObject(SEARCH_TAG).create(TXSAPI.class).searchBook(generateFormRequestBody(requestDataMap)).flatMap(new Function<String, ObservableSource<List<SearchBookBean>>>() {
                @Override
                public ObservableSource<List<SearchBookBean>> apply(String s) throws Exception {
                    return analySearchBook(s);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public Observable<List<SearchBookBean>> analySearchBook(final String s) {
        return Observable.create(new ObservableOnSubscribe<List<SearchBookBean>>() {
            @Override
            public void subscribe(ObservableEmitter<List<SearchBookBean>> e) throws Exception {
                try {
                    Document doc = Jsoup.parse(s);
                    Elements booksE = doc.getElementsByClass("library").get(0).getElementsByTag("li");
                    if (null != booksE && booksE.size() > 1) {
                        List<SearchBookBean> books = new ArrayList<SearchBookBean>();
                        for (int i = 0; i < booksE.size(); i++) {
                            SearchBookBean item = new SearchBookBean();
                            item.setTag(TAG);
                            item.setAuthor(booksE.get(i).getElementsByClass("author").get(0).text());
                            item.setKind("九桃小说");
                            item.setOrigin(ORIGIN);
                            item.setName(booksE.get(i).getElementsByClass("bookname").get(0).text());
                            String href = booksE.get(i).getElementsByClass("bookname").get(0).attr("href");
                            item.setNoteUrl(href);
                            item.setCoverUrl(booksE.get(i).getElementsByClass("bookimg").get(0).getElementsByTag("img").get(0).attr("src"));
                            item.setKind(booksE.get(i).getElementsByTag("p").get(0).getElementsByTag("a").get(1).text());
                            item.setLastChapter(booksE.get(i).getElementsByClass("chapter").get(0).text().replace("最新章节：", ""));
                            String contentEs = booksE.get(i).getElementsByClass("intro").get(0).text();
                            item.setDesc(contentEs);

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
        return getRetrofitObject(TAG).create(TXSAPI.class).getBookInfo(collBookBean.get_id().replace(TAG, "")).flatMap(new Function<String, ObservableSource<CollBookBean>>() {
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
                Element resultE = doc.getElementsByClass("detail").get(0);

                collBookBean.setCover(resultE.getElementsByClass("bookimg").get(0).getElementsByTag("img").get(0).attr("src"));

                collBookBean.setTitle(resultE.getElementsByTag("h1").get(0).text());

                String author = resultE.getElementsByTag("p").get(0).getElementsByTag("a").get(0).text().toString().trim();
                author = author.replace(" ", "").replace("  ", "").replace("作者：", "");
                collBookBean.setAuthor(author);

                String updatedTime = resultE.getElementsByTag("p").get(3).getElementsByTag("span").get(0).text().toString().trim();
                updatedTime = updatedTime.replace("(", "").replace(")", "");
                collBookBean.setUpdated(updatedTime);

                String lastChapter = resultE.getElementsByTag("p").get(3).getElementsByTag("a").text().toString().trim();
                collBookBean.setLastChapter(lastChapter);

                collBookBean.setBookChapterUrl(TAG + doc.getElementsByClass("more").get(0).attr("href"));


                List<TextNode> contentEs = doc.getElementsByClass("intro").get(0).textNodes();
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
                e.onNext(collBookBean);
                e.onComplete();
            }
        });
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////    @Override
    public Single<List<BookChapterBean>> getBookChapters(CollBookBean collBookBean) {
        return getRetrofitObject(TAG).create(TXSAPI.class).getChapterLists(collBookBean.getBookChapterUrl())
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
        Elements chapterlist = doc.getElementsByClass("read").get(0).getElementsByTag("dd");
        List<BookChapterBean> chapterBeans = new ArrayList<BookChapterBean>();
        for (int i = 0; i < chapterlist.size(); i++) {
            BookChapterBean temp = new BookChapterBean();
            String linkUrl = TAG + chapterlist.get(i).getElementsByTag("a").get(0).attr("href");
            temp.setId(MD5Utils.strToMd5By16(linkUrl));
            temp.setTitle(chapterlist.get(i).getElementsByTag("a").get(0).text());
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
        return getRetrofitObject(TAG).create(TXSAPI.class).getChapterInfo(url)
                .flatMap(new Function<String, SingleSource<? extends ChapterInfoBean>>() {
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

    @Override
    public String getTAG() {
        return TAG;
    }

    private ChapterInfoBean analysisChapterInfo(String s, String url) {
        ChapterInfoBean chapterInfoBean = new ChapterInfoBean();
        try {
            Document doc = Jsoup.parse(s);
            Elements contentEs = doc.getElementById("content").getElementsByTag("p");
            StringBuilder content = new StringBuilder();
            for (int i = 0; i < contentEs.size(); i++) {
                String temp = contentEs.get(i).text();
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
