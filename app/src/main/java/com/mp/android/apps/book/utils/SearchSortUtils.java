package com.mp.android.apps.book.utils;

//import com.google.common.collect.EnumMultiset;
import com.mp.android.apps.book.bean.SearchBookBean;

import java.util.ArrayList;
import java.util.List;

import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.ToStringFunction;
import me.xdrop.fuzzywuzzy.model.BoundExtractedResult;


public class SearchSortUtils {
    public static List<SearchBookBean> filerSearchTools(List<SearchBookBean> searchBookBeans, String filter) {
        if (searchBookBeans == null || searchBookBeans.isEmpty()) {
            return new ArrayList<>();
        }
        // filter 为用户输入，作为模糊匹配的关键字，null 时按空串处理
        return searchSort(searchBookBeans, filter == null ? "" : filter);
    }


    static List<SearchBookBean> searchSort(List<SearchBookBean> searchBookBeans, String filter) {
        List<BoundExtractedResult<SearchBookBean>> list = FuzzySearch.extractSorted(filter, searchBookBeans, new ToStringFunction<SearchBookBean>() {
            @Override
            public String apply(SearchBookBean item) {
                // 书名可能为空，避免模糊匹配时空指针
                return item == null || item.getName() == null ? "" : item.getName();
            }
        });
        List<SearchBookBean> newList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            newList.add(list.get(i).getReferent());
        }
        return newList;
    }


}
