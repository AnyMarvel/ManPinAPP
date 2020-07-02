package com.mp.android.apps.main.bookR.adapter.recommendholder;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.mp.android.apps.R;
import com.mp.android.apps.main.bookR.adapter.recommendholder.BookRRecommendListener;
import com.mp.android.apps.main.home.bean.SourceListContent;

import java.util.List;

public class BookRRecommendListHolder extends RecyclerView.ViewHolder {

    private ImageView itemImage;
    private TextView bookName;
    private TextView bookDesc;
    private TextView bookAuthor;
    private TextView bookCategory;
    private LinearLayout linearLayout;

    public BookRRecommendListHolder(@NonNull View itemView) {
        super(itemView);
        itemImage = itemView.findViewById(R.id.mp_bookr_recommend_list_item_image);
        bookName = itemView.findViewById(R.id.mp_bookr_recommend_list_item_bookname);
        bookDesc = itemView.findViewById(R.id.mp_bookr_recommend_list_item_bookdesc);
        bookAuthor = itemView.findViewById(R.id.mp_bookr_recommend_list_item_bookauthor);
        bookCategory = itemView.findViewById(R.id.mp_bookr_recommend_list_item_bookcategory);
        linearLayout = itemView.findViewById(R.id.mp_bookr_recommend_list_item_layout);
    }

    public void handleBookRRecommendContent(Context context, List<SourceListContent> contentList
            , BookRRecommendListener listener, int position) {
        if (contentList.size() > 0 && contentList.size() > position && contentList.get(position) != null) {
            Glide.with(context).load(contentList.get(position).getCoverUrl())
                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(10))).into(itemImage);
            bookName.setText(contentList.get(position).getName());
            bookDesc.setText(contentList.get(position).getBookdesc());
            bookAuthor.setText(contentList.get(position).getAuthor());
            bookCategory.setText(contentList.get(position).getKind());
            linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onLayoutClickListener(v, contentList.get(position));
                }
            });
        }
    }


}
