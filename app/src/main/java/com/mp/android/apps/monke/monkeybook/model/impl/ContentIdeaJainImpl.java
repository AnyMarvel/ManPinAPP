
package com.mp.android.apps.monke.monkeybook.model.impl;


import com.google.android.apps.photolab.storyboard.download.MD5Utils;
import com.mp.android.apps.monke.monkeybook.base.MBaseModelImpl;
import com.mp.android.apps.monke.monkeybook.base.observer.SimpleObserver;
import com.mp.android.apps.monke.monkeybook.bean.SearchBookBean;
import com.mp.android.apps.monke.monkeybook.common.api.IIdeaJianAPI;
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
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * 得间小说
 */

public class ContentIdeaJainImpl extends MBaseModelImpl implements IReaderBookModel {
    public static final String TAG = "https://www.idejian.com";
    public static final String ORIGIN = "idejian.com";

    public static ContentIdeaJainImpl getInstance() {
        return new ContentIdeaJainImpl();
    }

    private ContentIdeaJainImpl() {

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public Observable<List<SearchBookBean>> searchBook(String content, int page) {
        return getRetrofitObject(TAG).create(IIdeaJianAPI.class).searchBook(content).flatMap(new Function<String, ObservableSource<List<SearchBookBean>>>() {
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
                    Elements booksE = doc.getElementsByClass("rank_ullist").get(0).getElementsByTag("li");
                    if (null != booksE && booksE.size() > 1) {
                        List<SearchBookBean> books = new ArrayList<SearchBookBean>();
                        for (int i = 1; i < booksE.size(); i++) {
                            SearchBookBean item = new SearchBookBean();
                            item.setTag(TAG);
                            item.setAuthor(booksE.get(i).getElementsByClass("author").get(0).text());
                            item.setKind("得间小说");
//                            item.setState();
                            item.setOrigin(ORIGIN);
                            item.setName(booksE.get(i).getElementsByClass("rank_bkname").get(0).getElementsByTag("a").get(0).text());
                            item.setNoteUrl(TAG + booksE.get(i).getElementsByClass("rank_bkname").get(0).getElementsByTag("a").get(0).attr("href"));
                            item.setCoverUrl(booksE.get(i).getElementsByClass("items_l").get(0).getElementsByTag("a").get(0).getElementsByTag("img").get(0).attr("src"));
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
        return getRetrofitObject(TAG).create(IIdeaJianAPI.class).getBookInfo(collBookBean.get_id().replace(TAG, "")).flatMap(new Function<String, ObservableSource<CollBookBean>>() {
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

                Element bookInfo = doc.getElementsByClass("detail_bkinfo").get(0);
                collBookBean.setCover(bookInfo.getElementsByClass("info_bookimg").get(0).getElementsByTag("img").attr("src"));

                collBookBean.setTitle(bookInfo.getElementsByClass("detail_bkname").get(0).getElementsByTag("a").get(0).text());
                String author = bookInfo.getElementsByClass("detail_bkauthor").get(0).text().toString().trim();
                author = author.replace(" ", "").replace("  ", "").replace("\"", "");
                collBookBean.setAuthor(author);
                Element bookPage = doc.getElementsByClass("book_page").get(0);
                String updatedTime = bookPage.getElementsByTag("span").get(0).text().toString().trim();
                updatedTime = updatedTime.replace(" ", "").replace("  ", "");

                collBookBean.setUpdated(updatedTime);

                collBookBean.setLastChapter(bookPage.getElementsByClass("link_name").get(0).text().toString().trim());
                List<TextNode> contentEs = doc.getElementsByClass("brief_con").get(0).textNodes();
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
                collBookBean.setBookChapterUrl(collBookBean.get_id());
                try {
                    String kind = bookInfo.getElementsByClass("detail_bkgrade").get(0).getElementsByTag("span").get(1).text();
                    String lastChapter = bookPage.getElementsByClass("link_name").get(0).text().toString().trim();
                    ObtainBookInfoImpl.getInstance().senMessageManpin(collBookBean, kind, lastChapter);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                e.onNext(collBookBean);
                e.onComplete();
            }
        });
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////    @Override
    public Single<List<BookChapterBean>> getBookChapters(CollBookBean collBookBean) {
        return getRetrofitObject(TAG).create(IIdeaJianAPI.class).getChapterLists(collBookBean.getBookChapterUrl())
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
        Elements chapterlist = doc.getElementsByClass("catelog_list").get(0).getElementsByTag("li");
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

        return getRetrofitObject(TAG).create(IIdeaJianAPI.class).getChapterInfo(url).flatMap(new Function<String, SingleSource<? extends ChapterInfoBean>>() {
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
            List<Element> contentEs = doc.getElementsByClass("read_content").get(0).getElementsByClass("bodytext");
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
            System.out.println(ex.getStackTrace());
            ex.printStackTrace();

        }
        return chapterInfoBean;
    }

}
