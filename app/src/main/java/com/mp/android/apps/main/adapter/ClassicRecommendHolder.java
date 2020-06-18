package com.mp.android.apps.main.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.mp.android.apps.R;
import com.mp.android.apps.main.bean.HomeDesignBean;
import com.mp.android.apps.main.bean.SourceListContent;

import java.util.List;

public class ClassicRecommendHolder extends RecyclerView.ViewHolder {
    /**
     * 经典推荐第一个图片
     */
    ImageView recommendFirstImage;
    /**
     * 经典推荐第二个图片
     */
    ImageView recommendTowImage;
    /**
     * 经典推荐第三个图片
     */
    ImageView recommendThreeImage;

    TextView recommendFirstText;
    TextView recommendTowText;
    TextView recommendThreeText;


    LinearLayout recommendFirstLayout;
    LinearLayout recommendTowLayout;
    LinearLayout recommendThreeLayout;

    public ClassicRecommendHolder(@NonNull View itemView) {
        super(itemView);
        recommendFirstImage = itemView.findViewById(R.id.mp_home_recommend_firstImage);
        recommendTowImage = itemView.findViewById(R.id.mp_home_recommend_towImage);
        recommendThreeImage = itemView.findViewById(R.id.mp_home_recommend_threeImage);

        recommendFirstText = itemView.findViewById(R.id.mp_home_recommend_fristText);
        recommendTowText = itemView.findViewById(R.id.mp_home_recommend_towText);
        recommendThreeText = itemView.findViewById(R.id.mp_home_recommend_threeText);

        recommendFirstLayout = itemView.findViewById(R.id.FirstLayout);
        recommendTowLayout = itemView.findViewById(R.id.TowLayout);
        recommendThreeLayout = itemView.findViewById(R.id.ThreeLayout);
    }

    public void handleClassicRecommendEvent(Context context, List<SourceListContent> recommendList, int mContentPosition, OnHomeAdapterClickListener listener) {


        Glide.with(context).load(recommendList.get(0).getCoverUrl())
                .apply(RequestOptions.bitmapTransform(new RoundedCorners(10))).into(recommendFirstImage);
        Glide.with(context).load(recommendList.get(1).getCoverUrl())
                .apply(RequestOptions.bitmapTransform(new RoundedCorners(10))).into(recommendTowImage);
        Glide.with(context).load(recommendList.get(2).getCoverUrl())
                .apply(RequestOptions.bitmapTransform(new RoundedCorners(10))).into(recommendThreeImage);

        recommendFirstText.setText(recommendList.get(0).getName());
        recommendTowText.setText(recommendList.get(1).getName());
        recommendThreeText.setText(recommendList.get(2).getName());

        recommendFirstLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onLayoutClickListener(v, recommendList.get(0));
            }
        });
        recommendTowLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onLayoutClickListener(v, recommendList.get(1));
            }
        });
        recommendThreeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onLayoutClickListener(v, recommendList.get(2));
            }
        });


    }
}
