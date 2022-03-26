package com.mp.android.apps.book.presenter.impl;

import android.text.TextUtils;

import com.mp.android.apps.basemvplib.impl.BasePresenterImpl;
import com.mp.android.apps.book.base.observer.SimpleObserver;
import com.mp.android.apps.book.model.BookRankListModelImpl;
import com.mp.android.apps.book.presenter.IBookRankListPresenter;
import com.mp.android.apps.book.view.IBookRankListView;
import com.mp.android.apps.main.bookR.model.IBookRFragmentModelImpl;
import com.mp.android.apps.main.home.bean.SourceListContent;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class BookRankListPresenterImpl extends BasePresenterImpl<IBookRankListView> implements IBookRankListPresenter {

    @Override
    public void detachView() {

    }

    @Override
    public void initBookRankListData(String routePath) {
        getRankListContent(routePath,1);
    }

    @Override
    public void getNextPageContent(String routePath,int pageNumber) {
        getRankListContent(routePath,pageNumber);
    }

    private void getRankListContent(String routePath,int pageNumber){
        if (!TextUtils.isEmpty(routePath)){
            BookRankListModelImpl.getInstance().getBookRankList(routePath,pageNumber)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SimpleObserver<String>() {
                        @Override
                        public void onNext(String s) {
                            analysisQidianRankList(s,pageNumber);
                        }

                        @Override
                        public void onError(Throwable e) {
                            if (pageNumber==1){
                                mView.showError(pageNumber);
                            }
                        }
                    });
        }
    }

    /**
     * https://www.qidian.com/rank/recom/
     * 分析起点中文网 推荐榜
     * @param s
     */
    private void analysisQidianRankList(String s,int pageNumber){
        try {
            Document doc = Jsoup.parse(s);
            Element element=doc.getElementById("rank-view-list");
            Elements bookNodes=element.getElementsByTag("li");
            List<SourceListContent> contentList=new ArrayList<>();
            if (bookNodes!=null && bookNodes.size()>0){
                for (int i = 0; i < bookNodes.size(); i++) {
                    SourceListContent sourceListContent=new SourceListContent();
                    Element bookNode=bookNodes.get(i);
                    Element bookNodeInfo=bookNode.getElementsByClass("book-mid-info").get(0);
                    String corveImage="https:"+bookNode.getElementsByClass("book-img-box").get(0).getElementsByTag("a").get(0).
                            getElementsByTag("img").get(0).
                            attr("src");
                    sourceListContent.setCoverUrl(corveImage);
                    sourceListContent.setName(bookNodeInfo.getElementsByTag("a").get(0).text());
                    sourceListContent.setBookdesc(bookNodeInfo.getElementsByClass("intro").get(0).text());
                    contentList.add(sourceListContent);
                }
                mView.notifyRecyclerView(contentList,false,pageNumber);

            }

        }catch (Exception e){

        }
    }


}
