
package com.mp.android.apps.monke.monkeybook.model.impl;

import android.net.Uri;

import com.mp.android.apps.monke.monkeybook.bean.BookContentBean;
import com.mp.android.apps.monke.monkeybook.bean.SearchBookBean;
import com.mp.android.apps.monke.readActivity.bean.BookChapterBean;
import com.mp.android.apps.monke.readActivity.bean.ChapterInfoBean;
import com.mp.android.apps.monke.readActivity.bean.CollBookBean;
import com.mp.android.apps.utils.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Single;

import static com.mp.android.apps.monke.monkeybook.presenter.impl.SearchPresenterImpl.TAG_KEY;

/**
 * 图书内容获取 加载，解析，章节处理，内容处理等问题
 */
public class WebBookModelImpl {
    private static WebBookModelImpl webBookModel;

    private WebBookModelImpl() {
    }

    public static WebBookModelImpl getInstance() {
        if (webBookModel == null) {
            synchronized (WebBookModelImpl.class) {
                if (webBookModel == null) {
                    webBookModel = new WebBookModelImpl();
                }
            }
        }
        return webBookModel;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public Observable<CollBookBean> getBookInfo(CollBookBean collBookBean) {
        switch (collBookBean.getBookTag()) {
            case ReaderContentWxguanModelImpl.TAG:
                return ReaderContentWxguanModelImpl.getInstance().getBookInfo(collBookBean);
            case ReaderGxwztvBookModelImpl.TAG:
                return ReaderGxwztvBookModelImpl.getInstance().getBookInfo(collBookBean);
            default:
                return null;
        }

    }

    /**
     * 根据 图书url获取章节目录
     *
     * @param bookId
     * @return
     */
    public Single<List<BookChapterBean>> getBookChapters(String bookId) {
        Uri uri = Uri.parse(bookId);
        String TAG = uri.getScheme() + "://" + uri.getHost();
        Logger.d("Current website" + TAG);
        switch (TAG) {
            case ReaderContentWxguanModelImpl.TAG:
                return ReaderContentWxguanModelImpl.getInstance().getBookChapters(bookId);
            case ReaderGxwztvBookModelImpl.TAG:
                return ReaderGxwztvBookModelImpl.getInstance().getBookChapters(bookId);
            default:
                return null;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 章节内容
     */
    public Observable<BookContentBean> getBookContent(String durChapterUrl, int durChapterIndex, String tag) {
        if (tag.equals(GxwztvBookModelImpl.TAG)) {
            return GxwztvBookModelImpl.getInstance().getBookContent(durChapterUrl, durChapterIndex);
        } else if (tag.equals(LingdiankanshuStationBookModelImpl.TAG)) {
            return LingdiankanshuStationBookModelImpl.getInstance().getBookContent(durChapterUrl, durChapterIndex);
        } else if (tag.equals(ContentYb3ModelImpl.TAG)) {
            return ContentYb3ModelImpl.getInstance().getBookContent(durChapterUrl, durChapterIndex);
        } else if (tag.equals(ContentWxguanModelImpl.TAG)) {
            return ContentWxguanModelImpl.getInstance().getBookContent(durChapterUrl, durChapterIndex);
        } else
            return Observable.create(new ObservableOnSubscribe<BookContentBean>() {
                @Override
                public void subscribe(ObservableEmitter<BookContentBean> e) throws Exception {
                    e.onNext(new BookContentBean());
                    e.onComplete();
                }
            });
    }

    /**
     * 获取章节内容
     *
     * @param url
     * @return
     */
    public Single<ChapterInfoBean> getChapterInfo(String url) {
        Uri uri = Uri.parse(url);
        String TAG = uri.getScheme() + "://" + uri.getHost();
        Logger.d("Current website" + TAG);
        switch (TAG) {
            case ReaderContentWxguanModelImpl.TAG:
                return ReaderContentWxguanModelImpl.getInstance().getChapterInfo(url);
            case ReaderGxwztvBookModelImpl.TAG:
                return ReaderGxwztvBookModelImpl.getInstance().getChapterInfo(url);
            default:
                return null;
        }


    }

    /**
     * 其他站点集合搜索
     */
    public Observable<List<SearchBookBean>> searchOtherBook(String content, int page, String tag) {
        switch (tag) {
            case ReaderContentWxguanModelImpl.TAG:
                return ReaderContentWxguanModelImpl.getInstance().searchBook(content, page);
            case ReaderGxwztvBookModelImpl.TAG:
                return ReaderGxwztvBookModelImpl.getInstance().searchBook(content, page);
            default:
                return Observable.create(new ObservableOnSubscribe<List<SearchBookBean>>() {
                    @Override
                    public void subscribe(ObservableEmitter<List<SearchBookBean>> e) throws Exception {
                        e.onNext(new ArrayList<SearchBookBean>());
                        e.onComplete();
                    }
                });

        }

//        if (tag.equals(ContentAimanpinModeImpl.TAG)) {
//            return ContentAimanpinModeImpl.getInstance().searchBook(content, page);
//        } else if (tag.equals(GxwztvBookModelImpl.TAG)) {
//            return GxwztvBookModelImpl.getInstance().searchBook(content, page);
//        } else if (tag.equals(LingdiankanshuStationBookModelImpl.TAG)) {
//            return LingdiankanshuStationBookModelImpl.getInstance().searchBook(content, page);
//        } else if (tag.equals(ContentYb3ModelImpl.TAG)) {
//            return ContentYb3ModelImpl.getInstance().searchBook(content, page);
//        } else if (tag.equals(ContentWxguanModelImpl.TAG)) {
//            return ContentWxguanModelImpl.getInstance().searchBook(content, page);
//        } else {
//            return Observable.create(new ObservableOnSubscribe<List<SearchBookBean>>() {
//                @Override
//                public void subscribe(ObservableEmitter<List<SearchBookBean>> e) throws Exception {
//                    e.onNext(new ArrayList<SearchBookBean>());
//                    e.onComplete();
//                }
//            });
//        }
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * 新增jsoup分析网站需要这里进行注册
     *
     * @param searchEngine
     */
    public void registerSearchEngine(List<Map> searchEngine) {
        //搜索引擎初始化
        newSearchEngine(searchEngine, ContentAimanpinModeImpl.TAG);
        newSearchEngine(searchEngine, ReaderContentWxguanModelImpl.TAG);

        newSearchEngine(searchEngine, ContentWxguanModelImpl.TAG);

        newSearchEngine(searchEngine, GxwztvBookModelImpl.TAG);
        newSearchEngine(searchEngine, LingdiankanshuStationBookModelImpl.TAG);
        newSearchEngine(searchEngine, ContentYb3ModelImpl.TAG);

    }

    private void newSearchEngine(List<Map> searchEngine, String ImplTAG) {
        Map map = new HashMap();
        map.put(TAG_KEY, ImplTAG);
        searchEngine.add(map);
    }
}
