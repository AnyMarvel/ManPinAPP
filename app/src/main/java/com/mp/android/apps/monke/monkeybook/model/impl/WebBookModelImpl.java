
package com.mp.android.apps.monke.monkeybook.model.impl;

import android.content.Context;
import android.net.Uri;

import com.mp.android.apps.monke.monkeybook.bean.SearchBookBean;
import com.mp.android.apps.monke.readActivity.bean.BookChapterBean;
import com.mp.android.apps.monke.readActivity.bean.ChapterInfoBean;
import com.mp.android.apps.monke.readActivity.bean.CollBookBean;
import com.mp.android.apps.utils.Logger;
import com.mp.android.apps.utils.SharedPreferenceUtil;

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
            case TXSBookModelImpl.TAG:
                return TXSBookModelImpl.getInstance().getBookInfo(collBookBean);

            case ContentYb3ModelImpl.TAG:
                return ContentYb3ModelImpl.getInstance().getBookInfo(collBookBean);

            case ContentIdeaJainImpl.TAG:
                return ContentIdeaJainImpl.getInstance().getBookInfo(collBookBean);
            default:
                return null;
        }

    }

    /**
     * 根据 图书url获取章节目录
     *
     * @return
     */
    public Single<List<BookChapterBean>> getBookChapters(CollBookBean collBookBean) {

        Uri uri = Uri.parse(collBookBean.getBookChapterUrl());
        String TAG = uri.getScheme() + "://" + uri.getHost();
        Logger.d("Current website:  " + TAG);
        switch (TAG) {
            case TXSBookModelImpl.TAG:
                return TXSBookModelImpl.getInstance().getBookChapters(collBookBean);

            case ContentYb3ModelImpl.TAG:
                return ContentYb3ModelImpl.getInstance().getBookChapters(collBookBean);
            case ContentIdeaJainImpl.TAG:
                return ContentIdeaJainImpl.getInstance().getBookChapters(collBookBean);
            default:
                return null;
        }


    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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
            case TXSBookModelImpl.TAG:
                return TXSBookModelImpl.getInstance().getChapterInfo(url);

            case ContentYb3ModelImpl.TAG:
                return ContentYb3ModelImpl.getInstance().getChapterInfo(url);
            case ContentIdeaJainImpl.TAG:
                return ContentIdeaJainImpl.getInstance().getChapterInfo(url);
            default:
                return null;
        }


    }

    /**
     * 其他站点集合搜索
     */
    public Observable<List<SearchBookBean>> searchOtherBook(String content, int page, String tag) {
        switch (tag) {
            case TXSBookModelImpl.TAG:
                return TXSBookModelImpl.getInstance().searchBook(content, page);

            case ContentYb3ModelImpl.TAG:
                return ContentYb3ModelImpl.getInstance().searchBook(content, page);
            case ContentAimanpinModeImpl.TAG:
                return ContentAimanpinModeImpl.getInstance().searchBook(content, page);
            case ContentIdeaJainImpl.TAG:
                return ContentIdeaJainImpl.getInstance().searchBook(content, page);
            default:
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
     * 新增jsoup分析网站需要这里进行注册
     * 基于用户选择书源对书籍搜索进行动态管理
     *
     * @param searchEngine
     */
    public void registerSearchEngine(List<Map> searchEngine, Context context) {
        //搜索引擎初始化
        if ((Boolean) SharedPreferenceUtil.get(context, ContentAimanpinModeImpl.TAG, false))
            newSearchEngine(searchEngine, ContentAimanpinModeImpl.TAG);

        if ((Boolean) SharedPreferenceUtil.get(context, TXSBookModelImpl.TAG, false))
            newSearchEngine(searchEngine, TXSBookModelImpl.TAG);

        if ((Boolean) SharedPreferenceUtil.get(context, ContentYb3ModelImpl.TAG, false))
            newSearchEngine(searchEngine, ContentYb3ModelImpl.TAG);

        if ((Boolean) SharedPreferenceUtil.get(context, ContentIdeaJainImpl.TAG, false))
            newSearchEngine(searchEngine, ContentIdeaJainImpl.TAG);


    }

    private void newSearchEngine(List<Map> searchEngine, String ImplTAG) {
        Map map = new HashMap();
        map.put(TAG_KEY, ImplTAG);
        searchEngine.add(map);
    }
}
