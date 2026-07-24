package com.mp.android.apps.book.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mp.android.apps.R;
import com.mp.android.apps.book.utils.FileUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 崩溃日志列表适配器。每项展示崩溃时间与文件大小，点击"分享"回调交由页面处理。
 */
public class CrashLogAdapter extends RecyclerView.Adapter<CrashLogAdapter.ViewHolder> {

    public interface OnShareListener {
        void onShare(File file);
    }

    private final List<File> files;
    private final OnShareListener shareListener;
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    public CrashLogAdapter(List<File> files, OnShareListener shareListener) {
        this.files = files;
        this.shareListener = shareListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_crash_log, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final File file = files.get(position);
        holder.tvTime.setText(dateFormat.format(new Date(file.lastModified())));
        holder.tvSize.setText(FileUtil.readableFileSize(file.length()));

        View.OnClickListener share = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (shareListener != null) {
                    shareListener.onShare(file);
                }
            }
        };
        holder.tvShare.setOnClickListener(share);
        holder.itemView.setOnClickListener(share);
    }

    @Override
    public int getItemCount() {
        return files == null ? 0 : files.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime;
        TextView tvSize;
        TextView tvShare;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvSize = itemView.findViewById(R.id.tv_size);
            tvShare = itemView.findViewById(R.id.tv_share);
        }
    }
}
