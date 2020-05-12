package com.mp.android.apps.livevblank;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.mp.android.apps.R;
import com.mp.android.apps.StoryboardActivity;
import com.mp.android.apps.livevblank.adapter.MySeachListAdapter;
import com.mp.android.apps.livevblank.view.MySearchView;

import java.util.ArrayList;
import java.util.List;

public class SearchPeopleActivity extends StoryboardActivity implements View.OnClickListener {
    private ImageView backImage;
    private TextView title;
    private TextView rightButton;
    private SharedPreferences sharedPreferences;
    private String tileName;
    private List<String> sourceData;

    private MySearchView mySearchView;
    private RecyclerView recyclerView;
    private MySeachListAdapter mySeachListAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.live_search_main);
        tileName = getIntent().getStringExtra("people");
        sharedPreferences = getSharedPreferences(tileName, MODE_PRIVATE);
        String tempSource = sharedPreferences.getString("sourceData", "");
        sourceData = JSON.parseArray(tempSource, String.class);
        if (sourceData == null) {
            sourceData = new ArrayList();
        }
        initView();

    }

    private void initView() {
        mySearchView = findViewById(R.id.sv_input);
        recyclerView = findViewById(R.id.rv_search);
        backImage = findViewById(R.id.iv_back);
        backImage.setOnClickListener(this);
        title = findViewById(R.id.tv_title);
        title.setText(tileName);
        rightButton = findViewById(R.id.tv_right_btn);
        rightButton.setText("确认");
        rightButton.setOnClickListener(this);
        if (sourceData != null && sourceData.size() > 0) {
            initRecyclerView();
            mySearchView.setEditText(sourceData.get(0));
            mySearchView.setSearchViewListener(new MySearchView.onSearchViewListener() {
                @Override
                public void onQueryTextChange(String text) {
                    if (TextUtils.isEmpty(text)) {
                        mySeachListAdapter.setData(sourceData);
                        mySeachListAdapter.notifyDataSetChanged();
                    }
                    List<String> tempList = searchItems(text, sourceData);
                    if (tempList != null && tempList.size() > 0) {
                        mySeachListAdapter.setData(tempList);
                        mySeachListAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
    }

    private void initRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mySeachListAdapter = new MySeachListAdapter(sourceData, this);
        mySeachListAdapter.setOnItemClickListener(new MySeachListAdapter.OnItemClickListener() {
            @Override
            public void onClick(String name) {
                mySearchView.setEditText(name);
            }

            @Override
            public void onLongClick(String name) {
                new AlertDialog.Builder(SearchPeopleActivity.this)
                        .setTitle("确认")
                        .setMessage("确定删除此联系人么？")
                        .setPositiveButton("是", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (sourceData.contains(name)) {
                                    sourceData.remove(name);
                                    pustSharedPreferences(sourceData);
                                    mySeachListAdapter.setData(sourceData);
                                    mySeachListAdapter.notifyDataSetChanged();
                                }
                            }
                        })
                        .setNegativeButton("否", null)
                        .show();
            }
        });
        recyclerView.setAdapter(mySeachListAdapter);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.iv_back:
                super.onBackPressed();
                break;
            case R.id.tv_right_btn:
                String tempName = mySearchView.getEditText();
                if (!TextUtils.isEmpty(tempName)) {
                    if (!sourceData.contains(tempName)) {
                        sourceData.add(0, tempName);
                        pustSharedPreferences(sourceData);
                    }
                    Intent intent = new Intent();
                    intent.putExtra("peopleName", tempName);
                    setResult(RESULT_OK, intent);
                }
                finish();
                break;
        }
    }

    public List<String> searchItems(String name, List<String> data) {
        ArrayList<String> mSearchList = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            int index = data.get(i).indexOf(name);
            // 存在匹配的数据
            if (index != -1) {
                mSearchList.add(data.get(i));
            }
        }
        return mSearchList;
    }

    private void pustSharedPreferences(List<String> sourceData) {
        String finalStr = JSON.toJSONString(sourceData);
        sharedPreferences.edit().putString("sourceData", finalStr).commit();
    }

}
