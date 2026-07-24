package com.mp.android.apps.main;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.mp.android.apps.R;
import com.mp.android.apps.utils.ImmersiveUtils;


public class ManpinWXActivity extends AppCompatActivity implements View.OnClickListener {

    /** 作者微信号 */
    private static final String AUTHOR_WECHAT = "maxianer01";
    private static final String WEIXIN_PACKAGE = "com.tencent.mm";

    private TextView tvTitle;
    private ImageView ivBack;
    private Button btnWeixin;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImmersiveUtils.setLightStatusBar(this);
        setContentView(R.layout.activity_manpin_weixin);

        tvTitle = findViewById(R.id.tv_title);
        tvTitle.setText("联系作者");
        ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(this);

        btnWeixin = findViewById(R.id.btn_weixin01);
        btnWeixin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_weixin01) {
            copyAndJumpWeixin();
        } else if (id == R.id.iv_back) {
            finish();
        }
    }

    /**
     * 复制作者微信号到剪贴板，并跳转微信；用户可在「添加朋友」中粘贴搜索。
     */
    private void copyAndJumpWeixin() {
        copyToClipboard(AUTHOR_WECHAT);

        if (!isWeixinAvailable()) {
            Toast.makeText(this, "微信号已复制：" + AUTHOR_WECHAT + "，请手动打开微信添加作者", Toast.LENGTH_LONG).show();
            return;
        }

        Toast.makeText(this, "微信号已复制，正在跳转微信…", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isFinishing()) return;
                Intent intent = getPackageManager().getLaunchIntentForPackage(WEIXIN_PACKAGE);
                if (intent != null) {
                    startActivity(intent);
                } else {
                    Toast.makeText(ManpinWXActivity.this,
                            "无法打开微信，微信号已复制：" + AUTHOR_WECHAT, Toast.LENGTH_LONG).show();
                }
            }
        }, 800);
    }

    private void copyToClipboard(String text) {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) {
            cm.setPrimaryClip(ClipData.newPlainText("wechat", text));
        }
    }

    /**
     * 判断用户是否安装微信客户端
     */
    private boolean isWeixinAvailable() {
        try {
            getPackageManager().getPackageInfo(WEIXIN_PACKAGE, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
