package com.mp.android.apps.main.bookR.adapter.recommendholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mp.android.apps.R;
import com.mp.android.apps.main.home.adapter.OnHomeAdapterClickListener;

public class HeaderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private LinearLayout categoryLayout;
    private LinearLayout rankingLayout;
    private OnHomeAdapterClickListener listener;

    private TextView recommendText;
    private TextView collectionText;

    public HeaderViewHolder(@NonNull View itemView) {
        super(itemView);
        categoryLayout = itemView.findViewById(R.id.mp_bookr_recommend_category);
        categoryLayout.setOnClickListener(this);
        rankingLayout = itemView.findViewById(R.id.mp_bookr_recommend_ranking);
        rankingLayout.setOnClickListener(this);
        recommendText=itemView.findViewById(R.id.mp_header_tuijian);
        collectionText=itemView.findViewById(R.id.mp_header_collection);
    }

    public void handleHeaderView(OnHomeAdapterClickListener listener) {
        this.listener = listener;
        if (iRankTitle!=null){
            recommendText.setText(iRankTitle.recommendTitle());
            collectionText.setText(iRankTitle.collectionTitle());
        }
    }

    @Override
    public void onClick(View v) {
        listener.onItemClickListener(v);
    }

    private IRankTitle iRankTitle;

    public void setiRankTitle(IRankTitle iRankTitle) {
        this.iRankTitle = iRankTitle;
    }

    public interface IRankTitle{
        String recommendTitle();
        String collectionTitle();
    }
}
