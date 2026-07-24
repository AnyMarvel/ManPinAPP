package com.mp.android.apps.read;


import com.mp.android.apps.read.base.BaseContract;
import com.mp.android.apps.read.bean.BookChapterBean;
import com.mp.android.apps.read.bean.CollBookBean;
import com.mp.android.apps.read.view.TxtChapter;

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
