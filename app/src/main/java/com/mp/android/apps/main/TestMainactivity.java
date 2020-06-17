package com.mp.android.apps.main;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.mp.android.apps.R;
import com.mp.android.apps.StoryboardActivity;

public class TestMainactivity extends StoryboardActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manfragment_recycle_item);
    }
}
