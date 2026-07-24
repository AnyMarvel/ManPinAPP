package com.mp.android.apps.book.view.impl;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mp.android.apps.BuildConfig;
import com.mp.android.apps.R;
import com.mp.android.apps.book.utils.FileUtil;
import com.mp.android.apps.book.view.adapter.CrashLogAdapter;
import com.mp.android.apps.utils.crash.CrashHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 崩溃日志列表页：展示本地记录的崩溃日志，支持分享给作者、一键清空。
 */
public class CrashLogActivity extends AppCompatActivity {

    private TextView tvTitle;
    private ImageView ivBack;
    private TextView tvRightBtn;
    private RecyclerView rvCrashLog;
    private TextView tvEmpty;

    private final List<File> files = new ArrayList<>();
    private CrashLogAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setImmersiveStatusBar();
        setContentView(R.layout.activity_crash_log);

        tvTitle = findViewById(R.id.tv_title);
        tvTitle.setText("崩溃日志");
        ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(v -> finish());
        tvRightBtn = findViewById(R.id.tv_right_btn);
        tvRightBtn.setText("清空");
        tvRightBtn.setOnClickListener(v -> confirmClear());

        tvEmpty = findViewById(R.id.tv_empty);
        rvCrashLog = findViewById(R.id.rv_crash_log);
        rvCrashLog.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CrashLogAdapter(files, this::shareFile);
        rvCrashLog.setAdapter(adapter);

        //调试专用：长按标题主动制造一次崩溃，用于验证崩溃日志的记录/展示/分享；Release 包不生效
        if (BuildConfig.DEBUG) {
            Toast.makeText(this, "调试模式：长按标题可制造测试崩溃", Toast.LENGTH_SHORT).show();
            tvTitle.setOnLongClickListener(v -> {
                throw new RuntimeException("测试崩溃：来自崩溃日志页的手动触发 @" + System.currentTimeMillis());
            });
        }
    }

    /**
     * 沉浸式状态栏：状态栏透明、内容延伸到其下方（根布局 fitsSystemWindows=true 自动让出状态栏高度）；
     * 页面为浅色底，用 LIGHT_STATUS_BAR 让状态栏图标变深色可见。
     */
    private void setImmersiveStatusBar() {
        android.view.Window window = getWindow();
        window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        }
        window.getDecorView().setSystemUiVisibility(flags);
        window.setStatusBarColor(android.graphics.Color.TRANSPARENT);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        files.clear();
        Collections.addAll(files, CrashHandler.listCrashFiles(this));
        adapter.notifyDataSetChanged();

        boolean empty = files.isEmpty();
        tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvCrashLog.setVisibility(empty ? View.GONE : View.VISIBLE);
        tvRightBtn.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void shareFile(File file) {
        if (file == null || !file.exists()) {
            Toast.makeText(this, "日志文件不存在", Toast.LENGTH_SHORT).show();
            loadData();
            return;
        }
        try {
            Uri uri = FileUtil.getUriFromFile(this, file);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.putExtra(Intent.EXTRA_SUBJECT, "码仙儿App崩溃日志");
            intent.putExtra(Intent.EXTRA_TEXT, "这是App崩溃日志，麻烦作者帮忙看一下～");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "分享崩溃日志给作者"));
        } catch (Exception e) {
            android.util.Log.e("CrashLogActivity", "share failed", e);
            Toast.makeText(this, "分享失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmClear() {
        if (files.isEmpty()) {
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("清空崩溃日志")
                .setMessage("确定删除全部崩溃日志吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    CrashHandler.clearAll(this);
                    loadData();
                })
                .setNegativeButton("取消", null)
                .show();
    }
}
