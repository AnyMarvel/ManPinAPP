
package com.mp.android.apps.book.model;

import android.content.Context;
import android.net.Uri;

import com.mp.android.apps.book.bean.SearchBookBean;
import com.mp.android.apps.book.model.impl.ContentAimanpinModeImpl;

import com.mp.android.apps.book.model.impl.Content3040ModelImpl;
import com.mp.android.apps.book.model.impl.ContentLingDianModelImpl;
import com.mp.android.apps.book.model.impl.ContentSoDuModelImpl;
import com.mp.android.apps.book.model.impl.ContentTaduImpl;
import com.mp.android.apps.book.model.impl.ContentXXBiQuGeModelImpl;
import com.mp.android.apps.book.model.impl.ContentYb3ModelImpl;
import com.mp.android.apps.book.model.impl.TXSBookModelImpl;
import com.mp.android.apps.readActivity.bean.BookChapterBean;
import com.mp.android.apps.readActivity.bean.ChapterInfoBean;
import com.mp.android.apps.readActivity.bean.CollBookBean;
import com.mp.android.apps.utils.Logger;
import com.mp.android.apps.utils.SharedPreferenceUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.functions.Function;

import static com.mp.android.apps.book.presenter.impl.SearchPresenterImpl.TAG_KEY;

/**
 * 图书内容获取 加载，解析，章节处理，内容处理等问题
 * 引擎增加只需要修改initModels 添加实体类，减少代码建设量
 */
public class WebBookModelControl {
    /**
     * 解析引擎队列
     */
    private List<IReaderBookModel> models = new ArrayList<>();


    private static WebBookModelControl webBookModel;

    private WebBookModelControl() {

    }

    public static WebBookModelControl getInstance() {
        if (webBookModel == null) {
            synchronized (WebBookModelControl.class) {
                if (webBookModel == null) {
                    webBookModel = new WebBookModelControl();
                    webBookModel.initModels();
                }
            }
        }
        return webBookModel;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 增加引擎
     */
    private void initModels() {
//        models.add(ContentAimanpinModeImpl.getInstance());
        //只具有搜索功能的下发服务器,为用户点击的上报数据
        models.add(ContentSoDuModelImpl.getInstance());
        models.add(ContentXXBiQuGeModelImpl.getInstance());
        models.add(Content3040ModelImpl.getInstance());
        models.add(TXSBookModelImpl.getInstance());
        models.add(ContentYb3ModelImpl.getInstance());
        models.add(ContentTaduImpl.getInstance());
        models.add(ContentLingDianModelImpl.getInstance());


    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public Observable<CollBookBean> getBookInfo(CollBookBean collBookBean) {
        for (IReaderBookModel model : models) {
            if (model.getTAG().equals(collBookBean.getBookTag())) {
                return model.getBookInfo(collBookBean);
            }
        }
        return null;
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

        for (IReaderBookModel model : models) {
            if (model.getTAG().equals(TAG)) {
               return model.getBookChapters(collBookBean).map(new Function<List<BookChapterBean>, List<BookChapterBean>>() {
                    @Override
                    public List<BookChapterBean> apply(List<BookChapterBean> bookChapterBeans) throws Exception {
                        return removeDuplication(bookChapterBeans);
                    }
                });
            }
        }
        return null;
    }
    private List<BookChapterBean> removeDuplication( List<BookChapterBean> bookChapterSource){
        CopyOnWriteArrayList<BookChapterBean> result=new CopyOnWriteArrayList<>();
        for (int i = 0; i < bookChapterSource.size(); i++) {

            Iterator<BookChapterBean> iterator = result.iterator();
            while (iterator.hasNext()){
                BookChapterBean bookTemp=iterator.next();
                if (bookTemp.getId().equals(bookChapterSource.get(i).getId())){
                    result.remove(bookTemp);
                }
            }
            result.add(bookChapterSource.get(i));
        }
        for (int i = 0; i < result.size(); i++) {
            result.get(i).setPosition(i);
        }

        return result;
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

        for (IReaderBookModel model : models) {
            if (model.getTAG().equals(TAG)) {
                return model.getChapterInfo(url);
            }
        }
        return null;

    }

    /**
     * 其他站点集合搜索
     */
    public Observable<List<SearchBookBean>> searchOtherBook(String content, int page, String tag) {

        for (IReaderBookModel model : models) {
            if (model.getTAG().equals(tag)) {
                return model.searchBook(content, page);
            }
        }

        return Observable.create(new ObservableOnSubscribe<List<SearchBookBean>>() {
            @Override
            public void subscribe(ObservableEmitter<List<SearchBookBean>> e) throws Exception {
                e.onNext(new ArrayList<SearchBookBean>());
                e.onComplete();
            }
        });

    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * 新增jsoup分析网站需要这里进行注册
     * 基于用户选择书源对书籍搜索进行动态管理
     *
     * @param searchEngine
     */
    public void registerSearchEngine(List<Map<String,String>> searchEngine, Context context) {

        //搜索引擎初始化
        for (IReaderBookModel model : models) {
            if ((Boolean) SharedPreferenceUtil.get(context, model.getTAG(), false)) {
                newSearchEngine(searchEngine, model.getTAG());
            }
        }
    }

    private void newSearchEngine(List<Map<String,String>> searchEngine, String ImplTAG) {
        Map<String,String> map = new HashMap<String,String>();
        map.put(TAG_KEY, ImplTAG);
        searchEngine.add(map);
    }
}
