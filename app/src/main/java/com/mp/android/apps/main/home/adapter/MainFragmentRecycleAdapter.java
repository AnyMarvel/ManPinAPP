package com.mp.android.apps.main.home.adapter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mp.android.apps.R;
import com.mp.android.apps.main.home.adapter.viewholder.ClassicRecommendHolder;
import com.mp.android.apps.main.home.adapter.viewholder.HeaderViewHolder;
import com.mp.android.apps.main.home.adapter.viewholder.MainRecommendHolder;
import com.mp.android.apps.main.home.bean.HomeDesignBean;
import com.mp.android.apps.main.home.bean.SourceListContent;

import java.util.List;
import java.util.Map;

public class MainFragmentRecycleAdapter extends RecyclerView.Adapter {
    private int mHeaderCount = 1;// 头部的数量
    private int mRecommendCount = 2;//经典推荐的数量

    // 首先定义几个常量标记item的类型
    private static final int ITEM_TYPE_HEADER = 0;
    private static final int ITEM_TYPE_RECOMMEND = 1;
    private static final int ITEM_TYPE_RECOMMEND_R = 2;

    private Context context;
    private List<HomeDesignBean> listContent;
    //中间内容位置信息
    private int mContentPosition;

    //各种点击时间接口,实现在fragment中
    private OnHomeAdapterClickListener listener;

    //推荐位数据源
    private List<SourceListContent> recommendList;
    private List<Map<String, String>> carouselList;
    private List<Map<String, String>> recommendInfoList;
    public MainFragmentRecycleAdapter(Context context, List<Map<String, String>> carouselList, List<Map<String, String>> recommendInfoList
            , OnHomeAdapterClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.carouselList = carouselList;
        this.recommendInfoList = recommendInfoList;
    }

    // 中间内容长度
    private int getContentItemCount() {
        return listContent.size();
    }

    // 判断当前item是否是头部（根据position来判断）
    private boolean isHeaderView(int position) {
        return mHeaderCount != 0 && position < mHeaderCount;
    }


    // 判断当前item是否为经典推荐位
    private boolean isRecommendView(int position) {
        return mRecommendCount != 0 && position == 1;
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
            if (position==1){
                list= recommendInfoList.subList(0,3);
            }else {
                list = recommendInfoList.subList(3,6);
            }
            ((MainRecommendHolder) holder).handleClassicRecommendEvent(context, list, "推荐");
        }

    }

    @Override
    public int getItemCount() {
        return  mHeaderCount + mRecommendCount;
    }

    /**
     * 基于position修改数据源,刷新单个item
     *
     * @param mContentPosition   mContentPosition位置,比list postion大小小两位
     * @param sourceListContents content内部一个item的数据内容
     */
    public void updateContentByPosition(int mContentPosition, List<SourceListContent> sourceListContents) {
        if (mContentPosition >= 0 && sourceListContents != null) {
            listContent.get(mContentPosition).setSourceListContent(sourceListContents);
            notifyItemChanged(mContentPosition + 2);
        }

    }
}
