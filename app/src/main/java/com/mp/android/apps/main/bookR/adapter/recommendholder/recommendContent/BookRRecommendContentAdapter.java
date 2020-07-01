package com.mp.android.apps.main.bookR.adapter.recommendholder.recommendContent;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mp.android.apps.R;
import com.mp.android.apps.main.bookR.adapter.recommendholder.BookRRecommendListener;
import com.mp.android.apps.main.home.bean.SourceListContent;

import java.util.List;

public class BookRRecommendContentAdapter extends RecyclerView.Adapter {

    private Context context;
    private List<SourceListContent> contentList;
    private BookRRecommendListener listener;

    public BookRRecommendContentAdapter(Context context, List<SourceListContent> contentList, BookRRecommendListener listener) {
        this.context = context;
        this.contentList = contentList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.mp_bookr_recommend_list_item, parent, false);
        return new BookRRecommendContentHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof BookRRecommendContentHolder){
            ((BookRRecommendContentHolder) holder).handleBookRRecommendContent(context, contentList, listener,position);
        }
    }

    @Override
    public int getItemCount() {
        return contentList.size();
    }


}
