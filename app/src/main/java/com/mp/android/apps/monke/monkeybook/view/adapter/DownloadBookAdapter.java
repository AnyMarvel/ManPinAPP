package com.mp.android.apps.monke.monkeybook.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mp.android.apps.R;
import com.mp.android.apps.monke.monkeybook.bean.DownloadTaskBean;

import java.util.List;

public class DownloadBookAdapter extends RecyclerView.Adapter<DownloadBookAdapter.ViewHolder> {
    private Context context;
    private List<DownloadTaskBean> downloadTaskBeans;

    public DownloadBookAdapter(Context context, List<DownloadTaskBean> downloadTaskBeans) {
        this.context = context;
        this.downloadTaskBeans = downloadTaskBeans;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.view_pop_downloadlist, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 3;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvName;
        TextView tvChapterName;
        TextView tvCancel;
        TextView tvNone;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.iv_cover);
            tvName = itemView.findViewById(R.id.tv_name);
            tvChapterName = itemView.findViewById(R.id.tv_chapter_name);
            tvCancel = itemView.findViewById(R.id.tv_cancel);
            tvNone = itemView.findViewById(R.id.tv_none);

        }
    }
}
