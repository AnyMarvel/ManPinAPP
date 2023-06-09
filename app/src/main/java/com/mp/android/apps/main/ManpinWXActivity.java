package com.mp.android.apps.main;


import android.content.ClipData;
import android.content.ClipboardManager;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.mp.android.apps.R;
import com.mp.android.apps.StoryboardActivity;
import java.util.List;


public class ManpinWXActivity extends StoryboardActivity implements View.OnClickListener {
    TextView textView;
    ImageView iv_back;

    Button weixin01;
    Button weixin02;
    Button btnGZH;
    Button btnEmail;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manpin_weixin);
        textView = findViewById(R.id.tv_title);
        textView.setText("联系作者");
        iv_back = findViewById(R.id.iv_back);
        iv_back.setOnClickListener(this);
        initView();
    }

    private void initView() {
        weixin01 = findViewById(R.id.btn_weixin01);
        weixin01.setOnClickListener(this);
        weixin02 = findViewById(R.id.btn_weixin02);
        weixin02.setOnClickListener(this);
        btnGZH = findViewById(R.id.btn_gongzhonghao);
        btnGZH.setOnClickListener(this);
        btnEmail = findViewById(R.id.btn_email);
        btnEmail.setOnClickListener(this);
    }

    private void gotoWX(String text,boolean wx) {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
// 创建普通字符型ClipData
        ClipData mClipData = ClipData.newPlainText("Label", text);
// 将ClipData内容放到系统剪贴板里。
        cm.setPrimaryClip(mClipData);
        if (wx){
            if (isWeixinAvilible()) {
                Toast.makeText(ManpinWXActivity.this, text + "已经复制到粘贴板，正在跳转WX中", Toast.LENGTH_LONG).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = getPackageManager().getLaunchIntentForPackage("com.tencent.mm");
                        startActivity(intent);
                    }
                }, 1000);

            } else {
                Toast.makeText(ManpinWXActivity.this, "微信未安装,请使用微博,意见反馈等联系的作者", Toast.LENGTH_LONG).show();
            }
        }

    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btn_weixin01:
                gotoWX("maxianer01",true);
                break;
            case R.id.btn_weixin02:
                gotoWX("kongkong7119",true);
                break;
            case R.id.btn_gongzhonghao:
                gotoWX("码仙儿",true);
                break;
            case R.id.btn_email:
                gotoWX("ljtdss5233@gmail.com",false);
                break;
            case R.id.iv_back:
                finish();
                break;
            default:
                break;
        }
    }


    /**
     * 判断 用户是否安装微信客户端
     */
    private boolean isWeixinAvilible() {
        final PackageManager packageManager = getPackageManager();// 获取packagemanager
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);// 获取所有已安装程序的包信息
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                if (pn.equals("com.tencent.mm")) {
                    return true;
                }
            }
        }
        return false;
    }
}
