package com.mp.android.apps.book.view.impl;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.mp.android.apps.R;
import com.mp.android.apps.utils.SpacesItemDecoration;
import com.mp.android.apps.basemvplib.impl.BaseActivity;
import com.mp.android.apps.book.bean.BookSourceBean;
import com.mp.android.apps.book.presenter.impl.BookSourcePresenterImpl;
import com.mp.android.apps.book.view.IBookSourceView;
import com.mp.android.apps.book.view.adapter.BookSourceAdapter;
import com.mp.android.apps.utils.AssertFileUtils;
import com.mp.android.apps.utils.SharedPreferenceUtil;

import java.util.List;

import butterknife.BindView;

public class BookSourceActivity extends BaseActivity<BookSourcePresenterImpl> implements IBookSourceView {

    @BindView(R.id.iv_back)
    ImageView ivBack;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.tv_right_btn)
    TextView tvRightBtn;
    @BindView(R.id.source_recyclerView)
    RecyclerView sourceRecyclerView;

    //    图书源数据源
    private String localBookSource;
    private List<BookSourceBean> sourceBeans;
    private BookSourceAdapter bookSourceAdapter;

    @Override
    protected BookSourcePresenterImpl initInjector() {
        return new BookSourcePresenterImpl();
    }

    @Override
    protected void onCreateActivity() {
        setContentView(R.layout.mp_book_source);
    }

    @Override
    protected void initData() {
        localBookSource = AssertFileUtils.getJson(this, "booksource.json");
        if (!TextUtils.isEmpty(localBookSource)) {
            sourceBeans = JSON.parseArray(localBookSource, BookSourceBean.class);
        }
    }


    @Override
    protected void bindView() {
        super.bindView();

        tvTitle.setText("我的书源");
        tvRightBtn.setText("全选");

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        //设置布局管理器
        sourceRecyclerView.setLayoutManager(layoutManager);
        //设置为垂直布局，这也是默认的
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        //设置分隔线
        sourceRecyclerView.addItemDecoration(new SpacesItemDecoration(15));
        //设置增加或删除条目的动画
        sourceRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }


    @Override
    protected void bindEvent() {
        super.bindEvent();
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BookSourceActivity.super.onBackPressed();
            }
        });
        tvRightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sourceBeans != null && sourceBeans.size() > 0) {
                    for (BookSourceBean bookSourceBean : sourceBeans) {
                        SharedPreferenceUtil.put(getContext(), bookSourceBean.getBookSourceAddress(), true);
                    }
                }
                if (sourceBeans != null) {
                    bookSourceAdapter.setSourceBeanList(mPresenter.handleSource(sourceBeans));
                    bookSourceAdapter.notifyDataSetChanged();
                }

            }
        });
    }

    @Override
    protected void firstRequest() {
        super.firstRequest();
        if (!TextUtils.isEmpty(localBookSource) && sourceBeans != null) {
            bookSourceAdapter = new BookSourceAdapter(mPresenter.handleSource(sourceBeans));
            sourceRecyclerView.setAdapter(bookSourceAdapter);
        }
    }

}
