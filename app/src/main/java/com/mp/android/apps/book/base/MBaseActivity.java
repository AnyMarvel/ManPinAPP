
package com.mp.android.apps.book.base;

import com.mp.android.apps.base.IPresenter;
import com.mp.android.apps.base.impl.BaseActivity;


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
