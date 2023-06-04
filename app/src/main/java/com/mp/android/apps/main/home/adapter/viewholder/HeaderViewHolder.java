package com.mp.android.apps.main.home.adapter.viewholder;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;

import com.mp.android.apps.R;
import com.mp.android.apps.main.home.adapter.OnHomeAdapterClickListener;
import com.mp.android.apps.main.home.cycleimage.BannerInfo;
import com.mp.android.apps.main.home.cycleimage.CycleViewPager;
import com.mp.android.apps.main.home.view.MyImageTextView;
import com.mp.android.apps.main.home.view.impl.OnMainFragmentClickListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HeaderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    /**
     * 轮播图
     */
    private CycleViewPager mCycleViewPager;
    private MyImageTextView dongman;
    private MyImageTextView mingxinpian;
    private MyImageTextView xiaoshuo;
    private MyImageTextView guangchang;
    private FrameLayout searchImage;
    OnMainFragmentClickListener listener;

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

    public void handleClassicRecommendEvent(List<Map<String, String>> carouselList, OnMainFragmentClickListener listener) {
        this.listener = listener;
        dongman.setOnClickListener(this);
        mingxinpian.setOnClickListener(this);
        xiaoshuo.setOnClickListener(this);
        guangchang.setOnClickListener(this);
        searchImage.setOnClickListener(this);
        updatemCycleViewPager(carouselList);
    }

    public void updatemCycleViewPager(List<Map<String, String>> carouselList) {
        List<BannerInfo> mList = new ArrayList<>();

        for (int i = 0; i < carouselList.size(); i++) {
            mList.add(new BannerInfo(carouselList.get(i).get("name"), carouselList.get(i).get("url")));
        }

        //设置选中和未选中时的图片
        assert mCycleViewPager != null;
        mCycleViewPager.setIndicators(R.mipmap.ad_select, R.mipmap.ad_unselect);
        mCycleViewPager.setDelay(3000);
        mCycleViewPager.setData(mList, new CycleViewPager.ImageCycleViewListener() {
            @Override
            public void onImageClick(BannerInfo info, int position, View imageView) {

                if (mCycleViewPager.isCycle()) {
                    position = position - 1;
                }
                listener.onLayoutClickListener(info.getTitle());
            }
        });
    }

    @Override
    public void onClick(View v) {
        listener.onItemClickListener(v);
    }
}
