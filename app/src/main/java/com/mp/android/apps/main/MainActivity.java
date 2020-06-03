package com.mp.android.apps.main;


import com.mp.android.apps.R;

import com.mp.android.apps.main.fragment.MainFragment;
import com.mp.android.apps.monke.basemvplib.IPresenter;
import com.mp.android.apps.monke.monkeybook.base.MBaseActivity;

public class MainActivity extends MBaseActivity {
    MainFragment mainFragment;

    @Override
    protected IPresenter initInjector() {
        return null;
    }

    @Override
    protected void onCreateActivity() {
        setContentView(R.layout.main_layout);
        mainFragment = new MainFragment();
        getFragmentManager().beginTransaction().replace(R.id.main_contain, mainFragment).commit();
    }

    @Override
    protected void initData() {

    }
}
