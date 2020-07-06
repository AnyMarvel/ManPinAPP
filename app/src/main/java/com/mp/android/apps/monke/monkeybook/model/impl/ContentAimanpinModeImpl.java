package com.mp.android.apps.monke.monkeybook.model.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mp.android.apps.main.home.bean.SourceListContent;
import com.mp.android.apps.monke.monkeybook.base.MBaseModelImpl;
import com.mp.android.apps.monke.monkeybook.bean.BookContentBean;
import com.mp.android.apps.monke.monkeybook.bean.BookShelfBean;
import com.mp.android.apps.monke.monkeybook.bean.SearchBookBean;
import com.mp.android.apps.monke.monkeybook.listener.OnGetChapterListListener;
import com.mp.android.apps.monke.monkeybook.model.IStationBookModel;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.functions.Function;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class ContentAimanpinModeImpl extends MBaseModelImpl implements IStationBookModel {
    //    public static final String TAG = "http://aimanpin.com/";
    public static final String TAG = "http://10.12.176.59:8080";

    public static ContentAimanpinModeImpl getInstance() {
        return new ContentAimanpinModeImpl();
    }

    private ContentAimanpinModeImpl() {
    }

    @Override
    public Observable<List<SearchBookBean>> searchBook(String content, int page) {
        return getRetrofitObject(TAG).create(SearchManpin.class).searchBook(content).flatMap(new Function<String, ObservableSource<List<SearchBookBean>>>() {
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
                    JSONObject jsonObject = JSON.parseObject(s);
                    String data = JSONArray.toJSONString(jsonObject.get("data"));
                    List<SearchBookBean> books = new ArrayList<SearchBookBean>();
                    List<SourceListContent> sourceListContents = JSON.parseArray(data, SourceListContent.class);
                    if (sourceListContents != null && sourceListContents.size() > 0) {
                        for (SourceListContent sourceListContent : sourceListContents) {
                            SearchBookBean searchBookBean = new SearchBookBean();
                            searchBookBean.setName(sourceListContent.getName());
                            searchBookBean.setCoverUrl(sourceListContent.getCoverUrl());
                            searchBookBean.setNoteUrl(sourceListContent.getNoteUrl());
                            searchBookBean.setAuthor(sourceListContent.getAuthor());
                            searchBookBean.setDesc(sourceListContent.getBookdesc());
                            searchBookBean.setOrigin(sourceListContent.getOrigin());
                            searchBookBean.setKind(sourceListContent.getKind());
                            searchBookBean.setTag(sourceListContent.getTag());
                            searchBookBean.setLastChapter(sourceListContent.getLastChapter());
                            searchBookBean.setAdd(false);
                            searchBookBean.setWords(0);
                            books.add(searchBookBean);
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

    @Override
    public Observable<BookShelfBean> getBookInfo(BookShelfBean bookShelfBean) {
        return null;
    }

    @Override
    public void getChapterList(BookShelfBean bookShelfBean, OnGetChapterListListener getChapterListListener) {

    }

    @Override
    public Observable<BookContentBean> getBookContent(String durChapterUrl, int durChapterIndex) {
        return null;
    }

    interface SearchManpin {
        @GET("/appview/searchBook")
        Observable<String> searchBook(@Query("bookName") String bookName);
    }
}
