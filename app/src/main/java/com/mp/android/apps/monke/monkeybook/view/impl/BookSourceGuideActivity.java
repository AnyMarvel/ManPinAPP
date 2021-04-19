package com.mp.android.apps.monke.monkeybook.view.impl;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.alibaba.fastjson.JSON;
import com.mp.android.apps.R;
import com.mp.android.apps.StoryboardActivity;
import com.mp.android.apps.monke.monkeybook.bean.BookSourceBean;
import com.mp.android.apps.utils.AssertFileUtils;

import java.util.List;

public class BookSourceGuideActivity extends StoryboardActivity implements View.OnClickListener {
    private ImageView ivBack;
    private TextView title;
    private TextView bookLocal;
    private TextView bookSource;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.book_source_guide_layout);
        ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(this);
        title = findViewById(R.id.tv_title);
        title.setText("准备阅读");
        bookLocal = findViewById(R.id.book_guide_import_local);
        bookLocal.setOnClickListener(this);
        bookSource = findViewById(R.id.book_guide_import_source);
        bookSource.setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.iv_back:
                super.onBackPressed();
                break;
            case R.id.book_guide_import_local:
                Intent intent = new Intent(BookSourceGuideActivity.this, ImportBookActivity.class);
                startActivity(intent);
                break;
            case R.id.book_guide_import_source:
                Intent intent1 = new Intent(BookSourceGuideActivity.this, BookSourceActivity.class);
                startActivity(intent1);
                break;
        }
    }
}
