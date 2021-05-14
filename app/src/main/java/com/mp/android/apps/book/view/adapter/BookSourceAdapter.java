package com.mp.android.apps.book.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mp.android.apps.R;
import com.mp.android.apps.book.bean.BookSourceBean;
import com.mp.android.apps.book.view.adapter.viewholder.BookSourceViewHolder;

import java.util.List;

public class BookSourceAdapter extends RecyclerView.Adapter {
    public void setSourceBeanList(List<BookSourceBean> sourceBeanList) {
        this.sourceBeanList = sourceBeanList;
    }

    private List<BookSourceBean> sourceBeanList;

    public BookSourceAdapter(List<BookSourceBean> sourceBeanList) {
        this.sourceBeanList = sourceBeanList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mp_book_source_item, parent, false);
        return new BookSourceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (sourceBeanList.size() > position)
            ((BookSourceViewHolder) holder).handleBookSourceView(sourceBeanList.get(position));
    }

    @Override
    public int getItemCount() {
        return sourceBeanList.size();
    }
}
