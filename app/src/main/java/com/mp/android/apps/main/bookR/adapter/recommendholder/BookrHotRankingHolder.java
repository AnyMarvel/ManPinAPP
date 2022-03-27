package com.mp.android.apps.main.bookR.adapter.recommendholder;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.mp.android.apps.R;
import com.mp.android.apps.main.home.adapter.OnHomeAdapterClickListener;
import com.mp.android.apps.main.home.bean.SourceListContent;

import java.util.List;

/**
 * BookrHotRankingHolder简介
 * 热度排行item holder
 *
 * @author lijuntao
 * @date 2020-07-01 16:52
 */
public class BookrHotRankingHolder extends RecyclerView.ViewHolder {
    private LinearLayout hotBookLayout1;
    private ImageView hotBookImage1;
    private TextView hotBookName1;
    private TextView hotBookNumber1;

    private LinearLayout hotBookLayout2;
    private ImageView hotBookImage2;
    private TextView hotBookName2;
    private TextView hotBookNumber2;

    private LinearLayout hotBookLayout3;
    private ImageView hotBookImage3;
    private TextView hotBookName3;
    private TextView hotBookNumber3;

    private LinearLayout hotBookLayout4;
    private ImageView hotBookImage4;
    private TextView hotBookName4;
    private TextView hotBookNumber4;

    private LinearLayout hotBookLayout5;
    private ImageView hotBookImage5;
    private TextView hotBookName5;
    private TextView hotBookNumber5;

    private LinearLayout hotBookLayout6;
    private ImageView hotBookImage6;
    private TextView hotBookName6;
    private TextView hotBookNumber6;

    private Context context;

    public BookrHotRankingHolder(@NonNull View itemView) {
        super(itemView);
        initView(itemView);

    }

    /**
     * 初始化view
     *
     * @param itemView
     */
    private void initView(View itemView) {
        hotBookLayout1 = itemView.findViewById(R.id.mp_bookr_hot_1_layout);
        hotBookImage1 = itemView.findViewById(R.id.mp_bookr_hot_1_layout_image);
        hotBookName1 = itemView.findViewById(R.id.mp_bookr_hot_1_layout_text);
        hotBookNumber1 = itemView.findViewById(R.id.mp_bookr_hot_1_layout_hot);


        hotBookLayout2 = itemView.findViewById(R.id.mp_bookr_hot_2_layout);
        hotBookImage2 = itemView.findViewById(R.id.mp_bookr_hot_2_layout_image);
        hotBookName2 = itemView.findViewById(R.id.mp_bookr_hot_2_layout_text);
        hotBookNumber2 = itemView.findViewById(R.id.mp_bookr_hot_2_layout_hot);

        hotBookLayout3 = itemView.findViewById(R.id.mp_bookr_hot_3_layout);
        hotBookImage3 = itemView.findViewById(R.id.mp_bookr_hot_3_layout_image);
        hotBookName3 = itemView.findViewById(R.id.mp_bookr_hot_3_layout_text);
        hotBookNumber3 = itemView.findViewById(R.id.mp_bookr_hot_3_layout_hot);

        hotBookLayout4 = itemView.findViewById(R.id.mp_bookr_hot_4_layout);
        hotBookImage4 = itemView.findViewById(R.id.mp_bookr_hot_4_layout_image);
        hotBookName4 = itemView.findViewById(R.id.mp_bookr_hot_4_layout_text);
        hotBookNumber4 = itemView.findViewById(R.id.mp_bookr_hot_4_layout_hot);

        hotBookLayout5 = itemView.findViewById(R.id.mp_bookr_hot_5_layout);
        hotBookImage5 = itemView.findViewById(R.id.mp_bookr_hot_5_layout_image);
        hotBookName5 = itemView.findViewById(R.id.mp_bookr_hot_5_layout_text);
        hotBookNumber5 = itemView.findViewById(R.id.mp_bookr_hot_5_layout_hot);

        hotBookLayout6 = itemView.findViewById(R.id.mp_bookr_hot_6_layout);
        hotBookImage6 = itemView.findViewById(R.id.mp_bookr_hot_6_layout_image);
        hotBookName6 = itemView.findViewById(R.id.mp_bookr_hot_6_layout_text);
        hotBookNumber6 = itemView.findViewById(R.id.mp_bookr_hot_6_layout_hot);
    }

    List<SourceListContent> hotRankingList;
    OnHomeAdapterClickListener listener;

    public void handleBookRHotRanking(Context context, List<SourceListContent> hotRankingList, OnHomeAdapterClickListener listener) {
        this.context = context;
        this.hotRankingList = hotRankingList;
        this.listener = listener;
        setItemEnent(hotBookLayout1, hotBookImage1, hotBookName1, hotBookNumber1, 0);
        setItemEnent(hotBookLayout2, hotBookImage2, hotBookName2, hotBookNumber2, 1);
        setItemEnent(hotBookLayout3, hotBookImage3, hotBookName3, hotBookNumber3, 2);
        setItemEnent(hotBookLayout4, hotBookImage4, hotBookName4, hotBookNumber4, 3);
        setItemEnent(hotBookLayout5, hotBookImage5, hotBookName5, hotBookNumber5, 4);
        setItemEnent(hotBookLayout6, hotBookImage6, hotBookName6, hotBookNumber6, 5);

    }

    private void setItemEnent(LinearLayout linearLayout, ImageView imageView, TextView name, TextView hot, int position) {
        if (hotRankingList.size()>position){
            Glide.with(context).load(hotRankingList.get(position).getCoverUrl())
                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(10))).into(imageView);
            name.setText(hotRankingList.get(position).getName());
            String count = hotRankingList.get(position).getSearCount() + "万热度";
            hot.setText(count);
            linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onLayoutClickListener(v, hotRankingList.get(position));
                }
            });
        }

    }
}
