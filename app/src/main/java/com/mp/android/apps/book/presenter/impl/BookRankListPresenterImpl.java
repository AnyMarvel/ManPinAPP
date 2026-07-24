package com.mp.android.apps.book.presenter.impl;

import android.text.TextUtils;

import com.mp.android.apps.base.impl.BasePresenterImpl;
import com.mp.android.apps.book.base.observer.SimpleObserver;
import com.mp.android.apps.book.model.BookRankListModelImpl;
import com.mp.android.apps.book.presenter.IBookRankListPresenter;
import com.mp.android.apps.book.view.IBookRankListView;
import com.mp.android.apps.main.home.bean.SourceListContent;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

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
                            //网络失败：打日志暴露真因(域名被拦/超时等)，并让 UI 停止 loading
                            android.util.Log.e("BookRankList",
                                    "getRankListContent network error, page=" + pageNumber, e);
                            mView.showError(pageNumber);
                        }
                    });
        }
    }

    /**
     * 解析起点移动站 majax 榜单 JSON。
     * 结构(实测)：{"code":0,"data":{"total":..,"isLast":0,"pageNum":N,
     *   "records":[{"bid","bName","bAuth","desc","cat","cnt","rankCnt","rankNum"}, ...]}}
     * JSON 无封面字段，封面由 bid 拼：https://bookcover.yuewen.com/qdbimg/349573/{bid}/180
     * (349573 为起点在阅文图床的固定站点段，实测各 bid 通用)。
     */
    private void analysisQidianRankList(String s,int pageNumber){
        try {
            JSONObject root = JSONObject.parseObject(s);
            //code!=0(如缺 token 的 {"code":1,"msg":"失败"})或结构异常：打日志并停 loading、提示重试
            if (root == null || root.getIntValue("code") != 0) {
                android.util.Log.e("BookRankList",
                        "majax code!=0 (被拦截/参数问题)，resp=" + (s == null ? "null" : s.substring(0, Math.min(120, s.length()))));
                mView.showError(pageNumber);
                return;
            }
            JSONObject data = root.getJSONObject("data");
            JSONArray records = data == null ? null : data.getJSONArray("records");
            if (records == null || records.isEmpty()) {
                android.util.Log.e("BookRankList", "rank records empty, page=" + pageNumber);
                mView.showError(pageNumber);
                return;
            }
            List<SourceListContent> contentList = new ArrayList<>();
            for (int i = 0; i < records.size(); i++) {
                try {
                    JSONObject r = records.getJSONObject(i);
                    SourceListContent content = new SourceListContent();
                    content.setName(r.getString("bName"));
                    content.setBookdesc(r.getString("desc"));
                    String bid = r.getString("bid");
                    if (!TextUtils.isEmpty(bid)) {
                        content.setCoverUrl("https://bookcover.yuewen.com/qdbimg/349573/" + bid + "/180");
                    }
                    //书名为空的条目跳过，避免脏数据
                    if (!TextUtils.isEmpty(content.getName())) {
                        contentList.add(content);
                    }
                } catch (Exception itemEx) {
                    //单本解析失败不影响其余，跳过即可
                    android.util.Log.w("BookRankList", "parse one book record failed, skip", itemEx);
                }
            }

            if (contentList.isEmpty()) {
                android.util.Log.e("BookRankList", "rank list empty after parse, page=" + pageNumber);
                mView.showError(pageNumber);
                return;
            }
            mView.notifyRecyclerView(contentList, pageNumber);

        }catch (Exception e){
            //原来是空 catch 静默吞异常，是"永久转圈"的元凶之一
            android.util.Log.e("BookRankList", "parse qidian rank json failed, page=" + pageNumber, e);
            mView.showError(pageNumber);
        }
    }


}
