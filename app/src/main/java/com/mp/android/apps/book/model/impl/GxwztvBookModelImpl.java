
package com.mp.android.apps.book.model.impl;

import com.google.android.apps.photolab.storyboard.download.MD5Utils;
import com.mp.android.apps.book.base.MBaseModelImpl;
import com.mp.android.apps.book.bean.SearchBookBean;
import com.mp.android.apps.book.common.api.IGxwztvApi;
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
import java.util.List;
import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;
//梧桐中文网 http://www.ztv.la/28/28945/ 添加了反扒策略 需要重新适配

/**
 * 梧桐中文网地址网站已废弃
 * http://www.wzzw.la 已废弃
 */

@Deprecated
public class GxwztvBookModelImpl extends MBaseModelImpl implements IReaderBookModel {
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
    public Observable<CollBookBean> getBookInfo(final CollBookBean collBookBean) {
        return getRetrofitObject(TAG).create(IGxwztvApi.class).getBookInfo(collBookBean.get_id().replace(TAG, ""))
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
                collBookBean.setBookTag(TAG);
                Document doc = Jsoup.parse(s);
                Element resultE = doc.getElementsByClass("panel panel-warning").get(0);
                collBookBean.setCover(resultE.getElementsByClass("panel-body").get(0).getElementsByClass("img-thumbnail").get(0).attr("src"));
                collBookBean.setTitle(resultE.getElementsByClass("active").get(0).text());
                collBookBean.setAuthor(resultE.getElementsByClass("col-xs-12 list-group-item no-border").get(0).getElementsByTag("small").get(0).text());
                Element introduceE = resultE.getElementsByClass("panel panel-default mt20").get(0);
                String introduce = "";
                if (introduceE.getElementById("all") != null) {
                    introduce = introduceE.getElementById("all").text().replace("[收起]", "");
                } else {
                    introduce = Objects.requireNonNull(introduceE.getElementById("shot")).text();
                }
                collBookBean.setShortIntro("\u3000\u3000" + introduce);
                String bookChapterUrl = TAG + resultE.getElementsByClass("list-group-item tac").get(0).getElementsByTag("a").get(0).attr("href");
                collBookBean.setBookChapterUrl(bookChapterUrl);
                String updatedTime = resultE.getElementsByClass("col-xs-4 list-group-item no-border").get(2).text().replace(" ", "").replace("  ", "").replace("更新时间：", "");
                collBookBean.setUpdated(updatedTime);

                String kind = resultE.getElementsByClass("col-xs-4 list-group-item no-border").get(0).getElementsByTag("a").get(0).text();
                String lastChapter = resultE.getElementsByClass("col-xs-12 list-group-item no-border").get(1).getElementsByTag("a").get(0).text();
                collBookBean.setLastChapter(lastChapter);
                ObtainBookInfoImpl.getInstance().senMessageManpin(collBookBean, kind, lastChapter);

                e.onNext(collBookBean);
                e.onComplete();
            }
        });
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public Single<List<BookChapterBean>> getBookChapters(CollBookBean collBookBean) {
        return getRetrofitObject(TAG).create(IGxwztvApi.class).getChapterLists(collBookBean.getBookChapterUrl())
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
        Elements chapterlist = doc.getElementById("chapters-list").getElementsByTag("a");
        List<BookChapterBean> chapterBeans = new ArrayList<BookChapterBean>();
        for (int i = 0; i < chapterlist.size(); i++) {
            BookChapterBean temp = new BookChapterBean();
            String linkUrl = TAG + chapterlist.get(i).attr("href");
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


    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public Single<ChapterInfoBean> getChapterInfo(String url) {
        return getRetrofitObject(TAG).create(IGxwztvApi.class).getChapterInfo(url).flatMap(new Function<String, SingleSource<? extends ChapterInfoBean>>() {
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
            chapterInfoBean.setBody(content.toString());


        } catch (Exception ex) {
            ex.printStackTrace();
        }


        return chapterInfoBean;
    }
}
