package com.mp.android.apps.main.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mp.android.apps.R;
import com.mp.android.apps.main.adapter.viewholder.BottomViewHolder;
import com.mp.android.apps.main.adapter.viewholder.ClassicRecommendHolder;
import com.mp.android.apps.main.adapter.viewholder.ContentViewHolder;
import com.mp.android.apps.main.adapter.viewholder.HeaderViewHolder;
import com.mp.android.apps.main.bean.HomeDesignBean;
import com.mp.android.apps.main.bean.SourceListContent;

import java.util.List;

public class MainFragmentRecycleAdapter extends RecyclerView.Adapter {
    private int mHeaderCount = 1;// 头部的数量
    private int mBottomCount = 1;// 底部的数量
    private int mRecommendCount = 1;//经典推荐的数量

    // 首先定义几个常量标记item的类型
    private static final int ITEM_TYPE_HEADER = 0;
    private static final int ITEM_TYPE_CONTENT = 1;
    private static final int ITEM_TYPE_BOTTOM = 2;
    private static final int ITEM_TYPE_RECOMMEND = 3;

    private Context context;
    private List<HomeDesignBean> listContent;
    //中间内容位置信息
    private int mContentPosition;
    //底部view位置信息
    private int mBottomPosition;
    //各种点击时间接口,实现在fragment中
    private OnHomeAdapterClickListener listener;
    //轮播图数据源
    private List<String> carouselImages;
    //推荐位数据源
    private List<SourceListContent> recommendList;

    public MainFragmentRecycleAdapter(Context context, List<HomeDesignBean> listContent
            , OnHomeAdapterClickListener listener, List<String> carouselImages, List<SourceListContent> recommendList) {
        this.context = context;
        this.listContent = listContent;
        this.listener = listener;
        this.carouselImages = carouselImages;
        this.recommendList = recommendList;
    }

    // 中间内容长度
    private int getContentItemCount() {
        return listContent.size();
    }

    // 判断当前item是否是头部（根据position来判断）
    private boolean isHeaderView(int position) {
        return mHeaderCount != 0 && position < mHeaderCount;
    }

    // 判断当前item是否是底部
    private boolean isBottomView(int position) {
        return mBottomCount != 0 && position >= (mHeaderCount + mRecommendCount + getContentItemCount());
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
        } else if (viewType == ITEM_TYPE_RECOMMEND) {
            view = LayoutInflater.from(context).inflate(R.layout.main_fragment_recycle_item_recommend, parent, false);
            return new ClassicRecommendHolder(view);
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
        } else if (isRecommendView(position)) {
            return ITEM_TYPE_RECOMMEND;
        } else {
            // 内容View
            return ITEM_TYPE_CONTENT;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        mContentPosition = position - mHeaderCount - mRecommendCount;
        mBottomPosition = position - mHeaderCount - -mRecommendCount - getContentItemCount();
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).handleClassicRecommendEvent(carouselImages, listener);
        } else if (holder instanceof ClassicRecommendHolder) {
            ((ClassicRecommendHolder) holder).handleClassicRecommendEvent(context, recommendList, mContentPosition, listener);
        } else if (holder instanceof ContentViewHolder) {
            ((ContentViewHolder) holder).handleContentEvent(context, listContent, mContentPosition, listener);
        } else {

        }

    }

    @Override
    public int getItemCount() {
        return listContent.size() + mHeaderCount + mRecommendCount + mBottomCount;
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
