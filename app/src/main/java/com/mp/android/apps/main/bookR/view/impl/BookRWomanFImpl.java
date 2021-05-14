package com.mp.android.apps.main.bookR.view.impl;

public class BookRWomanFImpl extends BookRManFImpl {

    @Override
    public void initLocalData() {
        mPresenter.initWoManData();
    }

    @Override
    public void setClassicRecommendTitle(String title) {
        super.setClassicRecommendTitle("女生推荐");
    }
}
