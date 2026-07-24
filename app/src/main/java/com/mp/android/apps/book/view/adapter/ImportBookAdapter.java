
package com.mp.android.apps.book.view.adapter;

import android.content.Context;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mp.android.apps.R;
import com.mp.android.apps.book.widget.checkbox.SmoothCheckBox;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ImportBookAdapter extends RecyclerView.Adapter {
    private List<File> datas;
    private List<File> selectDatas;
    private Context context;

    public interface OnCheckBookListener {
        void checkBook(int count);
    }

    private OnCheckBookListener checkBookListener;

    public ImportBookAdapter(@NonNull OnCheckBookListener checkBookListener,Context context) {
        datas = new ArrayList<>();
        selectDatas = new ArrayList<>();
        this.context=context;
        this.checkBookListener = checkBookListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new Viewholder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_adapter_importbook, parent, false));
    }


    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final Viewholder vh = (Viewholder) holder;
        final File file = datas.get(position);
        vh.tvNmae.setText(file.getName());
        vh.tvSize.setText(convertByte(file.length()));
        vh.tvLoc.setText(file.getAbsolutePath().replace(Environment.getExternalStorageDirectory().getAbsolutePath(), "存储空间"));

        // 复用时恢复勾选态：先摘掉监听避免误触发，再按选中集合还原
        vh.scbSelect.setOnCheckedChangeListener(null);
        vh.scbSelect.setChecked(selectDatas.contains(file), false);
        vh.scbSelect.setOnCheckedChangeListener(new SmoothCheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SmoothCheckBox checkBox, boolean isChecked) {
                int pos = vh.getAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;
                File item = datas.get(pos);
                if (isChecked) {
                    if (!selectDatas.contains(item)) selectDatas.add(item);
                } else {
                    selectDatas.remove(item);
                }
                checkBookListener.checkBook(selectDatas.size());
            }
        });
        if (canCheck) {
            vh.scbSelect.setVisibility(View.VISIBLE);
            vh.llContent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    vh.scbSelect.setChecked(!vh.scbSelect.isChecked(), true);
                }
            });
        } else {
            vh.scbSelect.setVisibility(View.INVISIBLE);
            vh.llContent.setOnClickListener(null);
        }
    }

    public void addData(File newItem) {
        if (datas.contains(newItem)){
            Toast.makeText(context, "此图书已存在", Toast.LENGTH_SHORT).show();
        }else {
            datas.add(0,newItem);
            notifyItemInserted(0);
        }
    }

    public void addAllDatas(List<File> files) {
        datas.addAll(files);
        notifyDataSetChanged();
    }

    private Boolean canCheck = false;

    public void setCanCheck(Boolean canCheck) {
        this.canCheck = canCheck;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    class Viewholder extends RecyclerView.ViewHolder {
        LinearLayout llContent;
        TextView tvNmae;
        TextView tvSize;
        TextView tvLoc;
        SmoothCheckBox scbSelect;

        public Viewholder(View itemView) {
            super(itemView);
            llContent = (LinearLayout) itemView.findViewById(R.id.ll_content);
            tvNmae = (TextView) itemView.findViewById(R.id.tv_name);
            tvSize = (TextView) itemView.findViewById(R.id.tv_size);
            scbSelect = (SmoothCheckBox) itemView.findViewById(R.id.scb_select);
            tvLoc = (TextView) itemView.findViewById(R.id.tv_loc);
        }
    }


    public static String convertByte(long size) {
        DecimalFormat df = new DecimalFormat("###.#");
        if (size < 1024) {
            return df.format((double) size) + "B";
        } else if (size < 1024 * 1024) {
            return df.format(size / 1024.0) + "KB";
        } else if (size < 1024 * 1024 * 1024) {
            return df.format(size / (1024.0 * 1024)) + "MB";
        } else {
            return df.format(size / (1024.0 * 1024 * 1024)) + "GB";
        }
    }

    public List<File> getSelectDatas() {
        return selectDatas;
    }
}
