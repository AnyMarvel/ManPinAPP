package com.mp.android.apps;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.google.android.libraries.social.licenses.LicenseMenuActivity;
import com.tencent.bugly.beta.Beta;

public class SettingAboutActivity extends StoryboardActivity implements View.OnClickListener {
    /**
     * 点击检测更新
     */
    private RelativeLayout checkUpgrade;
    /**
     * 跳转开源协议
     */
    private RelativeLayout openSourceLicense;
    /**
     * 关于界面显示图标
     */
    private ImageView iconImage;
    /**
     * title返回按钮
     */
    private ImageView titleBackSetting;
    private RelativeLayout kaiyuandizhi;
    private LinearLayout sinalayout;
    private RelativeLayout maillayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_about);
        initView();
        setListener();
    }

    /**
     * 初始化View
     */
    private void initView() {
        checkUpgrade = findViewById(R.id.checkupdate);
        openSourceLicense = findViewById(R.id.open_source_license);
        iconImage = findViewById(R.id.iconImage);
        titleBackSetting = findViewById(R.id.title_back_setting);
        kaiyuandizhi=findViewById(R.id.kaiyuandizhi);
        rectRoundBitmap();
    }

    /**
     * 初始化监听
     */
    private void setListener() {
        checkUpgrade.setOnClickListener(this);
        openSourceLicense.setOnClickListener(this);
        titleBackSetting.setOnClickListener(this);
        kaiyuandizhi.setOnClickListener(this);
//        sinalayout.setOnClickListener(this);
//        maillayout.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.checkupdate:
                Beta.checkUpgrade();
                break;
            case R.id.open_source_license:
                Intent intent = new Intent(this, LicenseMenuActivity.class);
                startActivity(intent);
                break;
            case R.id.title_back_setting:
                super.onBackPressed();
                break;
            case R.id.kaiyuandizhi:
                Intent intent1 = new Intent("android.intent.action.VIEW");
                intent1.setData(Uri.parse("https://github.com/AnyMarvel/ManPinAPP"));
                startActivity(intent1);
                break;
//            case R.id.sinalayout:
//                Intent intent1 = new Intent("android.intent.action.VIEW");
//                intent1.setData(Uri.parse("https://weibo.com/lijuntaosky/home"));
//                startActivity(intent1);
//                break;
//            case R.id.maillayout:
//                Intent data = new Intent(Intent.ACTION_SENDTO);
//                data.setData(Uri.parse("mailto:314599558@qq.com"));
//                data.putExtra(Intent.EXTRA_SUBJECT, "漫品客户端使用问题反馈");
//                data.putExtra(Intent.EXTRA_TEXT, getHandSetInfo());
//                startActivity(data);
//                break;
            default:
                break;

        }
    }


    /**
     * 设置显示图标圆角内容
     */
    private void rectRoundBitmap() {
        //得到资源文件的BitMap
        Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.launch);
        //创建RoundedBitmapDrawable对象
        RoundedBitmapDrawable roundImg = RoundedBitmapDrawableFactory.create(getResources(), image);
        //抗锯齿
        roundImg.setAntiAlias(true);
        //设置圆角半径
        roundImg.setCornerRadius(200);
        //设置显示图片
        iconImage.setImageDrawable(roundImg);
    }


}
