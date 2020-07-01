package com.mp.android.apps.main.bookR.view.impl;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.viewpager.widget.ViewPager;

import com.lwj.widget.viewpagerindicator.ViewPagerIndicator;
import com.mp.android.apps.R;
import com.mp.android.apps.main.bookR.adapter.MybookViewPagerAdapter;
import com.mp.android.apps.main.bookR.presenter.IBookRFragmentPresenter;
import com.mp.android.apps.main.bookR.presenter.impl.BookRFragmentPresenterImpl;
import com.mp.android.apps.main.bookR.view.IBookRFragmentView;
import com.mp.android.apps.monke.basemvplib.impl.BaseFragment;
import com.mp.android.apps.monke.monkeybook.widget.refreshview.BaseRefreshListener;

import java.util.ArrayList;
import java.util.List;

public class BookRFragment extends BaseFragment<IBookRFragmentPresenter> implements IBookRFragmentView, View.OnClickListener {
    public TextView layoutRecommend;
    public TextView layoutMan;
    public TextView layoutWomen;
    private ViewPager viewPager;
    private List<BaseFragment> sourceList;
    ViewPagerIndicator viewPagerIndicator;
    /**
     * 推荐fragment
     */
    private BookRRecommendFImpl recommendFagment;

    /**
     * manFragment 男士专区
     */
    private BookRManFImpl manFragment;

    /**
     * womanFragmen 女士专区
     */
    private BookRWomanFImpl womanFragmen;

    private static final int RECOMMENDFRAGMENT = 0;
    private static final int MANFRAGMENT = 1;
    private static final int WOMANFRAGMENT = 2;

    @Override
    protected void initData() {
        super.initData();
        sourceList = new ArrayList<>();
        recommendFagment = new BookRRecommendFImpl();
        manFragment = new BookRManFImpl();
        womanFragmen = new BookRWomanFImpl();
    }

    @Override
    protected void bindView() {
        super.bindView();
        layoutRecommend = view.findViewById(R.id.mp_bookr_layout_recommend);
        layoutRecommend.setOnClickListener(this);
        layoutMan = view.findViewById(R.id.mp_bookr_layout_men);
        layoutMan.setOnClickListener(this);
        layoutWomen = view.findViewById(R.id.mp_bookr_layout_women);
        layoutWomen.setOnClickListener(this);
        viewPager = view.findViewById(R.id.mp_bookr_viewpager);
        viewPagerIndicator = view.findViewById(R.id.indicator_circle_line);


    }

    @Override
    protected void bindEvent() {
        super.bindEvent();
        sourceList.add(recommendFagment);
        sourceList.add(manFragment);
        sourceList.add(womanFragmen);
        viewPager.setAdapter(new MybookViewPagerAdapter(getFragmentManager(), sourceList));
        viewPager.addOnPageChangeListener(new MybookViewPageChangeListener());
        viewPagerIndicator.setViewPager(viewPager, sourceList.size());
    }

    @Override
    protected IBookRFragmentPresenter initInjector() {
        return new BookRFragmentPresenterImpl();
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.mp_book_r_layout, container, false);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.mp_bookr_layout_recommend:
                viewPager.setCurrentItem(RECOMMENDFRAGMENT);
                break;
            case R.id.mp_bookr_layout_men:
                viewPager.setCurrentItem(MANFRAGMENT);
                break;
            case R.id.mp_bookr_layout_women:
                viewPager.setCurrentItem(WOMANFRAGMENT);
                break;
            default:
                break;
        }
    }

    private class MybookViewPageChangeListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            switch (position) {
                case RECOMMENDFRAGMENT:
                    setSelectPageTextSize(layoutRecommend);
                    break;
                case MANFRAGMENT:
                    setSelectPageTextSize(layoutMan);
                    break;
                case WOMANFRAGMENT:
                    setSelectPageTextSize(layoutWomen);
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }

        private void setSelectPageTextSize(TextView text) {
            layoutRecommend.setTextSize(20);
            layoutMan.setTextSize(20);
            layoutWomen.setTextSize(20);
            text.setTextSize(26);
        }
    }
}
