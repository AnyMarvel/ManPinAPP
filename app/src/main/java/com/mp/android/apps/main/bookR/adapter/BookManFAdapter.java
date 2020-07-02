package com.mp.android.apps.main.bookR.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mp.android.apps.R;
import com.mp.android.apps.main.bookR.adapter.manholder.BookManContentHolder;
import com.mp.android.apps.main.bookR.adapter.recommendholder.BookrHotRankingHolder;
import com.mp.android.apps.main.bookR.adapter.recommendholder.HeaderViewHolder;
import com.mp.android.apps.main.home.adapter.OnHomeAdapterClickListener;
import com.mp.android.apps.main.home.adapter.viewholder.ClassicRecommendHolder;
import com.mp.android.apps.main.home.bean.HomeDesignBean;
import com.mp.android.apps.main.home.bean.SourceListContent;

import java.util.List;

public class BookManFAdapter extends RecyclerView.Adapter {
    // 首先定义几个常量标记item的类型
    private static final int ITEM_TYPE_HEADER = 0;
    private static final int ITEM_TYPE_RECOMMEND = 1;
    private static final int ITEM_TYPE_HOT_SEARCH = 2;
    private static final int ITEM_TYPE_CONTENT = 3;


    private Context context;

    private OnHomeAdapterClickListener listener;

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return ITEM_TYPE_HEADER;
        } else if (position == 1) {
            return ITEM_TYPE_RECOMMEND;
        } else if (position == 2) {
            return ITEM_TYPE_HOT_SEARCH;
        } else {
            return ITEM_TYPE_CONTENT;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == ITEM_TYPE_HEADER) {
            view = LayoutInflater.from(context).inflate(R.layout.mp_bookr_recommend_header, parent, false);
            return new HeaderViewHolder(view);
        } else if (viewType == ITEM_TYPE_RECOMMEND) {
            view = LayoutInflater.from(context).inflate(R.layout.main_fragment_recycle_item_recommend, parent, false);
            return new ClassicRecommendHolder(view);
        } else if (viewType == ITEM_TYPE_HOT_SEARCH) {
            view = LayoutInflater.from(context).inflate(R.layout.mp_bookr_hot_ranking, parent, false);
            return new BookrHotRankingHolder(view);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.mp_book_man_recycle_item, parent, false);
            return new BookManContentHolder(view);
        }
    }

    /**
     * 随机推荐数据为3条数据
     */
    private List<SourceListContent> recommendList;
    /**
     * 热榜数据为6条数据
     */
    private List<SourceListContent> hotRankingList;

    /**
     * 推荐页内容显示
     */
    private List<HomeDesignBean> listContent;

    public BookManFAdapter(Context context, OnHomeAdapterClickListener listener, List<SourceListContent> recommendList, List<SourceListContent> hotRankingList, List<HomeDesignBean> listContent) {
        this.context = context;
        this.listener = listener;
        this.recommendList = recommendList;
        this.hotRankingList = hotRankingList;
        this.listContent = listContent;
    }

    public void setRecommendList(List<SourceListContent> recommendList) {
        this.recommendList = recommendList;
    }

    public void setHotRankingList(List<SourceListContent> hotRankingList) {
        this.hotRankingList = hotRankingList;
    }

    public void setListContent(List<HomeDesignBean> listContent) {
        this.listContent = listContent;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).handleHeaderView(listener);
        } else if (holder instanceof ClassicRecommendHolder) {
            ((ClassicRecommendHolder) holder).handleClassicRecommendEvent(context, recommendList, "男生推荐", listener);
        } else if (holder instanceof BookrHotRankingHolder) {
            ((BookrHotRankingHolder) holder).handleBookRHotRanking(context, hotRankingList, listener);
        } else if (holder instanceof BookManContentHolder) {
            ((BookManContentHolder) holder).handleBookManContent(context, listContent, position - 3, listener);
        }
    }

    @Override
    public int getItemCount() {
        return 3 + listContent.size();
    }
}
