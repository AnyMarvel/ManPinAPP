package com.mp.android.apps.book.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mp.android.apps.R;
import com.mp.android.apps.book.view.adapter.viewholder.BookRRecommendListHolder;
import com.mp.android.apps.main.home.adapter.OnHomeAdapterClickListener;
import com.mp.android.apps.main.home.bean.SourceListContent;

import java.util.List;

public class BookRankListAdapter extends RecyclerView.Adapter {
    private Context context;
    private List<SourceListContent> contentList;
    private OnHomeAdapterClickListener listener;

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    private int pageNumber;

    public void resetContentList(List<SourceListContent> mContentList){
        if (contentList!=null){
            contentList.clear();
            contentList.addAll(mContentList);
            notifyDataSetChanged();
        }
    }

    public void addContentList(List<SourceListContent> mContentList){
        if (contentList!=null){
            contentList.addAll(mContentList);
            notifyDataSetChanged();
        }
    }
    public BookRankListAdapter(Context context, List<SourceListContent> contentList, OnHomeAdapterClickListener listener) {
        this.context = context;
        this.contentList = contentList;
        this.listener = listener;
        pageNumber=1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.mp_bookr_recommend_list_item, parent, false);
        return new BookRRecommendListHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((BookRRecommendListHolder) holder).handleBookRRecommendContent(context, contentList, listener, position);

    }

    @Override
    public int getItemCount() {
        return contentList.size();
    }
}
