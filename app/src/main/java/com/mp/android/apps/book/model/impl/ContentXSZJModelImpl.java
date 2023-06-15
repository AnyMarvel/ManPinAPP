
package com.mp.android.apps.book.model.impl;

import android.text.TextUtils;

import com.mp.android.apps.book.base.MBaseModelImpl;
import com.mp.android.apps.book.bean.SearchBookBean;
import com.mp.android.apps.book.common.api.IxszjAPI;
import com.mp.android.apps.book.model.IReaderBookModel;
import com.mp.android.apps.downloadUtils.RegexUtils;
import com.mp.android.apps.readActivity.bean.BookChapterBean;
import com.mp.android.apps.readActivity.bean.ChapterInfoBean;
import com.mp.android.apps.readActivity.bean.CollBookBean;
import com.mp.android.apps.utils.MD5Utils;
import com.mp.android.apps.utils.StringUtils;

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
public class ContentXSZJModelImpl extends MBaseModelImpl implements IReaderBookModel {
    public static final String TAG = "https://xszj.org";

    public static ContentXSZJModelImpl getInstance() {
        return new ContentXSZJModelImpl();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public Observable<List<SearchBookBean>> searchBook(String content, int page) {

        return getRetrofitObject(TAG).create(IxszjAPI.class).searchBook(content).flatMap(new Function<String, ObservableSource<List<SearchBookBean>>>() {
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
                    Elements booksE = doc.getElementsByClass("l rank").get(0).getElementsByClass("item");
                    if (null != booksE && booksE.size() > 0) {
                        List<SearchBookBean> books = new ArrayList<SearchBookBean>();
                        for (int i = 0; i < booksE.size(); i++) {
                            SearchBookBean item = new SearchBookBean();
                            item.setTag(TAG);
                            item.setAuthor("暂无");
                            item.setKind("未获取");
                            item.setLastChapter("未获取");
                            item.setOrigin("xszj.org");
                            item.setName(booksE.get(i).getElementsByClass("image").get(0).getElementsByTag("a").get(0).attr("title"));
                            item.setNoteUrl(TAG+booksE.get(i).getElementsByClass("image").get(0).getElementsByTag("a").get(0).attr("href"));
                            item.setCoverUrl(TAG+booksE.get(i).getElementsByClass("image").get(0).getElementsByClass("lazy").attr("data-original"));
                            item.setUpdated("未获取");
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
        return getRetrofitObject(TAG).create(IxszjAPI.class).getBookInfo(collBookBean.get_id().replace(TAG, "")).flatMap(new Function<String, ObservableSource<CollBookBean>>() {
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
                Element resultE = doc.getElementById("maininfo");

                Element info=doc.getElementById("info");
                Elements tagP=info.getElementsByTag("p");
                collBookBean.setAuthor(tagP.get(0).text().replace("作者：","").trim());

                String content=resultE.getElementById("intro").text().trim();
                collBookBean.setShortIntro(content);

                collBookBean.setUpdated(info.getElementsByTag("p").get(5).text().trim());
                collBookBean.setBookChapterUrl(collBookBean.get_id()+"/cs/1");
                String lastChapter =info.getElementsByTag("p").get(4).getElementsByTag("a").get(0).text().trim();
                collBookBean.setLastChapter(lastChapter);
                e.onNext(collBookBean);
                e.onComplete();
            }
        });
    }
///////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public Single<List<BookChapterBean>> getBookChapters(CollBookBean collBookBean) {
        List<BookChapterBean> chapterBeans = new ArrayList<BookChapterBean>();
        return loopAnalyChapterlist(collBookBean.getBookChapterUrl(),chapterBeans,collBookBean.getTitle());
    }



    private void handleAnalyChapterlist(String s, List<BookChapterBean> chapterBeans,String title) {
        Document doc = Jsoup.parse(s);
        Elements chapterlist = doc.getElementById("content_1").getElementsByTag("a");

        for (int i = 0; i < chapterlist.size(); i++) {
            BookChapterBean temp = new BookChapterBean();
            String linkUrl =TAG + chapterlist.get(i).getElementsByTag("a").get(0).attr("href");
            temp.setId(MD5Utils.strToMd5By16(linkUrl));
            temp.setTitle(chapterlist.get(i).getElementsByTag("a").text());
            temp.setPosition(i);
            temp.setLink(linkUrl);
            temp.setUnreadble(false);
            chapterBeans.add(temp);
        }
    }


    private Single<List<BookChapterBean>> loopAnalyChapterlist(String url, List<BookChapterBean> chapterBeans,String title) {
        return getRetrofitObject(TAG).create(IxszjAPI.class).getChapterLists(url)
                .flatMap(new Function<String, Single<List<BookChapterBean>>>() {

                    @Override
                    public Single<List<BookChapterBean>> apply(String s) throws Exception {

                        Document doc = Jsoup.parse(s);
                        Element indexselect=doc.getElementById("indexselect");
                        List<String> indexUrlList=new ArrayList<>();
                        if (indexselect!=null){
                            Elements indexselects=indexselect.getElementsByTag("option");
                            for (int i = 0; i < indexselects.size(); i++) {
                                indexUrlList.add(indexselects.get(i).attr("value"));
                            }
                        }
                        int currentPosition=0;
                        if (indexUrlList.size()>0){
                            currentPosition=indexUrlList.indexOf(url);
                        }
                        if (currentPosition < indexUrlList.size()-1){
                            handleAnalyChapterlist(s,chapterBeans,title);
                            return loopAnalyChapterlist(indexUrlList.get(currentPosition+1),chapterBeans,title);
                        }else {
                            return Single.create(new SingleOnSubscribe<List<BookChapterBean>>() {
                                @Override
                                public void subscribe(SingleEmitter<List<BookChapterBean>> emitter) throws Exception {
                                    handleAnalyChapterlist(s,chapterBeans,title);
                                    emitter.onSuccess(chapterBeans);
                                }
                            });
                        }
                    }
                });
    }

    /////////////////////////////////////////////////////////////////////////////////


    @Override
    public Single<ChapterInfoBean> getChapterInfo(String url) {
        int currentPosition=1;
        ChapterInfoBean chapterInfoBean = new ChapterInfoBean();
        return  handlejChapterInfo(url,chapterInfoBean,currentPosition);
    }


    private Single<ChapterInfoBean> handlejChapterInfo(String url,ChapterInfoBean chapterInfoBean,int currentPosition) {
        return getRetrofitObject(TAG).create(IxszjAPI.class).getChapterInfo(url.replace(TAG,"") + "?page=" + currentPosition).flatMap(new Function<String, SingleSource<? extends ChapterInfoBean>>() {
            @Override
            public SingleSource<? extends ChapterInfoBean> apply(String s) throws Exception {
                Document doc = Jsoup.parse(s);
                String name = doc.getElementsByClass("bookname").get(0).text();

                String pageNumber = RegexUtils.getMatch0(name, "（.*?）", false).replace("（","").replace("）","").trim();
                if (pageNumber != null) {
                    String[] result = pageNumber.split("/");
                    if (result != null && result.length > 1) {
                        if (currentPosition < StringUtils.tryParseInt(result[1])) {
                            analysisChapterInfo(s, chapterInfoBean);
                            return handlejChapterInfo(url, chapterInfoBean, StringUtils.tryParseInt(result[1]));
                        }
                    }
                }
                return Single.create(new SingleOnSubscribe<ChapterInfoBean>() {
                    @Override
                    public void subscribe(SingleEmitter<ChapterInfoBean> emitter) throws Exception {
                        analysisChapterInfo(s, chapterInfoBean);
                        emitter.onSuccess(chapterInfoBean);
                    }
                });

            }
        });
    }

    private String analysisChapterInfo(String s,ChapterInfoBean chapterInfoBean){
        StringBuilder content = new StringBuilder();
        if (chapterInfoBean!=null && !TextUtils.isEmpty(chapterInfoBean.getBody())){
            content.append(chapterInfoBean.getBody());
        }
        try {
            Document doc = Jsoup.parse(s);
            Elements contentEs = doc.getElementById("booktxt").getElementsByTag("p");

            for (int i = 0; i < contentEs.size(); i++) {
                String temp = contentEs.get(i).text().trim();
                temp = temp.replaceAll(" ", "").replaceAll(" ", "");
                if (temp.length() > 0) {
                    content.append("\u3000\u3000" + temp);
                    if (i < contentEs.size()) {
                        content.append("\r\n");
                    }
                }
            }
            chapterInfoBean.setBody(content.toString());
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
