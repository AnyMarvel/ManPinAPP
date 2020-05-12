package com.mp.android.apps.explore.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.mp.android.apps.R;
import com.mp.android.apps.explore.bean.Data;
import com.mp.android.apps.explore.utils.FormatCurrentData;
import com.mp.android.apps.explore.views.RoundAngleImageView;
import com.mp.android.apps.livevblank.bean.ImageDesBean;
import com.mp.android.apps.utils.Logger;

import java.util.ArrayList;
import java.util.List;

public class StaggeredExploreSquareAdapter extends RecyclerView.Adapter<StaggeredExploreSquareAdapter.ExploreSquareViewHolder> {
    private Context context;
    private List<Data> exploreData = new ArrayList<>();
    private int pageNumber;

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public List<Data> getExploreData() {
        return exploreData;
    }

    public void setExploreData(List<Data> exploreData) {
        this.exploreData.clear();
        this.exploreData.addAll(exploreData);
    }

    public void addExploreData(List<Data> exploreData) {
        this.exploreData.addAll(exploreData);
    }

    public StaggeredExploreSquareAdapter(Context context) {
        this.context = context;

    }

    @NonNull
    @Override
    public ExploreSquareViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new ExploreSquareViewHolder(LayoutInflater.from(context).inflate(R.layout.explore_staggered_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ExploreSquareViewHolder holder, int position) {
        Glide.with(context).asBitmap().load(exploreData.get(position).getImageInfo().getImageUrl()).into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                holder.itemView.getViewTreeObserver().addOnDrawListener(new ViewTreeObserver.OnDrawListener() {
                    @Override
                    public void onDraw() {
                        if (exploreData.size() > position) {
                            int width = resource.getWidth();//图片原始宽度
                            int height = resource.getHeight();//图片原始高度
                            float scaleType = (float) holder.exploreImage.getWidth() / width;
                            ViewGroup.LayoutParams layoutParams = holder.exploreImage.getLayoutParams();
                            layoutParams.height = Math.round(height * scaleType);
                            layoutParams.width = holder.exploreImage.getWidth();
                            holder.exploreImage.setLayoutParams(layoutParams);
                            holder.exploreImage.setBackground(new BitmapDrawable(context.getResources(), resource));
                            ImageDesBean imageDes = JSON.parseObject(exploreData.get(position).getImageInfo().getImageDes(), ImageDesBean.class);
                            holder.pic_des.setText(imageDes.getImageDes());
                        }
                    }
                });
            }
        });

    }


    @Override
    public int getItemCount() {
        return exploreData.size();
    }

    static class ExploreSquareViewHolder extends RecyclerView.ViewHolder {
        /**
         * 显示图片
         */
        public RoundAngleImageView exploreImage;


        public TextView pic_des;

        public ExploreSquareViewHolder(@NonNull View itemView) {
            super(itemView);
            exploreImage = itemView.findViewById(R.id.exploreImage);
            pic_des = itemView.findViewById(R.id.pic_des);
        }
    }
}
