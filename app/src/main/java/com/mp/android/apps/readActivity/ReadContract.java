package com.mp.android.apps.readActivity;


import com.mp.android.apps.readActivity.base.BaseContract;
import com.mp.android.apps.readActivity.bean.BookChapterBean;
import com.mp.android.apps.readActivity.bean.CollBookBean;
import com.mp.android.apps.readActivity.view.TxtChapter;

import java.util.List;

public interface ReadContract extends BaseContract {
    interface View extends BaseContract.BaseView {
        void showCategory(List<BookChapterBean> bookChapterList);
        void finishChapter();
        void errorChapter();
    }

    interface Presenter extends BaseContract.BasePresenter<View>{
        void loadCategory(CollBookBean collBookBean);
        void loadChapter(String bookId,List<TxtChapter> bookChapterList);
    }
}
