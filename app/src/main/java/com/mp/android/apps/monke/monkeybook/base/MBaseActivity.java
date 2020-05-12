//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.mp.android.apps.monke.monkeybook.base;

import com.mp.android.apps.monke.basemvplib.IPresenter;
import com.mp.android.apps.monke.basemvplib.impl.BaseActivity;


public abstract class MBaseActivity<T extends IPresenter> extends BaseActivity<T> {
    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();

    }
}
