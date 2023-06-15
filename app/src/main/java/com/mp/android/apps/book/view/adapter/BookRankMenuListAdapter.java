package com.mp.android.apps.book.view.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mp.android.apps.R;
import com.mp.android.apps.book.bean.RankListMenubean;

import java.util.List;

public class BookRankMenuListAdapter extends RecyclerView.Adapter<BookRankMenuListAdapter.BookRankMenuViewHolder> {

    private List<RankListMenubean> sourceList;
    private int selectedIndex;
    private MenuItemClickListener menuItemClickListener;

    public BookRankMenuListAdapter(List<RankListMenubean> sourceList,MenuItemClickListener menuItemClickListener) {
        this.sourceList = sourceList;
        this.menuItemClickListener=menuItemClickListener;
    }

    @NonNull
    @Override
    public BookRankMenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rank_list_menu_item_layout, parent, false);
        return new BookRankMenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookRankMenuViewHolder holder, int position) {
        if (position < sourceList.size()) {
            holder.textView.setText(sourceList.get(position).name);
            holder.frameLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (position < sourceList.size()) {
                        selectedIndex = position;
                        notifyDataSetChanged();
                        menuItemClickListener.OnClick(sourceList.get(position));
                    }
                }
            });
            if (position == selectedIndex) {
                holder.frameLayout.setBackgroundColor(Color.GRAY);
            }else {
                holder.frameLayout.setBackgroundColor(Color.WHITE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return sourceList.size();
    }

    class BookRankMenuViewHolder extends RecyclerView.ViewHolder {
        public FrameLayout frameLayout;
        public TextView textView;

        public BookRankMenuViewHolder(@NonNull View itemView) {
            super(itemView);
            frameLayout = itemView.findViewById(R.id.rank_menu_layout);
            textView = itemView.findViewById(R.id.rank_name);
        }
    }




    public interface MenuItemClickListener{
        void OnClick(RankListMenubean menubean);
    }


}
