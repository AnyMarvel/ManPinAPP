package com.mp.android.apps.monke.readActivity.ui;

import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.mp.android.apps.monke.readActivity.base.adapter.BaseListAdapter;
import com.mp.android.apps.monke.readActivity.base.adapter.BaseViewHolder;
import com.mp.android.apps.monke.readActivity.base.adapter.IViewHolder;
import com.mp.android.apps.monke.readActivity.view.PageStyle;

/**
 * Created by newbiechen on 17-5-19.
 */

public class PageStyleAdapter extends BaseListAdapter<Drawable> {
    private int currentChecked;

    @Override
    protected IViewHolder<Drawable> createViewHolder(int viewType) {
        return new PageStyleHolder();
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        IViewHolder iHolder = ((BaseViewHolder) holder).holder;
        PageStyleHolder pageStyleHolder = (PageStyleHolder) iHolder;
        if (currentChecked == position){
            pageStyleHolder.setChecked();
        }
    }

    public void setPageStyleChecked(PageStyle pageStyle){
        currentChecked = pageStyle.ordinal();
    }

    @Override
    protected void onItemClick(View v, int pos) {
        super.onItemClick(v, pos);
        currentChecked = pos;
        notifyDataSetChanged();
    }
}
