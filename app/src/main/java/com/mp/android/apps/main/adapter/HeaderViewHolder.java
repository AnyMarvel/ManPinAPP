package com.mp.android.apps.main.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;

import com.mp.android.apps.R;
import com.mp.android.apps.main.cycleimage.CycleViewPager;
import com.mp.android.apps.main.view.MyImageTextView;

public class HeaderViewHolder extends RecyclerView.ViewHolder {
    /**
     * 轮播图
     */
    public CycleViewPager mCycleViewPager;
    public MyImageTextView dongman;
    public MyImageTextView mingxinpian;
    public MyImageTextView xiaoshuo;
    public MyImageTextView guangchang;
    public FrameLayout searchImage;


    public HeaderViewHolder(@NonNull View itemView) {
        super(itemView);
        mCycleViewPager = (CycleViewPager) itemView.findViewById(R.id.cycle_view);
        //导航设置点击事件
        dongman = itemView.findViewById(R.id.huojian);
//        dongman.setOnClickListener(this);
        mingxinpian = itemView.findViewById(R.id.jingxuan);
//        mingxinpian.setOnClickListener(this);
        xiaoshuo = itemView.findViewById(R.id.xiaoshuo);
//        xiaoshuo.setOnClickListener(this);
        guangchang = itemView.findViewById(R.id.guangchang);
//        guangchang.setOnClickListener(this);
        searchImage = itemView.findViewById(R.id.search_image);
//        searchImage.setOnClickListener(this);
    }
}
