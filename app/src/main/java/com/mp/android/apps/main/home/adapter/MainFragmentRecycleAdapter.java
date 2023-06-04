package com.mp.android.apps.main.home.adapter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mp.android.apps.R;
import com.mp.android.apps.main.home.adapter.viewholder.HeaderViewHolder;
import com.mp.android.apps.main.home.adapter.viewholder.MainRecommendHolder;
import com.mp.android.apps.main.home.view.impl.OnMainFragmentClickListener;

import java.util.List;
import java.util.Map;

public class MainFragmentRecycleAdapter extends RecyclerView.Adapter {
    private int mHeaderCount = 1;// 头部的数量
    private int mRecommendCount = 2;//经典推荐的数量

    // 首先定义几个常量标记item的类型
    private static final int ITEM_TYPE_HEADER = 0;
    private static final int ITEM_TYPE_RECOMMEND = 1;

    private Context context;
    //各种点击时间接口,实现在fragment中
    private OnMainFragmentClickListener listener;
    private List<Map<String, String>> carouselList;
    private List<Map<String, String>> recommendInfoList;
    public MainFragmentRecycleAdapter(Context context, List<Map<String, String>> carouselList, List<Map<String, String>> recommendInfoList
            , OnMainFragmentClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.carouselList = carouselList;
        this.recommendInfoList = recommendInfoList;
    }

    // 判断当前item是否是头部（根据position来判断）
    private boolean isHeaderView(int position) {
        return mHeaderCount != 0 && position < mHeaderCount;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == ITEM_TYPE_HEADER) {
            view = LayoutInflater.from(context).inflate(R.layout.main_fragment_layout_header, parent, false);
            return new HeaderViewHolder(view);
        }else {
            view = LayoutInflater.from(context).inflate(R.layout.main_fragment_recycle_item_recommend, parent, false);
            return new MainRecommendHolder(view);
        }
    }


    @Override
    public int getItemViewType(int position) {
        if (isHeaderView(position)) {
            // 头部View
            return ITEM_TYPE_HEADER;
        } else {
            return ITEM_TYPE_RECOMMEND;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).handleClassicRecommendEvent(carouselList, listener);
        } else {
            List<Map<String, String>> list;
            String recommendName;
            if (position==1){
                list= recommendInfoList.subList(0,3);
                recommendName="小编推荐";
            }else {
                list = recommendInfoList.subList(3,6);
                recommendName="网友推荐";
            }
            ((MainRecommendHolder) holder).handleClassicRecommendEvent(context, list, recommendName,listener);
        }

    }

    @Override
    public int getItemCount() {
        return  mHeaderCount + mRecommendCount;
    }

}
