package com.mp.android.apps.main.bookR.adapter.recommendholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mp.android.apps.R;

public class HeaderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private LinearLayout categoryLayout;
    private LinearLayout rankingLayout;
    private BookRRecommendListener listener;

    public HeaderViewHolder(@NonNull View itemView) {
        super(itemView);
        categoryLayout = itemView.findViewById(R.id.mp_bookr_recommend_category);
        categoryLayout.setOnClickListener(this);
        rankingLayout = itemView.findViewById(R.id.mp_bookr_recommend_ranking);
        rankingLayout.setOnClickListener(this);
    }

    public void handleHeaderView(BookRRecommendListener listener) {
        this.listener = listener;
    }

    @Override
    public void onClick(View v) {
        listener.onItemClick(v);
    }
}
