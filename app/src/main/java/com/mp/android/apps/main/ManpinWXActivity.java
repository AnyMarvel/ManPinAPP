package com.mp.android.apps.main;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.mp.android.apps.R;
import com.mp.android.apps.StoryboardActivity;
import com.mp.android.apps.login.LoginActivity;
import com.mp.android.apps.login.utils.LoginManager;
import com.mylhyl.acp.Acp;
import com.mylhyl.acp.AcpListener;
import com.mylhyl.acp.AcpOptions;
import com.xinlan.imageeditlibrary.editimage.utils.BitmapUtils;

import java.util.List;

public class ManpinWXActivity extends StoryboardActivity implements View.OnClickListener {
    TextView textView;
    Button button;
    ImageView iv_back;
    ImageView weixinImage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manpin_weixin);
        textView = findViewById(R.id.tv_title);
        textView.setText("微信的联系漫品小编");
        button = findViewById(R.id.manpin_save_open_weixin);
        button.setOnClickListener(this);
        iv_back = findViewById(R.id.iv_back);
        iv_back.setOnClickListener(this);
        weixinImage = findViewById(R.id.manpin_weixin_image);
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.manpin_save_open_weixin:
                Acp.getInstance(this).request(new AcpOptions.Builder()
                        .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE).build(), new AcpListener() {
                    @Override
                    public void onGranted() {
                        Bitmap bitmap = ((BitmapDrawable) weixinImage.getDrawable()).getBitmap();
                        String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() + "/manpin_" + System.currentTimeMillis() + ".png";
                        BitmapUtils.saveBitmap(bitmap, filePath);

                        if (isWeixinAvilible()) {
                            Toast.makeText(ManpinWXActivity.this, "二维码保存成功,请使用微信扫描添加好友", Toast.LENGTH_LONG).show();
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

                    @Override
                    public void onDenied(List<String> permissions) {

                    }
                });


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
