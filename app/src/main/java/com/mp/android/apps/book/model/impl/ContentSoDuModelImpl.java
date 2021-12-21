
package com.mp.android.apps.book.model.impl;

import android.text.TextUtils;

import com.google.android.apps.photolab.storyboard.download.MD5Utils;
import com.mp.android.apps.book.base.MBaseModelImpl;
import com.mp.android.apps.book.bean.SearchBookBean;
import com.mp.android.apps.book.common.api.ISoduApi;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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

public class ContentSoDuModelImpl extends MBaseModelImpl implements IReaderBookModel {
    public static final String TAG = "http://www.soduzw.com";
    private final String Origin = "soduzw.com";

    public static ContentSoDuModelImpl getInstance() {
        return new ContentSoDuModelImpl();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public Observable<List<SearchBookBean>> searchBook(String content, int page) {
        Map<String,String> map=new HashMap<>();
        map.put("searchtype","novelname");
        map.put("searchkey",content);
        return getRetrofitObject(TAG).create(ISoduApi.class).searchBook(map).flatMap(new Function<String, ObservableSource<List<SearchBookBean>>>() {
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
                    Elements booksE = doc.getElementsByClass("Look").get(0).getElementsByClass("Search");
                    if (null != booksE && booksE.size() > 0) {
                        List<SearchBookBean> books = new ArrayList<SearchBookBean>();
                        for (int i = 0; i < booksE.size(); i++) {
                            SearchBookBean item = new SearchBookBean();
                            item.setTag(TAG);
                            item.setAuthor(booksE.get(i).getElementsByClass("Search_title").get(0).getElementsByTag("span").get(1).text());
//                            item.setKind(booksE.get(i).getElementsByClass("s2").get(0).text());
                            item.setLastChapter(booksE.get(i).getElementsByClass("Search_update").get(0).getElementsByTag("a").get(0).text());
                            item.setOrigin(Origin);
                            item.setName(booksE.get(i).getElementsByClass("Search_title").get(0).getElementsByTag("span").get(0).getElementsByTag("a").get(0).text());
                            item.setNoteUrl(TAG + booksE.get(i).getElementsByClass("Search_title").get(0).getElementsByTag("span").get(0).getElementsByTag("a").get(0).attr("href"));
                            item.setCoverUrl(TAG + booksE.get(i).getElementsByClass("Search_tuku").get(0).getElementsByTag("img").get(0).attr("src"));
                            item.setUpdated(booksE.get(i).getElementsByClass("Search_update").get(0).text());
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
        return getRetrofitObject(TAG).create(ISoduApi.class).getBookInfo(collBookBean.get_id().replace(TAG, "")).flatMap(new Function<String, ObservableSource<CollBookBean>>() {
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
                Element resultE = doc.getElementsByClass("Look").get(0);
                collBookBean.set_id(collBookBean.get_id());

                collBookBean.setTitle(resultE.getElementsByClass("Mulu_title").get(0).text());


                String contentEs = resultE.getElementsByTag("div").get(0).text();

                collBookBean.setShortIntro(contentEs);

                collBookBean.setUpdated(resultE.getElementsByClass("Look_list").get(0).getElementsByTag("li").get(0).getElementsByClass("chapterlist3").get(0).text());

                collBookBean.setBookChapterUrl(collBookBean.get_id());
                String lastChapter = resultE.getElementsByClass("Look_list").get(0).getElementsByTag("li").get(0).getElementsByClass("chapterlist1").get(0).getElementsByTag("a").get(0).text();
                collBookBean.setLastChapter(lastChapter);
                try {
                    ObtainBookInfoUtils.getInstance().senMessageManpin(collBookBean, "", lastChapter);
                } catch (Exception e1) {

                }
                e.onNext(collBookBean);
                e.onComplete();
            }
        });
    }
///////////////////////////////////////////////////////////////////////////////////////////////////////


    public Single<List<BookChapterBean>> getBookChapters(CollBookBean collBookBean) {
        return getRetrofitObject(TAG).create(ISoduApi.class).getChapterLists(collBookBean.getBookChapterUrl())
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
            String paginationTip=doc.getElementsByClass("pagination").get(0).getElementsByTag("span").get(0).text();
            String pageNumberStr=paginationTip.substring(paginationTip.indexOf("共")+1,paginationTip.lastIndexOf("页")).trim();
            int pageNumber=1;
            if (!TextUtils.isEmpty(pageNumberStr) && TextUtils.isDigitsOnly(pageNumberStr)){
                pageNumber=Integer.parseInt(pageNumberStr);
            }
            Object object=new Object();
            final int finalPageNumber = pageNumber;
            // 数据源
            Map<Integer,Map<Integer,BookChapterBean>> chaptersMap=new HashMap<>();
            final long startTime = System.currentTimeMillis();


                synchronized (object){
                    for (int j = 1; j <= pageNumber; j++) {
                            String requestUrl=collBookBean.getBookChapterUrl().replace(".html","_"+j+".html");
                            int finalJ = j;
                            getRetrofitObject(TAG).create(ISoduApi.class).getChapterLists(requestUrl)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new SingleObserver<String>() {
                                        @Override
                                        public void onSubscribe(Disposable d) {

                                        }

                                        @Override
                                        public void onSuccess(String s) {
                                            synchronized (object) {
                                                Document doc = Jsoup.parse(s);
                                                Elements chapterlist = doc.getElementsByClass("Look_list").get(0).getElementsByTag("li");
                                                Map<Integer,BookChapterBean> singleChapterMap=new HashMap<>();
                                                for (int i = 0; i < chapterlist.size(); i++) {
                                                    BookChapterBean temp = new BookChapterBean();
                                                    String linkUrl = TAG + chapterlist.get(i).getElementsByClass("chapterlist1").get(0).getElementsByTag("a").get(0).attr("href");
                                                    temp.setId(MD5Utils.strToMd5By16(linkUrl));
                                                    temp.setTitle(chapterlist.get(i).getElementsByClass("chapterlist1").get(0).getElementsByTag("a").get(0).text());
                                                    temp.setPosition(i);
                                                    temp.setLink(linkUrl);
                                                    temp.setBookId(collBookBean.get_id());
                                                    temp.setUnreadble(false);
                                                    singleChapterMap.put(i, temp);
                                                    chaptersMap.put(finalJ,singleChapterMap);
                                                    long endTime = System.currentTimeMillis();

                                                    if (chaptersMap.size() == finalPageNumber || endTime-startTime>20000){
                                                        object.notify();
                                                    }

                                                }
                                            }

                                        }

                                        @Override
                                        public void onError(Throwable e) {

                                        }
                                    });


                    }
                    try {
                        object.wait();

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
        List<BookChapterBean> chapterBeans = new ArrayList<BookChapterBean>();
                for (int i = 1; i <= chaptersMap.size() ; i++) {
                    Map<Integer,BookChapterBean> singleChapter=chaptersMap.get(i);
                    if (singleChapter!=null){
                        List<Map.Entry<Integer,BookChapterBean>> list=new ArrayList<>(singleChapter.entrySet());
                        Collections.sort(list, new Comparator<Map.Entry<Integer, BookChapterBean>>() {
                            @Override
                            public int compare(Map.Entry<Integer, BookChapterBean> o1, Map.Entry<Integer, BookChapterBean> o2) {
                                return o1.getKey().compareTo(o2.getKey());
                            }
                        });

                        for (int j = 0; j < list.size(); j++) {
                            chapterBeans.add(list.get(j).getValue());
                        }
                    }
                }
        Collections.reverse(chapterBeans);

        return chapterBeans;


    }

    /////////////////////////////////////////////////////////////////////////////////


    @Override
    public Single<ChapterInfoBean> getChapterInfo(String url) {
        return getRetrofitObject(TAG).create(ISoduApi.class).getChapterInfo(url).flatMap(new Function<String, SingleSource<? extends ChapterInfoBean>>() {
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
            List<TextNode> contentEs = doc.getElementsByClass("content").get(1).textNodes();
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

}
