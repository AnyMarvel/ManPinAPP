
package com.mp.android.apps.monke.monkeybook.model.impl;

import android.net.Uri;

import com.mp.android.apps.monke.monkeybook.bean.BookContentBean;
import com.mp.android.apps.monke.monkeybook.bean.BookShelfBean;
import com.mp.android.apps.monke.monkeybook.bean.SearchBookBean;
import com.mp.android.apps.monke.monkeybook.listener.OnGetChapterListListener;
import com.mp.android.apps.monke.monkeybook.model.IWebBookModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

import static com.mp.android.apps.monke.monkeybook.presenter.impl.SearchPresenterImpl.TAG_KEY;

public class WebBookModelImpl implements IWebBookModel {

    public static WebBookModelImpl getInstance() {
        return new WebBookModelImpl();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 网络请求并解析书籍信息
     * return BookShelfBean
     */
    @Override
    public Observable<BookShelfBean> getBookInfo(BookShelfBean bookShelfBean) {
        switch (bookShelfBean.getTag()) {
            case GxwztvBookModelImpl.TAG:
                return GxwztvBookModelImpl.getInstance().getBookInfo(bookShelfBean);
            case LingdiankanshuStationBookModelImpl.TAG:
                return LingdiankanshuStationBookModelImpl.getInstance().getBookInfo(bookShelfBean);
            case ContentYb3ModelImpl.TAG:
                return ContentYb3ModelImpl.getInstance().getBookInfo(bookShelfBean);
            case ContentWxguanModelImpl.TAG:
                return ContentWxguanModelImpl.getInstance().getBookInfo(bookShelfBean);
            default:
                return null;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 网络解析图书目录
     * return BookShelfBean
     */
    @Override
    public void getChapterList(final BookShelfBean bookShelfBean, OnGetChapterListListener getChapterListListener) {
        if (bookShelfBean.getTag().equals(GxwztvBookModelImpl.TAG)) {
            GxwztvBookModelImpl.getInstance().getChapterList(bookShelfBean, getChapterListListener);
        } else if (bookShelfBean.getTag().equals(LingdiankanshuStationBookModelImpl.TAG)) {
            LingdiankanshuStationBookModelImpl.getInstance().getChapterList(bookShelfBean, getChapterListListener);
        } else if (bookShelfBean.getTag().equals(ContentYb3ModelImpl.TAG)) {
            ContentYb3ModelImpl.getInstance().getChapterList(bookShelfBean, getChapterListListener);
        } else if (bookShelfBean.getTag().equals(ContentWxguanModelImpl.TAG)) {
            ContentWxguanModelImpl.getInstance().getChapterList(bookShelfBean, getChapterListListener);
        } else {
            if (getChapterListListener != null)
                getChapterListListener.success(bookShelfBean);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 章节缓存
     */
    @Override
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
     * 其他站点集合搜索
     */
    @Override
    public Observable<List<SearchBookBean>> searchOtherBook(String content, int page, String tag) {
        if (tag.equals(ContentAimanpinModeImpl.TAG)) {
            return ContentAimanpinModeImpl.getInstance().searchBook(content, page);
        } else if (tag.equals(GxwztvBookModelImpl.TAG)) {
            return GxwztvBookModelImpl.getInstance().searchBook(content, page);
        } else if (tag.equals(LingdiankanshuStationBookModelImpl.TAG)) {
            return LingdiankanshuStationBookModelImpl.getInstance().searchBook(content, page);
        } else if (tag.equals(ContentYb3ModelImpl.TAG)) {
            return ContentYb3ModelImpl.getInstance().searchBook(content, page);
        } else if (tag.equals(ContentWxguanModelImpl.TAG)) {
            return ContentWxguanModelImpl.getInstance().searchBook(content, page);
        } else {
            return Observable.create(new ObservableOnSubscribe<List<SearchBookBean>>() {
                @Override
                public void subscribe(ObservableEmitter<List<SearchBookBean>> e) throws Exception {
                    e.onNext(new ArrayList<SearchBookBean>());
                    e.onComplete();
                }
            });
        }
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 点击事件获取分类书籍
     */
    @Override
    public Observable<List<SearchBookBean>> getKindBook(String url, int page) {
        Uri uri = Uri.parse(url);
        String tag = uri.getScheme() + "://" + uri.getHost();
        if (tag.equals(GxwztvBookModelImpl.TAG)) {
            return GxwztvBookModelImpl.getInstance().getKindBook(url, page);
        } else if (tag.equals(LingdiankanshuStationBookModelImpl.TAG)) {
            return LingdiankanshuStationBookModelImpl.getInstance().getKindBook(url, page);
        } else {
            return GxwztvBookModelImpl.getInstance().getKindBook(url, page);
        }
    }



    /**
     * 新增jsoup分析网站需要这里进行注册
     *
     * @param searchEngine
     */
    public void registerSearchEngine(List<Map> searchEngine) {
        //搜索引擎初始化
        newSearchEngine(searchEngine, ContentAimanpinModeImpl.TAG);
        newSearchEngine(searchEngine, GxwztvBookModelImpl.TAG);
        newSearchEngine(searchEngine, LingdiankanshuStationBookModelImpl.TAG);
        newSearchEngine(searchEngine, ContentYb3ModelImpl.TAG);
        newSearchEngine(searchEngine, ContentWxguanModelImpl.TAG);
    }

    private void newSearchEngine(List<Map> searchEngine, String ImplTAG) {
        Map map = new HashMap();
        map.put(TAG_KEY, ImplTAG);
        searchEngine.add(map);
    }
}
