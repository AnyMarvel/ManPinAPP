package com.mp.android.apps.main.bookR.adapter.manholder;

import android.content.Context;
import android.graphics.Color;
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
import com.mp.android.apps.main.home.adapter.OnHomeAdapterClickListener;
import com.mp.android.apps.main.home.bean.HomeDesignBean;
import com.mp.android.apps.main.home.bean.SourceListContent;
import com.mp.android.apps.utils.RadiusUtils;

import java.util.List;

public class BookManContentHolder extends RecyclerView.ViewHolder {
    private TextView cardTitle;
    private ImageView changeCard;
    private ImageView recommendImage;
    private TextView recommendName;
    private TextView recommendDesc;
    private LinearLayout recommendLayout;

    private LinearLayout recommendLayout1;
    private ImageView recommendImage1;
    private TextView recommendBookName1;


    private LinearLayout recommendLayout2;
    private ImageView recommendImage2;
    private TextView recommendBookName2;

    private LinearLayout recommendLayout3;
    private ImageView recommendImage3;
    private TextView recommendBookName3;

    private LinearLayout recommendLayout4;
    private ImageView recommendImage4;
    private TextView recommendBookName4;

    private LinearLayout recommendLayout5;
    private ImageView recommendImage5;
    private TextView recommendBookName5;

    private LinearLayout recommendLayout6;
    private ImageView recommendImage6;
    private TextView recommendBookName6;

    private LinearLayout recommendLayout7;
    private ImageView recommendImage7;
    private TextView recommendBookName7;

    private LinearLayout recommendLayout8;
    private ImageView recommendImage8;
    private TextView recommendBookName8;

    public BookManContentHolder(@NonNull View itemView) {
        super(itemView);
        initView(itemView);
    }

    private void initView(View itemView) {
        cardTitle = itemView.findViewById(R.id.mp_book_man_content_cardTitle);
        changeCard = itemView.findViewById(R.id.mp_book_man_content_change);
        recommendImage = itemView.findViewById(R.id.mp_book_man_content_recommend_image);
        recommendName = itemView.findViewById(R.id.mp_book_man_content_recommend_book_name);
        recommendDesc = itemView.findViewById(R.id.mp_book_man_content_card_bref);
        recommendLayout = itemView.findViewById(R.id.mp_book_info_container);

        recommendLayout1 = itemView.findViewById(R.id.mp_book_man_content_Layout_1);
        recommendImage1 = itemView.findViewById(R.id.mp_book_man_content_Layout_1_image);
        recommendBookName1 = itemView.findViewById(R.id.mp_book_man_content_Layout_1_text);

        recommendLayout2 = itemView.findViewById(R.id.mp_book_man_content_Layout_2);
        recommendImage2 = itemView.findViewById(R.id.mp_book_man_content_Layout_2_image);
        recommendBookName2 = itemView.findViewById(R.id.mp_book_man_content_Layout_2_text);

        recommendLayout3 = itemView.findViewById(R.id.mp_book_man_content_Layout_3);
        recommendImage3 = itemView.findViewById(R.id.mp_book_man_content_Layout_3_image);
        recommendBookName3 = itemView.findViewById(R.id.mp_book_man_content_Layout_3_text);

        recommendLayout4 = itemView.findViewById(R.id.mp_book_man_content_Layout_4);
        recommendImage4 = itemView.findViewById(R.id.mp_book_man_content_Layout_4_image);
        recommendBookName4 = itemView.findViewById(R.id.mp_book_man_content_Layout_4_text);

        recommendLayout5 = itemView.findViewById(R.id.mp_book_man_content_Layout_5);
        recommendImage5 = itemView.findViewById(R.id.mp_book_man_content_Layout_5_image);
        recommendBookName5 = itemView.findViewById(R.id.mp_book_man_content_Layout_5_text);

        recommendLayout6 = itemView.findViewById(R.id.mp_book_man_content_Layout_6);
        recommendImage6 = itemView.findViewById(R.id.mp_book_man_content_Layout_6_image);
        recommendBookName6 = itemView.findViewById(R.id.mp_book_man_content_Layout_6_text);

        recommendLayout7 = itemView.findViewById(R.id.mp_book_man_content_Layout_7);
        recommendImage7 = itemView.findViewById(R.id.mp_book_man_content_Layout_7_image);
        recommendBookName7 = itemView.findViewById(R.id.mp_book_man_content_Layout_7_text);

        recommendLayout8 = itemView.findViewById(R.id.mp_book_man_content_Layout_8);
        recommendImage8 = itemView.findViewById(R.id.mp_book_man_content_Layout_8_image);
        recommendBookName8 = itemView.findViewById(R.id.mp_book_man_content_Layout_8_text);
    }

    public void handleBookManContent(Context context, List<HomeDesignBean> listContent, int mContentPosition, OnHomeAdapterClickListener listener) {
        if (listContent.size() > mContentPosition) {
            HomeDesignBean homeDesignBean = listContent.get(mContentPosition);
            List<SourceListContent> sourceContents = homeDesignBean.getSourceListContent();

            this.context = context;
            this.sourceContents = sourceContents;
            this.listener = listener;

            changeCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onContentChangeClickListener(mContentPosition, homeDesignBean.getKind());
                }
            });
            Glide.with(context).load(sourceContents.get(0).getCoverUrl())
                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(10))).into(recommendImage);
            cardTitle.setText(homeDesignBean.getKind());
            recommendName.setText(sourceContents.get(0).getName());
            recommendDesc.setText(sourceContents.get(0).getBookdesc());
            RadiusUtils.setClipViewCornerRadius(recommendLayout, 10);
            recommendLayout.setBackgroundColor(Color.parseColor(homeDesignBean.getCardColor()));
            recommendLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onLayoutClickListener(v, sourceContents.get(0));
                }
            });
            handleItemCard(recommendLayout1, recommendImage1, recommendBookName1, 1);
            handleItemCard(recommendLayout2, recommendImage2, recommendBookName2, 2);
            handleItemCard(recommendLayout3, recommendImage3, recommendBookName3, 3);
            handleItemCard(recommendLayout4, recommendImage4, recommendBookName4, 4);
            handleItemCard(recommendLayout5, recommendImage5, recommendBookName5, 5);
            handleItemCard(recommendLayout6, recommendImage6, recommendBookName6, 6);
            handleItemCard(recommendLayout7, recommendImage7, recommendBookName7, 7);
            handleItemCard(recommendLayout8, recommendImage8, recommendBookName8, 8);


        }
    }

    private List<SourceListContent> sourceContents;
    private Context context;
    private OnHomeAdapterClickListener listener;

    private void handleItemCard(LinearLayout linearLayout, ImageView imageView, TextView textView, int position) {
        textView.setText(sourceContents.get(position).getName());
        Glide.with(context).load(sourceContents.get(position).getCoverUrl())
                .apply(RequestOptions.bitmapTransform(new RoundedCorners(10))).into(imageView);
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onLayoutClickListener(v, sourceContents.get(position));
            }
        });
    }


}
