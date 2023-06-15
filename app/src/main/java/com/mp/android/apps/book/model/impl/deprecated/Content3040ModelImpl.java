
package com.mp.android.apps.book.model.impl.deprecated;

import android.text.TextUtils;

import com.mp.android.apps.utils.MD5Utils;
import com.mp.android.apps.book.base.MBaseModelImpl;
import com.mp.android.apps.book.bean.SearchBookBean;
import com.mp.android.apps.book.common.api.I3040API;
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
 * 3040文学
 */
@Deprecated
public class Content3040ModelImpl extends MBaseModelImpl implements IReaderBookModel {
    public static final String TAG = "https://www.130140.com";

    public static Content3040ModelImpl getInstance() {
        return new Content3040ModelImpl();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public Observable<List<SearchBookBean>> searchBook(String content, int page) {

        return getRetrofitObject(TAG).create(I3040API.class).searchBook("search",content).flatMap(new Function<String, ObservableSource<List<SearchBookBean>>>() {
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
                    Elements booksE = doc.getElementsByClass("table").get(0).getElementsByTag("tr");
                    if (null != booksE && booksE.size() > 1) {
                        List<SearchBookBean> books = new ArrayList<SearchBookBean>();
                        for (int i = 1; i < booksE.size(); i++) {
                            SearchBookBean item = new SearchBookBean();
                            item.setTag(TAG);
                            item.setAuthor(booksE.get(i).getElementsByClass("text-muted").get(0).text());
                            item.setKind(booksE.get(i).getElementsByTag("td").get(5).text());
                            item.setLastChapter(booksE.get(i).getElementsByTag("td").get(5).text());
                            item.setOrigin("130140.com");
                            item.setName(booksE.get(i).getElementsByTag("td").get(0).getElementsByTag("a").get(0).text());
                            item.setNoteUrl(booksE.get(i).getElementsByTag("td").get(0).getElementsByTag("a").get(0).attr("href"));
                            item.setCoverUrl("noimage");
                            item.setUpdated(booksE.get(i).getElementsByClass("hidden-xs").get(0).text());
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
        return getRetrofitObject(TAG).create(I3040API.class).getBookInfo(collBookBean.get_id().replace(TAG, "")).flatMap(new Function<String, ObservableSource<CollBookBean>>() {
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
                Element resultE = doc.getElementsByClass("panel-body").get(0);
                collBookBean.set_id(collBookBean.get_id());
                collBookBean.setCover(resultE.getElementsByClass("img-thumbnail").get(0).attr("src"));

                collBookBean.setTitle(resultE.getElementsByClass("bookTitle").get(0).text());
                if (!TextUtils.isEmpty(collBookBean.getAuthor())){
                    collBookBean.setAuthor("暂无");
                }

                List<TextNode> contentEs = resultE.getElementsByClass("text-muted").get(0).textNodes();
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
                collBookBean.setUpdated(resultE.getElementsByClass("visible-xs").get(0).text().toString().trim());
                collBookBean.setBookChapterUrl(collBookBean.get_id());
                String lastChapter = resultE.getElementsByClass("col-md-10").get(0).getElementsByTag("p").get(1).getElementsByTag("a").get(0).text();
                collBookBean.setLastChapter(lastChapter);
                e.onNext(collBookBean);
                e.onComplete();
            }
        });
    }
///////////////////////////////////////////////////////////////////////////////////////////////////////


    public Single<List<BookChapterBean>> getBookChapters(CollBookBean collBookBean) {
        return getRetrofitObject(TAG).create(I3040API.class).getChapterLists(collBookBean.getBookChapterUrl())
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
        Elements chapterlist = doc.getElementById("list-chapterAll").getElementsByTag("dd");

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

    /////////////////////////////////////////////////////////////////////////////////


    @Override
    public Single<ChapterInfoBean> getChapterInfo(String url) {
        return getRetrofitObject(TAG).create(I3040API.class).getChapterInfo(url).flatMap(new Function<String, SingleSource<? extends ChapterInfoBean>>() {
            @Override
            public SingleSource<? extends ChapterInfoBean> apply(String s) throws Exception {

               StringBuilder content=new StringBuilder();
               Object object=new Object();
               getNextPageContent(object,s,content);
               synchronized (object){
                   object.wait();

                   return Single.create(new SingleOnSubscribe<ChapterInfoBean>() {
                       @Override
                       public void subscribe(SingleEmitter<ChapterInfoBean> emitter) throws Exception {
                           ChapterInfoBean chapterInfoBean = new ChapterInfoBean();
                           chapterInfoBean.setBody(content.toString());
                           emitter.onSuccess(chapterInfoBean);
                       }
                   });

               }
            }
        });
    }

    private void getNextPageContent(Object localObject,String s,StringBuilder content){
        synchronized (localObject) {
            content.append(analysisChapterInfo(s));
            Document doc = Jsoup.parse(s);
            Element linkNext = doc.getElementById("linkNext");
            if (linkNext != null && linkNext.text() != null && linkNext.text().contains("下一页")) {
                String nextPageUrl = linkNext.attr("href").replace(TAG, "");
                getRetrofitObject(TAG).create(I3040API.class).getChapterInfo(nextPageUrl)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new SingleObserver<String>() {
                            @Override
                            public void onSubscribe(Disposable d) {

                            }

                            @Override
                            public void onSuccess(String s) {
                                getNextPageContent(localObject, s, content);
                            }

                            @Override
                            public void onError(Throwable e) {

                            }
                        });
            } else {
                localObject.notify();
            }
        }

    }
    private String analysisChapterInfo(String s){
        StringBuilder content = new StringBuilder();
        try {
            Document doc = Jsoup.parse(s);
            List<TextNode> contentEs = doc.getElementById("wudidexiaoxiao").textNodes();

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

            content.append("部分章节解析失败，请翻页尝试，或到我的界面。联系管理员");
        }
        return  content.toString();
    }

    @Override
    public String getTAG() {
        return TAG;
    }
}
