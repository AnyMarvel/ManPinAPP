package com.mp.android.apps.monke.monkeybook.utils;

import android.content.Context;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.mp.android.apps.monke.monkeybook.bean.BookSourceBean;
import com.mp.android.apps.utils.AssertFileUtils;
import com.mp.android.apps.utils.SharedPreferenceUtil;

import java.util.ArrayList;
import java.util.List;

public class BookSourceCheckUtils {

    public static boolean bookSourceSwitch(Context context) {
        List<BookSourceBean> sourceBeans = new ArrayList<>();

        //    图书源数据源
        String localBookSource = AssertFileUtils.getJson(context, "booksource.json");
        if (!TextUtils.isEmpty(localBookSource)) {
            sourceBeans = JSON.parseArray(localBookSource, BookSourceBean.class);
        }
        boolean sourceSwitch = false;
        if (sourceBeans != null && sourceBeans.size() > 0) {
            for (BookSourceBean sourceBean : sourceBeans) {
                if ((boolean) SharedPreferenceUtil.get(context, sourceBean.getBookSourceAddress(), false)) {
                    sourceSwitch = true;
                }
            }
        }
        return sourceSwitch;
    }
}
