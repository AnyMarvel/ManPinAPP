package com.mp.android.apps.explore.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
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

import java.util.ArrayList;
import java.util.List;

public class ExploreSquareAdapter extends RecyclerView.Adapter<ExploreSquareAdapter.ExploreSquareViewHolder> {
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
        this.exploreData = exploreData;
    }

    public void addExploreData(List<Data> exploreData) {
        this.exploreData.addAll(exploreData);
    }

    public ExploreSquareAdapter(Context context) {
        this.context = context;

    }

    @NonNull
    @Override
    public ExploreSquareViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new ExploreSquareViewHolder(LayoutInflater.from(context).inflate(R.layout.explore_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ExploreSquareViewHolder holder, int position) {
        Glide.with(context).asBitmap().load(exploreData.get(position).getImageInfo().getImageUrl()).into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                holder.itemView.getViewTreeObserver().addOnDrawListener(new ViewTreeObserver.OnDrawListener() {
                    @Override
                    public void onDraw() {
                        int width = resource.getWidth();//图片原始宽度
                        int height = resource.getHeight();//图片原始高度
                        float scaleType = (float) holder.exploreImage.getWidth() / width;
                        ViewGroup.LayoutParams layoutParams = holder.exploreImage.getLayoutParams();

                        layoutParams.height = Math.round(height * scaleType);
                        layoutParams.width = holder.exploreImage.getWidth();
                        holder.exploreImage.setLayoutParams(layoutParams);
                        holder.exploreImage.setBackground(new BitmapDrawable(context.getResources(), resource));
                    }
                });

            }
        });

        Glide.with(context).load(exploreData.get(position).getPersonIconUrl()).into(holder.userImage);
        holder.userName.setText(exploreData.get(position).getPersonName());
        String temp = exploreData.get(position).getImageInfo().getProvince() + " " + exploreData.get(position).getImageInfo().getCity();
        holder.currentPosition.setText(temp);
        holder.currentTime.setText(FormatCurrentData.getTimeRange(exploreData.get(position).getImageInfo().getTime()));
        ImageDesBean imageDes = JSON.parseObject(exploreData.get(position).getImageInfo().getImageDes(), ImageDesBean.class);
        holder.pic_des.setText(imageDes.getImageDes());
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
        /**
         * 用户头像
         */
        public ImageView userImage;
        /**
         * 用户昵称
         */
        public TextView userName;

        /**
         * 图片拍摄位置
         */
        public TextView currentPosition;
        /**
         * 图片发布时间
         */
        public TextView currentTime;

        public TextView pic_des;

        public ExploreSquareViewHolder(@NonNull View itemView) {
            super(itemView);
            exploreImage = itemView.findViewById(R.id.exploreImage);
            userImage = itemView.findViewById(R.id.user_image);
            userName = itemView.findViewById(R.id.user_name);

            currentPosition = itemView.findViewById(R.id.current_position);
            currentTime = itemView.findViewById(R.id.current_time);
            pic_des = itemView.findViewById(R.id.pic_des);
        }
    }
}
