package com.mp.android.apps.main.bookR.view.impl;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.mp.android.apps.R;
import com.mp.android.apps.main.bookR.adapter.MybookViewPagerAdapter;
import com.mp.android.apps.main.bookR.presenter.IBookRFragmentPresenter;
import com.mp.android.apps.main.bookR.presenter.impl.BookRFragmentPresenterImpl;
import com.mp.android.apps.main.bookR.view.IBookRFragmentView;
import com.mp.android.apps.basemvplib.impl.BaseActivity;
import com.mp.android.apps.basemvplib.impl.BaseFragment;
import com.mp.android.apps.book.view.impl.SearchActivity;

import java.util.ArrayList;
import java.util.List;

public class BookRActivity extends BaseActivity<IBookRFragmentPresenter> implements IBookRFragmentView, View.OnClickListener {
    public TextView layoutRecommend;
    public TextView layoutMan;
    public TextView layoutWomen;
    private ViewPager viewPager;
    private List<BaseFragment> sourceList;
    private ImageView searchImage;
    private ImageView ivBack;

    private TabLayout viewPagerIndicator;
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
        sourceList = new ArrayList<>();
        recommendFagment = new BookRRecommendFImpl();
        manFragment = new BookRManFImpl();
        womanFragmen = new BookRWomanFImpl();
    }

    @Override
    protected void bindView() {
        super.bindView();
        layoutRecommend = findViewById(R.id.mp_bookr_layout_recommend);
        layoutRecommend.setOnClickListener(this);
        layoutMan = findViewById(R.id.mp_bookr_layout_men);
        layoutMan.setOnClickListener(this);
        layoutWomen = findViewById(R.id.mp_bookr_layout_women);
        layoutWomen.setOnClickListener(this);
        viewPager = findViewById(R.id.mp_bookr_viewpager);
        viewPagerIndicator = findViewById(R.id.tablayout);
        searchImage = findViewById(R.id.bookr_fragment_search);
        searchImage.setOnClickListener(this);
        ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(this);

    }

    @Override
    protected void bindEvent() {
        super.bindEvent();
        sourceList.add(recommendFagment);
        sourceList.add(manFragment);
        sourceList.add(womanFragmen);
        viewPager.setAdapter(new MybookViewPagerAdapter(getSupportFragmentManager(), sourceList));
        viewPager.setOffscreenPageLimit(3);
        viewPager.addOnPageChangeListener(new MybookViewPageChangeListener());
        //设置TabLayout的模式
        viewPagerIndicator.setTabMode(TabLayout.MODE_SCROLLABLE);
        //关联ViewPager和TabLayout
        viewPagerIndicator.setupWithViewPager(viewPager,false);

    }

    @Override
    protected IBookRFragmentPresenter initInjector() {
        return new BookRFragmentPresenterImpl();
    }

    @Override
    protected void onCreateActivity() {
        setContentView(R.layout.mp_book_r_layout);
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
            case R.id.bookr_fragment_search:
                Intent searchIntent = new Intent(this, SearchActivity.class);
                startActivity(searchIntent);
                break;
            case R.id.iv_back:
                super.onBackPressed();
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
