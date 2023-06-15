package com.mp.android.apps.book.view.adapter;

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
import com.mp.android.apps.book.bean.DownloadTaskBean;
import com.mp.android.apps.readActivity.local.BookRepository;

import java.text.NumberFormat;
import java.util.List;

public class DownloadBookAdapter extends RecyclerView.Adapter<DownloadBookAdapter.ViewHolder> {
    private Context context;

    private List<DownloadTaskBean> downloadTaskBeanList;
    private ItemClickListener itemClickListener;

    public DownloadBookAdapter(Context context, List<DownloadTaskBean> downloadTaskBeanList,ItemClickListener itemClickListener) {
        this.context = context;
        this.downloadTaskBeanList = downloadTaskBeanList;
        this.itemClickListener=itemClickListener;
    }

    public void removeDownloadBean(DownloadTaskBean downloadTaskBean){
        downloadTaskBeanList.remove(downloadTaskBean);
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.view_pop_downloadlist, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DownloadTaskBean downloadTaskBean = downloadTaskBeanList.get(position);
        Glide.with(context).load(downloadTaskBean.getCoverUrl())
                .placeholder(R.drawable.img_cover_default)
                .fallback(R.drawable.img_cover_default)
                .error(R.drawable.img_cover_default)
                .into(holder.ivCover);

        holder.tvName.setText(downloadTaskBean.getTaskName());
        if (downloadTaskBean.getBookChapters().size() > downloadTaskBean.getCurrentChapter())
            holder.tvChapterName.setText(downloadTaskBean.getBookChapters().get(downloadTaskBean.getCurrentChapter()).getTitle());

        // 创建一个数值格式化对象
        NumberFormat numberFormat = NumberFormat.getInstance();
        // 设置精确到小数点后2位
        numberFormat.setMaximumFractionDigits(2);
        String result = numberFormat.format((float) downloadTaskBean.getCurrentChapter() / (float) downloadTaskBean.getLastChapter() * 100) + "%";
        holder.tvProgresss.setText(result);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                itemClickListener.onClick(downloadTaskBean);
            }
        });
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

    public interface ItemClickListener{
      void  onClick(DownloadTaskBean downloadTaskBean);
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
