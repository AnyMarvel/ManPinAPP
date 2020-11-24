
package com.mp.android.apps.monke.monkeybook.model.impl;

import android.text.TextUtils;

import com.google.android.apps.photolab.storyboard.download.MD5Utils;
import com.mp.android.apps.monke.monkeybook.ErrorAnalyContentManager;
import com.mp.android.apps.monke.monkeybook.base.MBaseModelImpl;
import com.mp.android.apps.monke.monkeybook.bean.BookContentBean;
import com.mp.android.apps.monke.monkeybook.bean.ChapterListBean;
import com.mp.android.apps.monke.monkeybook.bean.SearchBookBean;
import com.mp.android.apps.monke.monkeybook.bean.WebChapterBean;
import com.mp.android.apps.monke.monkeybook.common.api.IGxwztvApi;
import com.mp.android.apps.monke.monkeybook.common.api.ILingdiankanshuApi;
import com.mp.android.apps.monke.monkeybook.common.api.IWxguanAPI;
import com.mp.android.apps.monke.monkeybook.listener.OnGetChapterListListener;
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
import java.util.LinkedHashMap;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class ReaderLingdiankanshuStationBookModelImpl extends MBaseModelImpl implements IReaderBookModel {
    public static final String TAG = "https://www.lingdiankanshu.co";

    public static ReaderLingdiankanshuStationBookModelImpl getInstance() {
        return new ReaderLingdiankanshuStationBookModelImpl();
    }

    @Override
    public Observable<List<SearchBookBean>> searchBook(String content, int page) {
        return getRetrofitObject(TAG).create(ILingdiankanshuApi.class).searchBook(content).flatMap(new Function<String, ObservableSource<List<SearchBookBean>>>() {
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
                    Elements booksE = doc.getElementsByClass("txt-list txt-list-row5").get(0).getElementsByTag("li");
                    if (null != booksE && booksE.size() > 1) {
                        List<SearchBookBean> books = new ArrayList<SearchBookBean>();
                        for (int i = 1; i < booksE.size(); i++) {
                            SearchBookBean item = new SearchBookBean();
                            item.setTag(TAG);
                            item.setAuthor(booksE.get(i).getElementsByClass("s4").get(0).text());
                            item.setKind(booksE.get(i).getElementsByClass("s1").get(0).text());
                            item.setLastChapter(booksE.get(i).getElementsByClass("s3").get(0).getElementsByTag("a").get(0).text());
                            item.setOrigin("lingdiankanshu.co");
                            item.setName(booksE.get(i).getElementsByClass("s2").get(0).getElementsByTag("a").get(0).text());
                            item.setNoteUrl(TAG + booksE.get(i).getElementsByClass("s2").get(0).getElementsByTag("a").get(0).attr("href"));
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
    public Observable<CollBookBean> getBookInfo(CollBookBean collBookBean) {
        return getRetrofitObject(TAG).create(ILingdiankanshuApi.class).getBookInfo(collBookBean.get_id().replace(TAG, ""))
                .flatMap(new Function<String, ObservableSource<CollBookBean>>() {
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

                Document doc = Jsoup.parse(s);
                Element resultE = doc.getElementsByClass("row row-detail").get(0);
                collBookBean.setBookTag(TAG);
                collBookBean.setCover(TAG + resultE.getElementById("imgbox").getElementsByTag("img").get(0).attr("src"));
                Element bookInfo = resultE.getElementsByClass("info").get(0);
                collBookBean.setTitle(bookInfo.getElementsByClass("top").get(0).getElementsByTag("h1").get(0).text());
                String author = bookInfo.getElementsByClass("fix").get(0).getElementsByTag("p").get(0).text().toString().trim();
                author = author.replace(" ", "").replace("  ", "").replace("作者：", "");
                collBookBean.setAuthor(author);
                String updatedTime = bookInfo.getElementsByClass("fix").get(0).getElementsByTag("p").get(0).text().toString().trim()
                        .replace(" ", "").replace("  ", "").replace("作者：", "更新：");
                collBookBean.setUpdated(updatedTime);

                List<TextNode> contentEs = resultE.getElementsByClass("m-desc xs-show").get(0).textNodes();
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
                collBookBean.set_id(collBookBean.get_id());
                String lastChapter = resultE.getElementById("info").getElementsByTag("p").get(3).getElementsByTag("a").get(0).text();
                collBookBean.setLastChapter(lastChapter);
                e.onNext(collBookBean);
                e.onComplete();
            }
        });
    }


    //获得图书章节目录
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////


    @Override
    public Single<List<BookChapterBean>> getBookChapters(CollBookBean collBookBean) {

        return getRetrofitObject(TAG).create(ILingdiankanshuApi.class).getCharterPageUrls(collBookBean.getBookChapterUrl())
                .flatMap(new Function<String, ObservableSource<List<String>>>() {
                    @Override
                    public ObservableSource<List<String>> apply(@NonNull String s) throws Exception {
                        return Observable.create(new ObservableOnSubscribe<List<String>>() {
                            @Override
                            public void subscribe(@NonNull ObservableEmitter<List<String>> emitter) throws Exception {
                                Document doc = Jsoup.parse(s);
                                Elements chapterlist = doc.getElementsByClass("middle").get(0).getElementsByTag("option");
                                List<String> charterPageUrl = new ArrayList<>();
                                for (Element element : chapterlist) {
                                    if (!TextUtils.isEmpty(element.attr("value"))) {
                                        charterPageUrl.add(TAG + element.attr("value"));
                                    }
                                }
                                emitter.onNext(charterPageUrl);
                            }
                        });
                    }
                }).flatMap(new Function<List<String>, ObservableSource<List<BookChapterBean>>>() {
                    @Override
                    public ObservableSource<List<BookChapterBean>> apply(@NonNull List<String> strings) throws Exception {
                        for (String string : strings) {
                            getRetrofitObject(TAG).create(ILingdiankanshuApi.class).getCharterPageUrls(string).map(new Function<String, Object>() {
                                @Override
                                public Object apply(@NonNull String s) throws Exception {
                                    return analyChapterlist(s, TAG);
                                }
                            }

                            );

                        }


                        return Observable.create(new ObservableOnSubscribe<List<BookChapterBean>>() {
                            @Override
                            public void subscribe(@NonNull ObservableEmitter<List<BookChapterBean>> emitter) throws Exception {
                                for (String string : strings) {

                                }
                            }
                        });
                    }
                }).toSortedList().flatMap(new Function<List<List<BookChapterBean>>, SingleSource<? extends List<BookChapterBean>>>() {
                    @Override
                    public SingleSource<? extends List<BookChapterBean>> apply(@NonNull List<List<BookChapterBean>> lists) throws Exception {
                        return null;
                    }
                });



    }


    //todo 待适配的零点看书分页章节
    private List<BookChapterBean> analyChapterlist(String s, String novelUrl) {
        List<BookChapterBean> chapterBeans = new ArrayList<BookChapterBean>();
        Document doc = Jsoup.parse(s);
        Elements chapterlist = doc.getElementById("list").getElementsByTag("dd");

        for (int i = 0; i < chapterlist.size(); i++) {
            BookChapterBean temp = new BookChapterBean();
            String linkUrl = novelUrl + chapterlist.get(i).getElementsByTag("a").get(0).attr("href");
            temp.setLink(linkUrl);   //id
            temp.setId(MD5Utils.strToMd5By16(linkUrl));
            temp.setPosition(i);
            temp.setTitle(chapterlist.get(i).getElementsByTag("a").get(0).text());
            temp.setBookId(TAG);
            temp.setUnreadble(false);

            chapterBeans.add(temp);
        }

        return chapterBeans;


    }


    //获得图书内容
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public Single<ChapterInfoBean> getChapterInfo(String url) {
        return getRetrofitObject(TAG).create(ILingdiankanshuApi.class).getChapterInfo(url).flatMap(new Function<String, SingleSource<? extends ChapterInfoBean>>() {
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
