
package com.mp.android.apps.book.model.impl;

import com.google.android.apps.photolab.storyboard.download.MD5Utils;
import com.mp.android.apps.book.base.MBaseModelImpl;
import com.mp.android.apps.book.base.observer.SimpleObserver;
import com.mp.android.apps.book.bean.SearchBookBean;
import com.mp.android.apps.book.common.api.ILingDianAPI;
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
import io.reactivex.SingleObserver;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * 零点小说源
 */
public class ContentLingDianModelImpl extends MBaseModelImpl implements IReaderBookModel {
    public static final String TAG = "https://www.soudu.org";

    public static ContentLingDianModelImpl getInstance() {
        return new ContentLingDianModelImpl();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public Observable<List<SearchBookBean>> searchBook(String content, int page) {
        return getRetrofitObject(TAG).create(ILingDianAPI.class).searchBook(content).flatMap(new Function<String, ObservableSource<List<SearchBookBean>>>() {
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
                    Elements booksE = doc.getElementsByClass("txt-list").get(0).getElementsByTag("li");
                    if (null != booksE && booksE.size() > 1) {
                        List<SearchBookBean> books = new ArrayList<SearchBookBean>();
                        for (int i = 1; i < booksE.size(); i++) {
                            SearchBookBean item = new SearchBookBean();
                            item.setTag(TAG);
                            item.setAuthor(booksE.get(i).getElementsByClass("s4").get(0).text());
                            item.setKind(booksE.get(i).getElementsByClass("s1").get(0).text());
                            item.setLastChapter(booksE.get(i).getElementsByClass("s3").get(0).getElementsByTag("a").get(0).text());
                            item.setOrigin("soudu.org");
                            item.setName(booksE.get(i).getElementsByClass("s2").get(0).getElementsByTag("a").get(0).text());
                            item.setNoteUrl(TAG + booksE.get(i).getElementsByClass("s2").get(0).getElementsByTag("a").get(0).attr("href"));
                            item.setCoverUrl("noimage");
                            item.setUpdated(booksE.get(i).getElementsByClass("s5").get(0).text());
                            item.setLastChapter(booksE.get(i).getElementsByClass("s3").get(0).getElementsByTag("a").get(0).text());
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
        return getRetrofitObject(TAG).create(ILingDianAPI.class).getBookInfo(collBookBean.get_id().replace(TAG, "")).flatMap(new Function<String, ObservableSource<CollBookBean>>() {
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
                Element resultE = doc.getElementsByClass("detail-box").get(0);
                collBookBean.set_id(collBookBean.get_id());
                collBookBean.setCover(resultE.getElementsByClass("imgbox").get(0).getElementsByTag("img").get(0).attr("src"));
                Element info= resultE.getElementsByClass("info").get(0);
                collBookBean.setTitle(info.getElementsByClass("top").get(0).getElementsByTag("h1").get(0).text());
                String author = resultE.getElementsByClass("fix").get(0).getElementsByTag("p").get(0).text().toString().trim();
                author = author.replace(" ", "").replace("  ", "").replace("作者：", "");
                collBookBean.setAuthor(author);

                List<TextNode> contentEs = resultE.getElementsByClass("desc").get(0).textNodes();
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

                collBookBean.setUpdated(resultE.getElementsByClass("fix").get(0).getElementsByTag("p").get(4).text().toString().trim());
                collBookBean.setBookChapterUrl(collBookBean.get_id());
                try {
                    ObtainBookInfoUtils.getInstance().senMessageManpin(collBookBean, "", collBookBean.getLastChapter());
                } catch (Exception e1) {

                }
                e.onNext(collBookBean);
                e.onComplete();
            }
        });
    }
///////////////////////////////////////////////////////////////////////////////////////////////////////


    public Single<List<BookChapterBean>> getBookChapters(CollBookBean collBookBean) {
        return getRetrofitObject(TAG).create(ILingDianAPI.class).getChapterLists(collBookBean.getBookChapterUrl())
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
        Elements chapterlist = doc.getElementsByClass("section-list").get(1).getElementsByTag("li");

        for (int i = 0; i < chapterlist.size(); i++) {
            BookChapterBean temp = new BookChapterBean();
            String linkUrl = TAG + chapterlist.get(i).getElementsByTag("a").get(0).attr("href");
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

    /////////////////////////////////////////////////////////////////////////////////


    @Override
    public Single<ChapterInfoBean> getChapterInfo(String url) {
        final StringBuilder[] content = {new StringBuilder()};
        return getRetrofitObject(TAG).create(ILingDianAPI.class).getChapterInfo(url)
                .flatMap(new Function<String, SingleSource<String>>() {
                    @Override
                    public SingleSource<String> apply(String s) throws Exception {
                        Document doc = Jsoup.parse(s);
                        String nextTip=doc.getElementsByClass("section-opt").get(1).getElementsByTag("a").get(2).text();
                        String nextHref=doc.getElementsByClass("section-opt").get(1).getElementsByTag("a").get(2).attr("href");
                        if ("下一页".equals(nextTip)){
                            addChapterInfoStr(s, content[0]);
                            return getRetrofitObject(TAG).create(ILingDianAPI.class).getChapterInfo(nextHref);
                        }else {
                            content[0] =new StringBuilder();
                            return getRetrofitObject(TAG).create(ILingDianAPI.class).getChapterInfo(url);
                        }

                    }
                }).flatMap(new Function<String, SingleSource<? extends ChapterInfoBean>>() {
                    @Override
                    public SingleSource<? extends ChapterInfoBean> apply(String s) throws Exception {

                        return Single.create(new SingleOnSubscribe<ChapterInfoBean>() {
                            @Override
                            public void subscribe(SingleEmitter<ChapterInfoBean> emitter) throws Exception {
                                addChapterInfoStr(s, content[0]);
                                ChapterInfoBean chapterInfoBean = new ChapterInfoBean();
                                chapterInfoBean.setBody(content[0].toString());
                                emitter.onSuccess(chapterInfoBean);
                            }
                        });
                    }
                });
    }




    @Override
    public String getTAG() {
        return TAG;
    }

    private void addChapterInfoStr(String s,StringBuilder content){
        try {
            Document doc = Jsoup.parse(s);
            List<TextNode> contentEs = doc.getElementById("content").textNodes();

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

        } catch (Exception ex) {
            ex.printStackTrace();
        }
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
            chapterInfoBean.setBody("章节解析失败，请翻页尝试，或到我的界面。联系管理员");
        }
        return chapterInfoBean;
    }

}
