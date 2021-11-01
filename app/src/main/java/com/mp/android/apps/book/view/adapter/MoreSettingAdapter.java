package com.mp.android.apps.book.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mp.android.apps.R;
import com.mp.android.apps.book.bean.BookMoreSettingBean;
import com.mp.android.apps.book.view.impl.MoreSettingActivity;

import java.util.List;


public class MoreSettingAdapter extends RecyclerView.Adapter<MoreSettingAdapter.ViewHolder> {
    private List<BookMoreSettingBean> source;
    private IMoreSettingClickListener moreSettingClickListener;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.mp_book_read_more_setting_item, parent, false));

    }

    public MoreSettingAdapter(List<BookMoreSettingBean> source, IMoreSettingClickListener moreSettingClickListener) {
        this.source = source;
        this.moreSettingClickListener = moreSettingClickListener;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position < source.size()) {
            holder.settingName.setText(source.get(position).settingNameStr);
            holder.aSwitch.setChecked(source.get(position).aSwitchBoolean);
            holder.aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    moreSettingClickListener.onclickListerer(source.get(position), isChecked);
                }
            });
        }
    }


    @Override
    public int getItemCount() {
        return source.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public TextView settingName;
        public Switch aSwitch;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            settingName = itemView.findViewById(R.id.mp_more_setting_name);
            aSwitch = itemView.findViewById(R.id.mp_more_setting_button);
        }
    }

    public interface IMoreSettingClickListener {
        void onclickListerer(BookMoreSettingBean bookMoreSettingBean, boolean isChecked);
    }
}
