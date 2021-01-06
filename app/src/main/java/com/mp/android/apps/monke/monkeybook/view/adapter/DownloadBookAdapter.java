package com.mp.android.apps.monke.monkeybook.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mp.android.apps.R;
import com.mp.android.apps.monke.monkeybook.bean.DownloadTaskBean;
import com.mp.android.apps.monke.readActivity.local.BookRepository;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DownloadBookAdapter extends RecyclerView.Adapter<DownloadBookAdapter.ViewHolder> {
    private Context context;

    private List<DownloadTaskBean> downloadTaskBeanList;

    public DownloadBookAdapter(Context context, List<DownloadTaskBean> downloadTaskBeanList) {
        this.context = context;
        this.downloadTaskBeanList = downloadTaskBeanList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.view_pop_downloadlist, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DownloadTaskBean downloadTaskBean = downloadTaskBeanList.get(position);
        Glide.with(context).load(downloadTaskBean.getCoverUrl()).into(holder.ivCover);
        holder.tvName.setText(downloadTaskBean.getTaskName());
        if (downloadTaskBean.getBookChapters().size() > downloadTaskBean.getCurrentChapter())
            holder.tvChapterName.setText(downloadTaskBean.getBookChapters().get(downloadTaskBean.getCurrentChapter()).getTitle());

        // 创建一个数值格式化对象
        NumberFormat numberFormat = NumberFormat.getInstance();
        // 设置精确到小数点后2位
        numberFormat.setMaximumFractionDigits(2);
        String result = numberFormat.format((float) downloadTaskBean.getCurrentChapter() / (float) downloadTaskBean.getBookChapters().size() * 100) + "%";
        holder.tvProgresss.setText(result);


    }

    @Override
    public int getItemCount() {
        return downloadTaskBeanList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvName;
        TextView tvChapterName;
        TextView tvProgresss;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.iv_cover);
            tvName = itemView.findViewById(R.id.tv_name);
            tvChapterName = itemView.findViewById(R.id.tv_chapter_name);
            tvProgresss = itemView.findViewById(R.id.tv_progresss);

        }
    }

    public void downloadNotifyDataSetChanged() {
        BookRepository.getInstance().getSession().getDownloadTaskBeanDao().detachAll();
        List<DownloadTaskBean> daoTaskBeanList = BookRepository.getInstance().getSession().getDownloadTaskBeanDao().queryBuilder().list();

        for (DownloadTaskBean daoTaskBean : daoTaskBeanList) {
            for (DownloadTaskBean downloadTaskBean : downloadTaskBeanList) {
                if (daoTaskBean.getTaskName().equals(downloadTaskBean.getTaskName())
                        && daoTaskBean.getCurrentChapter() != downloadTaskBean.getCurrentChapter()) {
                    downloadTaskBean.setCurrentChapter(daoTaskBean.getCurrentChapter());
                }
            }
        }



        notifyDataSetChanged();
    }
}
