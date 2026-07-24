package com.mp.android.apps.read.base.adapter;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;


public class BaseViewHolder<T> extends RecyclerView.ViewHolder{
    public IViewHolder<T> holder;

    public BaseViewHolder(View itemView, IViewHolder<T> holder) {
        super(itemView);
        this.holder = holder;
        holder.initView();
    }
}
