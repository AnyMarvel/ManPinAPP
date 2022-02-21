
package com.mp.android.apps.book.view.adapter;

import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mp.android.apps.R;
import com.mp.android.apps.book.widget.checkbox.SmoothCheckBox;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ImportBookAdapter extends RecyclerView.Adapter {
    private List<File> datas;
    private List<File> selectDatas;

    public interface OnCheckBookListener {
        void checkBook(int count);
        void manualClick();
    }

    private OnCheckBookListener checkBookListener;

    public ImportBookAdapter(@NonNull OnCheckBookListener checkBookListener) {
        datas = new ArrayList<>();
        selectDatas = new ArrayList<>();
        this.checkBookListener = checkBookListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == MANUAL_VIEW_TYPE){
            return new ManualHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_adapter_importbook_manual, parent, false));
        }else {
            return new Viewholder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_adapter_importbook, parent, false));
        }
    }

    private final int NORMAL_VIEW_TYPE=1;
    private final int MANUAL_VIEW_TYPE=2;
    @Override
    public int getItemViewType(int position) {
        if (position == datas.size()){
            return MANUAL_VIEW_TYPE;
        }else {
            return NORMAL_VIEW_TYPE;
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof ManualHolder){
            ((ManualHolder)holder).tv_scan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkBookListener.manualClick();
                }
            });
        }else {
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
        }}
    }

    public void addData(File newItem) {
        datas.add(newItem);
        notifyDataSetChanged();
    }

    public void setSystemFiles(List<File> files) {
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
        return datas.size()+1;
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

    class ManualHolder extends RecyclerView.ViewHolder{
        TextView tv_scan;
        public ManualHolder(@NonNull View itemView) {
            super(itemView);
            tv_scan = (TextView) itemView.findViewById(R.id.tv_scan);
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
