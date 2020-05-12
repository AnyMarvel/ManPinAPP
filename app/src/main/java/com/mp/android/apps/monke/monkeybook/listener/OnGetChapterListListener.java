//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.mp.android.apps.monke.monkeybook.listener;

import com.mp.android.apps.monke.monkeybook.bean.BookShelfBean;

public interface OnGetChapterListListener {
    public void success(BookShelfBean bookShelfBean);
    public void error();
}
