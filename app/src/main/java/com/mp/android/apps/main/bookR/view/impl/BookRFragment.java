package com.mp.android.apps.main.bookR.view.impl;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.viewpager.widget.ViewPager;

import com.mp.android.apps.R;
import com.mp.android.apps.main.bookR.adapter.MybookViewPagerAdapter;
import com.mp.android.apps.main.bookR.presenter.IBookRFragmentPresenter;
import com.mp.android.apps.main.bookR.presenter.impl.BookRFragmentPresenterImpl;
import com.mp.android.apps.main.bookR.view.IBookRFragmentView;
import com.mp.android.apps.monke.basemvplib.impl.BaseFragment;

import java.util.ArrayList;
import java.util.List;

public class BookRFragment extends BaseFragment<IBookRFragmentPresenter> implements IBookRFragmentView {
    TextView layoutRecommend;
    TextView layoutMan;
    TextView layoutWomen;
    ViewPager viewPager;
    List<BaseFragment> sourceList;
    /**
     * 推荐fragment
     */
    BookRRecommendFImpl recommendFagment;

    /**
     * manFragment 男士专区
     */
    BookRManFImpl manFragment;

    /**
     * womanFragmen 女士专区
     */
    BookRWomanFImpl womanFragmen;

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
        layoutMan = view.findViewById(R.id.mp_bookr_layout_men);
        layoutWomen = view.findViewById(R.id.mp_bookr_layout_women);
        viewPager = view.findViewById(R.id.mp_bookr_viewpager);

    }

    @Override
    protected void bindEvent() {
        super.bindEvent();
        sourceList.add(recommendFagment);
        sourceList.add(manFragment);
        sourceList.add(womanFragmen);
        viewPager.setAdapter(new MybookViewPagerAdapter(getFragmentManager(), sourceList));
    }

    @Override
    protected IBookRFragmentPresenter initInjector() {
        return new BookRFragmentPresenterImpl();
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.mp_book_r_layout, container, false);
    }

}
