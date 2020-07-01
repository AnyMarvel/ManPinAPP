package com.mp.android.apps.main.bookR.adapter.recommendholder;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mp.android.apps.R;
import com.mp.android.apps.main.bookR.adapter.recommendholder.recommendContent.BookRRecommendContentAdapter;
import com.mp.android.apps.main.home.bean.SourceListContent;

import java.util.List;

/**
 * BookRRecommendListHolder简介
 *
 * @author lijuntao
 * @date 2020-07-01 16:52
 */
public class BookRRecommendListHolder extends RecyclerView.ViewHolder {
    private TextView textView;
    private RecyclerView recyclerView;
    private BookRRecommendContentAdapter listAdapter;
    private Context context;
    private List<SourceListContent> contentList;
    private BookRRecommendListener listener;

    public BookRRecommendListHolder(@NonNull View itemView) {
        super(itemView);
        textView = itemView.findViewById(R.id.mp_bookr_recommend_list_title);
        recyclerView = itemView.findViewById(R.id.mp_bookr_recommend_list);
    }


    public void handleClassicRecommendEvent(Context context, List<SourceListContent> contentList, BookRRecommendListener listener) {
        this.context = context;
        this.contentList = contentList;
        this.listener = listener;
        initRecyclerView();
    }

    private void initRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        listAdapter = new BookRRecommendContentAdapter(context, contentList, listener);
        recyclerView.setAdapter(listAdapter);
    }
}
