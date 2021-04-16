package com.mp.android.apps.monke.monkeybook.view.adapter.viewholder;

import android.view.View;
import android.widget.CompoundButton;
import android.widget.RatingBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mp.android.apps.R;
import com.mp.android.apps.monke.monkeybook.bean.BookSourceBean;
import com.mp.android.apps.utils.SharedPreferenceUtil;

public class BookSourceViewHolder extends RecyclerView.ViewHolder {
    TextView bookTitle;
    RatingBar bookSourceScore;
    TextView bookSourceAddress;
    Switch bookSourceSwitch;

    public BookSourceViewHolder(@NonNull View itemView) {
        super(itemView);
        bookTitle = itemView.findViewById(R.id.book_source_title);
        bookSourceScore = itemView.findViewById(R.id.book_source_score);
        bookSourceAddress = itemView.findViewById(R.id.book_source_address);
        bookSourceSwitch = itemView.findViewById(R.id.book_source_switch);
    }

    public void handleBookSourceView(BookSourceBean sourceBean) {
        bookTitle.setText(sourceBean.getBookTitle());
        bookSourceAddress.setText(sourceBean.getBookSourceAddress());
        bookSourceSwitch.setChecked(sourceBean.isBookSourceSwitch());
        bookSourceScore.setRating(Float.parseFloat(sourceBean.getBookSourceScore()));
        bookSourceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferenceUtil.put(itemView.getContext(), sourceBean.getBookSourceAddress(), isChecked);
            }
        });
    }


}
