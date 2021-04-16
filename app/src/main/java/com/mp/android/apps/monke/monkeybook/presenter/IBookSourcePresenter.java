package com.mp.android.apps.monke.monkeybook.presenter;

import com.mp.android.apps.monke.basemvplib.IPresenter;
import com.mp.android.apps.monke.monkeybook.bean.BookSourceBean;

import java.util.List;

public interface IBookSourcePresenter extends IPresenter {
    /**
     * 处理 图书源数据源
     * 数据由本地json文件获取，获取后switch开关与本地sp进行合并，以本地存储值为准
     *
     * @param bookSource
     * @return 返回处理后的数据源
     */
    List<BookSourceBean> handleSource(List<BookSourceBean> bookSource);
}
