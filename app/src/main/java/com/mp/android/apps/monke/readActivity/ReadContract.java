package com.mp.android.apps.monke.readActivity;


import com.mp.android.apps.monke.readActivity.base.BaseContract;
import com.mp.android.apps.monke.readActivity.bean.BookChapterBean;
import com.mp.android.apps.monke.readActivity.view.TxtChapter;

import java.util.List;

/**
 * Created by newbiechen on 17-5-16.
 */

public interface ReadContract extends BaseContract {
    interface View extends BaseContract.BaseView {
        void showCategory(List<BookChapterBean> bookChapterList);
        void finishChapter();
        void errorChapter();
    }

    interface Presenter extends BaseContract.BasePresenter<View>{
        void loadCategory(String bookId);
        void loadChapter(String bookId,List<TxtChapter> bookChapterList);
    }
}
