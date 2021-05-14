
package com.mp.android.apps.book.base;

import com.mp.android.apps.basemvplib.IPresenter;
import com.mp.android.apps.basemvplib.impl.BaseActivity;


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
