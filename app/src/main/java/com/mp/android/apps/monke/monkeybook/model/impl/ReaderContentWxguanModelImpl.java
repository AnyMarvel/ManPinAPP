
package com.mp.android.apps.monke.monkeybook.model.impl;

import com.mp.android.apps.monke.monkeybook.base.MBaseModelImpl;
import com.mp.android.apps.monke.monkeybook.common.api.IWxguanAPI;
import com.mp.android.apps.monke.readActivity.bean.BookChapterBean;

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
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class ReaderContentWxguanModelImpl extends MBaseModelImpl {
    public static final String TAG = "https://www.wxguan.com";
    public static final String TAG_SEARCH = "https://so.biqusoso.com";
    public static final String ORIGIN = "wxguan.com";

    public static ReaderContentWxguanModelImpl getInstance() {
        return new ReaderContentWxguanModelImpl();
    }

    private ReaderContentWxguanModelImpl() {

    }

    public Single<List<BookChapterBean>> getBookChapters(String bookurl) {
        return getRetrofitObject(TAG).create(IWxguanAPI.class).getChapterLists(bookurl)
                .flatMap(new Function<String, Single<List<BookChapterBean>>>() {

                    @Override
                    public Single<List<BookChapterBean>> apply(String s) throws Exception {
                        return Single.create(new SingleOnSubscribe<List<BookChapterBean>>() {
                            @Override
                            public void subscribe(SingleEmitter<List<BookChapterBean>> emitter) throws Exception {
                                emitter.onSuccess(analyChapterlist(s, bookurl));
                            }
                        });
                    }
                });

    }

    private List<BookChapterBean> analyChapterlist(String s, String novelUrl) {
        Document doc = Jsoup.parse(s);
        Elements chapterlist = doc.getElementsByClass("listmain").get(0).getElementsByTag("dd");
        List<BookChapterBean> chapterBeans = new ArrayList<BookChapterBean>();
        for (int i = 11; i < chapterlist.size(); i++) {
            BookChapterBean temp = new BookChapterBean();
            temp.setLink(TAG + chapterlist.get(i).getElementsByTag("a").get(0).attr("href"));   //id
            temp.setStart(i);
            temp.setBookId(chapterlist.get(i).getElementsByTag("a").get(0).text());
            temp.setId(novelUrl);
            chapterBeans.add(temp);
        }
        return chapterBeans;
    }


}
