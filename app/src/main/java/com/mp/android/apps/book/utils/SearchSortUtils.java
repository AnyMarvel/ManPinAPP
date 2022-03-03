package com.mp.android.apps.book.utils;

import com.google.common.collect.EnumMultiset;
import com.mp.android.apps.book.bean.SearchBookBean;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.ToStringFunction;
import me.xdrop.fuzzywuzzy.model.BoundExtractedResult;

/**
 * @author ShuiYu
 * Created on 3/2/22
 * Copyright © 2022 Alibaba-inc. All rights reserved.
 */

public class SearchSortUtils {
    public static List<SearchBookBean> filerSearchTools(List<SearchBookBean> searchBookBeans,String filter){
        List<SearchBookBean> newList=new ArrayList<>();
        for (SearchBookBean searchTemp:searchBookBeans) {
            if (specifyStringFiltering(filter,searchTemp.getName())){
                newList.add(searchTemp);
            }
        }
        return searchSort(searchBookBeans,filter);

    }


    static boolean specifyStringFiltering(String filter,String target) {
        return Pattern.matches(".*("+filter+").*", target);
    }


    static List<SearchBookBean> searchSort(List<SearchBookBean> searchBookBeans,String filter){
        List<BoundExtractedResult<SearchBookBean>>  list= FuzzySearch.extractSorted(filter, searchBookBeans, new ToStringFunction<SearchBookBean>() {
            @Override
            public String apply(SearchBookBean item) {
                return item.getName();
            }
        });
        List<SearchBookBean> newList=new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            newList.add(list.get(i).getReferent());
        }
        return newList;
    }



}
