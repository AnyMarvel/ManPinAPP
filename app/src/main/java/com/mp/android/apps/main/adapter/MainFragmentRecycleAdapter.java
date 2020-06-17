package com.mp.android.apps.main.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.mp.android.apps.R;
import com.mp.android.apps.main.bean.HomeDesignBean;
import com.mp.android.apps.main.bean.SourceListContent;
import com.mp.android.apps.main.cycleimage.BannerInfo;
import com.mp.android.apps.main.cycleimage.CycleViewPager;
import com.mp.android.apps.utils.Logger;

import java.util.ArrayList;
import java.util.List;

public class MainFragmentRecycleAdapter extends RecyclerView.Adapter implements View.OnClickListener {
    private int mHeaderCount = 1;// 头部的数量
    private int mBottomCount = 1;// 底部的数量

    // 首先定义几个常量标记item的类型
    public static final int ITEM_TYPE_HEADER = 0;
    public static final int ITEM_TYPE_CONTENT = 1;
    public static final int ITEM_TYPE_BOTTOM = 2;

    private Context context;
    private List<HomeDesignBean> list;
    //中间内容位置信息
    int mContentPosition;
    //底部view位置信息
    int mBottomPosition;
    OnHomeAdapterClickListener listener;
    List<String> carouselImages;

    public MainFragmentRecycleAdapter(Context context, List<HomeDesignBean> list
            , OnHomeAdapterClickListener listener, List<String> carouselImages) {
        this.context = context;
        this.list = list;
        this.listener = listener;
        this.carouselImages = carouselImages;
    }

    // 中间内容长度
    public int getContentItemCount() {
        return list.size();
    }

    // 判断当前item是否是头部（根据position来判断）
    public boolean isHeaderView(int position) {
        return mHeaderCount != 0 && position < mHeaderCount;
    }

    // 判断当前item是否是底部
    public boolean isBottomView(int position) {
        return mBottomCount != 0 && position >= (mHeaderCount + getContentItemCount());
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == ITEM_TYPE_HEADER) {
            view = LayoutInflater.from(context).inflate(R.layout.main_fragment_layout_header, parent, false);
            return new HeaderViewHolder(view);
        } else if (viewType == ITEM_TYPE_CONTENT) {
            view = LayoutInflater.from(context).inflate(R.layout.mian_fragment_recycle_item, parent, false);
            return new ContentViewHolder(view);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.main_fragment_layout_footer, parent, false);
            return new BottomViewHolder(view);
        }
    }


    @Override
    public int getItemViewType(int position) {
        if (isHeaderView(position)) {
            // 头部View
            return ITEM_TYPE_HEADER;
        } else if (isBottomView(position)) {
            // 底部View
            return ITEM_TYPE_BOTTOM;
        } else {
            // 内容View
            return ITEM_TYPE_CONTENT;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        mContentPosition = position - mHeaderCount;
        mBottomPosition = position - mHeaderCount - getContentItemCount();
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).dongman.setOnClickListener(this);
            ((HeaderViewHolder) holder).mingxinpian.setOnClickListener(this);
            ((HeaderViewHolder) holder).xiaoshuo.setOnClickListener(this);
            ((HeaderViewHolder) holder).guangchang.setOnClickListener(this);
            ((HeaderViewHolder) holder).searchImage.setOnClickListener(this);
            updatemCycleViewPager(((HeaderViewHolder) holder).mCycleViewPager);

        } else if (holder instanceof ContentViewHolder) {
            if (list.size() > mContentPosition) {
                HomeDesignBean homeDesignBean = list.get(mContentPosition);
                List<SourceListContent> sourceContents = homeDesignBean.getSourceListContent();

                Glide.with(context).load(sourceContents.get(0).getCoverUrl())
                        .apply(RequestOptions.bitmapTransform(new RoundedCorners(20))).into(((ContentViewHolder) holder).cardTitleImage);
                ((ContentViewHolder) holder).cardTitle.setText(homeDesignBean.getKind());
                ((ContentViewHolder) holder).cardBookName.setText(sourceContents.get(0).getName());
                ((ContentViewHolder) holder).cardBookbref.setText(sourceContents.get(0).getBookdesc());

                ((ContentViewHolder) holder).cardFristText.setText(sourceContents.get(1).getName());
                Glide.with(context).load(sourceContents.get(1).getCoverUrl())
                        .apply(RequestOptions.bitmapTransform(new RoundedCorners(20))).into(((ContentViewHolder) holder).cardFristImage);

                ((ContentViewHolder) holder).cardTowText.setText(sourceContents.get(2).getName());
                Glide.with(context).load(sourceContents.get(2).getCoverUrl())
                        .apply(RequestOptions.bitmapTransform(new RoundedCorners(20))).into(((ContentViewHolder) holder).cardTowImage);


                ((ContentViewHolder) holder).cardThreeText.setText(sourceContents.get(3).getName());
                Glide.with(context).load(sourceContents.get(3).getCoverUrl())
                        .apply(RequestOptions.bitmapTransform(new RoundedCorners(20))).into(((ContentViewHolder) holder).cardThreeImage);

                ((ContentViewHolder) holder).cardFourText.setText(sourceContents.get(4).getName());
                Glide.with(context).load(sourceContents.get(4).getCoverUrl())
                        .apply(RequestOptions.bitmapTransform(new RoundedCorners(20))).into(((ContentViewHolder) holder).cardFourImage);
            }
        } else {

        }

    }

    @Override
    public int getItemCount() {
        return list.size()+mHeaderCount+mBottomCount;
    }

    public void setData(List<HomeDesignBean> list) {
        this.list = list;
    }

    @Override
    public void onClick(View v) {
        listener.onItemClickListener(v);
    }


    public void updatemCycleViewPager(CycleViewPager mCycleViewPager) {
        List<BannerInfo> mList = new ArrayList<>();

        for (int i = 0; i < carouselImages.size(); i++) {
            mList.add(new BannerInfo("", carouselImages.get(i)));
        }

        //设置选中和未选中时的图片
        assert mCycleViewPager != null;
        mCycleViewPager.setIndicators(R.mipmap.ad_select, R.mipmap.ad_unselect);
        mCycleViewPager.setDelay(2000);
        mCycleViewPager.setData(mList, new CycleViewPager.ImageCycleViewListener() {
            @Override
            public void onImageClick(BannerInfo info, int position, View imageView) {

                if (mCycleViewPager.isCycle()) {
                    position = position - 1;
                }
            }
        });
    }
}
