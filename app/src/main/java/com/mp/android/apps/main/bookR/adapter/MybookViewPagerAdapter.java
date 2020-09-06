package com.mp.android.apps.main.bookR.adapter;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.mp.android.apps.monke.basemvplib.impl.BaseFragment;

import java.util.List;

/**
 * MybookViewPagerAdapter简介
 *
 * @author lijuntao
 * @date 2020-06-30 16:01
 */
public class MybookViewPagerAdapter extends FragmentPagerAdapter {
    private List<BaseFragment> list;

    public MybookViewPagerAdapter(FragmentManager fm, List<BaseFragment> list) {
        super(fm);
        this.list = list;
    }

    @Override
    public Fragment getItem(int position) {
        return list.get(position);
    }

    @Override
    public int getCount() {
        return list.size();
    }
}