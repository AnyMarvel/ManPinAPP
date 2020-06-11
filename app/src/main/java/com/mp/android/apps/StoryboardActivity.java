package com.mp.android.apps;

import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
//import com.google.android.apps.photolab.storyboard.telemetry.Telemetry;
//import com.google.firebase.analytics.FirebaseAnalytics.Event;
import com.umeng.socialize.UMAuthListener;
import com.umeng.socialize.bean.SHARE_MEDIA;

import java.util.Map;
import java.util.UUID;


public class StoryboardActivity extends AppCompatActivity {
    public static final int PICK_VIDEO_REQUEST = 12321;
    private static final String TAG = "StoryboardActivity";
    public static final String LOGIN_SP_STATE = "logininfomation";

    public StoryboardActivity() {
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        UnStatusBarTintUtil();
    }

    /**
     * 获取应用版本信息
     *
     * @return 返回应用版本信息
     */
    public String getAppVersionString() {
        String result = "0.0.0";
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            Log.i(TAG, "Unable to read version ID from Manifest.");
            return result;
        }
    }

    /**
     * 打开相册应用
     */
    public void selectVideo() {
        Intent intent = new Intent("android.intent.action.PICK");
        intent.setType("video/*");
        intent.setAction("android.intent.action.GET_CONTENT");
        startActivityForResult(Intent.createChooser(intent, "Select Video"), PICK_VIDEO_REQUEST);
    }

    /**
     * 调用系统相册
     *
     * @param requestCode
     */
    public void selectImage(int requestCode) {
        Intent intent = new Intent("android.intent.action.PICK");
        intent.setType("image/*");
        intent.setAction("android.intent.action.GET_CONTENT");
        startActivityForResult(Intent.createChooser(intent, "Select Video"), requestCode);
    }

    /**
     * 获取手机基本信息
     */

    public String getHandSetInfo() {
        return "手机型号:" + android.os.Build.MODEL +
                ",SDK版本:" + android.os.Build.VERSION.SDK +
                ",系统版本:" + android.os.Build.VERSION.RELEASE +
                ",软件版本:" + getAppVersionString();
    }

    /**
     * 沉浸式。去除状态栏
     */
    private void UnStatusBarTintUtil() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);  //设置状态栏背景颜色
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                View decorView = getWindow().getDecorView();
                if (decorView != null) {   //白色背景要设置暗色系的状态栏图标
                    int vis = decorView.getSystemUiVisibility();
                    vis |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                    decorView.setSystemUiVisibility(vis);
                }
            }
        }
    }

}
