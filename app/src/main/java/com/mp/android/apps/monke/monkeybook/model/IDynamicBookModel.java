package com.mp.android.apps.monke.monkeybook.model;

import com.mp.android.apps.monke.monkeybook.bean.SearchBookBean;

import java.util.LinkedHashMap;
import java.util.List;

import io.reactivex.Observable;


public interface IDynamicBookModel extends IStationBookModel {
    /**
     * 获取导航数据
     *
     * @return 导航设计为谁快搜索谁
     */
    LinkedHashMap<String, String> getBookNavs();

    /**
     * 获取导航对应的搜索图书的jsoup分析
     *
     * @param url
     * @param page
     * @return 对应网站进行数据爬虫
     */
    Observable<List<SearchBookBean>> getKindBook(String url, int page);

}
