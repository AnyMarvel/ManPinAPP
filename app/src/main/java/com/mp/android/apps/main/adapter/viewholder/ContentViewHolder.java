package com.mp.android.apps.main.adapter.viewholder;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.mp.android.apps.R;
import com.mp.android.apps.main.adapter.OnHomeAdapterClickListener;
import com.mp.android.apps.main.bean.HomeDesignBean;
import com.mp.android.apps.main.bean.SourceListContent;
import com.mp.android.apps.utils.RadiusUtils;

import java.util.List;

public class ContentViewHolder extends RecyclerView.ViewHolder {
    private ImageView cardTitleImage;
    private ImageView cardFristImage;
    private ImageView cardTowImage;
    private ImageView cardThreeImage;
    private ImageView cardFourImage;
    /**
     * 换一换图标
     */
    private ImageView contentChange;


    private TextView cardTitle;
    private TextView cardBookName;
    private TextView cardBookbref;
    private TextView cardFristText;
    private TextView cardTowText;
    private TextView cardThreeText;
    private TextView cardFourText;

    private FrameLayout cardLayout;
    private LinearLayout FirstLayout;
    private LinearLayout TowLayout;
    private LinearLayout ThreeLayout;
    private LinearLayout FourLayout;
    private LinearLayout mBookInfoLayout;
    /**
     * 首页Content布局中评分元素增加
     */
    private RatingBar ratingBar;

    public ContentViewHolder(@NonNull View itemView) {
        super(itemView);
        cardTitleImage = itemView.findViewById(R.id.cardTitleImage);
        cardFristImage = itemView.findViewById(R.id.cardFristImage);
        cardTowImage = itemView.findViewById(R.id.cardTowImage);
        cardThreeImage = itemView.findViewById(R.id.cardThreeImage);
        cardFourImage = itemView.findViewById(R.id.cardFourImage);
        contentChange = itemView.findViewById(R.id.mp_content_change);
        mBookInfoLayout = itemView.findViewById(R.id.mp_book_info_container);
        //cardBackground = itemView.findViewById(R.id.cardBackground);
        cardTitle = itemView.findViewById(R.id.cardTitle);
        cardBookName = itemView.findViewById(R.id.cardBookName);
        cardBookbref = itemView.findViewById(R.id.cardBookbref);
        cardFristText = itemView.findViewById(R.id.cardFristText);
        cardTowText = itemView.findViewById(R.id.cardTowText);
        cardThreeText = itemView.findViewById(R.id.cardThreeText);
        cardFourText = itemView.findViewById(R.id.cardFourText);
        cardLayout = itemView.findViewById(R.id.mz_book_head_container);
        FirstLayout = itemView.findViewById(R.id.FirstLayout);
        TowLayout = itemView.findViewById(R.id.TowLayout);
        ThreeLayout = itemView.findViewById(R.id.ThreeLayout);
        FourLayout = itemView.findViewById(R.id.FourLayout);
        ratingBar = itemView.findViewById(R.id.mp_content_card_ratingbar);
    }

    public void handleContentEvent(Context context, List<HomeDesignBean> listContent, int mContentPosition, OnHomeAdapterClickListener listener) {
        if (listContent.size() > mContentPosition) {
            HomeDesignBean homeDesignBean = listContent.get(mContentPosition);
            List<SourceListContent> sourceContents = homeDesignBean.getSourceListContent();

            contentChange.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onContentChangeClickListener(mContentPosition, homeDesignBean.getKind());
                }
            });
            Glide.with(context).load(sourceContents.get(0).getCoverUrl())
                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(10))).into(cardTitleImage);
            cardTitle.setText(homeDesignBean.getKind());
            cardBookName.setText(sourceContents.get(0).getName());
            cardBookbref.setText(sourceContents.get(0).getBookdesc());
            RadiusUtils.setClipViewCornerRadius(mBookInfoLayout, 10);
            mBookInfoLayout.setBackgroundColor(Color.parseColor(homeDesignBean.getCardColor()));
            cardLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onLayoutClickListener(v, sourceContents.get(0));
                }
            });

            cardFristText.setText(sourceContents.get(1).getName());
            Glide.with(context).load(sourceContents.get(1).getCoverUrl())
                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(10))).into(cardFristImage);
            FirstLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onLayoutClickListener(v, sourceContents.get(1));
                }
            });


            cardTowText.setText(sourceContents.get(2).getName());
            Glide.with(context).load(sourceContents.get(2).getCoverUrl())
                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(10))).into(cardTowImage);
            TowLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onLayoutClickListener(v, sourceContents.get(2));
                }
            });


            cardThreeText.setText(sourceContents.get(3).getName());
            Glide.with(context).load(sourceContents.get(3).getCoverUrl())
                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(10))).into(cardThreeImage);
            ThreeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onLayoutClickListener(v, sourceContents.get(3));
                }
            });

            cardFourText.setText(sourceContents.get(4).getName());
            Glide.with(context).load(sourceContents.get(4).getCoverUrl())
                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(10))).into(cardFourImage);
            FourLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onLayoutClickListener(v, sourceContents.get(4));
                }
            });

        }

    }

}
