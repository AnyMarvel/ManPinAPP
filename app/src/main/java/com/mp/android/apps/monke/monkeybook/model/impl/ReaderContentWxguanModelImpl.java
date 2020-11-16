
package com.mp.android.apps.monke.monkeybook.model.impl;


import com.mp.android.apps.monke.monkeybook.base.MBaseModelImpl;

import com.mp.android.apps.monke.monkeybook.common.api.IWxguanAPI;
import com.mp.android.apps.monke.readActivity.bean.BookChapterBean;
import com.mp.android.apps.monke.readActivity.bean.ChapterInfoBean;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;

public class ReaderContentWxguanModelImpl extends MBaseModelImpl {
    public static final String TAG = "https://www.wxguan.com";
    public static final String TAG_SEARCH = "https://so.biqusoso.com";
    public static final String ORIGIN = "wxguan.com";

    public static ReaderContentWxguanModelImpl getInstance() {
        return new ReaderContentWxguanModelImpl();
    }

    private ReaderContentWxguanModelImpl() {

    }

    //==================================================================================
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
            temp.setId(novelUrl);
            temp.setTitle(chapterlist.get(i).getElementsByTag("a").get(0).text());
            temp.setLink(TAG + chapterlist.get(i).getElementsByTag("a").get(0).attr("href"));   //id
            temp.setStart(i);
            temp.setBookId(novelUrl);
            temp.setUnreadble(false);
            chapterBeans.add(temp);
        }
        return chapterBeans;
    }

    //==================================
    public Single<ChapterInfoBean> getChapterInfo(String url) {
        return getRetrofitObject(TAG).create(IWxguanAPI.class).getChapterInfo(url).flatMap(new Function<String, SingleSource<? extends ChapterInfoBean>>() {
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
            chapterInfoBean.setBody("站点暂时不支持解析，请反馈给Monke QQ:314599558,半小时内解决，超级效率的程序员");

        }
        return chapterInfoBean;
    }

}
