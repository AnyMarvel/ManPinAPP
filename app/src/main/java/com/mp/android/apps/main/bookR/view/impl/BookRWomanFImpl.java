package com.mp.android.apps.main.bookR.view.impl;

import android.content.Intent;
import android.view.View;

import com.mp.android.apps.R;
import com.mp.android.apps.book.view.impl.BookRankListActivity;

public class BookRWomanFImpl extends BookRManFImpl {

    @Override
    public void initLocalData() {
        mPresenter.initWoManData();
    }

    @Override
    public void setClassicRecommendTitle(String title) {
        super.setClassicRecommendTitle("女生推荐");
    }

    @Override
    public String recommendTitle() {
        return "女生榜";
    }

    @Override
    public String collectionTitle() {
        return "粉丝榜";
    }

    @Override
    public void onItemClickListener(View view) {
        int id=view.getId();
        Intent intent=new Intent(getActivity(), BookRankListActivity.class);
        switch (id){
            case R.id.mp_bookr_recommend_category:
                intent.putExtra("rankRouteUrl",BookRankListActivity.RANKWOMENRECOM);
                break;
            case R.id.mp_bookr_recommend_ranking:
                intent.putExtra("rankRouteUrl",BookRankListActivity.RANKWOMENCOLLECT);
                break;
        }

        startActivity(intent);
    }
}
