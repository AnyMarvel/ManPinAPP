package com.mp.android.apps.main.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mp.android.apps.R;

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

    public ClassicRecommendHolder(@NonNull View itemView) {
        super(itemView);
        recommendFirstImage = itemView.findViewById(R.id.mp_home_recommend_firstImage);
        recommendTowImage = itemView.findViewById(R.id.mp_home_recommend_towImage);
        recommendThreeImage = itemView.findViewById(R.id.mp_home_recommend_threeImage);

        recommendFirstText = itemView.findViewById(R.id.mp_home_recommend_fristText);
        recommendTowText = itemView.findViewById(R.id.mp_home_recommend_towText);
        recommendThreeText = itemView.findViewById(R.id.mp_home_recommend_threeText);


    }
}
