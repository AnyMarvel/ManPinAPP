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
import com.mp.android.apps.book.dao.DownloadTaskBeanDao;
import com.mp.android.apps.read.local.BookRepository;

import java.text.NumberFormat;
import java.util.List;

public class DownloadBookAdapter extends RecyclerView.Adapter<DownloadBookAdapter.ViewHolder> {
    /** 仅进度变化时的局部刷新标记，避免整项重绑与封面重载 */
    private static final Object PAYLOAD_PROGRESS = new Object();

    private Context context;

    private List<DownloadTaskBean> downloadTaskBeanList;
    private ItemClickListener itemClickListener;

    public DownloadBookAdapter(Context context, List<DownloadTaskBean> downloadTaskBeanList,ItemClickListener itemClickListener) {
        this.context = context;
        this.downloadTaskBeanList = downloadTaskBeanList;
        this.itemClickListener=itemClickListener;
    }

    public void removeDownloadBean(DownloadTaskBean downloadTaskBean){
        int index = downloadTaskBeanList.indexOf(downloadTaskBean);
        if (index < 0) return;
        downloadTaskBeanList.remove(index);
        notifyItemRemoved(index);
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.view_pop_downloadlist, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
        // 仅进度刷新：只更新章节名与进度，不重载封面，避免闪烁抖动
        if (!payloads.isEmpty() && payloads.contains(PAYLOAD_PROGRESS)) {
            DownloadTaskBean bean = downloadTaskBeanList.get(position);
            bindChapterName(holder, bean);
            bindProgress(holder, bean);
            return;
        }
        super.onBindViewHolder(holder, position, payloads);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final DownloadTaskBean downloadTaskBean = downloadTaskBeanList.get(position);
        Glide.with(context).load(downloadTaskBean.getCoverUrl())
                .placeholder(R.drawable.img_cover_default)
                .fallback(R.drawable.img_cover_default)
                .error(R.drawable.img_cover_default)
                .dontAnimate()
                .into(holder.ivCover);

        holder.tvName.setText(downloadTaskBean.getTaskName());
        bindChapterName(holder, downloadTaskBean);
        bindProgress(holder, downloadTaskBean);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                itemClickListener.onClick(downloadTaskBean);
            }
        });
    }

    /** 设置当前章节名，越界/无数据时清空，避免复用残留导致的错乱 */
    private void bindChapterName(ViewHolder holder, DownloadTaskBean bean) {
        int current = bean.getCurrentChapter();
        List<?> chapters = bean.getBookChapters();
        if (chapters != null && current >= 0 && current < chapters.size()) {
            holder.tvChapterName.setText(bean.getBookChapters().get(current).getTitle());
        } else {
            holder.tvChapterName.setText("");
        }
    }

    /** 设置下载进度百分比，分母为 0 时显示 0% */
    private void bindProgress(ViewHolder holder, DownloadTaskBean bean) {
        NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setMaximumFractionDigits(2);
        int last = bean.getLastChapter();
        float percent = last > 0 ? (float) bean.getCurrentChapter() / (float) last * 100 : 0f;
        if (percent > 100f) percent = 100f;
        holder.tvProgresss.setText(numberFormat.format(percent) + "%");
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
        DownloadTaskBeanDao dao = BookRepository.getInstance().getSession().getDownloadTaskBeanDao();
        dao.detachAll();
        List<DownloadTaskBean> daoTaskBeanList = dao.queryBuilder().list();

        // 只对进度实际发生变化的项做局部刷新，避免整表 notify 引起的封面重载与抖动
        for (int i = 0; i < downloadTaskBeanList.size(); i++) {
            DownloadTaskBean local = downloadTaskBeanList.get(i);
            for (DownloadTaskBean daoTaskBean : daoTaskBeanList) {
                if (daoTaskBean.getTaskName().equals(local.getTaskName())
                        && daoTaskBean.getCurrentChapter() != local.getCurrentChapter()) {
                    local.setCurrentChapter(daoTaskBean.getCurrentChapter());
                    notifyItemChanged(i, PAYLOAD_PROGRESS);
                    break;
                }
            }
        }
    }
}
