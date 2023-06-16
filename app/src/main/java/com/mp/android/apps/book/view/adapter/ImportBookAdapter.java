
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
            ((Viewholder)holder).tvNmae.setText(datas.get(position).getName());
            ((Viewholder)holder).tvSize.setText(convertByte(datas.get(position).length()));
            ((Viewholder)holder).tvLoc.setText(datas.get(position).getAbsolutePath().replace(Environment.getExternalStorageDirectory().getAbsolutePath(), "存储空间"));

            ((Viewholder)holder).scbSelect.setOnCheckedChangeListener(new SmoothCheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SmoothCheckBox checkBox, boolean isChecked) {
                if (isChecked) {
                    selectDatas.add(datas.get(position));
                } else {
                    selectDatas.remove(datas.get(position));
                }
                checkBookListener.checkBook(selectDatas.size());
            }
        });
        if (canCheck) {
            ((Viewholder)holder).scbSelect.setVisibility(View.VISIBLE);
            ((Viewholder)holder).llContent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((Viewholder)holder).scbSelect.setChecked(!((Viewholder)holder).scbSelect.isChecked(), true);
                }
            });
        } else {
            ((Viewholder)holder).scbSelect.setVisibility(View.INVISIBLE);
            ((Viewholder)holder).llContent.setOnClickListener(null);
        }
    }

    public void addData(File newItem) {
        if (datas.contains(newItem)){
            Toast.makeText(context, "此图书已存在", Toast.LENGTH_SHORT).show();
        }else {
            datas.add(0,newItem);
            notifyItemChanged(0);
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
        float f;
        if (size < 1024) {
            f = size / 1.0f;
            return (df.format(new Float(f).doubleValue()) + "B");
        } else if (size < 1024 * 1024) {
            f = (float) ((float) size / (float) 1024);
            return (df.format(new Float(f).doubleValue()) + "KB");
        } else if (size < 1024 * 1024 * 1024) {
            f = (float) ((float) size / (float) (1024 * 1024));
            return (df.format(new Float(f).doubleValue()) + "MB");
        } else {
            f = (float) ((float) size / (float) (1024 * 1024 * 1024));
            return (df.format(new Float(f).doubleValue()) + "GB");
        }
    }

    public List<File> getSelectDatas() {
        return selectDatas;
    }
}
