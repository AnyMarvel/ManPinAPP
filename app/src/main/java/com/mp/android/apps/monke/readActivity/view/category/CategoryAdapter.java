package com.mp.android.apps.monke.readActivity.view.category;

import android.view.View;
import android.view.ViewGroup;


import com.mp.android.apps.monke.readActivity.base.EasyAdapter;
import com.mp.android.apps.monke.readActivity.base.adapter.IViewHolder;
import com.mp.android.apps.monke.readActivity.view.TxtChapter;

/**
 * Created by newbiechen on 17-6-5.
 */

public class CategoryAdapter extends EasyAdapter<TxtChapter> {
    private int currentSelected = 0;
    @Override
    protected IViewHolder<TxtChapter> onCreateViewHolder(int viewType) {
        return new CategoryHolder();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        CategoryHolder holder = (CategoryHolder) view.getTag();

        if (position == currentSelected){
            holder.setSelectedChapter();
        }

        return view;
    }

    public void setChapter(int pos){
        currentSelected = pos;
        notifyDataSetChanged();
    }
}
