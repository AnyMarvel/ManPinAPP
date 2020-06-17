package com.mp.android.apps.main.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mp.android.apps.R;

public class ContentViewHolder extends RecyclerView.ViewHolder {
    public ImageView cardTitleImage;
    public ImageView cardFristImage;
    public ImageView cardTowImage;
    public ImageView cardThreeImage;
    public ImageView cardFourImage;

    public View cardBackground;

    public TextView cardTitle;
    public TextView cardBookName;
    public TextView cardBookbref;
    public TextView cardFristText;
    public TextView cardTowText;
    public TextView cardThreeText;
    public TextView cardFourText;

    public ContentViewHolder(@NonNull View itemView) {
        super(itemView);
        cardTitleImage = itemView.findViewById(R.id.cardTitleImage);
        cardFristImage = itemView.findViewById(R.id.cardFristImage);
        cardTowImage = itemView.findViewById(R.id.cardTowImage);
        cardThreeImage = itemView.findViewById(R.id.cardThreeImage);
        cardFourImage = itemView.findViewById(R.id.cardFourImage);
        cardBackground = itemView.findViewById(R.id.cardBackground);
        cardTitle = itemView.findViewById(R.id.cardTitle);
        cardBookName = itemView.findViewById(R.id.cardBookName);
        cardBookbref = itemView.findViewById(R.id.cardBookbref);
        cardFristText = itemView.findViewById(R.id.cardFristText);
        cardTowText = itemView.findViewById(R.id.cardTowText);
        cardThreeText = itemView.findViewById(R.id.cardThreeText);
        cardFourText = itemView.findViewById(R.id.cardFourText);
    }

}
