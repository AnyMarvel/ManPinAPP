package com.mp.android.apps.main;

import android.os.Bundle;
import android.support.annotation.Nullable;


import com.mp.android.apps.R;
import com.mp.android.apps.StoryboardActivity;

public class MainActivity extends StoryboardActivity {
    MainFragment mainFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        mainFragment = new MainFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.main_contain, mainFragment).commit();
    }
}
