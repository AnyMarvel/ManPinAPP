package com.mp.android.apps.readActivity.view.category;

import android.view.View;
import android.view.ViewGroup;


import com.mp.android.apps.readActivity.base.EasyAdapter;
import com.mp.android.apps.readActivity.base.adapter.IViewHolder;
import com.mp.android.apps.readActivity.view.TxtChapter;


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
